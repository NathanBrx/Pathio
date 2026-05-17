package fds.hai811i.pathio.utils;

import android.content.res.ColorStateList;
import android.graphics.Color;

import com.google.android.material.button.MaterialButton;

public class ViewUtils {

    public static void setButtonActive(MaterialButton button) {
        button.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#B84B25")));
        button.setTextColor(Color.WHITE);
        button.setStrokeWidth(0);
    }

    public static void setButtonInactive(MaterialButton button) {
        button.setBackgroundTintList(ColorStateList.valueOf(Color.TRANSPARENT));
        button.setTextColor(Color.parseColor("#111827"));
        button.setStrokeColor(ColorStateList.valueOf(Color.parseColor("#BCBCBC")));
        button.setStrokeWidth(3);
    }

    // Bonus: A helper to swap both buttons at once!
    public static void switchTabs(MaterialButton activeBtn, MaterialButton inactiveBtn) {
        setButtonActive(activeBtn);
        setButtonInactive(inactiveBtn);
    }
}