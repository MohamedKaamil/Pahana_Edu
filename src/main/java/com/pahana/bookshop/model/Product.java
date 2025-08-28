package com.pahana.bookshop.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Product {
    private int productId;
    private String productCode;
    private String name;
    private BigDecimal price;
    private int quantity;
    private String category; // BOOK, STATIONARY
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Product() {
        this.status = "AVAILABLE";
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.quantity = 0;
        this.price = BigDecimal.ZERO;
    }

    public Product(String name, BigDecimal price, int quantity, String category) {
        this();
        this.name = name;
        this.price = price;
        this.quantity = quantity;
        this.category = category;
    }

    // Getters and Setters
    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public String getProductCode() {
        return productCode;
    }

    public void setProductCode(String productCode) {
        this.productCode = productCode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    // Stock management methods
    public boolean isLowStock() {
        return quantity < 5;
    }

    public boolean isOutOfStock() {
        return quantity == 0;
    }

    public void addStock(int amount) {
        this.quantity += amount;
        this.updatedAt = LocalDateTime.now();
    }

    public boolean reduceStock(int amount) {
        if (this.quantity >= amount) {
            this.quantity -= amount;
            this.updatedAt = LocalDateTime.now();
            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        return "Product{" +
                "productId=" + productId +
                ", name='" + name + '\'' +
                ", price=" + price +
                ", quantity=" + quantity +
                ", category='" + category + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}