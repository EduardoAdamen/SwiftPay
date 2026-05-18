package com.swiftpay.data.repository;

import android.content.Context;
import androidx.lifecycle.LiveData;
import androidx.paging.Pager;
import androidx.paging.PagingConfig;
import androidx.paging.PagingData;
import androidx.paging.PagingLiveData;
import androidx.sqlite.db.SimpleSQLiteQuery;
import com.swiftpay.data.db.SwiftPayDatabase;
import com.swiftpay.data.entity.Sale;
import com.swiftpay.data.entity.SaleItem;
import com.swiftpay.data.entity.SaleStatusHistory;
import com.swiftpay.data.entity.SystemEvent;
import com.swiftpay.util.AuditLogger;
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

    public LiveData<List<SaleItem>> getSaleItems(long saleId) {
        return db.saleItemDao().getBySaleId(saleId);
    }

    public interface CreateSaleCallback {
        void onResult(boolean success, String message, Long saleId);
    }

    /**
     * Crea una venta y descuenta el stock atómicamente.
     */
    public void createSale(Sale sale, List<SaleItem> items, CreateSaleCallback callback) {
        executor.execute(() -> {
            db.runInTransaction(() -> {
                try {
                    long now = System.currentTimeMillis();
                    sale.setCreatedAt(now);
                    sale.setUpdatedAt(now);
                    
                    long saleId = db.saleDao().insert(sale);

                    for (SaleItem item : items) {
                        item.setSaleId(saleId);
                        // Atómico decremento de stock
                        int updated = db.productDao().decrementStock(item.getProductId(), item.getQuantity());
                        if (updated == 0) {
                            throw new RuntimeException("Stock insuficiente para el producto ID: " + item.getProductId());
                        }
                    }
                    db.saleItemDao().insertAll(items);

                    // Insert status history
                    SaleStatusHistory history = new SaleStatusHistory();
                    history.setSaleId(saleId);
                    history.setPreviousStatus(null);
                    history.setNewStatus(sale.getStatus());
                    history.setChangedBy(sale.getSellerId());
                    history.setChangedAt(now);
                    db.saleStatusHistoryDao().insert(history);

                    // Insert system event
                    SystemEvent event = new SystemEvent();
                    event.setEventType("NEW_SALE");
                    event.setCreatedAt(now);
                    event.setEntityId(saleId);
                    event.setIsReviewed(0);
                    db.systemEventDao().insert(event);

                    AuditLogger.log(context, sale.getSellerId(), "CREATE_SALE", "SALE", saleId, "Venta creada");
                    
                    // Callback must be run synchronously inside transaction or posted to main thread
                    new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> 
                        callback.onResult(true, "Venta procesada con éxito", saleId)
                    );
                } catch (Exception e) {
                    new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> 
                        callback.onResult(false, e.getMessage(), null)
                    );
                    throw e; // To trigger rollback
                }
            });
        });
    }
}
