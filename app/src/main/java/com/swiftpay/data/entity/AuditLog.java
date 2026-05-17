// com/swiftpay/data/entity/AuditLog.java
package com.swiftpay.data.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "audit_log",
        foreignKeys = @ForeignKey(entity = User.class, parentColumns = "id", childColumns = "user_id", onDelete = ForeignKey.SET_NULL),
        indices = @Index(value = "user_id"))
public class AuditLog {
    @PrimaryKey(autoGenerate = true)
    private long id;
    @ColumnInfo(name = "user_id")
    private Long userId;
    @ColumnInfo(name = "action")
    private String action;
    @ColumnInfo(name = "entity_type")
    private String entityType;
    @ColumnInfo(name = "entity_id")
    private Long entityId;
    @ColumnInfo(name = "details")
    private String details;
    @ColumnInfo(name = "created_at")
    private long createdAt;

    public AuditLog() {}
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    public String getEntityType() { return entityType; }
    public void setEntityType(String entityType) { this.entityType = entityType; }
    public Long getEntityId() { return entityId; }
    public void setEntityId(Long entityId) { this.entityId = entityId; }
    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }
    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
}
