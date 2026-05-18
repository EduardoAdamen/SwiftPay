package com.swiftpay.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.paging.PagingDataAdapter;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;
import com.swiftpay.R;
import com.swiftpay.data.entity.Brand;

public class BrandPagingAdapter extends PagingDataAdapter<Brand, BrandPagingAdapter.BrandViewHolder> {

    private final OnBrandClickListener listener;

    public BrandPagingAdapter(OnBrandClickListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    private static final DiffUtil.ItemCallback<Brand> DIFF_CALLBACK = new DiffUtil.ItemCallback<Brand>() {
        @Override
        public boolean areItemsTheSame(@NonNull Brand oldItem, @NonNull Brand newItem) {
            return oldItem.getId() == newItem.getId();
        }
        @Override
        public boolean areContentsTheSame(@NonNull Brand oldItem, @NonNull Brand newItem) {
            return oldItem.getName().equals(newItem.getName());
        }
    };

    @NonNull
    @Override
    public BrandViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_brand, parent, false);
        return new BrandViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BrandViewHolder holder, int position) {
        Brand brand = getItem(position);
        if (brand != null) {
            holder.bind(brand);
        }
    }

    class BrandViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvName;

        BrandViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_brand_name);
        }

        void bind(Brand brand) {
            tvName.setText(brand.getName());
            itemView.setOnClickListener(v -> {
                if (listener != null) listener.onBrandClick(brand);
            });
        }
    }

    public interface OnBrandClickListener {
        void onBrandClick(Brand brand);
    }
}
