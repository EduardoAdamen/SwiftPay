package com.swiftpay.ui.ventas;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.google.android.material.button.MaterialButton;
import com.swiftpay.MainActivity;
import com.swiftpay.R;
import com.swiftpay.util.Constants;
import com.swiftpay.viewmodel.SaleViewModel;
import java.util.Locale;

public class SalePaymentFragment extends Fragment {

    private SaleViewModel viewModel;
    private String selectedMethod = Constants.PAYMENT_EFECTIVO;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_sale_payment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(SaleViewModel.class);

        TextView tvTotal = view.findViewById(R.id.tv_payment_total);
        TextView tvChange = view.findViewById(R.id.tv_payment_change);
        EditText etReceived = view.findViewById(R.id.et_amount_received);
        RadioGroup rgPayment = view.findViewById(R.id.rg_payment_method);
        MaterialButton btnConfirm = view.findViewById(R.id.btn_confirm_payment);

        viewModel.getTotal().observe(getViewLifecycleOwner(), total -> {
            tvTotal.setText(String.format(Locale.getDefault(), "$%.2f", total));
            calculateChange(etReceived.getText().toString(), total, tvChange);
        });

        rgPayment.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rb_cash) {
                selectedMethod = Constants.PAYMENT_EFECTIVO;
                etReceived.setEnabled(true);
            } else if (checkedId == R.id.rb_card) {
                selectedMethod = Constants.PAYMENT_TARJETA;
                etReceived.setEnabled(false);
                etReceived.setText("");
                tvChange.setText("$0.00");
            } else if (checkedId == R.id.rb_transfer) {
                selectedMethod = Constants.PAYMENT_TRANSFERENCIA;
                etReceived.setEnabled(false);
                etReceived.setText("");
                tvChange.setText("$0.00");
            }
        });

        etReceived.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                Double total = viewModel.getTotal().getValue();
                if (total != null) {
                    calculateChange(s.toString(), total, tvChange);
                }
            }
        });

        btnConfirm.setOnClickListener(v -> {
            double received = 0.0;
            if (selectedMethod.equals(Constants.PAYMENT_EFECTIVO)) {
                try {
                    received = Double.parseDouble(etReceived.getText().toString());
                } catch (NumberFormatException e) {
                    Toast.makeText(requireContext(), "Monto inválido", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
            long sellerId = ((MainActivity) requireActivity()).getSessionManager().getUserId();
            // Default cash register id
            viewModel.processPayment(selectedMethod, received, sellerId, 1L);
        });

        viewModel.getOperationSuccess().observe(getViewLifecycleOwner(), success -> {
            if (success) {
                Toast.makeText(requireContext(), "Venta Completada", Toast.LENGTH_LONG).show();
                // Regresar a inicio
            }
        });
    }

    private void calculateChange(String receivedStr, double total, TextView tvChange) {
        if (!selectedMethod.equals(Constants.PAYMENT_EFECTIVO)) return;
        try {
            double received = Double.parseDouble(receivedStr);
            double change = received - total;
            if (change < 0) change = 0;
            tvChange.setText(String.format(Locale.getDefault(), "$%.2f", change));
        } catch (NumberFormatException e) {
            tvChange.setText("$0.00");
        }
    }
}
