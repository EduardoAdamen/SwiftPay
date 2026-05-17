package com.swiftpay.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import com.swiftpay.data.entity.SaleItem;
import java.util.List;

@Dao
public interface SaleItemDao {
    @Insert
    long insert(SaleItem saleItem);
    @Insert
    void insertAll(List<SaleItem> items);
    @Query("SELECT * FROM sale_items WHERE sale_id = :saleId")
    LiveData<List<SaleItem>> getBySaleId(long saleId);
    @Query("SELECT * FROM sale_items WHERE sale_id = :saleId")
    List<SaleItem> getBySaleIdSync(long saleId);
}
