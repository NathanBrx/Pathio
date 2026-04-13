package fds.hai811i.pathio;

import android.graphics.Color;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import fds.hai811i.pathio.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private CardView[] navBackgrounds;
    private android.widget.ImageView[] navIcons;
    private final int COLOR_ACTIVE_BG = Color.parseColor("#B84B25");
    private final int COLOR_INACTIVE_BG = Color.TRANSPARENT;
    private final int COLOR_ACTIVE_ICON = Color.WHITE;
    private final int COLOR_INACTIVE_ICON = Color.parseColor("#4A5568");

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
            loadFragment(new HomeFragment());
            updateNavUI(2);
        }

        binding.navAdd.setOnClickListener(v -> navigateTo(new NewPathFragment(), 0));
        binding.navMap.setOnClickListener(v -> navigateTo(new MapFragment(), 1));
        binding.navHome.setOnClickListener(v -> navigateTo(new HomeFragment(), 2));
        binding.navGallery.setOnClickListener(v -> updateNavUI(3));
        binding.navProfile.setOnClickListener(v -> navigateTo(new ProfileFragment(), 4));
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

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit();
    }

    public void navigateTo(Fragment fragment, int navIndex) {
        updateNavUI(navIndex);
        loadFragment(fragment);
    }
}