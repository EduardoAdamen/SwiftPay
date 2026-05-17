package com.swiftpay.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import com.swiftpay.data.entity.ProductCategory;
import java.util.List;

@Dao
public interface ProductCategoryDao {
    @Insert
    long insert(ProductCategory category);
    @Update
    void update(ProductCategory category);
    @Delete
    void delete(ProductCategory category);
    @Query("SELECT * FROM product_categories WHERE id = :id")
    LiveData<ProductCategory> getById(long id);
    @Query("SELECT * FROM product_categories ORDER BY name ASC")
    LiveData<List<ProductCategory>> getAll();
    @Query("SELECT COUNT(*) FROM products WHERE category_id = :categoryId")
    int getProductCountByCategory(long categoryId);
}
