package com.pahana.bookshop.model;

import java.time.LocalDateTime;

public class Customer extends User {
    private String customerId;
    private int customerPoints;
    private String customerAddress;
    private String comment;

    public Customer() {
        super();
        this.setUserType("CUSTOMER");
    }

    public Customer(String username, String password, String email, String fullName) {
        super(username, password, "CUSTOMER");
        this.setEmail(email);
        this.setFullName(fullName);
        this.customerPoints = 0;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public int getCustomerPoints() {
        return customerPoints;
    }

    public void setCustomerPoints(int customerPoints) {
        this.customerPoints = customerPoints;
    }

    public String getCustomerAddress() {
        return customerAddress;
    }

    public void setCustomerAddress(String customerAddress) {
        this.customerAddress = customerAddress;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    @Override
    public String toString() {
        return "Customer{" +
                "userId=" + getUserId() +
                ", username='" + getUsername() + '\'' +
                ", customerId='" + customerId + '\'' +
                ", customerPoints=" + customerPoints +
                ", customerAddress='" + customerAddress + '\'' +
                ", comment='" + comment + '\'' +
                ", createdAt=" + getCreatedAt() +
                '}';
    }
}