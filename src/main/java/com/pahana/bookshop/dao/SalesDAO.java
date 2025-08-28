package com.pahana.bookshop.dao;

import com.pahana.bookshop.database.DatabaseConnection;
import com.pahana.bookshop.model.Sale;
import com.pahana.bookshop.model.SaleItem;
import com.pahana.bookshop.model.Product;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.math.BigDecimal;

public class SalesDAO {
    private ProductDAO productDAO;
    
    public SalesDAO() {
        this.productDAO = new ProductDAO();
    }
    
    public int createSale(Sale sale) throws SQLException {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getInstance().getConnection();
            conn.setAutoCommit(false); // Start transaction
            
            // Generate sale number if not provided
            if (sale.getSaleNumber() == null) {
                sale.setSaleNumber("SALE_" + System.currentTimeMillis());
            }
            
            // Calculate totals
            sale.calculateTotalAmount();
            
            // Insert sale record
            String saleSql = "INSERT INTO sales (sale_number, customer_id, customer_name, total_amount, discount, final_amount, payment_method, sale_status, notes, sold_by, sold_by_name, sale_date, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            
            int saleId;
            try (PreparedStatement stmt = conn.prepareStatement(saleSql, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, sale.getSaleNumber());
                stmt.setString(2, sale.getCustomerId());
                stmt.setString(3, sale.getCustomerName());
                stmt.setBigDecimal(4, sale.getTotalAmount());
                stmt.setBigDecimal(5, sale.getDiscount());
                stmt.setBigDecimal(6, sale.getFinalAmount());
                stmt.setString(7, sale.getPaymentMethod());
                stmt.setString(8, sale.getSaleStatus());
                stmt.setString(9, sale.getNotes());
                stmt.setInt(10, sale.getSoldBy());
                stmt.setString(11, sale.getSoldByName());
                stmt.setTimestamp(12, Timestamp.valueOf(sale.getSaleDate()));
                stmt.setTimestamp(13, Timestamp.valueOf(sale.getCreatedAt()));
                stmt.setTimestamp(14, Timestamp.valueOf(sale.getUpdatedAt()));
                
                int affectedRows = stmt.executeUpdate();
                if (affectedRows == 0) {
                    throw new SQLException("Creating sale failed, no rows affected");
                }
                
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        saleId = generatedKeys.getInt(1);
                        sale.setSaleId(saleId);
                    } else {
                        throw new SQLException("Creating sale failed, no ID obtained");
                    }
                }
            }
            
            // Insert sale items and update inventory
            if (sale.getSaleItems() != null && !sale.getSaleItems().isEmpty()) {
                for (SaleItem item : sale.getSaleItems()) {
                    // Check stock availability using the same connection
                    Product product = findProductById(conn, item.getProductId());
                    if (product == null) {
                        throw new SQLException("Product not found: " + item.getProductId());
                    }
                    
                    if (product.getQuantity() < item.getQuantity()) {
                        throw new SQLException("Insufficient stock for product: " + product.getName() + 
                                             ". Available: " + product.getQuantity() + 
                                             ", Required: " + item.getQuantity());
                    }
                    
                    // Insert sale item
                    item.setSaleId(saleId);
                    createSaleItem(conn, item);
                    
                    // Update inventory (reduce quantity)
                    int newQuantity = product.getQuantity() - item.getQuantity();
                    updateProductQuantity(conn, item.getProductId(), newQuantity);
                    
                    // Update product status if out of stock
                    if (newQuantity == 0) {
                        updateProductStatus(conn, item.getProductId(), "OUT_OF_STOCK");
                    }
                }
            }
            
            conn.commit(); // Commit transaction
            System.out.println("Sale created successfully: " + sale.getSaleNumber());
            return saleId;
            
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback(); // Rollback on error
                    System.err.println("Sale creation rolled back due to error: " + e.getMessage());
                } catch (SQLException rollbackEx) {
                    System.err.println("Rollback failed: " + rollbackEx.getMessage());
                }
            }
            throw e;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true); // Reset auto-commit
                    conn.close();
                } catch (SQLException e) {
                    System.err.println("Error closing connection: " + e.getMessage());
                }
            }
        }
    }
    
    private void createSaleItem(Connection conn, SaleItem item) throws SQLException {
        String sql = "INSERT INTO sale_items (sale_id, product_id, product_name, product_code, product_category, unit_price, quantity, sub_total, created_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, item.getSaleId());
            stmt.setInt(2, item.getProductId());
            stmt.setString(3, item.getProductName());
            stmt.setString(4, item.getProductCode());
            stmt.setString(5, item.getProductCategory());
            stmt.setBigDecimal(6, item.getUnitPrice());
            stmt.setInt(7, item.getQuantity());
            stmt.setBigDecimal(8, item.getSubTotal());
            stmt.setTimestamp(9, Timestamp.valueOf(item.getCreatedAt()));
            
            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        item.setSaleItemId(generatedKeys.getInt(1));
                    }
                }
            }
        }
    }
    
    private void updateProductQuantity(Connection conn, int productId, int newQuantity) throws SQLException {
        String sql = "UPDATE products SET quantity = ?, updated_at = ? WHERE product_id = ?";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, newQuantity);
            stmt.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setInt(3, productId);
            
            stmt.executeUpdate();
        }
    }
    
    private void updateProductStatus(Connection conn, int productId, String status) throws SQLException {
        String sql = "UPDATE products SET status = ?, updated_at = ? WHERE product_id = ?";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status);
            stmt.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setInt(3, productId);
            
            stmt.executeUpdate();
        }
    }
    
    public Sale findById(int saleId) throws SQLException {
        String sql = "SELECT * FROM sales WHERE sale_id = ?";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, saleId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Sale sale = mapResultSetToSale(rs);
                    sale.setSaleItems(findSaleItemsBySaleId(saleId));
                    return sale;
                }
            }
        }
        return null;
    }
    
    public List<Sale> findAll() throws SQLException {
        String sql = "SELECT * FROM sales ORDER BY sale_date DESC";
        List<Sale> sales = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Sale sale = mapResultSetToSale(rs);
                sales.add(sale);
            }
        }
        
        // Load sale items for each sale after closing the main ResultSet
        for (Sale sale : sales) {
            sale.setSaleItems(findSaleItemsBySaleId(sale.getSaleId()));
        }
        
        return sales;
    }
    
    public List<Sale> findByStatus(String status) throws SQLException {
        String sql = "SELECT * FROM sales WHERE sale_status = ? ORDER BY sale_date DESC";
        List<Sale> sales = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, status);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Sale sale = mapResultSetToSale(rs);
                    sales.add(sale);
                }
            }
        }
        
        // Load sale items for each sale after closing the main ResultSet
        for (Sale sale : sales) {
            sale.setSaleItems(findSaleItemsBySaleId(sale.getSaleId()));
        }
        
        return sales;
    }
    
    public List<Sale> findByCustomer(String customerId) throws SQLException {
        String sql = "SELECT * FROM sales WHERE customer_id = ? ORDER BY sale_date DESC";
        List<Sale> sales = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, customerId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Sale sale = mapResultSetToSale(rs);
                    sales.add(sale);
                }
            }
        }
        
        // Load sale items for each sale after closing the main ResultSet
        for (Sale sale : sales) {
            sale.setSaleItems(findSaleItemsBySaleId(sale.getSaleId()));
        }
        
        return sales;
    }
    
    public List<Sale> findByDateRange(LocalDateTime startDate, LocalDateTime endDate) throws SQLException {
        String sql = "SELECT * FROM sales WHERE sale_date BETWEEN ? AND ? ORDER BY sale_date DESC";
        List<Sale> sales = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setTimestamp(1, Timestamp.valueOf(startDate));
            stmt.setTimestamp(2, Timestamp.valueOf(endDate));
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Sale sale = mapResultSetToSale(rs);
                    sales.add(sale);
                }
            }
        }
        
        // Load sale items for each sale after closing the main ResultSet
        for (Sale sale : sales) {
            sale.setSaleItems(findSaleItemsBySaleId(sale.getSaleId()));
        }
        
        return sales;
    }
    
    private List<SaleItem> findSaleItemsBySaleId(int saleId) throws SQLException {
        String sql = "SELECT * FROM sale_items WHERE sale_id = ? ORDER BY sale_item_id";
        List<SaleItem> saleItems = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, saleId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    saleItems.add(mapResultSetToSaleItem(rs));
                }
            }
        }
        return saleItems;
    }
    
    public boolean updateSaleStatus(int saleId, String status) throws SQLException {
        String sql = "UPDATE sales SET sale_status = ?, updated_at = ? WHERE sale_id = ?";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, status);
            stmt.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setInt(3, saleId);
            
            return stmt.executeUpdate() > 0;
        }
    }
    
    // Sales analytics methods
    public BigDecimal getTotalSalesAmount() throws SQLException {
        String sql = "SELECT SUM(final_amount) as total FROM sales WHERE sale_status = 'COMPLETED'";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            if (rs.next()) {
                return rs.getBigDecimal("total") != null ? rs.getBigDecimal("total") : BigDecimal.ZERO;
            }
        }
        return BigDecimal.ZERO;
    }
    
    public int getTotalSalesCount() throws SQLException {
        String sql = "SELECT COUNT(*) as count FROM sales WHERE sale_status = 'COMPLETED'";
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            if (rs.next()) {
                return rs.getInt("count");
            }
        }
        return 0;
    }
    
    private Sale mapResultSetToSale(ResultSet rs) throws SQLException {
        Sale sale = new Sale();
        sale.setSaleId(rs.getInt("sale_id"));
        sale.setSaleNumber(rs.getString("sale_number"));
        sale.setCustomerId(rs.getString("customer_id"));
        sale.setCustomerName(rs.getString("customer_name"));
        sale.setTotalAmount(rs.getBigDecimal("total_amount"));
        sale.setDiscount(rs.getBigDecimal("discount"));
        sale.setFinalAmount(rs.getBigDecimal("final_amount"));
        sale.setPaymentMethod(rs.getString("payment_method"));
        sale.setSaleStatus(rs.getString("sale_status"));
        sale.setNotes(rs.getString("notes"));
        sale.setSoldBy(rs.getInt("sold_by"));
        sale.setSoldByName(rs.getString("sold_by_name"));
        sale.setSaleDate(rs.getTimestamp("sale_date").toLocalDateTime());
        sale.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        sale.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        return sale;
    }
    
    private SaleItem mapResultSetToSaleItem(ResultSet rs) throws SQLException {
        SaleItem item = new SaleItem();
        item.setSaleItemId(rs.getInt("sale_item_id"));
        item.setSaleId(rs.getInt("sale_id"));
        item.setProductId(rs.getInt("product_id"));
        item.setProductName(rs.getString("product_name"));
        item.setProductCode(rs.getString("product_code"));
        item.setProductCategory(rs.getString("product_category"));
        item.setUnitPrice(rs.getBigDecimal("unit_price"));
        item.setQuantity(rs.getInt("quantity"));
        item.setSubTotal(rs.getBigDecimal("sub_total"));
        item.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        return item;
    }
    
    // Helper method to find product using existing connection
    private Product findProductById(Connection conn, int productId) throws SQLException {
        String sql = "SELECT * FROM products WHERE product_id = ?";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, productId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
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
        }
        return null;
    }
}