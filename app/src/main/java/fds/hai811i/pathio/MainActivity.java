package fds.hai811i.pathio;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

public class MainActivity extends AppCompatActivity {
    private CardView[] navBackgrounds;
    private ImageView[] navIcons;
    private final int COLOR_ACTIVE_BG = Color.parseColor("#B84B25");
    private final int COLOR_INACTIVE_BG = Color.TRANSPARENT;
    private final int COLOR_ACTIVE_ICON = Color.WHITE;
    private final int COLOR_INACTIVE_ICON = Color.parseColor("#4A5568");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FrameLayout navAdd = findViewById(R.id.navAdd);
        FrameLayout navMap = findViewById(R.id.navMap);
        FrameLayout navHome = findViewById(R.id.navHome);
        FrameLayout navGallery = findViewById(R.id.navGallery);
        FrameLayout navProfile = findViewById(R.id.navProfile);

        navBackgrounds = new CardView[]{
                findViewById(R.id.bgAdd),
                findViewById(R.id.bgMap),
                findViewById(R.id.bgHome),
                findViewById(R.id.bgGallery),
                findViewById(R.id.bgProfile)
        };

        navIcons = new ImageView[]{
                findViewById(R.id.iconAdd),
                findViewById(R.id.iconMap),
                findViewById(R.id.iconHome),
                findViewById(R.id.iconGallery),
                findViewById(R.id.iconProfile)
        };

        if (savedInstanceState == null) {
            loadFragment(new HomeFragment());
            updateNavUI(2);
        }

        navAdd.setOnClickListener(v -> {
            updateNavUI(0);
            loadFragment(new NewPathFragment());
        });

        navMap.setOnClickListener(v -> {
            updateNavUI(1);
            // loadFragment(new MapFragment());
        });

        navHome.setOnClickListener(v -> {
            updateNavUI(2);
            loadFragment(new HomeFragment());
        });

        navGallery.setOnClickListener(v -> {
            updateNavUI(3);
            // loadFragment(new GalleryFragment());
        });

        navProfile.setOnClickListener(v -> {
            updateNavUI(4);
            // loadFragment(new ProfileFragment());
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

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit();
    }
}