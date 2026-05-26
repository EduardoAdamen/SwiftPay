package com.swiftpay.ui.catalog;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.swiftpay.MainActivity;
import com.swiftpay.R;
import com.swiftpay.data.entity.Brand;
import com.swiftpay.data.entity.Product;
import com.swiftpay.data.entity.Supplier;
import com.swiftpay.util.ValidationUtils;
import android.net.Uri;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import android.widget.ImageView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.swiftpay.util.ImageLoader;
import com.swiftpay.viewmodel.BrandViewModel;
import com.swiftpay.viewmodel.ProductViewModel;
import com.swiftpay.viewmodel.SupplierViewModel;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import java.util.ArrayList;
import java.util.List;

public class ProductFormFragment extends Fragment {

    private ProductViewModel viewModel;
    private SupplierViewModel supplierViewModel;
    private BrandViewModel brandViewModel;
    private long productId = -1;
    private Product currentProduct;
    private String selectedImagePath = null;
    private ActivityResultLauncher<String> imagePickerLauncher;

    private ImageView ivProductImage;
    private FloatingActionButton fabChangeImage;
    private TextInputLayout tilSku, tilName, tilPrice, tilStock, tilBrand, tilSupplier;
    private TextInputEditText etSku, etName, etPrice, etStock;
    private AutoCompleteTextView actBrand, actSupplier;
    private MaterialButton btnSave;

    /** Lista de proveedores cargados del ViewModel */
    private List<Supplier> supplierList = new ArrayList<>();
    /** Posición seleccionada en el dropdown (-1 = sin proveedor) */
    private int selectedSupplierPosition = -1;

    /** Lista de marcas cargadas del ViewModel */
    private List<Brand> brandList = new ArrayList<>();
    /** Posición seleccionada en el dropdown (-1 = sin marca) */
    private int selectedBrandPosition = -1;

    public ProductFormFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_product_form, container, false);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        handleImageSelection(uri);
                    }
                }
        );
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(ProductViewModel.class);
        supplierViewModel = new ViewModelProvider(this).get(SupplierViewModel.class);
        brandViewModel = new ViewModelProvider(this).get(BrandViewModel.class);

        if (getArguments() != null) {
            productId = getArguments().getLong("productId", -1);
        }

        ivProductImage = view.findViewById(R.id.iv_product_image);
        fabChangeImage = view.findViewById(R.id.fab_change_image);
        tilSku = view.findViewById(R.id.til_sku);
        tilName = view.findViewById(R.id.til_name);
        tilPrice = view.findViewById(R.id.til_price);
        tilStock = view.findViewById(R.id.til_stock);
        tilBrand = view.findViewById(R.id.til_brand);
        tilSupplier = view.findViewById(R.id.til_supplier);
        
        etSku = view.findViewById(R.id.et_sku);
        etName = view.findViewById(R.id.et_name);
        etPrice = view.findViewById(R.id.et_price);
        etStock = view.findViewById(R.id.et_stock);
        actBrand = view.findViewById(R.id.act_brand);
        actSupplier = view.findViewById(R.id.act_supplier);
        btnSave = view.findViewById(R.id.btn_save);

        // --- Eventos ---
        fabChangeImage.setOnClickListener(v -> {
            imagePickerLauncher.launch("image/*");
        });

        // --- Setup dropdowns ---
        setupBrandDropdown();
        setupSupplierDropdown();

        if (productId != -1) {
            viewModel.getProductById(productId).observe(getViewLifecycleOwner(), product -> {
                if (product != null) {
                    currentProduct = product;
                    selectedImagePath = product.getImagePath();
                    etSku.setText(product.getSku());
                    etName.setText(product.getName());
                    etPrice.setText(String.valueOf(product.getPrice()));
                    etStock.setText(String.valueOf(product.getStock()));
                    // Pre-seleccionar
                    preselectBrand(product.getBrandId());
                    preselectSupplier(product.getSupplierId());
                    if (selectedImagePath != null && !selectedImagePath.isEmpty()) {
                        ImageLoader.loadLocalImage(requireContext(), selectedImagePath, ivProductImage, true);
                    }
                }
            });
        }

        btnSave.setOnClickListener(v -> saveProduct());

        viewModel.getOperationSuccess().observe(getViewLifecycleOwner(), success -> {
            if (Boolean.TRUE.equals(success)) {
                Toast.makeText(requireContext(), "Producto guardado", Toast.LENGTH_SHORT).show();
                requireActivity().getOnBackPressedDispatcher().onBackPressed();
            }
        });

        viewModel.getOperationMessage().observe(getViewLifecycleOwner(), msg -> {
            if (msg != null && !Boolean.TRUE.equals(viewModel.getOperationSuccess().getValue())) {
                // RNF 4.4: No conflict dialog in ProductFormFragment
                // The repository throws OptimisticLockException which sets the message to:
                // "El producto fue modificado por otro usuario..."
                if (msg.contains("modificado por otro usuario")) {
                    new AlertDialog.Builder(requireContext())
                        .setTitle(R.string.optimistic_lock_title)
                        .setMessage(R.string.optimistic_lock_message)
                        .setPositiveButton(R.string.btn_retry, (dialog, which) -> {
                            // Reload the product
                            if (productId != -1) {
                                viewModel.getProductById(productId).removeObservers(getViewLifecycleOwner());
                                viewModel.getProductById(productId).observe(getViewLifecycleOwner(), product -> {
                                    if (product != null) {
                                        currentProduct = product;
                                        selectedImagePath = product.getImagePath();
                                        etSku.setText(product.getSku());
                                        etName.setText(product.getName());
                                        etPrice.setText(String.valueOf(product.getPrice()));
                                        etStock.setText(String.valueOf(product.getStock()));
                                        preselectBrand(product.getBrandId());
                                        preselectSupplier(product.getSupplierId());
                                        if (selectedImagePath != null && !selectedImagePath.isEmpty()) {
                                            ImageLoader.loadLocalImage(requireContext(), selectedImagePath, ivProductImage, true);
                                        }
                                    }
                                });
                            }
                        })
                        .setNegativeButton(R.string.btn_cancel, null)
                        .show();
                } else {
                    Toast.makeText(requireContext(), msg, Toast.LENGTH_LONG).show();
                }
            }
        });
        
        viewModel.getIsLoading().observe(getViewLifecycleOwner(), loading -> btnSave.setEnabled(!Boolean.TRUE.equals(loading)));
    }

    private void handleImageSelection(Uri uri) {
        try {
            InputStream in = requireContext().getContentResolver().openInputStream(uri);
            if (in == null) return;
            
            // Generate a unique filename
            String fileName = "product_" + UUID.randomUUID().toString() + ".jpg";
            File destFile = new File(requireContext().getFilesDir(), fileName);
            OutputStream out = new FileOutputStream(destFile);
            
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            in.close();
            out.close();
            
            selectedImagePath = fileName;
            ImageLoader.loadLocalImage(requireContext(), selectedImagePath, ivProductImage, true);
        } catch (Exception e) {
            Toast.makeText(requireContext(), "Error al guardar la imagen", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    /**
     * Configura el dropdown de marcas.
     */
    private void setupBrandDropdown() {
        brandViewModel.getAllBrands().observe(getViewLifecycleOwner(), brands -> {
            brandList = brands != null ? brands : new ArrayList<>();
            List<String> displayNames = new ArrayList<>();
            displayNames.add("Sin marca");
            for (Brand b : brandList) {
                displayNames.add(b.getName());
            }
            ArrayAdapter<String> adapter = new ArrayAdapter<>(
                    requireContext(),
                    android.R.layout.simple_dropdown_item_1line,
                    displayNames
            );
            actBrand.setAdapter(adapter);

            actBrand.setOnItemClickListener((parent, v, position, id) -> {
                selectedBrandPosition = position - 1; // -1 porque index 0 = "Sin marca"
            });

            if (currentProduct != null) {
                preselectBrand(currentProduct.getBrandId());
            }
        });
    }

    /**
     * Pre-selecciona la marca en el dropdown.
     */
    private void preselectBrand(Long brandId) {
        if (brandId == null || brandId == 0) {
            actBrand.setText("Sin marca", false);
            selectedBrandPosition = -1;
            return;
        }
        for (int i = 0; i < brandList.size(); i++) {
            if (brandList.get(i).getId() == brandId) {
                actBrand.setText(brandList.get(i).getName(), false);
                selectedBrandPosition = i;
                return;
            }
        }
        actBrand.setText("Sin marca", false);
        selectedBrandPosition = -1;
    }

    /**
     * Configura el dropdown de proveedores con la lista de proveedores del ViewModel.
     * La primera opción es "Sin proveedor" para permitir productos sin proveedor asignado.
     */
    private void setupSupplierDropdown() {
        supplierViewModel.getAllSuppliers().observe(getViewLifecycleOwner(), suppliers -> {
            supplierList = suppliers != null ? suppliers : new ArrayList<>();
            List<String> displayNames = new ArrayList<>();
            displayNames.add("Sin proveedor");
            for (Supplier s : supplierList) {
                displayNames.add(s.getName());
            }
            ArrayAdapter<String> adapter = new ArrayAdapter<>(
                    requireContext(),
                    android.R.layout.simple_dropdown_item_1line,
                    displayNames
            );
            actSupplier.setAdapter(adapter);

            actSupplier.setOnItemClickListener((parent, v, position, id) -> {
                selectedSupplierPosition = position - 1; // -1 porque index 0 = "Sin proveedor"
            });

            // Si estamos editando y ya se cargó el producto, pre-seleccionar
            if (currentProduct != null) {
                preselectSupplier(currentProduct.getSupplierId());
            }
        });
    }

    /**
     * Pre-selecciona el proveedor en el dropdown basándose en el supplierId del producto.
     */
    private void preselectSupplier(Long supplierId) {
        if (supplierId == null) {
            actSupplier.setText("Sin proveedor", false);
            selectedSupplierPosition = -1;
            return;
        }
        for (int i = 0; i < supplierList.size(); i++) {
            if (supplierList.get(i).getId() == supplierId) {
                actSupplier.setText(supplierList.get(i).getName(), false);
                selectedSupplierPosition = i;
                return;
            }
        }
        // Si no se encontró, dejar como "Sin proveedor"
        actSupplier.setText("Sin proveedor", false);
        selectedSupplierPosition = -1;
    }

    private void saveProduct() {
        tilSku.setError(null);
        tilName.setError(null);
        tilPrice.setError(null);
        tilStock.setError(null);

        String sku = etSku.getText() != null ? etSku.getText().toString().trim() : "";
        String name = etName.getText() != null ? etName.getText().toString().trim() : "";
        String priceStr = etPrice.getText() != null ? etPrice.getText().toString().trim() : "";
        String stockStr = etStock.getText() != null ? etStock.getText().toString().trim() : "";

        boolean valid = true;
        if (sku.isEmpty() || sku.length() < 3) {
            tilSku.setError("SKU muy corto");
            valid = false;
        }
        if (name.isEmpty()) {
            tilName.setError("Nombre requerido");
            valid = false;
        }
        
        double price = 0;
        try {
            price = Double.parseDouble(priceStr);
            if (price <= 0) {
                tilPrice.setError("Precio inválido");
                valid = false;
            }
        } catch (NumberFormatException e) {
            tilPrice.setError("Precio requerido");
            valid = false;
        }
        
        int stock = 0;
        try {
            stock = Integer.parseInt(stockStr);
            if (stock < 0) {
                tilStock.setError("Stock inválido");
                valid = false;
            }
        } catch (NumberFormatException e) {
            tilStock.setError("Stock requerido");
            valid = false;
        }

        if (!valid) return;

        if (currentProduct == null) {
            currentProduct = new Product();
            currentProduct.setIsActive(1);
        }
        
        currentProduct.setSku(sku);
        currentProduct.setName(name);
        currentProduct.setPrice(price);
        currentProduct.setStock(stock);
        currentProduct.setImagePath(selectedImagePath);

        // Como no usamos categoría en el UI pero es FK requerida en BD, se fuerza un default
        currentProduct.setCategoryId(1);

        // Asignar proveedor seleccionado
        if (selectedSupplierPosition >= 0 && selectedSupplierPosition < supplierList.size()) {
            currentProduct.setSupplierId(supplierList.get(selectedSupplierPosition).getId());
        } else {
            currentProduct.setSupplierId(null);
        }

        // Asignar marca seleccionada
        if (selectedBrandPosition >= 0 && selectedBrandPosition < brandList.size()) {
            currentProduct.setBrandId(brandList.get(selectedBrandPosition).getId());
        } else {
            currentProduct.setBrandId(null);
        }

        long userId = ((MainActivity) requireActivity()).getSessionManager().getUserId();
        viewModel.saveProduct(currentProduct, userId);
    }
}