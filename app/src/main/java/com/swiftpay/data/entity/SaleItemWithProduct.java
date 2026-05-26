package com.swiftpay.data.entity;

import androidx.room.Embedded;
import androidx.room.Relation;

public class SaleItemWithProduct {
    @Embedded
    public SaleItem saleItem;

    @Relation(
            parentColumn = "product_id",
            entityColumn = "id"
    )
    public Product product;
}