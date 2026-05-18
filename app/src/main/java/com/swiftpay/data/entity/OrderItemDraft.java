package com.swiftpay.data.entity;

public class OrderItemDraft {
    private long productId;
    private String productName;
    private int quantity;
    private double unitCost;

    public OrderItemDraft(long productId, String productName, int quantity, double unitCost) {
        this.productId = productId;
        this.productName = productName;
        this.quantity = quantity;
        this.unitCost = unitCost;
    }

    public long getProductId() { return productId; }
    public String getProductName() { return productName; }
    public int getQuantity() { return quantity; }
    public double getUnitCost() { return unitCost; }
    
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public void setUnitCost(double unitCost) { this.unitCost = unitCost; }
    
    public double getSubtotal() { return quantity * unitCost; }
}
