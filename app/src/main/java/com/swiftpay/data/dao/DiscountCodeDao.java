package com.swiftpay.data.dao;

import androidx.lifecycle.LiveData;
import androidx.paging.PagingSource;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import com.swiftpay.data.entity.DiscountCode;
import java.util.List;

@Dao
public interface DiscountCodeDao {
    @Insert
    long insert(DiscountCode code);
    @Update
    void update(DiscountCode code);
    @Query("SELECT * FROM discount_codes WHERE id = :id")
    LiveData<DiscountCode> getById(long id);
    @Query("SELECT * FROM discount_codes WHERE code = :code LIMIT 1")
    DiscountCode getByCode(String code);
    @Query("SELECT * FROM discount_codes ORDER BY created_at DESC")
    PagingSource<Integer, DiscountCode> getAllPaged();
    @Query("SELECT * FROM discount_codes ORDER BY created_at DESC")
    LiveData<List<DiscountCode>> getAll();
}
