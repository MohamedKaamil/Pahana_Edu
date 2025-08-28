package com.pahana.bookshop.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.io.InputStream;
import java.io.IOException;

public class DatabaseConnection {
    private static DatabaseConnection instance;
    private Connection connection;
    private String url;
    private String username;
    private String password;

    private DatabaseConnection() throws SQLException {
        try {
            loadDatabaseProperties();
            Class.forName("com.mysql.cj.jdbc.Driver");
            this.connection = DriverManager.getConnection(url, username, password);
        } catch (ClassNotFoundException ex) {
            System.err.println("Database Connection Creation Failed : " + ex.getMessage());
            throw new SQLException("MySQL JDBC Driver not found", ex);
        } catch (IOException ex) {
            System.err.println("Failed to load database properties : " + ex.getMessage());
            throw new SQLException("Failed to load database configuration", ex);
        }
    }

    private void loadDatabaseProperties() throws IOException {
        Properties props = new Properties();
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("db.properties")) {
            if (input == null) {
                throw new IOException("Unable to find db.properties");
            }
            props.load(input);
            
            this.url = props.getProperty("db.url", "jdbc:mysql://localhost:3306/bookshop");
            this.username = props.getProperty("db.user", "root");
            this.password = props.getProperty("db.password", "");
        }
    }

    public Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                synchronized (DatabaseConnection.class) {
                    if (connection == null || connection.isClosed()) {
                        connection = DriverManager.getConnection(url, username, password);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Failed to get database connection: " + e.getMessage());
        }
        return connection;
    }

    public static DatabaseConnection getInstance() throws SQLException {
        if (instance == null) {
            synchronized (DatabaseConnection.class) {
                if (instance == null) {
                    instance = new DatabaseConnection();
                }
            }
        }
        return instance;
    }

    public void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                connection = null;
            } catch (SQLException e) {
                System.err.println("Failed to close database connection: " + e.getMessage());
            }
        }
    }

    @Override
    protected void finalize() throws Throwable {
        closeConnection();
        super.finalize();
    }
}