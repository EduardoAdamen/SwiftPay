package com.swiftpay.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import com.swiftpay.data.entity.User;
import java.util.List;

@Dao
public interface UserDao {
    @Insert
    long insert(User user);
    @Update
    void update(User user);
    @Delete
    void delete(User user);
    @Query("SELECT * FROM users WHERE id = :id")
    LiveData<User> getById(long id);
    @Query("SELECT * FROM users WHERE id = :id")
    User getByIdSync(long id);
    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    User getByUsername(String username);
    @Query("SELECT * FROM users ORDER BY full_name ASC")
    LiveData<List<User>> getAll();
    @Query("SELECT * FROM users WHERE is_active = 1 ORDER BY full_name ASC")
    LiveData<List<User>> getAllActive();
    @Query("SELECT COUNT(*) FROM users")
    int getCount();
}
