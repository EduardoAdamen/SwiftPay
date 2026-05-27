package com.swiftpay.ui.proveedores;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.swiftpay.R;
import com.swiftpay.data.entity.PurchaseOrder;
import com.swiftpay.viewmodel.PurchaseOrderViewModel;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.swiftpay.ui.adapter.PurchaseOrderItemAdapter;

public class PurchaseOrderDetailFragment extends Fragment {

    private PurchaseOrderViewModel viewModel;
    private long orderId;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
    private PurchaseOrder currentOrder;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_purchase_order_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() != null) {
            orderId = getArguments().getLong("orderId", -1);
        }

        TextView tvOrderId = view.findViewById(R.id.tv_detail_order_id);
        TextView tvStatus = view.findViewById(R.id.tv_detail_order_status);
        TextView tvTotal = view.findViewById(R.id.tv_detail_order_total);
        TextView tvDate = view.findViewById(R.id.tv_detail_order_date);
        View llActions = view.findViewById(R.id.ll_pending_actions);

        viewModel = new ViewModelProvider(this).get(PurchaseOrderViewModel.class);
        
        if (orderId != -1) {
            viewModel.getOrderById(orderId).observe(getViewLifecycleOwner(), order -> {
                if (order != null) {
                    currentOrder = order;
                    tvOrderId.setText("Orden #: " + order.getId());
                    tvStatus.setText("Estado: " + order.getStatus());
                    tvTotal.setText(String.format(Locale.getDefault(), "Total: $%.2f", order.getTotal()));
                    tvDate.setText("Fecha: " + dateFormat.format(new Date(order.getCreatedAt())));

                    if ("PENDIENTE".equals(order.getStatus())) {
                        llActions.setVisibility(View.VISIBLE);
                    } else {
                        llActions.setVisibility(View.GONE);
                    }
                }
            });

            RecyclerView rvItems = view.findViewById(R.id.rv_detail_order_items);
            if (rvItems != null) {
                rvItems.setLayoutManager(new LinearLayoutManager(getContext()));
                PurchaseOrderItemAdapter adapter = new PurchaseOrderItemAdapter();
                rvItems.setAdapter(adapter);

                viewModel.getItemsWithProductForOrder(orderId).observe(getViewLifecycleOwner(), items -> {
                    adapter.submitList(items);
                });
            }
        }

        view.findViewById(R.id.btn_edit_order).setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putLong("orderId", orderId);
            Navigation.findNavController(v).navigate(R.id.action_purchaseOrderDetail_to_purchaseOrderForm, bundle);
        });

        view.findViewById(R.id.btn_receive_order).setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putLong("orderId", orderId);
            Navigation.findNavController(v).navigate(R.id.action_purchaseOrderDetail_to_purchaseOrderReceive, bundle);
        });

        view.findViewById(R.id.btn_cancel_order).setOnClickListener(v -> {
             viewModel.cancelOrder(orderId);
        });
        
        viewModel.getOperationStatus().observe(getViewLifecycleOwner(), status -> {
            if ("SUCCESS_CANCEL".equals(status)) {
                Toast.makeText(getContext(), "Orden Cancelada", Toast.LENGTH_SHORT).show();
                viewModel.resetStatus();
            }
        });
    }
}
