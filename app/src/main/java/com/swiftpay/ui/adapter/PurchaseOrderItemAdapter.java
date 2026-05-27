package com.swiftpay.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.swiftpay.R;
import com.swiftpay.data.entity.PurchaseOrderItemWithProduct;

import java.util.Locale;

public class PurchaseOrderItemAdapter extends ListAdapter<PurchaseOrderItemWithProduct, PurchaseOrderItemAdapter.ViewHolder> {

    public PurchaseOrderItemAdapter() {
        super(new DiffUtil.ItemCallback<PurchaseOrderItemWithProduct>() {
            @Override
            public boolean areItemsTheSame(@NonNull PurchaseOrderItemWithProduct oldItem, @NonNull PurchaseOrderItemWithProduct newItem) {
                return oldItem.item.getId() == newItem.item.getId();
            }

            @Override
            public boolean areContentsTheSame(@NonNull PurchaseOrderItemWithProduct oldItem, @NonNull PurchaseOrderItemWithProduct newItem) {
                return oldItem.item.getQuantity() == newItem.item.getQuantity() &&
                       oldItem.item.getUnitCost() == newItem.item.getUnitCost() &&
                       oldItem.item.getProductId() == newItem.item.getProductId();
            }
        });
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_draft_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PurchaseOrderItemWithProduct current = getItem(position);
        holder.bind(current);
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private TextView tvName, tvDetails;
        private View btnRemove;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_draft_item_name);
            tvDetails = itemView.findViewById(R.id.tv_draft_item_details);
            btnRemove = itemView.findViewById(R.id.btn_remove_draft_item);
            
            if (btnRemove != null) btnRemove.setVisibility(View.GONE);
        }

        public void bind(PurchaseOrderItemWithProduct itemWithProduct) {
            String name = itemWithProduct.product != null ? itemWithProduct.product.getName() : "Producto #" + itemWithProduct.item.getProductId();
            tvName.setText(name);
            
            double subtotal = itemWithProduct.item.getQuantity() * itemWithProduct.item.getUnitCost();
            tvDetails.setText(String.format(Locale.getDefault(), "%d x $%.2f = $%.2f", 
                    itemWithProduct.item.getQuantity(), itemWithProduct.item.getUnitCost(), subtotal));
        }
    }
}
