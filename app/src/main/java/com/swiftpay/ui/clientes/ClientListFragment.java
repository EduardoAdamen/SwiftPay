package com.swiftpay.ui.clientes;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.swiftpay.R;
import com.swiftpay.ui.adapter.ClientPagingAdapter;
import com.swiftpay.viewmodel.ClientViewModel;

/**
 * Fragmento para listar clientes con Paging 3 y filtros.
 */
public class ClientListFragment extends Fragment {

    private ClientViewModel viewModel;
    private ClientPagingAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_client_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(ClientViewModel.class);

        RecyclerView rvClients = view.findViewById(R.id.rv_clients);
        FloatingActionButton fabAdd = view.findViewById(R.id.fab_add_client);
        EditText etSearch = view.findViewById(R.id.et_search_client);

        adapter = new ClientPagingAdapter(client -> {
            Toast.makeText(requireContext(), "Cliente seleccionado: " + client.getFullName(), Toast.LENGTH_SHORT).show();
            // Implementar navegación a ClientDetailFragment
        });

        rvClients.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvClients.setAdapter(adapter);

        viewModel.getFilteredClientsPaged().observe(getViewLifecycleOwner(), pagingData -> {
            adapter.submitData(getLifecycle(), pagingData);
        });

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Actualizar filtro de búsqueda
                viewModel.setFilters(s.toString(), null, null);
                viewModel.getFilteredClientsPaged().observe(getViewLifecycleOwner(), pagingData -> {
                    adapter.submitData(getLifecycle(), pagingData);
                });
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        fabAdd.setOnClickListener(v -> {
            Toast.makeText(requireContext(), "Nuevo Cliente", Toast.LENGTH_SHORT).show();
            // Implementar navegación a ClientFormFragment
        });
    }
}
