package com.swiftpay.ui.auth;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.textfield.TextInputEditText;
import com.swiftpay.MainActivity;
import com.swiftpay.R;
import com.swiftpay.data.entity.User;
import com.swiftpay.data.preferences.SessionManager;
import com.swiftpay.viewmodel.UserViewModel;

public class UserFormFragment extends Fragment {

    private UserViewModel viewModel;
    private SessionManager sessionManager;

    private TextInputEditText etFullName, etUsername, etPassword;
    private Spinner spinnerRole;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_user_form, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        sessionManager = ((MainActivity) requireActivity()).getSessionManager();
        viewModel = new ViewModelProvider(this).get(UserViewModel.class);

        etFullName = view.findViewById(R.id.et_user_full_name);
        etUsername = view.findViewById(R.id.et_user_username);
        etPassword = view.findViewById(R.id.et_user_password);
        spinnerRole = view.findViewById(R.id.spinner_user_role);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(requireContext(),
                R.array.user_roles, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRole.setAdapter(adapter);

        Button btnSave = view.findViewById(R.id.btn_save_user);
        btnSave.setOnClickListener(v -> saveUser());
    }

    private void saveUser() {
        String fullName = etFullName.getText().toString().trim();
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString();
        String role = spinnerRole.getSelectedItem().toString();

        if (TextUtils.isEmpty(fullName) || TextUtils.isEmpty(username) || TextUtils.isEmpty(password)) {
            Toast.makeText(requireContext(), "Por favor llena todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        viewModel.createUser(fullName, username, password, role, sessionManager.getUserId());
        Toast.makeText(requireContext(), "Usuario creado", Toast.LENGTH_SHORT).show();
        requireActivity().getOnBackPressedDispatcher().onBackPressed();
    }
}