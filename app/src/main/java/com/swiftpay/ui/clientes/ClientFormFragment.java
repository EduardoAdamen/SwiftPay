// app/src/main/java/com/swiftpay/ui/clientes/ClientFormFragment.java
package com.swiftpay.ui.clientes;

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
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.swiftpay.MainActivity;
import com.swiftpay.R;
import com.swiftpay.data.entity.Client;
import com.swiftpay.util.ValidationUtils;
import com.swiftpay.viewmodel.ClientViewModel;

/**
 * Client form with strict fiscal, email and phone validation before persistence.
 * Supports both creating and editing clients.
 */
public class ClientFormFragment extends Fragment {

    private ClientViewModel viewModel;
    private long clientId = -1;
    private Client currentClient;

    private TextInputLayout tilName;
    private TextInputLayout tilPhone;
    private TextInputLayout tilEmail;
    private TextInputLayout tilRfc;
    private TextInputEditText etName;
    private TextInputEditText etPhone;
    private TextInputEditText etEmail;
    private TextInputEditText etRfc;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_client_form, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(ClientViewModel.class);

        if (getArguments() != null) {
            clientId = getArguments().getLong("clientId", -1);
        }

        tilName = view.findViewById(R.id.til_client_name);
        tilPhone = view.findViewById(R.id.til_client_phone);
        tilEmail = view.findViewById(R.id.til_client_email);
        tilRfc = view.findViewById(R.id.til_client_rfc);
        etName = view.findViewById(R.id.et_client_name);
        etPhone = view.findViewById(R.id.et_client_phone);
        etEmail = view.findViewById(R.id.et_client_email);
        etRfc = view.findViewById(R.id.et_client_rfc);
        MaterialButton saveButton = view.findViewById(R.id.btn_save_client);

        if (clientId != -1) {
            viewModel.getClientById(clientId).observe(getViewLifecycleOwner(), client -> {
                if (client != null) {
                    currentClient = client;
                    etName.setText(client.getFullName());
                    etPhone.setText(client.getPhone() != null ? client.getPhone() : "");
                    etEmail.setText(client.getEmail() != null ? client.getEmail() : "");
                    etRfc.setText(client.getRfc() != null ? client.getRfc() : "");
                }
            });
        }

        saveButton.setOnClickListener(v -> saveClient());

        viewModel.getOperationSuccess().observe(getViewLifecycleOwner(), success -> {
            if (Boolean.TRUE.equals(success)) {
                Toast.makeText(requireContext(), "Cliente guardado exitosamente", Toast.LENGTH_SHORT).show();
                requireActivity().getOnBackPressedDispatcher().onBackPressed();
            }
        });
        viewModel.getOperationMessage().observe(getViewLifecycleOwner(), message -> {
            if (message != null && !message.isEmpty() && !Boolean.TRUE.equals(viewModel.getOperationSuccess().getValue())) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show();
            }
        });
        viewModel.getIsLoading().observe(getViewLifecycleOwner(), loading -> saveButton.setEnabled(!Boolean.TRUE.equals(loading)));
    }

    private void saveClient() {
        clearErrors();
        String name = read(etName);
        String phone = read(etPhone);
        String email = read(etEmail);
        String rfc = read(etRfc).toUpperCase();

        boolean valid = true;
        if (name.isEmpty()) {
            tilName.setError("El nombre es obligatorio");
            valid = false;
        }
        if (!phone.isEmpty() && !ValidationUtils.isValidPhone(phone)) {
            tilPhone.setError("El telefono debe tener 10 digitos");
            valid = false;
        }
        if (!email.isEmpty() && !ValidationUtils.isValidEmail(email)) {
            tilEmail.setError("Correo electronico invalido");
            valid = false;
        }
        if (!rfc.isEmpty() && !ValidationUtils.isValidRFC(rfc)) {
            tilRfc.setError("RFC invalido");
            valid = false;
        }
        if (!valid) {
            return;
        }

        if (currentClient == null) {
            currentClient = new Client();
            currentClient.setIsActive(1);
        }
        currentClient.setFullName(name);
        currentClient.setPhone(phone.isEmpty() ? null : phone);
        currentClient.setEmail(email.isEmpty() ? null : email);
        currentClient.setRfc(rfc.isEmpty() ? null : rfc);

        long userId = ((MainActivity) requireActivity()).getSessionManager().getUserId();
        viewModel.saveClient(currentClient, userId);
    }

    private String read(TextInputEditText editText) {
        return editText.getText() == null ? "" : editText.getText().toString().trim();
    }

    private void clearErrors() {
        tilName.setError(null);
        tilPhone.setError(null);
        tilEmail.setError(null);
        tilRfc.setError(null);
    }
}
