package com.pahana.bookshop.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class Sale {
    private int saleId;
    private String saleNumber;
    private String customerId;
    private String customerName;
    private BigDecimal totalAmount;
    private BigDecimal discount;
    private BigDecimal finalAmount;
    private String paymentMethod; // CASH, CARD, DIGITAL
    private String saleStatus; // PENDING, COMPLETED, CANCELLED, REFUNDED
    private String notes;
    private int soldBy; // cashier user_id
    private String soldByName;
    private LocalDateTime saleDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Sale items (products sold in this transaction)
    private List<SaleItem> saleItems;

    public Sale() {
        this.saleStatus = "PENDING";
        this.paymentMethod = "CASH";
        this.discount = BigDecimal.ZERO;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.saleDate = LocalDateTime.now();
    }

    public Sale(String customerId, String customerName, int soldBy, String soldByName) {
        this();
        this.customerId = customerId;
        this.customerName = customerName;
        this.soldBy = soldBy;
        this.soldByName = soldByName;
    }

    // Getters and Setters
    public int getSaleId() {
        return saleId;
    }

    public void setSaleId(int saleId) {
        this.saleId = saleId;
    }

    public String getSaleNumber() {
        return saleNumber;
    }

    public void setSaleNumber(String saleNumber) {
        this.saleNumber = saleNumber;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public BigDecimal getDiscount() {
        return discount;
    }

    public void setDiscount(BigDecimal discount) {
        this.discount = discount;
    }

    public BigDecimal getFinalAmount() {
        return finalAmount;
    }

    public void setFinalAmount(BigDecimal finalAmount) {
        this.finalAmount = finalAmount;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getSaleStatus() {
        return saleStatus;
    }

    public void setSaleStatus(String saleStatus) {
        this.saleStatus = saleStatus;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public int getSoldBy() {
        return soldBy;
    }

    public void setSoldBy(int soldBy) {
        this.soldBy = soldBy;
    }

    public String getSoldByName() {
        return soldByName;
    }

    public void setSoldByName(String soldByName) {
        this.soldByName = soldByName;
    }

    public LocalDateTime getSaleDate() {
        return saleDate;
    }

    public void setSaleDate(LocalDateTime saleDate) {
        this.saleDate = saleDate;
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

    public List<SaleItem> getSaleItems() {
        return saleItems;
    }

    public void setSaleItems(List<SaleItem> saleItems) {
        this.saleItems = saleItems;
    }

    // Utility methods
    public void calculateTotalAmount() {
        if (saleItems != null && !saleItems.isEmpty()) {
            this.totalAmount = saleItems.stream()
                .map(SaleItem::getSubTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            // Calculate final amount after discount
            this.finalAmount = this.totalAmount.subtract(this.discount != null ? this.discount : BigDecimal.ZERO);
        }
    }

    public int getTotalItemsCount() {
        if (saleItems != null) {
            return saleItems.stream().mapToInt(SaleItem::getQuantity).sum();
        }
        return 0;
    }

    @Override
    public String toString() {
        return "Sale{" +
                "saleId=" + saleId +
                ", saleNumber='" + saleNumber + '\'' +
                ", customerName='" + customerName + '\'' +
                ", totalAmount=" + totalAmount +
                ", finalAmount=" + finalAmount +
                ", paymentMethod='" + paymentMethod + '\'' +
                ", saleStatus='" + saleStatus + '\'' +
                ", soldByName='" + soldByName + '\'' +
                ", saleDate=" + saleDate +
                '}';
    }
}