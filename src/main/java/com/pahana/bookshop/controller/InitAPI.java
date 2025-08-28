package com.pahana.bookshop.controller;

import java.io.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;
import com.pahana.bookshop.database.DatabaseConnection;
import com.pahana.bookshop.util.JsonUtil;
import java.sql.*;

@WebServlet(name = "initAPI", urlPatterns = {"/api/init/*"})
public class InitAPI extends HttpServlet {

    // POST /api/init/tables - Create all tables
    // POST /api/init/reset - Drop all tables and recreate fresh schema
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        
        try {
            String pathInfo = request.getPathInfo();
            
            if ("/tables".equals(pathInfo)) {
                createTables(response, out);
            } else if ("/reset".equals(pathInfo)) {
                resetDatabase(response, out);
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.print(JsonUtil.createErrorJson("Available: /tables, /reset"));
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print(JsonUtil.createErrorJson("Error: " + e.getMessage()));
        }
    }

    private void createTables(HttpServletResponse response, PrintWriter out) {
        try (Connection conn = DatabaseConnection.getInstance().getConnection()) {
            
            // First check what the primary key column name is in users table
            String checkUsersTable = "SHOW COLUMNS FROM users";
            String userPkColumn = "user_id"; // default assumption
            
            try (Statement checkStmt = conn.createStatement();
                 ResultSet rs = checkStmt.executeQuery(checkUsersTable)) {
                while (rs.next()) {
                    String columnName = rs.getString("Field");
                    String key = rs.getString("Key");
                    if ("PRI".equals(key)) {
                        userPkColumn = columnName;
                        System.out.println("Found users table primary key: " + userPkColumn);
                        break;
                    }
                }
            }
            
            // Create customers table
            String createCustomersTable = """
                CREATE TABLE IF NOT EXISTS customers (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    user_id INT NOT NULL,
                    customer_id VARCHAR(50) UNIQUE NOT NULL,
                    customer_points INT DEFAULT 0,
                    customer_address TEXT,
                    comment TEXT,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                    FOREIGN KEY (user_id) REFERENCES users(%s) ON DELETE CASCADE
                )
            """.formatted(userPkColumn);
            
            // Create cashiers table
            String createCashiersTable = """
                CREATE TABLE IF NOT EXISTS cashiers (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    user_id INT NOT NULL,
                    cashier_id VARCHAR(50) UNIQUE NOT NULL,
                    record_info TEXT DEFAULT 'NEW',
                    salary DECIMAL(10,2) DEFAULT 0.00,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                    FOREIGN KEY (user_id) REFERENCES users(%s) ON DELETE CASCADE
                )
            """.formatted(userPkColumn);
            
            try (Statement stmt = conn.createStatement()) {
                // Create customers table
                stmt.executeUpdate(createCustomersTable);
                System.out.println("Customers table created successfully");
                
                // Create cashiers table
                stmt.executeUpdate(createCashiersTable);
                System.out.println("Cashiers table created successfully");
                
                response.setStatus(HttpServletResponse.SC_CREATED);
                out.print(JsonUtil.createSuccessJson("Tables created successfully: customers, cashiers"));
            }
            
        } catch (SQLException e) {
            System.err.println("Error creating tables: " + e.getMessage());
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print(JsonUtil.createErrorJson("Database error: " + e.getMessage()));
        }
    }

    // GET /api/init/status - Check table status
    // GET /api/init/debug - Debug table structure
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        
        try {
            String pathInfo = request.getPathInfo();
            
            if ("/status".equals(pathInfo)) {
                checkTableStatus(response, out);
            } else if ("/debug".equals(pathInfo)) {
                debugTableStructure(response, out);
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.print(JsonUtil.createErrorJson("Available: /status, /debug"));
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print(JsonUtil.createErrorJson("Error: " + e.getMessage()));
        }
    }
    
    private void checkTableStatus(HttpServletResponse response, PrintWriter out) {
        try (Connection conn = DatabaseConnection.getInstance().getConnection()) {
            DatabaseMetaData metaData = conn.getMetaData();
            
            boolean usersExists = false;
            boolean customersExists = false;
            boolean cashiersExists = false;
            
            // Check if tables exist
            try (ResultSet rs = metaData.getTables(null, null, "%", new String[]{"TABLE"})) {
                while (rs.next()) {
                    String tableName = rs.getString("TABLE_NAME").toLowerCase();
                    if ("users".equals(tableName)) usersExists = true;
                    if ("customers".equals(tableName)) customersExists = true;
                    if ("cashiers".equals(tableName)) cashiersExists = true;
                }
            }
            
            StringBuilder status = new StringBuilder();
            status.append("{\"tables\": {");
            status.append("\"users\": ").append(usersExists).append(",");
            status.append("\"customers\": ").append(customersExists).append(",");
            status.append("\"cashiers\": ").append(cashiersExists);
            status.append("}}");
            
            out.print(status.toString());
            
        } catch (SQLException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print(JsonUtil.createErrorJson("Database error: " + e.getMessage()));
        }
    }
    
    private void debugTableStructure(HttpServletResponse response, PrintWriter out) {
        try (Connection conn = DatabaseConnection.getInstance().getConnection()) {
            StringBuilder debug = new StringBuilder();
            debug.append("{\"users_table_structure\": [");
            
            String checkUsersTable = "SHOW COLUMNS FROM users";
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(checkUsersTable)) {
                
                boolean first = true;
                while (rs.next()) {
                    if (!first) debug.append(",");
                    debug.append("{");
                    debug.append("\"field\": \"").append(rs.getString("Field")).append("\",");
                    debug.append("\"type\": \"").append(rs.getString("Type")).append("\",");
                    debug.append("\"key\": \"").append(rs.getString("Key")).append("\"");
                    debug.append("}");
                    first = false;
                }
            }
            debug.append("]}");
            
            out.print(debug.toString());
            
        } catch (SQLException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print(JsonUtil.createErrorJson("Database error: " + e.getMessage()));
        }
    }
    
    private void resetDatabase(HttpServletResponse response, PrintWriter out) {
        try (Connection conn = DatabaseConnection.getInstance().getConnection()) {
            
            // Disable foreign key checks to allow dropping tables
            try (Statement stmt = conn.createStatement()) {
                stmt.executeUpdate("SET FOREIGN_KEY_CHECKS = 0");
                
                // Get all tables in the database
                DatabaseMetaData metaData = conn.getMetaData();
                try (ResultSet rs = metaData.getTables(conn.getCatalog(), null, "%", new String[]{"TABLE"})) {
                    while (rs.next()) {
                        String tableName = rs.getString("TABLE_NAME");
                        System.out.println("Dropping table: " + tableName);
                        stmt.executeUpdate("DROP TABLE IF EXISTS " + tableName);
                    }
                }
                
                // Re-enable foreign key checks
                stmt.executeUpdate("SET FOREIGN_KEY_CHECKS = 1");
                
                System.out.println("All tables dropped successfully");
                
                // Now create fresh tables
                createFreshTables(stmt);
                
                response.setStatus(HttpServletResponse.SC_CREATED);
                out.print(JsonUtil.createSuccessJson("Database reset successfully. All tables dropped and recreated: users, customers, cashiers, products, books, stationary, sales, sale_items"));
            }
            
        } catch (SQLException e) {
            System.err.println("Error resetting database: " + e.getMessage());
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print(JsonUtil.createErrorJson("Database error: " + e.getMessage()));
        }
    }
    
    private void createFreshTables(Statement stmt) throws SQLException {
        // Create users table
        String createUsersTable = """
            CREATE TABLE users (
                user_id INT AUTO_INCREMENT PRIMARY KEY,
                username VARCHAR(50) UNIQUE NOT NULL,
                password VARCHAR(255) NOT NULL,
                email VARCHAR(100),
                full_name VARCHAR(100),
                user_type ENUM('ADMIN', 'USER', 'CUSTOMER', 'CASHIER') NOT NULL DEFAULT 'USER',
                status ENUM('ACTIVE', 'INACTIVE') DEFAULT 'ACTIVE',
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
            )
        """;
        
        // Create customers table
        String createCustomersTable = """
            CREATE TABLE customers (
                id INT AUTO_INCREMENT PRIMARY KEY,
                user_id INT NOT NULL,
                customer_id VARCHAR(50) UNIQUE NOT NULL,
                customer_points INT DEFAULT 0,
                customer_address TEXT,
                comment TEXT,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
            )
        """;
        
        // Create cashiers table
        String createCashiersTable = """
            CREATE TABLE cashiers (
                id INT AUTO_INCREMENT PRIMARY KEY,
                user_id INT NOT NULL,
                cashier_id VARCHAR(50) UNIQUE NOT NULL,
                record_info TEXT DEFAULT 'NEW',
                salary DECIMAL(10,2) DEFAULT 0.00,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
            )
        """;
        
        // Create products table
        String createProductsTable = """
            CREATE TABLE products (
                product_id INT AUTO_INCREMENT PRIMARY KEY,
                product_code VARCHAR(50) UNIQUE,
                name VARCHAR(255) NOT NULL,
                price DECIMAL(10,2) NOT NULL DEFAULT 0.00,
                quantity INT NOT NULL DEFAULT 0,
                category ENUM('BOOK', 'STATIONARY') NOT NULL,
                status ENUM('AVAILABLE', 'OUT_OF_STOCK', 'DISCONTINUED') DEFAULT 'AVAILABLE',
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
            )
        """;
        
        // Create books table
        String createBooksTable = """
            CREATE TABLE books (
                id INT AUTO_INCREMENT PRIMARY KEY,
                product_id INT NOT NULL,
                genre ENUM('STORY', 'NOVEL', 'ACTION', 'EDUCATIONAL', 'COMICS', 'BIOGRAPHY'),
                author VARCHAR(255),
                isbn VARCHAR(50) UNIQUE,
                publisher VARCHAR(255),
                pages INT,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                FOREIGN KEY (product_id) REFERENCES products(product_id) ON DELETE CASCADE
            )
        """;
        
        // Create stationary table
        String createStationaryTable = """
            CREATE TABLE stationary (
                id INT AUTO_INCREMENT PRIMARY KEY,
                product_id INT NOT NULL,
                type ENUM('PENS', 'PENCILS', 'NOTEBOOKS', 'ERASERS', 'RULERS', 'FILES'),
                brand VARCHAR(100),
                color VARCHAR(50),
                size VARCHAR(50),
                material VARCHAR(100),
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                FOREIGN KEY (product_id) REFERENCES products(product_id) ON DELETE CASCADE
            )
        """;

        // Execute table creation
        stmt.executeUpdate(createUsersTable);
        System.out.println("Users table created");
        
        stmt.executeUpdate(createCustomersTable);
        System.out.println("Customers table created");
        
        stmt.executeUpdate(createCashiersTable);
        System.out.println("Cashiers table created");
        
        stmt.executeUpdate(createProductsTable);
        System.out.println("Products table created");
        
        stmt.executeUpdate(createBooksTable);
        System.out.println("Books table created");
        
        stmt.executeUpdate(createStationaryTable);
        System.out.println("Stationary table created");
        
        // Create sales table
        String createSalesTable = """
            CREATE TABLE sales (
                sale_id INT AUTO_INCREMENT PRIMARY KEY,
                sale_number VARCHAR(50) UNIQUE NOT NULL,
                customer_id VARCHAR(50),
                customer_name VARCHAR(255),
                total_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00,
                discount DECIMAL(10,2) DEFAULT 0.00,
                final_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00,
                payment_method ENUM('CASH', 'CARD', 'DIGITAL') DEFAULT 'CASH',
                sale_status ENUM('PENDING', 'COMPLETED', 'CANCELLED', 'REFUNDED') DEFAULT 'PENDING',
                notes TEXT,
                sold_by INT NOT NULL,
                sold_by_name VARCHAR(255),
                sale_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                FOREIGN KEY (sold_by) REFERENCES users(user_id) ON DELETE RESTRICT
            )
        """;
        
        // Create sale_items table
        String createSaleItemsTable = """
            CREATE TABLE sale_items (
                sale_item_id INT AUTO_INCREMENT PRIMARY KEY,
                sale_id INT NOT NULL,
                product_id INT NOT NULL,
                product_name VARCHAR(255) NOT NULL,
                product_code VARCHAR(50),
                product_category ENUM('BOOK', 'STATIONARY') NOT NULL,
                unit_price DECIMAL(10,2) NOT NULL,
                quantity INT NOT NULL,
                sub_total DECIMAL(10,2) NOT NULL,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (sale_id) REFERENCES sales(sale_id) ON DELETE CASCADE,
                FOREIGN KEY (product_id) REFERENCES products(product_id) ON DELETE RESTRICT
            )
        """;
        
        stmt.executeUpdate(createSalesTable);
        System.out.println("Sales table created");
        
        stmt.executeUpdate(createSaleItemsTable);
        System.out.println("Sale_items table created");
    }

    public void destroy() {}
}