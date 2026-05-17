package com.swiftpay.data.dao;

import androidx.lifecycle.LiveData;
import androidx.paging.PagingSource;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import com.swiftpay.data.entity.Product;
import java.util.List;

@Dao
public interface ProductDao {
    @Insert
    long insert(Product product);
    @Update
    void update(Product product);
    @Delete
    void delete(Product product);
    @Query("SELECT * FROM products WHERE id = :id")
    LiveData<Product> getById(long id);
    @Query("SELECT * FROM products WHERE id = :id")
    Product getByIdSync(long id);
    @Query("SELECT * FROM products WHERE sku = :sku LIMIT 1")
    Product getBySku(String sku);
    @Query("SELECT * FROM products WHERE is_active = 1 ORDER BY name ASC")
    PagingSource<Integer, Product> getAllActivePaged();
    @Query("SELECT * FROM products ORDER BY name ASC")
    PagingSource<Integer, Product> getAllPaged();
    @Query("SELECT * FROM products WHERE is_active = 1 ORDER BY name ASC")
    LiveData<List<Product>> getAllActive();
    @Query("UPDATE products SET stock = stock - :qty WHERE id = :productId AND stock >= :qty")
    int decrementStock(long productId, int qty);
    @Query("UPDATE products SET stock = stock + :qty WHERE id = :productId")
    void incrementStock(long productId, int qty);
    @Query("SELECT COUNT(*) FROM sale_items WHERE product_id = :productId")
    int getSaleCountByProduct(long productId);
}
