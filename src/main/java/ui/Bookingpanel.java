package ui;

import model.User;
import model.Booking;
import service.BookingService;
import service.AuthenticationService;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.util.List;

public class BookingsPanel extends JPanel {
    private User currentUser;
    private BookingService bookingService;
    private AuthenticationService authService;
    private DefaultTableModel tableModel;
    private JTable bookingTable;
    private JButton checkInButton, checkOutButton, cancelButton, refreshButton;
    private JComboBox<String> statusFilter;

    public BookingsPanel(User currentUser) {
        this.currentUser = currentUser;
        this.bookingService = new BookingService();
        this.authService = new AuthenticationService();
        initializeUI();
        loadBookingData();
    }

    private void initializeUI() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Create top panel with filters
        JPanel topPanel = createTopPanel();
        add(topPanel, BorderLayout.NORTH);

        // Create table
        createTable();
        JScrollPane scrollPane = new JScrollPane(bookingTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder(getTableTitle()));
        add(scrollPane, BorderLayout.CENTER);

        // Create bottom panel with action buttons
        JPanel bottomPanel = createBottomPanel();
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private String getTableTitle() {
        switch (currentUser.getRole()) {
            case "guest":
                return "My Bookings";
            case "staff":
            case "admin":
                return "All Bookings";
            default:
                return "Bookings";
        }
    }

    private JPanel createTopPanel() {
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

        // Status filter
        topPanel.add(new JLabel("Status:"));
        statusFilter = new JComboBox<>(new String[]{"All", "Booked", "Checked In", "Checked Out", "Cancelled"});
        statusFilter.addActionListener(e -> applyFilters());
        topPanel.add(statusFilter);

        topPanel.add(Box.createHorizontalStrut(20));

        // Refresh button
        refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> refreshData());
        topPanel.add(refreshButton);

        return topPanel;
    }

    private void createTable() {
        String[] columns;

        if ("guest".equals(currentUser.getRole())) {
            columns = new String[]{"ID", "Room Number", "Check-in Date", "Check-out Date", "Total Price", "Status"};
        } else {
            columns = new String[]{"ID", "Guest Name", "Room Number", "Check-in Date", "Check-out Date", "Total Price", "Status"};
        }

        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make table read-only
            }
        };

        bookingTable = new JTable(tableModel);
        bookingTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        bookingTable.setRowHeight(25);
        bookingTable.getTableHeader().setReorderingAllowed(false);

        // Hide ID column
        bookingTable.getColumnModel().getColumn(0).setMinWidth(0);
        bookingTable.getColumnModel().getColumn(0).setMaxWidth(0);
        bookingTable.getColumnModel().getColumn(0).setWidth(0);

        // Add row sorter
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(tableModel);
        bookingTable.setRowSorter(sorter);

        // Add selection listener for action buttons
        bookingTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                updateButtonStates();
            }
        });
    }

    private JPanel createBottomPanel() {
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        if (authService.canManageBookings(currentUser)) {
            // Staff and Admin buttons
            checkInButton = new JButton("Check In");
            checkInButton.setEnabled(false);
            checkInButton.addActionListener(e -> processCheckIn());
            bottomPanel.add(checkInButton);

            checkOutButton = new JButton("Check Out");
            checkOutButton.setEnabled(false);
            checkOutButton.addActionListener(e -> processCheckOut());
            bottomPanel.add(checkOutButton);
        }

        if (authService.isGuest(currentUser) || authService.canManageBookings(currentUser)) {
            // Cancel booking button (available for guests and staff/admin)
            cancelButton = new JButton(authService.isGuest(currentUser) ? "Cancel Booking" : "Cancel Booking");
            cancelButton.setEnabled(false);
            cancelButton.addActionListener(e -> cancelBooking());
            bottomPanel.add(cancelButton);
        }

        return bottomPanel;
    }

    public void loadBookingData() {
        SwingUtilities.invokeLater(() -> {
            try {
                List<Booking> bookings;

                if ("guest".equals(currentUser.getRole())) {
                    bookings = bookingService.getGuestBookings(currentUser.getUsername());
                } else {
                    bookings = bookingService.getAllBookings();
                }

                updateTableData(bookings);

            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error loading bookings: " + e.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    private void updateTableData(List<Booking> bookings) {
        tableModel.setRowCount(0); // Clear existing data

        for (Booking booking : bookings) {
            Object[] row;

            if ("guest".equals(currentUser.getRole())) {
                row = new Object[]{
                        booking.getId(),
                        booking.getRoomNumber(),
                        booking.getCheckInDate(),
                        booking.getCheckOutDate(),
                        String.format("$%.2f", booking.getTotalPrice()),
                        booking.getStatus()
                };
            } else {
                row = new Object[]{
                        booking.getId(),
                        booking.getGuestName(),
                        booking.getRoomNumber(),
                        booking.getCheckInDate(),
                        booking.getCheckOutDate(),
                        String.format("$%.2f", booking.getTotalPrice()),
                        booking.getStatus()
                };
            }

            tableModel.addRow(row);
        }
    }

    private void applyFilters() {
        String selectedStatus = (String) statusFilter.getSelectedItem();

        if ("All".equals(selectedStatus)) {
            loadBookingData();
            return;
        }

        try {
            List<Booking> bookings;

            if ("guest".equals(currentUser.getRole())) {
                bookings = bookingService.getGuestBookings(currentUser.getUsername());
            } else {
                bookings = bookingService.getAllBookings();
            }

            // Filter by status
            bookings = bookings.stream()
                    .filter(booking -> booking.getStatus().equals(selectedStatus))
                    .toList();

            updateTableData(bookings);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error applying filters: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateButtonStates() {
        int selectedRow = bookingTable.getSelectedRow();
        boolean hasSelection = selectedRow != -1;

        if (hasSelection) {
            int modelRow = bookingTable.convertRowIndexToModel(selectedRow);
            String status = getStatusFromTable(modelRow);

            if (checkInButton != null) {
                checkInButton.setEnabled("Booked".equals(status));
            }

            if (checkOutButton != null) {
                checkOutButton.setEnabled("Checked In".equals(status));
            }

            if (cancelButton != null) {
                cancelButton.setEnabled("Booked".equals(status) || "Checked In".equals(status));
            }
        } else {
            if (checkInButton != null) checkInButton.setEnabled(false);
            if (checkOutButton != null) checkOutButton.setEnabled(false);
            if (cancelButton != null) cancelButton.setEnabled(false);
        }
    }

    private String getStatusFromTable(int modelRow) {
        int statusColumn = "guest".equals(currentUser.getRole()) ? 5 : 6;
        return (String) tableModel.getValueAt(modelRow, statusColumn);
    }

    private int getBookingIdFromTable(int modelRow) {
        return (Integer) tableModel.getValueAt(modelRow, 0);
    }

    private void processCheckIn() {
        int selectedRow = bookingTable.getSelectedRow();
        if (selectedRow == -1) return;

        int modelRow = bookingTable.convertRowIndexToModel(selectedRow);
        int bookingId = getBookingIdFromTable(modelRow);

        if (bookingService.checkInGuest(bookingId)) {
            JOptionPane.showMessageDialog(this, "Guest checked in successfully!",
                    "Success", JOptionPane.INFORMATION_MESSAGE);
            loadBookingData();
        } else {
            JOptionPane.showMessageDialog(this, "Failed to check in guest.",
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void processCheckOut() {
        int selectedRow = bookingTable.getSelectedRow();
        if (selectedRow == -1) return;

        int modelRow = bookingTable.convertRowIndexToModel(selectedRow);
        int bookingId = getBookingIdFromTable(modelRow);

        if (bookingService.checkOutGuest(bookingId)) {
            JOptionPane.showMessageDialog(this, "Guest checked out successfully!",
                    "Success", JOptionPane.INFORMATION_MESSAGE);
            loadBookingData();
        } else {
            JOptionPane.showMessageDialog(this, "Failed to check out guest.",
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void cancelBooking() {
        int selectedRow = bookingTable.getSelectedRow();
        if (selectedRow == -1) return;

        int result = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to cancel this booking?",
                "Confirm Cancellation",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (result == JOptionPane.YES_OPTION) {
            int modelRow = bookingTable.convertRowIndexToModel(selectedRow);
            int bookingId = getBookingIdFromTable(modelRow);

            if (bookingService.cancelBooking(bookingId)) {
                JOptionPane.showMessageDialog(this, "Booking cancelled successfully!",
                        "Success", JOptionPane.INFORMATION_MESSAGE);
                loadBookingData();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to cancel booking.",
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public void refreshData() {
        loadBookingData();
        statusFilter.setSelectedItem("All");
    }
}