package com.swiftpay.ui.clientes;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import com.google.android.material.button.MaterialButton;
import com.swiftpay.MainActivity;
import com.swiftpay.R;
import com.swiftpay.data.db.SwiftPayDatabase;
import com.swiftpay.data.entity.Client;
import com.swiftpay.data.preferences.SessionManager;
import com.swiftpay.viewmodel.ClientViewModel;
import java.util.Locale;

public class ClientDetailFragment extends Fragment {

    private ClientViewModel viewModel;
    private long clientId = -1;
    private Client currentClient;
    private SessionManager sessionManager;

    private TextView tvName, tvCategory, tvPurchases, tvSpent;
    private MaterialButton btnEdit, btnDelete;

    public ClientDetailFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_client_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        sessionManager = ((MainActivity) requireActivity()).getSessionManager();
        viewModel = new ViewModelProvider(this).get(ClientViewModel.class);

        if (getArguments() != null) {
            clientId = getArguments().getLong("clientId", -1);
        }

        tvName = view.findViewById(R.id.tv_detail_name);
        tvCategory = view.findViewById(R.id.tv_detail_category);
        tvPurchases = view.findViewById(R.id.tv_detail_purchases);
        tvSpent = view.findViewById(R.id.tv_detail_spent);
        btnEdit = view.findViewById(R.id.btn_edit_client);
        btnDelete = view.findViewById(R.id.btn_delete_client);

        if (sessionManager.hasRole("ADMINISTRADOR") || sessionManager.hasRole("VENDEDOR")) {
            btnEdit.setVisibility(View.VISIBLE);
            btnDelete.setVisibility(View.VISIBLE);
        } else {
            btnEdit.setVisibility(View.GONE);
            btnDelete.setVisibility(View.GONE);
        }

        if (clientId != -1) {
            viewModel.getClientById(clientId).observe(getViewLifecycleOwner(), client -> {
                if (client != null) {
                    currentClient = client;
                    displayClient(client);
                }
            });

            viewModel.getClientStats(clientId, (totalPurchases, totalSpent) -> {
                if (isAdded()) {
                    requireActivity().runOnUiThread(() -> {
                        tvPurchases.setText(String.valueOf(totalPurchases));
                        tvSpent.setText(String.format(Locale.getDefault(), "$%.2f", totalSpent));
                    });
                }
            });
        }

        btnEdit.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putLong("clientId", clientId);
            Navigation.findNavController(v).navigate(R.id.action_clientDetail_to_clientForm, bundle);
        });

        btnDelete.setOnClickListener(v -> {
            new AlertDialog.Builder(requireContext())
                    .setTitle("Desactivar o Eliminar Cliente")
                    .setMessage("¿Estás seguro de que deseas desactivar o eliminar este cliente?")
                    .setPositiveButton("Aceptar", (dialog, which) -> {
                        viewModel.deleteOrDeactivateClient(currentClient, sessionManager.getUserId());
                    })
                    .setNegativeButton("Cancelar", null)
                    .show();
        });

        viewModel.getOperationSuccess().observe(getViewLifecycleOwner(), success -> {
            if (Boolean.TRUE.equals(success)) {
                Toast.makeText(requireContext(), "Operación realizada con éxito", Toast.LENGTH_SHORT).show();
                Navigation.findNavController(requireView()).popBackStack();
            }
        });

        viewModel.getOperationMessage().observe(getViewLifecycleOwner(), msg -> {
            if (msg != null && !msg.isEmpty() && !Boolean.TRUE.equals(viewModel.getOperationSuccess().getValue())) {
                Toast.makeText(requireContext(), msg, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void displayClient(Client client) {
        tvName.setText(client.getFullName());
        
        StringBuilder details = new StringBuilder();
        if (client.getEmail() != null && !client.getEmail().trim().isEmpty()) {
            details.append("Email: ").append(client.getEmail()).append("\n");
        }
        if (client.getPhone() != null && !client.getPhone().trim().isEmpty()) {
            details.append("Teléfono: ").append(client.getPhone()).append("\n");
        }
        if (client.getRfc() != null && !client.getRfc().trim().isEmpty()) {
            details.append("RFC: ").append(client.getRfc()).append("\n");
        }
        
        if (client.getCategoryId() != null) {
            SwiftPayDatabase.getInstance(requireContext()).clientCategoryDao()
                    .getById(client.getCategoryId())
                    .observe(getViewLifecycleOwner(), category -> {
                        if (category != null) {
                            tvCategory.setText(category.getName() + "\n" + details.toString());
                        } else {
                            tvCategory.setText(details.toString());
                        }
                    });
        } else {
            tvCategory.setText(details.toString());
        }
    }
}