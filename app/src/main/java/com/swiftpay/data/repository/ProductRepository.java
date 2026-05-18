package com.swiftpay.data.repository;

import android.content.Context;
import androidx.lifecycle.LiveData;
import androidx.paging.Pager;
import androidx.paging.PagingConfig;
import androidx.paging.PagingData;
import com.swiftpay.data.db.SwiftPayDatabase;
import com.swiftpay.data.entity.Product;
import com.swiftpay.util.AuditLogger;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import androidx.paging.PagingLiveData;

/**
 * Repository para la gestión del catálogo de Productos.
 * RF 4.1 - 4.10: CRUD de productos, paginación, escáner, imágenes y bloqueo optimista.
 */
public class ProductRepository {

    private final SwiftPayDatabase db;
    private final Context context;
    private final ExecutorService executor;

    public ProductRepository(Context context) {
        this.context = context.getApplicationContext();
        this.db = SwiftPayDatabase.getInstance(this.context);
        this.executor = Executors.newSingleThreadExecutor();
    }

    /**
     * Obtiene el catálogo completo paginado.
     */
    public LiveData<PagingData<Product>> getAllProductsPaged() {
        return PagingLiveData.getLiveData(new Pager<>(
                new PagingConfig(20, 5, false, 60),
                () -> db.productDao().getAllPaged()
        ));
    }

    /**
     * Obtiene solo productos activos paginados (para Vendedor).
     */
    public LiveData<PagingData<Product>> getActiveProductsPaged() {
        return PagingLiveData.getLiveData(new Pager<>(
                new PagingConfig(20, 5, false, 60),
                () -> db.productDao().getAllActivePaged()
        ));
    }

    /**
     * Obtiene un producto por ID reactivamente.
     */
    public LiveData<Product> getProductById(long id) {
        return db.productDao().getById(id);
    }

    /**
     * Busca un producto por código SKU.
     */
    public void getProductBySku(String sku, GetProductCallback callback) {
        executor.execute(() -> {
            try {
                Product product = db.productDao().getBySku(sku);
                callback.onResult(product);
            } catch (Exception e) {
                callback.onResult(null);
            }
        });
    }

    /**
     * Crea un nuevo producto.
     */
    public void createProduct(Product product, long userId, OperationCallback callback) {
        executor.execute(() -> {
            try {
                long now = System.currentTimeMillis();
                product.setCreatedAt(now);
                product.setUpdatedAt(now);
                product.setVersion(1); // Inicio bloqueo optimista
                
                long newId = db.productDao().insert(product);
                AuditLogger.log(context, userId, "CREATE_PRODUCT", "PRODUCT", newId, 
                        "Producto creado: " + product.getSku());
                
                callback.onResult(true, "Producto creado exitosamente");
            } catch (Exception e) {
                if (e.getMessage() != null && e.getMessage().contains("UNIQUE")) {
                    callback.onResult(false, "El SKU ya existe");
                } else {
                    callback.onResult(false, "Error: " + e.getMessage());
                }
            }
        });
    }

    /**
     * Actualiza un producto usando bloqueo optimista.
     * RF 4.10: Bloqueo optimista si 2 usuarios editan el mismo producto.
     */
    public void updateProduct(Product product, long userId, OperationCallback callback) {
        executor.execute(() -> {
            try {
                long now = System.currentTimeMillis();
                
                int rowsAffected = db.productDao().updateOptimistic(
                        product.getId(),
                        product.getSku(),
                        product.getName(),
                        product.getPrice(),
                        product.getStock(),
                        product.getCategoryId(),
                        product.getBrandId(),
                        product.getImagePath(),
                        product.getIsActive(),
                        now,
                        product.getVersion() // expectedVersion
                );
                
                if (rowsAffected == 0) {
                    callback.onResult(false, "OPTIMISTIC_LOCK_ERROR");
                    return;
                }
                
                AuditLogger.log(context, userId, "UPDATE_PRODUCT", "PRODUCT", product.getId(), 
                        "Producto actualizado: " + product.getSku());
                
                callback.onResult(true, "Producto guardado exitosamente");
            } catch (Exception e) {
                callback.onResult(false, "Error: " + e.getMessage());
            }
        });
    }

    /**
     * Elimina un producto validando que no tenga ventas.
     */
    public void deleteProduct(Product product, long adminUserId, OperationCallback callback) {
        executor.execute(() -> {
            try {
                int saleCount = db.productDao().getSaleCountByProduct(product.getId());
                if (saleCount > 0) {
                    callback.onResult(false, "No se puede eliminar el producto porque tiene historial de ventas.");
                    return;
                }
                
                db.productDao().delete(product);
                AuditLogger.log(context, adminUserId, "DELETE_PRODUCT", "PRODUCT", product.getId(), 
                        "Producto eliminado: " + product.getSku());
                
                callback.onResult(true, "Producto eliminado exitosamente");
            } catch (Exception e) {
                callback.onResult(false, "Error al eliminar: " + e.getMessage());
            }
        });
    }

    public interface OperationCallback {
        void onResult(boolean success, String message);
    }
    
    public interface GetProductCallback {
        void onResult(Product product);
    }
}
