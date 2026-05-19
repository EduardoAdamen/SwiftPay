package com.swiftpay;

import android.app.Application;
import com.swiftpay.data.db.SwiftPayDatabase;
import com.swiftpay.data.preferences.SessionManager;
import com.swiftpay.data.entity.UserPreferences;
import com.swiftpay.util.ThemeManager;
import java.util.concurrent.Executors;

public class SwiftPayApplication extends Application {

    private SwiftPayDatabase database;

    @Override
    public void onCreate() {
        super.onCreate();
        database = SwiftPayDatabase.getInstance(this);

        SessionManager sessionManager = new SessionManager(this);
        long userId = sessionManager.getUserId();
        if (userId != -1) {
            Executors.newSingleThreadExecutor().execute(() -> {
                UserPreferences prefs = database.userPreferencesDao().getByUserIdSync(userId);
                if (prefs != null) {
                    com.swiftpay.util.ThemeManager.applyTheme(prefs.getThemeMode());
                }
            });
        }
    }

    public SwiftPayDatabase getDatabase() {
        return database;
    }
}
