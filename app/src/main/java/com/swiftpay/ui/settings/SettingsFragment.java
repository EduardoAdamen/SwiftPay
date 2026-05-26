// app/src/main/java/com/swiftpay/ui/settings/SettingsFragment.java
package com.swiftpay.ui.settings;

import android.app.TimePickerDialog;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.TimePicker;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.swiftpay.MainActivity;
import com.swiftpay.R;
import com.swiftpay.data.entity.UserPreferences;
import com.swiftpay.util.AlarmScheduler;
import com.swiftpay.util.NotificationHelper;
import com.swiftpay.util.ThemeManager;
import com.swiftpay.viewmodel.SettingsViewModel;
import java.util.Calendar;
import java.util.Locale;

/**
 * Settings screen implementing UX-E1 (theme), UX-B7 (color scheme),
 * UX-F1 (font size), UX-E2 (compact view), UX-F2 (accessibility),
 * UX-C5 (images toggle) and UX-E4 (animations toggle).
 *
 * Implements UX-E3 (wallpaper), UX-D2 (notification sound) and UX-D3 (recurring alarms).
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

    private TextView tvWallpaperStatus;
    private MaterialButton btnPickWallpaper;
    private MaterialButton btnClearWallpaper;
    private TextView tvSoundStatus;
    private MaterialButton btnPickSound;
    private MaterialButton btnTestSound;

    private SwitchMaterial switchAlarms;
    private MaterialButton btnPickAlarmTime;
    private ChipGroup chipGroupAlarmDays;
    private Chip chipMon, chipTue, chipWed, chipThu, chipFri, chipSat, chipSun;

    private String wallpaperUriString = null;
    private String notificationSoundUriString = null;
    private int alarmHour = 9;
    private int alarmMin = 0;
    private int alarmDaysMask = 0b0111110; // Lun-Sáb

    private final ActivityResultLauncher<String[]> pickWallpaperLauncher =
            registerForActivityResult(new ActivityResultContracts.OpenDocument(), uri -> {
                if (uri == null) return;
                try {
                    requireContext().getContentResolver().takePersistableUriPermission(
                            uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                } catch (Exception ignored) {}
                wallpaperUriString = uri.toString();
                refreshWallpaperStatus();
            });

    private final ActivityResultLauncher<Intent> pickSoundLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result == null || result.getData() == null) return;
                Uri uri = result.getData().getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
                notificationSoundUriString = uri != null ? uri.toString() : null;
                refreshSoundStatus();
            });

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

        tvWallpaperStatus = view.findViewById(R.id.tvWallpaperStatus);
        btnPickWallpaper = view.findViewById(R.id.btnPickWallpaper);
        btnClearWallpaper = view.findViewById(R.id.btnClearWallpaper);
        tvSoundStatus = view.findViewById(R.id.tvSoundStatus);
        btnPickSound = view.findViewById(R.id.btnPickSound);
        btnTestSound = view.findViewById(R.id.btnTestSound);

        switchAlarms = view.findViewById(R.id.switchAlarms);
        btnPickAlarmTime = view.findViewById(R.id.btnPickAlarmTime);
        chipGroupAlarmDays = view.findViewById(R.id.chipGroupAlarmDays);
        chipMon = view.findViewById(R.id.chipMon);
        chipTue = view.findViewById(R.id.chipTue);
        chipWed = view.findViewById(R.id.chipWed);
        chipThu = view.findViewById(R.id.chipThu);
        chipFri = view.findViewById(R.id.chipFri);
        chipSat = view.findViewById(R.id.chipSat);
        chipSun = view.findViewById(R.id.chipSun);

        MaterialButton btnSaveSettings = view.findViewById(R.id.btnSaveSettings);

        setupSpinners();
        if (switchAlarms != null && btnPickAlarmTime != null && chipGroupAlarmDays != null) {
            loadAlarmPrefsFromSharedPrefs();
            refreshAlarmUi();
        }

        viewModel.getUserPreferences(currentUserId).observe(getViewLifecycleOwner(), prefs -> {
            if (prefs != null) {
                currentPrefs = prefs;
                loadPrefsIntoUi(prefs);
            } else {
                currentPrefs = new UserPreferences();
                currentPrefs.setUserId(currentUserId);
            }
        });

        if (btnPickWallpaper != null) {
            btnPickWallpaper.setOnClickListener(v -> pickWallpaperLauncher.launch(new String[]{"image/*"}));
        }
        if (btnClearWallpaper != null) {
            btnClearWallpaper.setOnClickListener(v -> {
                wallpaperUriString = null;
                refreshWallpaperStatus();
            });
        }

        if (btnPickSound != null) {
            btnPickSound.setOnClickListener(v -> launchSoundPicker());
        }
        if (btnTestSound != null) {
            btnTestSound.setOnClickListener(v -> testSelectedSound());
        }

        if (btnPickAlarmTime != null && switchAlarms != null && chipGroupAlarmDays != null) {
            btnPickAlarmTime.setOnClickListener(v -> showTimePicker());
            switchAlarms.setOnCheckedChangeListener((buttonView, isChecked) -> {
                btnPickAlarmTime.setEnabled(isChecked);
                chipGroupAlarmDays.setEnabled(isChecked);
                setChipsEnabled(isChecked);
            });
        }

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

        wallpaperUriString = prefs.getWallpaperPath();
        notificationSoundUriString = prefs.getNotificationSound();
        refreshWallpaperStatus();
        refreshSoundStatus();
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
        currentPrefs.setWallpaperPath(wallpaperUriString);
        currentPrefs.setNotificationSound(notificationSoundUriString);

        viewModel.saveUserPreferences(currentPrefs);

        // Also save to a safe SharedPreferences file for MainActivity's attachBaseContext and onCreate
        android.content.SharedPreferences uxPrefs = requireContext().getSharedPreferences("swiftpay_ux_prefs", android.content.Context.MODE_PRIVATE);
        uxPrefs.edit()
                .putString("font_size", currentPrefs.getFontSize())
                .putString("color_scheme", currentPrefs.getColorScheme())
                .putInt("accessibility_mode", currentPrefs.getAccessibilityMode())
                .putString("wallpaper_path", wallpaperUriString)
                .putString("notification_sound", notificationSoundUriString)
                .putBoolean(AlarmScheduler.KEY_ALARMS_ENABLED, switchAlarms.isChecked())
                .putInt(AlarmScheduler.KEY_ALARM_HOUR, alarmHour)
                .putInt(AlarmScheduler.KEY_ALARM_MIN, alarmMin)
                .putInt(AlarmScheduler.KEY_ALARM_DAYS, collectDaysMask())
                .apply();

        Toast.makeText(getContext(), R.string.settings_save, Toast.LENGTH_SHORT).show();

        // Apply the night-mode globally first — this may or may not trigger a
        // config change depending on whether the mode actually changed.
        ThemeManager.applyTheme(currentPrefs.getThemeMode());

        // Apply alarms and notification channel sound without requiring recreate.
        AlarmScheduler.applyFromPrefs(requireContext().getApplicationContext());
        NotificationHelper.recreateNotificationChannel(requireContext().getApplicationContext());

        // Single recreate to pick up font scale, color scheme and accessibility
        // changes via attachBaseContext.
        requireActivity().recreate();
    }

    private int resolveIndex(String[] values, String currentValue, String fallbackValue) {
        String value = currentValue == null ? fallbackValue : currentValue;
        int index = java.util.Arrays.asList(values).indexOf(value);
        return Math.max(index, 0);
    }

    private void refreshWallpaperStatus() {
        if (tvWallpaperStatus == null) return;
        if (wallpaperUriString == null || wallpaperUriString.trim().isEmpty()) {
            tvWallpaperStatus.setText(R.string.settings_wallpaper_none);
        } else {
            tvWallpaperStatus.setText("Fondo: seleccionado");
        }
    }

    private void refreshSoundStatus() {
        if (tvSoundStatus == null) return;
        if (notificationSoundUriString == null || notificationSoundUriString.trim().isEmpty()) {
            tvSoundStatus.setText(R.string.settings_sound_default);
            return;
        }
        android.content.Context ctx = getContext();
        if (ctx == null) return; // Fragment detached — skip UI update safely
        try {
            Uri uri = Uri.parse(notificationSoundUriString);
            Ringtone r = RingtoneManager.getRingtone(ctx, uri);
            String title = r != null ? r.getTitle(ctx) : "Personalizado";
            tvSoundStatus.setText("Sonido: " + title);
        } catch (Exception e) {
            tvSoundStatus.setText(R.string.settings_sound_default);
        }
    }

    private void launchSoundPicker() {
        Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_NOTIFICATION);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, true);
        if (notificationSoundUriString != null && !notificationSoundUriString.trim().isEmpty()) {
            try {
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, Uri.parse(notificationSoundUriString));
            } catch (Exception ignored) {}
        }
        pickSoundLauncher.launch(intent);
    }

    private void testSelectedSound() {
        android.content.Context ctx = getContext();
        if (ctx == null) return; // Fragment detached — skip safely
        if (notificationSoundUriString == null || notificationSoundUriString.trim().isEmpty()) {
            Toast.makeText(ctx, "Sonido predeterminado", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            Uri uri = Uri.parse(notificationSoundUriString);
            Ringtone r = RingtoneManager.getRingtone(ctx, uri);
            if (r != null) r.play();
        } catch (Exception e) {
            Toast.makeText(ctx, "No se pudo reproducir", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadAlarmPrefsFromSharedPrefs() {
        if (switchAlarms == null) return;
        android.content.SharedPreferences prefs = requireContext().getSharedPreferences(AlarmScheduler.PREFS, android.content.Context.MODE_PRIVATE);
        alarmHour = prefs.getInt(AlarmScheduler.KEY_ALARM_HOUR, 9);
        alarmMin = prefs.getInt(AlarmScheduler.KEY_ALARM_MIN, 0);
        alarmDaysMask = prefs.getInt(AlarmScheduler.KEY_ALARM_DAYS, 0b0111110);
        switchAlarms.setChecked(prefs.getBoolean(AlarmScheduler.KEY_ALARMS_ENABLED, false));
    }

    private void refreshAlarmUi() {
        if (switchAlarms == null || btnPickAlarmTime == null || chipGroupAlarmDays == null) return;
        boolean enabled = switchAlarms.isChecked();
        btnPickAlarmTime.setEnabled(enabled);
        chipGroupAlarmDays.setEnabled(enabled);
        setChipsEnabled(enabled);
        btnPickAlarmTime.setText(String.format(Locale.getDefault(), "%02d:%02d", alarmHour, alarmMin));
        setChipCheckedFromMask();
    }

    private void setChipsEnabled(boolean enabled) {
        if (chipMon == null || chipTue == null || chipWed == null || chipThu == null || chipFri == null || chipSat == null || chipSun == null) return;
        chipMon.setEnabled(enabled);
        chipTue.setEnabled(enabled);
        chipWed.setEnabled(enabled);
        chipThu.setEnabled(enabled);
        chipFri.setEnabled(enabled);
        chipSat.setEnabled(enabled);
        chipSun.setEnabled(enabled);
    }

    private void setChipCheckedFromMask() {
        if (chipMon == null || chipTue == null || chipWed == null || chipThu == null || chipFri == null || chipSat == null || chipSun == null) return;
        chipMon.setChecked((alarmDaysMask & (1 << 0)) != 0);
        chipTue.setChecked((alarmDaysMask & (1 << 1)) != 0);
        chipWed.setChecked((alarmDaysMask & (1 << 2)) != 0);
        chipThu.setChecked((alarmDaysMask & (1 << 3)) != 0);
        chipFri.setChecked((alarmDaysMask & (1 << 4)) != 0);
        chipSat.setChecked((alarmDaysMask & (1 << 5)) != 0);
        chipSun.setChecked((alarmDaysMask & (1 << 6)) != 0);
    }

    private int collectDaysMask() {
        if (chipMon == null || chipTue == null || chipWed == null || chipThu == null || chipFri == null || chipSat == null || chipSun == null) return 0;
        int mask = 0;
        if (chipMon.isChecked()) mask |= (1 << 0);
        if (chipTue.isChecked()) mask |= (1 << 1);
        if (chipWed.isChecked()) mask |= (1 << 2);
        if (chipThu.isChecked()) mask |= (1 << 3);
        if (chipFri.isChecked()) mask |= (1 << 4);
        if (chipSat.isChecked()) mask |= (1 << 5);
        if (chipSun.isChecked()) mask |= (1 << 6);
        return mask;
    }

    private void showTimePicker() {
        Calendar now = Calendar.getInstance();
        TimePickerDialog dlg = new TimePickerDialog(
                requireContext(),
                (TimePicker view, int hourOfDay, int minute) -> {
                    alarmHour = hourOfDay;
                    alarmMin = minute;
                    refreshAlarmUi();
                },
                alarmHour,
                alarmMin,
                true
        );
        dlg.show();
    }
}
