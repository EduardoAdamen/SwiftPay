package com.swiftpay.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.paging.PagingDataAdapter;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.chip.Chip;
import com.swiftpay.R;
import com.swiftpay.data.entity.Client;

public class ClientPagingAdapter extends PagingDataAdapter<Client, ClientPagingAdapter.ClientViewHolder> {

    private final OnClientClickListener listener;

    public ClientPagingAdapter(OnClientClickListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    private static final DiffUtil.ItemCallback<Client> DIFF_CALLBACK = new DiffUtil.ItemCallback<Client>() {
        @Override
        public boolean areItemsTheSame(@NonNull Client oldItem, @NonNull Client newItem) {
            return oldItem.getId() == newItem.getId();
        }
        @Override
        public boolean areContentsTheSame(@NonNull Client oldItem, @NonNull Client newItem) {
            return oldItem.getFullName().equals(newItem.getFullName())
                    && oldItem.getIsActive() == newItem.getIsActive()
                    && oldItem.getEmail() != null && oldItem.getEmail().equals(newItem.getEmail());
        }
    };

    @NonNull
    @Override
    public ClientViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_client, parent, false);
        return new ClientViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ClientViewHolder holder, int position) {
        Client client = getItem(position);
        if (client != null) {
            holder.bind(client);
        }
    }

    class ClientViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvName, tvEmail, tvPhone;
        private final Chip chipStatus;

        ClientViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_client_name);
            tvEmail = itemView.findViewById(R.id.tv_client_email);
            tvPhone = itemView.findViewById(R.id.tv_client_phone);
            chipStatus = itemView.findViewById(R.id.chip_client_status);
        }

        void bind(Client client) {
            tvName.setText(client.getFullName());
            tvEmail.setText(client.getEmail() != null ? client.getEmail() : "Sin correo");
            tvPhone.setText(client.getPhone() != null ? client.getPhone() : "Sin teléfono");

            if (client.getIsActive() == 1) {
                chipStatus.setText("Activo");
                chipStatus.setChipBackgroundColorResource(R.color.colorSuccess);
            } else {
                chipStatus.setText("Inactivo");
                chipStatus.setChipBackgroundColorResource(R.color.colorError);
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) listener.onClientClick(client);
            });
        }
    }

    public interface OnClientClickListener {
        void onClientClick(Client client);
    }
}
