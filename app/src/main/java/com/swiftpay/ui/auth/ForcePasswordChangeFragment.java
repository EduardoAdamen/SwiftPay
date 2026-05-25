// app/src/main/java/com/swiftpay/ui/auth/ForcePasswordChangeFragment.java
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
import androidx.navigation.NavOptions;
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
 * Fragment para cambio obligatorio de contraseña temporal.
 * RF 1.8: Usuario forzado a cambiar contraseña temporal al siguiente login.
 */
public class ForcePasswordChangeFragment extends Fragment {

    private UserViewModel viewModel;
    private TextInputLayout tilNew, tilConfirm;
    private TextInputEditText etNew, etConfirm;
    private MaterialButton btnSave;
    private CircularProgressIndicator progress;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_force_password_change, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(UserViewModel.class);

        tilNew = view.findViewById(R.id.til_new_password);
        tilConfirm = view.findViewById(R.id.til_confirm_password);
        etNew = view.findViewById(R.id.et_new_password);
        etConfirm = view.findViewById(R.id.et_confirm_password);
        btnSave = view.findViewById(R.id.btn_save_password);
        progress = view.findViewById(R.id.progress_password);

        btnSave.setOnClickListener(v -> attemptChange());

        viewModel.getOperationSuccess().observe(getViewLifecycleOwner(), success -> {
            if (success != null && success) {
                Toast.makeText(requireContext(), R.string.password_changed_success, Toast.LENGTH_SHORT).show();
                SessionManager session = ((MainActivity) requireActivity()).getSessionManager();
                session.markTemporaryPasswordResolved();
                ((MainActivity) requireActivity()).setDrawerLocked(false);
                NavOptions options = new NavOptions.Builder()
                        .setPopUpTo(R.id.nav_graph, true)
                        .build();
                ((MainActivity) requireActivity()).getNavController().navigate(R.id.nav_dashboard, null, options);
            }
        });

        viewModel.getOperationMessage().observe(getViewLifecycleOwner(), msg -> {
            if (msg != null) {
                Boolean success = viewModel.getOperationSuccess().getValue();
                if (success == null || !success) {
                    tilNew.setError(msg);
                }
            }
        });

        viewModel.getIsLoading().observe(getViewLifecycleOwner(), loading -> {
            progress.setVisibility(loading ? View.VISIBLE : View.GONE);
            btnSave.setEnabled(!loading);
        });
    }

    private void attemptChange() {
        tilNew.setError(null);
        tilConfirm.setError(null);

        String newPwd = etNew.getText() != null ? etNew.getText().toString() : "";
        String confirm = etConfirm.getText() != null ? etConfirm.getText().toString() : "";

        String pwdError = ValidationUtils.validatePassword(newPwd);
        if (pwdError != null) { tilNew.setError(pwdError); return; }
        if (!newPwd.equals(confirm)) { tilConfirm.setError("Las contraseñas no coinciden"); return; }

        SessionManager session = ((MainActivity) requireActivity()).getSessionManager();
        viewModel.forceChangePassword(session.getUserId(), newPwd);
    }

    @Override
    public void onResume() {
        super.onResume();
        // Bloquear drawer durante cambio forzado
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).setDrawerLocked(true);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).setDrawerLocked(false);
        }
    }
}
