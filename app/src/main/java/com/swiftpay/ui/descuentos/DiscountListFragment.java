package com.swiftpay.ui.descuentos;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.swiftpay.MainActivity;
import com.swiftpay.R;
import com.swiftpay.data.entity.DiscountCode;
import com.swiftpay.data.preferences.SessionManager;
import com.swiftpay.ui.adapter.DiscountPagingAdapter;
import com.swiftpay.viewmodel.DiscountViewModel;

/**
 * Fragmento para listar códigos de descuento.
 */
public class DiscountListFragment extends Fragment {

    private DiscountViewModel viewModel;
    private DiscountPagingAdapter adapter;
    private SessionManager sessionManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_discount_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        sessionManager = ((MainActivity) requireActivity()).getSessionManager();
        if (!sessionManager.hasRole("ADMINISTRADOR") && !sessionManager.hasRole("VENDEDOR")) {
            ((MainActivity) requireActivity()).getNavController().navigate(R.id.accessDeniedFragment);
            return;
        }

        viewModel = new ViewModelProvider(this).get(DiscountViewModel.class);

        RecyclerView rvDiscounts = view.findViewById(R.id.rv_discounts);
        FloatingActionButton fabAdd = view.findViewById(R.id.fab_add_discount);

        if (sessionManager.hasRole("VENDEDOR") && !sessionManager.hasRole("ADMINISTRADOR")) {
            fabAdd.setVisibility(View.GONE);
        }

        adapter = new DiscountPagingAdapter(
            code -> {
                // El usuario solicitó que no pase nada al tocar un descuento y no mostrar toast.
            },
            (code, isActive) -> {
                if (sessionManager.hasRole("VENDEDOR") && !sessionManager.hasRole("ADMINISTRADOR")) {
                    adapter.notifyDataSetChanged(); // Revert visual switch state silently sin Toast
                    return;
                }
                code.setIsActive(isActive ? 1 : 0);
                viewModel.saveDiscount(code, sessionManager.getUserId());
            }
        );

        rvDiscounts.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvDiscounts.setAdapter(adapter);

        viewModel.getAllDiscountsPaged().observe(getViewLifecycleOwner(), pagingData -> {
            adapter.submitData(getLifecycle(), pagingData);
        });

        viewModel.getOperationMessage().observe(getViewLifecycleOwner(), msg -> {
            if (msg != null && !msg.isEmpty()) {
                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
            }
        });

        fabAdd.setOnClickListener(v -> {
            androidx.navigation.Navigation.findNavController(v).navigate(R.id.action_discountList_to_discountForm);
        });
    }
}
