package com.swiftpay.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import com.swiftpay.data.entity.CashRegister;

@Dao
public interface CashRegisterDao {
    @Insert
    long insert(CashRegister register);
    @Update
    void update(CashRegister register);
    @Query("SELECT * FROM cash_registers WHERE id = :id")
    LiveData<CashRegister> getById(long id);
    @Query("SELECT * FROM cash_registers WHERE id = :id")
    CashRegister getByIdSync(long id);
    @Query("SELECT * FROM cash_registers WHERE seller_id = :sellerId AND closed_at IS NULL LIMIT 1")
    CashRegister getOpenRegisterBySeller(long sellerId);
}
