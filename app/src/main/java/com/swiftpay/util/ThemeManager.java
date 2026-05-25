// app/src/main/java/com/swiftpay/util/ThemeManager.java
package com.swiftpay.util;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import androidx.appcompat.app.AppCompatDelegate;
import com.swiftpay.R;
import java.util.Locale;

/**
 * Applies SwiftPay runtime UX preferences for theme, color scheme and font scale.
 */
public final class ThemeManager {

    private ThemeManager() {
    }

    /** Applies light, dark or system night mode immediately. */
    public static void applyTheme(String themeMode) {
        if ("LIGHT".equals(themeMode)) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        } else if ("DARK".equals(themeMode)) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        }
    }

    /** Applies one of the five configured color schemes to an activity theme. */
    public static void applyColorScheme(Activity activity, String colorScheme) {
        activity.getTheme().applyStyle(getColorSchemeResId(colorScheme), true);
    }

    /** Returns the concrete style for DEFAULT, EMERALD, PURPLE, CORAL or TEAL. */
    public static int getColorSchemeResId(String colorScheme) {
        String normalized = colorScheme == null ? "DEFAULT" : colorScheme.toUpperCase(Locale.ROOT);
        switch (normalized) {
            case "EMERALD":
                return R.style.ThemeOverlay_SwiftPay_Emerald;
            case "PURPLE":
                return R.style.ThemeOverlay_SwiftPay_Purple;
            case "CORAL":
                return R.style.ThemeOverlay_SwiftPay_Coral;
            case "TEAL":
                return R.style.ThemeOverlay_SwiftPay_Teal;
            case "DEFAULT":
            default:
                return R.style.ThemeOverlay_SwiftPay_Default;
        }
    }

    /** Applies the configured font scale through Configuration. */
    public static Context applyFontScale(Context context, String fontSize) {
        float scale;
        if ("SMALL".equals(fontSize)) {
            scale = 0.85f;
        } else if ("LARGE".equals(fontSize)) {
            scale = 1.3f;
        } else {
            scale = 1.0f;
        }

        Configuration configuration = new Configuration(context.getResources().getConfiguration());
        configuration.fontScale = scale;
        return context.createConfigurationContext(configuration);
    }
}
