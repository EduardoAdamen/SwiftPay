package com.swiftpay.ui.auth;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
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
import com.swiftpay.util.ValidationUtils;
import com.swiftpay.viewmodel.UserViewModel;

/**
 * Fragment para cambio de contraseña (usuario autenticado).
 * RF 1.9: Cambiar contraseña verificando la actual. Mínimo 8 chars, letras+números.
 */
public class ChangePasswordFragment extends Fragment {

    private UserViewModel viewModel;
    private TextInputLayout tilCurrent, tilNew, tilConfirm;
    private TextInputEditText etCurrent, etNew, etConfirm;
    private MaterialButton btnSave;
    private CircularProgressIndicator progress;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_change_password, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(UserViewModel.class);

        tilCurrent = view.findViewById(R.id.til_current_password);
        tilNew = view.findViewById(R.id.til_new_password);
        tilConfirm = view.findViewById(R.id.til_confirm_password);
        etCurrent = view.findViewById(R.id.et_current_password);
        etNew = view.findViewById(R.id.et_new_password);
        etConfirm = view.findViewById(R.id.et_confirm_password);
        btnSave = view.findViewById(R.id.btn_save_password);
        progress = view.findViewById(R.id.progress_password);

        btnSave.setOnClickListener(v -> attemptChangePassword());

        viewModel.getOperationSuccess().observe(getViewLifecycleOwner(), success -> {
            if (success != null && success) {
                Toast.makeText(requireContext(), R.string.password_changed_success, Toast.LENGTH_SHORT).show();
                requireActivity().onBackPressed();
            }
        });

        viewModel.getOperationMessage().observe(getViewLifecycleOwner(), msg -> {
            if (msg != null && !msg.isEmpty()) {
                Boolean success = viewModel.getOperationSuccess().getValue();
                if (success == null || !success) {
                    tilCurrent.setError(msg);
                }
            }
        });

        viewModel.getIsLoading().observe(getViewLifecycleOwner(), loading -> {
            progress.setVisibility(loading ? View.VISIBLE : View.GONE);
            btnSave.setEnabled(!loading);
        });
    }

    private void attemptChangePassword() {
        tilCurrent.setError(null);
        tilNew.setError(null);
        tilConfirm.setError(null);

        String current = getText(etCurrent);
        String newPwd = getText(etNew);
        String confirm = getText(etConfirm);

        if (current.isEmpty()) { tilCurrent.setError("Ingresa tu contraseña actual"); return; }

        String pwdError = ValidationUtils.validatePassword(newPwd);
        if (pwdError != null) { tilNew.setError(pwdError); return; }

        if (!newPwd.equals(confirm)) { tilConfirm.setError("Las contraseñas no coinciden"); return; }

        SessionManager session = ((MainActivity) requireActivity()).getSessionManager();
        viewModel.changePassword(session.getUserId(), current, newPwd);
    }

    private String getText(TextInputEditText et) {
        return et.getText() != null ? et.getText().toString() : "";
    }
}
