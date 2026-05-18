package com.swiftpay.ui.auth;

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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.swiftpay.MainActivity;
import com.swiftpay.R;
import com.swiftpay.data.entity.User;
import com.swiftpay.data.preferences.SessionManager;
import com.swiftpay.ui.adapter.UserAdapter;
import com.swiftpay.viewmodel.UserViewModel;

/**
 * Fragment para gestión de usuarios (solo ADMIN).
 * RF 1.6: Admin puede crear usuarios.
 * RF 1.7: Admin puede desactivar/reactivar usuarios.
 * RF 1.8: Admin puede restablecer contraseña.
 */
public class UserManagementFragment extends Fragment {

    private UserViewModel viewModel;
    private UserAdapter adapter;
    private SessionManager sessionManager;
    private RecyclerView rvUsers;
    private TextView tvEmpty;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_user_management, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        sessionManager = ((MainActivity) requireActivity()).getSessionManager();
        if (!sessionManager.hasRole("ADMINISTRADOR")) {
            // Seguridad: Si no es admin, no puede estar aquí
            ((MainActivity) requireActivity()).getNavController().navigate(R.id.accessDeniedFragment);
            return;
        }

        viewModel = new ViewModelProvider(this).get(UserViewModel.class);

        rvUsers = view.findViewById(R.id.rv_users);
        tvEmpty = view.findViewById(R.id.tv_empty);
        FloatingActionButton fabAdd = view.findViewById(R.id.fab_add_user);

        setupRecyclerView();

        fabAdd.setOnClickListener(v -> showAddUserDialog());

        // Observar lista de usuarios
        viewModel.getAllUsers().observe(getViewLifecycleOwner(), users -> {
            if (users != null && !users.isEmpty()) {
                adapter.submitList(users);
                rvUsers.setVisibility(View.VISIBLE);
                tvEmpty.setVisibility(View.GONE);
            } else {
                rvUsers.setVisibility(View.GONE);
                tvEmpty.setVisibility(View.VISIBLE);
            }
        });

        // Observar resultados de operaciones
        viewModel.getOperationMessage().observe(getViewLifecycleOwner(), msg -> {
            if (msg != null) Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
        });
    }

    private void setupRecyclerView() {
        adapter = new UserAdapter(this::showUserOptionsDialog);
        rvUsers.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvUsers.setAdapter(adapter);
    }

    private void showAddUserDialog() {
        androidx.navigation.Navigation.findNavController(requireView()).navigate(R.id.action_userManagement_to_userForm);
    }

    private void showUserOptionsDialog(User user) {
        if (user.getId() == sessionManager.getUserId()) {
            Toast.makeText(requireContext(), "No puedes modificar tu propio usuario desde aquí", Toast.LENGTH_SHORT).show();
            return;
        }

        String[] options = {
                user.getIsActive() == 1 ? "Desactivar usuario" : "Reactivar usuario",
                "Restablecer contraseña"
        };

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Opciones para " + user.getUsername())
                .setItems(options, (dialog, which) -> {
                    long adminId = sessionManager.getUserId();
                    if (which == 0) {
                        viewModel.toggleUserActive(user.getId(), adminId);
                    } else if (which == 1) {
                        viewModel.resetPassword(user.getId(), "Password123", adminId); // Default temporal
                        Toast.makeText(requireContext(), "Contraseña restablecida a: Password123", Toast.LENGTH_LONG).show();
                    }
                })
                .show();
    }
}
