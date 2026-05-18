package com.swiftpay.data.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.swiftpay.data.dao.AuditLogDao;
import com.swiftpay.data.dao.ProductDao;
import com.swiftpay.data.dao.PurchaseOrderDao;
import com.swiftpay.data.dao.PurchaseOrderItemDao;
import com.swiftpay.data.db.SwiftPayDatabase;
import com.swiftpay.data.entity.AuditLog;
import com.swiftpay.data.entity.PurchaseOrder;
import com.swiftpay.data.entity.PurchaseOrderItem;
import com.swiftpay.data.preferences.SessionManager;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PurchaseOrderRepository {
    private SwiftPayDatabase db;
    private PurchaseOrderDao purchaseOrderDao;
    private PurchaseOrderItemDao purchaseOrderItemDao;
    private ProductDao productDao;
    private AuditLogDao auditLogDao;
    private SessionManager sessionManager;
    private LiveData<List<PurchaseOrder>> allOrdersDesc;
    private ExecutorService executorService;

    public PurchaseOrderRepository(Application application) {
        db = SwiftPayDatabase.getInstance(application);
        purchaseOrderDao = db.purchaseOrderDao();
        purchaseOrderItemDao = db.purchaseOrderItemDao();
        productDao = db.productDao();
        auditLogDao = db.auditLogDao();
        sessionManager = new SessionManager(application);
        allOrdersDesc = purchaseOrderDao.getAllOrderedByDateDesc();
        executorService = Executors.newFixedThreadPool(2);
    }

    public LiveData<List<PurchaseOrder>> getAllOrdersDesc() {
        return allOrdersDesc;
    }

    public LiveData<PurchaseOrder> getOrderById(long id) {
        return purchaseOrderDao.getById(id);
    }

    public LiveData<List<PurchaseOrderItem>> getOrderItems(long orderId) {
        return purchaseOrderItemDao.getItemsForOrder(orderId);
    }

    public void createOrderWithItems(PurchaseOrder order, List<PurchaseOrderItem> items, OnOperationCompleteListener listener) {
        executorService.execute(() -> {
            try {
                db.runInTransaction(() -> {
                    long orderId = purchaseOrderDao.insert(order);
                    for (PurchaseOrderItem item : items) {
                        item.setOrderId(orderId);
                    }
                    purchaseOrderItemDao.insertAll(items);
                    
                    AuditLog log = new AuditLog();
                    log.setUserId(sessionManager.getUserId());
                    log.setAction("CREATE_PURCHASE_ORDER");
                    log.setEntityType("purchase_orders");
                    log.setEntityId(orderId);
                    log.setCreatedAt(System.currentTimeMillis());
                    auditLogDao.insert(log);
                });
                if (listener != null) listener.onSuccess();
            } catch (Exception e) {
                if (listener != null) listener.onError("Error al crear orden: " + e.getMessage());
            }
        });
    }

    public void updateOrderPending(PurchaseOrder order, List<PurchaseOrderItem> newItems, OnOperationCompleteListener listener) {
        executorService.execute(() -> {
            try {
                db.runInTransaction(() -> {
                    PurchaseOrder existing = purchaseOrderDao.getByIdSync(order.getId());
                    if (existing != null && "PENDIENTE".equals(existing.getStatus())) {
                        purchaseOrderDao.update(order);
                        purchaseOrderItemDao.deleteByOrderId(order.getId());
                        for (PurchaseOrderItem item : newItems) {
                            item.setOrderId(order.getId());
                        }
                        purchaseOrderItemDao.insertAll(newItems);
                        
                        AuditLog log = new AuditLog();
                        log.setUserId(sessionManager.getUserId());
                        log.setAction("UPDATE_PURCHASE_ORDER_PENDING");
                        log.setEntityType("purchase_orders");
                        log.setEntityId(order.getId());
                        log.setCreatedAt(System.currentTimeMillis());
                        auditLogDao.insert(log);
                    } else {
                        throw new IllegalStateException("La orden no existe o no está PENDIENTE.");
                    }
                });
                if (listener != null) listener.onSuccess();
            } catch (Exception e) {
                if (listener != null) listener.onError(e.getMessage());
            }
        });
    }

    public void receiveOrder(long orderId, OnOperationCompleteListener listener) {
        executorService.execute(() -> {
            try {
                db.runInTransaction(() -> {
                    PurchaseOrder order = purchaseOrderDao.getByIdSync(orderId);
                    if (order == null || !"PENDIENTE".equals(order.getStatus())) {
                        throw new IllegalStateException("La orden no existe o ya fue recibida/cancelada.");
                    }

                    List<PurchaseOrderItem> items = purchaseOrderItemDao.getItemsForOrderSync(orderId);
                    
                    for (PurchaseOrderItem item : items) {
                        productDao.incrementStock(item.getProductId(), item.getQuantity());
                    }

                    order.setStatus("RECIBIDA");
                    order.setReceivedAt(System.currentTimeMillis());
                    purchaseOrderDao.update(order);

                    AuditLog log = new AuditLog();
                    log.setUserId(sessionManager.getUserId());
                    log.setAction("RECEIVE_PURCHASE_ORDER");
                    log.setEntityType("purchase_orders");
                    log.setEntityId(orderId);
                    log.setCreatedAt(System.currentTimeMillis());
                    auditLogDao.insert(log);
                });
                if (listener != null) listener.onSuccess();
            } catch (Exception e) {
                if (listener != null) listener.onError(e.getMessage());
            }
        });
    }
    
    public void cancelOrder(long orderId, OnOperationCompleteListener listener) {
        executorService.execute(() -> {
            try {
                db.runInTransaction(() -> {
                    PurchaseOrder order = purchaseOrderDao.getByIdSync(orderId);
                    if (order == null || !"PENDIENTE".equals(order.getStatus())) {
                        throw new IllegalStateException("La orden no existe o ya fue recibida/cancelada.");
                    }

                    order.setStatus("CANCELADA");
                    purchaseOrderDao.update(order);

                    AuditLog log = new AuditLog();
                    log.setUserId(sessionManager.getUserId());
                    log.setAction("CANCEL_PURCHASE_ORDER");
                    log.setEntityType("purchase_orders");
                    log.setEntityId(orderId);
                    log.setCreatedAt(System.currentTimeMillis());
                    auditLogDao.insert(log);
                });
                if (listener != null) listener.onSuccess();
            } catch (Exception e) {
                if (listener != null) listener.onError(e.getMessage());
            }
        });
    }

    public interface OnOperationCompleteListener {
        void onSuccess();
        void onError(String error);
    }
}
