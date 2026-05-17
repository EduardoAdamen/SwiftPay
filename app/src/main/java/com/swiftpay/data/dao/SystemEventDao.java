package com.swiftpay.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import com.swiftpay.data.entity.SystemEvent;
import java.util.List;

@Dao
public interface SystemEventDao {
    @Insert
    long insert(SystemEvent event);
    @Query("SELECT * FROM system_events WHERE is_reviewed = 0 ORDER BY created_at DESC")
    LiveData<List<SystemEvent>> getUnreviewed();
    @Query("SELECT COUNT(*) FROM system_events WHERE is_reviewed = 0 AND event_type = :eventType")
    LiveData<Integer> getUnreviewedCountByType(String eventType);
    @Query("SELECT COUNT(*) FROM system_events WHERE is_reviewed = 0")
    LiveData<Integer> getTotalUnreviewedCount();
    @Query("UPDATE system_events SET is_reviewed = 1, reviewed_at = :reviewedAt WHERE id = :eventId")
    void markAsReviewed(long eventId, long reviewedAt);
    @Query("UPDATE system_events SET is_reviewed = 1, reviewed_at = :now WHERE is_reviewed = 0")
    void markAllAsReviewed(long now);
}
