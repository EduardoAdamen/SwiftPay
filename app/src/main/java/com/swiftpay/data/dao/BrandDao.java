package com.swiftpay.data.dao;

import androidx.lifecycle.LiveData;
import androidx.paging.PagingSource;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import com.swiftpay.data.entity.Brand;
import java.util.List;

@Dao
public interface BrandDao {
    @Insert
    long insert(Brand brand);
    @Update
    void update(Brand brand);
    @Delete
    void delete(Brand brand);
    @Query("SELECT * FROM brands WHERE id = :id")
    LiveData<Brand> getById(long id);
    @Query("SELECT * FROM brands WHERE id = :id")
    Brand getByIdSync(long id);
    @Query("SELECT * FROM brands ORDER BY name ASC")
    LiveData<List<Brand>> getAll();
    @Query("SELECT * FROM brands ORDER BY name ASC")
    PagingSource<Integer, Brand> getAllPaged();
    @Query("SELECT COUNT(*) FROM products WHERE brand_id = :brandId")
    int getProductCountByBrand(long brandId);
}
