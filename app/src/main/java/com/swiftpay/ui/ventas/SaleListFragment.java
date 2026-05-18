package com.swiftpay.ui.ventas;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.swiftpay.R;
import com.swiftpay.ui.adapter.SalePagingAdapter;
import com.swiftpay.viewmodel.SaleViewModel;

public class SaleListFragment extends Fragment {

    private SaleViewModel viewModel;
    private SalePagingAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_sale_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(SaleViewModel.class);

        RecyclerView rvSales = view.findViewById(R.id.rv_sales);
        EditText etSearch = view.findViewById(R.id.et_search_sale);

        adapter = new SalePagingAdapter(sale -> {
            // Navegar a detalle
        });

        rvSales.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvSales.setAdapter(adapter);

        viewModel.getFilteredSalesPaged().observe(getViewLifecycleOwner(), pagingData -> {
            adapter.submitData(getLifecycle(), pagingData);
        });

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                viewModel.setFilters(null, s.toString());
                viewModel.getFilteredSalesPaged().observe(getViewLifecycleOwner(), pagingData -> {
                    adapter.submitData(getLifecycle(), pagingData);
                });
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });
    }
}
