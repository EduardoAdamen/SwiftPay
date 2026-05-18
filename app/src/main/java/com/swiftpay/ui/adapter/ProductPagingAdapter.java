package com.swiftpay.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.paging.PagingDataAdapter;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.swiftpay.R;
import com.swiftpay.data.entity.Product;
import com.swiftpay.util.CurrencyUtils;
import java.io.File;

public class ProductPagingAdapter extends PagingDataAdapter<Product, ProductPagingAdapter.ProductViewHolder> {

    private final OnProductClickListener listener;

    public ProductPagingAdapter(OnProductClickListener listener) {
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
            return oldItem.getSku().equals(newItem.getSku())
                    && oldItem.getName().equals(newItem.getName())
                    && oldItem.getPrice() == newItem.getPrice()
                    && oldItem.getStock() == newItem.getStock()
                    && oldItem.getIsActive() == newItem.getIsActive()
                    && oldItem.getVersion() == newItem.getVersion();
        }
    };

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_product, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = getItem(position);
        if (product != null) {
            holder.bind(product);
        }
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

            // Cargar imagen con Glide
            if (product.getImagePath() != null && !product.getImagePath().isEmpty()) {
                File imgFile = new File(itemView.getContext().getFilesDir(), product.getImagePath());
                Glide.with(itemView.getContext())
                        .load(imgFile)
                        .placeholder(R.drawable.ic_image)
                        .error(R.drawable.ic_image)
                        .centerCrop()
                        .into(ivImage);
            } else {
                Glide.with(itemView.getContext())
                        .load(R.drawable.ic_inventory_2)
                        .centerInside()
                        .into(ivImage);
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) listener.onProductClick(product);
            });
        }
    }

    public interface OnProductClickListener {
        void onProductClick(Product product);
    }
}
