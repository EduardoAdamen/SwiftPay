package com.swiftpay.data.entity;

/**
 * POJO para los items del carrito de compras.
 * No es una entidad de la base de datos (Room). Se guarda en memoria en el ViewModel.
 */
public class CartItem {
    private long productId;
    private String productName;
    private String sku;
    private int quantity;
    private double catalogPrice;
    private double unitPrice;

    public CartItem(long productId, String productName, String sku, double catalogPrice, double unitPrice, int quantity) {
        this.productId = productId;
        this.productName = productName;
        this.sku = sku;
        this.catalogPrice = catalogPrice;
        this.unitPrice = unitPrice;
        this.quantity = quantity;
    }

    public long getProductId() { return productId; }
    public void setProductId(long productId) { this.productId = productId; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public double getCatalogPrice() { return catalogPrice; }
    public void setCatalogPrice(double catalogPrice) { this.catalogPrice = catalogPrice; }

    public double getUnitPrice() { return unitPrice; }
    public void setUnitPrice(double unitPrice) { this.unitPrice = unitPrice; }

    public double getSubtotal() {
        return unitPrice * quantity;
    }
}
