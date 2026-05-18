package com.swiftpay.data.repository;

import android.content.Context;
import androidx.lifecycle.LiveData;
import androidx.paging.Pager;
import androidx.paging.PagingConfig;
import androidx.paging.PagingData;
import androidx.paging.PagingSource;
import com.swiftpay.data.db.SwiftPayDatabase;
import com.swiftpay.data.entity.Brand;
import com.swiftpay.util.AuditLogger;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import androidx.paging.PagingLiveData;

/**
 * Repository para la gestión de Marcas (Brands).
 * RF 5.1: CRUD de marcas con paginación Paging 3.
 */
public class BrandRepository {

    private final SwiftPayDatabase db;
    private final Context context;
    private final ExecutorService executor;

    public BrandRepository(Context context) {
        this.context = context.getApplicationContext();
        this.db = SwiftPayDatabase.getInstance(this.context);
        this.executor = Executors.newSingleThreadExecutor();
    }

    /**
     * Obtiene la lista paginada de marcas usando Paging 3.
     */
    public LiveData<PagingData<Brand>> getAllBrandsPaged() {
        return PagingLiveData.getLiveData(new Pager<>(
                new PagingConfig(20, 5, false, 60),
                () -> db.brandDao().getAllPaged()
        ));
    }

    /**
     * Obtiene una marca específica.
     */
    public LiveData<Brand> getBrandById(long brandId) {
        return db.brandDao().getById(brandId);
    }

    /**
     * Crea o actualiza una marca.
     */
    public void saveBrand(Brand brand, long adminUserId, OperationCallback callback) {
        executor.execute(() -> {
            try {
                long now = System.currentTimeMillis();
                boolean isNew = brand.getId() == 0;
                
                if (isNew) {
                    brand.setCreatedAt(now);
                    brand.setUpdatedAt(now);
                    long newId = db.brandDao().insert(brand);
                    AuditLogger.log(context, adminUserId, "CREATE_BRAND", "BRAND", newId, 
                            "Marca creada: " + brand.getName());
                } else {
                    brand.setUpdatedAt(now);
                    db.brandDao().update(brand);
                    AuditLogger.log(context, adminUserId, "UPDATE_BRAND", "BRAND", brand.getId(), 
                            "Marca actualizada: " + brand.getName());
                }
                
                callback.onResult(true, "Marca guardada exitosamente");
            } catch (Exception e) {
                // Posible error de constraint UNIQUE
                if (e.getMessage() != null && e.getMessage().contains("UNIQUE")) {
                    callback.onResult(false, "El nombre de la marca ya existe");
                } else {
                    callback.onResult(false, "Error al guardar: " + e.getMessage());
                }
            }
        });
    }

    /**
     * Elimina una marca si no tiene productos asociados.
     * RF 5.2: Validación de eliminación.
     */
    public void deleteBrand(Brand brand, long adminUserId, OperationCallback callback) {
        executor.execute(() -> {
            try {
                int productCount = db.brandDao().getProductCountByBrand(brand.getId());
                if (productCount > 0) {
                    callback.onResult(false, "No se puede eliminar la marca porque tiene " + productCount + " producto(s) asociado(s).");
                    return;
                }
                
                db.brandDao().delete(brand);
                AuditLogger.log(context, adminUserId, "DELETE_BRAND", "BRAND", brand.getId(), 
                        "Marca eliminada: " + brand.getName());
                
                callback.onResult(true, "Marca eliminada exitosamente");
            } catch (Exception e) {
                callback.onResult(false, "Error al eliminar: " + e.getMessage());
            }
        });
    }

    public interface OperationCallback {
        void onResult(boolean success, String message);
    }
}
