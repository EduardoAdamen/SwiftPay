package com.swiftpay.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.swiftpay.data.entity.Supplier;
import com.swiftpay.data.repository.SupplierRepository;

import java.util.List;

public class SupplierViewModel extends AndroidViewModel {
    private SupplierRepository repository;
    private LiveData<List<Supplier>> allSuppliers;
    private MutableLiveData<String> errorDesc;

    public SupplierViewModel(@NonNull Application application) {
        super(application);
        repository = new SupplierRepository(application);
        allSuppliers = repository.getAllSuppliers();
        errorDesc = new MutableLiveData<>();
    }

    public LiveData<List<Supplier>> getAllSuppliers() {
        return allSuppliers;
    }

    public LiveData<Supplier> getSupplier(long id) {
        return repository.getSupplierById(id);
    }

    public void insert(Supplier supplier) {
        repository.insert(supplier);
    }

    public void update(Supplier supplier) {
        repository.update(supplier);
    }

    public LiveData<String> getError() {
        return errorDesc;
    }
}
