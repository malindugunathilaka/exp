package ui;
import service.UserService;
import model.User;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.util.List;

public class UsersPanel extends JPanel {
    private UserService userService;
    private DefaultTableModel tableModel;
    private JTable userTable;
    private JButton addButton, editButton, deleteButton, refreshButton;
    private JComboBox<String> roleFilter;

    public UsersPanel() {
        this.userService = new UserService();
        initializeUI();
        loadUserData();
    }

    private void initializeUI() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Create top panel with filters
        JPanel topPanel = createTopPanel();
        add(topPanel, BorderLayout.NORTH);

        // Create table
        createTable();
        JScrollPane scrollPane = new JScrollPane(userTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("System Users"));
        add(scrollPane, BorderLayout.CENTER);

        // Create bottom panel with action buttons
        JPanel bottomPanel = createBottomPanel();
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private JPanel createTopPanel() {
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

        // Role filter
        topPanel.add(new JLabel("Role:"));
        roleFilter = new JComboBox<>(new String[]{"All", "admin", "staff", "guest"});
        roleFilter.addActionListener(e -> applyFilters());
        topPanel.add(roleFilter);

        topPanel.add(Box.createHorizontalStrut(20));

        // Refresh button
        refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> refreshData());
        topPanel.add(refreshButton);

        return topPanel;
    }

    private void createTable() {
        String[] columns = {"ID", "Username", "Full Name", "Role"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make table read-only
            }
        };

        userTable = new JTable(tableModel);
        userTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        userTable.setRowHeight(25);
        userTable.getTableHeader().setReorderingAllowed(false);

        // Set column widths
        userTable.getColumnModel().getColumn(0).setPreferredWidth(50);  // ID
        userTable.getColumnModel().getColumn(1).setPreferredWidth(150); // Username
        userTable.getColumnModel().getColumn(2).setPreferredWidth(200); // Full Name
        userTable.getColumnModel().getColumn(3).setPreferredWidth(100); // Role

        // Hide ID column
        userTable.getColumnModel().getColumn(0).setMinWidth(0);
        userTable.getColumnModel().getColumn(0).setMaxWidth(0);
        userTable.getColumnModel().getColumn(0).setWidth(0);

        // Add row sorter
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(tableModel);
        userTable.setRowSorter(sorter);

        // Add selection listener for edit/delete buttons
        userTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                boolean hasSelection = userTable.getSelectedRow() != -1;
                editButton.setEnabled(hasSelection);
                deleteButton.setEnabled(hasSelection);
            }
        });

        // Add double-click listener for editing
        userTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    editSelectedUser();
                }
            }
        });
    }

    private JPanel createBottomPanel() {
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        addButton = new JButton("Add User");
        addButton.addActionListener(e -> showAddUserDialog());
        bottomPanel.add(addButton);

        editButton = new JButton("Edit User");
        editButton.setEnabled(false);
        editButton.addActionListener(e -> editSelectedUser());
        bottomPanel.add(editButton);

        deleteButton = new JButton("Delete User");
        deleteButton.setEnabled(false);
        deleteButton.addActionListener(e -> deleteSelectedUser());
        bottomPanel.add(deleteButton);

        return bottomPanel;
    }

    public void loadUserData() {
        try {
            List<User> users = userService.getAllUsers();
            updateTableData(users);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading users: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateTableData(List<User> users) {
        tableModel.setRowCount(0); // Clear existing data

        for (User user : users) {
            Object[] row = {
                    user.getId(),
                    user.getUsername(),
                    user.getFullname(),
                    user.getRole()
            };
            tableModel.addRow(row);
        }
    }

    private void applyFilters() {
        try {
            String selectedRole = (String) roleFilter.getSelectedItem();
            List<User> users;

            if ("All".equals(selectedRole)) {
                users = userService.getAllUsers();
            } else {
                users = userService.getUsersByRole(selectedRole);
            }

            updateTableData(users);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error applying filters: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showAddUserDialog() {
        UserDialog dialog = new UserDialog((JFrame) SwingUtilities.getWindowAncestor(this), "Add New User", true);
        dialog.setVisible(true);

        if (dialog.isConfirmed()) {
            User newUser = dialog.getUser();
            UserService.UserResult result = userService.createUser(
                    newUser.getUsername(),
                    newUser.getPassword(),
                    newUser.getRole(),
                    newUser.getFullname());

            if (result.isSuccess()) {
                loadUserData(); // Refresh table
                JOptionPane.showMessageDialog(this, "User added successfully!",
                        "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Error adding user: " + result.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void editSelectedUser() {
        int selectedRow = userTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a user to edit.");
            return;
        }

        // Get user ID from hidden column
        int modelRow = userTable.convertRowIndexToModel(selectedRow);
        int userId = (Integer) tableModel.getValueAt(modelRow, 0);

        User user = userService.getUserById(userId);
        if (user != null) {
            UserDialog dialog = new UserDialog((JFrame) SwingUtilities.getWindowAncestor(this),
                    "Edit User", true, user);
            dialog.setVisible(true);

            if (dialog.isConfirmed()) {
                User updatedUser = dialog.getUser();
                updatedUser.setId(userId);

                if (userService.updateUser(updatedUser)) {
                    loadUserData(); // Refresh table
                    JOptionPane.showMessageDialog(this, "User updated successfully!",
                            "Success", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to update user.",
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    private void deleteSelectedUser() {
        int selectedRow = userTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a user to delete.");
            return;
        }

        int modelRow = userTable.convertRowIndexToModel(selectedRow);
        String username = (String) tableModel.getValueAt(modelRow, 1);

        int result = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete user '" + username + "'?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (result == JOptionPane.YES_OPTION) {
            int userId = (Integer) tableModel.getValueAt(modelRow, 0);

            if (userService.deleteUser(userId)) {
                loadUserData(); // Refresh table
                JOptionPane.showMessageDialog(this, "User deleted successfully!",
                        "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Failed to delete user.",
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public void refreshData() {
        loadUserData();
        roleFilter.setSelectedItem("All");
    }
}