package com.pahana.bookshop.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Cashier extends User {
    private String cashierId;
    private String record;
    private BigDecimal salary;

    public Cashier() {
        super();
        this.setUserType("CASHIER");
    }

    public Cashier(String username, String password, String email, String fullName) {
        super(username, password, "CASHIER");
        this.setEmail(email);
        this.setFullName(fullName);
        this.salary = BigDecimal.ZERO;
        this.record = "NEW";
    }

    public String getCashierId() {
        return cashierId;
    }

    public void setCashierId(String cashierId) {
        this.cashierId = cashierId;
    }

    public String getRecord() {
        return record;
    }

    public void setRecord(String record) {
        this.record = record;
    }

    public BigDecimal getSalary() {
        return salary;
    }

    public void setSalary(BigDecimal salary) {
        this.salary = salary;
    }

    @Override
    public String toString() {
        return "Cashier{" +
                "userId=" + getUserId() +
                ", username='" + getUsername() + '\'' +
                ", cashierId='" + cashierId + '\'' +
                ", record='" + record + '\'' +
                ", salary=" + salary +
                ", createdAt=" + getCreatedAt() +
                '}';
    }
}