package dao;

import model.Booking;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Date;

public class BookingDAO {

    // Save new booking to database
    public int saveBooking(Booking booking) throws SQLException {
        String sql = "INSERT INTO bookings (guest_id, room_id, check_in_date, check_out_date, total_price, status) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setInt(1, booking.getGuestId());
            pstmt.setInt(2, booking.getRoomId());
            pstmt.setDate(3, new java.sql.Date(booking.getCheckInDate().getTime()));
            pstmt.setDate(4, new java.sql.Date(booking.getCheckOutDate().getTime()));
            pstmt.setDouble(5, booking.getTotalPrice());
            pstmt.setString(6, booking.getStatus() != null ? booking.getStatus() : "Booked");

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int bookingId = generatedKeys.getInt(1);
                        booking.setId(bookingId);
                        return bookingId;
                    }
                }
            }
        }
        throw new SQLException("Failed to save booking");
    }

    // Get booking by ID
    public Booking getBookingById(int id) throws SQLException {
        String sql = "SELECT b.*, r.room_number, u.fullname as guest_name " +
                "FROM bookings b " +
                "JOIN rooms r ON b.room_id = r.id " +
                "JOIN users u ON b.guest_id = u.id " +
                "WHERE b.id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToBooking(rs);
                }
            }
        }
        return null;
    }

    // Get all bookings with room and guest information
    public List<Booking> getAllBookings() throws SQLException {
        List<Booking> bookings = new ArrayList<>();
        String sql = "SELECT b.*, r.room_number, u.fullname as guest_name " +
                "FROM bookings b " +
                "JOIN rooms r ON b.room_id = r.id " +
                "JOIN users u ON b.guest_id = u.id " +
                "ORDER BY b.id DESC";

        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                bookings.add(mapResultSetToBooking(rs));
            }
        }
        return bookings;
    }

    // Get bookings by guest ID
    public List<Booking> getBookingsByGuestId(int guestId) throws SQLException {
        List<Booking> bookings = new ArrayList<>();
        String sql = "SELECT b.*, r.room_number, u.fullname as guest_name " +
                "FROM bookings b " +
                "JOIN rooms r ON b.room_id = r.id " +
                "JOIN users u ON b.guest_id = u.id " +
                "WHERE b.guest_id = ? " +
                "ORDER BY b.check_in_date DESC";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, guestId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    bookings.add(mapResultSetToBooking(rs));
                }
            }
        }
        return bookings;
    }

    // Get bookings by guest username
    public List<Booking> getBookingsByUsername(String username) throws SQLException {
        List<Booking> bookings = new ArrayList<>();
        String sql = "SELECT b.*, r.room_number, u.fullname as guest_name " +
                "FROM bookings b " +
                "JOIN rooms r ON b.room_id = r.id " +
                "JOIN users u ON b.guest_id = u.id " +
                "WHERE u.username = ? " +
                "ORDER BY b.check_in_date DESC";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    bookings.add(mapResultSetToBooking(rs));
                }
            }
        }
        return bookings;
    }

    // Get bookings by status
    public List<Booking> getBookingsByStatus(String status) throws SQLException {
        List<Booking> bookings = new ArrayList<>();
        String sql = "SELECT b.*, r.room_number, u.fullname as guest_name " +
                "FROM bookings b " +
                "JOIN rooms r ON b.room_id = r.id " +
                "JOIN users u ON b.guest_id = u.id " +
                "WHERE b.status = ? " +
                "ORDER BY b.check_in_date";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, status);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    bookings.add(mapResultSetToBooking(rs));
                }
            }
        }
        return bookings;
    }

    // Update booking information
    public boolean updateBooking(Booking booking) throws SQLException {
        String sql = "UPDATE bookings SET guest_id = ?, room_id = ?, check_in_date = ?, " +
                "check_out_date = ?, total_price = ?, status = ? WHERE id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, booking.getGuestId());
            pstmt.setInt(2, booking.getRoomId());
            pstmt.setDate(3, new java.sql.Date(booking.getCheckInDate().getTime()));
            pstmt.setDate(4, new java.sql.Date(booking.getCheckOutDate().getTime()));
            pstmt.setDouble(5, booking.getTotalPrice());
            pstmt.setString(6, booking.getStatus());
            pstmt.setInt(7, booking.getId());

            return pstmt.executeUpdate() > 0;
        }
    }

    // Update booking status
    public boolean updateBookingStatus(int bookingId, String status) throws SQLException {
        String sql = "UPDATE bookings SET status = ? WHERE id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, status);
            pstmt.setInt(2, bookingId);

            return pstmt.executeUpdate() > 0;
        }
    }

    // Delete booking by ID
    public boolean deleteBooking(int id) throws SQLException {
        String sql = "DELETE FROM bookings WHERE id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        }
    }

    // Check for conflicting bookings
    public boolean hasConflictingBooking(int roomId, Date checkIn, Date checkOut, int excludeBookingId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM bookings WHERE room_id = ? AND status IN ('Booked', 'Checked In') " +
                "AND ((check_in_date <= ? AND check_out_date > ?) OR " +
                "(check_in_date < ? AND check_out_date >= ?) OR " +
                "(check_in_date >= ? AND check_out_date <= ?))";

        if (excludeBookingId > 0) {
            sql += " AND id != ?";
        }

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            java.sql.Date sqlCheckIn = new java.sql.Date(checkIn.getTime());
            java.sql.Date sqlCheckOut = new java.sql.Date(checkOut.getTime());

            pstmt.setInt(1, roomId);
            pstmt.setDate(2, sqlCheckOut);
            pstmt.setDate(3, sqlCheckIn);
            pstmt.setDate(4, sqlCheckOut);
            pstmt.setDate(5, sqlCheckIn);
            pstmt.setDate(6, sqlCheckIn);
            pstmt.setDate(7, sqlCheckOut);

            if (excludeBookingId > 0) {
                pstmt.setInt(8, excludeBookingId);
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    // Get total booking count
    public int getTotalBookingCount() throws SQLException {
        String sql = "SELECT COUNT(*) FROM bookings";

        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    // Get current active bookings
    public List<Booking> getActiveBookings() throws SQLException {
        return getBookingsByStatus("Checked In");
    }

    // Get today's check-ins
    public List<Booking> getTodayCheckIns() throws SQLException {
        List<Booking> bookings = new ArrayList<>();
        String sql = "SELECT b.*, r.room_number, u.fullname as guest_name " +
                "FROM bookings b " +
                "JOIN rooms r ON b.room_id = r.id " +
                "JOIN users u ON b.guest_id = u.id " +
                "WHERE DATE(b.check_in_date) = CURDATE() " +
                "AND b.status = 'Booked' " +
                "ORDER BY b.check_in_date";

        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                bookings.add(mapResultSetToBooking(rs));
            }
        }
        return bookings;
    }

    // Get today's check-outs
    public List<Booking> getTodayCheckOuts() throws SQLException {
        List<Booking> bookings = new ArrayList<>();
        String sql = "SELECT b.*, r.room_number, u.fullname as guest_name " +
                "FROM bookings b " +
                "JOIN rooms r ON b.room_id = r.id " +
                "JOIN users u ON b.guest_id = u.id " +
                "WHERE DATE(b.check_out_date) = CURDATE() " +
                "AND b.status = 'Checked In' " +
                "ORDER BY b.check_out_date";

        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                bookings.add(mapResultSetToBooking(rs));
            }
        }
        return bookings;
    }

    // Helper method to map ResultSet to Booking object
    private Booking mapResultSetToBooking(ResultSet rs) throws SQLException {
        Booking booking = new Booking();
        booking.setId(rs.getInt("id"));
        booking.setGuestId(rs.getInt("guest_id"));
        booking.setRoomId(rs.getInt("room_id"));
        booking.setCheckInDate(rs.getDate("check_in_date"));
        booking.setCheckOutDate(rs.getDate("check_out_date"));
        booking.setTotalPrice(rs.getDouble("total_price"));
        booking.setStatus(rs.getString("status"));

        // Set additional fields from joins
        booking.setRoomNumber(rs.getString("room_number"));
        booking.setGuestName(rs.getString("guest_name"));

        return booking;
    }
}