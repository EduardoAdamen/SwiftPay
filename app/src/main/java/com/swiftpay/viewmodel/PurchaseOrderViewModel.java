package com.swiftpay.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.swiftpay.data.entity.OrderItemDraft;
import com.swiftpay.data.entity.PurchaseOrder;
import com.swiftpay.data.entity.PurchaseOrderItem;
import com.swiftpay.data.repository.PurchaseOrderRepository;

import java.util.ArrayList;
import java.util.List;

public class PurchaseOrderViewModel extends AndroidViewModel {
    private PurchaseOrderRepository repository;
    private LiveData<List<PurchaseOrder>> allOrdersDesc;
    
    private MutableLiveData<List<OrderItemDraft>> draftItems = new MutableLiveData<>(new ArrayList<>());
    private MutableLiveData<String> operationStatus = new MutableLiveData<>();

    public PurchaseOrderViewModel(@NonNull Application application) {
        super(application);
        repository = new PurchaseOrderRepository(application);
        allOrdersDesc = repository.getAllOrdersDesc();
    }

    public LiveData<List<PurchaseOrder>> getAllOrdersDesc() {
        return allOrdersDesc;
    }

    public LiveData<PurchaseOrder> getOrderById(long id) {
        return repository.getOrderById(id);
    }

    public LiveData<List<PurchaseOrderItem>> getOrderItems(long orderId) {
        return repository.getOrderItems(orderId);
    }

    public LiveData<List<OrderItemDraft>> getDraftItems() {
        return draftItems;
    }

    public void addDraftItem(OrderItemDraft item) {
        List<OrderItemDraft> current = draftItems.getValue();
        if (current == null) current = new ArrayList<>();
        
        boolean exists = false;
        for (OrderItemDraft d : current) {
            if (d.getProductId() == item.getProductId()) {
                d.setQuantity(d.getQuantity() + item.getQuantity());
                d.setUnitCost(item.getUnitCost()); 
                exists = true;
                break;
            }
        }
        if (!exists) {
            current.add(item);
        }
        draftItems.setValue(current);
    }
    
    public void removeDraftItem(long productId) {
        List<OrderItemDraft> current = draftItems.getValue();
        if (current != null) {
            current.removeIf(d -> d.getProductId() == productId);
            draftItems.setValue(current);
        }
    }
    
    public void clearDraftItems() {
        draftItems.setValue(new ArrayList<>());
    }

    public void initDraftFromExisting(List<PurchaseOrderItem> items, List<String> productNames) {
        List<OrderItemDraft> drafts = new ArrayList<>();
        for (int i = 0; i < items.size(); i++) {
            PurchaseOrderItem poi = items.get(i);
            String pName = (productNames != null && i < productNames.size()) ? productNames.get(i) : "Producto " + poi.getProductId();
            drafts.add(new OrderItemDraft(poi.getProductId(), pName, poi.getQuantity(), poi.getUnitCost()));
        }
        draftItems.setValue(drafts);
    }

    private double calculateTotal() {
        double total = 0.0;
        List<OrderItemDraft> current = draftItems.getValue();
        if (current != null) {
            for (OrderItemDraft d : current) total += (d.getQuantity() * d.getUnitCost());
        }
        return total;
    }

    public void createOrder(long supplierId) {
        List<OrderItemDraft> current = draftItems.getValue();
        if (current == null || current.isEmpty()) {
            operationStatus.setValue("ERROR: No hay items en la orden");
            return;
        }

        PurchaseOrder order = new PurchaseOrder();
        order.setSupplierId(supplierId);
        order.setTotal(calculateTotal());
        order.setStatus("PENDIENTE");
        order.setCreatedAt(System.currentTimeMillis());

        List<PurchaseOrderItem> items = new ArrayList<>();
        for (OrderItemDraft draft : current) {
            PurchaseOrderItem poi = new PurchaseOrderItem();
            poi.setProductId(draft.getProductId());
            poi.setQuantity(draft.getQuantity());
            poi.setUnitCost(draft.getUnitCost());
            items.add(poi);
        }

        repository.createOrderWithItems(order, items, new PurchaseOrderRepository.OnOperationCompleteListener() {
            @Override
            public void onSuccess() {
                operationStatus.postValue("SUCCESS");
                clearDraftItems();
            }

            @Override
            public void onError(String error) {
                operationStatus.postValue("ERROR: " + error);
            }
        });
    }

    public void updatePendingOrder(PurchaseOrder order) {
        List<OrderItemDraft> current = draftItems.getValue();
        if (current == null || current.isEmpty()) {
            operationStatus.setValue("ERROR: No hay items en la orden");
            return;
        }

        order.setTotal(calculateTotal());

        List<PurchaseOrderItem> items = new ArrayList<>();
        for (OrderItemDraft draft : current) {
            PurchaseOrderItem poi = new PurchaseOrderItem();
            poi.setProductId(draft.getProductId());
            poi.setQuantity(draft.getQuantity());
            poi.setUnitCost(draft.getUnitCost());
            items.add(poi);
        }

        repository.updateOrderPending(order, items, new PurchaseOrderRepository.OnOperationCompleteListener() {
            @Override
            public void onSuccess() {
                operationStatus.postValue("SUCCESS_UPDATE");
                clearDraftItems();
            }

            @Override
            public void onError(String error) {
                operationStatus.postValue("ERROR: " + error);
            }
        });
    }

    public void receiveOrder(long orderId) {
        repository.receiveOrder(orderId, new PurchaseOrderRepository.OnOperationCompleteListener() {
            @Override
            public void onSuccess() {
                operationStatus.postValue("SUCCESS_RECEIVE");
            }
            @Override
            public void onError(String error) {
                operationStatus.postValue("ERROR: " + error);
            }
        });
    }
    
    public void cancelOrder(long orderId) {
        repository.cancelOrder(orderId, new PurchaseOrderRepository.OnOperationCompleteListener() {
            @Override
            public void onSuccess() {
                operationStatus.postValue("SUCCESS_CANCEL");
            }
            @Override
            public void onError(String error) {
                operationStatus.postValue("ERROR: " + error);
            }
        });
    }

    public LiveData<String> getOperationStatus() {
        return operationStatus;
    }
    
    public void resetStatus() {
        operationStatus.setValue(null);
    }
}
