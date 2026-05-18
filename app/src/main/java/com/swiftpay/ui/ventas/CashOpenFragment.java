package com.swiftpay.ui.ventas;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.google.android.material.button.MaterialButton;
import com.swiftpay.MainActivity;
import com.swiftpay.R;
import com.swiftpay.viewmodel.CashRegisterViewModel;

public class CashOpenFragment extends Fragment {

    private CashRegisterViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_cash_open, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(CashRegisterViewModel.class);

        EditText etBase = view.findViewById(R.id.et_base_amount);
        MaterialButton btnOpen = view.findViewById(R.id.btn_open_cash);

        btnOpen.setOnClickListener(v -> {
            String baseStr = etBase.getText().toString();
            if (baseStr.isEmpty()) {
                Toast.makeText(requireContext(), "Ingresa el monto base", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                double base = Double.parseDouble(baseStr);
                long sellerId = ((MainActivity) requireActivity()).getSessionManager().getUserId();
                viewModel.openCashRegister(sellerId, base);
            } catch (NumberFormatException e) {
                Toast.makeText(requireContext(), "Monto inválido", Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.getOperationSuccess().observe(getViewLifecycleOwner(), success -> {
            if (Boolean.TRUE.equals(success)) {
                Toast.makeText(requireContext(), "Caja Abierta Exitosamente", Toast.LENGTH_SHORT).show();
                // Navegar o cerrar
            }
        });

        viewModel.getOperationMessage().observe(getViewLifecycleOwner(), msg -> {
            if (msg != null && !msg.isEmpty() && !Boolean.TRUE.equals(viewModel.getOperationSuccess().getValue())) {
                Toast.makeText(requireContext(), msg, Toast.LENGTH_LONG).show();
            }
        });
    }
}
