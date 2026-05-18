package com.swiftpay.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import com.swiftpay.data.entity.ClientCategory;
import java.util.List;

@Dao
public interface ClientCategoryDao {
    @Insert
    long insert(ClientCategory category);
    @Update
    void update(ClientCategory category);
    @Delete
    void delete(ClientCategory category);
    @Query("SELECT * FROM client_categories WHERE id = :id")
    LiveData<ClientCategory> getById(long id);
    @Query("SELECT * FROM client_categories ORDER BY name ASC")
    LiveData<List<ClientCategory>> getAll();
    @Query("SELECT COUNT(*) FROM clients WHERE category_id = :categoryId")
    int getClientCountByCategory(long categoryId);
    
    @Query("SELECT * FROM client_categories WHERE id = :id")
    ClientCategory getByIdSync(long id);
}
