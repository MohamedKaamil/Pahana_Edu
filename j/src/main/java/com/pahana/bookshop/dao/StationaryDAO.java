package com.pahana.bookshop.dao;

import com.pahana.bookshop.database.DatabaseConnection;
import com.pahana.bookshop.model.Stationary;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class StationaryDAO {
    
    public int create(Stationary stationary) throws SQLException {
        // First create the product record
        ProductDAO productDAO = new ProductDAO();
        int productId = productDAO.create(stationary);
        
        if (productId > 0) {
            // Then create the stationary-specific record
            String sql = "INSERT INTO stationary (product_id, type, brand, color, size, material) VALUES (?, ?, ?, ?, ?, ?)";
            
            try (Connection conn = DatabaseConnection.getInstance().getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                
                stmt.setInt(1, productId);
                stmt.setString(2, stationary.getType());
                stmt.setString(3, stationary.getBrand());
                stmt.setString(4, stationary.getColor());
                stmt.setString(5, stationary.getSize());
                stmt.setString(6, stationary.getMaterial());
                
                int result = stmt.executeUpdate();
                if (result > 0) {
                    System.out.println("Stationary record created successfully for product_id: " + productId);
                    return productId;
                }
            } catch (SQLException e) {
                System.err.println("Error creating stationary record: " + e.getMessage());
                throw e;
            }
        }
        return 0;
    }
    
    public Stationary findById(int productId) throws SQLException {
        String sql = "SELECT p.*, s.type, s.brand, s.color, s.size, s.material " +
                     "FROM products p JOIN stationary s ON p.product_id = s.product_id WHERE p.product_id = ?";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, productId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToStationary(rs);
                }
            }
        }
        return null;
    }
    
    public List<Stationary> findAll() throws SQLException {
        String sql = "SELECT p.*, s.type, s.brand, s.color, s.size, s.material " +
                     "FROM products p JOIN stationary s ON p.product_id = s.product_id ORDER BY p.created_at DESC";
        List<Stationary> stationaryList = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                stationaryList.add(mapResultSetToStationary(rs));
            }
        }
        return stationaryList;
    }
    
    public List<Stationary> findByType(String type) throws SQLException {
        String sql = "SELECT p.*, s.type, s.brand, s.color, s.size, s.material " +
                     "FROM products p JOIN stationary s ON p.product_id = s.product_id WHERE s.type = ? ORDER BY p.name";
        List<Stationary> stationaryList = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, type.toUpperCase());
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    stationaryList.add(mapResultSetToStationary(rs));
                }
            }
        }
        return stationaryList;
    }
    
    public List<Stationary> findByBrand(String brand) throws SQLException {
        String sql = "SELECT p.*, s.type, s.brand, s.color, s.size, s.material " +
                     "FROM products p JOIN stationary s ON p.product_id = s.product_id WHERE s.brand LIKE ? ORDER BY p.name";
        List<Stationary> stationaryList = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, "%" + brand + "%");
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    stationaryList.add(mapResultSetToStationary(rs));
                }
            }
        }
        return stationaryList;
    }
    
    public boolean updateStationaryInfo(Stationary stationary) throws SQLException {
        // First update the product info
        ProductDAO productDAO = new ProductDAO();
        boolean productUpdated = productDAO.update(stationary);
        
        if (productUpdated) {
            // Then update stationary-specific info
            String sql = "UPDATE stationary SET type = ?, brand = ?, color = ?, size = ?, material = ? WHERE product_id = ?";
            
            try (Connection conn = DatabaseConnection.getInstance().getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                
                stmt.setString(1, stationary.getType());
                stmt.setString(2, stationary.getBrand());
                stmt.setString(3, stationary.getColor());
                stmt.setString(4, stationary.getSize());
                stmt.setString(5, stationary.getMaterial());
                stmt.setInt(6, stationary.getProductId());
                
                return stmt.executeUpdate() > 0;
            }
        }
        return false;
    }
    
    private Stationary mapResultSetToStationary(ResultSet rs) throws SQLException {
        Stationary stationary = new Stationary();
        // Map Product fields
        stationary.setProductId(rs.getInt("product_id"));
        stationary.setProductCode(rs.getString("product_code"));
        stationary.setName(rs.getString("name"));
        stationary.setPrice(rs.getBigDecimal("price"));
        stationary.setQuantity(rs.getInt("quantity"));
        stationary.setCategory(rs.getString("category"));
        stationary.setStatus(rs.getString("status"));
        stationary.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        stationary.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        
        // Map Stationary-specific fields
        stationary.setType(rs.getString("type"));
        stationary.setBrand(rs.getString("brand"));
        stationary.setColor(rs.getString("color"));
        stationary.setSize(rs.getString("size"));
        stationary.setMaterial(rs.getString("material"));
        
        return stationary;
    }
}