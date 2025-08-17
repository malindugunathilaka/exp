package ui;
import service.AuthenticationService;
import model.User;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

public class LoginPanel extends BackgroundPanel {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private AuthenticationService authService;
    private User authenticatedUser;
    private ActionListener loginSuccessListener;

    public LoginPanel() {
        super("src/images/loginbackgound.jpg");
        this.authService = new AuthenticationService(new dao.UserDAO());
        initializeUI();
    }

    private void initializeUI() {
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Title label with background for visibility
        JLabel titleLabel = new JLabel("HOTEL MANAGEMENT SYSTEM", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setOpaque(true);
        titleLabel.setBackground(new Color(0, 0, 0, 150)); // Semi-transparent black
        titleLabel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        add(titleLabel, gbc);

        // Username label
        gbc.gridwidth = 1;
        gbc.gridy = 1;
        JLabel usernameLabel = new JLabel("Username:");
        usernameLabel.setForeground(Color.WHITE);
        usernameLabel.setFont(new Font("Arial", Font.BOLD, 14));
        add(usernameLabel, gbc);

        // Username field
        gbc.gridx = 1;
        usernameField = new JTextField(20);
        usernameField.setFont(new Font("Arial", Font.PLAIN, 14));
        usernameField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createRaisedBevelBorder(),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        add(usernameField, gbc);

        // Password label
        gbc.gridx = 0;
        gbc.gridy = 2;
        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setForeground(Color.WHITE);
        passwordLabel.setFont(new Font("Arial", Font.BOLD, 14));
        add(passwordLabel, gbc);

        // Password field
        gbc.gridx = 1;
        passwordField = new JPasswordField(20);
        passwordField.setFont(new Font("Arial", Font.PLAIN, 14));
        passwordField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createRaisedBevelBorder(),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        add(passwordField, gbc);

        // Login button
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(20, 10, 10, 10);
        loginButton = new JButton("Login");
        loginButton.setFont(new Font("Arial", Font.BOLD, 16));
        loginButton.setPreferredSize(new Dimension(200, 40));
        loginButton.setBackground(new Color(41, 128, 185)); // Nice blue
        loginButton.setForeground(Color.WHITE);
        loginButton.setFocusPainted(false);
        loginButton.setBorder(BorderFactory.createRaisedBevelBorder());

        // Add hover effect
        loginButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                loginButton.setBackground(new Color(52, 152, 219));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                loginButton.setBackground(new Color(41, 128, 185));
            }
        });

        add(loginButton, gbc);

        // Login button action
        loginButton.addActionListener(e -> handleLogin());

        // Allow Enter key to login
        passwordField.addActionListener(e -> handleLogin());

        // Set focus to username field
        SwingUtilities.invokeLater(() -> usernameField.requestFocus());
    }

    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        // Validate input
        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter both username and password");
            return;
        }

        // Authenticate user
        AuthenticationService.AuthenticationResult result = authService.authenticateUser(username, password);
        authenticatedUser = result.getUser();

        if (result.isSuccess()) {
            // Clear password field for security
            passwordField.setText("");

            // Notify parent component of successful login
            if (loginSuccessListener != null) {
                loginSuccessListener.actionPerformed(null);
            }
        } else {
            JOptionPane.showMessageDialog(this, result.getMessage());
            passwordField.setText(""); // Clear password field on failure
            passwordField.requestFocus();
        }
    }

    public void clearFields() {
        usernameField.setText("");
        passwordField.setText("");
        usernameField.requestFocus();
    }

    public User getAuthenticatedUser() {
        return authenticatedUser;
    }

    public void setLoginSuccessListener(ActionListener listener) {
        this.loginSuccessListener = listener;
    }

    // Method to pre-fill username for testing
    public void setUsername(String username) {
        usernameField.setText(username);
        passwordField.requestFocus();
    }
}