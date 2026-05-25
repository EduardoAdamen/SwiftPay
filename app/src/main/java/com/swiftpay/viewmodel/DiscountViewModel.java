package com.swiftpay.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.paging.PagingData;
import com.swiftpay.data.entity.DiscountCode;
import com.swiftpay.data.repository.DiscountRepository;

/**
 * ViewModel para Códigos de Descuento.
 */
public class DiscountViewModel extends AndroidViewModel {

    private final DiscountRepository repository;
    private final MutableLiveData<String> operationMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> operationSuccess = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private LiveData<PagingData<DiscountCode>> pagedDiscounts;

    public DiscountViewModel(@NonNull Application application) {
        super(application);
        repository = new DiscountRepository(application);
    }

    public LiveData<String> getOperationMessage() { return operationMessage; }
    public LiveData<Boolean> getOperationSuccess() { return operationSuccess; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }

    public LiveData<PagingData<DiscountCode>> getAllDiscountsPaged() {
        if (pagedDiscounts == null) {
            pagedDiscounts = androidx.paging.PagingLiveData.cachedIn(
                    repository.getAllDiscountsPaged(),
                    androidx.lifecycle.ViewModelKt.getViewModelScope(this)
            );
        }
        return pagedDiscounts;
    }

    public LiveData<DiscountCode> getDiscountById(long id) {
        return repository.getDiscountById(id);
    }

    public void saveDiscount(DiscountCode discount, long adminUserId) {
        isLoading.setValue(true);
        repository.saveDiscount(discount, adminUserId, (success, message) -> {
            isLoading.postValue(false);
            operationSuccess.postValue(success);
            operationMessage.postValue(message);
        });
    }

    public void validateDiscount(String code, DiscountRepository.ValidationCallback callback) {
        isLoading.setValue(true);
        repository.validateDiscountCode(code, (discountCode, error) -> {
            isLoading.postValue(false);
            callback.onResult(discountCode, error);
        });
    }
}
