package ui;
import model.User;
import model.Room;
import dao.RoomDAO;
import service.BookingService;
import javax.swing.*;
import java.awt.*;
import java.util.Date;
import java.util.List;

public class BookingFormpanel extends JPanel {
    private User currentUser;
    private BookingService bookingService;
    private RoomDAO roomDAO;

    private JComboBox<String> roomCombo;
    private JSpinner checkInSpinner;
    private JSpinner checkOutSpinner;
    private JLabel totalLabel;
    private JComboBox<String> paymentMethodCombo;
    private JButton bookButton;
    private JTextArea roomDetailsArea;

    public BookingFormpanel(User currentUser) {
        this.currentUser = currentUser;
        this.bookingService = new BookingService();
        this.roomDAO = new RoomDAO();
        initializeUI();
        loadAvailableRooms();
    }

    private void initializeUI() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Create main form panel
        JPanel mainPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;

        // Title
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        JLabel titleLabel = new JLabel("Make a Reservation");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setForeground(new Color(52, 73, 94));
        mainPanel.add(titleLabel, gbc);

        gbc.gridwidth = 1;

        // Room selection
        gbc.gridx = 0; gbc.gridy = 1;
        mainPanel.add(new JLabel("Select Room:"), gbc);
        gbc.gridx = 1;
        roomCombo = new JComboBox<>();
        roomCombo.setPreferredSize(new Dimension(200, 25));
        roomCombo.addActionListener(e -> {
            updateRoomDetails();
            calculateTotal();
        });
        mainPanel.add(roomCombo, gbc);

        // Check-in date
        gbc.gridx = 0; gbc.gridy = 2;
        mainPanel.add(new JLabel("Check-in Date:"), gbc);
        gbc.gridx = 1;
        checkInSpinner = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor checkInEditor = new JSpinner.DateEditor(checkInSpinner, "MMM dd, yyyy");
        checkInSpinner.setEditor(checkInEditor);
        checkInSpinner.setValue(new Date()); // Default to today
        checkInSpinner.addChangeListener(e -> calculateTotal());
        mainPanel.add(checkInSpinner, gbc);

        // Check-out date
        gbc.gridx = 0; gbc.gridy = 3;
        mainPanel.add(new JLabel("Check-out Date:"), gbc);
        gbc.gridx = 1;
        checkOutSpinner = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor checkOutEditor = new JSpinner.DateEditor(checkOutSpinner, "MMM dd, yyyy");
        checkOutSpinner.setEditor(checkOutEditor);
        // Default to tomorrow
        checkOutSpinner.setValue(new Date(System.currentTimeMillis() + 24 * 60 * 60 * 1000));
        checkOutSpinner.addChangeListener(e -> calculateTotal());
        mainPanel.add(checkOutSpinner, gbc);

        // Total amount
        gbc.gridx = 0; gbc.gridy = 4;
        mainPanel.add(new JLabel("Total Amount:"), gbc);
        gbc.gridx = 1;
        totalLabel = new JLabel("$0.00");
        totalLabel.setFont(new Font("Arial", Font.BOLD, 16));
        totalLabel.setForeground(new Color(46, 204, 113));
        mainPanel.add(totalLabel, gbc);

        // Payment method
        gbc.gridx = 0; gbc.gridy = 5;
        mainPanel.add(new JLabel("Payment Method:"), gbc);
        gbc.gridx = 1;
        paymentMethodCombo = new JComboBox<>(new String[]{"Credit Card", "Cash", "Bank Transfer"});
        paymentMethodCombo.setPreferredSize(new Dimension(200, 25));
        mainPanel.add(paymentMethodCombo, gbc);

        // Book button
        gbc.gridx = 0; gbc.gridy = 6; gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(20, 10, 10, 10);
        bookButton = new JButton("Book Now");
        bookButton.setFont(new Font("Arial", Font.BOLD, 14));
        bookButton.setPreferredSize(new Dimension(150, 35));
        bookButton.setBackground(new Color(52, 152, 219));
        bookButton.setForeground(Color.WHITE);
        bookButton.setFocusPainted(false);
        bookButton.addActionListener(e -> processBooking());
        mainPanel.add(bookButton, gbc);

        add(mainPanel, BorderLayout.WEST);

        // Room details panel
        createRoomDetailsPanel();
    }

    private void createRoomDetailsPanel() {
        JPanel detailsPanel = new JPanel(new BorderLayout());
        detailsPanel.setBorder(BorderFactory.createTitledBorder("Room Details"));
        detailsPanel.setPreferredSize(new Dimension(300, 200));

        roomDetailsArea = new JTextArea(10, 25);
        roomDetailsArea.setEditable(false);
        roomDetailsArea.setBackground(new Color(248, 249, 250));
        roomDetailsArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        roomDetailsArea.setFont(new Font("Arial", Font.PLAIN, 12));

        JScrollPane scrollPane = new JScrollPane(roomDetailsArea);
        detailsPanel.add(scrollPane, BorderLayout.CENTER);

        add(detailsPanel, BorderLayout.CENTER);
    }

    private void loadAvailableRooms() {
        try {
            List<Room> availableRooms = roomDAO.getAvailableRooms();
            roomCombo.removeAllItems();

            if (availableRooms.isEmpty()) {
                roomCombo.addItem("No rooms available");
                bookButton.setEnabled(false);
            } else {
                for (Room room : availableRooms) {
                    roomCombo.addItem(room.getRoomNumber() + " - " + room.getType() + " ($" + room.getPrice() + ")");
                }
                updateRoomDetails();
                calculateTotal();
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading available rooms: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateRoomDetails() {
        String selectedItem = (String) roomCombo.getSelectedItem();
        if (selectedItem == null || selectedItem.equals("No rooms available")) {
            roomDetailsArea.setText("No room selected");
            return;
        }

        try {
            String roomNumber = selectedItem.split(" - ")[0];
            Room room = roomDAO.getRoomByNumber(roomNumber);

            if (room != null) {
                StringBuilder details = new StringBuilder();
                details.append("Room Number: ").append(room.getRoomNumber()).append("\n\n");
                details.append("Room Type: ").append(room.getType()).append("\n\n");
                details.append("Price per Night: $").append(String.format("%.2f", room.getPrice())).append("\n\n");
                details.append("Status: ").append(room.getStatus()).append("\n\n");

                // Add room type description
                details.append("Description:\n");
                switch (room.getType()) {
                    case "Standard":
                        details.append("• Single bed\n• Basic amenities\n• 20 sq.m\n• Free WiFi");
                        break;
                    case "Deluxe":
                        details.append("• Queen bed\n• Premium amenities\n• 35 sq.m\n• Free WiFi\n• Mini-bar\n• City view");
                        break;
                    case "Suite":
                        details.append("• King bed\n• Luxury amenities\n• 50 sq.m\n• Free WiFi\n• Mini-bar\n• Ocean view\n• Separate living area\n• Jacuzzi");
                        break;
                }

                roomDetailsArea.setText(details.toString());
            }

        } catch (Exception e) {
            roomDetailsArea.setText("Error loading room details: " + e.getMessage());
        }
    }

    private void calculateTotal() {
        if (checkInSpinner.getValue() == null || checkOutSpinner.getValue() == null) {
            totalLabel.setText("$0.00");
            return;
        }

        String selectedItem = (String) roomCombo.getSelectedItem();
        if (selectedItem == null || selectedItem.equals("No rooms available")) {
            totalLabel.setText("$0.00");
            return;
        }

        try {
            Date checkIn = (Date) checkInSpinner.getValue();
            Date checkOut = (Date) checkOutSpinner.getValue();

            if (checkOut.before(checkIn) || checkOut.equals(checkIn)) {
                totalLabel.setText("Invalid dates");
                totalLabel.setForeground(Color.RED);
                bookButton.setEnabled(false);
                return;
            }

            String roomNumber = selectedItem.split(" - ")[0];
            Room room = roomDAO.getRoomByNumber(roomNumber);

            if (room != null) {
                double total = bookingService.calculateTotalPrice(room.getPrice(), checkIn, checkOut);
                totalLabel.setText(String.format("$%.2f", total));
                totalLabel.setForeground(new Color(46, 204, 113));
                bookButton.setEnabled(true);
            }

        } catch (Exception e) {
            totalLabel.setText("Error calculating total");
            totalLabel.setForeground(Color.RED);
            bookButton.setEnabled(false);
        }
    }

    private void processBooking() {
        try {
            // Validate inputs
            String selectedItem = (String) roomCombo.getSelectedItem();
            if (selectedItem == null || selectedItem.equals("No rooms available")) {
                JOptionPane.showMessageDialog(this, "Please select a room.");
                return;
            }

            Date checkIn = (Date) checkInSpinner.getValue();
            Date checkOut = (Date) checkOutSpinner.getValue();

            if (checkIn == null || checkOut == null) {
                JOptionPane.showMessageDialog(this, "Please select check-in and check-out dates.");
                return;
            }

            if (checkOut.before(checkIn) || checkOut.equals(checkIn)) {
                JOptionPane.showMessageDialog(this, "Check-out date must be after check-in date.");
                return;
            }

            // Extract room number
            String roomNumber = selectedItem.split(" - ")[0];
            String paymentMethod = (String) paymentMethodCombo.getSelectedItem();

            // Show confirmation dialog
            int result = JOptionPane.showConfirmDialog(this,
                    "Confirm booking details:\n\n" +
                            "Room: " + roomNumber + "\n" +
                            "Check-in: " + checkIn + "\n" +
                            "Check-out: " + checkOut + "\n" +
                            "Total: " + totalLabel.getText() + "\n" +
                            "Payment: " + paymentMethod + "\n\n" +
                            "Proceed with booking?",
                    "Confirm Booking",
                    JOptionPane.YES_NO_OPTION);

            if (result == JOptionPane.YES_OPTION) {
                // Process booking
                BookingService.BookingResult bookingResult = bookingService.createBooking(
                        currentUser.getUsername(), roomNumber, checkIn, checkOut, paymentMethod);

                if (bookingResult.isSuccess()) {
                    JOptionPane.showMessageDialog(this,
                            "Booking created successfully!\n\nBooking ID: " + bookingResult.getBooking().getId(),
                            "Success", JOptionPane.INFORMATION_MESSAGE);

                    // Reset form
                    resetForm();
                } else {
                    JOptionPane.showMessageDialog(this,
                            "Booking failed: " + bookingResult.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error processing booking: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void resetForm() {
        loadAvailableRooms();
        checkInSpinner.setValue(new Date());
        checkOutSpinner.setValue(new Date(System.currentTimeMillis() + 24 * 60 * 60 * 1000));
        paymentMethodCombo.setSelectedIndex(0);
        calculateTotal();
    }

    public void refreshAvailableRooms() {
        loadAvailableRooms();
    }
}