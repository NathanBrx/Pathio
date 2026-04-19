package fds.hai811i.pathio;

import android.graphics.Color;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import fds.hai811i.pathio.databinding.ActivityMainBinding;

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

        fm.beginTransaction()
                .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                .hide(activeFragment)
                .show(targetFragment)
                .commit();

        activeFragment = targetFragment;
    }
}