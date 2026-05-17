package com.swiftpay.ui.auth;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.swiftpay.MainActivity;
import com.swiftpay.R;

/**
 * Fragment placeholder de Login. Será implementado completamente en Sprint 1.
 */
public class LoginFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Layout placeholder simple
        TextView tv = new TextView(requireContext());
        tv.setText(R.string.login_placeholder);
        tv.setTextSize(24);
        tv.setPadding(64, 64, 64, 64);
        return tv;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Bloquear drawer en login
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).setDrawerLocked(true);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).setDrawerLocked(false);
        }
    }
}
