package ui;

import dao.RoomDAO;
import model.Room;
import model.User;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

public class RoomsPanel extends JPanel {
    private User currentUser;
    private RoomDAO roomDAO;
    private DefaultTableModel tableModel;
    private JTable roomTable;
    private JButton addButton, editButton, deleteButton, refreshButton;
    private JComboBox<String> statusFilter, typeFilter;

    public RoomsPanel(User currentUser) {
        this.currentUser = currentUser;
        this.roomDAO = new RoomDAO();
        initializeUI();
        loadRoomData();
    }

    private void initializeUI() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Create top panel with filters and controls
        JPanel topPanel = createTopPanel();
        add(topPanel, BorderLayout.NORTH);

        // Create table
        createTable();
        JScrollPane scrollPane = new JScrollPane(roomTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Hotel Rooms"));
        add(scrollPane, BorderLayout.CENTER);

        // Create bottom panel with action buttons
        if ("admin".equals(currentUser.getRole())) {
            JPanel bottomPanel = createBottomPanel();
            add(bottomPanel, BorderLayout.SOUTH);
        }
    }

    private JPanel createTopPanel() {
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

        // Status filter
        topPanel.add(new JLabel("Status:"));
        statusFilter = new JComboBox<>(new String[]{"All", "Available", "Booked", "Occupied", "Maintenance"});
        statusFilter.addActionListener(e -> applyFilters());
        topPanel.add(statusFilter);

        topPanel.add(Box.createHorizontalStrut(20));

        // Type filter
        topPanel.add(new JLabel("Type:"));
        typeFilter = new JComboBox<>(new String[]{"All", "Standard", "Deluxe", "Suite"});
        typeFilter.addActionListener(e -> applyFilters());
        topPanel.add(typeFilter);

        topPanel.add(Box.createHorizontalStrut(20));

        // Refresh button
        refreshButton = new JButton("Refresh");
        refreshButton.setIcon(createRefreshIcon());
        refreshButton.addActionListener(e -> refreshData());
        topPanel.add(refreshButton);

        return topPanel;
    }

    private void createTable() {
        String[] columns = {"ID", "Room Number", "Type", "Price", "Status"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make table read-only
            }
        };

        roomTable = new JTable(tableModel);
        roomTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        roomTable.setRowHeight(25);
        roomTable.getTableHeader().setReorderingAllowed(false);

        // Set column widths
        roomTable.getColumnModel().getColumn(0).setPreferredWidth(50);  // ID
        roomTable.getColumnModel().getColumn(1).setPreferredWidth(100); // Room Number
        roomTable.getColumnModel().getColumn(2).setPreferredWidth(100); // Type
        roomTable.getColumnModel().getColumn(3).setPreferredWidth(80);  // Price
        roomTable.getColumnModel().getColumn(4).setPreferredWidth(100); // Status

        // Hide ID column
        roomTable.getColumnModel().getColumn(0).setMinWidth(0);
        roomTable.getColumnModel().getColumn(0).setMaxWidth(0);
        roomTable.getColumnModel().getColumn(0).setWidth(0);

        // Add row sorter
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(tableModel);
        roomTable.setRowSorter(sorter);

        // Add selection listener for edit/delete buttons
        roomTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                boolean hasSelection = roomTable.getSelectedRow() != -1;
                if (editButton != null) editButton.setEnabled(hasSelection);
                if (deleteButton != null) deleteButton.setEnabled(hasSelection);
            }
        });

        // Add double-click listener for editing
        if ("admin".equals(currentUser.getRole())) {
            roomTable.addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseClicked(java.awt.event.MouseEvent evt) {
                    if (evt.getClickCount() == 2) {
                        editSelectedRoom();
                    }
                }
            });
        }
    }

    private JPanel createBottomPanel() {
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        addButton = new JButton("Add Room");
        addButton.setIcon(createAddIcon());
        addButton.addActionListener(e -> showAddRoomDialog());
        bottomPanel.add(addButton);

        editButton = new JButton("Edit Room");
        editButton.setIcon(createEditIcon());
        editButton.setEnabled(false);
        editButton.addActionListener(e -> editSelectedRoom());
        bottomPanel.add(editButton);

        deleteButton = new JButton("Delete Room");
        deleteButton.setIcon(createDeleteIcon());
        deleteButton.setEnabled(false);
        deleteButton.addActionListener(e -> deleteSelectedRoom());
        bottomPanel.add(deleteButton);

        return bottomPanel;
    }

    public void loadRoomData() {
        try {
            List<Room> rooms = roomDAO.getAllRooms();
            updateTableData(rooms);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading rooms: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateTableData(List<Room> rooms) {
        tableModel.setRowCount(0); // Clear existing data

        for (Room room : rooms) {
            Object[] row = {
                    room.getId(),
                    room.getRoomNumber(),
                    room.getType(),
                    String.format("$%.2f", room.getPrice()),
                    room.getStatus()
            };
            tableModel.addRow(row);
        }
    }

    private void applyFilters() {
        try {
            List<Room> rooms = roomDAO.getAllRooms();

            String selectedStatus = (String) statusFilter.getSelectedItem();
            String selectedType = (String) typeFilter.getSelectedItem();

            // Filter by status
            if (!"All".equals(selectedStatus)) {
                rooms = rooms.stream()
                        .filter(room -> room.getStatus().equals(selectedStatus))
                        .toList();
            }

            // Filter by type
            if (!"All".equals(selectedType)) {
                rooms = rooms.stream()
                        .filter(room -> room.getType().equals(selectedType))
                        .toList();
            }

            updateTableData(rooms);

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error applying filters: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showAddRoomDialog() {
        RoomDialog dialog = new RoomDialog((JFrame) SwingUtilities.getWindowAncestor(this), "Add New Room", true);
        dialog.setVisible(true);

        if (dialog.isConfirmed()) {
            Room newRoom = dialog.getRoom();
            try {
                roomDAO.saveRoom(newRoom);
                loadRoomData(); // Refresh table
                JOptionPane.showMessageDialog(this, "Room added successfully!",
                        "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Error adding room: " + e.getMessage(),
                        "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void editSelectedRoom() {
        int selectedRow = roomTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a room to edit.");
            return;
        }

        // Get room ID from hidden column
        int modelRow = roomTable.convertRowIndexToModel(selectedRow);
        int roomId = (Integer) tableModel.getValueAt(modelRow, 0);

        try {
            Room room = roomDAO.getRoomById(roomId);
            if (room != null) {
                RoomDialog dialog = new RoomDialog((JFrame) SwingUtilities.getWindowAncestor(this),
                        "Edit Room", true, room);
                dialog.setVisible(true);

                if (dialog.isConfirmed()) {
                    Room updatedRoom = dialog.getRoom();
                    updatedRoom.setId(roomId);

                    if (roomDAO.updateRoom(updatedRoom)) {
                        loadRoomData(); // Refresh table
                        JOptionPane.showMessageDialog(this, "Room updated successfully!",
                                "Success", JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(this, "Failed to update room.",
                                "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error editing room: " + e.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteSelectedRoom() {
        int selectedRow = roomTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a room to delete.");
            return;
        }

        int result = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete this room?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (result == JOptionPane.YES_OPTION) {
            int modelRow = roomTable.convertRowIndexToModel(selectedRow);
            int roomId = (Integer) tableModel.getValueAt(modelRow, 0);

            try {
                if (roomDAO.deleteRoom(roomId)) {
                    loadRoomData(); // Refresh table
                    JOptionPane.showMessageDialog(this, "Room deleted successfully!",
                            "Success", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to delete room.",
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Error deleting room: " + e.getMessage(),
                        "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public void refreshData() {
        loadRoomData();
        statusFilter.setSelectedItem("All");
        typeFilter.setSelectedItem("All");
    }

    // Icon creation methods
    private Icon createRefreshIcon() {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                g.setColor(new Color(52, 152, 219));
                g.fillOval(x + 2, y + 2, 12, 12);
                g.setColor(Color.WHITE);
                g.drawString("↻", x + 5, y + 11);
            }
            @Override
            public int getIconWidth() { return 16; }
            @Override
            public int getIconHeight() { return 16; }
        };
    }

    private Icon createAddIcon() {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                g.setColor(new Color(46, 204, 113));
                g.fillOval(x + 2, y + 2, 12, 12);
                g.setColor(Color.WHITE);
                g.drawString("+", x + 6, y + 11);
            }
            @Override
            public int getIconWidth() { return 16; }
            @Override
            public int getIconHeight() { return 16; }
        };
    }

    private Icon createEditIcon() {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                g.setColor(new Color(241, 196, 15));
                g.fillOval(x + 2, y + 2, 12, 12);
                g.setColor(Color.WHITE);
                g.drawString("✎", x + 5, y + 11);
            }
            @Override
            public int getIconWidth() { return 16; }
            @Override
            public int getIconHeight() { return 16; }
        };
    }

    private Icon createDeleteIcon() {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                g.setColor(new Color(231, 76, 60));
                g.fillOval(x + 2, y + 2, 12, 12);
                g.setColor(Color.WHITE);
                g.drawString("×", x + 5, y + 11);
            }
            @Override
            public int getIconWidth() { return 16; }
            @Override
            public int getIconHeight() { return 16; }
        };
    }
}