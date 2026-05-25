// app/src/main/java/com/swiftpay/ui/settings/SettingsFragment.java
package com.swiftpay.ui.settings;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.swiftpay.MainActivity;
import com.swiftpay.R;
import com.swiftpay.data.entity.UserPreferences;
import com.swiftpay.util.ThemeManager;
import com.swiftpay.viewmodel.SettingsViewModel;

/**
 * Settings screen implementing UX-E1 (theme), UX-B7 (color scheme),
 * UX-F1 (font size), UX-E2 (compact view), UX-F2 (accessibility),
 * UX-C5 (images toggle) and UX-E4 (animations toggle).
 *
 * <p>Preferences are persisted in {@code user_preferences} via the ViewModel.
 * After saving, the theme is applied immediately and the Activity is recreated
 * exactly once so all font scale / color scheme changes take effect through
 * {@link MainActivity#attachBaseContext(android.content.Context)}.</p>
 */
public class SettingsFragment extends Fragment {

    private SettingsViewModel viewModel;
    private UserPreferences currentPrefs;
    private long currentUserId;

    private Spinner spinnerTheme;
    private Spinner spinnerColorScheme;
    private Spinner spinnerFontSize;
    private SwitchMaterial switchCompactView;
    private SwitchMaterial switchAccessibility;
    private SwitchMaterial switchImages;
    private SwitchMaterial switchAnimations;

    private final String[] themeOptions = {"SYSTEM", "LIGHT", "DARK"};
    private final String[] colorOptions = {"DEFAULT", "EMERALD", "PURPLE", "CORAL", "TEAL"};
    private final String[] fontOptions = {"SMALL", "NORMAL", "LARGE"};

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(SettingsViewModel.class);
        currentUserId = ((MainActivity) requireActivity()).getSessionManager().getUserId();

        spinnerTheme = view.findViewById(R.id.spinnerTheme);
        spinnerColorScheme = view.findViewById(R.id.spinnerColorScheme);
        spinnerFontSize = view.findViewById(R.id.spinnerFontSize);
        switchCompactView = view.findViewById(R.id.switchCompactView);
        switchAccessibility = view.findViewById(R.id.switchAccessibility);
        switchImages = view.findViewById(R.id.switchImages);
        switchAnimations = view.findViewById(R.id.switchAnimations);
        MaterialButton btnSaveSettings = view.findViewById(R.id.btnSaveSettings);

        setupSpinners();

        viewModel.getUserPreferences(currentUserId).observe(getViewLifecycleOwner(), prefs -> {
            if (prefs != null) {
                currentPrefs = prefs;
                loadPrefsIntoUi(prefs);
            } else {
                currentPrefs = new UserPreferences();
                currentPrefs.setUserId(currentUserId);
            }
        });

        btnSaveSettings.setOnClickListener(v -> savePreferences());
    }

    private void setupSpinners() {
        spinnerTheme.setAdapter(new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_dropdown_item, themeOptions));
        spinnerColorScheme.setAdapter(new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_dropdown_item, colorOptions));
        spinnerFontSize.setAdapter(new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_dropdown_item, fontOptions));
    }

    private void loadPrefsIntoUi(UserPreferences prefs) {
        spinnerTheme.setSelection(resolveIndex(themeOptions, prefs.getThemeMode(), "SYSTEM"));
        spinnerColorScheme.setSelection(resolveIndex(colorOptions, prefs.getColorScheme(), "DEFAULT"));
        spinnerFontSize.setSelection(resolveIndex(fontOptions, prefs.getFontSize(), "NORMAL"));
        switchCompactView.setChecked(prefs.getCompactView() == 1);
        switchAccessibility.setChecked(prefs.getAccessibilityMode() == 1);
        switchImages.setChecked(prefs.getImagesEnabled() == 1);
        switchAnimations.setChecked(prefs.getAnimationsEnabled() == 1);
    }

    /**
     * Persists user preferences, applies the night-mode toggle immediately
     * via {@link ThemeManager#applyTheme(String)}, then recreates the Activity
     * so that {@code attachBaseContext} picks up the new font scale and color
     * scheme.  A single {@code recreate()} is sufficient because all other
     * preference consumers (adapters for compact view, ImageLoader for images,
     * nav options for animations) read the persisted value each time they bind.
     */
    private void savePreferences() {
        if (currentPrefs == null) {
            return;
        }
        currentPrefs.setThemeMode(themeOptions[spinnerTheme.getSelectedItemPosition()]);
        currentPrefs.setColorScheme(colorOptions[spinnerColorScheme.getSelectedItemPosition()]);
        currentPrefs.setFontSize(fontOptions[spinnerFontSize.getSelectedItemPosition()]);
        currentPrefs.setCompactView(switchCompactView.isChecked() ? 1 : 0);
        currentPrefs.setAccessibilityMode(switchAccessibility.isChecked() ? 1 : 0);
        currentPrefs.setImagesEnabled(switchImages.isChecked() ? 1 : 0);
        currentPrefs.setAnimationsEnabled(switchAnimations.isChecked() ? 1 : 0);

        viewModel.saveUserPreferences(currentPrefs);

        // Also save to a safe SharedPreferences file for MainActivity's attachBaseContext and onCreate
        android.content.SharedPreferences uxPrefs = requireContext().getSharedPreferences("swiftpay_ux_prefs", android.content.Context.MODE_PRIVATE);
        uxPrefs.edit()
                .putString("font_size", currentPrefs.getFontSize())
                .putString("color_scheme", currentPrefs.getColorScheme())
                .apply();

        Toast.makeText(getContext(), R.string.settings_save, Toast.LENGTH_SHORT).show();

        // Apply the night-mode globally first — this may or may not trigger a
        // config change depending on whether the mode actually changed.
        ThemeManager.applyTheme(currentPrefs.getThemeMode());

        // Single recreate to pick up font scale, color scheme and accessibility
        // changes via attachBaseContext.
        requireActivity().recreate();
    }

    private int resolveIndex(String[] values, String currentValue, String fallbackValue) {
        String value = currentValue == null ? fallbackValue : currentValue;
        int index = java.util.Arrays.asList(values).indexOf(value);
        return Math.max(index, 0);
    }
}
