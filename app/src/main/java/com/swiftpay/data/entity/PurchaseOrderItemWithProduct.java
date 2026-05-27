package com.swiftpay.data.entity;

import androidx.room.Embedded;
import androidx.room.Relation;

public class PurchaseOrderItemWithProduct {
    @Embedded
    public PurchaseOrderItem item;

    @Relation(
            parentColumn = "product_id",
            entityColumn = "id"
    )
    public Product product;
}
