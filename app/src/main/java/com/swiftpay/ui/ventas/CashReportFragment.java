package com.swiftpay.ui.ventas;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.google.android.material.button.MaterialButton;
import com.swiftpay.R;
import com.swiftpay.viewmodel.CashRegisterViewModel;
import java.util.Locale;

public class CashReportFragment extends Fragment {

    private CashRegisterViewModel viewModel;
    private long cashRegisterId = 1L; // Debería llegar por args

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_cash_report, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(CashRegisterViewModel.class);

        TextView tvBase = view.findViewById(R.id.tv_report_base);
        TextView tvExpected = view.findViewById(R.id.tv_report_expected);
        TextView tvPhysical = view.findViewById(R.id.tv_report_physical);
        TextView tvDiff = view.findViewById(R.id.tv_report_diff);
        MaterialButton btnFinish = view.findViewById(R.id.btn_finish_report);

        viewModel.getCashRegister(cashRegisterId).observe(getViewLifecycleOwner(), register -> {
            if (register != null) {
                tvBase.setText(String.format(Locale.getDefault(), "$%.2f", register.getBaseAmount()));
                tvExpected.setText(String.format(Locale.getDefault(), "$%.2f", register.getExpectedAmount() != null ? register.getExpectedAmount() : 0.0));
                tvPhysical.setText(String.format(Locale.getDefault(), "$%.2f", register.getPhysicalAmount() != null ? register.getPhysicalAmount() : 0.0));
                
                double diff = register.getDifference() != null ? register.getDifference() : 0.0;
                tvDiff.setText(String.format(Locale.getDefault(), "$%.2f", diff));
                
                if (diff < 0) {
                    tvDiff.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorError)); // Faltante
                } else if (diff > 0) {
                    tvDiff.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorWarning)); // Sobrante
                } else {
                    tvDiff.setTextColor(ContextCompat.getColor(requireContext(), R.color.colorSuccess)); // Cuadrado
                }
            }
        });

        btnFinish.setOnClickListener(v -> {
            // Volver al inicio
        });
    }
}
