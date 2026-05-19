package com.swiftpay.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import com.swiftpay.data.db.SwiftPayDatabase;
import com.swiftpay.data.entity.UserPreferences;
import com.swiftpay.data.repository.UserPreferencesRepository;

public class SettingsViewModel extends AndroidViewModel {
    private final UserPreferencesRepository repository;

    public SettingsViewModel(@NonNull Application application) {
        super(application);
        SwiftPayDatabase db = SwiftPayDatabase.getInstance(application);
        repository = new UserPreferencesRepository(db);
    }

    public LiveData<UserPreferences> getUserPreferences(long userId) {
        return repository.getByUserId(userId);
    }

    public void saveUserPreferences(UserPreferences prefs) {
        // Assume prefs exists or if null we insert
        repository.insert(prefs); // Room insert with REPLACE strategy handles update
    }
}
