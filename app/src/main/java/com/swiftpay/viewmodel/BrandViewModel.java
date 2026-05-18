package com.swiftpay.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.paging.PagingData;
import com.swiftpay.data.entity.Brand;
import com.swiftpay.data.repository.BrandRepository;

/**
 * ViewModel para la gestión de Marcas.
 */
public class BrandViewModel extends AndroidViewModel {

    private final BrandRepository repository;
    private final MutableLiveData<String> operationMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> operationSuccess = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private LiveData<PagingData<Brand>> pagedBrands;

    public BrandViewModel(@NonNull Application application) {
        super(application);
        this.repository = new BrandRepository(application);
    }

    public LiveData<String> getOperationMessage() { return operationMessage; }
    public LiveData<Boolean> getOperationSuccess() { return operationSuccess; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }

    public LiveData<PagingData<Brand>> getPagedBrands() {
        if (pagedBrands == null) {
            pagedBrands = repository.getAllBrandsPaged();
        }
        return pagedBrands;
    }

    public LiveData<Brand> getBrandById(long brandId) {
        return repository.getBrandById(brandId);
    }

    public void saveBrand(Brand brand, long adminUserId) {
        isLoading.setValue(true);
        repository.saveBrand(brand, adminUserId, (success, message) -> {
            isLoading.postValue(false);
            operationSuccess.postValue(success);
            operationMessage.postValue(message);
        });
    }

    public void deleteBrand(Brand brand, long adminUserId) {
        isLoading.setValue(true);
        repository.deleteBrand(brand, adminUserId, (success, message) -> {
            isLoading.postValue(false);
            operationSuccess.postValue(success);
            operationMessage.postValue(message);
        });
    }
}
