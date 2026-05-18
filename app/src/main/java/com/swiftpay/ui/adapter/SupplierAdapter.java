package com.swiftpay.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.swiftpay.R;
import com.swiftpay.data.entity.Supplier;

import java.util.ArrayList;
import java.util.List;

public class SupplierAdapter extends RecyclerView.Adapter<SupplierAdapter.SupplierViewHolder> {
    private List<Supplier> suppliers = new ArrayList<>();
    private final OnSupplierClickListener listener;

    public interface OnSupplierClickListener {
        void onSupplierClick(Supplier supplier);
    }

    public SupplierAdapter(OnSupplierClickListener listener) {
        this.listener = listener;
    }

    public void setSuppliers(List<Supplier> suppliers) {
        this.suppliers = suppliers;
        notifyDataSetDataSetChanged();
    }

    private void notifyDataSetDataSetChanged() {
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public SupplierViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_supplier, parent, false);
        return new SupplierViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull SupplierViewHolder holder, int position) {
        Supplier current = suppliers.get(position);
        holder.tvName.setText(current.getName());
        holder.tvRfc.setText("RFC: " + (current.getRfc() != null ? current.getRfc() : "N/A"));
        holder.tvPhone.setText("Tel: " + (current.getPhone() != null ? current.getPhone() : "N/A"));
        
        holder.itemView.setOnClickListener(v -> listener.onSupplierClick(current));
    }

    @Override
    public int getItemCount() {
        return suppliers.size();
    }

    static class SupplierViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvName;
        private final TextView tvRfc;
        private final TextView tvPhone;

        public SupplierViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_supplier_name);
            tvRfc = itemView.findViewById(R.id.tv_supplier_rfc);
            tvPhone = itemView.findViewById(R.id.tv_supplier_phone);
        }
    }
}
