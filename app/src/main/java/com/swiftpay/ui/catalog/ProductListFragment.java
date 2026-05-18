package com.swiftpay.ui.catalog;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
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
import com.swiftpay.ui.adapter.ProductPagingAdapter;
import com.swiftpay.viewmodel.ProductViewModel;

/**
 * Catálogo de productos con Paging 3.
 * RF 4.1: Mostrar productos.
 * RF 4.2: Búsqueda.
 */
public class ProductListFragment extends Fragment {

    private ProductViewModel viewModel;
    private ProductPagingAdapter adapter;
    private SessionManager sessionManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_product_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        sessionManager = ((MainActivity) requireActivity()).getSessionManager();
        viewModel = new ViewModelProvider(this).get(ProductViewModel.class);

        RecyclerView rvProducts = view.findViewById(R.id.rv_products);
        FloatingActionButton fabAdd = view.findViewById(R.id.fab_add_product);
        EditText etSearch = view.findViewById(R.id.et_search_product);
        ImageButton btnScan = view.findViewById(R.id.btn_scan_barcode);

        // Configurar FAB solo para ADMIN o GESTOR_PRODUCTOS
        if (sessionManager.hasRole("ADMINISTRADOR") || sessionManager.hasRole("GESTOR_PRODUCTOS")) {
            fabAdd.setVisibility(View.VISIBLE);
        } else {
            fabAdd.setVisibility(View.GONE);
        }

        // Adapter
        adapter = new ProductPagingAdapter(product -> {
            // Navegar a detalle de producto
            Toast.makeText(requireContext(), "Click en: " + product.getName(), Toast.LENGTH_SHORT).show();
            // Implementar navegación a ProductDetailFragment
        });
        
        rvProducts.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvProducts.setAdapter(adapter);

        // Cargar datos
        if (sessionManager.hasRole("VENDEDOR")) {
            viewModel.getActiveProductsPaged().observe(getViewLifecycleOwner(), pagingData -> {
                adapter.submitData(getLifecycle(), pagingData);
            });
        } else {
            viewModel.getAllProductsPaged().observe(getViewLifecycleOwner(), pagingData -> {
                adapter.submitData(getLifecycle(), pagingData);
            });
        }

        // Escáner
        btnScan.setOnClickListener(v -> {
            // Implementar navegación a BarcodeScannerFragment
            Toast.makeText(requireContext(), "Abriendo escáner...", Toast.LENGTH_SHORT).show();
        });

        // Búsqueda simple en local por ahora (idealmente sería una query separada)
        // Por la limitación de la base, el filtrado Paging 3 se manejaría con Room y parámetros de búsqueda
    }
}
