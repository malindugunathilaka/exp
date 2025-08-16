package ui;
import service.BookingService;
import model.Booking;
import javax.swing.*;
import java.awt.*;

public class CheckInOutPanel extends JPanel {
    private BookingService bookingService;
    private JTextField bookingIdField;
    private JComboBox<String> actionCombo;
    private JTextField roomField;
    private JTextField guestField;
    private JTextField checkInDateField;
    private JTextField checkOutDateField;
    private JTextField statusField;
    private JButton loadButton, processButton, clearButton;

    private Booking currentBooking;

    public CheckInOutPanel() {
        this.bookingService = new BookingService();
        initializeUI();
    }

    private void initializeUI() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Create title
        JLabel titleLabel = new JLabel("Check-In / Check-Out Management");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        add(titleLabel, BorderLayout.NORTH);

        // Create main form panel
        JPanel mainPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;

        // Booking ID input section
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        JPanel inputPanel = createInputPanel();
        mainPanel.add(inputPanel, gbc);

        // Separator
        gbc.gridy = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        JSeparator separator = new JSeparator();
        separator.setPreferredSize(new Dimension(400, 1));
        mainPanel.add(separator, gbc);

        // Booking details section
        gbc.gridy = 2; gbc.fill = GridBagConstraints.NONE;
        JPanel detailsPanel = createDetailsPanel();
        mainPanel.add(detailsPanel, gbc);

        add(mainPanel, BorderLayout.CENTER);

        // Action buttons panel
        JPanel bottomPanel = createBottomPanel();
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private JPanel createInputPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Search Booking"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        // Booking ID
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Booking ID:"), gbc);
        gbc.gridx = 1;
        bookingIdField = new JTextField(15);
        bookingIdField.setFont(new Font("Arial", Font.PLAIN, 12));
        panel.add(bookingIdField, gbc);

        gbc.gridx = 2;
        loadButton = new JButton("Load");
        loadButton.setPreferredSize(new Dimension(80, 25));
        loadButton.addActionListener(e -> loadBookingDetails());
        panel.add(loadButton, gbc);

        // Action type
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Action:"), gbc);
        gbc.gridx = 1;
        actionCombo = new JComboBox<>(new String[]{"Check In", "Check Out"});
        actionCombo.setPreferredSize(new Dimension(120, 25));
        panel.add(actionCombo, gbc);

        return panel;
    }

    private JPanel createDetailsPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Booking Details"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        // Room Number
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Room Number:"), gbc);
        gbc.gridx = 1;
        roomField = new JTextField(15);
        roomField.setEditable(false);
        roomField.setBackground(new Color(240, 240, 240));
        panel.add(roomField, gbc);

        // Guest Name
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Guest Name:"), gbc);
        gbc.gridx = 1;
        guestField = new JTextField(15);
        guestField.setEditable(false);
        guestField.setBackground(new Color(240, 240, 240));
        panel.add(guestField, gbc);

        // Check-in Date
        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(new JLabel("Check-in Date:"), gbc);
        gbc.gridx = 1;
        checkInDateField = new JTextField(15);
        checkInDateField.setEditable(false);
        checkInDateField.setBackground(new Color(240, 240, 240));
        panel.add(checkInDateField, gbc);

        // Check-out Date
        gbc.gridx = 0; gbc.gridy = 3;
        panel.add(new JLabel("Check-out Date:"), gbc);
        gbc.gridx = 1;
        checkOutDateField = new JTextField(15);
        checkOutDateField.setEditable(false);
        checkOutDateField.setBackground(new Color(240, 240, 240));
        panel.add(checkOutDateField, gbc);

        // Current Status
        gbc.gridx = 0; gbc.gridy = 4;
        panel.add(new JLabel("Current Status:"), gbc);
        gbc.gridx = 1;
        statusField = new JTextField(15);
        statusField.setEditable(false);
        statusField.setBackground(new Color(240, 240, 240));
        statusField.setFont(new Font("Arial", Font.BOLD, 12));
        panel.add(statusField, gbc);

        return panel;
    }

    private JPanel createBottomPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));

        processButton = new JButton("Process");
        processButton.setPreferredSize(new Dimension(100, 35));
        processButton.setBackground(new Color(46, 204, 113));
        processButton.setForeground(Color.WHITE);
        processButton.setFocusPainted(false);
        processButton.setEnabled(false);
        processButton.addActionListener(e -> processAction());

        clearButton = new JButton("Clear");
        clearButton.setPreferredSize(new Dimension(100, 35));
        clearButton.setBackground(new Color(149, 165, 166));
        clearButton.setForeground(Color.WHITE);
        clearButton.setFocusPainted(false);
        clearButton.addActionListener(e -> clearFields());

        panel.add(processButton);
        panel.add(clearButton);

        return panel;
    }

    private void loadBookingDetails() {
        String bookingIdText = bookingIdField.getText().trim();

        if (bookingIdText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a booking ID",
                    "Input Required", JOptionPane.WARNING_MESSAGE);
            bookingIdField.requestFocus();
            return;
        }

        try {
            int bookingId = Integer.parseInt(bookingIdText);
            currentBooking = bookingService.getBookingById(bookingId);

            if (currentBooking != null) {
                populateBookingDetails(currentBooking);
                updateActionAvailability();
                processButton.setEnabled(true);
            } else {
                JOptionPane.showMessageDialog(this, "Booking not found with ID: " + bookingId,
                        "Booking Not Found", JOptionPane.ERROR_MESSAGE);
                clearBookingDetails();
            }

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter a valid booking ID (numbers only)",
                    "Invalid Input", JOptionPane.ERROR_MESSAGE);
            bookingIdField.requestFocus();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading booking: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            clearBookingDetails();
        }
    }

    private void populateBookingDetails(Booking booking) {
        roomField.setText(booking.getRoomNumber());
        guestField.setText(booking.getGuestName());
        checkInDateField.setText(booking.getCheckInDate().toString());
        checkOutDateField.setText(booking.getCheckOutDate().toString());
        statusField.setText(booking.getStatus());

        // Set status field color based on status
        switch (booking.getStatus()) {
            case "Booked":
                statusField.setForeground(new Color(241, 196, 15)); // Yellow
                break;
            case "Checked In":
                statusField.setForeground(new Color(46, 204, 113)); // Green
                break;
            case "Checked Out":
                statusField.setForeground(new Color(149, 165, 166)); // Gray
                break;
            case "Cancelled":
                statusField.setForeground(new Color(231, 76, 60)); // Red
                break;
            default:
                statusField.setForeground(Color.BLACK);
        }
    }

    private void updateActionAvailability() {
        if (currentBooking == null) {
            actionCombo.setEnabled(false);
            return;
        }

        String status = currentBooking.getStatus();
        actionCombo.removeAllItems();

        switch (status) {
            case "Booked":
                actionCombo.addItem("Check In");
                actionCombo.setEnabled(true);
                break;
            case "Checked In":
                actionCombo.addItem("Check Out");
                actionCombo.setEnabled(true);
                break;
            case "Checked Out":
            case "Cancelled":
                actionCombo.addItem("No actions available");
                actionCombo.setEnabled(false);
                processButton.setEnabled(false);
                break;
            default:
                actionCombo.addItem("Unknown status");
                actionCombo.setEnabled(false);
                processButton.setEnabled(false);
        }
    }

    private void processAction() {
        if (currentBooking == null) {
            JOptionPane.showMessageDialog(this, "No booking loaded",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String action = (String) actionCombo.getSelectedItem();
        if (action == null || action.equals("No actions available") || action.equals("Unknown status")) {
            JOptionPane.showMessageDialog(this, "No valid action selected",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Confirm action
        int result = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to " + action.toLowerCase() + " for:\n\n" +
                        "Booking ID: " + currentBooking.getId() + "\n" +
                        "Guest: " + currentBooking.getGuestName() + "\n" +
                        "Room: " + currentBooking.getRoomNumber(),
                "Confirm " + action,
                JOptionPane.YES_NO_OPTION);

        if (result == JOptionPane.YES_OPTION) {
            boolean success = false;

            try {
                if ("Check In".equals(action)) {
                    success = bookingService.checkInGuest(currentBooking.getId());
                } else if ("Check Out".equals(action)) {
                    success = bookingService.checkOutGuest(currentBooking.getId());
                }

                if (success) {
                    JOptionPane.showMessageDialog(this,
                            action + " processed successfully!",
                            "Success", JOptionPane.INFORMATION_MESSAGE);

                    // Reload booking details to show updated status
                    loadBookingDetails();
                } else {
                    JOptionPane.showMessageDialog(this,
                            "Failed to process " + action.toLowerCase() + ". Please try again.",
                            "Error", JOptionPane.ERROR_MESSAGE);
                }

            } catch (Exception e) {
                JOptionPane.showMessageDialog(this,
                        "Error processing " + action.toLowerCase() + ": " + e.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void clearFields() {
        bookingIdField.setText("");
        clearBookingDetails();
        currentBooking = null;
        actionCombo.removeAllItems();
        actionCombo.addItem("Check In");
        actionCombo.addItem("Check Out");
        actionCombo.setEnabled(true);
        processButton.setEnabled(false);
        bookingIdField.requestFocus();
    }

    private void clearBookingDetails() {
        roomField.setText("");
        guestField.setText("");
        checkInDateField.setText("");
        checkOutDateField.setText("");
        statusField.setText("");
        statusField.setForeground(Color.BLACK);
        processButton.setEnabled(false);
    }

    // Method to pre-fill booking ID (useful for integration with other panels)
    public void setBookingId(int bookingId) {
        bookingIdField.setText(String.valueOf(bookingId));
        loadBookingDetails();
    }
}
