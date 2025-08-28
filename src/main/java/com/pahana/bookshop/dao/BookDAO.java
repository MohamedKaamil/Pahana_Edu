package com.pahana.bookshop.dao;

import com.pahana.bookshop.database.DatabaseConnection;
import com.pahana.bookshop.model.Book;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BookDAO {
    
    public int create(Book book) throws SQLException {
        // First create the product record
        ProductDAO productDAO = new ProductDAO();
        int productId = productDAO.create(book);
        
        if (productId > 0) {
            // Then create the book-specific record
            String sql = "INSERT INTO books (product_id, genre, author, isbn, publisher, pages) VALUES (?, ?, ?, ?, ?, ?)";
            
            try (Connection conn = DatabaseConnection.getInstance().getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                
                stmt.setInt(1, productId);
                stmt.setString(2, book.getGenre());
                stmt.setString(3, book.getAuthor());
                stmt.setString(4, book.getIsbn());
                stmt.setString(5, book.getPublisher());
                stmt.setInt(6, book.getPages());
                
                int result = stmt.executeUpdate();
                if (result > 0) {
                    System.out.println("Book record created successfully for product_id: " + productId);
                    return productId;
                }
            } catch (SQLException e) {
                System.err.println("Error creating book record: " + e.getMessage());
                throw e;
            }
        }
        return 0;
    }
    
    public Book findById(int productId) throws SQLException {
        String sql = "SELECT p.*, b.genre, b.author, b.isbn, b.publisher, b.pages " +
                     "FROM products p JOIN books b ON p.product_id = b.product_id WHERE p.product_id = ?";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, productId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToBook(rs);
                }
            }
        }
        return null;
    }
    
    public Book findByIsbn(String isbn) throws SQLException {
        String sql = "SELECT p.*, b.genre, b.author, b.isbn, b.publisher, b.pages " +
                     "FROM products p JOIN books b ON p.product_id = b.product_id WHERE b.isbn = ?";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, isbn);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToBook(rs);
                }
            }
        }
        return null;
    }
    
    public List<Book> findAll() throws SQLException {
        String sql = "SELECT p.*, b.genre, b.author, b.isbn, b.publisher, b.pages " +
                     "FROM products p JOIN books b ON p.product_id = b.product_id ORDER BY p.created_at DESC";
        List<Book> books = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                books.add(mapResultSetToBook(rs));
            }
        }
        return books;
    }
    
    public List<Book> findByGenre(String genre) throws SQLException {
        String sql = "SELECT p.*, b.genre, b.author, b.isbn, b.publisher, b.pages " +
                     "FROM products p JOIN books b ON p.product_id = b.product_id WHERE b.genre = ? ORDER BY p.name";
        List<Book> books = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, genre.toUpperCase());
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    books.add(mapResultSetToBook(rs));
                }
            }
        }
        return books;
    }
    
    public List<Book> findByAuthor(String author) throws SQLException {
        String sql = "SELECT p.*, b.genre, b.author, b.isbn, b.publisher, b.pages " +
                     "FROM products p JOIN books b ON p.product_id = b.product_id WHERE b.author LIKE ? ORDER BY p.name";
        List<Book> books = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, "%" + author + "%");
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    books.add(mapResultSetToBook(rs));
                }
            }
        }
        return books;
    }
    
    public boolean updateBookInfo(Book book) throws SQLException {
        // First update the product info
        ProductDAO productDAO = new ProductDAO();
        boolean productUpdated = productDAO.update(book);
        
        if (productUpdated) {
            // Then update book-specific info
            String sql = "UPDATE books SET genre = ?, author = ?, isbn = ?, publisher = ?, pages = ? WHERE product_id = ?";
            
            try (Connection conn = DatabaseConnection.getInstance().getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                
                stmt.setString(1, book.getGenre());
                stmt.setString(2, book.getAuthor());
                stmt.setString(3, book.getIsbn());
                stmt.setString(4, book.getPublisher());
                stmt.setInt(5, book.getPages());
                stmt.setInt(6, book.getProductId());
                
                return stmt.executeUpdate() > 0;
            }
        }
        return false;
    }
    
    private Book mapResultSetToBook(ResultSet rs) throws SQLException {
        Book book = new Book();
        // Map Product fields
        book.setProductId(rs.getInt("product_id"));
        book.setProductCode(rs.getString("product_code"));
        book.setName(rs.getString("name"));
        book.setPrice(rs.getBigDecimal("price"));
        book.setQuantity(rs.getInt("quantity"));
        book.setCategory(rs.getString("category"));
        book.setStatus(rs.getString("status"));
        book.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        book.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        
        // Map Book-specific fields
        book.setGenre(rs.getString("genre"));
        book.setAuthor(rs.getString("author"));
        book.setIsbn(rs.getString("isbn"));
        book.setPublisher(rs.getString("publisher"));
        book.setPages(rs.getInt("pages"));
        
        return book;
    }
}