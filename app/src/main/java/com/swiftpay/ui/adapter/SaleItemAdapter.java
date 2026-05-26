package com.swiftpay.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.swiftpay.R;
import com.swiftpay.data.entity.SaleItemWithProduct;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SaleItemAdapter extends RecyclerView.Adapter<SaleItemAdapter.SaleItemViewHolder> {

    private List<SaleItemWithProduct> items = new ArrayList<>();

    public void submitList(List<SaleItemWithProduct> newItems) {
        this.items = newItems;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public SaleItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_sale_item, parent, false);
        return new SaleItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SaleItemViewHolder holder, int position) {
        SaleItemWithProduct item = items.get(position);
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class SaleItemViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvQty, tvName, tvUnitPrice, tvSubtotal;

        SaleItemViewHolder(@NonNull View itemView) {
            super(itemView);
            tvQty = itemView.findViewById(R.id.tv_item_qty);
            tvName = itemView.findViewById(R.id.tv_item_name);
            tvUnitPrice = itemView.findViewById(R.id.tv_item_unit_price);
            tvSubtotal = itemView.findViewById(R.id.tv_item_subtotal);
        }

        void bind(SaleItemWithProduct wrappedItem) {
            com.swiftpay.data.entity.SaleItem item = wrappedItem.saleItem;
            tvQty.setText(item.getQuantity() + "x");
            
            if (wrappedItem.product != null) {
                tvName.setText(wrappedItem.product.getName());
            } else {
                tvName.setText("Prod #" + item.getProductId());
            }

            tvUnitPrice.setText(String.format(Locale.getDefault(), "$%.2f c/u", item.getUnitPrice()));
            tvSubtotal.setText(String.format(Locale.getDefault(), "$%.2f", item.getQuantity() * item.getUnitPrice()));
        }
    }
}
