package com.swiftpay.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import com.swiftpay.data.entity.PurchaseOrder;
import java.util.List;

@Dao
public interface PurchaseOrderDao {
    @Insert
    long insert(PurchaseOrder order);
    @Update
    void update(PurchaseOrder order);
    @Query("SELECT * FROM purchase_orders WHERE id = :id")
    LiveData<PurchaseOrder> getById(long id);
    @Query("SELECT * FROM purchase_orders WHERE id = :id")
    PurchaseOrder getByIdSync(long id);
    @Query("SELECT * FROM purchase_orders ORDER BY created_at DESC")
    LiveData<List<PurchaseOrder>> getAllOrderedByDateDesc();
}
