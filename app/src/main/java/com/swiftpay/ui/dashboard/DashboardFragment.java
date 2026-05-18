package com.swiftpay.ui.dashboard;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import com.swiftpay.R;
import com.swiftpay.viewmodel.DashboardViewModel;

public class DashboardFragment extends Fragment {

    private DashboardViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_dashboard, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        viewModel = new ViewModelProvider(this).get(DashboardViewModel.class);
        
        TextView tvNewSalesCount = view.findViewById(R.id.tvNewSalesCount);
        TextView tvNewClientsCount = view.findViewById(R.id.tvNewClientsCount);
        Button btnViewEvents = view.findViewById(R.id.btnViewEvents);

        viewModel.getNewSalesCount().observe(getViewLifecycleOwner(), count -> {
            tvNewSalesCount.setText(count != null ? String.valueOf(count) : "0");
        });

        viewModel.getNewClientsCount().observe(getViewLifecycleOwner(), count -> {
            tvNewClientsCount.setText(count != null ? String.valueOf(count) : "0");
        });

        viewModel.getTotalUnreviewedCount().observe(getViewLifecycleOwner(), count -> {
            int unreviewed = count != null ? count : 0;
            btnViewEvents.setText("Ver Eventos Pendientes (" + unreviewed + ")");
        });

        btnViewEvents.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.eventListFragment);
        });
    }
}
