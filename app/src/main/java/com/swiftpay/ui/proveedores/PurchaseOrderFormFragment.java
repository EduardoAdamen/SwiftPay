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
import com.swiftpay.data.entity.Product;
import com.swiftpay.data.entity.PurchaseOrder;
import com.swiftpay.data.entity.Supplier;
import com.swiftpay.ui.adapter.DraftItemAdapter;
import com.swiftpay.ui.adapter.ProductListAdapter;
import com.swiftpay.viewmodel.ProductViewModel;
import com.swiftpay.viewmodel.PurchaseOrderViewModel;
import com.swiftpay.viewmodel.SupplierViewModel;
import com.google.android.material.textfield.TextInputEditText;
import androidx.appcompat.app.AlertDialog;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PurchaseOrderFormFragment extends Fragment {

    private PurchaseOrderViewModel poViewModel;
    private SupplierViewModel supplierViewModel;
    private ProductViewModel productViewModel;
    private Spinner spinnerSupplier;
    private List<Supplier> supplierList = new ArrayList<>();
    private DraftItemAdapter draftAdapter;
    
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
        
        draftAdapter = new DraftItemAdapter(productId -> poViewModel.removeDraftItem(productId));
        rvItems.setAdapter(draftAdapter);
        
        poViewModel = new ViewModelProvider(this).get(PurchaseOrderViewModel.class);
        supplierViewModel = new ViewModelProvider(this).get(SupplierViewModel.class);
        productViewModel = new ViewModelProvider(this).get(ProductViewModel.class);

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
                draftAdapter.submitList(items);
            }
            tvTotal.setText(String.format(Locale.getDefault(), "Total: $%.2f", total));
        });

        view.findViewById(R.id.btn_add_product_to_order).setOnClickListener(v -> showProductSelectionDialog());

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

    private void showProductSelectionDialog() {
        int selectedPosition = spinnerSupplier.getSelectedItemPosition();
        if (selectedPosition < 0 || selectedPosition >= supplierList.size()) {
            Toast.makeText(getContext(), "Selecciona un proveedor primero", Toast.LENGTH_SHORT).show();
            return;
        }

        long supplierId = supplierList.get(selectedPosition).getId();
        
        productViewModel.fetchActiveProductsBySupplier(supplierId, products -> {
            if (products == null || products.isEmpty()) {
                Toast.makeText(getContext(), "Este proveedor no tiene productos asignados", Toast.LENGTH_SHORT).show();
                return;
            }

            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
            builder.setTitle("Seleccionar Producto");

            RecyclerView rvProducts = new RecyclerView(requireContext());
            rvProducts.setLayoutManager(new LinearLayoutManager(requireContext()));
            
            AlertDialog dialog = builder.setView(rvProducts)
                    .setNegativeButton("Cancelar", null)
                    .create();

            ProductListAdapter adapter = new ProductListAdapter(product -> {
                dialog.dismiss();
                showQuantityCostDialog(product);
            });
            adapter.submitList(products);
            rvProducts.setAdapter(adapter);

            dialog.show();
        });
    }

    private void showQuantityCostDialog(Product product) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_purchase_item, null);
        builder.setView(dialogView);
        builder.setTitle(product.getName());

        TextInputEditText etQuantity = dialogView.findViewById(R.id.et_quantity);
        TextInputEditText etCost = dialogView.findViewById(R.id.et_unit_cost);

        builder.setPositiveButton("Añadir", (dialog, which) -> {
            String qtyStr = etQuantity.getText() != null ? etQuantity.getText().toString() : "";
            String costStr = etCost.getText() != null ? etCost.getText().toString() : "";

            try {
                int qty = Integer.parseInt(qtyStr);
                double cost = Double.parseDouble(costStr);
                
                if (qty > 0 && cost >= 0) {
                    poViewModel.addDraftItem(new OrderItemDraft(product.getId(), product.getName(), qty, cost));
                } else {
                    Toast.makeText(getContext(), "Valores inválidos", Toast.LENGTH_SHORT).show();
                }
            } catch (NumberFormatException e) {
                Toast.makeText(getContext(), "Valores requeridos", Toast.LENGTH_SHORT).show();
            }
        });
        
        builder.setNegativeButton("Cancelar", null);
        builder.show();
    }
}
