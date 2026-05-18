package com.swiftpay.data.repository;

import androidx.lifecycle.LiveData;
import com.swiftpay.data.dao.SystemEventDao;
import com.swiftpay.data.db.SwiftPayDatabase;
import com.swiftpay.data.entity.SystemEvent;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SystemEventRepository {
    private final SystemEventDao eventDao;
    private final ExecutorService executorService;

    public SystemEventRepository(SwiftPayDatabase database) {
        this.eventDao = database.systemEventDao();
        this.executorService = Executors.newSingleThreadExecutor();
    }

    public void insert(SystemEvent event) {
        executorService.execute(() -> eventDao.insert(event));
    }

    public LiveData<List<SystemEvent>> getUnreviewedEvents() {
        return eventDao.getUnreviewed();
    }

    public LiveData<Integer> getUnreviewedCountByType(String eventType) {
        return eventDao.getUnreviewedCountByType(eventType);
    }
    
    public LiveData<Integer> getTotalUnreviewedCount() {
        return eventDao.getTotalUnreviewedCount();
    }

    public void markAsReviewed(long eventId) {
        executorService.execute(() -> eventDao.markAsReviewed(eventId, System.currentTimeMillis()));
    }

    public void markAllAsReviewed() {
        executorService.execute(() -> eventDao.markAllAsReviewed(System.currentTimeMillis()));
    }
}
