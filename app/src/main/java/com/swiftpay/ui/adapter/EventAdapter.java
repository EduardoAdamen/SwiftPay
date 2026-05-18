package com.swiftpay.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.swiftpay.R;
import com.swiftpay.data.entity.SystemEvent;
import com.swiftpay.util.DateUtils;
import java.util.ArrayList;
import java.util.List;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {

    private List<SystemEvent> events = new ArrayList<>();
    private final OnEventInteractListener listener;

    public interface OnEventInteractListener {
        void onMarkAsReviewed(SystemEvent event);
    }

    public EventAdapter(OnEventInteractListener listener) {
        this.listener = listener;
    }

    public void submitList(List<SystemEvent> newEvents) {
        this.events = newEvents;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_event, parent, false);
        return new EventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        SystemEvent event = events.get(position);
        holder.bind(event, listener);
    }

    @Override
    public int getItemCount() {
        return events != null ? events.size() : 0;
    }

    static class EventViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvEventType;
        private final TextView tvEventDescription;
        private final TextView tvEventDate;
        private final Button btnMarkReviewed;

        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            tvEventType = itemView.findViewById(R.id.tvEventType);
            tvEventDescription = itemView.findViewById(R.id.tvEventDescription);
            tvEventDate = itemView.findViewById(R.id.tvEventDate);
            btnMarkReviewed = itemView.findViewById(R.id.btnMarkReviewed);
        }

        public void bind(SystemEvent event, OnEventInteractListener listener) {
            tvEventType.setText(event.getEventType());
            tvEventDescription.setText(event.getEventType() + " ID: " + event.getEntityId());
            tvEventDate.setText(DateUtils.formatDateTime(event.getCreatedAt()));
            btnMarkReviewed.setOnClickListener(v -> listener.onMarkAsReviewed(event));
        }
    }
}
