// com/swiftpay/data/entity/SaleStatusHistory.java
package com.swiftpay.data.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * Entidad Room que registra cada cambio de estado de una venta para auditoría.
 */
@Entity(tableName = "sale_status_history",
        foreignKeys = {
                @ForeignKey(entity = Sale.class, parentColumns = "id", childColumns = "sale_id", onDelete = ForeignKey.CASCADE),
                @ForeignKey(entity = User.class, parentColumns = "id", childColumns = "changed_by", onDelete = ForeignKey.RESTRICT)
        },
        indices = {
                @Index(value = "sale_id"),
                @Index(value = "changed_by")
        })
public class SaleStatusHistory {

    @PrimaryKey(autoGenerate = true)
    private long id;

    @ColumnInfo(name = "sale_id")
    private long saleId;

    @ColumnInfo(name = "previous_status")
    private String previousStatus;

    @ColumnInfo(name = "new_status")
    private String newStatus;

    @ColumnInfo(name = "changed_by")
    private long changedBy;

    @ColumnInfo(name = "changed_at")
    private long changedAt;

    public SaleStatusHistory() {}

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public long getSaleId() { return saleId; }
    public void setSaleId(long saleId) { this.saleId = saleId; }
    public String getPreviousStatus() { return previousStatus; }
    public void setPreviousStatus(String previousStatus) { this.previousStatus = previousStatus; }
    public String getNewStatus() { return newStatus; }
    public void setNewStatus(String newStatus) { this.newStatus = newStatus; }
    public long getChangedBy() { return changedBy; }
    public void setChangedBy(long changedBy) { this.changedBy = changedBy; }
    public long getChangedAt() { return changedAt; }
    public void setChangedAt(long changedAt) { this.changedAt = changedAt; }
}
