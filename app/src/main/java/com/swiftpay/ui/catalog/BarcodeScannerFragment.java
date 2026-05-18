package com.swiftpay.ui.catalog;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;
import com.swiftpay.MainActivity;
import com.swiftpay.R;
import com.swiftpay.viewmodel.ProductViewModel;

/**
 * Escáner de código de barras usando ZXing.
 * UX-C2: Escáner de código de barras rápido.
 */
public class BarcodeScannerFragment extends Fragment {

    private DecoratedBarcodeView barcodeView;
    private ProductViewModel viewModel;
    private boolean isScanning = false;

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    startScanning();
                } else {
                    Toast.makeText(requireContext(), "Permiso de cámara denegado", Toast.LENGTH_LONG).show();
                    ((MainActivity) requireActivity()).getNavController().popBackStack();
                }
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_barcode_scanner, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        barcodeView = view.findViewById(R.id.barcode_scanner);
        viewModel = new ViewModelProvider(requireActivity()).get(ProductViewModel.class);

        // Verificar permiso
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            startScanning();
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA);
        }

        // Observar resultado
        viewModel.getScannedProductResult().observe(getViewLifecycleOwner(), product -> {
            if (product != null && isScanning) {
                isScanning = false;
                barcodeView.pause();
                Toast.makeText(requireContext(), "Producto encontrado: " + product.getName(), Toast.LENGTH_SHORT).show();
                // Aquí se podría navegar a ProductDetailFragment o añadirlo al carrito de ventas
                ((MainActivity) requireActivity()).getNavController().popBackStack();
            }
        });
    }

    private void startScanning() {
        isScanning = true;
        barcodeView.decodeSingle(result -> {
            if (result.getText() != null) {
                String sku = result.getText();
                Toast.makeText(requireContext(), "Buscando SKU: " + sku, Toast.LENGTH_SHORT).show();
                viewModel.searchBySku(sku);
            }
        });
        barcodeView.resume();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (isScanning) {
            barcodeView.resume();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        barcodeView.pause();
    }
}
