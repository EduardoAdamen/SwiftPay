package com.swiftpay.data.dao;

import androidx.lifecycle.LiveData;
import androidx.paging.PagingSource;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import com.swiftpay.data.entity.Sale;
import java.util.List;

@Dao
public interface SaleDao {
    @Insert
    long insert(Sale sale);
    @Update
    void update(Sale sale);
    @Query("SELECT * FROM sales WHERE id = :id")
    LiveData<Sale> getById(long id);
    @Query("SELECT * FROM sales WHERE id = :id")
    Sale getByIdSync(long id);
    @Query("SELECT * FROM sales ORDER BY created_at DESC")
    PagingSource<Integer, Sale> getAllPaged();
    @Query("SELECT * FROM sales ORDER BY created_at DESC")
    LiveData<List<Sale>> getAll();
    @Query("SELECT SUM(total) FROM sales WHERE cash_register_id = :cashRegisterId AND payment_method = 'EFECTIVO' AND status IN ('PAGADA','COMPLETADA')")
    Double getCashSalesTotal(long cashRegisterId);

    @androidx.room.RawQuery(observedEntities = Sale.class)
    PagingSource<Integer, Sale> getFilteredSales(androidx.sqlite.db.SupportSQLiteQuery query);
}
