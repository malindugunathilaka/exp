package dao;

import model.Payment;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Date;

public class PaymentDAO {

    // Save new payment to database
    public int savePayment(Payment payment) throws SQLException {
        String sql = "INSERT INTO payments (booking_id, amount, payment_date, method) VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setInt(1, payment.getBookingId());
            pstmt.setDouble(2, payment.getAmount());

            // If payment date is null, use current timestamp
            if (payment.getPaymentDate() != null) {
                pstmt.setTimestamp(3, new Timestamp(payment.getPaymentDate().getTime()));
            } else {
                pstmt.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
            }

            pstmt.setString(4, payment.getMethod());

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int paymentId = generatedKeys.getInt(1);
                        payment.setId(paymentId);
                        return paymentId;
                    }
                }
            }
        }
        throw new SQLException("Failed to save payment");
    }

    // Create payment with current timestamp
    public int createPayment(int bookingId, double amount, String method) throws SQLException {
        String sql = "INSERT INTO payments (booking_id, amount, payment_date, method) VALUES (?, ?, NOW(), ?)";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setInt(1, bookingId);
            pstmt.setDouble(2, amount);
            pstmt.setString(3, method);

            pstmt.executeUpdate();

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                }
            }
        }
        throw new SQLException("Failed to create payment");
    }

    // Get payment by ID
    public Payment getPaymentById(int id) throws SQLException {
        String sql = "SELECT * FROM payments WHERE id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToPayment(rs);
                }
            }
        }
        return null;
    }

    // Get all payments
    public List<Payment> getAllPayments() throws SQLException {
        List<Payment> payments = new ArrayList<>();
        String sql = "SELECT * FROM payments ORDER BY payment_date DESC";

        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                payments.add(mapResultSetToPayment(rs));
            }
        }
        return payments;
    }

    // Get payments by booking ID
    public List<Payment> getPaymentsByBookingId(int bookingId) throws SQLException {
        List<Payment> payments = new ArrayList<>();
        String sql = "SELECT * FROM payments WHERE booking_id = ? ORDER BY payment_date DESC";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, bookingId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    payments.add(mapResultSetToPayment(rs));
                }
            }
        }
        return payments;
    }

    // Get payments by method
    public List<Payment> getPaymentsByMethod(String method) throws SQLException {
        List<Payment> payments = new ArrayList<>();
        String sql = "SELECT * FROM payments WHERE method = ? ORDER BY payment_date DESC";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, method);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    payments.add(mapResultSetToPayment(rs));
                }
            }
        }
        return payments;
    }

    // Get payments within date range
    public List<Payment> getPaymentsByDateRange(Date startDate, Date endDate) throws SQLException {
        List<Payment> payments = new ArrayList<>();
        String sql = "SELECT * FROM payments WHERE DATE(payment_date) BETWEEN ? AND ? ORDER BY payment_date DESC";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setDate(1, new java.sql.Date(startDate.getTime()));
            pstmt.setDate(2, new java.sql.Date(endDate.getTime()));

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    payments.add(mapResultSetToPayment(rs));
                }
            }
        }
        return payments;
    }

    // Update payment information
    public boolean updatePayment(Payment payment) throws SQLException {
        String sql = "UPDATE payments SET booking_id = ?, amount = ?, payment_date = ?, method = ? WHERE id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, payment.getBookingId());
            pstmt.setDouble(2, payment.getAmount());
            pstmt.setTimestamp(3, new Timestamp(payment.getPaymentDate().getTime()));
            pstmt.setString(4, payment.getMethod());
            pstmt.setInt(5, payment.getId());

            return pstmt.executeUpdate() > 0;
        }
    }

    // Delete payment by ID
    public boolean deletePayment(int id) throws SQLException {
        String sql = "DELETE FROM payments WHERE id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        }
    }

    // Get total revenue
    public double getTotalRevenue() throws SQLException {
        String sql = "SELECT SUM(amount) FROM payments";

        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getDouble(1);
            }
        }
        return 0.0;
    }

    // Get revenue by month
    public Map<String, Double> getRevenueByMonth() throws SQLException {
        Map<String, Double> revenue = new HashMap<>();
        String sql = "SELECT DATE_FORMAT(payment_date, '%Y-%m') AS month, SUM(amount) AS total " +
                "FROM payments GROUP BY month ORDER BY month";

        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                revenue.put(rs.getString("month"), rs.getDouble("total"));
            }
        }
        return revenue;
    }

    // Get revenue by payment method
    public Map<String, Double> getRevenueByMethod() throws SQLException {
        Map<String, Double> revenue = new HashMap<>();
        String sql = "SELECT method, SUM(amount) AS total FROM payments GROUP BY method";

        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                revenue.put(rs.getString("method"), rs.getDouble("total"));
            }
        }
        return revenue;
    }

    // Get today's revenue
    public double getTodayRevenue() throws SQLException {
        String sql = "SELECT SUM(amount) FROM payments WHERE DATE(payment_date) = CURDATE()";

        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getDouble(1);
            }
        }
        return 0.0;
    }

    // Get this month's revenue
    public double getThisMonthRevenue() throws SQLException {
        String sql = "SELECT SUM(amount) FROM payments " +
                "WHERE YEAR(payment_date) = YEAR(CURDATE()) " +
                "AND MONTH(payment_date) = MONTH(CURDATE())";

        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getDouble(1);
            }
        }
        return 0.0;
    }

    // Get total payment count
    public int getTotalPaymentCount() throws SQLException {
        String sql = "SELECT COUNT(*) FROM payments";

        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    // Get payment count by method
    public Map<String, Integer> getPaymentCountByMethod() throws SQLException {
        Map<String, Integer> counts = new HashMap<>();
        String sql = "SELECT method, COUNT(*) AS count FROM payments GROUP BY method";

        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                counts.put(rs.getString("method"), rs.getInt("count"));
            }
        }
        return counts;
    }

    // Helper method to map ResultSet to Payment object
    private Payment mapResultSetToPayment(ResultSet rs) throws SQLException {
        Payment payment = new Payment();
        payment.setId(rs.getInt("id"));
        payment.setBookingId(rs.getInt("booking_id"));
        payment.setAmount(rs.getDouble("amount"));
        payment.setPaymentDate(rs.getTimestamp("payment_date"));
        payment.setMethod(rs.getString("method"));
        return payment;
    }
}