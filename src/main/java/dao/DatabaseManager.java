package dao;

import java.sql.*;

public class DatabaseManager {
    private static final String URL = "jdbc:mysql://localhost:3306/hotel_db";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "root";

    private static Connection connection;

    // Get database connection
    public static Connection getConnection() throws SQLException {
        try {
            if (connection == null || connection.isClosed()) {
                Class.forName("com.mysql.cj.jdbc.Driver");
                connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
            }
            return connection;
        } catch (ClassNotFoundException e) {
            throw new SQLException("MySQL Driver not found", e);
        }
    }

    // Close database connection
    public static void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            System.err.println("Error closing database connection: " + e.getMessage());
        }
    }

    // Initialize database tables
    public static void initializeDatabase() throws SQLException {
        createTables();
        insertSampleData();
    }

    // Create all necessary tables
    private static void createTables() throws SQLException {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            // Create Users table
            stmt.execute("CREATE TABLE IF NOT EXISTS users (" +
                    "id INT PRIMARY KEY AUTO_INCREMENT," +
                    "username VARCHAR(50) UNIQUE NOT NULL," +
                    "password VARCHAR(50) NOT NULL," +
                    "role VARCHAR(20) NOT NULL," +
                    "fullname VARCHAR(100))");

            // Create Rooms table
            stmt.execute("CREATE TABLE IF NOT EXISTS rooms (" +
                    "id INT PRIMARY KEY AUTO_INCREMENT," +
                    "room_number VARCHAR(10) UNIQUE NOT NULL," +
                    "type VARCHAR(20) NOT NULL," +
                    "price DOUBLE NOT NULL," +
                    "status VARCHAR(20) DEFAULT 'Available')");

            // Create Bookings table
            stmt.execute("CREATE TABLE IF NOT EXISTS bookings (" +
                    "id INT PRIMARY KEY AUTO_INCREMENT," +
                    "guest_id INT NOT NULL," +
                    "room_id INT NOT NULL," +
                    "check_in_date DATE NOT NULL," +
                    "check_out_date DATE NOT NULL," +
                    "total_price DOUBLE NOT NULL," +
                    "status VARCHAR(20) DEFAULT 'Booked'," +
                    "FOREIGN KEY (guest_id) REFERENCES users(id)," +
                    "FOREIGN KEY (room_id) REFERENCES rooms(id))");

            // Create Payments table
            stmt.execute("CREATE TABLE IF NOT EXISTS payments (" +
                    "id INT PRIMARY KEY AUTO_INCREMENT," +
                    "booking_id INT NOT NULL," +
                    "amount DOUBLE NOT NULL," +
                    "payment_date DATETIME NOT NULL," +
                    "method VARCHAR(20) NOT NULL," +
                    "FOREIGN KEY (booking_id) REFERENCES bookings(id))");
        }
    }

    // Insert sample data for testing
    private static void insertSampleData() throws SQLException {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            // Insert sample users
            stmt.execute("INSERT IGNORE INTO users (username, password, role, fullname) VALUES " +
                    "('admin', 'admin123', 'admin', 'Admin User')," +
                    "('staff', 'staff123', 'staff', 'Staff Member')," +
                    "('guest', 'guest123', 'guest', 'John Doe')");

            // Insert sample rooms
            stmt.execute("INSERT IGNORE INTO rooms (room_number, type, price) VALUES " +
                    "('101', 'Standard', 100.00)," +
                    "('102', 'Deluxe', 150.00)," +
                    "('201', 'Suite', 250.00)," +
                    "('103', 'Standard', 100.00)," +
                    "('202', 'Deluxe', 150.00)");
        }
    }

    // Utility method to check if database connection is healthy
    public static boolean isConnectionHealthy() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }

    // Get the last inserted ID
    public static int getLastInsertId(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT LAST_INSERT_ID()")) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        throw new SQLException("Failed to get last insert ID");
    }
}