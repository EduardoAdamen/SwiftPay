package com.swiftpay.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.paging.PagingDataAdapter;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.chip.Chip;
import com.swiftpay.R;
import com.swiftpay.data.entity.Sale;
import com.swiftpay.util.Constants;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class SalePagingAdapter extends PagingDataAdapter<Sale, SalePagingAdapter.SaleViewHolder> {

    private final OnSaleClickListener listener;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat(Constants.DATE_FORMAT_DISPLAY, Locale.getDefault());

    public SalePagingAdapter(OnSaleClickListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    private static final DiffUtil.ItemCallback<Sale> DIFF_CALLBACK = new DiffUtil.ItemCallback<Sale>() {
        @Override
        public boolean areItemsTheSame(@NonNull Sale oldItem, @NonNull Sale newItem) {
            return oldItem.getId() == newItem.getId();
        }
        @Override
        public boolean areContentsTheSame(@NonNull Sale oldItem, @NonNull Sale newItem) {
            return oldItem.getStatus().equals(newItem.getStatus())
                    && oldItem.getTotal() == newItem.getTotal();
        }
    };

    @NonNull
    @Override
    public SaleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_sale, parent, false);
        return new SaleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SaleViewHolder holder, int position) {
        Sale sale = getItem(position);
        if (sale != null) {
            holder.bind(sale);
        }
    }

    class SaleViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvIdDate, tvTotal;
        private final Chip chipStatus;

        SaleViewHolder(@NonNull View itemView) {
            super(itemView);
            tvIdDate = itemView.findViewById(R.id.tv_sale_id_date);
            tvTotal = itemView.findViewById(R.id.tv_sale_total);
            chipStatus = itemView.findViewById(R.id.chip_sale_status);
        }

        void bind(Sale sale) {
            String dateStr = dateFormat.format(new Date(sale.getCreatedAt()));
            tvIdDate.setText(String.format(Locale.getDefault(), "#%d - %s", sale.getId(), dateStr));
            tvTotal.setText(String.format(Locale.getDefault(), "$%.2f", sale.getTotal()));

            chipStatus.setText(sale.getStatus());
            switch (sale.getStatus()) {
                case Constants.STATUS_PENDIENTE:
                    chipStatus.setChipBackgroundColorResource(R.color.colorWarning);
                    break;
                case Constants.STATUS_PAGADA:
                case Constants.STATUS_COMPLETADA:
                    chipStatus.setChipBackgroundColorResource(R.color.colorSuccess);
                    break;
                case Constants.STATUS_CANCELADA:
                    chipStatus.setChipBackgroundColorResource(R.color.colorError);
                    break;
                default:
                    chipStatus.setChipBackgroundColorResource(R.color.colorTextSecondary);
                    break;
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) listener.onSaleClick(sale);
            });
        }
    }

    public interface OnSaleClickListener {
        void onSaleClick(Sale sale);
    }
}
