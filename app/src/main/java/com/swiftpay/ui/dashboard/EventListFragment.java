package com.swiftpay.ui.dashboard;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.swiftpay.R;
import com.swiftpay.ui.adapter.EventAdapter;
import com.swiftpay.viewmodel.DashboardViewModel;

public class EventListFragment extends Fragment {

    private DashboardViewModel viewModel;
    private EventAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_event_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        viewModel = new ViewModelProvider(this).get(DashboardViewModel.class);
        
        RecyclerView recyclerView = view.findViewById(R.id.recyclerViewEvents);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        
        adapter = new EventAdapter(event -> viewModel.markAsReviewed(event.getId()));
        recyclerView.setAdapter(adapter);

        Button btnMarkAll = view.findViewById(R.id.btnMarkAll);
        btnMarkAll.setOnClickListener(v -> viewModel.markAllAsReviewed());

        viewModel.getUnreviewedEvents().observe(getViewLifecycleOwner(), events -> {
            adapter.submitList(events);
        });
    }
}
