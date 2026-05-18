package com.swiftpay.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.paging.PagingDataAdapter;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.swiftpay.R;
import com.swiftpay.data.entity.DiscountCode;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DiscountPagingAdapter extends PagingDataAdapter<DiscountCode, DiscountPagingAdapter.DiscountViewHolder> {

    private final OnDiscountClickListener listener;
    private final OnDiscountToggleListener toggleListener;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

    public DiscountPagingAdapter(OnDiscountClickListener listener, OnDiscountToggleListener toggleListener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
        this.toggleListener = toggleListener;
    }

    private static final DiffUtil.ItemCallback<DiscountCode> DIFF_CALLBACK = new DiffUtil.ItemCallback<DiscountCode>() {
        @Override
        public boolean areItemsTheSame(@NonNull DiscountCode oldItem, @NonNull DiscountCode newItem) {
            return oldItem.getId() == newItem.getId();
        }
        @Override
        public boolean areContentsTheSame(@NonNull DiscountCode oldItem, @NonNull DiscountCode newItem) {
            return oldItem.getCode().equals(newItem.getCode())
                    && oldItem.getDiscountPercentage() == newItem.getDiscountPercentage()
                    && oldItem.getIsActive() == newItem.getIsActive()
                    && oldItem.getExpirationDate() == newItem.getExpirationDate();
        }
    };

    @NonNull
    @Override
    public DiscountViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_discount, parent, false);
        return new DiscountViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DiscountViewHolder holder, int position) {
        DiscountCode code = getItem(position);
        if (code != null) {
            holder.bind(code);
        }
    }

    class DiscountViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvCode, tvPercentage, tvExpiration;
        private final SwitchMaterial switchActive;

        DiscountViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCode = itemView.findViewById(R.id.tv_discount_code);
            tvPercentage = itemView.findViewById(R.id.tv_discount_percentage);
            tvExpiration = itemView.findViewById(R.id.tv_discount_expiration);
            switchActive = itemView.findViewById(R.id.switch_discount_active);
        }

        void bind(DiscountCode code) {
            tvCode.setText(code.getCode());
            tvPercentage.setText(String.format(Locale.getDefault(), "%.0f%%", code.getDiscountPercentage()));
            
            String expiration = dateFormat.format(new Date(code.getExpirationDate()));
            boolean isExpired = code.getExpirationDate() < System.currentTimeMillis();
            
            if (isExpired) {
                tvExpiration.setText("Expiró el: " + expiration);
                tvExpiration.setTextColor(itemView.getContext().getColor(R.color.colorError));
            } else {
                tvExpiration.setText("Expira: " + expiration);
                tvExpiration.setTextColor(itemView.getContext().getColor(R.color.colorTextSecondary));
            }

            switchActive.setOnCheckedChangeListener(null);
            switchActive.setChecked(code.getIsActive() == 1);
            switchActive.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (toggleListener != null) toggleListener.onToggle(code, isChecked);
            });

            itemView.setOnClickListener(v -> {
                if (listener != null) listener.onDiscountClick(code);
            });
        }
    }

    public interface OnDiscountClickListener {
        void onDiscountClick(DiscountCode code);
    }

    public interface OnDiscountToggleListener {
        void onToggle(DiscountCode code, boolean isActive);
    }
}
