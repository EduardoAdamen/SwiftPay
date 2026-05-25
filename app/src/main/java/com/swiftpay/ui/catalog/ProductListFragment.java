// app/src/main/java/com/swiftpay/ui/catalog/ProductListFragment.java
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
import com.swiftpay.data.db.SwiftPayDatabase;
import com.swiftpay.data.preferences.SessionManager;
import com.swiftpay.data.repository.UserPreferencesRepository;
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
            fabAdd.setOnClickListener(v -> {
                androidx.navigation.Navigation.findNavController(v).navigate(R.id.action_productList_to_productForm);
            });
        } else {
            fabAdd.setVisibility(View.GONE);
        }

        // Capture NavController once — using requireView() inside adapter callbacks
        // can crash when the view is destroyed while Paging 3 is still diffing.
        final androidx.navigation.NavController navController =
                androidx.navigation.Navigation.findNavController(view);

        // Adapter
        adapter = new ProductPagingAdapter(product -> {
            // Navegar a detalle de producto
            Bundle bundle = new Bundle();
            bundle.putLong("productId", product.getId());
            navController.navigate(R.id.action_productList_to_productDetail, bundle);
        });

        
        rvProducts.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvProducts.setAdapter(adapter);
        new UserPreferencesRepository(SwiftPayDatabase.getInstance(requireContext()))
                .getByUserId(sessionManager.getUserId())
                .observe(getViewLifecycleOwner(), prefs -> {
                    if (prefs != null) {
                        adapter.setImagesEnabled(prefs.getImagesEnabled() == 1);
                        adapter.setCompactView(prefs.getCompactView() == 1);
                        if (prefs.getAnimationsEnabled() == 0) {
                            rvProducts.setItemAnimator(null);
                        }
                    }
                });

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
        btnScan.setOnClickListener(v -> navController.navigate(R.id.barcodeScannerFragment));


    }
}
