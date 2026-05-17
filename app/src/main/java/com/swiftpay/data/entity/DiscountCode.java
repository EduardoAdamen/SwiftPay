// com/swiftpay/data/entity/DiscountCode.java
package com.swiftpay.data.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * Entidad Room que representa un código de descuento.
 * Los códigos tienen vigencia basada en fecha de expiración y estado activo/inactivo.
 */
@Entity(tableName = "discount_codes",
        indices = @Index(value = "code", unique = true))
public class DiscountCode {

    @PrimaryKey(autoGenerate = true)
    private long id;

    @ColumnInfo(name = "code")
    private String code;

    @ColumnInfo(name = "discount_percentage")
    private double discountPercentage;

    @ColumnInfo(name = "expiration_date")
    private long expirationDate;

    @ColumnInfo(name = "is_active", defaultValue = "1")
    private int isActive;

    @ColumnInfo(name = "created_at")
    private long createdAt;

    @ColumnInfo(name = "updated_at")
    private long updatedAt;

    public DiscountCode() {}

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public double getDiscountPercentage() { return discountPercentage; }
    public void setDiscountPercentage(double discountPercentage) { this.discountPercentage = discountPercentage; }
    public long getExpirationDate() { return expirationDate; }
    public void setExpirationDate(long expirationDate) { this.expirationDate = expirationDate; }
    public int getIsActive() { return isActive; }
    public void setIsActive(int isActive) { this.isActive = isActive; }
    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
    public long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }
}
