package com.swiftpay.data.repository;

import android.content.Context;
import androidx.lifecycle.LiveData;
import androidx.paging.Pager;
import androidx.paging.PagingConfig;
import androidx.paging.PagingData;
import androidx.paging.PagingLiveData;
import com.swiftpay.data.db.SwiftPayDatabase;
import com.swiftpay.data.entity.DiscountCode;
import com.swiftpay.util.AuditLogger;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Repository para la gestión de Códigos de Descuento.
 * RF 6.1 - 6.5: CRUD, validación de vigencia.
 */
public class DiscountRepository {

    private final SwiftPayDatabase db;
    private final Context context;
    private final ExecutorService executor;

    public DiscountRepository(Context context) {
        this.context = context.getApplicationContext();
        this.db = SwiftPayDatabase.getInstance(this.context);
        this.executor = Executors.newSingleThreadExecutor();
    }

    public LiveData<PagingData<DiscountCode>> getAllDiscountsPaged() {
        return PagingLiveData.getLiveData(new Pager<>(
                new PagingConfig(20, 5, false, 60),
                () -> db.discountCodeDao().getAllPaged()
        ));
    }

    public LiveData<DiscountCode> getDiscountById(long id) {
        return db.discountCodeDao().getById(id);
    }

    public interface ValidationCallback {
        void onResult(DiscountCode code, String error);
    }

    /**
     * Valida si un código de descuento es válido (existe, activo y no expirado).
     */
    public void validateDiscountCode(String code, ValidationCallback callback) {
        executor.execute(() -> {
            DiscountCode discount = db.discountCodeDao().getByCode(code);
            if (discount == null) {
                callback.onResult(null, "Código no encontrado");
            } else if (discount.getIsActive() == 0) {
                callback.onResult(null, "El código está inactivo");
            } else if (discount.getExpirationDate() < System.currentTimeMillis()) {
                callback.onResult(null, "El código ha expirado");
            } else {
                callback.onResult(discount, null);
            }
        });
    }

    public interface OperationCallback {
        void onResult(boolean success, String message);
    }

    public void saveDiscount(DiscountCode discount, long adminUserId, OperationCallback callback) {
        executor.execute(() -> {
            try {
                // Check unique code
                DiscountCode existing = db.discountCodeDao().getByCode(discount.getCode());
                if (existing != null && existing.getId() != discount.getId()) {
                    callback.onResult(false, "El código de descuento ya existe");
                    return;
                }

                long now = System.currentTimeMillis();
                if (discount.getId() == 0) {
                    discount.setCreatedAt(now);
                    discount.setUpdatedAt(now);
                    long newId = db.discountCodeDao().insert(discount);
                    AuditLogger.log(context, adminUserId, "CREATE_DISCOUNT", "DISCOUNT_CODE", newId, "Descuento creado: " + discount.getCode());
                } else {
                    discount.setUpdatedAt(now);
                    db.discountCodeDao().update(discount);
                    AuditLogger.log(context, adminUserId, "UPDATE_DISCOUNT", "DISCOUNT_CODE", discount.getId(), "Descuento actualizado: " + discount.getCode());
                }
                callback.onResult(true, "Descuento guardado exitosamente");
            } catch (Exception e) {
                callback.onResult(false, "Error al guardar: " + e.getMessage());
            }
        });
    }
}
