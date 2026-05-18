package com.swiftpay.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.swiftpay.R;
import com.swiftpay.data.entity.SaleStatusHistory;
import com.swiftpay.util.Constants;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SaleStatusHistoryAdapter extends RecyclerView.Adapter<SaleStatusHistoryAdapter.HistoryViewHolder> {

    private List<SaleStatusHistory> items = new ArrayList<>();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat(Constants.DATE_FORMAT_DISPLAY, Locale.getDefault());

    public void submitList(List<SaleStatusHistory> newItems) {
        this.items = newItems;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_sale_status_history, parent, false);
        return new HistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
        SaleStatusHistory item = items.get(position);
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class HistoryViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvDate, tvOld, tvNew, tvUser;

        HistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDate = itemView.findViewById(R.id.tv_history_date);
            tvOld = itemView.findViewById(R.id.tv_history_old);
            tvNew = itemView.findViewById(R.id.tv_history_new);
            tvUser = itemView.findViewById(R.id.tv_history_user);
        }

        void bind(SaleStatusHistory item) {
            tvDate.setText(dateFormat.format(new Date(item.getChangedAt())));
            tvOld.setText(item.getPreviousStatus() != null ? item.getPreviousStatus() : "CREADO");
            tvNew.setText(item.getNewStatus());
            tvUser.setText("Usuario #" + item.getChangedBy());
        }
    }
}
