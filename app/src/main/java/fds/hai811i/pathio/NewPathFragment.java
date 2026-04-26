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

import fds.hai811i.pathio.databinding.FragmentNewPathBinding;

public class NewPathFragment extends Fragment {
    private FragmentNewPathBinding binding;
    private final int COLOR_INACTIVE_TEXT = Color.parseColor("#000000");
    private final int COLOR_WHITE = Color.parseColor("#FFFFFF");
    private final int COLOR_ACTIVE_ORANGE = Color.parseColor("#D45D3B");
    private final int COLOR_ACTIVE_DARK = Color.parseColor("#2D3142");
    RangeSlider budget;

    public NewPathFragment(){}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentNewPathBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupToggle(binding.cardCulture, binding.textCulture, binding.iconCulture, binding.circleCulture, COLOR_ACTIVE_ORANGE);
        setupToggle(binding.cardLoisirs, binding.textLoisirs, binding.iconLoisirs, binding.circleLoisirs, COLOR_ACTIVE_ORANGE);
        setupToggle(binding.cardFood, binding.textFood, binding.iconFood, binding.circleFood, COLOR_ACTIVE_ORANGE);
        setupToggle(binding.cardDiscover, binding.textDiscover, binding.iconDiscover, binding.circleDiscover, COLOR_ACTIVE_ORANGE);

        CardView[] durationCards = {binding.card2Hours, binding.cardHalfDay, binding.cardFullDay};
        TextView[] durationTexts = {binding.text2Hours, binding.textHalfDay, binding.textFullDay};

        // custom duration input handling
        Runnable onDurationCardClicked = () -> {
            binding.inputCustomHours.clearFocus();
            binding.inputCustomHours.setText("");
        };
        setupExclusiveGroup(durationCards, durationTexts, COLOR_ACTIVE_DARK, onDurationCardClicked);

        // if the user focuses the custom input, deselect all duration cards
        binding.inputCustomHours.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                for (int i = 0; i < durationCards.length; i++) {
                    durationCards[i].setSelected(false);
                    updateToggleVisuals(durationCards[i], durationTexts[i], null, null, COLOR_ACTIVE_DARK, false);
                }
            }
        });

        CardView[] effortCards = {binding.cardEffortFaible, binding.cardEffortModere, binding.cardEffortEleve};
        TextView[] effortTexts = {binding.textEffortFaible, binding.textEffortModere, binding.textEffortEleve};
        setupExclusiveGroup(effortCards, effortTexts, COLOR_ACTIVE_DARK, null);

        setupToggle(binding.cardOldPeople, binding.textOldPeople, binding.iconOldPeople, null, COLOR_ACTIVE_DARK);
        setupToggle(binding.cardEnfant, binding.textEnfant, binding.iconEnfant, null, COLOR_ACTIVE_DARK);
        setupToggle(binding.cardPMAPMR, binding.textPMAPMR, binding.iconPMAPMR, null, COLOR_ACTIVE_DARK);
        setupToggle(binding.cardFroid, binding.textFroid, binding.iconFroid, null, COLOR_ACTIVE_DARK);
        setupToggle(binding.cardChaud, binding.textChaud, binding.iconChaud, null, COLOR_ACTIVE_DARK);
        setupToggle(binding.cardWater, binding.textWater, binding.iconWater, null, COLOR_ACTIVE_DARK);

        budget = binding.rangeSliderBudget;
        budget.setValues(150f, 450f);
        updateBudgetText(150, 450);

        budget.addOnChangeListener((slider, value, fromUser) -> {
            int minVal = Math.round(slider.getValues().get(0));
            int maxVal = Math.round(slider.getValues().get(1));

            updateBudgetText(minVal, maxVal);
        });

        binding.btnGenerateItinerary.setOnClickListener(v ->
                ((MainActivity) requireActivity()).navigateTo(new ItineraryListFragment(), 0)
        );
    }

    /**
     * Sélection normale (plusieurs choix simultanés possibles)
     * @param card "Bouton"
     * @param text Texte du bouton
     * @param icon Icône du bouton
     * @param circle Cercle autour de l'icône pour certains boutons
     * @param activeColorBg Couleur choisie pour le fond d'un bouton lorsqu'il est sélectionné
     */
    private void setupToggle(CardView card, TextView text, @Nullable ImageView icon, @Nullable CardView circle, int activeColorBg) {
        card.setOnClickListener(v -> {
            boolean isNowSelected = !v.isSelected();
            v.setSelected(isNowSelected);
            updateToggleVisuals(card, text, icon, circle, activeColorBg, isNowSelected);
        });
    }

    /**
     * Sélection style bouton radio : quand un de la liste est sélectionné, les autres sont désélectionnés.
     * @param cards Liste de "boutons"
     * @param texts Liste des textes des "boutons"
     * @param activeColorBg Couleur choisie pour le fond d'un bouton lorsqu'il est sélectionné
     * @param onSelectedCallback Callback exécuté lorsqu'une carte est sélectionnée
     */
    private void setupExclusiveGroup(CardView[] cards, TextView[] texts, int activeColorBg, @Nullable Runnable onSelectedCallback) {
        for (int i = 0; i < cards.length; i++) {
            final int clickedIndex = i;
            cards[i].setOnClickListener(v -> {
                boolean isNowSelected = !v.isSelected();

                for (int j = 0; j < cards.length; j++) {
                    if (j == clickedIndex) {
                        cards[j].setSelected(isNowSelected);
                        updateToggleVisuals(cards[j], texts[j], null, null, activeColorBg, isNowSelected);
                    } else {
                        cards[j].setSelected(false);
                        updateToggleVisuals(cards[j], texts[j], null, null, activeColorBg, false);
                    }
                }

                if (isNowSelected && onSelectedCallback != null) {
                    onSelectedCallback.run();
                }
            });
        }
    }

    private void updateToggleVisuals(CardView card, TextView text, @Nullable ImageView icon, @Nullable CardView circle, int activeColorBg, boolean isSelected) {
        if (isSelected) {
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
    }

    private void updateBudgetText(int min, int max) {
        binding.currentMinBudget.setText(String.format(Locale.getDefault(), "%d", min));
        binding.currentMaxBudget.setText(String.format(Locale.getDefault(), "%d", max));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}