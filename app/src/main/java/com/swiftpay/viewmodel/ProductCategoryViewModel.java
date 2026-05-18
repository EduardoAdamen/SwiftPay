package com.swiftpay.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.swiftpay.data.entity.ProductCategory;
import com.swiftpay.data.repository.ProductCategoryRepository;
import java.util.List;

/**
 * ViewModel para la gestión de Categorías de Productos.
 */
public class ProductCategoryViewModel extends AndroidViewModel {

    private final ProductCategoryRepository repository;
    private final MutableLiveData<String> operationMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> operationSuccess = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);

    public ProductCategoryViewModel(@NonNull Application application) {
        super(application);
        this.repository = new ProductCategoryRepository(application);
    }

    public LiveData<String> getOperationMessage() { return operationMessage; }
    public LiveData<Boolean> getOperationSuccess() { return operationSuccess; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }

    public LiveData<List<ProductCategory>> getAllCategories() {
        return repository.getAllCategories();
    }

    public LiveData<ProductCategory> getCategoryById(long categoryId) {
        return repository.getCategoryById(categoryId);
    }

    public void saveCategory(ProductCategory category, long adminUserId) {
        isLoading.setValue(true);
        repository.saveCategory(category, adminUserId, (success, message) -> {
            isLoading.postValue(false);
            operationSuccess.postValue(success);
            operationMessage.postValue(message);
        });
    }

    public void deleteCategory(ProductCategory category, long adminUserId) {
        isLoading.setValue(true);
        repository.deleteCategory(category, adminUserId, (success, message) -> {
            isLoading.postValue(false);
            operationSuccess.postValue(success);
            operationMessage.postValue(message);
        });
    }
}
