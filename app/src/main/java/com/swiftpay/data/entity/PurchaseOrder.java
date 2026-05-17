// com/swiftpay/data/entity/PurchaseOrder.java
package com.swiftpay.data.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * Entidad Room que representa una orden de compra a un proveedor.
 * Estados posibles: PENDIENTE, RECIBIDA, CANCELADA.
 */
@Entity(tableName = "purchase_orders",
        foreignKeys = @ForeignKey(
                entity = Supplier.class,
                parentColumns = "id",
                childColumns = "supplier_id",
                onDelete = ForeignKey.RESTRICT
        ),
        indices = @Index(value = "supplier_id"))
public class PurchaseOrder {

    @PrimaryKey(autoGenerate = true)
    private long id;

    @ColumnInfo(name = "supplier_id")
    private long supplierId;

    @ColumnInfo(name = "total", defaultValue = "0")
    private double total;

    @ColumnInfo(name = "status", defaultValue = "'PENDIENTE'")
    private String status;

    @ColumnInfo(name = "created_at")
    private long createdAt;

    @ColumnInfo(name = "received_at")
    private Long receivedAt;

    public PurchaseOrder() {}

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public long getSupplierId() { return supplierId; }
    public void setSupplierId(long supplierId) { this.supplierId = supplierId; }
    public double getTotal() { return total; }
    public void setTotal(double total) { this.total = total; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
    public Long getReceivedAt() { return receivedAt; }
    public void setReceivedAt(Long receivedAt) { this.receivedAt = receivedAt; }
}
