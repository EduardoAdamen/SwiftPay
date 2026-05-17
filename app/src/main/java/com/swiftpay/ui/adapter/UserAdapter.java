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
import com.swiftpay.data.entity.User;

/**
 * Adapter para lista de usuarios en UserManagementFragment.
 */
public class UserAdapter extends ListAdapter<User, UserAdapter.UserViewHolder> {

    private final OnUserActionListener listener;

    public UserAdapter(OnUserActionListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    private static final DiffUtil.ItemCallback<User> DIFF_CALLBACK = new DiffUtil.ItemCallback<User>() {
        @Override
        public boolean areItemsTheSame(@NonNull User oldItem, @NonNull User newItem) {
            return oldItem.getId() == newItem.getId();
        }
        @Override
        public boolean areContentsTheSame(@NonNull User oldItem, @NonNull User newItem) {
            return oldItem.getUsername().equals(newItem.getUsername())
                    && oldItem.getFullName().equals(newItem.getFullName())
                    && oldItem.getRole().equals(newItem.getRole())
                    && oldItem.getIsActive() == newItem.getIsActive();
        }
    };

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = getItem(position);
        holder.bind(user);
    }

    class UserViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvName, tvRole, tvStatus;
        private final ImageView ivMenu;

        UserViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_user_name);
            tvRole = itemView.findViewById(R.id.tv_user_role);
            tvStatus = itemView.findViewById(R.id.tv_user_status);
            ivMenu = itemView.findViewById(R.id.iv_user_menu);
        }

        void bind(User user) {
            tvName.setText(user.getFullName());
            tvRole.setText(user.getRole());

            // Estado activo/inactivo
            boolean active = user.getIsActive() == 1;
            tvStatus.setText(active ? "Activo" : "Inactivo");
            tvStatus.setTextColor(itemView.getContext().getColor(
                    active ? R.color.colorSuccess : R.color.colorError));

            ivMenu.setOnClickListener(v -> {
                if (listener != null) listener.onUserAction(user);
            });

            itemView.setOnClickListener(v -> {
                if (listener != null) listener.onUserAction(user);
            });
        }
    }

    public interface OnUserActionListener {
        void onUserAction(User user);
    }
}
