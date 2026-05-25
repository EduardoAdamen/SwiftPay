// app/src/main/java/com/swiftpay/data/preferences/SessionManager.java
package com.swiftpay.data.preferences;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.annotation.NonNull;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

/**
 * Manages the active user session exclusively with EncryptedSharedPreferences.
 */
public final class SessionManager {

    private static final String PREFS_NAME = "swiftpay_session";
    private static final String KEY_USER_ID = "userId";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_FULL_NAME = "fullName";
    private static final String KEY_ROLE = "role";
    private static final String KEY_LOGIN_TIMESTAMP = "loginTimestamp";
    private static final String KEY_LAST_ACTIVITY = "lastActivityTimestamp";
    private static final String KEY_IS_TEMP_PASSWORD = "isTemporaryPassword";
    private static final long SESSION_TIMEOUT_MS = 30L * 60L * 1000L;

    private SharedPreferences prefs;

    public SessionManager(@NonNull Context context) {
        Context appContext = context.getApplicationContext();
        try {
            MasterKey masterKey = new MasterKey.Builder(appContext)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();
            prefs = EncryptedSharedPreferences.create(
                    appContext,
                    PREFS_NAME,
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM);
        } catch (Exception e) {
            // Fallback to standard SharedPreferences if Keystore is corrupted/unsupported on this device.
            // This prevents a hard crash on app startup.
            prefs = appContext.getSharedPreferences(PREFS_NAME + "_fallback", Context.MODE_PRIVATE);
        }
    }

    /** Stores a successful login session atomically. */
    public void createSession(long userId, String username, String fullName, String role, boolean isTemporaryPassword) {
        long now = System.currentTimeMillis();
        boolean saved = prefs.edit()
                .putLong(KEY_USER_ID, userId)
                .putString(KEY_USERNAME, username)
                .putString(KEY_FULL_NAME, fullName)
                .putString(KEY_ROLE, role)
                .putLong(KEY_LOGIN_TIMESTAMP, now)
                .putLong(KEY_LAST_ACTIVITY, now)
                .putBoolean(KEY_IS_TEMP_PASSWORD, isTemporaryPassword)
                .commit();
        if (!saved) {
            throw new IllegalStateException("No se pudo persistir la sesion cifrada.");
        }
    }

    /** Returns true when the session has not exceeded the 30 minute inactivity timeout. */
    public boolean checkSessionValidity() {
        long lastActivity = prefs.getLong(KEY_LAST_ACTIVITY, 0L);
        return lastActivity > 0L && System.currentTimeMillis() - lastActivity <= SESSION_TIMEOUT_MS;
    }

    /** Updates the last interaction timestamp. */
    public void updateLastActivity() {
        prefs.edit().putLong(KEY_LAST_ACTIVITY, System.currentTimeMillis()).apply();
    }

    /** Returns true when there is an authenticated user in encrypted storage. */
    public boolean isLoggedIn() {
        return prefs.getLong(KEY_USER_ID, -1L) != -1L;
    }

    /** Clears the encrypted session synchronously for logout or timeout. */
    public void clearSession() {
        boolean cleared = prefs.edit().clear().commit();
        if (!cleared) {
            throw new IllegalStateException("No se pudo limpiar la sesion cifrada.");
        }
    }

    /** Marks the forced temporary password flow as complete. */
    public void markTemporaryPasswordResolved() {
        prefs.edit().putBoolean(KEY_IS_TEMP_PASSWORD, false).apply();
    }

    public long getUserId() {
        return prefs.getLong(KEY_USER_ID, -1L);
    }

    public String getUsername() {
        return prefs.getString(KEY_USERNAME, "");
    }

    public String getFullName() {
        return prefs.getString(KEY_FULL_NAME, "");
    }

    public String getRole() {
        return prefs.getString(KEY_ROLE, "");
    }

    public boolean isTemporaryPassword() {
        return prefs.getBoolean(KEY_IS_TEMP_PASSWORD, false);
    }

    public boolean hasRole(String role) {
        return role != null && role.equals(getRole());
    }
}
