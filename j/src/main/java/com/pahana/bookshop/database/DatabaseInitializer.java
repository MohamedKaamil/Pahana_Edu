package com.pahana.bookshop.database;

import java.sql.Connection;
import java.sql.Statement;
import java.sql.SQLException;

public class DatabaseInitializer {
    
    public static void initializeDatabase() {
        try {
            Connection connection = DatabaseConnection.getInstance().getConnection();
            Statement statement = connection.createStatement();
            
            createUsersTable(statement);
            createCustomersTable(statement);
            createItemsTable(statement);
            createBillsTable(statement);
            createBillItemsTable(statement);
            
            insertSampleData(statement);
            
            System.out.println("Database initialized successfully!");
            
        } catch (SQLException e) {
            System.err.println("Database initialization failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void createUsersTable(Statement statement) throws SQLException {
        String createUsersTable = """
            CREATE TABLE IF NOT EXISTS users (
                id INT PRIMARY KEY AUTO_INCREMENT,
                username VARCHAR(50) UNIQUE NOT NULL,
                password VARCHAR(255) NOT NULL,
                role VARCHAR(20) NOT NULL,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
        """;
        statement.executeUpdate(createUsersTable);
    }
    
    private static void createCustomersTable(Statement statement) throws SQLException {
        String createCustomersTable = """
            CREATE TABLE IF NOT EXISTS customers (
                account_number VARCHAR(20) PRIMARY KEY,
                name VARCHAR(100) NOT NULL,
                address TEXT,
                telephone VARCHAR(15),
                units_consumed INT DEFAULT 0,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
        """;
        statement.executeUpdate(createCustomersTable);
    }
    
    private static void createItemsTable(Statement statement) throws SQLException {
        String createItemsTable = """
            CREATE TABLE IF NOT EXISTS items (
                item_id INT PRIMARY KEY AUTO_INCREMENT,
                item_name VARCHAR(100) NOT NULL,
                description TEXT,
                price DECIMAL(10,2) NOT NULL,
                quantity INT NOT NULL DEFAULT 0,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
        """;
        statement.executeUpdate(createItemsTable);
    }
    
    private static void createBillsTable(Statement statement) throws SQLException {
        String createBillsTable = """
            CREATE TABLE IF NOT EXISTS bills (
                bill_id INT PRIMARY KEY AUTO_INCREMENT,
                account_number VARCHAR(20),
                bill_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                total_amount DECIMAL(10,2) NOT NULL,
                units_consumed INT DEFAULT 0,
                status VARCHAR(20) DEFAULT 'PENDING',
                FOREIGN KEY (account_number) REFERENCES customers(account_number)
            )
        """;
        statement.executeUpdate(createBillsTable);
    }
    
    private static void createBillItemsTable(Statement statement) throws SQLException {
        String createBillItemsTable = """
            CREATE TABLE IF NOT EXISTS bill_items (
                bill_id INT,
                item_id INT,
                quantity INT NOT NULL,
                unit_price DECIMAL(10,2) NOT NULL,
                total_price DECIMAL(10,2) NOT NULL,
                PRIMARY KEY (bill_id, item_id),
                FOREIGN KEY (bill_id) REFERENCES bills(bill_id),
                FOREIGN KEY (item_id) REFERENCES items(item_id)
            )
        """;
        statement.executeUpdate(createBillItemsTable);
    }
    
    private static void insertSampleData(Statement statement) throws SQLException {
        String insertUsers = """
            INSERT IGNORE INTO users (username, password, role) VALUES 
            ('admin', 'admin123', 'ADMIN'),
            ('manager', 'manager123', 'MANAGER'),
            ('cashier', 'cashier123', 'CASHIER')
        """;
        statement.executeUpdate(insertUsers);
        
        String insertCustomers = """
            INSERT IGNORE INTO customers (account_number, name, address, telephone, units_consumed) VALUES 
            ('CUST001', 'John Doe', '123 Main St, City', '123-456-7890', 150),
            ('CUST002', 'Jane Smith', '456 Oak Ave, Town', '098-765-4321', 200),
            ('CUST003', 'Bob Johnson', '789 Pine Rd, Village', '555-123-4567', 100)
        """;
        statement.executeUpdate(insertCustomers);
        
        String insertItems = """
            INSERT IGNORE INTO items (item_name, description, price, quantity) VALUES 
            ('Programming Book', 'Learn Java Programming', 29.99, 50),
            ('Database Guide', 'Complete SQL Reference', 39.99, 30),
            ('Web Development', 'HTML, CSS, JavaScript', 24.99, 40),
            ('Software Engineering', 'Best Practices Guide', 49.99, 25),
            ('Data Structures', 'Algorithms and Implementation', 34.99, 35)
        """;
        statement.executeUpdate(insertItems);
    }
}