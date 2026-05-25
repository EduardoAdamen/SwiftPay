package com.swiftpay.ui.catalog;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.swiftpay.MainActivity;
import com.swiftpay.R;
import com.swiftpay.data.entity.Product;
import com.swiftpay.util.ValidationUtils;
import com.swiftpay.viewmodel.ProductViewModel;

public class ProductFormFragment extends Fragment {

    private ProductViewModel viewModel;
    private long productId = -1;
    private Product currentProduct;

    private TextInputLayout tilSku, tilName, tilPrice, tilStock;
    private TextInputEditText etSku, etName, etPrice, etStock;
    private MaterialButton btnSave;

    public ProductFormFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_product_form, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(ProductViewModel.class);

        if (getArguments() != null) {
            productId = getArguments().getLong("productId", -1);
        }

        tilSku = view.findViewById(R.id.til_sku);
        tilName = view.findViewById(R.id.til_name);
        tilPrice = view.findViewById(R.id.til_price);
        tilStock = view.findViewById(R.id.til_stock);
        
        etSku = view.findViewById(R.id.et_sku);
        etName = view.findViewById(R.id.et_name);
        etPrice = view.findViewById(R.id.et_price);
        etStock = view.findViewById(R.id.et_stock);
        btnSave = view.findViewById(R.id.btn_save);

        if (productId != -1) {
            viewModel.getProductById(productId).observe(getViewLifecycleOwner(), product -> {
                if (product != null) {
                    currentProduct = product;
                    etSku.setText(product.getSku());
                    etName.setText(product.getName());
                    etPrice.setText(String.valueOf(product.getPrice()));
                    etStock.setText(String.valueOf(product.getStock()));
                }
            });
        }

        btnSave.setOnClickListener(v -> saveProduct());

        viewModel.getOperationSuccess().observe(getViewLifecycleOwner(), success -> {
            if (Boolean.TRUE.equals(success)) {
                Toast.makeText(requireContext(), "Producto guardado", Toast.LENGTH_SHORT).show();
                requireActivity().getOnBackPressedDispatcher().onBackPressed();
            }
        });

        viewModel.getOperationMessage().observe(getViewLifecycleOwner(), msg -> {
            if (msg != null && !Boolean.TRUE.equals(viewModel.getOperationSuccess().getValue())) {
                // RNF 4.4: No conflict dialog in ProductFormFragment
                // The repository throws OptimisticLockException which sets the message to:
                // "El producto fue modificado por otro usuario..."
                if (msg.contains("modificado por otro usuario")) {
                    new AlertDialog.Builder(requireContext())
                        .setTitle(R.string.optimistic_lock_title)
                        .setMessage(R.string.optimistic_lock_message)
                        .setPositiveButton(R.string.btn_retry, (dialog, which) -> {
                            // Reload the product
                            if (productId != -1) {
                                viewModel.getProductById(productId).removeObservers(getViewLifecycleOwner());
                                viewModel.getProductById(productId).observe(getViewLifecycleOwner(), product -> {
                                    if (product != null) {
                                        currentProduct = product;
                                        etSku.setText(product.getSku());
                                        etName.setText(product.getName());
                                        etPrice.setText(String.valueOf(product.getPrice()));
                                        etStock.setText(String.valueOf(product.getStock()));
                                    }
                                });
                            }
                        })
                        .setNegativeButton(R.string.btn_cancel, null)
                        .show();
                } else {
                    Toast.makeText(requireContext(), msg, Toast.LENGTH_LONG).show();
                }
            }
        });
        
        viewModel.getIsLoading().observe(getViewLifecycleOwner(), loading -> btnSave.setEnabled(!Boolean.TRUE.equals(loading)));
    }

    private void saveProduct() {
        tilSku.setError(null);
        tilName.setError(null);
        tilPrice.setError(null);
        tilStock.setError(null);

        String sku = etSku.getText() != null ? etSku.getText().toString().trim() : "";
        String name = etName.getText() != null ? etName.getText().toString().trim() : "";
        String priceStr = etPrice.getText() != null ? etPrice.getText().toString().trim() : "";
        String stockStr = etStock.getText() != null ? etStock.getText().toString().trim() : "";

        boolean valid = true;
        if (sku.isEmpty() || sku.length() < 3) {
            tilSku.setError("SKU muy corto");
            valid = false;
        }
        if (name.isEmpty()) {
            tilName.setError("Nombre requerido");
            valid = false;
        }
        
        double price = 0;
        try {
            price = Double.parseDouble(priceStr);
            if (price <= 0) {
                tilPrice.setError("Precio inválido");
                valid = false;
            }
        } catch (NumberFormatException e) {
            tilPrice.setError("Precio requerido");
            valid = false;
        }
        
        int stock = 0;
        try {
            stock = Integer.parseInt(stockStr);
            if (stock < 0) {
                tilStock.setError("Stock inválido");
                valid = false;
            }
        } catch (NumberFormatException e) {
            tilStock.setError("Stock requerido");
            valid = false;
        }

        if (!valid) return;

        if (currentProduct == null) {
            currentProduct = new Product();
            currentProduct.setIsActive(1);
        }
        
        currentProduct.setSku(sku);
        currentProduct.setName(name);
        currentProduct.setPrice(price);
        currentProduct.setStock(stock);

        long userId = ((MainActivity) requireActivity()).getSessionManager().getUserId();
        viewModel.saveProduct(currentProduct, userId);
    }
}