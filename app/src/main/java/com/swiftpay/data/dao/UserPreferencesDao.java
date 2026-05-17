package com.swiftpay.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import com.swiftpay.data.entity.UserPreferences;

@Dao
public interface UserPreferencesDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(UserPreferences preferences);
    @Update
    void update(UserPreferences preferences);
    @Query("SELECT * FROM user_preferences WHERE user_id = :userId")
    LiveData<UserPreferences> getByUserId(long userId);
    @Query("SELECT * FROM user_preferences WHERE user_id = :userId")
    UserPreferences getByUserIdSync(long userId);
}
