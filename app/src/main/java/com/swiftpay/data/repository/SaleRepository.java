// app/src/main/java/com/swiftpay/data/repository/SaleRepository.java
package com.swiftpay.data.repository;

import android.content.Context;
import androidx.lifecycle.LiveData;
import androidx.paging.Pager;
import androidx.paging.PagingConfig;
import androidx.paging.PagingData;
import androidx.paging.PagingLiveData;
import androidx.room.Transaction;
import androidx.sqlite.db.SimpleSQLiteQuery;
import com.swiftpay.data.db.SwiftPayDatabase;
import com.swiftpay.data.entity.Sale;
import com.swiftpay.data.entity.SaleItem;
import com.swiftpay.data.entity.SaleStatusHistory;
import com.swiftpay.data.entity.SystemEvent;
import com.swiftpay.util.AuditLogger;
import com.swiftpay.util.Constants;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SaleRepository {

    private final SwiftPayDatabase db;
    private final Context context;
    private final ExecutorService executor;

    public SaleRepository(Context context) {
        this.context = context.getApplicationContext();
        this.db = SwiftPayDatabase.getInstance(this.context);
        this.executor = Executors.newSingleThreadExecutor();
    }

    public LiveData<PagingData<Sale>> getFilteredSales(String queryStr, String status) {
        StringBuilder queryBuilder = new StringBuilder("SELECT * FROM sales WHERE 1=1");
        List<Object> args = new ArrayList<>();

        if (status != null && !status.isEmpty()) {
            queryBuilder.append(" AND status = ?");
            args.add(status);
        }

        if (queryStr != null && !queryStr.trim().isEmpty()) {
            queryBuilder.append(" AND id = ?");
            try {
                args.add(Long.parseLong(queryStr.trim()));
            } catch (NumberFormatException e) {
                args.add(-1L); // No match
            }
        }

        queryBuilder.append(" ORDER BY created_at DESC");
        SimpleSQLiteQuery sqliteQuery = new SimpleSQLiteQuery(queryBuilder.toString(), args.toArray());

        return PagingLiveData.getLiveData(new Pager<>(
                new PagingConfig(20, 5, false, 60),
                () -> db.saleDao().getFilteredSales(sqliteQuery)
        ));
    }

    public LiveData<Sale> getSaleById(long id) {
        return db.saleDao().getById(id);
    }

    public LiveData<List<com.swiftpay.data.entity.SaleItemWithProduct>> getSaleItems(long saleId) {
        return db.saleItemDao().getSaleItemsWithProduct(saleId);
    }

    public LiveData<List<SaleStatusHistory>> getSaleStatusHistory(long saleId) {
        return db.saleStatusHistoryDao().getBySaleId(saleId);
    }

    public interface CreateSaleCallback {
        void onResult(boolean success, String message, Long saleId);
    }

    /**
     * Crea una venta y descuenta el stock atómicamente.
     */
    public void createSale(Sale sale, List<SaleItem> items, CreateSaleCallback callback) {
        executor.execute(() -> {
            Long saleIdResult = null;
            String errorMessage = null;
            try {
                saleIdResult = db.runInTransaction(() -> {
                    long now = System.currentTimeMillis();
                    sale.setCreatedAt(now);
                    sale.setUpdatedAt(now);

                    resolveCashRegisterId(sale);

                    long saleId = db.saleDao().insert(sale);

                    for (SaleItem item : items) {
                        item.setSaleId(saleId);
                        int updated = db.productDao().decrementStock(item.getProductId(), item.getQuantity());
                        if (updated == 0) {
                            throw new RuntimeException("Stock insuficiente para el producto ID: " + item.getProductId());
                        }
                    }
                    db.saleItemDao().insertAll(items);

                    SaleStatusHistory history = new SaleStatusHistory();
                    history.setSaleId(saleId);
                    history.setPreviousStatus(null);
                    history.setNewStatus(sale.getStatus());
                    history.setChangedBy(sale.getSellerId());
                    history.setChangedAt(now);
                    db.saleStatusHistoryDao().insert(history);

                    SystemEvent event = new SystemEvent();
                    event.setEventType(Constants.EVENT_NEW_SALE);
                    event.setCreatedAt(now);
                    event.setEntityId(saleId);
                    event.setIsReviewed(0);
                    db.systemEventDao().insert(event);

                    return saleId;
                });
            } catch (Exception e) {
                errorMessage = e.getMessage() != null ? e.getMessage() : "Error al procesar la venta";
            }

            final Long finalSaleId = saleIdResult;
            final String finalError = errorMessage;
            new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
                if (finalError == null && finalSaleId != null) {
                    AuditLogger.log(context, sale.getSellerId(), "CREATE_SALE", "SALE", finalSaleId, "Venta creada");
                    callback.onResult(true, "Venta procesada con éxito", finalSaleId);
                } else {
                    callback.onResult(false, finalError != null ? finalError : "Error al procesar la venta", null);
                }
            });
        });
    }

    /** Asigna caja abierta del vendedor o null si no hay / no es válida (evita FK crash). */
    private void resolveCashRegisterId(Sale sale) {
        Long requestedId = sale.getCashRegisterId();
        if (requestedId != null) {
            com.swiftpay.data.entity.CashRegister register = db.cashRegisterDao().getByIdSync(requestedId);
            if (register == null || register.getClosedAt() != null) {
                sale.setCashRegisterId(null);
            }
            return;
        }
        com.swiftpay.data.entity.CashRegister open = db.cashRegisterDao().getOpenRegisterBySeller(sale.getSellerId());
        if (open != null) {
            sale.setCashRegisterId(open.getId());
        }
    }

    public void updateStatus(long saleId, String newStatus, long changedByUserId, CreateSaleCallback callback) {
        executor.execute(() -> {
            db.runInTransaction(() -> {
                try {
                    Sale sale = db.saleDao().getByIdSync(saleId);
                    if (sale == null) throw new RuntimeException("Venta no encontrada");
                    
                    String oldStatus = sale.getStatus();
                    
                    // Validate transitions
                    boolean valid = false;
                    if (Constants.STATUS_PENDIENTE.equals(oldStatus)) {
                        valid = Constants.STATUS_PAGADA.equals(newStatus) || Constants.STATUS_CANCELADA.equals(newStatus);
                    } else if (Constants.STATUS_PAGADA.equals(oldStatus)) {
                        valid = Constants.STATUS_COMPLETADA.equals(newStatus) || Constants.STATUS_CANCELADA.equals(newStatus);
                    }
                    
                    if (!valid) {
                        throw new RuntimeException("Transición de estado inválida: " + oldStatus + " -> " + newStatus);
                    }

                    sale.setStatus(newStatus);
                    sale.setUpdatedAt(System.currentTimeMillis());
                    db.saleDao().update(sale);

                    SaleStatusHistory history = new SaleStatusHistory();
                    history.setSaleId(saleId);
                    history.setPreviousStatus(oldStatus);
                    history.setNewStatus(newStatus);
                    history.setChangedBy(changedByUserId);
                    history.setChangedAt(System.currentTimeMillis());
                    db.saleStatusHistoryDao().insert(history);

                    AuditLogger.log(context, changedByUserId, "UPDATE_STATUS", "SALE", saleId, oldStatus + "->" + newStatus);

                    new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> 
                        callback.onResult(true, "Estado actualizado", saleId)
                    );
                } catch (Exception e) {
                    new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> 
                        callback.onResult(false, e.getMessage(), null)
                    );
                    throw e;
                }
            });
        });
    }
}
