package com.swiftpay.data.repository;

import android.content.Context;
import android.graphics.Bitmap;
import androidx.lifecycle.LiveData;
import com.swiftpay.data.db.SwiftPayDatabase;
import com.swiftpay.data.entity.User;
import com.swiftpay.util.AuditLogger;
import com.swiftpay.util.ImageUtils;
import com.swiftpay.util.PasswordUtils;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;

/**
 * Repository de Autenticación y gestión de usuarios.
 * RF 1.1-1.12: Login, logout, CRUD usuarios, cambio de contraseña, foto de perfil.
 * Todas las operaciones CPU-intensivas (BCrypt) se ejecutan en background thread.
 */
public class AuthRepository {

    private final SwiftPayDatabase db;
    private final Context context;
    private final ExecutorService executor;

    public AuthRepository(Context context) {
        this.context = context.getApplicationContext();
        this.db = SwiftPayDatabase.getInstance(this.context);
        this.executor = Executors.newSingleThreadExecutor();
    }

    // ==================== LOGIN ====================

    /**
     * Intenta autenticar al usuario con username y password.
     * @param callback resultado del login
     */
    public void login(String username, String password, LoginCallback callback) {
        executor.execute(() -> {
            try {
                User user = db.userDao().getByUsername(username);

                if (user == null) {
                    callback.onResult(new LoginResult(false, null, "Usuario no encontrado"));
                    AuditLogger.log(context, null, "LOGIN_FAILED", "USER", null,
                            "Usuario no encontrado: " + username);
                    return;
                }

                if (user.getIsActive() != 1) {
                    callback.onResult(new LoginResult(false, null, "La cuenta está desactivada"));
                    AuditLogger.log(context, user.getId(), "LOGIN_FAILED", "USER", user.getId(),
                            "Cuenta desactivada: " + username);
                    return;
                }

                if (!PasswordUtils.checkPassword(password, user.getPasswordHash())) {
                    callback.onResult(new LoginResult(false, null, "Contraseña incorrecta"));
                    AuditLogger.log(context, user.getId(), "LOGIN_FAILED", "USER", user.getId(),
                            "Contraseña incorrecta: " + username);
                    return;
                }

                // Login exitoso
                AuditLogger.log(context, user.getId(), "LOGIN_SUCCESS", "USER", user.getId(),
                        "Login exitoso: " + username + " [" + user.getRole() + "]");

                callback.onResult(new LoginResult(true, user, null));
            } catch (Exception e) {
                callback.onResult(new LoginResult(false, null, "Error interno: " + e.getMessage()));
            }
        });
    }

    // ==================== CRUD USUARIOS (ADMIN) ====================

    /**
     * Crea un nuevo usuario con contraseña temporal.
     * RF 1.6: Admin crea usuario con nombre, username, password temporal y rol.
     */
    public void createUser(String fullName, String username, String tempPassword, String role,
                           long adminUserId, OperationCallback callback) {
        executor.execute(() -> {
            try {
                // Verificar username único
                User existing = db.userDao().getByUsername(username);
                if (existing != null) {
                    callback.onResult(false, "El nombre de usuario ya existe");
                    return;
                }

                long now = System.currentTimeMillis();
                User user = new User();
                user.setFullName(fullName);
                user.setUsername(username);
                user.setPasswordHash(PasswordUtils.hashPassword(tempPassword));
                user.setRole(role);
                user.setIsActive(1);
                user.setIsTemporaryPassword(1); // contraseña temporal
                user.setCreatedAt(now);
                user.setUpdatedAt(now);

                long newId = db.userDao().insert(user);

                AuditLogger.log(context, adminUserId, "CREATE_USER", "USER", newId,
                        "Usuario creado: " + username + " [" + role + "]");

                callback.onResult(true, "Usuario creado exitosamente");
            } catch (Exception e) {
                callback.onResult(false, "Error al crear usuario: " + e.getMessage());
            }
        });
    }

    /**
     * Desactiva o reactiva un usuario.
     * RF 1.7: Admin puede desactivar/reactivar usuarios.
     */
    public void toggleUserActive(long userId, long adminUserId, OperationCallback callback) {
        executor.execute(() -> {
            try {
                User user = db.userDao().getByIdSync(userId);
                if (user == null) {
                    callback.onResult(false, "Usuario no encontrado");
                    return;
                }

                boolean newState = user.getIsActive() != 1;
                user.setIsActive(newState ? 1 : 0);
                user.setUpdatedAt(System.currentTimeMillis());
                db.userDao().update(user);

                String action = newState ? "ACTIVATE_USER" : "DEACTIVATE_USER";
                AuditLogger.log(context, adminUserId, action, "USER", userId,
                        "Usuario " + (newState ? "activado" : "desactivado") + ": " + user.getUsername());

                callback.onResult(true, "Usuario " + (newState ? "activado" : "desactivado") + " exitosamente");
            } catch (Exception e) {
                callback.onResult(false, "Error: " + e.getMessage());
            }
        });
    }

    /**
     * Restablece la contraseña de un usuario (Admin).
     * RF 1.8: Admin restablece contraseña → usuario forzado a cambiar.
     */
    public void resetPassword(long userId, String newPassword, long adminUserId, OperationCallback callback) {
        executor.execute(() -> {
            try {
                User user = db.userDao().getByIdSync(userId);
                if (user == null) {
                    callback.onResult(false, "Usuario no encontrado");
                    return;
                }

                user.setPasswordHash(PasswordUtils.hashPassword(newPassword));
                user.setIsTemporaryPassword(1); // forzar cambio
                user.setUpdatedAt(System.currentTimeMillis());
                db.userDao().update(user);

                AuditLogger.log(context, adminUserId, "RESET_PASSWORD", "USER", userId,
                        "Contraseña restablecida para: " + user.getUsername());

                callback.onResult(true, "Contraseña restablecida exitosamente");
            } catch (Exception e) {
                callback.onResult(false, "Error: " + e.getMessage());
            }
        });
    }

    // ==================== CAMBIO DE CONTRASEÑA ====================

    /**
     * Cambia la contraseña del usuario actual (verificando la contraseña actual).
     * RF 1.9: Cualquier usuario cambia su contraseña.
     */
    public void changePassword(long userId, String currentPassword, String newPassword,
                               OperationCallback callback) {
        executor.execute(() -> {
            try {
                User user = db.userDao().getByIdSync(userId);
                if (user == null) {
                    callback.onResult(false, "Usuario no encontrado");
                    return;
                }

                if (!PasswordUtils.checkPassword(currentPassword, user.getPasswordHash())) {
                    callback.onResult(false, "La contraseña actual es incorrecta");
                    return;
                }

                user.setPasswordHash(PasswordUtils.hashPassword(newPassword));
                user.setIsTemporaryPassword(0); // ya no es temporal
                user.setUpdatedAt(System.currentTimeMillis());
                db.userDao().update(user);

                AuditLogger.log(context, userId, "CHANGE_PASSWORD", "USER", userId,
                        "Contraseña cambiada por el usuario: " + user.getUsername());

                callback.onResult(true, "Contraseña cambiada exitosamente");
            } catch (Exception e) {
                callback.onResult(false, "Error: " + e.getMessage());
            }
        });
    }

    /**
     * Fuerza el cambio de contraseña (sin verificar la actual).
     * Usado cuando is_temporary_password = 1.
     */
    public void forceChangePassword(long userId, String newPassword, OperationCallback callback) {
        executor.execute(() -> {
            try {
                User user = db.userDao().getByIdSync(userId);
                if (user == null) {
                    callback.onResult(false, "Usuario no encontrado");
                    return;
                }

                user.setPasswordHash(PasswordUtils.hashPassword(newPassword));
                user.setIsTemporaryPassword(0);
                user.setUpdatedAt(System.currentTimeMillis());
                db.userDao().update(user);

                AuditLogger.log(context, userId, "FORCE_CHANGE_PASSWORD", "USER", userId,
                        "Cambio de contraseña obligatorio completado: " + user.getUsername());

                callback.onResult(true, "Contraseña actualizada exitosamente");
            } catch (Exception e) {
                callback.onResult(false, "Error: " + e.getMessage());
            }
        });
    }

    // ==================== PERFIL ====================

    /**
     * Obtiene un usuario por ID (LiveData reactivo).
     */
    public LiveData<User> getUserById(long userId) {
        return db.userDao().getById(userId);
    }

    /**
     * Obtiene todos los usuarios (LiveData reactivo).
     */
    public LiveData<List<User>> getAllUsers() {
        return db.userDao().getAll();
    }

    /**
     * Sube/actualiza la foto de perfil del usuario.
     * RF 1.11: Cualquier usuario sube foto JPG/JPEG/PNG, máx 5MB.
     */
    public void uploadProfilePhoto(long userId, Bitmap bitmap, OperationCallback callback) {
        executor.execute(() -> {
            try {
                String path = ImageUtils.saveProfileImage(context, userId, bitmap);
                if (path == null) {
                    callback.onResult(false, "Error al guardar la imagen");
                    return;
                }

                User user = db.userDao().getByIdSync(userId);
                if (user == null) {
                    callback.onResult(false, "Usuario no encontrado");
                    return;
                }

                user.setProfileImagePath(path);
                user.setUpdatedAt(System.currentTimeMillis());
                db.userDao().update(user);

                AuditLogger.log(context, userId, "UPLOAD_PHOTO", "USER", userId,
                        "Foto de perfil actualizada");

                callback.onResult(true, "Foto de perfil actualizada");
            } catch (Exception e) {
                callback.onResult(false, "Error: " + e.getMessage());
            }
        });
    }

    // ==================== CALLBACKS ====================

    /** Callback para resultado de login */
    public interface LoginCallback {
        void onResult(LoginResult result);
    }

    /** Callback para operaciones genéricas */
    public interface OperationCallback {
        void onResult(boolean success, String message);
    }

    /** Resultado del login */
    public static class LoginResult {
        public final boolean success;
        public final User user;
        public final String errorMessage;

        public LoginResult(boolean success, User user, String errorMessage) {
            this.success = success;
            this.user = user;
            this.errorMessage = errorMessage;
        }
    }
}
