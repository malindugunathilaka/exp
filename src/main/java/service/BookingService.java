package service;

import dao.BookingDAO;
import dao.RoomDAO;
import dao.UserDAO;
import dao.PaymentDAO;
import model.Booking;
import model.Room;
import model.User;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

public class BookingService {
    private BookingDAO bookingDAO;
    private RoomDAO roomDAO;
    private UserDAO userDAO;
    private PaymentDAO paymentDAO;

    public BookingService() {
        this.bookingDAO = new BookingDAO();
        this.roomDAO = new RoomDAO();
        this.userDAO = new UserDAO();
        this.paymentDAO = new PaymentDAO();
    }

    // Constructor with dependency injection for testing
    public BookingService(BookingDAO bookingDAO, RoomDAO roomDAO, UserDAO userDAO, PaymentDAO paymentDAO) {
        this.bookingDAO = bookingDAO;
        this.roomDAO = roomDAO;
        this.userDAO = userDAO;
        this.paymentDAO = paymentDAO;
    }

    /**
     * Create a new booking
     * @param guestUsername Username of the guest
     * @param roomNumber Room number to book
     * @param checkInDate Check-in date
     * @param checkOutDate Check-out date
     * @param paymentMethod Payment method
     * @return BookingResult containing booking information and status
     */
    public BookingResult createBooking(String guestUsername, String roomNumber,
                                       Date checkInDate, Date checkOutDate, String paymentMethod) {
        try {
            // Validate input
            BookingValidation validation = validateBookingData(guestUsername, roomNumber,
                    checkInDate, checkOutDate);
            if (!validation.isValid()) {
                return new BookingResult(false, validation.getMessage(), null);
            }

            // Get guest and room information
            User guest = userDAO.getUserByUsername(guestUsername);
            if (guest == null) {
                return new BookingResult(false, "Guest not found", null);
            }

            Room room = roomDAO.getRoomByNumber(roomNumber);
            if (room == null) {
                return new BookingResult(false, "Room not found", null);
            }

            if (!room.isAvailable()) {
                return new BookingResult(false, "Room is not available", null);
            }

            // Check for conflicting bookings
            if (bookingDAO.hasConflictingBooking(room.getId(), checkInDate, checkOutDate, 0)) {
                return new BookingResult(false, "Room is already booked for the selected dates", null);
            }

            // Calculate total price
            double totalPrice = calculateTotalPrice(room.getPrice(), checkInDate, checkOutDate);

            // Create booking
            Booking booking = new Booking(guest.getId(), room.getId(), checkInDate, checkOutDate, totalPrice);
            int bookingId = bookingDAO.saveBooking(booking);

            // Create payment record
            paymentDAO.createPayment(bookingId, totalPrice, paymentMethod);

            // Update room status
            roomDAO.updateRoomStatus(room.getId(), "Booked");

            // Retrieve the complete booking with joined data
            Booking completedBooking = bookingDAO.getBookingById(bookingId);

            return new BookingResult(true, "Booking created successfully", completedBooking);

        } catch (SQLException e) {
            System.err.println("Database error creating booking: " + e.getMessage());
            return new BookingResult(false, "Database error: " + e.getMessage(), null);
        } catch (Exception e) {
            System.err.println("Unexpected error creating booking: " + e.getMessage());
            return new BookingResult(false, "Unexpected error: " + e.getMessage(), null);
        }
    }

    /**
     * Update booking status
     * @param bookingId ID of the booking to update
     * @param newStatus New status for the booking
     * @return true if update successful, false otherwise
     */
    public boolean updateBookingStatus(int bookingId, String newStatus) {
        try {
            return bookingDAO.updateBookingStatus(bookingId, newStatus);
        } catch (SQLException e) {
            System.err.println("Error updating booking status: " + e.getMessage());
            return false;
        }
    }

    /**
     * Check in a guest
     * @param bookingId ID of the booking
     * @return true if check-in successful, false otherwise
     */
    public boolean checkInGuest(int bookingId) {
        try {
            Booking booking = bookingDAO.getBookingById(bookingId);
            if (booking == null) {
                return false;
            }

            if (!"Booked".equals(booking.getStatus())) {
                return false;
            }

            return bookingDAO.updateBookingStatus(bookingId, "Checked In");
        } catch (SQLException e) {
            System.err.println("Error checking in guest: " + e.getMessage());
            return false;
        }
    }

    /**
     * Check out a guest
     * @param bookingId ID of the booking
     * @return true if check-out successful, false otherwise
     */
    public boolean checkOutGuest(int bookingId) {
        try {
            Booking booking = bookingDAO.getBookingById(bookingId);
            if (booking == null) {
                return false;
            }

            if (!"Checked In".equals(booking.getStatus())) {
                return false;
            }

            boolean statusUpdated = bookingDAO.updateBookingStatus(bookingId, "Checked Out");

            if (statusUpdated) {
                // Update room status to available
                roomDAO.updateRoomStatus(booking.getRoomId(), "Available");
            }

            return statusUpdated;
        } catch (SQLException e) {
            System.err.println("Error checking out guest: " + e.getMessage());
            return false;
        }
    }

    /**
     * Cancel a booking
     * @param bookingId ID of the booking to cancel
     * @return true if cancellation successful, false otherwise
     */
    public boolean cancelBooking(int bookingId) {
        try {
            Booking booking = bookingDAO.getBookingById(bookingId);
            if (booking == null) {
                return false;
            }

            if ("Cancelled".equals(booking.getStatus()) || "Checked Out".equals(booking.getStatus())) {
                return false; // Already cancelled or completed
            }

            boolean statusUpdated = bookingDAO.updateBookingStatus(bookingId, "Cancelled");

            if (statusUpdated && "Booked".equals(booking.getStatus())) {
                // If booking was only booked (not checked in), make room available
                roomDAO.updateRoomStatus(booking.getRoomId(), "Available");
            }

            return statusUpdated;
        } catch (SQLException e) {
            System.err.println("Error cancelling booking: " + e.getMessage());
            return false;
        }
    }

    /**
     * Get all bookings
     * @return List of all bookings
     */
    public List<Booking> getAllBookings() {
        try {
            return bookingDAO.getAllBookings();
        } catch (SQLException e) {
            System.err.println("Error retrieving bookings: " + e.getMessage());
            return List.of(); // Return empty list
        }
    }

    /**
     * Get bookings for a specific guest
     * @param username Guest's username
     * @return List of bookings for the guest
     */
    public List<Booking> getGuestBookings(String username) {
        try {
            return bookingDAO.getBookingsByUsername(username);
        } catch (SQLException e) {
            System.err.println("Error retrieving guest bookings: " + e.getMessage());
            return List.of(); // Return empty list
        }
    }

    /**
     * Get booking by ID
     * @param bookingId ID of the booking
     * @return Booking object if found, null otherwise
     */
    public Booking getBookingById(int bookingId) {
        try {
            return bookingDAO.getBookingById(bookingId);
        } catch (SQLException e) {
            System.err.println("Error retrieving booking: " + e.getMessage());
            return null;
        }
    }

    /**
     * Get today's check-ins
     * @return List of bookings with check-in today
     */
    public List<Booking> getTodayCheckIns() {
        try {
            return bookingDAO.getTodayCheckIns();
        } catch (SQLException e) {
            System.err.println("Error retrieving today's check-ins: " + e.getMessage());
            return List.of();
        }
    }

    /**
     * Get today's check-outs
     * @return List of bookings with check-out today
     */
    public List<Booking> getTodayCheckOuts() {
        try {
            return bookingDAO.getTodayCheckOuts();
        } catch (SQLException e) {
            System.err.println("Error retrieving today's check-outs: " + e.getMessage());
            return List.of();
        }
    }

    /**
     * Calculate total price for a booking
     * @param pricePerNight Price per night
     * @param checkIn Check-in date
     * @param checkOut Check-out date
     * @return Total price for the stay
     */
    public double calculateTotalPrice(double pricePerNight, Date checkIn, Date checkOut) {
        if (checkIn == null || checkOut == null || checkOut.before(checkIn)) {
            return 0.0;
        }

        long diffInMillies = checkOut.getTime() - checkIn.getTime();
        long nights = diffInMillies / (1000 * 60 * 60 * 24);

        if (nights <= 0) {
            nights = 1; // Minimum 1 night
        }

        return pricePerNight * nights;
    }

    /**
     * Validate booking data
     * @param guestUsername Guest username
     * @param roomNumber Room number
     * @param checkIn Check-in date
     * @param checkOut Check-out date
     * @return BookingValidation result
     */
    private BookingValidation validateBookingData(String guestUsername, String roomNumber,
                                                  Date checkIn, Date checkOut) {
        if (guestUsername == null || guestUsername.trim().isEmpty()) {
            return new BookingValidation(false, "Guest username is required");
        }

        if (roomNumber == null || roomNumber.trim().isEmpty()) {
            return new BookingValidation(false, "Room number is required");
        }

        if (checkIn == null) {
            return new BookingValidation(false, "Check-in date is required");
        }

        if (checkOut == null) {
            return new BookingValidation(false, "Check-out date is required");
        }

        if (checkOut.before(checkIn)) {
            return new BookingValidation(false, "Check-out date must be after check-in date");
        }

        Date today = new Date();
        if (checkIn.before(today)) {
            return new BookingValidation(false, "Check-in date cannot be in the past");
        }

        return new BookingValidation(true, "Booking data is valid");
    }

    /**
     * Inner class for booking results
     */
    public static class BookingResult {
        private boolean success;
        private String message;
        private Booking booking;

        public BookingResult(boolean success, String message, Booking booking) {
            this.success = success;
            this.message = message;
            this.booking = booking;
        }

        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public Booking getBooking() { return booking; }
    }

    /**
     * Inner class for booking validation
     */
    private static class BookingValidation {
        private boolean valid;
        private String message;

        public BookingValidation(boolean valid, String message) {
            this.valid = valid;
            this.message = message;
        }

        public boolean isValid() { return valid; }
        public String getMessage() { return message; }
    }
}