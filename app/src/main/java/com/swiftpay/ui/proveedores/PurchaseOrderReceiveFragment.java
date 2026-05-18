package com.swiftpay.ui.proveedores;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.swiftpay.R;
import com.swiftpay.viewmodel.PurchaseOrderViewModel;

public class PurchaseOrderReceiveFragment extends Fragment {

    private PurchaseOrderViewModel viewModel;
    private long orderId;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_purchase_order_receive, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() != null) {
            orderId = getArguments().getLong("orderId", -1);
        }

        viewModel = new ViewModelProvider(this).get(PurchaseOrderViewModel.class);

        view.findViewById(R.id.btn_confirm_receive).setOnClickListener(v -> {
            if (orderId != -1) {
                viewModel.receiveOrder(orderId);
            }
        });

        viewModel.getOperationStatus().observe(getViewLifecycleOwner(), status -> {
            if ("SUCCESS_RECEIVE".equals(status)) {
                Toast.makeText(getContext(), "Mercancía recibida. Stock actualizado atómicamente.", Toast.LENGTH_LONG).show();
                viewModel.resetStatus();
                Navigation.findNavController(view).popBackStack(R.id.nav_purchase_orders, false); // Returns to list
            } else if (status != null && status.startsWith("ERROR")) {
                Toast.makeText(getContext(), status, Toast.LENGTH_LONG).show();
                viewModel.resetStatus();
            }
        });
    }
}
