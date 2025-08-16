package ui;

import dao.RoomDAO;
import dao.PaymentDAO;
import dao.BookingDAO;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.Map;

public class ReportsPanel extends JPanel {
    private RoomDAO roomDAO;
    private PaymentDAO paymentDAO;
    private BookingDAO bookingDAO;

    private JLabel totalRoomsLabel;
    private JLabel totalBookingsLabel;
    private JLabel totalRevenueLabel;
    private JLabel todayRevenueLabel;

    public ReportsPanel() {
        this.roomDAO = new RoomDAO();
        this.paymentDAO = new PaymentDAO();
        this.bookingDAO = new BookingDAO();
        initializeUI();
        loadReportData();
    }

    private void initializeUI() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Create summary panel at top
        JPanel summaryPanel = createSummaryPanel();
        add(summaryPanel, BorderLayout.NORTH);

        // Create main reports panel
        JPanel reportsPanel = new JPanel(new GridLayout(1, 2, 15, 15));

        // Occupancy report
        JPanel occupancyPanel = createOccupancyPanel();
        reportsPanel.add(occupancyPanel);

        // Revenue report
        JPanel revenuePanel = createRevenuePanel();
        reportsPanel.add(revenuePanel);

        add(reportsPanel, BorderLayout.CENTER);

        // Refresh button
        JPanel bottomPanel = new JPanel(new FlowLayout());
        JButton refreshButton = new JButton("Refresh Reports");
        refreshButton.addActionListener(e -> refreshData());
        bottomPanel.add(refreshButton);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private JPanel createSummaryPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 4, 15, 10));
        panel.setBorder(BorderFactory.createTitledBorder("Summary"));
        panel.setPreferredSize(new Dimension(0, 80));

        // Total Rooms
        JPanel roomsCard = createSummaryCard("Total Rooms", "0", new Color(52, 152, 219));
        totalRoomsLabel = (JLabel) ((JPanel) roomsCard.getComponent(1)).getComponent(0);
        panel.add(roomsCard);

        // Total Bookings
        JPanel bookingsCard = createSummaryCard("Total Bookings", "0", new Color(46, 204, 113));
        totalBookingsLabel = (JLabel) ((JPanel) bookingsCard.getComponent(1)).getComponent(0);
        panel.add(bookingsCard);

        // Total Revenue
        JPanel revenueCard = createSummaryCard("Total Revenue", "$0.00", new Color(155, 89, 182));
        totalRevenueLabel = (JLabel) ((JPanel) revenueCard.getComponent(1)).getComponent(0);
        panel.add(revenueCard);

        // Today's Revenue
        JPanel todayCard = createSummaryCard("Today's Revenue", "$0.00", new Color(241, 196, 15));
        todayRevenueLabel = (JLabel) ((JPanel) todayCard.getComponent(1)).getComponent(0);
        panel.add(todayCard);

        return panel;
    }

    private JPanel createSummaryCard(String title, String value, Color color) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(color);
        card.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(new Font("Arial", Font.PLAIN, 11));
        card.add(titleLabel, BorderLayout.NORTH);

        JPanel valuePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        valuePanel.setOpaque(false);
        JLabel valueLabel = new JLabel(value);
        valueLabel.setForeground(Color.WHITE);
        valueLabel.setFont(new Font("Arial", Font.BOLD, 16));
        valuePanel.add(valueLabel);
        card.add(valuePanel, BorderLayout.CENTER);

        return card;
    }

    private JPanel createOccupancyPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Room Occupancy Report"));

        DefaultTableModel occupancyModel = new DefaultTableModel();
        occupancyModel.addColumn("Room Status");
        occupancyModel.addColumn("Count");
        occupancyModel.addColumn("Percentage");

        JTable occupancyTable = new JTable(occupancyModel);
        occupancyTable.setRowHeight(25);
        occupancyTable.getTableHeader().setReorderingAllowed(false);

        JScrollPane scrollPane = new JScrollPane(occupancyTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        // Load occupancy data
        loadOccupancyData(occupancyModel);

        return panel;
    }

    private JPanel createRevenuePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Monthly Revenue Report"));

        DefaultTableModel revenueModel = new DefaultTableModel();
        revenueModel.addColumn("Month");
        revenueModel.addColumn("Revenue");
        revenueModel.addColumn("Bookings");

        JTable revenueTable = new JTable(revenueModel);
        revenueTable.setRowHeight(25);
        revenueTable.getTableHeader().setReorderingAllowed(false);

        JScrollPane scrollPane = new JScrollPane(revenueTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        // Load revenue data
        loadRevenueData(revenueModel);

        return panel;
    }

    private void loadOccupancyData(DefaultTableModel model) {
        try {
            Map<String, Integer> statistics = roomDAO.getRoomStatistics();
            int totalRooms = roomDAO.getTotalRoomCount();

            model.setRowCount(0); // Clear existing data

            for (Map.Entry<String, Integer> entry : statistics.entrySet()) {
                String status = entry.getKey();
                int count = entry.getValue();
                double percentage = totalRooms > 0 ? (count * 100.0 / totalRooms) : 0;

                model.addRow(new Object[]{
                        status,
                        count,
                        String.format("%.1f%%", percentage)
                });
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading occupancy data: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadRevenueData(DefaultTableModel model) {
        try {
            Map<String, Double> revenueByMonth = paymentDAO.getRevenueByMonth();

            model.setRowCount(0); // Clear existing data

            for (Map.Entry<String, Double> entry : revenueByMonth.entrySet()) {
                String month = entry.getKey();
                double revenue = entry.getValue();

                // Get booking count for the month (this would require additional DAO method)
                // For now, we'll show "-" as placeholder

                model.addRow(new Object[]{
                        month,
                        String.format("$%.2f", revenue),
                        "-" // Placeholder for booking count
                });
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading revenue data: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void loadReportData() {
        try {
            // Load summary data
            int totalRooms = roomDAO.getTotalRoomCount();
            totalRoomsLabel.setText(String.valueOf(totalRooms));

            int totalBookings = bookingDAO.getTotalBookingCount();
            totalBookingsLabel.setText(String.valueOf(totalBookings));

            double totalRevenue = paymentDAO.getTotalRevenue();
            totalRevenueLabel.setText(String.format("$%.2f", totalRevenue));

            double todayRevenue = paymentDAO.getTodayRevenue();
            todayRevenueLabel.setText(String.format("$%.2f", todayRevenue));

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading report data: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void refreshData() {
        loadReportData();

        // Refresh table data
        Component[] components = ((JPanel) ((BorderLayout) getLayout()).getLayoutComponent(BorderLayout.CENTER)).getComponents();
        for (Component component : components) {
            if (component instanceof JPanel) {
                JPanel panel = (JPanel) component;
                Component[] panelComponents = panel.getComponents();
                for (Component panelComponent : panelComponents) {
                    if (panelComponent instanceof JScrollPane) {
                        JScrollPane scrollPane = (JScrollPane) panelComponent;
                        JTable table = (JTable) scrollPane.getViewport().getView();
                        DefaultTableModel model = (DefaultTableModel) table.getModel();

                        if (panel.getBorder() != null &&
                                ((javax.swing.border.TitledBorder) panel.getBorder()).getTitle().contains("Occupancy")) {
                            loadOccupancyData(model);
                        } else if (panel.getBorder() != null &&
                                ((javax.swing.border.TitledBorder) panel.getBorder()).getTitle().contains("Revenue")) {
                            loadRevenueData(model);
                        }
                    }
                }
            }
        }
    }
}