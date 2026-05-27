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
    @Query("UPDATE products SET sku = :sku, name = :name, " +
           "price = :price, stock = :stock, category_id = :categoryId, brand_id = :brandId, " +
           "supplier_id = :supplierId, image_path = :imagePath, is_active = :isActive, updated_at = :updatedAt, " +
           "version = version + 1 WHERE id = :id AND version = :expectedVersion")
    int updateOptimistic(long id, String sku, String name, double price, 
                         int stock, long categoryId, Long brandId, Long supplierId,
                         String imagePath, int isActive, long updatedAt, int expectedVersion);
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
    @Query("SELECT * FROM products WHERE is_active = 1 AND (name LIKE '%' || :query || '%' OR sku LIKE '%' || :query || '%') ORDER BY name ASC")
    PagingSource<Integer, Product> searchActivePaged(String query);
    @Query("SELECT * FROM products ORDER BY name ASC")
    PagingSource<Integer, Product> getAllPaged();
    @Query("SELECT * FROM products WHERE name LIKE '%' || :query || '%' OR sku LIKE '%' || :query || '%' ORDER BY name ASC")
    PagingSource<Integer, Product> searchAllPaged(String query);
    @Query("SELECT * FROM products WHERE is_active = 1 ORDER BY name ASC")
    LiveData<List<Product>> getAllActive();
    @Query("UPDATE products SET stock = stock - :qty WHERE id = :productId AND stock >= :qty")
    int decrementStock(long productId, int qty);
    @Query("UPDATE products SET stock = stock + :qty WHERE id = :productId")
    void incrementStock(long productId, int qty);
    @Query("SELECT COUNT(*) FROM sale_items WHERE product_id = :productId")
    int getSaleCountByProduct(long productId);
    @Query("SELECT * FROM products WHERE supplier_id = :supplierId AND is_active = 1 ORDER BY name ASC")
    LiveData<List<Product>> getActiveBySupplier(long supplierId);
    @Query("SELECT * FROM products WHERE supplier_id = :supplierId AND is_active = 1 ORDER BY name ASC")
    List<Product> getActiveBySupplierSync(long supplierId);
    @Query("SELECT * FROM products WHERE is_active = 1 ORDER BY name ASC")
    List<Product> getAllActiveSync();
}
