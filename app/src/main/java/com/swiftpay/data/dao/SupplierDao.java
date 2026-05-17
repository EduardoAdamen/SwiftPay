package com.swiftpay.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import com.swiftpay.data.entity.Supplier;
import java.util.List;

@Dao
public interface SupplierDao {
    @Insert
    long insert(Supplier supplier);
    @Update
    void update(Supplier supplier);
    @Delete
    void delete(Supplier supplier);
    @Query("SELECT * FROM suppliers WHERE id = :id")
    LiveData<Supplier> getById(long id);
    @Query("SELECT * FROM suppliers WHERE id = :id")
    Supplier getByIdSync(long id);
    @Query("SELECT * FROM suppliers ORDER BY name ASC")
    LiveData<List<Supplier>> getAll();
}
