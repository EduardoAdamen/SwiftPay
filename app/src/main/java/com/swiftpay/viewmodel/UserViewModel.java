package com.swiftpay.viewmodel;

import android.app.Application;
import android.graphics.Bitmap;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.swiftpay.data.entity.User;
import com.swiftpay.data.repository.AuthRepository;
import java.util.List;

/**
 * ViewModel para gestión de usuarios (CRUD admin) y perfil de usuario.
 * RF 1.6-1.12: Crear, desactivar, restablecer contraseña, cambiar contraseña, foto perfil.
 */
public class UserViewModel extends AndroidViewModel {

    private final AuthRepository authRepository;
    private final MutableLiveData<String> operationMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> operationSuccess = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);

    public UserViewModel(@NonNull Application application) {
        super(application);
        this.authRepository = new AuthRepository(application);
    }

    // --- Observables ---

    public LiveData<String> getOperationMessage() { return operationMessage; }
    public LiveData<Boolean> getOperationSuccess() { return operationSuccess; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }

    /** Obtiene un usuario reactivo por ID */
    public LiveData<User> getUserById(long userId) {
        return authRepository.getUserById(userId);
    }

    /** Obtiene todos los usuarios (para lista de admin) */
    public LiveData<List<User>> getAllUsers() {
        return authRepository.getAllUsers();
    }

    // --- Operaciones CRUD (Admin) ---

    /** Crea un nuevo usuario con contraseña temporal */
    public void createUser(String fullName, String username, String tempPassword, String role, long adminUserId) {
        isLoading.setValue(true);
        authRepository.createUser(fullName, username, tempPassword, role, adminUserId,
                (success, message) -> {
                    isLoading.postValue(false);
                    operationSuccess.postValue(success);
                    operationMessage.postValue(message);
                });
    }

    /** Activa/desactiva un usuario */
    public void toggleUserActive(long userId, long adminUserId) {
        isLoading.setValue(true);
        authRepository.toggleUserActive(userId, adminUserId,
                (success, message) -> {
                    isLoading.postValue(false);
                    operationSuccess.postValue(success);
                    operationMessage.postValue(message);
                });
    }

    /** Restablece la contraseña de un usuario (Admin) */
    public void resetPassword(long userId, String newPassword, long adminUserId) {
        isLoading.setValue(true);
        authRepository.resetPassword(userId, newPassword, adminUserId,
                (success, message) -> {
                    isLoading.postValue(false);
                    operationSuccess.postValue(success);
                    operationMessage.postValue(message);
                });
    }

    // --- Cambio de contraseña ---

    /** Cambia la contraseña del usuario actual (verificando la actual) */
    public void changePassword(long userId, String currentPassword, String newPassword) {
        isLoading.setValue(true);
        authRepository.changePassword(userId, currentPassword, newPassword,
                (success, message) -> {
                    isLoading.postValue(false);
                    operationSuccess.postValue(success);
                    operationMessage.postValue(message);
                });
    }

    /** Fuerza el cambio de contraseña (sin verificar la actual) */
    public void forceChangePassword(long userId, String newPassword) {
        isLoading.setValue(true);
        authRepository.forceChangePassword(userId, newPassword,
                (success, message) -> {
                    isLoading.postValue(false);
                    operationSuccess.postValue(success);
                    operationMessage.postValue(message);
                });
    }

    // --- Perfil ---

    /** Sube/actualiza la foto de perfil */
    public void uploadProfilePhoto(long userId, Bitmap bitmap) {
        isLoading.setValue(true);
        authRepository.uploadProfilePhoto(userId, bitmap,
                (success, message) -> {
                    isLoading.postValue(false);
                    operationSuccess.postValue(success);
                    operationMessage.postValue(message);
                });
    }
}
