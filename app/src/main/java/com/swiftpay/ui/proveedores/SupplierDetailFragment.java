package com.swiftpay.ui.proveedores;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.swiftpay.R;
import com.swiftpay.viewmodel.SupplierViewModel;

public class SupplierDetailFragment extends Fragment {

    private SupplierViewModel viewModel;
    private long supplierId;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_supplier_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() != null) {
            supplierId = getArguments().getLong("supplierId", -1);
        }

        TextView tvName = view.findViewById(R.id.tv_detail_name);
        TextView tvRfc = view.findViewById(R.id.tv_detail_rfc);
        TextView tvPhone = view.findViewById(R.id.tv_detail_phone);
        TextView tvEmail = view.findViewById(R.id.tv_detail_email);
        TextView tvNotes = view.findViewById(R.id.tv_detail_notes);

        viewModel = new ViewModelProvider(this).get(SupplierViewModel.class);
        if (supplierId != -1) {
            viewModel.getSupplier(supplierId).observe(getViewLifecycleOwner(), supplier -> {
                if (supplier != null) {
                    tvName.setText(supplier.getName());
                    tvRfc.setText("RFC: " + (supplier.getRfc() != null ? supplier.getRfc() : "N/A"));
                    tvPhone.setText("Teléfono: " + (supplier.getPhone() != null ? supplier.getPhone() : "N/A"));
                    tvEmail.setText("Email: " + (supplier.getEmail() != null ? supplier.getEmail() : "N/A"));
                    tvNotes.setText("Notas: " + (supplier.getNotes() != null ? supplier.getNotes() : "N/A"));
                }
            });
        }

        view.findViewById(R.id.btn_edit_supplier).setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putLong("supplierId", supplierId);
            Navigation.findNavController(v).navigate(R.id.action_supplierDetail_to_supplierForm, bundle);
        });
    }
}
