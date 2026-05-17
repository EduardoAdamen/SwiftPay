// com/swiftpay/data/entity/Sale.java
package com.swiftpay.data.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * Entidad Room que representa una venta en el sistema.
 * Estados posibles: PENDIENTE, PAGADA, COMPLETADA, CANCELADA.
 */
@Entity(tableName = "sales",
        foreignKeys = {
                @ForeignKey(entity = Client.class, parentColumns = "id", childColumns = "client_id", onDelete = ForeignKey.SET_NULL),
                @ForeignKey(entity = User.class, parentColumns = "id", childColumns = "seller_id", onDelete = ForeignKey.RESTRICT),
                @ForeignKey(entity = DiscountCode.class, parentColumns = "id", childColumns = "discount_code_id", onDelete = ForeignKey.SET_NULL),
                @ForeignKey(entity = CashRegister.class, parentColumns = "id", childColumns = "cash_register_id", onDelete = ForeignKey.SET_NULL)
        },
        indices = {
                @Index(value = "client_id"),
                @Index(value = "seller_id"),
                @Index(value = "discount_code_id"),
                @Index(value = "cash_register_id")
        })
public class Sale {

    @PrimaryKey(autoGenerate = true)
    private long id;

    @ColumnInfo(name = "client_id")
    private Long clientId;

    @ColumnInfo(name = "seller_id")
    private long sellerId;

    @ColumnInfo(name = "subtotal")
    private double subtotal;

    @ColumnInfo(name = "discount_percentage", defaultValue = "0")
    private double discountPercentage;

    @ColumnInfo(name = "discount_code_id")
    private Long discountCodeId;

    @ColumnInfo(name = "total")
    private double total;

    @ColumnInfo(name = "payment_method")
    private String paymentMethod;

    @ColumnInfo(name = "amount_received", defaultValue = "0")
    private double amountReceived;

    @ColumnInfo(name = "change_amount", defaultValue = "0")
    private double changeAmount;

    @ColumnInfo(name = "status", defaultValue = "'PENDIENTE'")
    private String status;

    @ColumnInfo(name = "cash_register_id")
    private Long cashRegisterId;

    @ColumnInfo(name = "created_at")
    private long createdAt;

    @ColumnInfo(name = "updated_at")
    private long updatedAt;

    public Sale() {}

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public Long getClientId() { return clientId; }
    public void setClientId(Long clientId) { this.clientId = clientId; }
    public long getSellerId() { return sellerId; }
    public void setSellerId(long sellerId) { this.sellerId = sellerId; }
    public double getSubtotal() { return subtotal; }
    public void setSubtotal(double subtotal) { this.subtotal = subtotal; }
    public double getDiscountPercentage() { return discountPercentage; }
    public void setDiscountPercentage(double discountPercentage) { this.discountPercentage = discountPercentage; }
    public Long getDiscountCodeId() { return discountCodeId; }
    public void setDiscountCodeId(Long discountCodeId) { this.discountCodeId = discountCodeId; }
    public double getTotal() { return total; }
    public void setTotal(double total) { this.total = total; }
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    public double getAmountReceived() { return amountReceived; }
    public void setAmountReceived(double amountReceived) { this.amountReceived = amountReceived; }
    public double getChangeAmount() { return changeAmount; }
    public void setChangeAmount(double changeAmount) { this.changeAmount = changeAmount; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Long getCashRegisterId() { return cashRegisterId; }
    public void setCashRegisterId(Long cashRegisterId) { this.cashRegisterId = cashRegisterId; }
    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
    public long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }
}
