package ui;

import model.Room;
import javax.swing.*;
import java.awt.*;

public class RoomDialog extends JDialog {
    private JTextField roomNumberField;
    private JComboBox<String> typeCombo;
    private JTextField priceField;
    private JComboBox<String> statusCombo;
    private boolean confirmed = false;
    private Room room;

    public RoomDialog(JFrame parent, String title, boolean modal) {
        this(parent, title, modal, null);
    }

    public RoomDialog(JFrame parent, String title, boolean modal, Room existingRoom) {
        super(parent, title, modal);
        this.room = existingRoom;
        initializeUI();
        setupEventHandlers();

        if (existingRoom != null) {
            populateFields(existingRoom);
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

        // Room Number
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Room Number:"), gbc);
        gbc.gridx = 1;
        roomNumberField = new JTextField(15);
        formPanel.add(roomNumberField, gbc);

        // Room Type
        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("Room Type:"), gbc);
        gbc.gridx = 1;
        typeCombo = new JComboBox<>(new String[]{"Standard", "Deluxe", "Suite"});
        formPanel.add(typeCombo, gbc);

        // Price
        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(new JLabel("Price per Night:"), gbc);
        gbc.gridx = 1;
        priceField = new JTextField(15);
        formPanel.add(priceField, gbc);

        // Status
        gbc.gridx = 0; gbc.gridy = 3;
        formPanel.add(new JLabel("Status:"), gbc);
        gbc.gridx = 1;
        statusCombo = new JComboBox<>(new String[]{"Available", "Occupied", "Maintenance"});
        formPanel.add(statusCombo, gbc);

        add(formPanel, BorderLayout.CENTER);

        // Create button panel
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 20, 20));

        JButton saveButton = new JButton("Save");
        JButton cancelButton = new JButton("Cancel");

        saveButton.setPreferredSize(new Dimension(80, 30));
        cancelButton.setPreferredSize(new Dimension(80, 30));

        saveButton.addActionListener(e -> saveRoom());
        cancelButton.addActionListener(e -> cancel());

        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

        add(buttonPanel, BorderLayout.SOUTH);

        // Set default button
        getRootPane().setDefaultButton(saveButton);
    }

    private void setupEventHandlers() {
        // Add input validation
        priceField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                char c = evt.getKeyChar();
                if (!Character.isDigit(c) && c != '.' && c != '\b') {
                    evt.consume();
                }
            }
        });

        // Focus to first field
        SwingUtilities.invokeLater(() -> roomNumberField.requestFocus());
    }

    private void populateFields(Room room) {
        roomNumberField.setText(room.getRoomNumber());
        typeCombo.setSelectedItem(room.getType());
        priceField.setText(String.valueOf(room.getPrice()));
        statusCombo.setSelectedItem(room.getStatus());
    }

    private void saveRoom() {
        try {
            // Validate input
            String roomNumber = roomNumberField.getText().trim();
            if (roomNumber.isEmpty()) {
                showError("Room number is required.");
                roomNumberField.requestFocus();
                return;
            }

            String priceText = priceField.getText().trim();
            if (priceText.isEmpty()) {
                showError("Price is required.");
                priceField.requestFocus();
                return;
            }

            double price;
            try {
                price = Double.parseDouble(priceText);
                if (price <= 0) {
                    showError("Price must be greater than 0.");
                    priceField.requestFocus();
                    return;
                }
            } catch (NumberFormatException e) {
                showError("Please enter a valid price.");
                priceField.requestFocus();
                return;
            }

            // Create or update room object
            if (room == null) {
                room = new Room();
            }

            room.setRoomNumber(roomNumber);
            room.setType((String) typeCombo.getSelectedItem());
            room.setPrice(price);
            room.setStatus((String) statusCombo.getSelectedItem());

            confirmed = true;
            dispose();

        } catch (Exception e) {
            showError("Error saving room: " + e.getMessage());
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

    public Room getRoom() {
        return room;
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