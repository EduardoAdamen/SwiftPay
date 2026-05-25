// app/src/main/java/com/swiftpay/ui/adapter/ProductPagingAdapter.java
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
import com.swiftpay.R;
import com.swiftpay.data.entity.Product;
import com.swiftpay.util.CurrencyUtils;
import com.swiftpay.util.ImageLoader;

public class ProductPagingAdapter extends PagingDataAdapter<Product, ProductPagingAdapter.ProductViewHolder> {

    private final OnProductClickListener listener;
    private boolean imagesEnabled = true;
    private boolean compactView = false;

    public ProductPagingAdapter(OnProductClickListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    public void setImagesEnabled(boolean imagesEnabled) {
        this.imagesEnabled = imagesEnabled;
        notifyDataSetChanged();
    }

    public void setCompactView(boolean compactView) {
        this.compactView = compactView;
        notifyDataSetChanged();
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
            itemView.setMinimumHeight(itemView.getResources().getDimensionPixelSize(
                    compactView ? R.dimen.list_item_compact_height : R.dimen.list_item_height));
            tvSku.setVisibility(compactView ? View.GONE : View.VISIBLE);

            if (product.getStock() <= 5) {
                tvStock.setTextColor(itemView.getContext().getColor(R.color.colorWarning));
            } else {
                tvStock.setTextColor(itemView.getContext().getColor(R.color.colorTextSecondary));
            }

            if (product.getImagePath() != null && !product.getImagePath().isEmpty()) {
                ImageLoader.loadLocalImage(itemView.getContext(), product.getImagePath(), ivImage, imagesEnabled);
            } else {
                ivImage.setImageResource(R.drawable.ic_image);
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
