package service;

import dao.UserDAO;
import model.User;
import java.sql.SQLException;
import java.util.List;

public class UserService {
    private UserDAO userDAO;

    public UserService() {
        this.userDAO = new UserDAO();
    }

    // Constructor with dependency injection for testing
    public UserService(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    /**
     * Create a new user
     * @param username User's username
     * @param password User's password
     * @param role User's role (admin, staff, guest)
     * @param fullname User's full name
     * @return UserResult containing user creation status and information
     */
    public UserResult createUser(String username, String password, String role, String fullname) {
        try {
            // Validate input
            UserValidation validation = validateUserData(username, password, role, fullname);
            if (!validation.isValid()) {
                return new UserResult(false, validation.getMessage(), null);
            }

            // Check if username already exists
            if (userDAO.usernameExists(username.trim())) {
                return new UserResult(false, "Username already exists", null);
            }

            // Create user object
            User user = new User(username.trim(), password, role, fullname.trim());

            // Save to database
            int userId = userDAO.saveUser(user);
            user.setId(userId);

            return new UserResult(true, "User created successfully", user);

        } catch (SQLException e) {
            System.err.println("Database error creating user: " + e.getMessage());
            return new UserResult(false, "Database error: " + e.getMessage(), null);
        } catch (Exception e) {
            System.err.println("Unexpected error creating user: " + e.getMessage());
            return new UserResult(false, "Unexpected error: " + e.getMessage(), null);
        }
    }

    /**
     * Update user information
     * @param user User object with updated information
     * @return true if update successful, false otherwise
     */
    public boolean updateUser(User user) {
        try {
            if (user == null || user.getId() <= 0) {
                return false;
            }

            UserValidation validation = validateUserData(user.getUsername(), user.getPassword(),
                    user.getRole(), user.getFullname());
            if (!validation.isValid()) {
                return false;
            }

            return userDAO.updateUser(user);
        } catch (SQLException e) {
            System.err.println("Error updating user: " + e.getMessage());
            return false;
        }
    }

    /**
     * Delete user by ID
     * @param userId ID of the user to delete
     * @return true if deletion successful, false otherwise
     */
    public boolean deleteUser(int userId) {
        try {
            return userDAO.deleteUser(userId);
        } catch (SQLException e) {
            System.err.println("Error deleting user: " + e.getMessage());
            return false;
        }
    }

    /**
     * Get user by ID
     * @param userId ID of the user
     * @return User object if found, null otherwise
     */
    public User getUserById(int userId) {
        try {
            return userDAO.getUserById(userId);
        } catch (SQLException e) {
            System.err.println("Error retrieving user: " + e.getMessage());
            return null;
        }
    }

    /**
     * Get user by username
     * @param username Username to search for
     * @return User object if found, null otherwise
     */
    public User getUserByUsername(String username) {
        try {
            if (username == null || username.trim().isEmpty()) {
                return null;
            }
            return userDAO.getUserByUsername(username.trim());
        } catch (SQLException e) {
            System.err.println("Error retrieving user: " + e.getMessage());
            return null;
        }
    }

    /**
     * Get all users
     * @return List of all users
     */
    public List<User> getAllUsers() {
        try {
            return userDAO.getAllUsers();
        } catch (SQLException e) {
            System.err.println("Error retrieving users: " + e.getMessage());
            return List.of(); // Return empty list
        }
    }

    /**
     * Get users by role
     * @param role Role to filter by
     * @return List of users with the specified role
     */
    public List<User> getUsersByRole(String role) {
        try {
            if (role == null || role.trim().isEmpty()) {
                return List.of();
            }
            return userDAO.getUsersByRole(role.trim());
        } catch (SQLException e) {
            System.err.println("Error retrieving users by role: " + e.getMessage());
            return List.of();
        }
    }

    /**
     * Check if username is available
     * @param username Username to check
     * @return true if username is available, false if taken
     */
    public boolean isUsernameAvailable(String username) {
        try {
            if (username == null || username.trim().isEmpty()) {
                return false;
            }
            return !userDAO.usernameExists(username.trim());
        } catch (SQLException e) {
            System.err.println("Error checking username availability: " + e.getMessage());
            return false;
        }
    }

    /**
     * Get total user count
     * @return Total number of users
     */
    public int getTotalUserCount() {
        try {
            return userDAO.getTotalUserCount();
        } catch (SQLException e) {
            System.err.println("Error getting user count: " + e.getMessage());
            return 0;
        }
    }

    /**
     * Change user password
     * @param userId ID of the user
     * @param newPassword New password
     * @return true if password change successful, false otherwise
     */
    public boolean changePassword(int userId, String newPassword) {
        try {
            if (newPassword == null || newPassword.length() < 6) {
                return false;
            }

            User user = userDAO.getUserById(userId);
            if (user == null) {
                return false;
            }

            user.setPassword(newPassword);
            return userDAO.updateUser(user);
        } catch (SQLException e) {
            System.err.println("Error changing password: " + e.getMessage());
            return false;
        }
    }

    /**
     * Change user role
     * @param userId ID of the user
     * @param newRole New role
     * @return true if role change successful, false otherwise
     */
    public boolean changeUserRole(int userId, String newRole) {
        try {
            if (!isValidRole(newRole)) {
                return false;
            }

            User user = userDAO.getUserById(userId);
            if (user == null) {
                return false;
            }

            user.setRole(newRole);
            return userDAO.updateUser(user);
        } catch (SQLException e) {
            System.err.println("Error changing user role: " + e.getMessage());
            return false;
        }
    }

    /**
     * Validate user data
     * @param username Username
     * @param password Password
     * @param role Role
     * @param fullname Full name
     * @return UserValidation result
     */
    private UserValidation validateUserData(String username, String password, String role, String fullname) {
        if (username == null || username.trim().isEmpty()) {
            return new UserValidation(false, "Username is required");
        }

        if (username.trim().length() < 3) {
            return new UserValidation(false, "Username must be at least 3 characters long");
        }

        if (password == null || password.length() < 6) {
            return new UserValidation(false, "Password must be at least 6 characters long");
        }

        if (!isValidRole(role)) {
            return new UserValidation(false, "Invalid role. Must be admin, staff, or guest");
        }

        if (fullname == null || fullname.trim().isEmpty()) {
            return new UserValidation(false, "Full name is required");
        }

        return new UserValidation(true, "User data is valid");
    }

    /**
     * Check if role is valid
     * @param role Role to check
     * @return true if valid, false otherwise
     */
    private boolean isValidRole(String role) {
        return role != null && (role.equals("admin") || role.equals("staff") || role.equals("guest"));
    }

    /**
     * Inner class for user operation results
     */
    public static class UserResult {
        private boolean success;
        private String message;
        private User user;

        public UserResult(boolean success, String message, User user) {
            this.success = success;
            this.message = message;
            this.user = user;
        }

        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public User getUser() { return user; }
    }

    /**
     * Inner class for user validation
     */
    private static class UserValidation {
        private boolean valid;
        private String message;

        public UserValidation(boolean valid, String message) {
            this.valid = valid;
            this.message = message;

        }

        public boolean isValid() { return valid; }
        public String getMessage() { return message; }
    }
}