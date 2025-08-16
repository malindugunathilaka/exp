package utill;

import java.awt.*;

/**
 * UIConstants class containing all UI-related constants, colors, fonts, and themes
 * for the Hotel Management System application.
 */
public class UIConstants {

    // Color Scheme
    public static final Color PRIMARY_COLOR = new Color(41, 128, 185);        // Blue
    public static final Color SECONDARY_COLOR = new Color(52, 152, 219);     // Light Blue
    public static final Color ACCENT_COLOR = new Color(26, 188, 156);        // Teal
    public static final Color SUCCESS_COLOR = new Color(46, 204, 113);       // Green
    public static final Color WARNING_COLOR = new Color(241, 196, 15);       // Yellow
    public static final Color DANGER_COLOR = new Color(231, 76, 60);         // Red
    public static final Color DARK_COLOR = new Color(44, 62, 80);            // Dark Blue
    public static final Color LIGHT_COLOR = new Color(236, 240, 241);        // Light Gray
    public static final Color WHITE_COLOR = Color.WHITE;
    public static final Color BLACK_COLOR = Color.BLACK;

    // Background Colors
    public static final Color PANEL_BACKGROUND = new Color(248, 249, 250);
    public static final Color CARD_BACKGROUND = Color.WHITE;
    public static final Color HEADER_BACKGROUND = PRIMARY_COLOR;
    public static final Color SIDEBAR_BACKGROUND = new Color(52, 73, 94);

    // Text Colors
    public static final Color TEXT_PRIMARY = new Color(33, 37, 41);
    public static final Color TEXT_SECONDARY = new Color(108, 117, 125);
    public static final Color TEXT_LIGHT = Color.WHITE;
    public static final Color TEXT_MUTED = new Color(134, 142, 150);

    // Button Colors
    public static final Color BUTTON_PRIMARY = PRIMARY_COLOR;
    public static final Color BUTTON_SUCCESS = SUCCESS_COLOR;
    public static final Color BUTTON_WARNING = WARNING_COLOR;
    public static final Color BUTTON_DANGER = DANGER_COLOR;
    public static final Color BUTTON_SECONDARY = new Color(108, 117, 125);

    // Fonts
    public static final Font FONT_LARGE = new Font("Segoe UI", Font.BOLD, 24);
    public static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 20);
    public static final Font FONT_SUBTITLE = new Font("Segoe UI", Font.BOLD, 16);
    public static final Font FONT_REGULAR = new Font("Segoe UI", Font.PLAIN, 14);
    public static final Font FONT_SMALL = new Font("Segoe UI", Font.PLAIN, 12);
    public static final Font FONT_BUTTON = new Font("Segoe UI", Font.BOLD, 14);
    public static final Font FONT_HEADER = new Font("Segoe UI", Font.BOLD, 18);

    // Dimensions
    public static final int BUTTON_HEIGHT = 35;
    public static final int INPUT_HEIGHT = 30;
    public static final int PANEL_PADDING = 20;
    public static final int COMPONENT_SPACING = 10;
    public static final int BORDER_RADIUS = 5;

    // Window Dimensions
    public static final int MAIN_WINDOW_WIDTH = 1200;
    public static final int MAIN_WINDOW_HEIGHT = 800;
    public static final int LOGIN_WINDOW_WIDTH = 400;
    public static final int LOGIN_WINDOW_HEIGHT = 500;
    public static final int DIALOG_WIDTH = 400;
    public static final int DIALOG_HEIGHT = 300;

    // Table Settings
    public static final int TABLE_ROW_HEIGHT = 25;
    public static final Color TABLE_HEADER_BACKGROUND = LIGHT_COLOR;
    public static final Color TABLE_ALTERNATE_ROW = new Color(248, 249, 250);
    public static final Color TABLE_SELECTION_BACKGROUND = SECONDARY_COLOR;
    public static final Color TABLE_SELECTION_FOREGROUND = WHITE_COLOR;

    // Border Settings
    public static final int BORDER_THICKNESS = 1;
    public static final Color BORDER_COLOR = new Color(222, 226, 230);
    public static final Color FOCUS_BORDER_COLOR = PRIMARY_COLOR;

    // Animation Settings
    public static final int ANIMATION_DURATION = 200; // milliseconds
    public static final int HOVER_ALPHA = 20;

    // Status Colors
    public static final Color STATUS_AVAILABLE = SUCCESS_COLOR;
    public static final Color STATUS_OCCUPIED = DANGER_COLOR;
    public static final Color STATUS_MAINTENANCE = WARNING_COLOR;
    public static final Color STATUS_BOOKED = new Color(155, 89, 182);

    // Role Colors
    public static final Color ROLE_ADMIN = DANGER_COLOR;
    public static final Color ROLE_STAFF = WARNING_COLOR;
    public static final Color ROLE_GUEST = SUCCESS_COLOR;

    // Icons (Unicode symbols)
    public static final String ICON_HOME = "🏠";
    public static final String ICON_ROOM = "🏨";
    public static final String ICON_BOOKING = "📅";
    public static final String ICON_USER = "👤";
    public static final String ICON_REPORT = "📊";
    public static final String ICON_SETTINGS = "⚙";
    public static final String ICON_LOGOUT = "🚪";
    public static final String ICON_SUCCESS = "✓";
    public static final String ICON_ERROR = "✗";
    public static final String ICON_WARNING = "⚠";
    public static final String ICON_INFO = "ℹ";

    // Messages
    public static final String APP_TITLE = "Hotel Management System";
    public static final String LOGIN_TITLE = "Welcome Back";
    public static final String LOGIN_SUBTITLE = "Sign in to your account";

    // Validation Messages
    public static final String REQUIRED_FIELD = "This field is required";
    public static final String INVALID_EMAIL = "Please enter a valid email address";
    public static final String INVALID_PHONE = "Please enter a valid phone number";
    public static final String INVALID_DATE = "Please select a valid date";
    public static final String PASSWORD_TOO_SHORT = "Password must be at least 6 characters";

    // Success Messages
    public static final String LOGIN_SUCCESS = "Login successful";
    public static final String ROOM_ADDED = "Room added successfully";
    public static final String BOOKING_CREATED = "Booking created successfully";
    public static final String USER_CREATED = "User created successfully";
    public static final String UPDATE_SUCCESS = "Update successful";
    public static final String DELETE_SUCCESS = "Deleted successfully";

    // Error Messages
    public static final String LOGIN_FAILED = "Invalid username or password";
    public static final String CONNECTION_ERROR = "Database connection error";
    public static final String SAVE_ERROR = "Error saving data";
    public static final String LOAD_ERROR = "Error loading data";
    public static final String DELETE_ERROR = "Error deleting record";
    public static final String PERMISSION_DENIED = "Permission denied";

    /**
     * Creates a rounded border with the specified color and radius
     */
    public static javax.swing.border.Border createRoundedBorder(Color color, int radius) {
        return javax.swing.BorderFactory.createCompoundBorder(
                javax.swing.BorderFactory.createLineBorder(color, BORDER_THICKNESS),
                javax.swing.BorderFactory.createEmptyBorder(5, 10, 5, 10)
        );
    }

    /**
     * Creates a card-style border for panels
     */
    public static javax.swing.border.Border createCardBorder() {
        return javax.swing.BorderFactory.createCompoundBorder(
                javax.swing.BorderFactory.createLineBorder(BORDER_COLOR, BORDER_THICKNESS),
                javax.swing.BorderFactory.createEmptyBorder(PANEL_PADDING, PANEL_PADDING, PANEL_PADDING, PANEL_PADDING)
        );
    }

    /**
     * Creates a title border with the specified title
     */
    public static javax.swing.border.Border createTitleBorder(String title) {
        return javax.swing.BorderFactory.createTitledBorder(
                javax.swing.BorderFactory.createLineBorder(BORDER_COLOR),
                title,
                javax.swing.border.TitledBorder.LEFT,
                javax.swing.border.TitledBorder.TOP,
                FONT_SUBTITLE,
                TEXT_PRIMARY
        );
    }

    /**
     * Returns the appropriate color for a room status
     */
    public static Color getStatusColor(String status) {
        switch (status.toLowerCase()) {
            case "available": return STATUS_AVAILABLE;
            case "occupied": return STATUS_OCCUPIED;
            case "maintenance": return STATUS_MAINTENANCE;
            case "booked": return STATUS_BOOKED;
            default: return TEXT_SECONDARY;
        }
    }

    /**
     * Returns the appropriate color for a user role
     */
    public static Color getRoleColor(String role) {
        switch (role.toLowerCase()) {
            case "admin": return ROLE_ADMIN;
            case "staff": return ROLE_STAFF;
            case "guest": return ROLE_GUEST;
            default: return TEXT_SECONDARY;
        }
    }

    // Private constructor to prevent instantiation
    private UIConstants() {}
}