package fds.hai811i.pathio;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.google.android.material.slider.RangeSlider;

import java.util.Locale;

public class NewPathFragment extends Fragment {
    private final int COLOR_INACTIVE_TEXT = Color.parseColor("#000000");
    private final int COLOR_WHITE = Color.parseColor("#FFFFFF");
    private final int COLOR_ACTIVE_ORANGE = Color.parseColor("#D45D3B");
    private final int COLOR_ACTIVE_DARK = Color.parseColor("#2D3142");
    RangeSlider budget;
    TextView currentMinBudget, currentMaxBudget;

    public NewPathFragment(){}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_new_path, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initConstraintToggle(view, R.id.cardCulture, R.id.textCulture, 0, 0, COLOR_ACTIVE_ORANGE);
        initConstraintToggle(view, R.id.cardLoisirs, R.id.textLoisirs, 0, 0, COLOR_ACTIVE_ORANGE);
        initConstraintToggle(view, R.id.cardFood, R.id.textFood, 0, 0, COLOR_ACTIVE_ORANGE);
        initConstraintToggle(view, R.id.cardDiscover, R.id.textDiscover, R.id.iconDiscover, R.id.circleDiscover, COLOR_ACTIVE_ORANGE);

        initConstraintToggle(view, R.id.card2Hours, R.id.text2Hours, 0, 0, COLOR_ACTIVE_DARK);
        initConstraintToggle(view, R.id.cardHalfDay, R.id.textHalfDay, 0, 0, COLOR_ACTIVE_DARK);
        initConstraintToggle(view, R.id.cardFullDay, R.id.textFullDay, 0, 0, COLOR_ACTIVE_DARK);

        initConstraintToggle(view, R.id.cardEffortFaible, R.id.textEffortFaible, 0, 0, COLOR_ACTIVE_DARK);
        initConstraintToggle(view, R.id.cardEffortModere, R.id.textEffortModere, 0, 0, COLOR_ACTIVE_DARK);
        initConstraintToggle(view, R.id.cardEffortEleve, R.id.textEffortEleve, 0, 0, COLOR_ACTIVE_DARK);

        initConstraintToggle(view, R.id.cardOldPeople, R.id.textOldPeople, R.id.iconOldPeople, 0, COLOR_ACTIVE_DARK);
        initConstraintToggle(view, R.id.cardEnfant, R.id.textEnfant, R.id.iconEnfant, 0, COLOR_ACTIVE_DARK);
        initConstraintToggle(view, R.id.cardPMAPMR, R.id.textPMAPMR, R.id.iconPMAPMR, 0, COLOR_ACTIVE_DARK);
        initConstraintToggle(view, R.id.cardFroid, R.id.textFroid, R.id.iconFroid, 0, COLOR_ACTIVE_DARK);
        initConstraintToggle(view, R.id.cardChaud, R.id.textChaud, R.id.iconChaud, 0, COLOR_ACTIVE_DARK);
        initConstraintToggle(view, R.id.cardWater, R.id.textWater, R.id.iconWater, 0, COLOR_ACTIVE_DARK);

        currentMinBudget = view.findViewById(R.id.currentMinBudget);
        currentMaxBudget = view.findViewById(R.id.currentMaxBudget);

        budget = view.findViewById(R.id.rangeSliderBudget);
        budget.setValues(150f, 450f);
        updateBudgetText(150, 450);

        budget.addOnChangeListener((slider, value, fromUser) -> {
            int minVal = Math.round(slider.getValues().get(0));
            int maxVal = Math.round(slider.getValues().get(1));

            updateBudgetText(minVal, maxVal);
        });
    }

    private void initConstraintToggle(View view, int cardId, int textId, int iconId, int circleId, int activeColor) {
        CardView card = view.findViewById(cardId);
        TextView text = view.findViewById(textId);
        ImageView icon = (iconId != 0) ? view.findViewById(iconId) : null;
        CardView circle = (circleId != 0) ? view.findViewById(circleId) : null;

        if (card != null && text != null) {
            setupToggleSelection(card, text, icon, circle,activeColor);
        }
    }

    private void setupToggleSelection(CardView card, TextView text, @Nullable ImageView icon, @Nullable CardView circle, int activeColorBg) {
        card.setOnClickListener(v -> {
            boolean isNowSelected = !v.isSelected();
            v.setSelected(isNowSelected);

            if (isNowSelected) {
                card.setCardBackgroundColor(activeColorBg);
                text.setTextColor(COLOR_WHITE);
                if (icon != null) icon.setColorFilter(COLOR_WHITE);
                if (circle != null) circle.setCardBackgroundColor(Color.parseColor("#33FFFFFF"));
            } else {
                card.setCardBackgroundColor(COLOR_WHITE);
                text.setTextColor(COLOR_INACTIVE_TEXT);
                if (icon != null) {
                    if (circle != null) {
                        circle.setCardBackgroundColor(Color.parseColor("#EAE4DD"));
                        icon.setColorFilter(COLOR_ACTIVE_ORANGE);
                    } else {
                        icon.setColorFilter(COLOR_INACTIVE_TEXT);
                    }
                }
            }
        });
    }

    private void updateBudgetText(int min, int max) {
        currentMinBudget.setText(String.format(Locale.getDefault(), "%d", min));
        currentMaxBudget.setText(String.format(Locale.getDefault(), "%d", max));
    }
}