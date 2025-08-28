package com.pahana.bookshop.dao;

import com.pahana.bookshop.database.DatabaseConnection;
import com.pahana.bookshop.model.Product;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public
class ProductDAO {
    
    public int create(Product product) throws SQLException {
        String sql = "INSERT INTO products (product_code, name, price, quantity, category, status, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setString(1, product.getProductCode() != null ? product.getProductCode() : "PROD_" + System.currentTimeMillis());
            stmt.setString(2, product.getName());
            stmt.setBigDecimal(3, product.getPrice());
            stmt.setInt(4, product.getQuantity());
            stmt.setString(5, product.getCategory());
            stmt.setString(6, product.getStatus());
            stmt.setTimestamp(7, Timestamp.valueOf(product.getCreatedAt()));
            stmt.setTimestamp(8, Timestamp.valueOf(product.getUpdatedAt()));
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        product.setProductId(generatedKeys.getInt(1));
                        // Set product code if it was null
                        if (product.getProductCode() == null) {
                            product.setProductCode("PROD_" + product.getProductId());
                            updateProductCode(product.getProductId(), product.getProductCode());
                        }
                        return product.getProductId();
                    }
                }
            }
            return 0;
        }
    }
    
    public Product findById(int productId) throws SQLException {
        String sql = "SELECT * FROM products WHERE product_id = ?";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, productId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToProduct(rs);
                }
            }
        }
        return null;
    }
    
    public Product findByProductCode(String productCode) throws SQLException {
        String sql = "SELECT * FROM products WHERE product_code = ?";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, productCode);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToProduct(rs);
                }
            }
        }
        return null;
    }
    
    public List<Product> findAll() throws SQLException {
        String sql = "SELECT * FROM products ORDER BY created_at DESC";
        List<Product> products = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                products.add(mapResultSetToProduct(rs));
            }
        }
        return products;
    }
    
    public List<Product> findByCategory(String category) throws SQLException {
        String sql = "SELECT * FROM products WHERE category = ? ORDER BY name";
        List<Product> products = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, category.toUpperCase());
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    products.add(mapResultSetToProduct(rs));
                }
            }
        }
        return products;
    }
    
    public List<Product> searchByName(String name) throws SQLException {
        String sql = "SELECT * FROM products WHERE name LIKE ? ORDER BY name";
        List<Product> products = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, "%" + name + "%");
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    products.add(mapResultSetToProduct(rs));
                }
            }
        }
        return products;
    }
    
    public boolean update(Product product) throws SQLException {
        String sql = "UPDATE products SET product_code = ?, name = ?, price = ?, quantity = ?, category = ?, status = ?, updated_at = ? WHERE product_id = ?";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, product.getProductCode());
            stmt.setString(2, product.getName());
            stmt.setBigDecimal(3, product.getPrice());
            stmt.setInt(4, product.getQuantity());
            stmt.setString(5, product.getCategory());
            stmt.setString(6, product.getStatus());
            stmt.setTimestamp(7, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setInt(8, product.getProductId());
            
            return stmt.executeUpdate() > 0;
        }
    }
    
    public boolean updateQuantity(int productId, int newQuantity) throws SQLException {
        String sql = "UPDATE products SET quantity = ?, updated_at = ? WHERE product_id = ?";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, newQuantity);
            stmt.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setInt(3, productId);
            
            return stmt.executeUpdate() > 0;
        }
    }
    
    public boolean delete(int productId) throws SQLException {
        String sql = "DELETE FROM products WHERE product_id = ?";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, productId);
            return stmt.executeUpdate() > 0;
        }
    }
    
    // Stock management methods
    public List<Product> findLowStock(int threshold) throws SQLException {
        String sql = "SELECT * FROM products WHERE quantity <= ? ORDER BY quantity ASC";
        List<Product> products = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, threshold);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    products.add(mapResultSetToProduct(rs));
                }
            }
        }
        return products;
    }
    
    public List<Product> findOutOfStock() throws SQLException {
        return findLowStock(0);
    }
    
    private void updateProductCode(int productId, String productCode) throws SQLException {
        String sql = "UPDATE products SET product_code = ? WHERE product_id = ?";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, productCode);
            stmt.setInt(2, productId);
            stmt.executeUpdate();
        }
    }
    
    protected Product mapResultSetToProduct(ResultSet rs) throws SQLException {
        Product product = new Product();
        product.setProductId(rs.getInt("product_id"));
        product.setProductCode(rs.getString("product_code"));
        product.setName(rs.getString("name"));
        product.setPrice(rs.getBigDecimal("price"));
        product.setQuantity(rs.getInt("quantity"));
        product.setCategory(rs.getString("category"));
        product.setStatus(rs.getString("status"));
        product.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        product.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        return product;
    }
}