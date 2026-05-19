package com.swiftpay.data.repository;

import androidx.lifecycle.LiveData;
import com.swiftpay.data.dao.UserPreferencesDao;
import com.swiftpay.data.db.SwiftPayDatabase;
import com.swiftpay.data.entity.UserPreferences;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class UserPreferencesRepository {
    private final UserPreferencesDao prefsDao;
    private final ExecutorService executorService;

    public UserPreferencesRepository(SwiftPayDatabase database) {
        this.prefsDao = database.userPreferencesDao();
        this.executorService = Executors.newSingleThreadExecutor();
    }

    public void insert(UserPreferences prefs) {
        executorService.execute(() -> prefsDao.insert(prefs));
    }

    public void update(UserPreferences prefs) {
        executorService.execute(() -> prefsDao.update(prefs));
    }

    public LiveData<UserPreferences> getByUserId(long userId) {
        return prefsDao.getByUserId(userId);
    }
    
    public UserPreferences getByUserIdSync(long userId) {
        return prefsDao.getByUserIdSync(userId);
    }
}
