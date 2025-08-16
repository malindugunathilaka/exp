package ui;

import model.User;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

public class DashboardPanel extends JPanel {
    private User currentUser;
    private JTabbedPane tabbedPane;
    private ActionListener logoutListener;

    // UI Panels
    private RoomsPanel roomsPanel;
    private BookingsPanel bookingsPanel;
    private UsersPanel usersPanel;
    private ReportsPanel reportsPanel;

    public DashboardPanel(User user) {
        this.currentUser = user;
        initializeUI();
    }

    private void initializeUI() {
        setLayout(new BorderLayout());

        // Create header panel
        JPanel headerPanel = createHeaderPanel();
        add(headerPanel, BorderLayout.NORTH);

        // Create tabbed pane
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Arial", Font.BOLD, 12));
        addTabs();
        add(tabbedPane, BorderLayout.CENTER);

        // Set background color
        setBackground(Color.WHITE);
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        headerPanel.setBackground(new Color(52, 73, 94)); // Dark blue-gray

        // Welcome label
        JLabel welcomeLabel = new JLabel("Welcome, " + currentUser.getFullname() + " (" + currentUser.getRole() + ")");
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 16));
        welcomeLabel.setForeground(Color.WHITE);
        headerPanel.add(welcomeLabel, BorderLayout.WEST);

        // Right panel with user info and logout button
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        rightPanel.setOpaque(false);

        // User role badge
        JLabel roleLabel = new JLabel(currentUser.getRole().toUpperCase());
        roleLabel.setFont(new Font("Arial", Font.BOLD, 10));
        roleLabel.setForeground(Color.WHITE);
        roleLabel.setOpaque(true);
        roleLabel.setBorder(BorderFactory.createEmptyBorder(3, 8, 3, 8));

        // Set role-specific colors
        switch (currentUser.getRole()) {
            case "admin":
                roleLabel.setBackground(new Color(231, 76, 60)); // Red
                break;
            case "staff":
                roleLabel.setBackground(new Color(241, 196, 15)); // Yellow
                break;
            case "guest":
                roleLabel.setBackground(new Color(46, 204, 113)); // Green
                break;
        }

        rightPanel.add(roleLabel);
        rightPanel.add(Box.createHorizontalStrut(10));

        // Logout button
        JButton logoutButton = new JButton("Logout");
        logoutButton.setFont(new Font("Arial", Font.BOLD, 12));
        logoutButton.setBackground(new Color(231, 76, 60)); // Red
        logoutButton.setForeground(Color.WHITE);
        logoutButton.setFocusPainted(false);
        logoutButton.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));

        logoutButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                logoutButton.setBackground(new Color(192, 57, 43));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                logoutButton.setBackground(new Color(231, 76, 60));
            }
        });

        logoutButton.addActionListener(e -> {
            if (logoutListener != null) {
                logoutListener.actionPerformed(e);
            }
        });

        rightPanel.add(logoutButton);
        headerPanel.add(rightPanel, BorderLayout.EAST);

        return headerPanel;
    }

    private void addTabs() {
        // Add common tabs
        roomsPanel = new RoomsPanel(currentUser);
        tabbedPane.addTab("Rooms", createTabIcon("🏠"), roomsPanel, "View and manage hotel rooms");

        // Role-specific tabs
        if ("guest".equals(currentUser.getRole())) {
            bookingsPanel = new BookingsPanel(currentUser);
            tabbedPane.addTab("My Bookings", createTabIcon("📋"), bookingsPanel, "View your bookings");
            tabbedPane.addTab("Book a Room", createTabIcon("🎯"), new BookingFormpanel(currentUser), "Make a new reservation");

        } else if ("staff".equals(currentUser.getRole())) {
            bookingsPanel = new BookingsPanel(currentUser);
            tabbedPane.addTab("Manage Bookings", createTabIcon("📋"), bookingsPanel, "Manage all bookings");
            tabbedPane.addTab("Check In/Out", createTabIcon("🔑"), new CheckInOutPanel(), "Process check-ins and check-outs");

        } else if ("admin".equals(currentUser.getRole())) {
            bookingsPanel = new BookingsPanel(currentUser);
            tabbedPane.addTab("Manage Bookings", createTabIcon("📋"), bookingsPanel, "Manage all bookings");

            usersPanel = new UsersPanel();
            tabbedPane.addTab("Manage Users", createTabIcon("👥"), usersPanel, "Manage system users");

            reportsPanel = new ReportsPanel();
            tabbedPane.addTab("Reports", createTabIcon("📊"), reportsPanel, "View system reports");
        }
    }

    private Icon createTabIcon(String emoji) {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                g.setFont(new Font("Arial", Font.PLAIN, 14));
                g.drawString(emoji, x, y + 12);
            }

            @Override
            public int getIconWidth() { return 16; }

            @Override
            public int getIconHeight() { return 16; }
        };
    }

    public void refreshData() {
        // Refresh all panels
        if (roomsPanel != null) {
            roomsPanel.refreshData();
        }
        if (bookingsPanel != null) {
            bookingsPanel.refreshData();
        }
        if (usersPanel != null) {
            usersPanel.refreshData();
        }
        if (reportsPanel != null) {
            reportsPanel.refreshData();
        }
    }

    public void setLogoutListener(ActionListener listener) {
        this.logoutListener = listener;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public void showTab(String tabName) {
        for (int i = 0; i < tabbedPane.getTabCount(); i++) {
            if (tabbedPane.getTitleAt(i).equals(tabName)) {
                tabbedPane.setSelectedIndex(i);
                break;
            }
        }
    }

    // Method to get specific panels for external access
    public RoomsPanel getRoomsPanel() {
        return roomsPanel;
    }

    public BookingsPanel getBookingsPanel() {
        return bookingsPanel;
    }

    public UsersPanel getUsersPanel() {
        return usersPanel;
    }

    public ReportsPanel getReportsPanel() {
        return reportsPanel;
    }
}