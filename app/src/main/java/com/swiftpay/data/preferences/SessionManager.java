package com.swiftpay.data.preferences;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

/**
 * Gestiona la sesión activa del usuario usando EncryptedSharedPreferences.
 * Controla el timeout de 30 minutos de inactividad.
 */
public class SessionManager {

    private static final String PREFS_NAME = "swiftpay_session";
    private static final String KEY_USER_ID = "userId";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_FULL_NAME = "fullName";
    private static final String KEY_ROLE = "role";
    private static final String KEY_LOGIN_TIMESTAMP = "loginTimestamp";
    private static final String KEY_LAST_ACTIVITY = "lastActivityTimestamp";
    private static final String KEY_IS_TEMP_PASSWORD = "isTemporaryPassword";
    private static final long SESSION_TIMEOUT_MS = 30 * 60 * 1000; // 30 minutos

    private final SharedPreferences prefs;

    public SessionManager(Context context) {
        SharedPreferences tempPrefs;
        try {
            MasterKey masterKey = new MasterKey.Builder(context)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();
            tempPrefs = EncryptedSharedPreferences.create(
                    context,
                    PREFS_NAME,
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
        } catch (Exception e) {
            // Fallback a SharedPreferences normales si hay error con encriptación
            tempPrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        }
        this.prefs = tempPrefs;
    }

    /** Guarda la sesión del usuario tras un login exitoso */
    public void createSession(long userId, String username, String fullName, String role, boolean isTemporaryPassword) {
        long now = System.currentTimeMillis();
        prefs.edit()
                .putLong(KEY_USER_ID, userId)
                .putString(KEY_USERNAME, username)
                .putString(KEY_FULL_NAME, fullName)
                .putString(KEY_ROLE, role)
                .putLong(KEY_LOGIN_TIMESTAMP, now)
                .putLong(KEY_LAST_ACTIVITY, now)
                .putBoolean(KEY_IS_TEMP_PASSWORD, isTemporaryPassword)
                .apply();
    }

    /** Verifica si la sesión sigue siendo válida (no ha expirado) */
    public boolean checkSessionValidity() {
        long lastActivity = prefs.getLong(KEY_LAST_ACTIVITY, 0);
        if (lastActivity == 0) return false;
        return (System.currentTimeMillis() - lastActivity) <= SESSION_TIMEOUT_MS;
    }

    /** Actualiza el timestamp de última actividad */
    public void updateLastActivity() {
        prefs.edit().putLong(KEY_LAST_ACTIVITY, System.currentTimeMillis()).apply();
    }

    /** Verifica si hay una sesión activa */
    public boolean isLoggedIn() {
        return prefs.getLong(KEY_USER_ID, -1) != -1;
    }

    /** Limpia la sesión (logout) */
    public void clearSession() {
        prefs.edit().clear().apply();
    }

    public long getUserId() { return prefs.getLong(KEY_USER_ID, -1); }
    public String getUsername() { return prefs.getString(KEY_USERNAME, ""); }
    public String getFullName() { return prefs.getString(KEY_FULL_NAME, ""); }
    public String getRole() { return prefs.getString(KEY_ROLE, ""); }
    public boolean isTemporaryPassword() { return prefs.getBoolean(KEY_IS_TEMP_PASSWORD, false); }

    /** Verifica si el usuario tiene un rol específico */
    public boolean hasRole(String role) {
        return role != null && role.equals(getRole());
    }
}
