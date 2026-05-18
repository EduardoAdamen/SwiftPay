package com.swiftpay.ui.proveedores;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.google.android.material.textfield.TextInputEditText;
import com.swiftpay.R;
import com.swiftpay.data.entity.Supplier;
import com.swiftpay.util.ValidationUtils;
import com.swiftpay.viewmodel.SupplierViewModel;

public class SupplierFormFragment extends Fragment {

    private SupplierViewModel viewModel;
    private long supplierId = -1;
    private Supplier currentSupplier;

    private TextInputEditText etName, etRfc, etPhone, etEmail, etNotes;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_supplier_form, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        etName = view.findViewById(R.id.et_supplier_name);
        etRfc = view.findViewById(R.id.et_supplier_rfc);
        etPhone = view.findViewById(R.id.et_supplier_phone);
        etEmail = view.findViewById(R.id.et_supplier_email);
        etNotes = view.findViewById(R.id.et_supplier_notes);
        Button btnSave = view.findViewById(R.id.btn_save_supplier);

        viewModel = new ViewModelProvider(this).get(SupplierViewModel.class);

        if (getArguments() != null) {
            supplierId = getArguments().getLong("supplierId", -1);
        }

        if (supplierId != -1) {
            viewModel.getSupplier(supplierId).observe(getViewLifecycleOwner(), supplier -> {
                if (supplier != null && currentSupplier == null) {
                    currentSupplier = supplier;
                    etName.setText(supplier.getName());
                    etRfc.setText(supplier.getRfc());
                    etPhone.setText(supplier.getPhone());
                    etEmail.setText(supplier.getEmail());
                    etNotes.setText(supplier.getNotes());
                }
            });
        }

        btnSave.setOnClickListener(v -> saveSupplier());
    }

    private void saveSupplier() {
        String name = etName.getText().toString().trim();
        String rfc = etRfc.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String notes = etNotes.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            etName.setError("El nombre es requerido");
            return;
        }

        if (!TextUtils.isEmpty(rfc) && !ValidationUtils.isValidRFC(rfc)) {
            etRfc.setError("Formato de RFC inválido");
            return;
        }

        if (supplierId == -1) {
            Supplier s = new Supplier();
            s.setName(name);
            s.setRfc(TextUtils.isEmpty(rfc) ? null : rfc);
            s.setPhone(TextUtils.isEmpty(phone) ? null : phone);
            s.setEmail(TextUtils.isEmpty(email) ? null : email);
            s.setNotes(TextUtils.isEmpty(notes) ? null : notes);
            s.setCreatedAt(System.currentTimeMillis());
            s.setUpdatedAt(System.currentTimeMillis());
            viewModel.insert(s);
            Toast.makeText(getContext(), "Proveedor creado", Toast.LENGTH_SHORT).show();
        } else {
            if (currentSupplier != null) {
                currentSupplier.setName(name);
                currentSupplier.setRfc(TextUtils.isEmpty(rfc) ? null : rfc);
                currentSupplier.setPhone(TextUtils.isEmpty(phone) ? null : phone);
                currentSupplier.setEmail(TextUtils.isEmpty(email) ? null : email);
                currentSupplier.setNotes(TextUtils.isEmpty(notes) ? null : notes);
                currentSupplier.setUpdatedAt(System.currentTimeMillis());
                viewModel.update(currentSupplier);
                Toast.makeText(getContext(), "Proveedor actualizado", Toast.LENGTH_SHORT).show();
            }
        }
        Navigation.findNavController(getView()).navigateUp();
    }
}
