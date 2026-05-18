package com.swiftpay.ui.proveedores;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.swiftpay.R;
import com.swiftpay.data.entity.OrderItemDraft;
import com.swiftpay.data.entity.PurchaseOrder;
import com.swiftpay.data.entity.Supplier;
import com.swiftpay.viewmodel.PurchaseOrderViewModel;
import com.swiftpay.viewmodel.SupplierViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PurchaseOrderFormFragment extends Fragment {

    private PurchaseOrderViewModel poViewModel;
    private SupplierViewModel supplierViewModel;
    private Spinner spinnerSupplier;
    private List<Supplier> supplierList = new ArrayList<>();
    
    private long orderId = -1;
    private PurchaseOrder existingOrder;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_purchase_order_form, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        spinnerSupplier = view.findViewById(R.id.spinner_supplier);
        TextView tvTotal = view.findViewById(R.id.tv_draft_total);
        Button btnSave = view.findViewById(R.id.btn_save_order);
        RecyclerView rvItems = view.findViewById(R.id.rv_draft_items);
        rvItems.setLayoutManager(new LinearLayoutManager(getContext()));
        
        poViewModel = new ViewModelProvider(this).get(PurchaseOrderViewModel.class);
        supplierViewModel = new ViewModelProvider(this).get(SupplierViewModel.class);

        if (getArguments() != null) {
            orderId = getArguments().getLong("orderId", -1);
        }

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, new ArrayList<>());
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSupplier.setAdapter(spinnerAdapter);

        supplierViewModel.getAllSuppliers().observe(getViewLifecycleOwner(), suppliers -> {
            supplierList = suppliers;
            spinnerAdapter.clear();
            for (Supplier s : suppliers) {
                spinnerAdapter.add(s.getName());
            }
            spinnerAdapter.notifyDataSetChanged();
            
            // If editing, try to select the supplier
            if (existingOrder != null) {
                for (int i = 0; i < supplierList.size(); i++) {
                    if (supplierList.get(i).getId() == existingOrder.getSupplierId()) {
                        spinnerSupplier.setSelection(i);
                        break;
                    }
                }
            }
        });

        if (orderId != -1) {
            poViewModel.getOrderById(orderId).observe(getViewLifecycleOwner(), order -> {
                if (order != null && existingOrder == null) {
                    existingOrder = order;
                    // Note: Here we would trigger loading items into draft via poViewModel.initDraftFromExisting
                    // But for this sprint, we assume it's handled or empty initially.
                }
            });
        }

        poViewModel.getDraftItems().observe(getViewLifecycleOwner(), items -> {
            double total = 0;
            if (items != null) {
                for (OrderItemDraft d : items) total += d.getSubtotal();
                // We'd also update the rvItems adapter here.
            }
            tvTotal.setText(String.format(Locale.getDefault(), "Total: $%.2f", total));
        });

        view.findViewById(R.id.btn_add_product_to_order).setOnClickListener(v -> {
            // Mocking adding a product for the test since we lack ProductList picker in this sprint context
            poViewModel.addDraftItem(new OrderItemDraft(1, "Producto Test", 5, 10.5));
            Toast.makeText(getContext(), "Producto mock añadido al carrito", Toast.LENGTH_SHORT).show();
        });

        btnSave.setOnClickListener(v -> {
            int selectedPosition = spinnerSupplier.getSelectedItemPosition();
            if (selectedPosition >= 0 && selectedPosition < supplierList.size()) {
                long supId = supplierList.get(selectedPosition).getId();
                if (orderId == -1) {
                    poViewModel.createOrder(supId);
                } else {
                    if (existingOrder != null) {
                        existingOrder.setSupplierId(supId);
                        poViewModel.updatePendingOrder(existingOrder);
                    }
                }
            } else {
                Toast.makeText(getContext(), "Selecciona un proveedor", Toast.LENGTH_SHORT).show();
            }
        });

        poViewModel.getOperationStatus().observe(getViewLifecycleOwner(), status -> {
            if ("SUCCESS".equals(status) || "SUCCESS_UPDATE".equals(status)) {
                Toast.makeText(getContext(), "Orden guardada con éxito", Toast.LENGTH_SHORT).show();
                poViewModel.resetStatus();
                Navigation.findNavController(view).navigateUp();
            } else if (status != null && status.startsWith("ERROR")) {
                Toast.makeText(getContext(), status, Toast.LENGTH_LONG).show();
                poViewModel.resetStatus();
            }
        });
    }
}
