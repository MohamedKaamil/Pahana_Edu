package com.pahana.bookshop.factory;

import com.pahana.bookshop.model.Product;
import com.pahana.bookshop.model.Book;
import com.pahana.bookshop.model.Stationary;
import java.math.BigDecimal;

public class ProductFactory {
    
    public static Product createProduct(String category, String name, BigDecimal price, int quantity) {
        switch (category.toUpperCase()) {
            case "BOOK":
                return new Book(name, price, quantity, null, null, null);
            case "STATIONARY":
                return new Stationary(name, price, quantity, null, null);
            default:
                return new Product(name, price, quantity, category.toUpperCase());
        }
    }
    
    public static Book createBook(String name, BigDecimal price, int quantity, String genre, String author, String isbn) {
        Book book = new Book(name, price, quantity, genre, author, isbn);
        return book;
    }
    
    public static Stationary createStationary(String name, BigDecimal price, int quantity, String type, String brand) {
        Stationary stationary = new Stationary(name, price, quantity, type, brand);
        return stationary;
    }
    
    public static boolean isValidCategory(String category) {
        if (category == null) return false;
        String cat = category.toUpperCase();
        return cat.equals("BOOK") || cat.equals("STATIONARY");
    }
    
    public static String[] getValidCategories() {
        return new String[]{"BOOK", "STATIONARY"};
    }
    
    public static boolean isValidBookGenre(String genre) {
        return Book.isValidGenre(genre);
    }
    
    public static boolean isValidStationaryType(String type) {
        return Stationary.isValidType(type);
    }
    
    public static String[] getValidBookGenres() {
        return Book.getValidGenres();
    }
    
    public static String[] getValidStationaryTypes() {
        return Stationary.getValidTypes();
    }
}