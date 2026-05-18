package com.swiftpay.ui.catalog;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.swiftpay.MainActivity;
import com.swiftpay.R;
import com.swiftpay.data.preferences.SessionManager;
import com.swiftpay.ui.adapter.BrandPagingAdapter;
import com.swiftpay.viewmodel.BrandViewModel;

/**
 * Lista de marcas con Paging 3.
 * RF 5.1: Ver lista de marcas.
 */
public class BrandListFragment extends Fragment {

    private BrandViewModel viewModel;
    private BrandPagingAdapter adapter;
    private SessionManager sessionManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_brand_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        sessionManager = ((MainActivity) requireActivity()).getSessionManager();
        viewModel = new ViewModelProvider(this).get(BrandViewModel.class);

        RecyclerView rvBrands = view.findViewById(R.id.rv_brands);
        FloatingActionButton fabAdd = view.findViewById(R.id.fab_add_brand);

        // Seguridad
        if (!sessionManager.hasRole("ADMINISTRADOR") && !sessionManager.hasRole("GESTOR_PRODUCTOS")) {
            ((MainActivity) requireActivity()).getNavController().navigate(R.id.accessDeniedFragment);
            return;
        }

        // Adapter
        adapter = new BrandPagingAdapter(brand -> {
            Toast.makeText(requireContext(), "Editar marca: " + brand.getName(), Toast.LENGTH_SHORT).show();
            // Aquí se abriría el formulario BrandFormFragment
        });

        rvBrands.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvBrands.setAdapter(adapter);

        // Cargar datos
        viewModel.getPagedBrands().observe(getViewLifecycleOwner(), pagingData -> {
            adapter.submitData(getLifecycle(), pagingData);
        });

        fabAdd.setOnClickListener(v -> {
            Toast.makeText(requireContext(), "Nueva Marca", Toast.LENGTH_SHORT).show();
        });
    }
}
