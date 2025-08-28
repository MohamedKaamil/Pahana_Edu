package com.pahana.bookshop.service;

import com.pahana.bookshop.dao.UserDAO;
import com.pahana.bookshop.model.User;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;

public class AuthenticationService {
    private UserDAO userDAO;
    
    public AuthenticationService() {
        this.userDAO = new UserDAO();
    }
    
    public User authenticate(String username, String password) throws SQLException {
        if (username == null || password == null || username.trim().isEmpty() || password.trim().isEmpty()) {
            return null;
        }
        
        User user = userDAO.findByUsername(username);
        if (user != null && verifyPassword(password, user.getPassword())) {
            return user;
        }
        return null;
    }
    
    public boolean createUser(String username, String password, String role) throws SQLException {
        if (username == null || password == null || role == null) {
            return false;
        }
        
        if (userDAO.findByUsername(username) != null) {
            return false;
        }
        
        String hashedPassword = hashPassword(password);
        User newUser = new User(username, hashedPassword, role);
        // Set default values for all required fields
        newUser.setEmail(username + "@bookshop.com"); // Default email
        newUser.setFullName(username.toUpperCase()); // Default full name
        newUser.setStatus("ACTIVE");
        newUser.setCreatedAt(java.time.LocalDateTime.now());
        newUser.setUpdatedAt(java.time.LocalDateTime.now());
        
        return userDAO.create(newUser) > 0;
    }
    
    public boolean changePassword(String username, String oldPassword, String newPassword) throws SQLException {
        User user = authenticate(username, oldPassword);
        if (user == null) {
            return false;
        }
        
        String hashedNewPassword = hashPassword(newPassword);
        user.setPassword(hashedNewPassword);
        return userDAO.update(user);
    }
    
    private String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashedBytes = md.digest(password.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hashedBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error hashing password", e);
        }
    }
    
    private boolean verifyPassword(String plainPassword, String hashedPassword) {
        return hashPassword(plainPassword).equals(hashedPassword);
    }
    
    public boolean isValidRole(String role) {
        return "admin".equalsIgnoreCase(role) || 
               "manager".equalsIgnoreCase(role) || 
               "cashier".equalsIgnoreCase(role) || 
               "user".equalsIgnoreCase(role);
    }
}