package com.swiftpay.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import androidx.room.Transaction;
import com.swiftpay.data.entity.PurchaseOrderItem;
import com.swiftpay.data.entity.PurchaseOrderItemWithProduct;

import java.util.List;

@Dao
public interface PurchaseOrderItemDao {
    @Insert
    void insert(PurchaseOrderItem item);

    @Insert
    void insertAll(List<PurchaseOrderItem> items);

    @Delete
    void delete(PurchaseOrderItem item);

    @Query("DELETE FROM purchase_order_items WHERE order_id = :orderId")
    void deleteByOrderId(long orderId);

    @Query("SELECT * FROM purchase_order_items WHERE order_id = :orderId")
    LiveData<List<PurchaseOrderItem>> getItemsForOrder(long orderId);

    @Query("SELECT * FROM purchase_order_items WHERE order_id = :orderId")
    List<PurchaseOrderItem> getItemsForOrderSync(long orderId);

    @Transaction
    @Query("SELECT * FROM purchase_order_items WHERE order_id = :orderId")
    LiveData<List<PurchaseOrderItemWithProduct>> getItemsWithProductForOrder(long orderId);
}
