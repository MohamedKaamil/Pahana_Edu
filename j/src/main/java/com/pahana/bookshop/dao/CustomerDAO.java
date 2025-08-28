package com.pahana.bookshop.dao;

import com.pahana.bookshop.database.DatabaseConnection;
import com.pahana.bookshop.model.Customer;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CustomerDAO {
    
    public int create(Customer customer) throws SQLException {
        // First create the user record
        UserDAO userDAO = new UserDAO();
        int userId = userDAO.create(customer);
        
        if (userId > 0) {
            // Then create the customer-specific record
            String sql = "INSERT INTO customers (user_id, customer_id, customer_points, customer_address, comment) VALUES (?, ?, ?, ?, ?)";
            
            try (Connection conn = DatabaseConnection.getInstance().getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                
                System.out.println("Creating customer record for user_id: " + userId);
                stmt.setInt(1, userId);
                stmt.setString(2, customer.getCustomerId() != null ? customer.getCustomerId() : "CUST_" + userId);
                stmt.setInt(3, customer.getCustomerPoints());
                stmt.setString(4, customer.getCustomerAddress());
                stmt.setString(5, customer.getComment());
                
                int result = stmt.executeUpdate();
                if (result > 0) {
                    if (customer.getCustomerId() == null) {
                        customer.setCustomerId("CUST_" + userId);
                    }
                    System.out.println("Customer record created successfully");
                    return userId;
                }
            } catch (SQLException e) {
                System.err.println("Error creating customer record: " + e.getMessage());
                throw e;
            }
        }
        return 0;
    }
    
    public Customer findById(int userId) throws SQLException {
        String sql = "SELECT u.*, c.customer_id, c.customer_points, c.customer_address, c.comment " +
                     "FROM users u JOIN customers c ON u.user_id = c.user_id WHERE u.user_id = ?";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, userId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToCustomer(rs);
                }
            }
        }
        return null;
    }
    
    public Customer findByCustomerId(String customerId) throws SQLException {
        String sql = "SELECT u.*, c.customer_id, c.customer_points, c.customer_address, c.comment " +
                     "FROM users u JOIN customers c ON u.user_id = c.user_id WHERE c.customer_id = ?";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, customerId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToCustomer(rs);
                }
            }
        }
        return null;
    }
    
    public List<Customer> findAll() throws SQLException {
        String sql = "SELECT u.*, c.customer_id, c.customer_points, c.customer_address, c.comment " +
                     "FROM users u JOIN customers c ON u.user_id = c.user_id ORDER BY u.created_at DESC";
        List<Customer> customers = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                customers.add(mapResultSetToCustomer(rs));
            }
        }
        return customers;
    }
    
    public boolean updateCustomerInfo(Customer customer) throws SQLException {
        String sql = "UPDATE customers SET customer_points = ?, customer_address = ?, comment = ? WHERE user_id = ?";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, customer.getCustomerPoints());
            stmt.setString(2, customer.getCustomerAddress());
            stmt.setString(3, customer.getComment());
            stmt.setInt(4, customer.getUserId());
            
            return stmt.executeUpdate() > 0;
        }
    }
    
    private Customer mapResultSetToCustomer(ResultSet rs) throws SQLException {
        Customer customer = new Customer();
        customer.setUserId(rs.getInt("user_id"));
        customer.setUsername(rs.getString("username"));
        customer.setPassword(rs.getString("password"));
        customer.setEmail(rs.getString("email"));
        customer.setFullName(rs.getString("full_name"));
        customer.setUserType(rs.getString("user_type"));
        customer.setStatus(rs.getString("status"));
        customer.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        customer.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        
        customer.setCustomerId(rs.getString("customer_id"));
        customer.setCustomerPoints(rs.getInt("customer_points"));
        customer.setCustomerAddress(rs.getString("customer_address"));
        customer.setComment(rs.getString("comment"));
        
        return customer;
    }
}