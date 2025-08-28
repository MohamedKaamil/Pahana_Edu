package com.pahana.bookshop.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class SaleItem {
    private int saleItemId;
    private int saleId;
    private int productId;
    private String productName;
    private String productCode;
    private String productCategory; // BOOK, STATIONARY
    private BigDecimal unitPrice;
    private int quantity;
    private BigDecimal subTotal;
    private LocalDateTime createdAt;

    public SaleItem() {
        this.createdAt = LocalDateTime.now();
    }

    public SaleItem(int saleId, int productId, String productName, String productCode, 
                   String productCategory, BigDecimal unitPrice, int quantity) {
        this();
        this.saleId = saleId;
        this.productId = productId;
        this.productName = productName;
        this.productCode = productCode;
        this.productCategory = productCategory;
        this.unitPrice = unitPrice;
        this.quantity = quantity;
        this.calculateSubTotal();
    }

    // Getters and Setters
    public int getSaleItemId() {
        return saleItemId;
    }

    public void setSaleItemId(int saleItemId) {
        this.saleItemId = saleItemId;
    }

    public int getSaleId() {
        return saleId;
    }

    public void setSaleId(int saleId) {
        this.saleId = saleId;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getProductCode() {
        return productCode;
    }

    public void setProductCode(String productCode) {
        this.productCode = productCode;
    }

    public String getProductCategory() {
        return productCategory;
    }

    public void setProductCategory(String productCategory) {
        this.productCategory = productCategory;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
        this.calculateSubTotal();
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
        this.calculateSubTotal();
    }

    public BigDecimal getSubTotal() {
        return subTotal;
    }

    public void setSubTotal(BigDecimal subTotal) {
        this.subTotal = subTotal;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    // Utility methods
    private void calculateSubTotal() {
        if (this.unitPrice != null && this.quantity > 0) {
            this.subTotal = this.unitPrice.multiply(BigDecimal.valueOf(this.quantity));
        }
    }

    @Override
    public String toString() {
        return "SaleItem{" +
                "saleItemId=" + saleItemId +
                ", productName='" + productName + '\'' +
                ", productCode='" + productCode + '\'' +
                ", unitPrice=" + unitPrice +
                ", quantity=" + quantity +
                ", subTotal=" + subTotal +
                '}';
    }
}