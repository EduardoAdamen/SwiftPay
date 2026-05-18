package com.swiftpay.data.repository;

import androidx.lifecycle.LiveData;
import com.swiftpay.data.dao.SupplierDao;
import com.swiftpay.data.entity.Supplier;
import com.swiftpay.data.db.SwiftPayDatabase;
import android.app.Application;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SupplierRepository {
    private SupplierDao supplierDao;
    private LiveData<List<Supplier>> allSuppliers;
    private ExecutorService executorService;

    public SupplierRepository(Application application) {
        SwiftPayDatabase database = SwiftPayDatabase.getInstance(application);
        supplierDao = database.supplierDao();
        allSuppliers = supplierDao.getAll();
        executorService = Executors.newFixedThreadPool(2);
    }

    public LiveData<List<Supplier>> getAllSuppliers() {
        return allSuppliers;
    }

    public LiveData<Supplier> getSupplierById(long id) {
        return supplierDao.getById(id);
    }

    public void insert(Supplier supplier) {
        executorService.execute(() -> supplierDao.insert(supplier));
    }

    public void update(Supplier supplier) {
        executorService.execute(() -> supplierDao.update(supplier));
    }
}
