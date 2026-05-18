package com.swiftpay.data.repository;

import android.content.Context;
import androidx.lifecycle.LiveData;
import com.swiftpay.data.db.SwiftPayDatabase;
import com.swiftpay.data.entity.ProductCategory;
import com.swiftpay.util.AuditLogger;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Repository para categorías de productos.
 */
public class ProductCategoryRepository {

    private final SwiftPayDatabase db;
    private final Context context;
    private final ExecutorService executor;

    public ProductCategoryRepository(Context context) {
        this.context = context.getApplicationContext();
        this.db = SwiftPayDatabase.getInstance(this.context);
        this.executor = Executors.newSingleThreadExecutor();
    }

    public LiveData<List<ProductCategory>> getAllCategories() {
        return db.productCategoryDao().getAll();
    }

    public LiveData<ProductCategory> getCategoryById(long id) {
        return db.productCategoryDao().getById(id);
    }

    public void saveCategory(ProductCategory category, long adminUserId, OperationCallback callback) {
        executor.execute(() -> {
            try {
                long now = System.currentTimeMillis();
                if (category.getId() == 0) {
                    category.setCreatedAt(now);
                    long newId = db.productCategoryDao().insert(category);
                    AuditLogger.log(context, adminUserId, "CREATE_CATEGORY", "PRODUCT_CATEGORY", newId, 
                            "Categoría creada: " + category.getName());
                } else {
                    db.productCategoryDao().update(category);
                    AuditLogger.log(context, adminUserId, "UPDATE_CATEGORY", "PRODUCT_CATEGORY", category.getId(), 
                            "Categoría actualizada: " + category.getName());
                }
                callback.onResult(true, "Categoría guardada exitosamente");
            } catch (Exception e) {
                if (e.getMessage() != null && e.getMessage().contains("UNIQUE")) {
                    callback.onResult(false, "El nombre de la categoría ya existe");
                } else {
                    callback.onResult(false, "Error: " + e.getMessage());
                }
            }
        });
    }

    public void deleteCategory(ProductCategory category, long adminUserId, OperationCallback callback) {
        executor.execute(() -> {
            try {
                int productCount = db.productCategoryDao().getProductCountByCategory(category.getId());
                if (productCount > 0) {
                    callback.onResult(false, "No se puede eliminar la categoría porque tiene " + productCount + " producto(s) asociado(s).");
                    return;
                }
                
                db.productCategoryDao().delete(category);
                AuditLogger.log(context, adminUserId, "DELETE_CATEGORY", "PRODUCT_CATEGORY", category.getId(), 
                        "Categoría eliminada: " + category.getName());
                
                callback.onResult(true, "Categoría eliminada exitosamente");
            } catch (Exception e) {
                callback.onResult(false, "Error al eliminar: " + e.getMessage());
            }
        });
    }

    public interface OperationCallback {
        void onResult(boolean success, String message);
    }
}
