// com/swiftpay/data/entity/Product.java
package com.swiftpay.data.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * Entidad Room que representa un producto del catálogo.
 * Incluye campo 'version' para bloqueo optimista en actualizaciones concurrentes.
 */
@Entity(tableName = "products",
        foreignKeys = {
                @ForeignKey(
                        entity = ProductCategory.class,
                        parentColumns = "id",
                        childColumns = "category_id",
                        onDelete = ForeignKey.RESTRICT
                ),
                @ForeignKey(
                        entity = Brand.class,
                        parentColumns = "id",
                        childColumns = "brand_id",
                        onDelete = ForeignKey.SET_NULL
                )
        },
        indices = {
                @Index(value = "sku", unique = true),
                @Index(value = "category_id"),
                @Index(value = "brand_id")
        })
public class Product {

    @PrimaryKey(autoGenerate = true)
    private long id;

    @ColumnInfo(name = "sku")
    private String sku;

    @ColumnInfo(name = "name")
    private String name;

    @ColumnInfo(name = "price")
    private double price;

    @ColumnInfo(name = "stock", defaultValue = "0")
    private int stock;

    @ColumnInfo(name = "category_id")
    private long categoryId;

    @ColumnInfo(name = "brand_id")
    private Long brandId;

    @ColumnInfo(name = "image_path")
    private String imagePath;

    @ColumnInfo(name = "weight")
    private String weight;

    @ColumnInfo(name = "dimensions")
    private String dimensions;

    @ColumnInfo(name = "tags")
    private String tags;

    @ColumnInfo(name = "is_active", defaultValue = "1")
    private int isActive;

    @ColumnInfo(name = "version", defaultValue = "1")
    private int version;

    @ColumnInfo(name = "created_at")
    private long createdAt;

    @ColumnInfo(name = "updated_at")
    private long updatedAt;

    // --- Constructores ---

    public Product() {
    }

    // --- Getters y Setters ---

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    public long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(long categoryId) {
        this.categoryId = categoryId;
    }

    public Long getBrandId() {
        return brandId;
    }

    public void setBrandId(Long brandId) {
        this.brandId = brandId;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public String getWeight() {
        return weight;
    }

    public void setWeight(String weight) {
        this.weight = weight;
    }

    public String getDimensions() {
        return dimensions;
    }

    public void setDimensions(String dimensions) {
        this.dimensions = dimensions;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public int getIsActive() {
        return isActive;
    }

    public void setIsActive(int isActive) {
        this.isActive = isActive;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
    }
}
