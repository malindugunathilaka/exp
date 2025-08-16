package ui;

import model.User;
import javax.swing.*;
import java.awt.*;

public class UserDialog extends JDialog {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JTextField fullnameField;
    private JComboBox<String> roleCombo;
    private boolean confirmed = false;
    private User user;
    private boolean isEditMode = false;

    public UserDialog(JFrame parent, String title, boolean modal) {
        this(parent, title, modal, null);
    }

    public UserDialog(JFrame parent, String title, boolean modal, User existingUser) {
        super(parent, title, modal);
        this.user = existingUser;
        this.isEditMode = (existingUser != null);
        initializeUI();
        setupEventHandlers();

        if (existingUser != null) {
            populateFields(existingUser);
        }

        pack();
        setLocationRelativeTo(parent);
    }

    private void initializeUI() {
        setLayout(new BorderLayout());

        // Create form panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        // Username
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Username:"), gbc);
        gbc.gridx = 1;
        usernameField = new JTextField(20);
        if (isEditMode) {
            usernameField.setEditable(false); // Don't allow username changes in edit mode
            usernameField.setBackground(new Color(240, 240, 240));
        }
        formPanel.add(usernameField, gbc);

        // Password
        gbc.gridx = 0; gbc.gridy = 1;
        JLabel passwordLabel = new JLabel(isEditMode ? "New Password:" : "Password:");
        formPanel.add(passwordLabel, gbc);
        gbc.gridx = 1;
        passwordField = new JPasswordField(20);
        formPanel.add(passwordField, gbc);

        if (isEditMode) {
            gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
            JLabel passwordNote = new JLabel("(Leave blank to keep current password)");
            passwordNote.setFont(new Font("Arial", Font.ITALIC, 11));
            passwordNote.setForeground(Color.GRAY);
            formPanel.add(passwordNote, gbc);
            gbc.gridwidth = 1;
            gbc.gridy = 3;
        } else {
            gbc.gridy = 2;
        }

        // Full Name
        gbc.gridx = 0;
        formPanel.add(new JLabel("Full Name:"), gbc);
        gbc.gridx = 1;
        fullnameField = new JTextField(20);
        formPanel.add(fullnameField, gbc);

        // Role
        gbc.gridx = 0; gbc.gridy = isEditMode ? 4 : 3;
        formPanel.add(new JLabel("Role:"), gbc);
        gbc.gridx = 1;
        roleCombo = new JComboBox<>(new String[]{"guest", "staff", "admin"});
        formPanel.add(roleCombo, gbc);

        add(formPanel, BorderLayout.CENTER);

        // Create button panel
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 20, 20));

        JButton saveButton = new JButton(isEditMode ? "Update" : "Save");
        JButton cancelButton = new JButton("Cancel");

        saveButton.setPreferredSize(new Dimension(80, 30));
        cancelButton.setPreferredSize(new Dimension(80, 30));

        saveButton.addActionListener(e -> saveUser());
        cancelButton.addActionListener(e -> cancel());

        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

        add(buttonPanel, BorderLayout.SOUTH);

        // Set default button
        getRootPane().setDefaultButton(saveButton);
    }

    private void setupEventHandlers() {
        // Username validation
        usernameField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                char c = evt.getKeyChar();
                if (!Character.isLetterOrDigit(c) && c != '_' && c != '\b') {
                    evt.consume();
                }
            }
        });

        // Focus to first editable field
        SwingUtilities.invokeLater(() -> {
            if (isEditMode) {
                fullnameField.requestFocus();
            } else {
                usernameField.requestFocus();
            }
        });
    }

    private void populateFields(User user) {
        usernameField.setText(user.getUsername());
        fullnameField.setText(user.getFullname());
        roleCombo.setSelectedItem(user.getRole());
        // Don't populate password field for security
    }

    private void saveUser() {
        try {
            // Validate input
            String username = usernameField.getText().trim();
            if (username.isEmpty()) {
                showError("Username is required.");
                usernameField.requestFocus();
                return;
            }

            if (username.length() < 3) {
                showError("Username must be at least 3 characters long.");
                usernameField.requestFocus();
                return;
            }

            String password = new String(passwordField.getPassword());
            if (!isEditMode && password.isEmpty()) {
                showError("Password is required.");
                passwordField.requestFocus();
                return;
            }

            if (!password.isEmpty() && password.length() < 6) {
                showError("Password must be at least 6 characters long.");
                passwordField.requestFocus();
                return;
            }

            String fullname = fullnameField.getText().trim();
            if (fullname.isEmpty()) {
                showError("Full name is required.");
                fullnameField.requestFocus();
                return;
            }

            String role = (String) roleCombo.getSelectedItem();

            // Create or update user object
            if (user == null) {
                user = new User();
            }

            user.setUsername(username);
            if (!password.isEmpty()) {
                user.setPassword(password);
            }
            user.setFullname(fullname);
            user.setRole(role);

            confirmed = true;
            dispose();

        } catch (Exception e) {
            showError("Error saving user: " + e.getMessage());
        }
    }

    private void cancel() {
        confirmed = false;
        dispose();
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Input Error", JOptionPane.ERROR_MESSAGE);
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public User getUser() {
        return user;
    }

    // Override setVisible to center dialog
    @Override
    public void setVisible(boolean visible) {
        if (visible) {
            setLocationRelativeTo(getParent());
        }
        super.setVisible(visible);
    }
}