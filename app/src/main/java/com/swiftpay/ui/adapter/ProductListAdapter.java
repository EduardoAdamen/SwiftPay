package com.swiftpay.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.swiftpay.R;
import com.swiftpay.data.entity.Product;
import com.swiftpay.util.CurrencyUtils;
import com.swiftpay.util.ImageLoader;

/**
 * Lista simple de productos (sin paginación) para selección rápida en ventas.
 */
public class ProductListAdapter extends ListAdapter<Product, ProductListAdapter.ProductViewHolder> {

    private final OnProductClickListener listener;

    public ProductListAdapter(OnProductClickListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    private static final DiffUtil.ItemCallback<Product> DIFF_CALLBACK = new DiffUtil.ItemCallback<Product>() {
        @Override
        public boolean areItemsTheSame(@NonNull Product oldItem, @NonNull Product newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull Product oldItem, @NonNull Product newItem) {
            boolean sameImage = (oldItem.getImagePath() == null && newItem.getImagePath() == null) ||
                                (oldItem.getImagePath() != null && oldItem.getImagePath().equals(newItem.getImagePath()));
                                
            return oldItem.getSku().equals(newItem.getSku())
                    && oldItem.getName().equals(newItem.getName())
                    && oldItem.getPrice() == newItem.getPrice()
                    && oldItem.getStock() == newItem.getStock()
                    && sameImage;
        }
    };

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_product, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    class ProductViewHolder extends RecyclerView.ViewHolder {
        private final ImageView ivImage;
        private final TextView tvName, tvSku, tvPrice, tvStock;

        ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            ivImage = itemView.findViewById(R.id.iv_product_image);
            tvName = itemView.findViewById(R.id.tv_product_name);
            tvSku = itemView.findViewById(R.id.tv_product_sku);
            tvPrice = itemView.findViewById(R.id.tv_product_price);
            tvStock = itemView.findViewById(R.id.tv_product_stock);
        }

        void bind(Product product) {
            tvName.setText(product.getName());
            tvSku.setText("SKU: " + product.getSku());
            tvPrice.setText(CurrencyUtils.format(product.getPrice()));
            tvStock.setText("Stock: " + product.getStock());

            if (product.getStock() <= 5) {
                tvStock.setTextColor(itemView.getContext().getColor(R.color.colorWarning));
            } else {
                tvStock.setTextColor(itemView.getContext().getColor(R.color.colorTextSecondary));
            }

            if (product.getImagePath() != null && !product.getImagePath().isEmpty()) {
                ImageLoader.loadLocalImage(itemView.getContext(), product.getImagePath(), ivImage, true);
            } else {
                ivImage.setImageResource(R.drawable.ic_inventory_2);
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onProductClick(product);
                }
            });
        }
    }

    public interface OnProductClickListener {
        void onProductClick(Product product);
    }
}
