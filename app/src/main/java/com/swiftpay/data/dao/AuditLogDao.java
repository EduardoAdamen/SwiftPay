package com.swiftpay.data.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import com.swiftpay.data.entity.AuditLog;
import java.util.List;

@Dao
public interface AuditLogDao {
    @Insert
    long insert(AuditLog log);
    @Query("SELECT * FROM audit_log ORDER BY created_at DESC LIMIT :limit")
    List<AuditLog> getRecent(int limit);
}
