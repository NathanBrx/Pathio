package fds.hai811i.pathio;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessaging;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import fds.hai811i.pathio.databinding.ActivityMainBinding;
import fds.hai811i.pathio.utils.RetrofitClient;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private CardView[] navBackgrounds;
    private android.widget.ImageView[] navIcons;
    private final int COLOR_ACTIVE_BG = Color.parseColor("#B84B25");
    private final int COLOR_INACTIVE_BG = Color.TRANSPARENT;
    private final int COLOR_ACTIVE_ICON = Color.WHITE;
    private final int COLOR_INACTIVE_ICON = Color.parseColor("#4A5568");
    private final Fragment homeFragment = new HomeFragment();
    private final Fragment newPathFragment = new NewPathFragment();
    private final Fragment mapFragment = new MapFragment();
    private final Fragment galleryFragment = new GalleryFragment();
    private final Fragment profileFragment = new ProfileFragment();
    private final FragmentManager fm = getSupportFragmentManager();
    private Fragment activeFragment = homeFragment;

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    fetchAndSyncFcmToken();
                } else {
                    Log.w("FCM", "Permission de notification refusée par l'utilisateur.");
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        navBackgrounds = new CardView[]{
                binding.bgAdd,
                binding.bgMap,
                binding.bgHome,
                binding.bgGallery,
                binding.bgProfile
        };

        navIcons = new android.widget.ImageView[]{
                binding.iconAdd,
                binding.iconMap,
                binding.iconHome,
                binding.iconGallery,
                binding.iconProfile
        };

        if (savedInstanceState == null) {
            fm.beginTransaction().add(R.id.fragmentContainer, profileFragment, "5").hide(profileFragment).commit();
            fm.beginTransaction().add(R.id.fragmentContainer, galleryFragment, "4").hide(galleryFragment).commit();
            fm.beginTransaction().add(R.id.fragmentContainer, mapFragment, "2").hide(mapFragment).commit();
            fm.beginTransaction().add(R.id.fragmentContainer, newPathFragment, "1").hide(newPathFragment).commit();
            fm.beginTransaction().add(R.id.fragmentContainer, homeFragment, "3").commit();

            updateNavUI(2);
        }

        binding.navAdd.setOnClickListener(v -> navigateTo(newPathFragment, 0));
        binding.navMap.setOnClickListener(v -> navigateTo(mapFragment, 1));
        binding.navHome.setOnClickListener(v -> navigateTo(homeFragment, 2));
        binding.navGallery.setOnClickListener(v -> navigateTo(galleryFragment, 3));
        binding.navProfile.setOnClickListener(v -> navigateTo(profileFragment, 4));

        setupNotificationsIfLoggedIn();
    }

    public void setupNotificationsIfLoggedIn() {
        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        String token = prefs.getString("jwt_token", null);

        if (token != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
                    fetchAndSyncFcmToken();
                } else {
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
                }
            } else {
                fetchAndSyncFcmToken();
            }
        }
    }

    private void fetchAndSyncFcmToken() {
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Log.w("FCM", "Fetching FCM registration token failed", task.getException());
                        return;
                    }

                    // Get new FCM registration token
                    String fcmToken = task.getResult();
                    Log.d("FCM", "Token obtenu : " + fcmToken);

                    // Save locally just in case
                    getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)
                            .edit().putString("fcm_token", fcmToken).apply();

                    // Send to Node.js backend
                    String json = "{\"fcm_token\":\"" + fcmToken + "\"}";
                    RequestBody body = RequestBody.create(json, MediaType.parse("application/json; charset=utf-8"));

                    RetrofitClient.getApi(this).updateFcmToken(body).enqueue(new Callback<>() {
                        @Override
                        public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                            if(response.isSuccessful()) Log.d("FCM", "Token synchronisé avec succès !");
                        }
                        @Override
                        public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                            Log.e("FCM", "Erreur de synchro du token : " + t.getMessage());
                        }
                    });
                });
    }

    private void updateNavUI(int selectedIndex) {
        for (int i = 0; i < navBackgrounds.length; i++) {
            if (i == selectedIndex) {
                navBackgrounds[i].setCardBackgroundColor(COLOR_ACTIVE_BG);
                navIcons[i].setColorFilter(COLOR_ACTIVE_ICON);
            } else {
                navBackgrounds[i].setCardBackgroundColor(COLOR_INACTIVE_BG);
                navIcons[i].setColorFilter(COLOR_INACTIVE_ICON);
            }
        }
    }

    public void navigateTo(Fragment targetFragment, int navIndex) {
        if (activeFragment == targetFragment) return;

        updateNavUI(navIndex);

        androidx.fragment.app.FragmentTransaction transaction = fm.beginTransaction()
                .setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.fade_out)
                .hide(activeFragment);

        if (!targetFragment.isAdded()) {
            transaction.add(R.id.fragmentContainer, targetFragment);
        } else {
            transaction.show(targetFragment);
        }

        transaction.commit();
        activeFragment = targetFragment;
    }

    /**
     * Getter for already loaded fragments. Used in "back_buttons"
     * @param fragmentClass Class of the fragment to return to
     * @return The correct loaded fragment
     * @param <T> One of the fragment classes of the app
     */
    public <T extends Fragment> T getExistingFragment(Class<T> fragmentClass) {
        for (Fragment fragment : fm.getFragments()) {
            if (fragmentClass.isInstance(fragment)) {
                return fragmentClass.cast(fragment);
            }
        }
        return null;
    }
}