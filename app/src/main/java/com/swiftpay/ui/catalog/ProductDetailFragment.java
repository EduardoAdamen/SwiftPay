package com.swiftpay.ui.catalog;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import com.google.android.material.button.MaterialButton;
import com.swiftpay.MainActivity;
import com.swiftpay.R;
import com.swiftpay.data.db.SwiftPayDatabase;
import com.swiftpay.data.entity.Product;
import com.swiftpay.data.preferences.SessionManager;
import com.swiftpay.data.repository.UserPreferencesRepository;
import com.swiftpay.util.ImageLoader;
import com.swiftpay.viewmodel.BrandViewModel;
import com.swiftpay.viewmodel.ProductViewModel;
import com.swiftpay.viewmodel.SupplierViewModel;
import java.util.Locale;

public class ProductDetailFragment extends Fragment {

    private ProductViewModel viewModel;
    private SupplierViewModel supplierViewModel;
    private BrandViewModel brandViewModel;
    private long productId = -1;
    private Product currentProduct;
    private SessionManager sessionManager;
    private boolean imagesEnabled = true;

    private ImageView ivProductImage;
    private TextView tvName, tvSku, tvPrice, tvStock, tvDescription, tvBrand, tvSupplier;
    private MaterialButton btnEdit, btnDelete;

    public ProductDetailFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_product_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        sessionManager = ((MainActivity) requireActivity()).getSessionManager();
        viewModel = new ViewModelProvider(this).get(ProductViewModel.class);
        supplierViewModel = new ViewModelProvider(this).get(SupplierViewModel.class);
        brandViewModel = new ViewModelProvider(this).get(BrandViewModel.class);
        final androidx.navigation.NavController navController =
                androidx.navigation.Navigation.findNavController(view);

        if (getArguments() != null) {
            productId = getArguments().getLong("productId", -1);
        }

        ivProductImage = view.findViewById(R.id.iv_product_detail_image);
        tvName = view.findViewById(R.id.tv_detail_name);
        tvSku = view.findViewById(R.id.tv_detail_sku);
        tvPrice = view.findViewById(R.id.tv_detail_price);
        tvStock = view.findViewById(R.id.tv_detail_stock);
        tvDescription = view.findViewById(R.id.tv_detail_description);
        tvBrand = view.findViewById(R.id.tv_detail_brand);
        tvSupplier = view.findViewById(R.id.tv_detail_supplier);
        btnEdit = view.findViewById(R.id.btn_edit_product);
        btnDelete = view.findViewById(R.id.btn_delete_product);

        // Edit and Delete permissions
        if (sessionManager.hasRole("ADMINISTRADOR") || sessionManager.hasRole("GESTOR_PRODUCTOS")) {
            btnEdit.setVisibility(View.VISIBLE);
            btnDelete.setVisibility(View.VISIBLE);
        } else {
            btnEdit.setVisibility(View.GONE);
            btnDelete.setVisibility(View.GONE);
        }

        // Get user preference for images
        new UserPreferencesRepository(SwiftPayDatabase.getInstance(requireContext()))
                .getByUserId(sessionManager.getUserId())
                .observe(getViewLifecycleOwner(), prefs -> {
                    if (prefs != null) {
                        imagesEnabled = prefs.getImagesEnabled() == 1;
                        if (currentProduct != null) {
                            ImageLoader.loadLocalImage(requireContext(), currentProduct.getImagePath(), ivProductImage, imagesEnabled);
                        }
                    }
                });

        if (productId != -1) {
            viewModel.getProductById(productId).observe(getViewLifecycleOwner(), product -> {
                if (product != null) {
                    currentProduct = product;
                    displayProduct(product);
                }
            });
        }

        btnEdit.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putLong("productId", productId);
            navController.navigate(R.id.action_productDetail_to_productForm, bundle);
        });

        btnDelete.setOnClickListener(v -> {
            new AlertDialog.Builder(requireContext())
                    .setTitle("Eliminar Producto")
                    .setMessage("¿Estás seguro de que deseas eliminar este producto?")
                    .setPositiveButton("Eliminar", (dialog, which) -> {
                        viewModel.deleteProduct(currentProduct, sessionManager.getUserId());
                    })
                    .setNegativeButton("Cancelar", null)
                    .show();
        });

        viewModel.getOperationSuccess().observe(getViewLifecycleOwner(), success -> {
            if (Boolean.TRUE.equals(success) && isAdded()) {
                viewModel.resetOperationSuccess();
                Toast.makeText(requireContext(), "Operación realizada con éxito", Toast.LENGTH_SHORT).show();
                navController.navigateUp();
            }
        });

        viewModel.getOperationMessage().observe(getViewLifecycleOwner(), msg -> {
            if (msg != null && !msg.isEmpty() && !Boolean.TRUE.equals(viewModel.getOperationSuccess().getValue())) {
                Toast.makeText(requireContext(), msg, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void displayProduct(Product product) {
        tvName.setText(product.getName());
        tvSku.setText("SKU: " + product.getSku());
        tvPrice.setText(String.format(Locale.getDefault(), "$%.2f", product.getPrice()));
        tvStock.setText(String.valueOf(product.getStock()));

        // Resolver nombre de la marca
        if (product.getBrandId() != null && product.getBrandId() > 0) {
            brandViewModel.getBrandById(product.getBrandId()).observe(getViewLifecycleOwner(), brand -> {
                if (brand != null) {
                    tvBrand.setText(brand.getName());
                } else {
                    tvBrand.setText("Sin marca");
                }
            });
        } else {
            tvBrand.setText("Sin marca");
        }

        // Resolver nombre del proveedor
        if (product.getSupplierId() != null && product.getSupplierId() > 0) {
            supplierViewModel.getSupplier(product.getSupplierId()).observe(getViewLifecycleOwner(), supplier -> {
                if (supplier != null) {
                    tvSupplier.setText(supplier.getName());
                } else {
                    tvSupplier.setText("Sin proveedor");
                }
            });
        } else {
            tvSupplier.setText("Sin proveedor");
        }

        StringBuilder descBuilder = new StringBuilder();
        if (product.getWeight() != null && !product.getWeight().trim().isEmpty()) {
            descBuilder.append("Peso: ").append(product.getWeight()).append("\n");
        }
        if (product.getDimensions() != null && !product.getDimensions().trim().isEmpty()) {
            descBuilder.append("Dimensiones: ").append(product.getDimensions()).append("\n");
        }
        if (product.getTags() != null && !product.getTags().trim().isEmpty()) {
            descBuilder.append("Etiquetas: ").append(product.getTags()).append("\n");
        }
        if (product.getIsActive() == 1) {
            descBuilder.append("Estado: Activo");
        } else {
            descBuilder.append("Estado: Inactivo");
        }
        tvDescription.setText(descBuilder.toString());

        ImageLoader.loadLocalImage(requireContext(), product.getImagePath(), ivProductImage, imagesEnabled);
    }
}