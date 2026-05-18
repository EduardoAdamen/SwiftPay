package com.swiftpay.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.swiftpay.R;
import com.swiftpay.data.entity.PurchaseOrder;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PurchaseOrderAdapter extends RecyclerView.Adapter<PurchaseOrderAdapter.OrderViewHolder> {
    private List<PurchaseOrder> orders = new ArrayList<>();
    private final OnOrderClickListener listener;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

    public interface OnOrderClickListener {
        void onOrderClick(PurchaseOrder order);
    }

    public PurchaseOrderAdapter(OnOrderClickListener listener) {
        this.listener = listener;
    }

    public void setOrders(List<PurchaseOrder> orders) {
        this.orders = orders;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_purchase_order, parent, false);
        return new OrderViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        PurchaseOrder current = orders.get(position);
        holder.tvOrderId.setText("Orden #: " + current.getId());
        holder.tvStatus.setText("Estado: " + current.getStatus());
        holder.tvTotal.setText(String.format(Locale.getDefault(), "Total: $%.2f", current.getTotal()));
        holder.tvDate.setText("Fecha: " + dateFormat.format(new Date(current.getCreatedAt())));
        
        holder.itemView.setOnClickListener(v -> listener.onOrderClick(current));
    }

    @Override
    public int getItemCount() {
        return orders.size();
    }

    static class OrderViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvOrderId;
        private final TextView tvStatus;
        private final TextView tvTotal;
        private final TextView tvDate;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvOrderId = itemView.findViewById(R.id.tv_order_id);
            tvStatus = itemView.findViewById(R.id.tv_order_status);
            tvTotal = itemView.findViewById(R.id.tv_order_total);
            tvDate = itemView.findViewById(R.id.tv_order_date);
        }
    }
}
