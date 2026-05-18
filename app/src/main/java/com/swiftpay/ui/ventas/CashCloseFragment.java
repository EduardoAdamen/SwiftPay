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
import com.swiftpay.R;
import com.swiftpay.viewmodel.CashRegisterViewModel;

public class CashCloseFragment extends Fragment {

    private CashRegisterViewModel viewModel;
    private long activeCashRegisterId = 1L; // En un escenario real, se obtendría del estado global o DB.

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_cash_close, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(CashRegisterViewModel.class);

        EditText etPhysical = view.findViewById(R.id.et_physical_amount);
        MaterialButton btnClose = view.findViewById(R.id.btn_close_cash);

        btnClose.setOnClickListener(v -> {
            String physicalStr = etPhysical.getText().toString();
            if (physicalStr.isEmpty()) {
                Toast.makeText(requireContext(), "Ingresa el efectivo físico", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                double physical = Double.parseDouble(physicalStr);
                viewModel.closeCashRegister(activeCashRegisterId, physical);
            } catch (NumberFormatException e) {
                Toast.makeText(requireContext(), "Monto inválido", Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.getOperationSuccess().observe(getViewLifecycleOwner(), success -> {
            if (Boolean.TRUE.equals(success)) {
                // Navegar a CashReportFragment pasando el activeCashRegisterId
                Toast.makeText(requireContext(), "Generando Reporte...", Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.getOperationMessage().observe(getViewLifecycleOwner(), msg -> {
            if (msg != null && !msg.isEmpty() && !Boolean.TRUE.equals(viewModel.getOperationSuccess().getValue())) {
                Toast.makeText(requireContext(), msg, Toast.LENGTH_LONG).show();
            }
        });
    }
}
