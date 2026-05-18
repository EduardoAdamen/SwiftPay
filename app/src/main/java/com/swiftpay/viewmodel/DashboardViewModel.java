package com.swiftpay.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import com.swiftpay.data.db.SwiftPayDatabase;
import com.swiftpay.data.entity.SystemEvent;
import com.swiftpay.data.repository.SystemEventRepository;
import java.util.List;

public class DashboardViewModel extends AndroidViewModel {
    private final SystemEventRepository repository;
    private final LiveData<Integer> newClientsCount;
    private final LiveData<Integer> newSalesCount;
    private final LiveData<Integer> totalUnreviewedCount;
    private final LiveData<List<SystemEvent>> unreviewedEvents;

    public DashboardViewModel(@NonNull Application application) {
        super(application);
        SwiftPayDatabase database = SwiftPayDatabase.getInstance(application);
        repository = new SystemEventRepository(database);
        newClientsCount = repository.getUnreviewedCountByType("NEW_CLIENT");
        newSalesCount = repository.getUnreviewedCountByType("NEW_SALE");
        totalUnreviewedCount = repository.getTotalUnreviewedCount();
        unreviewedEvents = repository.getUnreviewedEvents();
    }

    public LiveData<Integer> getNewClientsCount() {
        return newClientsCount;
    }

    public LiveData<Integer> getNewSalesCount() {
        return newSalesCount;
    }

    public LiveData<Integer> getTotalUnreviewedCount() {
        return totalUnreviewedCount;
    }

    public LiveData<List<SystemEvent>> getUnreviewedEvents() {
        return unreviewedEvents;
    }

    public void markAsReviewed(long eventId) {
        repository.markAsReviewed(eventId);
    }

    public void markAllAsReviewed() {
        repository.markAllAsReviewed();
    }
}
