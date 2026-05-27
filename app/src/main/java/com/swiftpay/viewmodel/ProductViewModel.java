package com.swiftpay.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.paging.PagingData;
import com.swiftpay.data.entity.Product;
import com.swiftpay.data.repository.ProductRepository;

/**
 * ViewModel para la gestión del catálogo de Productos.
 */
public class ProductViewModel extends AndroidViewModel {

    private final ProductRepository repository;
    private final MutableLiveData<String> operationMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> operationSuccess = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    
    private LiveData<PagingData<Product>> allPagedProducts;
    private LiveData<PagingData<Product>> activePagedProducts;
    private final MutableLiveData<Product> scannedProductResult = new MutableLiveData<>();
    private final MutableLiveData<String> currentQuery = new MutableLiveData<>("");

    public ProductViewModel(@NonNull Application application) {
        super(application);
        this.repository = new ProductRepository(application);
    }

    public LiveData<String> getOperationMessage() { return operationMessage; }
    public LiveData<Boolean> getOperationSuccess() { return operationSuccess; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<Product> getScannedProductResult() { return scannedProductResult; }

    public void setSearchQuery(String query) {
        if (query == null) query = "";
        if (!query.equals(currentQuery.getValue())) {
            currentQuery.setValue(query);
        }
    }

    /** Obtiene el catálogo completo (para gestores/admin) */
    public LiveData<PagingData<Product>> getAllProductsPaged() {
        if (allPagedProducts == null) {
            allPagedProducts = androidx.lifecycle.Transformations.switchMap(currentQuery, query -> {
                if (query == null || query.trim().isEmpty()) {
                    return androidx.paging.PagingLiveData.cachedIn(
                            repository.getAllProductsPaged(),
                            androidx.lifecycle.ViewModelKt.getViewModelScope(this)
                    );
                } else {
                    return androidx.paging.PagingLiveData.cachedIn(
                            repository.searchAllProductsPaged(query.trim()),
                            androidx.lifecycle.ViewModelKt.getViewModelScope(this)
                    );
                }
            });
        }
        return allPagedProducts;
    }

    /** Obtiene solo los activos (para vendedores) */
    public LiveData<PagingData<Product>> getActiveProductsPaged() {
        if (activePagedProducts == null) {
            activePagedProducts = androidx.lifecycle.Transformations.switchMap(currentQuery, query -> {
                if (query == null || query.trim().isEmpty()) {
                    return androidx.paging.PagingLiveData.cachedIn(
                            repository.getActiveProductsPaged(),
                            androidx.lifecycle.ViewModelKt.getViewModelScope(this)
                    );
                } else {
                    return androidx.paging.PagingLiveData.cachedIn(
                            repository.searchActiveProductsPaged(query.trim()),
                            androidx.lifecycle.ViewModelKt.getViewModelScope(this)
                    );
                }
            });
        }
        return activePagedProducts;
    }

    public LiveData<Product> getProductById(long id) {
        return repository.getProductById(id);
    }

    /** Busca un producto por SKU y actualiza el LiveData de resultado */
    public void searchBySku(String sku) {
        isLoading.setValue(true);
        repository.getProductBySku(sku, product -> {
            isLoading.postValue(false);
            scannedProductResult.postValue(product);
        });
    }

    public void clearScannedResult() {
        scannedProductResult.setValue(null);
    }

    public void saveProduct(Product product, long userId) {
        isLoading.setValue(true);
        ProductRepository.OperationCallback callback = (success, message) -> {
            isLoading.postValue(false);
            operationSuccess.postValue(success);
            operationMessage.postValue(message);
        };
        
        if (product.getId() == 0) {
            repository.createProduct(product, userId, callback);
        } else {
            repository.updateProduct(product, userId, callback);
        }
    }

    /** Resets the success flag so it is not re-delivered on fragment re-entry. */
    public void resetOperationSuccess() {
        operationSuccess.setValue(null);
        operationMessage.setValue(null);
    }

    public void deleteProduct(Product product, long adminUserId) {
        isLoading.setValue(true);
        repository.deleteProduct(product, adminUserId, (success, message) -> {
            isLoading.postValue(false);
            operationSuccess.postValue(success);
            operationMessage.postValue(message);
        });
    }

    /** Obtiene productos activos por proveedor (para órdenes de compra) */
    public LiveData<java.util.List<Product>> getActiveProductsBySupplier(long supplierId) {
        return repository.getActiveProductsBySupplier(supplierId);
    }

    /** Obtiene productos activos por proveedor mediante un callback (evita problemas de ciclo de vida con LiveData en dialogos) */
    public void fetchActiveProductsBySupplier(long supplierId, ProductRepository.GetProductsCallback callback) {
        repository.getActiveProductsBySupplierSync(supplierId, products -> {
            new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> callback.onResult(products));
        });
    }

    /** Obtiene todos los productos activos (lista completa) */
    public LiveData<java.util.List<Product>> getAllActiveProducts() {
        return repository.getActiveProducts();
    }
}
