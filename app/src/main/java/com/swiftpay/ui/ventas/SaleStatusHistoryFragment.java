// app/src/main/java/com/swiftpay/ui/ventas/SaleStatusHistoryFragment.java
package com.swiftpay.ui.ventas;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.swiftpay.R;
import com.swiftpay.ui.adapter.SaleStatusHistoryAdapter;
import com.swiftpay.viewmodel.SaleViewModel;

public class SaleStatusHistoryFragment extends Fragment {
    
    private SaleViewModel viewModel;
    private long saleId = -1L;
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_sale_status_history, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        viewModel = new ViewModelProvider(this).get(SaleViewModel.class);
        if (getArguments() != null) {
            saleId = getArguments().getLong("saleId", -1L);
        }
        
        RecyclerView rvHistory = view.findViewById(R.id.rv_status_history);
        SaleStatusHistoryAdapter adapter = new SaleStatusHistoryAdapter();
        
        rvHistory.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvHistory.setAdapter(adapter);
        
        viewModel.getSaleStatusHistory(saleId).observe(getViewLifecycleOwner(), adapter::submitList);
    }
}
