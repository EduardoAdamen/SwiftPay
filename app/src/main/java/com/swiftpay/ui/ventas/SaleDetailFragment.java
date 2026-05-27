// app/src/main/java/com/swiftpay/ui/ventas/SaleDetailFragment.java
package com.swiftpay.ui.ventas;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import com.swiftpay.MainActivity;
import com.swiftpay.R;
import com.swiftpay.data.entity.Sale;
import com.swiftpay.data.entity.SaleItem;
import com.swiftpay.ui.adapter.SaleItemAdapter;
import com.swiftpay.util.BluetoothPrintHelper;
import com.swiftpay.util.Constants;
import java.text.SimpleDateFormat;
import java.util.Date;
import com.swiftpay.util.PdfGenerator;
import com.swiftpay.viewmodel.SaleViewModel;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SaleDetailFragment extends Fragment {

    private SaleViewModel viewModel;
    private long saleId = -1L;
    private Sale currentSale;
    private List<com.swiftpay.data.entity.SaleItemWithProduct> currentItems = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_sale_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(SaleViewModel.class);
        if (getArguments() != null) {
            saleId = getArguments().getLong("saleId", -1L);
        }

        TextView tvTotal = view.findViewById(R.id.tv_detail_total);
        TextView tvId = view.findViewById(R.id.tv_detail_id);
        TextView tvStatus = view.findViewById(R.id.chip_detail_status);
        TextView tvDate = view.findViewById(R.id.tv_detail_date);
        TextView tvPayment = view.findViewById(R.id.tv_detail_payment);
        TextView tvSubtotal = view.findViewById(R.id.tv_detail_subtotal);
        TextView tvDiscount = view.findViewById(R.id.tv_detail_discount);
        
        MaterialButton btnCancel = view.findViewById(R.id.btn_cancel_sale);
        MaterialButton btnComplete = view.findViewById(R.id.btn_complete_sale);
        MaterialButton btnPdf = view.findViewById(R.id.btn_generate_pdf);
        MaterialButton btnPrint = view.findViewById(R.id.btn_print_bluetooth);
        RecyclerView rvItems = view.findViewById(R.id.rv_sale_items);

        SaleItemAdapter adapter = new SaleItemAdapter();
        rvItems.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvItems.setAdapter(adapter);

        viewModel.getSaleById(saleId).observe(getViewLifecycleOwner(), sale -> {
            currentSale = sale;
            if (sale != null) {
                tvId.setText("Ticket #" + sale.getId());
                tvStatus.setText(sale.getStatus() != null ? sale.getStatus() : "Desconocido");
                
                int colorRes = Constants.STATUS_CANCELADA.equals(sale.getStatus()) ? R.color.colorError : 
                               Constants.STATUS_COMPLETADA.equals(sale.getStatus()) ? R.color.colorPrimary : R.color.colorSuccess;
                tvStatus.setBackgroundTintList(android.content.res.ColorStateList.valueOf(requireContext().getColor(colorRes)));
                
                SimpleDateFormat sdf = new SimpleDateFormat(Constants.DATE_FORMAT_DISPLAY, Locale.getDefault());
                tvDate.setText("Fecha: " + sdf.format(new Date(sale.getCreatedAt())));
                tvPayment.setText("Pago: " + sale.getPaymentMethod());
                
                tvSubtotal.setText(String.format(Locale.getDefault(), "$%.2f", sale.getSubtotal()));
                if (sale.getDiscountPercentage() > 0) {
                    tvDiscount.setText(String.format(Locale.getDefault(), "-$%.2f (%.0f%%)", 
                            sale.getSubtotal() * (sale.getDiscountPercentage()/100.0), sale.getDiscountPercentage()));
                } else {
                    tvDiscount.setText("$0.00");
                }
                
                tvTotal.setText(String.format(Locale.getDefault(), "$%.2f", sale.getTotal()));
                
                btnCancel.setEnabled(!Constants.STATUS_CANCELADA.equals(sale.getStatus()));
                btnComplete.setEnabled(Constants.STATUS_PAGADA.equals(sale.getStatus()));
            }
        });

        viewModel.getSaleItems(saleId).observe(getViewLifecycleOwner(), items -> {
            currentItems = items;
            adapter.submitList(items);
        });

        long currentUserId = ((MainActivity) requireActivity()).getSessionManager().getUserId();

        btnCancel.setOnClickListener(v -> {
            viewModel.updateStatus(saleId, Constants.STATUS_CANCELADA, currentUserId);
        });

        btnComplete.setOnClickListener(v -> {
            viewModel.updateStatus(saleId, Constants.STATUS_COMPLETADA, currentUserId);
        });

        btnPdf.setOnClickListener(v -> {
            if (currentSale != null && currentItems != null) {
                try {
                    File pdf = PdfGenerator.generateSaleReceipt(requireContext(), currentSale, currentItems);
                    Uri uri = FileProvider.getUriForFile(requireContext(), requireContext().getPackageName() + ".fileprovider", pdf);
                    Intent intent = new Intent(Intent.ACTION_SEND);
                    intent.setType("application/pdf");
                    intent.putExtra(Intent.EXTRA_STREAM, uri);
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    startActivity(Intent.createChooser(intent, "Compartir PDF"));
                } catch (Exception e) {
                    Toast.makeText(requireContext(), "Error PDF: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnPrint.setOnClickListener(v -> {
            if (currentSale != null && currentItems != null) {
                try {
                    File pdf = PdfGenerator.generateSaleReceipt(requireContext(), currentSale, currentItems);
                    BluetoothPrintHelper.printPdf(requireContext(), pdf);
                } catch (Exception e) {
                    Toast.makeText(requireContext(), "Error imprimiendo: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });

        viewModel.getOperationMessage().observe(getViewLifecycleOwner(), msg -> {
            if (msg != null && !msg.isEmpty() && !Boolean.TRUE.equals(viewModel.getOperationSuccess().getValue())) {
                Toast.makeText(requireContext(), msg, Toast.LENGTH_LONG).show();
            }
        });

        viewModel.getOperationSuccess().observe(getViewLifecycleOwner(), success -> {
            if (Boolean.TRUE.equals(success)) {
                Toast.makeText(requireContext(), "Estado actualizado exitosamente", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
