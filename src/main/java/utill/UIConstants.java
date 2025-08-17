package utill;

import java.awt.*;
import java.io.InputStream;

/**
 * UIConstants class containing all UI-related constants, colors, fonts, and themes
 * for the Hotel Management System application.
 */
public final class UIConstants {

    /* ------------------------------------------------------------------
       1.  CUSTOM FONT – loads once at class-loading time
       ------------------------------------------------------------------ */
    private static final String FONT_PATH = "/fonts/sf-pro-rounded.ttf"; // Path to the custom font file
    private static final Font   BASE_FONT;

    static {
        Font tmp;
        try (InputStream is = UIConstants.class.getResourceAsStream(FONT_PATH)) {
            tmp = Font.createFont(Font.TRUETYPE_FONT, is);
        } catch (Exception ex) {
            tmp = new Font("Segoe UI", Font.PLAIN, 12);   // fallback
        }
        BASE_FONT = tmp;
    }

    /* ------------------------------------------------------------------
       2.  COLOR SCHEME
       ------------------------------------------------------------------ */
    public static final Color PRIMARY_COLOR   = new Color(41, 128, 185);
    public static final Color SECONDARY_COLOR = new Color(52, 152, 219);
    public static final Color ACCENT_COLOR    = new Color(26, 188, 156);
    public static final Color SUCCESS_COLOR   = new Color(46, 204, 113);
    public static final Color WARNING_COLOR   = new Color(241, 196, 15);
    public static final Color DANGER_COLOR    = new Color(231, 76, 60);
    public static final Color DARK_COLOR      = new Color(44, 62, 80);
    public static final Color LIGHT_COLOR     = new Color(236, 240, 241);
    public static final Color WHITE_COLOR     = Color.WHITE;
    public static final Color BLACK_COLOR     = Color.BLACK;

    public static final Color PANEL_BACKGROUND   = new Color(248, 249, 250);
    public static final Color CARD_BACKGROUND    = WHITE_COLOR;
    public static final Color HEADER_BACKGROUND  = PRIMARY_COLOR;
    public static final Color SIDEBAR_BACKGROUND = new Color(52, 73, 94);

    public static final Color TEXT_PRIMARY   = new Color(33, 37, 41);
    public static final Color TEXT_SECONDARY = new Color(108, 117, 125);
    public static final Color TEXT_LIGHT     = WHITE_COLOR;
    public static final Color TEXT_MUTED     = new Color(134, 142, 150);

    public static final Color BUTTON_PRIMARY   = PRIMARY_COLOR;
    public static final Color BUTTON_SUCCESS   = SUCCESS_COLOR;
    public static final Color BUTTON_WARNING   = WARNING_COLOR;
    public static final Color BUTTON_DANGER    = DANGER_COLOR;
    public static final Color BUTTON_SECONDARY = new Color(108, 117, 125);

    /* ------------------------------------------------------------------
       3.  FONTS – all derived from BASE_FONT
       ------------------------------------------------------------------ */
    public static final Font FONT_LARGE    = BASE_FONT.deriveFont(Font.BOLD, 24f);
    public static final Font FONT_TITLE    = BASE_FONT.deriveFont(Font.BOLD, 20f);
    public static final Font FONT_SUBTITLE = BASE_FONT.deriveFont(Font.BOLD, 16f);
    public static final Font FONT_REGULAR  = BASE_FONT.deriveFont(Font.PLAIN, 14f);
    public static final Font FONT_SMALL    = BASE_FONT.deriveFont(Font.PLAIN, 12f);
    public static final Font FONT_BUTTON   = BASE_FONT.deriveFont(Font.BOLD, 14f);
    public static final Font FONT_HEADER   = BASE_FONT.deriveFont(Font.BOLD, 18f);

    /* ------------------------------------------------------------------
       4.  DIMENSIONS & MISC CONSTANTS
       ------------------------------------------------------------------ */
    public static final int BUTTON_HEIGHT      = 35;
    public static final int INPUT_HEIGHT       = 30;
    public static final int PANEL_PADDING      = 20;
    public static final int COMPONENT_SPACING  = 10;
    public static final int BORDER_RADIUS      = 5;

    public static final int MAIN_WINDOW_WIDTH  = 1200;
    public static final int MAIN_WINDOW_HEIGHT = 800;
    public static final int LOGIN_WINDOW_WIDTH = 400;
    public static final int LOGIN_WINDOW_HEIGHT= 500;
    public static final int DIALOG_WIDTH       = 400;
    public static final int DIALOG_HEIGHT      = 300;

    public static final int  TABLE_ROW_HEIGHT         = 25;
    public static final Color TABLE_HEADER_BACKGROUND = LIGHT_COLOR;
    public static final Color TABLE_ALTERNATE_ROW     = new Color(248, 249, 250);
    public static final Color TABLE_SELECTION_BACKGROUND= SECONDARY_COLOR;
    public static final Color TABLE_SELECTION_FOREGROUND= WHITE_COLOR;

    public static final int   BORDER_THICKNESS = 1;
    public static final Color BORDER_COLOR     = new Color(222, 226, 230);
    public static final Color FOCUS_BORDER_COLOR = PRIMARY_COLOR;

    public static final int ANIMATION_DURATION = 200;
    public static final int HOVER_ALPHA        = 20;

    public static final Color STATUS_AVAILABLE   = SUCCESS_COLOR;
    public static final Color STATUS_OCCUPIED    = DANGER_COLOR;
    public static final Color STATUS_MAINTENANCE = WARNING_COLOR;
    public static final Color STATUS_BOOKED      = new Color(155, 89, 182);

    public static final Color ROLE_ADMIN = DANGER_COLOR;
    public static final Color ROLE_STAFF = WARNING_COLOR;
    public static final Color ROLE_GUEST = SUCCESS_COLOR;

    /* ------------------------------------------------------------------
       5.  ICONS (Unicode)
       ------------------------------------------------------------------ */
    public static final String ICON_HOME     = "🏠";
    public static final String ICON_ROOM     = "🏨";
    public static final String ICON_BOOKING  = "📅";
    public static final String ICON_USER     = "👤";
    public static final String ICON_REPORT   = "📊";
    public static final String ICON_SETTINGS = "⚙";
    public static final String ICON_LOGOUT   = "🚪";
    public static final String ICON_SUCCESS  = "✓";
    public static final String ICON_ERROR    = "✗";
    public static final String ICON_WARNING  = "⚠";
    public static final String ICON_INFO     = "ℹ";

    /* ------------------------------------------------------------------
       6.  MESSAGES
       ------------------------------------------------------------------ */
    public static final String APP_TITLE      = "Hotel Management System";
    public static final String LOGIN_TITLE    = "Welcome Back";
    public static final String LOGIN_SUBTITLE = "Sign in to your account";

    public static final String REQUIRED_FIELD    = "This field is required";
    public static final String INVALID_EMAIL     = "Please enter a valid email address";
    public static final String INVALID_PHONE     = "Please enter a valid phone number";
    public static final String INVALID_DATE      = "Please select a valid date";
    public static final String PASSWORD_TOO_SHORT= "Password must be at least 6 characters";

    public static final String LOGIN_SUCCESS   = "Login successful";
    public static final String ROOM_ADDED      = "Room added successfully";
    public static final String BOOKING_CREATED = "Booking created successfully";
    public static final String USER_CREATED    = "User created successfully";
    public static final String UPDATE_SUCCESS  = "Update successful";
    public static final String DELETE_SUCCESS  = "Deleted successfully";

    public static final String LOGIN_FAILED       = "Invalid username or password";
    public static final String CONNECTION_ERROR   = "Database connection error";
    public static final String SAVE_ERROR         = "Error saving data";
    public static final String LOAD_ERROR         = "Error loading data";
    public static final String DELETE_ERROR       = "Error deleting record";
    public static final String PERMISSION_DENIED  = "Permission denied";

    /* ------------------------------------------------------------------
       7.  BORDER HELPERS
       ------------------------------------------------------------------ */
    public static javax.swing.border.Border createRoundedBorder(Color color, int radius) {
        return javax.swing.BorderFactory.createCompoundBorder(
                javax.swing.BorderFactory.createLineBorder(color, BORDER_THICKNESS),
                javax.swing.BorderFactory.createEmptyBorder(5, 10, 5, 10)
        );
    }

    public static javax.swing.border.Border createCardBorder() {
        return javax.swing.BorderFactory.createCompoundBorder(
                javax.swing.BorderFactory.createLineBorder(BORDER_COLOR, BORDER_THICKNESS),
                javax.swing.BorderFactory.createEmptyBorder(PANEL_PADDING, PANEL_PADDING,
                        PANEL_PADDING, PANEL_PADDING)
        );
    }

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

    /* ------------------------------------------------------------------
       8.  STATUS & ROLE COLOR HELPERS
       ------------------------------------------------------------------ */
    public static Color getStatusColor(String status) {
        switch (status.toLowerCase()) {
            case "available":   return STATUS_AVAILABLE;
            case "occupied":    return STATUS_OCCUPIED;
            case "maintenance": return STATUS_MAINTENANCE;
            case "booked":      return STATUS_BOOKED;
            default:            return TEXT_SECONDARY;
        }
    }

    public static Color getRoleColor(String role) {
        switch (role.toLowerCase()) {
            case "admin": return ROLE_ADMIN;
            case "staff": return ROLE_STAFF;
            case "guest": return ROLE_GUEST;
            default:      return TEXT_SECONDARY;
        }
    }

    /* ------------------------------------------------------------------
       9.  PREVENT INSTANTIATION
       ------------------------------------------------------------------ */
    private UIConstants() { }
}