package com.swiftpay.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.swiftpay.R;
import com.swiftpay.data.entity.OrderItemDraft;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DraftItemAdapter extends RecyclerView.Adapter<DraftItemAdapter.DraftItemViewHolder> {

    private List<OrderItemDraft> items = new ArrayList<>();
    private final OnDraftItemInteractionListener listener;

    public DraftItemAdapter(OnDraftItemInteractionListener listener) {
        this.listener = listener;
    }

    public void submitList(List<OrderItemDraft> newItems) {
        this.items = newItems;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public DraftItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_draft_item, parent, false);
        return new DraftItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DraftItemViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items == null ? 0 : items.size();
    }

    class DraftItemViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvName, tvDetails;
        private final ImageButton btnRemove;

        DraftItemViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_draft_item_name);
            tvDetails = itemView.findViewById(R.id.tv_draft_item_details);
            btnRemove = itemView.findViewById(R.id.btn_remove_draft_item);
        }

        void bind(OrderItemDraft item) {
            tvName.setText(item.getProductName());
            tvDetails.setText(String.format(Locale.getDefault(), "%d x $%.2f = $%.2f", 
                    item.getQuantity(), item.getUnitCost(), item.getSubtotal()));
            
            btnRemove.setOnClickListener(v -> {
                if (listener != null) listener.onRemoveItem(item.getProductId());
            });
        }
    }

    public interface OnDraftItemInteractionListener {
        void onRemoveItem(long productId);
    }
}
