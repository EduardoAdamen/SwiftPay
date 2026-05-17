package com.swiftpay.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import com.swiftpay.data.entity.SaleStatusHistory;
import java.util.List;

@Dao
public interface SaleStatusHistoryDao {
    @Insert
    long insert(SaleStatusHistory history);
    @Query("SELECT * FROM sale_status_history WHERE sale_id = :saleId ORDER BY changed_at DESC")
    LiveData<List<SaleStatusHistory>> getBySaleId(long saleId);
}
