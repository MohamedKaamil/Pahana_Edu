package com.pahana.bookshop.model;

import java.math.BigDecimal;

public class Book extends Product {
    private String genre; // Story, Novel, Action, Educational, Comics, Biography
    private String author;
    private String isbn;
    private String publisher;
    private int pages;

    public Book() {
        super();
        this.setCategory("BOOK");
    }

    public Book(String name, BigDecimal price, int quantity, String genre, String author, String isbn) {
        super(name, price, quantity, "BOOK");
        this.genre = genre;
        this.author = author;
        this.isbn = isbn;
    }

    // Getters and Setters
    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public int getPages() {
        return pages;
    }

    public void setPages(int pages) {
        this.pages = pages;
    }

    // Utility methods
    public static String[] getValidGenres() {
        return new String[]{"STORY", "NOVEL", "ACTION", "EDUCATIONAL", "COMICS", "BIOGRAPHY"};
    }

    public static boolean isValidGenre(String genre) {
        if (genre == null) return false;
        for (String validGenre : getValidGenres()) {
            if (validGenre.equalsIgnoreCase(genre)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return "Book{" +
                "productId=" + getProductId() +
                ", name='" + getName() + '\'' +
                ", genre='" + genre + '\'' +
                ", author='" + author + '\'' +
                ", isbn='" + isbn + '\'' +
                ", publisher='" + publisher + '\'' +
                ", pages=" + pages +
                ", price=" + getPrice() +
                ", quantity=" + getQuantity() +
                '}';
    }
}