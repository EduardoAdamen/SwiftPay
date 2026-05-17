// com/swiftpay/data/entity/SaleItem.java
package com.swiftpay.data.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * Entidad Room que representa un ítem dentro de una venta.
 * Almacena el precio unitario aplicado y el precio de catálogo para preservar el historial.
 */
@Entity(tableName = "sale_items",
        foreignKeys = {
                @ForeignKey(entity = Sale.class, parentColumns = "id", childColumns = "sale_id", onDelete = ForeignKey.CASCADE),
                @ForeignKey(entity = Product.class, parentColumns = "id", childColumns = "product_id", onDelete = ForeignKey.RESTRICT)
        },
        indices = {
                @Index(value = "sale_id"),
                @Index(value = "product_id")
        })
public class SaleItem {

    @PrimaryKey(autoGenerate = true)
    private long id;

    @ColumnInfo(name = "sale_id")
    private long saleId;

    @ColumnInfo(name = "product_id")
    private long productId;

    @ColumnInfo(name = "quantity")
    private int quantity;

    @ColumnInfo(name = "unit_price")
    private double unitPrice;

    @ColumnInfo(name = "catalog_price")
    private double catalogPrice;

    public SaleItem() {}

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public long getSaleId() { return saleId; }
    public void setSaleId(long saleId) { this.saleId = saleId; }
    public long getProductId() { return productId; }
    public void setProductId(long productId) { this.productId = productId; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public double getUnitPrice() { return unitPrice; }
    public void setUnitPrice(double unitPrice) { this.unitPrice = unitPrice; }
    public double getCatalogPrice() { return catalogPrice; }
    public void setCatalogPrice(double catalogPrice) { this.catalogPrice = catalogPrice; }
}
