package com.swiftpay.ui.proveedores;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.swiftpay.R;
import com.swiftpay.ui.adapter.SupplierAdapter;
import com.swiftpay.viewmodel.SupplierViewModel;

public class SupplierListFragment extends Fragment {

    private SupplierViewModel viewModel;
    private SupplierAdapter adapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_supplier_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView recyclerView = view.findViewById(R.id.rv_suppliers);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        
        adapter = new SupplierAdapter(supplier -> {
            Bundle bundle = new Bundle();
            bundle.putLong("supplierId", supplier.getId());
            Navigation.findNavController(view).navigate(R.id.action_supplierList_to_supplierDetail, bundle);
        });
        recyclerView.setAdapter(adapter);

        viewModel = new ViewModelProvider(this).get(SupplierViewModel.class);
        viewModel.getAllSuppliers().observe(getViewLifecycleOwner(), suppliers -> {
            adapter.setSuppliers(suppliers);
        });

        view.findViewById(R.id.fab_add_supplier).setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.action_supplierList_to_supplierForm);
        });
    }
}
