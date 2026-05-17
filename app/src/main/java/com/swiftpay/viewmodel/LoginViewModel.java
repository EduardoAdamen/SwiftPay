package com.swiftpay.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.swiftpay.data.entity.User;
import com.swiftpay.data.repository.AuthRepository;

/**
 * ViewModel para la pantalla de Login.
 * RF 1.1: Login con credenciales contra BD.
 * RF 1.2: Mensaje de error claro en credenciales incorrectas.
 */
public class LoginViewModel extends AndroidViewModel {

    private final AuthRepository authRepository;
    private final MutableLiveData<LoginUiState> loginState = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);

    public LoginViewModel(@NonNull Application application) {
        super(application);
        this.authRepository = new AuthRepository(application);
    }

    public LiveData<LoginUiState> getLoginState() {
        return loginState;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    /**
     * Ejecuta el login. El resultado se emite a través de loginState.
     */
    public void login(String username, String password) {
        if (username == null || username.trim().isEmpty()) {
            loginState.setValue(new LoginUiState(false, null,
                    false, "Ingresa tu nombre de usuario"));
            return;
        }
        if (password == null || password.isEmpty()) {
            loginState.setValue(new LoginUiState(false, null,
                    false, "Ingresa tu contraseña"));
            return;
        }

        isLoading.setValue(true);
        authRepository.login(username.trim(), password, result -> {
            isLoading.postValue(false);
            if (result.success && result.user != null) {
                boolean requiresPasswordChange = result.user.getIsTemporaryPassword() == 1;
                loginState.postValue(new LoginUiState(true, result.user,
                        requiresPasswordChange, null));
            } else {
                loginState.postValue(new LoginUiState(false, null,
                        false, result.errorMessage));
            }
        });
    }

    /**
     * Estado de la UI de login.
     */
    public static class LoginUiState {
        public final boolean success;
        public final User user;
        public final boolean requiresPasswordChange;
        public final String errorMessage;

        public LoginUiState(boolean success, User user, boolean requiresPasswordChange, String errorMessage) {
            this.success = success;
            this.user = user;
            this.requiresPasswordChange = requiresPasswordChange;
            this.errorMessage = errorMessage;
        }
    }
}
