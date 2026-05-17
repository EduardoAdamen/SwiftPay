// com/swiftpay/data/entity/PurchaseOrderItem.java
package com.swiftpay.data.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * Entidad Room que representa un ítem dentro de una orden de compra.
 */
@Entity(tableName = "purchase_order_items",
        foreignKeys = {
                @ForeignKey(entity = PurchaseOrder.class, parentColumns = "id", childColumns = "order_id", onDelete = ForeignKey.CASCADE),
                @ForeignKey(entity = Product.class, parentColumns = "id", childColumns = "product_id", onDelete = ForeignKey.RESTRICT)
        },
        indices = {
                @Index(value = "order_id"),
                @Index(value = "product_id")
        })
public class PurchaseOrderItem {

    @PrimaryKey(autoGenerate = true)
    private long id;

    @ColumnInfo(name = "order_id")
    private long orderId;

    @ColumnInfo(name = "product_id")
    private long productId;

    @ColumnInfo(name = "quantity")
    private int quantity;

    @ColumnInfo(name = "unit_cost")
    private double unitCost;

    public PurchaseOrderItem() {}

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public long getOrderId() { return orderId; }
    public void setOrderId(long orderId) { this.orderId = orderId; }
    public long getProductId() { return productId; }
    public void setProductId(long productId) { this.productId = productId; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public double getUnitCost() { return unitCost; }
    public void setUnitCost(double unitCost) { this.unitCost = unitCost; }
}
