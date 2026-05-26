package com.swiftpay.ui.ventas;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.swiftpay.R;
import com.swiftpay.data.entity.CartItem;
import com.swiftpay.data.entity.DiscountCode;
import com.swiftpay.data.entity.Product;
import com.swiftpay.ui.adapter.CartItemAdapter;
import com.swiftpay.ui.adapter.ProductListAdapter;
import com.swiftpay.viewmodel.SaleViewModel;
import com.swiftpay.viewmodel.ClientViewModel;
import com.swiftpay.data.entity.Client;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SaleCartFragment extends Fragment {

    private SaleViewModel viewModel;
    private ClientViewModel clientViewModel;
    private CartItemAdapter cartAdapter;
    private ProductListAdapter catalogAdapter;
    private List<Product> allProducts = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_sale_cart, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Compartir ViewModel con la Actividad para retener carrito
        viewModel = new ViewModelProvider(requireActivity()).get(SaleViewModel.class);
        clientViewModel = new ViewModelProvider(requireActivity()).get(ClientViewModel.class);

        RecyclerView rvCatalog = view.findViewById(R.id.rv_catalog_products);
        RecyclerView rvCart = view.findViewById(R.id.rv_cart_items);
        TextView tvSubtotal = view.findViewById(R.id.tv_cart_subtotal);
        TextView tvDiscount = view.findViewById(R.id.tv_cart_discount);
        TextView tvTotal = view.findViewById(R.id.tv_cart_total);
        MaterialButton btnCheckout = view.findViewById(R.id.btn_checkout);
        MaterialButton btnScan = view.findViewById(R.id.btn_scan_product);
        MaterialButton btnAddDiscount = view.findViewById(R.id.btn_add_discount);
        MaterialButton btnAssignClient = view.findViewById(R.id.btn_assign_client);
        TextView tvCartClient = view.findViewById(R.id.tv_cart_client);
        EditText etSearch = view.findViewById(R.id.et_search_product);

        catalogAdapter = new ProductListAdapter(product -> {
            if (product.getStock() <= 0) {
                Toast.makeText(requireContext(), "Sin stock disponible", Toast.LENGTH_SHORT).show();
                return;
            }
            viewModel.addProductToCart(product, 1);
            Toast.makeText(requireContext(), product.getName() + " añadido al carrito", Toast.LENGTH_SHORT).show();
        });
        rvCatalog.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvCatalog.setAdapter(catalogAdapter);

        cartAdapter = new CartItemAdapter(new CartItemAdapter.OnCartItemInteractionListener() {
            @Override
            public void onQuantityChanged(long productId, int newQuantity) {
                viewModel.updateCartItemQuantity(productId, newQuantity);
            }

            @Override
            public void onEditPriceRequested(CartItem item) {
                View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_edit_price, null);
                EditText etPrice = dialogView.findViewById(R.id.et_new_price);
                etPrice.setText(String.valueOf(item.getUnitPrice()));

                new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                        .setTitle("Editar Precio (Catálogo: $" + item.getCatalogPrice() + ")")
                        .setView(dialogView)
                        .setPositiveButton("Guardar", (dialog, which) -> {
                            String priceStr = etPrice.getText().toString();
                            if (!priceStr.isEmpty()) {
                                try {
                                    double newPrice = Double.parseDouble(priceStr);
                                    if (!viewModel.updateCartItemPrice(item.getProductId(), newPrice)) {
                                        Toast.makeText(requireContext(), "El precio no puede ser menor al 50% del catálogo", Toast.LENGTH_SHORT).show();
                                    }
                                } catch (NumberFormatException e) {
                                    Toast.makeText(requireContext(), "Precio inválido", Toast.LENGTH_SHORT).show();
                                }
                            }
                        })
                        .setNegativeButton("Cancelar", null)
                        .show();
            }
        });

        rvCart.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvCart.setAdapter(cartAdapter);

        viewModel.getActiveProducts().observe(getViewLifecycleOwner(), products -> {
            allProducts = products != null ? products : new ArrayList<>();
            applyProductFilter(etSearch.getText() != null ? etSearch.getText().toString() : "");
        });

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                applyProductFilter(s != null ? s.toString() : "");
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        viewModel.getCartItems().observe(getViewLifecycleOwner(), items -> {
            cartAdapter.submitList(items);
            btnCheckout.setEnabled(items != null && !items.isEmpty());
        });

        viewModel.getSubtotal().observe(getViewLifecycleOwner(), subtotal -> {
            tvSubtotal.setText(String.format(Locale.getDefault(), "$%.2f", subtotal));
        });

        viewModel.getTotal().observe(getViewLifecycleOwner(), total -> {
            if (total == null) {
                return;
            }
            tvTotal.setText(String.format(Locale.getDefault(), "$%.2f", total));
            Double subtotalValue = viewModel.getSubtotal().getValue();
            if (subtotalValue != null) {
                double diff = subtotalValue - total;
                if (diff > 0.001) {
                    tvDiscount.setText(String.format(Locale.getDefault(), "-$%.2f", diff));
                } else {
                    tvDiscount.setText(String.format(Locale.getDefault(), "-$%.2f", 0.0));
                }
            }
        });

        viewModel.getAppliedDiscount().observe(getViewLifecycleOwner(), discount ->
                updateDiscountButtonLabel(btnAddDiscount, discount));

        viewModel.getSelectedClientId().observe(getViewLifecycleOwner(), clientId -> {
            if (clientId == null) {
                tvCartClient.setText("Público General");
            } else {
                clientViewModel.getClientById(clientId).observe(getViewLifecycleOwner(), client -> {
                    if (client != null) {
                        tvCartClient.setText(client.getFullName());
                    }
                });
            }
        });

        btnAddDiscount.setOnClickListener(v -> showDiscountDialog());
        btnAssignClient.setOnClickListener(v -> showClientSelectionDialog());

        viewModel.getOperationMessage().observe(getViewLifecycleOwner(), msg -> {
            if (msg != null && !msg.isEmpty()) {
                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
            }
        });

        btnScan.setOnClickListener(v -> {
            androidx.navigation.Navigation.findNavController(v).navigate(R.id.action_saleCart_to_barcodeScanner);
        });

        btnCheckout.setOnClickListener(v -> {
            androidx.navigation.Navigation.findNavController(v).navigate(R.id.action_saleCart_to_salePayment);
        });
    }

    private void updateDiscountButtonLabel(MaterialButton btn, DiscountCode discount) {
        if (discount != null) {
            btn.setText(String.format(Locale.getDefault(), "Descuento: %s (%.0f%%)",
                    discount.getCode(), discount.getDiscountPercentage()));
        } else {
            btn.setText(R.string.sale_cart_apply_discount);
        }
    }

    private void showDiscountDialog() {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_discount_code, null);
        TextInputEditText etCode = dialogView.findViewById(R.id.et_discount_code_dialog);
        MaterialButton btnApply = dialogView.findViewById(R.id.btn_apply);
        MaterialButton btnCancel = dialogView.findViewById(R.id.btn_cancel);

        DiscountCode current = viewModel.getAppliedDiscount().getValue();
        if (current != null) {
            etCode.setText(current.getCode());
            etCode.setSelection(current.getCode().length());
        }

        androidx.appcompat.app.AlertDialog dialog = new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .create();

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnApply.setOnClickListener(v -> {
            List<CartItem> items = viewModel.getCartItems().getValue();
            if (items == null || items.isEmpty()) {
                Toast.makeText(requireContext(), "Agrega productos antes de aplicar un descuento", Toast.LENGTH_SHORT).show();
                return;
            }
            String code = etCode.getText() != null ? etCode.getText().toString().trim() : "";
            viewModel.applyDiscount(code);
            dialog.dismiss();
        });

        dialog.show();
    }

    private void showClientSelectionDialog() {
        clientViewModel.fetchAllActiveClients(clients -> {
            if (clients == null || clients.isEmpty()) {
                Toast.makeText(getContext(), "No hay clientes registrados o activos", Toast.LENGTH_SHORT).show();
                return;
            }

            androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(requireContext());
            builder.setTitle("Seleccionar Cliente");

            // Option 1: Público General
            List<String> displayNames = new ArrayList<>();
            displayNames.add("Público General (Sin Cliente)");
            for (Client c : clients) {
                displayNames.add(c.getFullName() + " (" + (c.getRfc() != null ? c.getRfc() : "Sin RFC") + ")");
            }

            builder.setItems(displayNames.toArray(new String[0]), (dialog, which) -> {
                if (which == 0) {
                    viewModel.setSelectedClient(null);
                    Toast.makeText(getContext(), "Venta asignada a Público General", Toast.LENGTH_SHORT).show();
                } else {
                    Client selected = clients.get(which - 1);
                    viewModel.setSelectedClient(selected.getId());
                    Toast.makeText(getContext(), "Venta vinculada a: " + selected.getFullName(), Toast.LENGTH_SHORT).show();
                }
            });

            builder.setNegativeButton("Cancelar", null);
            builder.show();
        });
    }

    private void applyProductFilter(String query) {
        if (query == null || query.trim().isEmpty()) {
            catalogAdapter.submitList(new ArrayList<>(allProducts));
            return;
        }
        String lower = query.trim().toLowerCase(Locale.getDefault());
        List<Product> filtered = new ArrayList<>();
        for (Product p : allProducts) {
            if (p.getName().toLowerCase(Locale.getDefault()).contains(lower)
                    || p.getSku().toLowerCase(Locale.getDefault()).contains(lower)) {
                filtered.add(p);
            }
        }
        catalogAdapter.submitList(filtered);
    }
}
