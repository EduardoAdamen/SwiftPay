package com.swiftpay.ui.settings;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.switchmaterial.SwitchMaterial;
import com.swiftpay.MainActivity;
import com.swiftpay.R;
import com.swiftpay.data.entity.UserPreferences;
import com.swiftpay.util.ThemeManager;
import com.swiftpay.viewmodel.SettingsViewModel;

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
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
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
        Button btnSaveSettings = view.findViewById(R.id.btnSaveSettings);

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
        spinnerTheme.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, themeOptions));
        spinnerColorScheme.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, colorOptions));
        spinnerFontSize.setAdapter(new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, fontOptions));
    }

    private void loadPrefsIntoUi(UserPreferences prefs) {
        spinnerTheme.setSelection(java.util.Arrays.asList(themeOptions).indexOf(prefs.getThemeMode()));
        spinnerColorScheme.setSelection(java.util.Arrays.asList(colorOptions).indexOf(prefs.getColorScheme()));
        spinnerFontSize.setSelection(java.util.Arrays.asList(fontOptions).indexOf(prefs.getFontSize()));
        switchCompactView.setChecked(prefs.getCompactView() == 1);
        switchAccessibility.setChecked(prefs.getAccessibilityMode() == 1);
        switchImages.setChecked(prefs.getImagesEnabled() == 1);
        switchAnimations.setChecked(prefs.getAnimationsEnabled() == 1);
    }

    private void savePreferences() {
        if (currentPrefs == null) return;
        currentPrefs.setThemeMode(themeOptions[spinnerTheme.getSelectedItemPosition()]);
        currentPrefs.setColorScheme(colorOptions[spinnerColorScheme.getSelectedItemPosition()]);
        currentPrefs.setFontSize(fontOptions[spinnerFontSize.getSelectedItemPosition()]);
        currentPrefs.setCompactView(switchCompactView.isChecked() ? 1 : 0);
        currentPrefs.setAccessibilityMode(switchAccessibility.isChecked() ? 1 : 0);
        currentPrefs.setImagesEnabled(switchImages.isChecked() ? 1 : 0);
        currentPrefs.setAnimationsEnabled(switchAnimations.isChecked() ? 1 : 0);

        viewModel.saveUserPreferences(currentPrefs);
        
        Toast.makeText(getContext(), "Configuración guardada", Toast.LENGTH_SHORT).show();
        
        ThemeManager.applyTheme(currentPrefs.getThemeMode());
        
        // Restart app to apply complete theme changes (especially colors and fonts)
        Intent intent = new Intent(requireActivity(), MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().finish();
    }
}
