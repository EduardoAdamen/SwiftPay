// app/src/main/java/com/swiftpay/util/ThemeManager.java
package com.swiftpay.util;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.view.View;
import android.view.ViewGroup;
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

    /** Applies the user-selected wallpaper (UX-E3) to the main DrawerLayout if present. */
    public static void applyWallpaper(Activity activity, String wallpaperUriString) {
        if (activity == null) return;
        View root = activity.findViewById(R.id.drawer_layout);
        if (root == null) return;

        if (wallpaperUriString == null || wallpaperUriString.trim().isEmpty()) {
            root.setBackground(null);
            return;
        }
        try {
            Uri uri = Uri.parse(wallpaperUriString);
            try (java.io.InputStream in = activity.getContentResolver().openInputStream(uri)) {
                if (in == null) {
                    root.setBackground(null);
                    return;
                }
                Bitmap bmp = BitmapFactory.decodeStream(in);
                if (bmp == null) {
                    root.setBackground(null);
                    return;
                }
                BitmapDrawable drawable = new BitmapDrawable(activity.getResources(), bmp);
                drawable.setTileModeXY(android.graphics.Shader.TileMode.CLAMP, android.graphics.Shader.TileMode.CLAMP);
                root.setBackground(drawable);
            }
        } catch (Exception e) {
            root.setBackground(null);
        }
    }

    /**
     * Applies accessibility sizing (UX-F2) by increasing minimum heights of touch targets.
     * This is a best-effort runtime pass; it complements font scaling.
     */
    public static void applyAccessibilitySizing(Activity activity, boolean enabled) {
        if (activity == null) return;
        View root = activity.getWindow() != null ? activity.getWindow().getDecorView() : null;
        if (root == null) return;
        applyAccessibilityRecursive(root, enabled, dpToPx(activity, 56));
    }

    private static void applyAccessibilityRecursive(View view, boolean enabled, int minHeightPx) {
        if (view == null) return;

        if (view instanceof android.widget.Button
                || view instanceof com.google.android.material.button.MaterialButton
                || view instanceof com.google.android.material.switchmaterial.SwitchMaterial) {
            if (enabled) {
                view.setMinimumHeight(minHeightPx);
                view.setPadding(
                        Math.max(view.getPaddingLeft(), minHeightPx / 6),
                        Math.max(view.getPaddingTop(), minHeightPx / 6),
                        Math.max(view.getPaddingRight(), minHeightPx / 6),
                        Math.max(view.getPaddingBottom(), minHeightPx / 6)
                );
            }
        }

        if (view instanceof ViewGroup) {
            ViewGroup vg = (ViewGroup) view;
            for (int i = 0; i < vg.getChildCount(); i++) {
                applyAccessibilityRecursive(vg.getChildAt(i), enabled, minHeightPx);
            }
        }
    }

    private static int dpToPx(Context context, int dp) {
        return Math.round(dp * context.getResources().getDisplayMetrics().density);
    }
}
