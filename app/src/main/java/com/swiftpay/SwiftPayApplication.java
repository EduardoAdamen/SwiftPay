// app/src/main/java/com/swiftpay/SwiftPayApplication.java
package com.swiftpay;

import android.app.Application;
import com.swiftpay.data.db.SwiftPayDatabase;
import com.swiftpay.data.preferences.SessionManager;
import com.swiftpay.data.entity.UserPreferences;
import com.swiftpay.util.NotificationHelper;
import com.swiftpay.util.ThemeManager;
import java.util.concurrent.Executors;

/**
 * Application entry point for SwiftPay.
 * Initialises the Room database, the notification channel, and applies
 * the persisted theme from user preferences when a session already exists.
 */
public class SwiftPayApplication extends Application {

    private SwiftPayDatabase database;

    @Override
    public void onCreate() {
        super.onCreate();
        database = SwiftPayDatabase.getInstance(this);

        // UX-D1: Create the notification channel eagerly so notifications
        // can fire even before the user opens any activity.
        NotificationHelper.createNotificationChannel(this);

        SessionManager sessionManager = new SessionManager(this);
        long userId = sessionManager.getUserId();
        if (userId != -1) {
            Executors.newSingleThreadExecutor().execute(() -> {
                UserPreferences prefs = database.userPreferencesDao().getByUserIdSync(userId);
                if (prefs != null) {
                    ThemeManager.applyTheme(prefs.getThemeMode());
                }
            });
        }
    }

    public SwiftPayDatabase getDatabase() {
        return database;
    }
}
