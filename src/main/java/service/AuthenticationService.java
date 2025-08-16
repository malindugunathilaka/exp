package service;



import dao.UserDAO;
import model.User;

import javax.swing.*;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.regex.Pattern;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * AuthenticationService handles all authentication-related business logic
 * including login, logout, session management, password security, and user validation.
 *
 * This service provides:
 * - User authentication with secure password handling
 * - Session management with timeout
 * - Password validation and security
 * - Role-based access control
 * - User account management
 * - Security logging and monitoring
 */
public class AuthenticationService {

    private static final Logger LOGGER = Logger.getLogger(AuthenticationService.class.getName());

    // Dependencies
    private final UserDAO userDAO;

    // Current session state
    private User currentUser;
    private boolean isLoggedIn;
    private long sessionStartTime;
    private long lastActivityTime;
    private String sessionId;

    // Security configuration
    private static final long SESSION_TIMEOUT = 30 * 60 * 1000; // 30 minutes in milliseconds
    private static final long WARNING_TIME = 5 * 60 * 1000;     // 5 minutes warning before timeout
    private static final int MAX_LOGIN_ATTEMPTS = 3;
    private static final long LOCKOUT_DURATION = 15 * 60 * 1000; // 15 minutes lockout

    // Password security patterns
    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
            "^(?=.[a-z])(?=.[A-Z])(?=.\\d)(?=.[@$!%?&])[A-Za-z\\d@$!%?&]{8,}$"
    );
    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_]{3,20}$");

    // Failed login tracking
    private int failedLoginAttempts = 0;
    private long lastFailedLoginTime = 0;
    private boolean isAccountLocked = false;

    /**
     * Constructor with UserDAO dependency injection
     *
     * @param userDAO The UserDAO instance for database operations
     */
    public AuthenticationService(UserDAO userDAO) {
        this.userDAO = userDAO;
        this.currentUser = null;
        this.isLoggedIn = false;
        this.sessionStartTime = 0;
        this.lastActivityTime = 0;
        this.sessionId = null;

        LOGGER.info("AuthenticationService initialized");
    }

    /**
     * Authenticates a user with username and password
     *
     * @param username The username to authenticate
     * @param password The password to authenticate
     * @return AuthenticationResult containing success status, message, and user info
     */
    public AuthenticationResult authenticateUser(String username, String password) {
        LOGGER.info("Authentication attempt for user: " + username);

        // Check if account is locked
        if (isAccountLocked()) {
            long remainingLockTime = getRemainingLockoutTime();
            if (remainingLockTime > 0) {
                String message = String.format("Account locked due to multiple failed attempts. Try again in %d minutes.",
                        remainingLockTime / (60 * 1000));
                LOGGER.warning("Authentication blocked - account locked for user: " + username);
                return new AuthenticationResult(false, message, null);
            } else {
                // Lockout period expired, reset
                resetFailedAttempts();
            }
        }

        // Validate input parameters
        ValidationResult usernameValidation = validateUsernameFormat(username);
        if (!usernameValidation.isValid()) {
            incrementFailedAttempts();
            LOGGER.warning("Authentication failed - invalid username format for: " + username);
            return new AuthenticationResult(false, usernameValidation.getMessage(), null);
        }

        if (password == null || password.trim().isEmpty()) {
            incrementFailedAttempts();
            LOGGER.warning("Authentication failed - empty password for user: " + username);
            return new AuthenticationResult(false, "Password cannot be empty", null);
        }

        try {
            // Attempt to authenticate with database
            User user = userDAO.authenticate(username.trim(), password);

            if (user != null) {
                // Authentication successful
                resetFailedAttempts();
                initializeSession(user);

                LOGGER.info("Authentication successful for user: " + username + " with role: " + user.getRole());
                return new AuthenticationResult(true, "Login successful", user);
            } else {
                // Authentication failed
                incrementFailedAttempts();
                LOGGER.warning("Authentication failed - invalid credentials for user: " + username);

                String message = "Invalid username or password";
                if (failedLoginAttempts >= MAX_LOGIN_ATTEMPTS - 1) {
                    message += ". Account will be locked after " + (MAX_LOGIN_ATTEMPTS - failedLoginAttempts) + " more failed attempt(s).";
                }

                return new AuthenticationResult(false, message, null);
            }

        } catch (Exception e) {
            incrementFailedAttempts();
            LOGGER.log(Level.SEVERE, "Authentication error for user: " + username, e);
            return new AuthenticationResult(false, "Authentication service error. Please try again later.", null);
        }
    }

    /**
     * Initializes a new user session
     *
     * @param user The authenticated user
     */
    private void initializeSession(User user) {
        this.currentUser = user;
        this.isLoggedIn = true;
        this.sessionStartTime = System.currentTimeMillis();
        this.lastActivityTime = this.sessionStartTime;
        this.sessionId = generateSessionId();

        LOGGER.info("Session initialized for user: " + user.getUsername() + " (Session ID: " + sessionId + ")");
    }

    /**
     * Generates a unique session ID
     *
     * @return A unique session identifier
     */
    private String generateSessionId() {
        return "SESSION_" + System.currentTimeMillis() + "_" + Math.random();
    }

    /**
     * Logs out the current user and clears session
     */
    public void logout() {
        if (currentUser != null) {
            LOGGER.info("User logged out: " + currentUser.getUsername() + " (Session ID: " + sessionId + ")");
        }

        clearSession();
    }

    /**
     * Clears all session data
     */
    private void clearSession() {
        currentUser = null;
        isLoggedIn = false;
        sessionStartTime = 0;
        lastActivityTime = 0;
        sessionId = null;
    }

    /**
     * Checks if a user is currently logged in and session is valid
     *
     * @return true if user is logged in and session is valid
     */
    public boolean isLoggedIn() {
        if (!isLoggedIn || currentUser == null) {
            return false;
        }

        if (isSessionExpired()) {
            LOGGER.info("Session expired for user: " + currentUser.getUsername());
            clearSession();
            return false;
        }

        return true;
    }

    /**
     * Gets the currently logged-in user
     *
     * @return The current user or null if not logged in
     */
    public User getCurrentUser() {
        if (!isLoggedIn()) {
            return null;
        }
        return currentUser;
    }

    /**
     * Checks if the current session has expired
     *
     * @return true if session has expired
     */
    public boolean isSessionExpired() {
        if (!isLoggedIn || sessionStartTime == 0) {
            return true;
        }

        long currentTime = System.currentTimeMillis();
        return (currentTime - lastActivityTime) > SESSION_TIMEOUT;
    }

    /**
     * Refreshes the current session (extends timeout)
     * Updates the last activity time to current time
     */
    public void refreshSession() {
        if (isLoggedIn) {
            lastActivityTime = System.currentTimeMillis();
            LOGGER.fine("Session refreshed for user: " + currentUser.getUsername());
        }
    }

    /**
     * Gets the remaining session time in milliseconds
     *
     * @return Remaining time in milliseconds, 0 if not logged in
     */
    public long getRemainingSessionTime() {
        if (!isLoggedIn) {
            return 0;
        }

        long elapsed = System.currentTimeMillis() - lastActivityTime;
        long remaining = SESSION_TIMEOUT - elapsed;
        return Math.max(0, remaining);
    }

    /**
     * Checks if session timeout warning should be shown
     *
     * @return true if warning should be displayed
     */
    public boolean shouldShowTimeoutWarning() {
        if (!isLoggedIn) {
            return false;
        }

        long remaining = getRemainingSessionTime();
        return remaining <= WARNING_TIME && remaining > 0;
    }

    /**
     * Gets remaining session time formatted as string
     *
     * @return Formatted time string (e.g., "15:30")
     */
    public String getFormattedRemainingTime() {
        long remaining = getRemainingSessionTime();
        if (remaining <= 0) {
            return "00:00";
        }

        long minutes = remaining / (60 * 1000);
        long seconds = (remaining % (60 * 1000)) / 1000;
        return String.format("%02d:%02d", minutes, seconds);
    }

    /**
     * Checks if the current user has the specified role
     *
     * @param role The role to check
     * @return true if user has the role
     */
    public boolean hasRole(String role) {
        return currentUser != null && role != null &&
                role.equalsIgnoreCase(currentUser.getRole());
    }

    /**
     * Checks if the current user has any of the specified roles
     *
     * @param roles The roles to check
     * @return true if user has any of the roles
     */
    public boolean hasAnyRole(String... roles) {
        if (currentUser == null || roles == null) {
            return false;
        }

        for (String role : roles) {
            if (hasRole(role)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if the current user is an admin
     *
     * @return true if user has admin role
     */
    public boolean isAdmin() {
        return hasRole("admin");
    }

    /**
     * Checks if the current user is staff (admin or staff role)
     *
     * @return true if user has admin or staff role
     */
    public boolean isStaff() {
        return hasAnyRole("admin", "staff");
    }

    /**
     * Checks if the current user is a guest
     *
     * @return true if user has guest role
     */
    public boolean isGuest() {
        return hasRole("guest");
    }

    /**
     * Validates password strength and format
     *
     * @param password The password to validate
     * @return ValidationResult with success status and message
     */
    public ValidationResult validatePassword(String password) {
        if (password == null || password.isEmpty()) {
            return new ValidationResult(false, "Password cannot be empty");
        }

        if (password.length() < 8) {
            return new ValidationResult(false, "Password must be at least 8 characters long");
        }

        if (password.length() > 128) {
            return new ValidationResult(false, "Password cannot exceed 128 characters");
        }

        // Check for basic requirements
        boolean hasLower = password.matches(".[a-z].");
        boolean hasUpper = password.matches(".[A-Z].");
        boolean hasDigit = password.matches(".\\d.");
        boolean hasSpecial = password.matches(".[@$!%?&].*");

        if (!hasLower) {
            return new ValidationResult(false, "Password must contain at least one lowercase letter");
        }

        if (!hasUpper) {
            return new ValidationResult(false, "Password must contain at least one uppercase letter");
        }

        if (!hasDigit) {
            return new ValidationResult(false, "Password must contain at least one digit");
        }

        if (!hasSpecial) {
            return new ValidationResult(false, "Password must contain at least one special character (@$!%*?&)");
        }

        // Check for common weak passwords
        String lowerPassword = password.toLowerCase();
        String[] weakPasswords = {"password", "123456", "admin", "guest", "hotel"};
        for (String weak : weakPasswords) {
            if (lowerPassword.contains(weak)) {
                return new ValidationResult(false, "Password contains common weak patterns");
            }
        }

        return new ValidationResult(true, "Password is valid");
    }

    /**
     * Validates username format
     *
     * @param username The username to validate
     * @return ValidationResult with success status and message
     */
    public ValidationResult validateUsernameFormat(String username) {
        if (username == null || username.trim().isEmpty()) {
            return new ValidationResult(false, "Username cannot be empty");
        }

        username = username.trim();

        if (username.length() < 3) {
            return new ValidationResult(false, "Username must be at least 3 characters long");
        }

        if (username.length() > 20) {
            return new ValidationResult(false, "Username cannot exceed 20 characters");
        }

        if (!USERNAME_PATTERN.matcher(username).matches()) {
            return new ValidationResult(false, "Username can only contain letters, numbers, and underscores");
        }

        return new ValidationResult(true, "Username is valid");
    }

    /**
     * Validates username availability (not already taken)
     *
     * @param username The username to check
     * @return ValidationResult with availability status
     */
    public ValidationResult validateUsernameAvailability(String username) {
        try {
            User existingUser = userDAO.getUserByUsername(username);
            if (existingUser != null) {
                return new ValidationResult(false, "Username is already taken");
            }
            return new ValidationResult(true, "Username is available");
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error checking username availability: " + username, e);
            return new ValidationResult(false, "Error checking username availability");
        }
    }

    /**
     * Changes the password for the current user
     *
     * @param currentPassword The current password for verification
     * @param newPassword The new password to set
     * @return ValidationResult with success status and message
     */
    public ValidationResult changePassword(String currentPassword, String newPassword) {
        if (!isLoggedIn()) {
            return new ValidationResult(false, "No user is currently logged in");
        }

        // Validate current password
        try {
            User verifyUser = userDAO.authenticate(currentUser.getUsername(), currentPassword);
            if (verifyUser == null) {
                return new ValidationResult(false, "Current password is incorrect");
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error verifying current password for user: " + currentUser.getUsername(), e);
            return new ValidationResult(false, "Error verifying current password");
        }

        // Validate new password
        ValidationResult validation = validatePassword(newPassword);
        if (!validation.isValid()) {
            return validation;
        }

        // Check if new password is same as current
        if (currentPassword != null && currentPassword.equals(newPassword)) {
            return new ValidationResult(false, "New password must be different from current password");
        }

        try {
                    // Update password in database
                    boolean updated = userDAO.updatePassword(currentUser.getId(), newPassword);
                    if (updated) {
                        LOGGER.info("Password changed successfully for user: " + currentUser.getUsername());
                        return new ValidationResult(true, "Password changed successfully");
            } else {
                return new ValidationResult(false, "Failed to update password");
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error changing password for user: " + currentUser.getUsername(), e);
            return new ValidationResult(false, "Error changing password: " + e.getMessage());
        }
    }

    /**
     * Creates a new user account (admin only)
     *
     * @param username The username for the new account
     * @param password The password for the new account
     * @param fullName The full name of the user
     * @param role The role for the new account
     * @return ValidationResult with success status and message
     */
    public ValidationResult createUserAccount(String username, String password, String fullName, String role) {
        if (!isAdmin()) {
            return new ValidationResult(false, "Only administrators can create user accounts");
        }

        // Validate username
        ValidationResult usernameValidation = validateUsernameFormat(username);
        if (!usernameValidation.isValid()) {
            return usernameValidation;
        }

        ValidationResult availabilityValidation = validateUsernameAvailability(username);
        if (!availabilityValidation.isValid()) {
            return availabilityValidation;
        }

        // Validate password
        ValidationResult passwordValidation = validatePassword(password);
        if (!passwordValidation.isValid()) {
            return passwordValidation;
        }

        // Validate other fields
        if (fullName == null || fullName.trim().isEmpty()) {
            return new ValidationResult(false, "Full name is required");
        }

        if (role == null || (!role.equals("admin") && !role.equals("staff") && !role.equals("guest"))) {
            return new ValidationResult(false, "Invalid role. Must be admin, staff, or guest");
        }

        try {
            User newUser = new User();
            newUser.setUsername(username.trim());
            newUser.setPassword(password);
            newUser.setFullname(fullName.trim());
            newUser.setRole(role);

            boolean created = userDAO.addUser(newUser);
            if (created) {
                LOGGER.info("User account created successfully: " + username + " (Role: " + role + ") by admin: " + currentUser.getUsername());
                return new ValidationResult(true, "User account created successfully");
            } else {
                return new ValidationResult(false, "Failed to create user account");
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error creating user account: " + username, e);
            return new ValidationResult(false, "Error creating user account: " + e.getMessage());
        }
    }

    /**
     * Increments failed login attempts and checks for lockout
     */
    private void incrementFailedAttempts() {
        failedLoginAttempts++;
        lastFailedLoginTime = System.currentTimeMillis();

        if (failedLoginAttempts >= MAX_LOGIN_ATTEMPTS) {
            isAccountLocked = true;
            LOGGER.warning("Account locked due to " + failedLoginAttempts + " failed login attempts");
        }
    }

    /**
     * Resets failed login attempts
     */
    private void resetFailedAttempts() {
        failedLoginAttempts = 0;
        lastFailedLoginTime = 0;
        isAccountLocked = false;
    }

    /**
     * Checks if account is currently locked
     *
     * @return true if account is locked
     */
    private boolean isAccountLocked() {
        if (!isAccountLocked) {
            return false;
        }

        long lockoutExpiry = lastFailedLoginTime + LOCKOUT_DURATION;
        return System.currentTimeMillis() < lockoutExpiry;
    }

    /**
     * Gets remaining lockout time in milliseconds
     *
     * @return Remaining lockout time
     */
    private long getRemainingLockoutTime() {
        if (!isAccountLocked()) {
            return 0;
        }

        long lockoutExpiry = lastFailedLoginTime + LOCKOUT_DURATION;
        return Math.max(0, lockoutExpiry - System.currentTimeMillis());
    }

    /**
     * Shows session timeout warning to user
     */
    public void showSessionTimeoutWarning() {
        if (!shouldShowTimeoutWarning()) {
            return;
        }

        long remaining = getRemainingSessionTime();
        int minutes = (int) (remaining / (60 * 1000));

        SwingUtilities.invokeLater(() -> {
            int option = JOptionPane.showConfirmDialog(
                    null,
                    "Your session will expire in " + minutes + " minute(s).\nDo you want to continue?",
                    "Session Timeout Warning",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE
            );

            if (option == JOptionPane.YES_OPTION) {
                refreshSession();
            } else {
                logout();
            }
        });
    }

    /**
     * Gets session information for display
     *
     * @return SessionInfo object with current session details
     */
    public SessionInfo getSessionInfo() {
        if (!isLoggedIn()) {
            return new SessionInfo(false, null, null, null, 0, 0);
        }

        LocalDateTime loginTime = LocalDateTime.ofInstant(
                java.time.Instant.ofEpochMilli(sessionStartTime),
                java.time.ZoneId.systemDefault()
        );

        LocalDateTime lastActivity = LocalDateTime.ofInstant(
                java.time.Instant.ofEpochMilli(lastActivityTime),
                java.time.ZoneId.systemDefault()
        );

        return new SessionInfo(
                true,
                sessionId,
                loginTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                lastActivity.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                getRemainingSessionTime(),
                SESSION_TIMEOUT
        );
    }

    /**
     * Validates email format (if used for username)
     *
     * @param email The email to validate
     * @return ValidationResult with validation status
     */
    public ValidationResult validateEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return new ValidationResult(false, "Email cannot be empty");
        }

        String emailPattern = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
        if (!Pattern.matches(emailPattern, email.trim())) {
            return new ValidationResult(false, "Invalid email format");
        }

        return new ValidationResult(true, "Email is valid");
    }

    // Inner Classes

    /**
     * Inner class to represent authentication results
     */
    public static class AuthenticationResult {
        private final boolean success;
        private final String message;
        private final User user;

        public AuthenticationResult(boolean success, String message, User user) {
            this.success = success;
            this.message = message;
            this.user = user;
        }

        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public User getUser() { return user; }

        @Override
        public String toString() {
            return "AuthenticationResult{" +
                    "success=" + success +
                    ", message='" + message + '\'' +
                    ", user=" + (user != null ? user.getUsername() : "null") +
                    '}';
        }
    }

    /**
     * Inner class to represent validation results
     */
    public static class ValidationResult {
        private final boolean valid;
        private final String message;

        public ValidationResult(boolean valid, String message) {
            this.valid = valid;
            this.message = message;
        }

        public boolean isValid() { return valid; }
        public String getMessage() { return message; }

        @Override
        public String toString() {
            return "ValidationResult{" +
                    "valid=" + valid +
                    ", message='" + message + '\'' +
                    '}';
        }
    }

    /**
     * Inner class to represent session information
     */
    public static class SessionInfo {
        private final boolean active;
        private final String sessionId;
        private final String loginTime;
        private final String lastActivity;
        private final long remainingTime;
        private final long totalTimeout;

        public SessionInfo(boolean active, String sessionId, String loginTime,
                           String lastActivity, long remainingTime, long totalTimeout) {
            this.active = active;
            this.sessionId = sessionId;
            this.loginTime = loginTime;
            this.lastActivity = lastActivity;
            this.remainingTime = remainingTime;
            this.totalTimeout = totalTimeout;
        }

        public boolean isActive() { return active; }
        public String getSessionId() { return sessionId; }
        public String getLoginTime() { return loginTime; }
        public String getLastActivity() { return lastActivity; }
        public long getRemainingTime() { return remainingTime; }
        public long getTotalTimeout() { return totalTimeout; }

        @Override
        public String toString() {
            return "SessionInfo{" +
                    "active=" + active +
                    ", sessionId='" + sessionId + '\'' +
                    ", loginTime='" + loginTime + '\'' +
                    ", lastActivity='" + lastActivity + '\'' +
                    ", remainingTime=" + remainingTime +
                    ", totalTimeout=" + totalTimeout +
                    '}';
        }
    }
}

