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
import com.swiftpay.R;
import com.swiftpay.data.entity.CartItem;
import com.swiftpay.ui.adapter.CartItemAdapter;
import com.swiftpay.viewmodel.SaleViewModel;
import java.util.Locale;

public class SaleCartFragment extends Fragment {

    private SaleViewModel viewModel;
    private CartItemAdapter adapter;

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

        RecyclerView rvCart = view.findViewById(R.id.rv_cart_items);
        TextView tvSubtotal = view.findViewById(R.id.tv_cart_subtotal);
        TextView tvDiscount = view.findViewById(R.id.tv_cart_discount);
        TextView tvTotal = view.findViewById(R.id.tv_cart_total);
        MaterialButton btnCheckout = view.findViewById(R.id.btn_checkout);
        MaterialButton btnScan = view.findViewById(R.id.btn_scan_product);
        EditText etSearch = view.findViewById(R.id.et_search_product);

        adapter = new CartItemAdapter(new CartItemAdapter.OnCartItemInteractionListener() {
            @Override
            public void onQuantityChanged(long productId, int newQuantity) {
                viewModel.updateCartItemQuantity(productId, newQuantity);
            }

            @Override
            public void onEditPriceRequested(CartItem item) {
                // Muestra dialogo para editar precio
                Toast.makeText(requireContext(), "Editar precio de " + item.getProductName(), Toast.LENGTH_SHORT).show();
            }
        });

        rvCart.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvCart.setAdapter(adapter);

        viewModel.getCartItems().observe(getViewLifecycleOwner(), items -> {
            adapter.submitList(items);
            btnCheckout.setEnabled(items != null && !items.isEmpty());
        });

        viewModel.getSubtotal().observe(getViewLifecycleOwner(), subtotal -> {
            tvSubtotal.setText(String.format(Locale.getDefault(), "$%.2f", subtotal));
        });

        viewModel.getTotal().observe(getViewLifecycleOwner(), total -> {
            tvTotal.setText(String.format(Locale.getDefault(), "$%.2f", total));
            Double subtotal = viewModel.getSubtotal().getValue();
            if (subtotal != null && total != null) {
                double diff = subtotal - total;
                tvDiscount.setText(String.format(Locale.getDefault(), "-$%.2f", diff));
            }
        });

        viewModel.getOperationMessage().observe(getViewLifecycleOwner(), msg -> {
            if (msg != null && !msg.isEmpty()) {
                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
            }
        });

        btnScan.setOnClickListener(v -> {
            // Navegar a escáner
        });

        btnCheckout.setOnClickListener(v -> {
            // Navegar a pago
            // Navigation.findNavController(v).navigate(R.id.salePaymentFragment);
        });
    }
}
