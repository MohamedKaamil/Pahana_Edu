package com.pahana.bookshop.dao;

import com.pahana.bookshop.database.DatabaseConnection;
import com.pahana.bookshop.model.Cashier;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CashierDAO {
    
    public int create(Cashier cashier) throws SQLException {
        // First create the user record
        UserDAO userDAO = new UserDAO();
        int userId = userDAO.create(cashier);
        
        if (userId > 0) {
            // Then create the cashier-specific record
            String sql = "INSERT INTO cashiers (user_id, cashier_id, record_info, salary) VALUES (?, ?, ?, ?)";
            
            try (Connection conn = DatabaseConnection.getInstance().getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                
                stmt.setInt(1, userId);
                stmt.setString(2, cashier.getCashierId() != null ? cashier.getCashierId() : "CASH_" + userId);
                stmt.setString(3, cashier.getRecord());
                stmt.setBigDecimal(4, cashier.getSalary());
                
                int result = stmt.executeUpdate();
                if (result > 0) {
                    if (cashier.getCashierId() == null) {
                        cashier.setCashierId("CASH_" + userId);
                    }
                    return userId;
                }
            }
        }
        return 0;
    }
    
    public Cashier findById(int userId) throws SQLException {
        String sql = "SELECT u.*, c.cashier_id, c.record_info, c.salary " +
                     "FROM users u JOIN cashiers c ON u.user_id = c.user_id WHERE u.user_id = ?";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, userId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToCashier(rs);
                }
            }
        }
        return null;
    }
    
    public Cashier findByCashierId(String cashierId) throws SQLException {
        String sql = "SELECT u.*, c.cashier_id, c.record_info, c.salary " +
                     "FROM users u JOIN cashiers c ON u.user_id = c.user_id WHERE c.cashier_id = ?";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, cashierId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToCashier(rs);
                }
            }
        }
        return null;
    }
    
    public List<Cashier> findAll() throws SQLException {
        String sql = "SELECT u.*, c.cashier_id, c.record_info, c.salary " +
                     "FROM users u JOIN cashiers c ON u.user_id = c.user_id ORDER BY u.created_at DESC";
        List<Cashier> cashiers = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                cashiers.add(mapResultSetToCashier(rs));
            }
        }
        return cashiers;
    }
    
    public boolean updateCashierInfo(Cashier cashier) throws SQLException {
        String sql = "UPDATE cashiers SET record_info = ?, salary = ? WHERE user_id = ?";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, cashier.getRecord());
            stmt.setBigDecimal(2, cashier.getSalary());
            stmt.setInt(3, cashier.getUserId());
            
            return stmt.executeUpdate() > 0;
        }
    }
    
    private Cashier mapResultSetToCashier(ResultSet rs) throws SQLException {
        Cashier cashier = new Cashier();
        cashier.setUserId(rs.getInt("user_id"));
        cashier.setUsername(rs.getString("username"));
        cashier.setPassword(rs.getString("password"));
        cashier.setEmail(rs.getString("email"));
        cashier.setFullName(rs.getString("full_name"));
        cashier.setUserType(rs.getString("user_type"));
        cashier.setStatus(rs.getString("status"));
        cashier.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        cashier.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        
        cashier.setCashierId(rs.getString("cashier_id"));
        cashier.setRecord(rs.getString("record_info"));
        cashier.setSalary(rs.getBigDecimal("salary"));
        
        return cashier;
    }
}