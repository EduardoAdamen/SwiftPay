package com.swiftpay.data.repository;

import android.content.Context;
import androidx.lifecycle.LiveData;
import com.swiftpay.data.db.SwiftPayDatabase;
import com.swiftpay.data.entity.CashRegister;
import com.swiftpay.data.entity.SystemEvent;
import com.swiftpay.util.Constants;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CashRegisterRepository {
    private final SwiftPayDatabase db;
    private final ExecutorService executor;

    public CashRegisterRepository(Context context) {
        this.db = SwiftPayDatabase.getInstance(context.getApplicationContext());
        this.executor = Executors.newSingleThreadExecutor();
    }

    public interface CashActionCallback {
        void onResult(boolean success, String message, Long registerId);
    }

    public void openCashRegister(long sellerId, double baseAmount, CashActionCallback callback) {
        executor.execute(() -> {
            try {
                CashRegister openRegister = db.cashRegisterDao().getOpenRegisterBySeller(sellerId);
                if (openRegister != null) {
                    postCallback(callback, false, "Ya tienes una caja abierta. Ciérrala antes de abrir otra.", null);
                    return;
                }

                CashRegister cr = new CashRegister();
                cr.setSellerId(sellerId);
                cr.setBaseAmount(baseAmount);
                cr.setOpenedAt(System.currentTimeMillis());

                long id = db.cashRegisterDao().insert(cr);
                postCallback(callback, true, "Caja abierta correctamente", id);
            } catch (Exception e) {
                postCallback(callback, false, e.getMessage(), null);
            }
        });
    }

    public void closeCashRegister(long cashRegisterId, double physicalAmount, CashActionCallback callback) {
        executor.execute(() -> {
            db.runInTransaction(() -> {
                try {
                    CashRegister cr = db.cashRegisterDao().getByIdSync(cashRegisterId);
                    if (cr == null || cr.getClosedAt() != null) {
                        throw new RuntimeException("Caja no encontrada o ya está cerrada.");
                    }

                    Double cashSalesTotal = db.saleDao().getCashSalesTotal(cashRegisterId);
                    if (cashSalesTotal == null) cashSalesTotal = 0.0;

                    double expectedAmount = cr.getBaseAmount() + cashSalesTotal;
                    double difference = physicalAmount - expectedAmount;

                    cr.setExpectedAmount(expectedAmount);
                    cr.setPhysicalAmount(physicalAmount);
                    cr.setDifference(difference);
                    cr.setClosedAt(System.currentTimeMillis());

                    db.cashRegisterDao().update(cr);

                    if (difference != 0) {
                        SystemEvent event = new SystemEvent();
                        event.setEventType(Constants.EVENT_CASH_DIFFERENCE);
                        event.setEntityId(cashRegisterId);
                        event.setCreatedAt(System.currentTimeMillis());
                        event.setIsReviewed(0);
                        db.systemEventDao().insert(event);
                    }

                    postCallback(callback, true, "Caja cerrada correctamente", cashRegisterId);
                } catch (Exception e) {
                    postCallback(callback, false, e.getMessage(), null);
                    throw e; // trigger rollback
                }
            });
        });
    }

    public LiveData<CashRegister> getCashRegister(long id) {
        return db.cashRegisterDao().getById(id);
    }

    private void postCallback(CashActionCallback callback, boolean success, String message, Long id) {
        new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
            if (callback != null) callback.onResult(success, message, id);
        });
    }
}
