// com/swiftpay/data/entity/SystemEvent.java
package com.swiftpay.data.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Entidad Room que representa un evento del sistema (nuevo cliente, nueva venta, etc.).
 * Los eventos se muestran en el panel de control del administrador.
 */
@Entity(tableName = "system_events")
public class SystemEvent {

    @PrimaryKey(autoGenerate = true)
    private long id;

    @ColumnInfo(name = "event_type")
    private String eventType;

    @ColumnInfo(name = "entity_id")
    private long entityId;

    @ColumnInfo(name = "is_reviewed", defaultValue = "0")
    private int isReviewed;

    @ColumnInfo(name = "reviewed_at")
    private Long reviewedAt;

    @ColumnInfo(name = "created_at")
    private long createdAt;

    public SystemEvent() {}

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }
    public long getEntityId() { return entityId; }
    public void setEntityId(long entityId) { this.entityId = entityId; }
    public int getIsReviewed() { return isReviewed; }
    public void setIsReviewed(int isReviewed) { this.isReviewed = isReviewed; }
    public Long getReviewedAt() { return reviewedAt; }
    public void setReviewedAt(Long reviewedAt) { this.reviewedAt = reviewedAt; }
    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
}
