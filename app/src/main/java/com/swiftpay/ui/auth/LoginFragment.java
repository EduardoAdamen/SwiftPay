package com.swiftpay.ui.auth;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.swiftpay.MainActivity;
import com.swiftpay.R;
import com.swiftpay.data.preferences.SessionManager;
import com.swiftpay.viewmodel.LoginViewModel;

/**
 * Fragment de Login. Implementación completa.
 * RF 1.1: Validación de credenciales contra BD.
 * RF 1.2: Mensajes de error claros.
 * UX-A1: Interfaz de login intuitiva.
 */
public class LoginFragment extends Fragment {

    private LoginViewModel viewModel;
    private TextInputLayout tilUsername, tilPassword;
    private TextInputEditText etUsername, etPassword;
    private MaterialButton btnLogin;
    private TextView tvError;
    private CircularProgressIndicator progress;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(LoginViewModel.class);

        // Bind views
        tilUsername = view.findViewById(R.id.til_username);
        tilPassword = view.findViewById(R.id.til_password);
        etUsername = view.findViewById(R.id.et_username);
        etPassword = view.findViewById(R.id.et_password);
        btnLogin = view.findViewById(R.id.btn_login);
        tvError = view.findViewById(R.id.tv_error_message);
        progress = view.findViewById(R.id.progress_login);

        // Login button
        btnLogin.setOnClickListener(v -> {
            clearErrors();
            String username = etUsername.getText() != null ? etUsername.getText().toString() : "";
            String password = etPassword.getText() != null ? etPassword.getText().toString() : "";
            viewModel.login(username, password);
        });

        // Observe login state
        viewModel.getLoginState().observe(getViewLifecycleOwner(), state -> {
            if (state == null) return;
            if (state.success && state.user != null) {
                // Login exitoso → crear sesión
                SessionManager session = getMainActivity().getSessionManager();
                session.createSession(
                        state.user.getId(),
                        state.user.getUsername(),
                        state.user.getFullName(),
                        state.user.getRole(),
                        state.requiresPasswordChange
                );

                // Configurar menú según rol
                getMainActivity().setupMenuForRole(state.user.getRole());
                getMainActivity().setDrawerLocked(false);

                // Navegar al destino correcto
                if (state.requiresPasswordChange) {
                    getMainActivity().getNavController().navigate(R.id.forcePasswordChangeFragment);
                } else {
                    getMainActivity().getNavController().navigate(R.id.nav_dashboard);
                }
            } else if (state.errorMessage != null) {
                tvError.setText(state.errorMessage);
                tvError.setVisibility(View.VISIBLE);
            }
        });

        // Observe loading
        viewModel.getIsLoading().observe(getViewLifecycleOwner(), loading -> {
            progress.setVisibility(loading ? View.VISIBLE : View.GONE);
            btnLogin.setEnabled(!loading);
        });
    }

    private void clearErrors() {
        tilUsername.setError(null);
        tilPassword.setError(null);
        tvError.setVisibility(View.GONE);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).setDrawerLocked(true);
            if (((MainActivity) getActivity()).getSupportActionBar() != null) {
                ((MainActivity) getActivity()).getSupportActionBar().hide();
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).setDrawerLocked(false);
            if (((MainActivity) getActivity()).getSupportActionBar() != null) {
                ((MainActivity) getActivity()).getSupportActionBar().show();
            }
        }
    }

    private MainActivity getMainActivity() {
        return (MainActivity) requireActivity();
    }
}
