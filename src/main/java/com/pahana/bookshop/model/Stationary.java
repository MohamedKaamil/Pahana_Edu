package com.pahana.bookshop.model;

import java.math.BigDecimal;

public class Stationary extends Product {
    private String type; // Pens, Pencils, Notebooks, Erasers, Rulers, Files
    private String brand;
    private String color;
    private String size;
    private String material;

    public Stationary() {
        super();
        this.setCategory("STATIONARY");
    }

    public Stationary(String name, BigDecimal price, int quantity, String type, String brand) {
        super(name, price, quantity, "STATIONARY");
        this.type = type;
        this.brand = brand;
    }

    // Getters and Setters
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getMaterial() {
        return material;
    }

    public void setMaterial(String material) {
        this.material = material;
    }

    // Utility methods
    public static String[] getValidTypes() {
        return new String[]{"PENS", "PENCILS", "NOTEBOOKS", "ERASERS", "RULERS", "FILES"};
    }

    public static boolean isValidType(String type) {
        if (type == null) return false;
        for (String validType : getValidTypes()) {
            if (validType.equalsIgnoreCase(type)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return "Stationary{" +
                "productId=" + getProductId() +
                ", name='" + getName() + '\'' +
                ", type='" + type + '\'' +
                ", brand='" + brand + '\'' +
                ", color='" + color + '\'' +
                ", size='" + size + '\'' +
                ", material='" + material + '\'' +
                ", price=" + getPrice() +
                ", quantity=" + getQuantity() +
                '}';
    }
}