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
import com.swiftpay.ui.adapter.PurchaseOrderAdapter;
import com.swiftpay.viewmodel.PurchaseOrderViewModel;

public class PurchaseOrderListFragment extends Fragment {

    private PurchaseOrderViewModel viewModel;
    private PurchaseOrderAdapter adapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_purchase_order_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView recyclerView = view.findViewById(R.id.rv_purchase_orders);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        
        adapter = new PurchaseOrderAdapter(order -> {
            Bundle bundle = new Bundle();
            bundle.putLong("orderId", order.getId());
            Navigation.findNavController(view).navigate(R.id.action_purchaseOrderList_to_purchaseOrderDetail, bundle);
        });
        recyclerView.setAdapter(adapter);

        viewModel = new ViewModelProvider(this).get(PurchaseOrderViewModel.class);
        viewModel.getAllOrdersDesc().observe(getViewLifecycleOwner(), orders -> {
            adapter.setOrders(orders);
        });

        view.findViewById(R.id.fab_add_purchase_order).setOnClickListener(v -> {
            viewModel.clearDraftItems();
            Navigation.findNavController(v).navigate(R.id.action_purchaseOrderList_to_purchaseOrderForm);
        });
    }
}
