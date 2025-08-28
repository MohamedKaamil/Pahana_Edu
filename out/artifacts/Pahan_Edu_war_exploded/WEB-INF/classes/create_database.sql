-- Create Database
CREATE DATABASE IF NOT EXISTS pahana_billing_db;
USE pahana_billing_db;

-- Create Users table
CREATE TABLE IF NOT EXISTS users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create Customers table
CREATE TABLE IF NOT EXISTS customers (
    account_number VARCHAR(20) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    address TEXT,
    telephone VARCHAR(20),
    units_consumed INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create Items table
CREATE TABLE IF NOT EXISTS items (
    item_id INT AUTO_INCREMENT PRIMARY KEY,
    item_name VARCHAR(100) NOT NULL,
    description TEXT,
    price DECIMAL(10, 2) NOT NULL,
    quantity INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create Bills table
CREATE TABLE IF NOT EXISTS bills (
    bill_id INT AUTO_INCREMENT PRIMARY KEY,
    account_number VARCHAR(20),
    bill_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    total_amount DECIMAL(10, 2) NOT NULL,
    units_consumed INT DEFAULT 0,
    status VARCHAR(20) DEFAULT 'PENDING',
    FOREIGN KEY (account_number) REFERENCES customers(account_number)
);

-- Create Bill Items table
CREATE TABLE IF NOT EXISTS bill_items (
    bill_id INT,
    item_id INT,
    quantity INT NOT NULL,
    unit_price DECIMAL(10, 2) NOT NULL,
    total_price DECIMAL(10, 2) NOT NULL,
    PRIMARY KEY (bill_id, item_id),
    FOREIGN KEY (bill_id) REFERENCES bills(bill_id) ON DELETE CASCADE,
    FOREIGN KEY (item_id) REFERENCES items(item_id)
);

-- Insert default admin user (password: admin123)
INSERT INTO users (username, password, role) VALUES 
('admin', 'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f', 'admin')
ON DUPLICATE KEY UPDATE username = username;

-- Insert sample customers
INSERT INTO customers (account_number, name, address, telephone, units_consumed) VALUES 
('CUST001', 'John Doe', '123 Main St, City', '555-1234', 50),
('CUST002', 'Jane Smith', '456 Oak Ave, Town', '555-5678', 75),
('CUST003', 'Bob Johnson', '789 Pine Rd, Village', '555-9012', 30)
ON DUPLICATE KEY UPDATE name = VALUES(name);

-- Insert sample items
INSERT INTO items (item_name, description, price, quantity) VALUES 
('Mathematics Textbook', 'Grade 10 Mathematics', 29.99, 100),
('Science Lab Kit', 'Complete chemistry set', 89.99, 25),
('Notebook Set', 'Pack of 5 ruled notebooks', 12.50, 200),
('Calculator', 'Scientific calculator', 45.00, 50),
('Art Supplies', 'Complete drawing set', 35.75, 75)
ON DUPLICATE KEY UPDATE item_name = VALUES(item_name);