package com.swiftpay.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.swiftpay.data.entity.CashRegister;
import com.swiftpay.data.repository.CashRegisterRepository;

public class CashRegisterViewModel extends AndroidViewModel {
    private final CashRegisterRepository repository;
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> operationMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> operationSuccess = new MutableLiveData<>();

    public CashRegisterViewModel(@NonNull Application application) {
        super(application);
        this.repository = new CashRegisterRepository(application);
    }

    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<String> getOperationMessage() { return operationMessage; }
    public LiveData<Boolean> getOperationSuccess() { return operationSuccess; }

    public void openCashRegister(long sellerId, double baseAmount) {
        isLoading.setValue(true);
        repository.openCashRegister(sellerId, baseAmount, (success, message, registerId) -> {
            isLoading.setValue(false);
            operationMessage.setValue(message);
            operationSuccess.setValue(success);
        });
    }

    public void closeCashRegister(long registerId, double physicalAmount) {
        isLoading.setValue(true);
        repository.closeCashRegister(registerId, physicalAmount, (success, message, id) -> {
            isLoading.setValue(false);
            operationMessage.setValue(message);
            operationSuccess.setValue(success);
        });
    }

    public LiveData<CashRegister> getCashRegister(long id) {
        return repository.getCashRegister(id);
    }
}
