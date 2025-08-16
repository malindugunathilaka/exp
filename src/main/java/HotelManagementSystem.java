package com.hotel.main;

import dao.DatabaseManager;
import ui.LoginPanel;
import ui.DashboardPanel;
import model.User;
import javax.swing.*;
import java.awt.*;

public class HotelManagementSystem extends JFrame {
    private LoginPanel loginPanel;
    private DashboardPanel dashboardPanel;
    private User currentUser;

    public HotelManagementSystem() {
        super("Hotel Management System");
        initializeApplication();
    }

    private void initializeApplication() {
        // Initialize database
        initializeDatabase();

        // Setup main window
        setupMainWindow();

        // Show login screen
        showLoginScreen();

        // Make window visible
        setVisible(true);
    }

    private void initializeDatabase() {
        try {
            DatabaseManager.initializeDatabase();
            System.out.println("Database initialized successfully");
        } catch (Exception e) {
            String errorMessage = "Failed to initialize database: " + e.getMessage();
            System.err.println(errorMessage);
            JOptionPane.showMessageDialog(null, errorMessage,
                    "Database Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }

    private void setupMainWindow() {
        // Window properties
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(800, 600));

        // Set application icon (if available)
        try {
            // You can add a hotel icon here if you have one
            // ImageIcon icon = new ImageIcon(getClass().getResource("/images/hotel_icon.png"));
            // setIconImage(icon.getImage());
        } catch (Exception e) {
            // Icon not found, continue without icon
        }

        // Set look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            // Use default look and feel
            System.out.println("Using default look and feel");
        }
    }

    private void showLoginScreen() {
        // Clear current content
        getContentPane().removeAll();

        // Create and setup login panel
        loginPanel = new LoginPanel();
        loginPanel.setLoginSuccessListener(e -> handleSuccessfulLogin());

        // Add to main window
        getContentPane().add(loginPanel);

        // Refresh display
        revalidate();
        repaint();

        // Update window title
        setTitle("Hotel Management System - Login");
    }

    private void handleSuccessfulLogin() {
        // Get authenticated user from login panel
        currentUser = loginPanel.getAuthenticatedUser();

        if (currentUser != null) {
            showDashboard();
        } else {
            JOptionPane.showMessageDialog(this, "Authentication failed. Please try again.",
                    "Login Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showDashboard() {
        // Clear current content
        getContentPane().removeAll();

        // Create and setup dashboard
        dashboardPanel = new DashboardPanel(currentUser);
        dashboardPanel.setLogoutListener(e -> handleLogout());

        // Add to main window
        getContentPane().add(dashboardPanel);

        // Refresh display
        revalidate();
        repaint();

        // Update window title
        setTitle("Hotel Management System - " + currentUser.getFullname() +
                " (" + currentUser.getRole() + ")");

        System.out.println("User logged in: " + currentUser.getUsername() +
                " (" + currentUser.getRole() + ")");
    }

    private void handleLogout() {
        // Confirm logout
        int result = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to logout?",
                "Confirm Logout",
                JOptionPane.YES_NO_OPTION);

        if (result == JOptionPane.YES_OPTION) {
            // Log the logout
            System.out.println("User logged out: " + currentUser.getUsername());

            // Clear current user
            currentUser = null;

            // Clear dashboard reference
            dashboardPanel = null;

            // Show login screen
            showLoginScreen();
        }
    }

    // Method to handle application shutdown
    private void shutdown() {
        try {
            // Close database connection
            DatabaseManager.closeConnection();
            System.out.println("Database connection closed");
        } catch (Exception e) {
            System.err.println("Error closing database connection: " + e.getMessage());
        }

        // Exit application
        System.exit(0);
    }

    // Override window closing to ensure proper cleanup
    @Override
    protected void processWindowEvent(java.awt.event.WindowEvent e) {
        if (e.getID() == java.awt.event.WindowEvent.WINDOW_CLOSING) {
            shutdown();
        } else {
            super.processWindowEvent(e);
        }
    }

    // Getter methods for external access (if needed)
    public User getCurrentUser() {
        return currentUser;
    }

    public DashboardPanel getDashboardPanel() {
        return dashboardPanel;
    }

    public LoginPanel getLoginPanel() {
        return loginPanel;
    }

    // Method to refresh dashboard data (useful for external calls)
    public void refreshDashboard() {
        if (dashboardPanel != null) {
            dashboardPanel.refreshData();
        }
    }

    // Main method - Application entry point
    public static void main(String[] args) {
        // Set system properties for better UI rendering
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");

        // Enable anti-aliasing for better text rendering
        System.setProperty("sun.java2d.uiScale", "1.0");

        // Run application on Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            try {
                // Create and start the application
                new HotelManagementSystem();
                System.out.println("Hotel Management System started successfully");
            } catch (Exception e) {
                System.err.println("Failed to start application: " + e.getMessage());
                e.printStackTrace();

                // Show error dialog
                JOptionPane.showMessageDialog(null,
                        "Failed to start Hotel Management System:\n" + e.getMessage(),
                        "Startup Error",
                        JOptionPane.ERROR_MESSAGE);

                System.exit(1);
            }
        });
    }
}