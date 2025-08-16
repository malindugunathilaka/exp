package ui;
import dao.RoomDAO;
import model.Room;
import utill.UIConstants;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.KeyEvent;

/**
 * RoomDialog provides a dialog interface for adding new rooms or editing existing ones.
 * Includes validation and user-friendly error handling.
 */
public class RoomDialog extends JDialog {

    private final RoomDAO roomDAO;
    private final Room existingRoom;
    private boolean roomSaved = false;

    // Form components
    private JTextField roomNumberField;
    private JComboBox<String> typeComboBox;
    private JSpinner priceSpinner;
    private JComboBox<String> statusComboBox;
    private JTextArea descriptionArea;
    private JSpinner capacitySpinner;
    private JCheckBox wifiCheckBox;
    private JCheckBox acCheckBox;
    private JCheckBox tvCheckBox;
    private JCheckBox minibarCheckBox;

    // Buttons
    private JButton saveButton;
    private JButton cancelButton;

    /**
     * Constructor for adding a new room
     */
    public RoomDialog(Frame parent, RoomDAO roomDAO) {
        this(parent, roomDAO, null);
    }

    /**
     * Constructor for editing an existing room
     */
    public RoomDialog(Frame parent, RoomDAO roomDAO, Room existingRoom) {
        super(parent, existingRoom == null ? "Add New Room" : "Edit Room", true);
        this.roomDAO = roomDAO;
        this.existingRoom = existingRoom;

        initializeComponents();
        layoutComponents();
        setupEventHandlers();
        populateFields();

        pack();
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        // Set initial focus
        SwingUtilities.invokeLater(() -> roomNumberField.requestFocusInWindow());
    }

    /**
     * Initializes all form components
     */
    private void initializeComponents() {
        // Room number field
        roomNumberField = new JTextField(10);
        roomNumberField.setFont(UIConstants.FONT_REGULAR);
        roomNumberField.setBorder(UIConstants.createRoundedBorder(UIConstants.BORDER_COLOR, 5));

        // Room type combo box
        typeComboBox = new JComboBox<>(new String[]{
                "Standard", "Deluxe", "Suite", "Presidential", "Family", "Business"
        });
        typeComboBox.setFont(UIConstants.FONT_REGULAR);

        // Price spinner
        priceSpinner = new JSpinner(new SpinnerNumberModel(100.0, 0.0, 10000.0, 1.0));
        priceSpinner.setFont(UIConstants.FONT_REGULAR);
        JSpinner.NumberEditor priceEditor = new JSpinner.NumberEditor(priceSpinner, "$#,##0.00");
        priceSpinner.setEditor(priceEditor);

        // Status combo box
        statusComboBox = new JComboBox<>(new String[]{
                "Available", "Occupied", "Maintenance", "Out of Order", "Cleaning"
        });
        statusComboBox.setFont(UIConstants.FONT_REGULAR);

        // Description text area
        descriptionArea = new JTextArea(3, 20);
        descriptionArea.setFont(UIConstants.FONT_REGULAR);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        descriptionArea.setBorder(UIConstants.createRoundedBorder(UIConstants.BORDER_COLOR, 5));

        // Capacity spinner
        capacitySpinner = new JSpinner(new SpinnerNumberModel(2, 1, 10, 1));
        capacitySpinner.setFont(UIConstants.FONT_REGULAR);

        // Amenity checkboxes
        wifiCheckBox = new JCheckBox("WiFi");
        wifiCheckBox.setFont(UIConstants.FONT_REGULAR);
        wifiCheckBox.setSelected(true); // Default amenity

        acCheckBox = new JCheckBox("Air Conditioning");
        acCheckBox.setFont(UIConstants.FONT_REGULAR);
        acCheckBox.setSelected(true); // Default amenity

        tvCheckBox = new JCheckBox("Television");
        tvCheckBox.setFont(UIConstants.FONT_REGULAR);
        tvCheckBox.setSelected(true); // Default amenity

        minibarCheckBox = new JCheckBox("Mini Bar");
        minibarCheckBox.setFont(UIConstants.FONT_REGULAR);

        // Buttons
        saveButton = new JButton(existingRoom == null ? "Add Room" : "Save Changes");
        saveButton.setFont(UIConstants.FONT_BUTTON);
        saveButton.setBackground(UIConstants.BUTTON_SUCCESS);
        saveButton.setForeground(UIConstants.TEXT_LIGHT);
        saveButton.setPreferredSize(new Dimension(120, UIConstants.BUTTON_HEIGHT));
        saveButton.setFocusPainted(false);

        cancelButton = new JButton("Cancel");
        cancelButton.setFont(UIConstants.FONT_BUTTON);
        cancelButton.setBackground(UIConstants.BUTTON_SECONDARY);
        cancelButton.setForeground(UIConstants.TEXT_LIGHT);
        cancelButton.setPreferredSize(new Dimension(100, UIConstants.BUTTON_HEIGHT));
        cancelButton.setFocusPainted(false);

        // Add mnemonics for accessibility
        saveButton.setMnemonic(KeyEvent.VK_S);
        cancelButton.setMnemonic(KeyEvent.VK_C);
    }

    /**
     * Layouts all components using GridBagLayout
     */
    private void layoutComponents() {
        setLayout(new BorderLayout());

        // Main content panel
        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setBorder(new EmptyBorder(20, 20, 10, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        // Room Number
        gbc.gridx = 0; gbc.gridy = 0;
        contentPanel.add(new JLabel("Room Number: *"), gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        contentPanel.add(roomNumberField, gbc);

        // Room Type
        gbc.gridx = 0; gbc.gridy = 1;
        gbc.fill = GridBagConstraints.NONE;
        contentPanel.add(new JLabel("Room Type: *"), gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        contentPanel.add(typeComboBox, gbc);

        // Price
        gbc.gridx = 0; gbc.gridy = 2;
        gbc.fill = GridBagConstraints.NONE;
        contentPanel.add(new JLabel("Price per Night: *"), gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        contentPanel.add(priceSpinner, gbc);

        // Capacity
        gbc.gridx = 0; gbc.gridy = 3;
        gbc.fill = GridBagConstraints.NONE;
        contentPanel.add(new JLabel("Capacity:"), gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        contentPanel.add(capacitySpinner, gbc);

        // Status
        gbc.gridx = 0; gbc.gridy = 4;
        gbc.fill = GridBagConstraints.NONE;
        contentPanel.add(new JLabel("Status:"), gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        contentPanel.add(statusComboBox, gbc);

        // Amenities section
        gbc.gridx = 0; gbc.gridy = 5;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        contentPanel.add(createAmenitiesPanel(), gbc);

        // Description
        gbc.gridx = 0; gbc.gridy = 6;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        contentPanel.add(new JLabel("Description:"), gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        contentPanel.add(new JScrollPane(descriptionArea), gbc);

        // Required fields note
        gbc.gridx = 0; gbc.gridy = 7;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 0.0;
        gbc.weighty = 0.0;
        gbc.anchor = GridBagConstraints.WEST;
        JLabel requiredLabel = new JLabel("* Required fields");
        requiredLabel.setFont(UIConstants.FONT_SMALL);
        requiredLabel.setForeground(UIConstants.TEXT_MUTED);
        contentPanel.add(requiredLabel, gbc);

        add(contentPanel, BorderLayout.CENTER);

        // Button panel
        JPanel buttonPanel = createButtonPanel();
        add(buttonPanel, BorderLayout.SOUTH);
    }

    /**
     * Creates the amenities panel with checkboxes
     */
    private JPanel createAmenitiesPanel() {
        JPanel amenitiesPanel = new JPanel();
        amenitiesPanel.setBorder(UIConstants.createTitleBorder("Amenities"));
        amenitiesPanel.setLayout(new GridLayout(2, 2, 10, 5));

        amenitiesPanel.add(wifiCheckBox);
        amenitiesPanel.add(acCheckBox);
        amenitiesPanel.add(tvCheckBox);
        amenitiesPanel.add(minibarCheckBox);

        return amenitiesPanel;
    }

    /**
     * Creates the button panel
     */
    private JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        buttonPanel.setBorder(new EmptyBorder(0, 10, 10, 10));

        buttonPanel.add(cancelButton);
        buttonPanel.add(saveButton);

        return buttonPanel;
    }

    /**
     * Sets up event handlers for all components
     */
    private void setupEventHandlers() {
        // Save button action
        saveButton.addActionListener(e -> saveRoom());

        // Cancel button action
        cancelButton.addActionListener(e -> dispose());

        // Enter key in room number field
        roomNumberField.addActionListener(e -> typeComboBox.requestFocusInWindow());

        // Validation listeners
        roomNumberField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { validateForm(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { validateForm(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { validateForm(); }
        });

        priceSpinner.addChangeListener(e -> validateForm());

        // ESC key to cancel
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "cancel");
        getRootPane().getActionMap().put("cancel", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                dispose();
            }
        });

        // Enter key to save
        getRootPane().setDefaultButton(saveButton);
    }

    /**
     * Populates fields if editing an existing room
     */
    private void populateFields() {
        if (existingRoom != null) {
            roomNumberField.setText(existingRoom.getRoomNumber());
            roomNumberField.setEditable(false); // Don't allow changing room number

            typeComboBox.setSelectedItem(existingRoom.getType());
            priceSpinner.setValue(existingRoom.getPrice());
            statusComboBox.setSelectedItem(existingRoom.getStatus());

            if (existingRoom.getDescription() != null) {
                descriptionArea.setText(existingRoom.getDescription());
            }

            // Set capacity if available (assuming it's stored in description or separate field)
            capacitySpinner.setValue(2); // Default value

            // Parse amenities from description or use default values
            String desc = existingRoom.getDescription();
            if (desc != null) {
                wifiCheckBox.setSelected(desc.toLowerCase().contains("wifi"));
                acCheckBox.setSelected(desc.toLowerCase().contains("air conditioning") || desc.toLowerCase().contains("ac"));
                tvCheckBox.setSelected(desc.toLowerCase().contains("tv") || desc.toLowerCase().contains("television"));
                minibarCheckBox.setSelected(desc.toLowerCase().contains("mini bar") || desc.toLowerCase().contains("minibar"));
            }
        }

        validateForm();
    }

    /**
     * Validates the form and enables/disables save button
     */
    private void validateForm() {
        boolean isValid = true;
        StringBuilder errors = new StringBuilder();

        // Check room number
        String roomNumber = roomNumberField.getText().trim();
        if (roomNumber.isEmpty()) {
            isValid = false;
            roomNumberField.setBorder(UIConstants.createRoundedBorder(UIConstants.DANGER_COLOR, 5));
        } else {
            roomNumberField.setBorder(UIConstants.createRoundedBorder(UIConstants.BORDER_COLOR, 5));
        }

        // Check price
        double price = (Double) priceSpinner.getValue();
        if (price <= 0) {
            isValid = false;
            errors.append("Price must be greater than 0\n");
        }

        saveButton.setEnabled(isValid);

        // Update tooltip with errors if any
        if (!isValid && errors.length() > 0) {
            saveButton.setToolTipText(errors.toString().trim());
        } else {
            saveButton.setToolTipText(null);
        }
    }

    /**
     * Saves the room data
     */
    private void saveRoom() {
        if (!validateInputs()) {
            return;
        }

        try {
            Room room = createRoomFromInputs();

            boolean success;
            if (existingRoom == null) {
                // Adding new room
                success = roomDAO.addRoom(room);
            } else {
                // Updating existing room
                room.setId(existingRoom.getId());
                success = roomDAO.updateRoom(room);
            }

            if (success) {
                roomSaved = true;
                JOptionPane.showMessageDialog(this,
                        existingRoom == null ? "Room added successfully!" : "Room updated successfully!",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);
                dispose();
            } else {
                JOptionPane.showMessageDialog(this,
                        "Failed to save room. Please try again.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Error saving room: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Validates all inputs before saving
     */
    private boolean validateInputs() {
        StringBuilder errors = new StringBuilder();

        // Validate room number
        String roomNumber = roomNumberField.getText().trim();
        if (roomNumber.isEmpty()) {
            errors.append("Room number is required.\n");
        } else if (roomNumber.length() > 10) {
            errors.append("Room number cannot exceed 10 characters.\n");
        }

        // Validate price
        double price = (Double) priceSpinner.getValue();
        if (price <= 0) {
            errors.append("Price must be greater than 0.\n");
        } else if (price > 10000) {
            errors.append("Price cannot exceed $10,000.\n");
        }

        // Check for duplicate room number (only when adding new room)
        if (existingRoom == null && !roomNumber.isEmpty()) {
            try {
                Room existing = roomDAO.getRoomByNumber(roomNumber);
                if (existing != null) {
                    errors.append("Room number already exists.\n");
                }
            } catch (Exception e) {
                errors.append("Error checking room number: ").append(e.getMessage()).append("\n");
            }
        }

        if (errors.length() > 0) {
            JOptionPane.showMessageDialog(this,
                    errors.toString().trim(),
                    "Validation Error",
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }

        return true;
    }

    /**
     * Creates a Room object from the form inputs
     */
    private Room createRoomFromInputs() {
        Room room = new Room();

        room.setRoomNumber(roomNumberField.getText().trim());
        room.setType((String) typeComboBox.getSelectedItem());
        room.setPrice((Double) priceSpinner.getValue());
        room.setStatus((String) statusComboBox.getSelectedItem());

        // Build description with amenities
        StringBuilder description = new StringBuilder();
        String userDescription = descriptionArea.getText().trim();
        if (!userDescription.isEmpty()) {
            description.append(userDescription);
        }

        // Add amenities to description
        StringBuilder amenities = new StringBuilder();
        if (wifiCheckBox.isSelected()) amenities.append("WiFi, ");
        if (acCheckBox.isSelected()) amenities.append("Air Conditioning, ");
        if (tvCheckBox.isSelected()) amenities.append("Television, ");
        if (minibarCheckBox.isSelected()) amenities.append("Mini Bar, ");

        if (amenities.length() > 0) {
            if (description.length() > 0) {
                description.append("\n\n");
            }
            description.append("Amenities: ");
            description.append(amenities.toString().replaceAll(", $", ""));
        }

        // Add capacity info
        int capacity = (Integer) capacitySpinner.getValue();
        if (description.length() > 0) {
            description.append("\n");
        }
        description.append("Capacity: ").append(capacity).append(" guest");
        if (capacity > 1) {
            description.append("s");
        }

        room.setDescription(description.toString());

        return room;
    }

    /**
     * Returns whether the room was successfully saved
     */
    public boolean isRoomSaved() {
        return roomSaved;
    }
}
