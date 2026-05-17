// com/swiftpay/data/entity/CashRegister.java
package com.swiftpay.data.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * Entidad Room que representa una sesión de caja (apertura/cierre).
 */
@Entity(tableName = "cash_registers",
        foreignKeys = @ForeignKey(
                entity = User.class,
                parentColumns = "id",
                childColumns = "seller_id",
                onDelete = ForeignKey.RESTRICT
        ),
        indices = @Index(value = "seller_id"))
public class CashRegister {

    @PrimaryKey(autoGenerate = true)
    private long id;

    @ColumnInfo(name = "seller_id")
    private long sellerId;

    @ColumnInfo(name = "base_amount")
    private double baseAmount;

    @ColumnInfo(name = "expected_amount")
    private Double expectedAmount;

    @ColumnInfo(name = "physical_amount")
    private Double physicalAmount;

    @ColumnInfo(name = "difference")
    private Double difference;

    @ColumnInfo(name = "opened_at")
    private long openedAt;

    @ColumnInfo(name = "closed_at")
    private Long closedAt;

    public CashRegister() {}

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public long getSellerId() { return sellerId; }
    public void setSellerId(long sellerId) { this.sellerId = sellerId; }
    public double getBaseAmount() { return baseAmount; }
    public void setBaseAmount(double baseAmount) { this.baseAmount = baseAmount; }
    public Double getExpectedAmount() { return expectedAmount; }
    public void setExpectedAmount(Double expectedAmount) { this.expectedAmount = expectedAmount; }
    public Double getPhysicalAmount() { return physicalAmount; }
    public void setPhysicalAmount(Double physicalAmount) { this.physicalAmount = physicalAmount; }
    public Double getDifference() { return difference; }
    public void setDifference(Double difference) { this.difference = difference; }
    public long getOpenedAt() { return openedAt; }
    public void setOpenedAt(long openedAt) { this.openedAt = openedAt; }
    public Long getClosedAt() { return closedAt; }
    public void setClosedAt(Long closedAt) { this.closedAt = closedAt; }
}
