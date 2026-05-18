package com.swiftpay.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.swiftpay.R;
import com.swiftpay.data.entity.CartItem;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CartItemAdapter extends RecyclerView.Adapter<CartItemAdapter.CartItemViewHolder> {

    private List<CartItem> items = new ArrayList<>();
    private final OnCartItemInteractionListener listener;

    public CartItemAdapter(OnCartItemInteractionListener listener) {
        this.listener = listener;
    }

    public void submitList(List<CartItem> newItems) {
        this.items = newItems;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CartItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cart_item, parent, false);
        return new CartItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartItemViewHolder holder, int position) {
        CartItem item = items.get(position);
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class CartItemViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvName, tvSku, tvPrice, tvQuantity;
        private final ImageButton btnMinus, btnPlus, btnEditPrice;

        CartItemViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_cart_item_name);
            tvSku = itemView.findViewById(R.id.tv_cart_item_sku);
            tvPrice = itemView.findViewById(R.id.tv_cart_item_price);
            tvQuantity = itemView.findViewById(R.id.tv_cart_quantity);
            btnMinus = itemView.findViewById(R.id.btn_cart_minus);
            btnPlus = itemView.findViewById(R.id.btn_cart_plus);
            btnEditPrice = itemView.findViewById(R.id.btn_edit_price);
        }

        void bind(CartItem item) {
            tvName.setText(item.getProductName());
            tvSku.setText("SKU: " + item.getSku());
            tvPrice.setText(String.format(Locale.getDefault(), "$%.2f", item.getUnitPrice()));
            tvQuantity.setText(String.valueOf(item.getQuantity()));

            btnMinus.setOnClickListener(v -> {
                if (listener != null) listener.onQuantityChanged(item.getProductId(), item.getQuantity() - 1);
            });

            btnPlus.setOnClickListener(v -> {
                if (listener != null) listener.onQuantityChanged(item.getProductId(), item.getQuantity() + 1);
            });

            btnEditPrice.setOnClickListener(v -> {
                if (listener != null) listener.onEditPriceRequested(item);
            });
        }
    }

    public interface OnCartItemInteractionListener {
        void onQuantityChanged(long productId, int newQuantity);
        void onEditPriceRequested(CartItem item);
    }
}
