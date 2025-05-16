package com.example.taskmanager.ui;

import com.example.taskmanager.model.Task;
import com.example.taskmanager.service.ApiService;
import com.example.taskmanager.service.AuthService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

public class TaskPanel extends JPanel {
    private final ApiService apiService;
    private final AuthService authService;
    private final MainWindow mainWindow;
    
    private final JTable userTable;
    private final DefaultTableModel tableModel;
    private final JButton refreshButton;
    private final JButton addButton;
    private final JButton editButton;
    private final JButton deleteButton;
    private final JButton logoutButton;
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    
    public TaskPanel(ApiService apiService, AuthService authService, MainWindow mainWindow) {
        this.apiService = apiService;
        this.authService = authService;
        this.mainWindow = mainWindow;
        
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Create table model with column names for user data
        String[] columnNames = {"ID", "Email", "Full Name", "Role", "Status", "Created Date", "Updated Date", "Phone", "Address", "Birth Date", "Identity Number"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        // Create table and scroll pane
        userTable = new JTable(tableModel);
        userTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        userTable.getTableHeader().setReorderingAllowed(false);
        
        JScrollPane scrollPane = new JScrollPane(userTable);
        
        // Create button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        
        refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> refreshUsers());
        
        addButton = new JButton("Add User");
        addButton.addActionListener(e -> showAddUserDialog());
        
        editButton = new JButton("Edit User");
        editButton.addActionListener(e -> {
            int selectedRow = userTable.getSelectedRow();
            if (selectedRow >= 0) {
                showEditUserDialog(getUserFromSelectedRow());
            } else {
                JOptionPane.showMessageDialog(this, 
                    "Please select a user to edit", 
                    "Selection Required", 
                    JOptionPane.INFORMATION_MESSAGE);
            }
        });
        
        deleteButton = new JButton("Delete User");
        deleteButton.addActionListener(e -> {
            int selectedRow = userTable.getSelectedRow();
            if (selectedRow >= 0) {
                confirmAndDeleteUser();
            } else {
                JOptionPane.showMessageDialog(this, 
                    "Please select a user to delete", 
                    "Selection Required", 
                    JOptionPane.INFORMATION_MESSAGE);
            }
        });
        
        logoutButton = new JButton("Logout");
        logoutButton.addActionListener(e -> {
            authService.logout();
            mainWindow.showLoginPanel();
        });
        
        buttonPanel.add(refreshButton);
        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(logoutButton);
        
        // Add components to panel
        add(buttonPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        
        // Initial load of users
        refreshUsers();
    }
    
//    public void refreshUsers() {
//        refreshButton.setEnabled(false);
//        refreshButton.setText("Loading...");
//        
//        // Use SwingWorker to load users in background
//        SwingWorker<List<Task>, Void> worker = new SwingWorker<>() {
//            @Override
//            protected List<Task> doInBackground() {
//                return apiService.getUsers();
//            }
//            
//            @Override
//            protected void done() {
//                try {
//                    List<Task> users = get();
//                    updateUserTable(users);
//                    refreshButton.setEnabled(true);
//                    refreshButton.setText("Refresh");
//                } catch (Exception e) {
//                    e.printStackTrace();
//                    JOptionPane.showMessageDialog(TaskPanel.this, 
//                        "Error loading users: " + e.getMessage(), 
//                        "Error", 
//                        JOptionPane.ERROR_MESSAGE);
//                    refreshButton.setEnabled(true);
//                    refreshButton.setText("Refresh");
//                }
//            }
//        };
//        
//        worker.execute();
//    }
//    public void refreshUsers() {
//    refreshButton.setEnabled(false);
//    refreshButton.setText("Loading...");
//
//    SwingWorker<Task, Void> worker = new SwingWorker<>() {
//        @Override
//        protected Task doInBackground() {
//            return apiService.getUsers();
//        }
//
//        @Override
//        protected void done() {
//            try {
//                Task user = get();
//                System.out.println("User fetched: " + (user != null ? user.getEmail() : "null"));
//                updateUserTable(user != null ? Collections.singletonList(user) : Collections.emptyList());
//                refreshButton.setEnabled(true);
//                refreshButton.setText("Refresh");
//            } catch (Exception e) {
//                e.printStackTrace();
//                JOptionPane.showMessageDialog(TaskPanel.this, 
//                    "Error loading users: " + e.getMessage(), 
//                    "Error", 
//                    JOptionPane.ERROR_MESSAGE);
//                refreshButton.setEnabled(true);
//                refreshButton.setText("Refresh");
//            }
//        }
//    };
//
//    worker.execute();
//}
    public void refreshUsers() {
    refreshButton.setEnabled(false);
    refreshButton.setText("Loading...");

    SwingWorker<List<Task>, Void> worker = new SwingWorker<>() {
        @Override
        protected List<Task> doInBackground() {
            return apiService.getUsers();
        }

        @Override
        protected void done() {
            try {
                List<Task> users = get();
                System.out.println("User fetched: " + (users != null ? users.size() : 0) + " users");
                updateUserTable(users != null ? users : Collections.emptyList());
                refreshButton.setEnabled(true);
                refreshButton.setText("Refresh");
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(TaskPanel.this, 
                    "Error loading users: " + e.getMessage(), 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
                refreshButton.setEnabled(true);
                refreshButton.setText("Refresh");
            }
        }
    };

    worker.execute();
}
//    private void updateUserTable(List<Task> users) {
//        // Clear existing data
//        tableModel.setRowCount(0);
//        
//        // Add users to table
//        for (Task user : users) {
//            Vector<Object> row = new Vector<>();
//            row.add(user.getId());
//            row.add(user.getEmail());
//            row.add(user.getFullName());
//            row.add(user.getRole());
//            row.add(user.getStatus());
//            row.add(formatDateTime(user.getCreateDate()));
//            row.add(formatDateTime(user.getUpdateDate()));
//            row.add(user.getPhone());
//            row.add(user.getAddress());
//            row.add(formatDateTime(user.getBirthDate()));
//            row.add(user.getIdentityNumber());
//            tableModel.addRow(row);
//        }
//    }
    private void updateUserTable(List<Task> users) {
    tableModel.setRowCount(0);
    System.out.println("Updating table with " + users.size() + " users");

    for (Task user : users) {
        Vector<Object> row = new Vector<>();
        row.add(user.getId() != null ? user.getId() : "");
        row.add(user.getEmail() != null ? user.getEmail() : "");
        row.add(user.getFullName() != null ? user.getFullName() : "");
        row.add(user.getRole() != null ? user.getRole() : "");
        row.add(user.getStatus() != null ? user.getStatus() : "");
        row.add(formatDateTime(user.getCreateDate()));
        row.add(formatDateTime(user.getUpdateDate()));
        row.add(user.getPhone() != null ? user.getPhone() : "");
        row.add(user.getAddress() != null ? user.getAddress() : "");
        row.add(formatDateTime(user.getBirthDate()));
        row.add(user.getIdentityNumber() != null ? user.getIdentityNumber() : "");
        tableModel.addRow(row);
    }

    userTable.repaint();
    userTable.revalidate();
}
    private String formatDateTime(Object dateTime) {
    if (dateTime == null) return "";
    if (dateTime instanceof LocalDateTime) {
        return ((LocalDateTime) dateTime).format(DATE_FORMATTER);
    } else if (dateTime instanceof LocalDate) {
        return ((LocalDate) dateTime).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }
    return "";
}
    
//    private Task getUserFromSelectedRow() {
//        int selectedRow = userTable.getSelectedRow();
//        if (selectedRow < 0) {
//            return null;
//        }
//        
//        Long id = (Long) tableModel.getValueAt(selectedRow, 0);
//        String email = (String) tableModel.getValueAt(selectedRow, 1);
//        String fullName = (String) tableModel.getValueAt(selectedRow, 2);
//        String role = (String) tableModel.getValueAt(selectedRow, 3);
//        String status = (String) tableModel.getValueAt(selectedRow, 4);
//        LocalDateTime createDate = parseDateTime((String) tableModel.getValueAt(selectedRow, 5));
//        LocalDateTime updateDate = parseDateTime((String) tableModel.getValueAt(selectedRow, 6));
//        String phone = (String) tableModel.getValueAt(selectedRow, 7);
//        String address = (String) tableModel.getValueAt(selectedRow, 8);
//        LocalDateTime birthDate = parseDateTime((String) tableModel.getValueAt(selectedRow, 9));
//        String identityNumber = (String) tableModel.getValueAt(selectedRow, 10);
//        
//        Task user = new Task();
//        user.setId(id);
//        user.setEmail(email);
//        user.setFullName(fullName);
//        user.setRole(role);
//        user.setStatus(status);
//        user.setCreateDate(createDate);
//        user.setUpdateDate(updateDate);
//        user.setPhone(phone);
//        user.setAddress(address);
//        user.setBirthDate(birthDate);
//        user.setIdentityNumber(identityNumber);
//        
//        return user;
//    }
    private Task getUserFromSelectedRow() {
    int selectedRow = userTable.getSelectedRow();
    if (selectedRow < 0) {
        return null;
    }

    Long id = (Long) tableModel.getValueAt(selectedRow, 0);
    String email = (String) tableModel.getValueAt(selectedRow, 1);
    String fullName = (String) tableModel.getValueAt(selectedRow, 2);
    String role = (String) tableModel.getValueAt(selectedRow, 3);
    String status = (String) tableModel.getValueAt(selectedRow, 4);
    LocalDateTime createDate = parseDateTime((String) tableModel.getValueAt(selectedRow, 5)) instanceof LocalDateTime 
        ? (LocalDateTime) parseDateTime((String) tableModel.getValueAt(selectedRow, 5)) : null;
    LocalDateTime updateDate = parseDateTime((String) tableModel.getValueAt(selectedRow, 6)) instanceof LocalDateTime 
        ? (LocalDateTime) parseDateTime((String) tableModel.getValueAt(selectedRow, 6)) : null;
    String phone = (String) tableModel.getValueAt(selectedRow, 7);
    String address = (String) tableModel.getValueAt(selectedRow, 8);
    LocalDate birthDate = parseDateTime((String) tableModel.getValueAt(selectedRow, 9)) instanceof LocalDate 
        ? (LocalDate) parseDateTime((String) tableModel.getValueAt(selectedRow, 9)) : null;
    String identityNumber = (String) tableModel.getValueAt(selectedRow, 10);

    Task user = new Task();
    user.setId(id);
    user.setEmail(email);
    user.setFullName(fullName);
    user.setRole(role);
    user.setStatus(status);
    user.setCreateDate(createDate);
    user.setUpdateDate(updateDate);
    user.setPhone(phone);
    user.setAddress(address);
    user.setBirthDate(birthDate); // Sử dụng LocalDate
    user.setIdentityNumber(identityNumber);

    return user;
}
    private Object parseDateTime(String dateTimeStr) {
    if (dateTimeStr == null || dateTimeStr.isEmpty()) return null;
    try {
        return LocalDateTime.parse(dateTimeStr, DATE_FORMATTER); // Thử parse với định dạng đầy đủ
    } catch (Exception e) {
        return LocalDate.parse(dateTimeStr, DateTimeFormatter.ofPattern("yyyy-MM-dd")); // Thử parse với định dạng ngày
    }
}
    
//    private void showAddUserDialog() {
//        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this), "Add New User", Dialog.ModalityType.APPLICATION_MODAL);
//        dialog.setLayout(new BorderLayout(10, 10));
//        dialog.setSize(400, 400);
//        dialog.setLocationRelativeTo(this);
//        
//        JPanel formPanel = new JPanel(new GridLayout(7, 2, 5, 10));
//        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
//        
//        JLabel emailLabel = new JLabel("Email:");
//        JTextField emailField = new JTextField(20);
//        
//        JLabel fullNameLabel = new JLabel("Full Name:");
//        JTextField fullNameField = new JTextField(20);
//        
//        JLabel roleLabel = new JLabel("Role:");
//        JTextField roleField = new JTextField("customer", 20);
//        
//        JLabel statusLabel = new JLabel("Status:");
//        JTextField statusField = new JTextField("Active", 20);
//        
//        JLabel phoneLabel = new JLabel("Phone:");
//        JTextField phoneField = new JTextField(20);
//        
//        JLabel addressLabel = new JLabel("Address:");
//        JTextField addressField = new JTextField(20);
//        
//        JLabel birthDateLabel = new JLabel("Birth Date (yyyy-MM-dd HH:mm):");
//        JTextField birthDateField = new JTextField(20);
//        
//        JLabel identityNumberLabel = new JLabel("Identity Number:");
//        JTextField identityNumberField = new JTextField(20);
//        
//        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
//        JButton saveButton = new JButton("Save");
//        JButton cancelButton = new JButton("Cancel");
//        
//        saveButton.addActionListener(e -> {
//    String email = emailField.getText().trim();
//    String fullName = fullNameField.getText().trim();
//    String role = roleField.getText().trim();
//    String status = statusField.getText().trim();
//    String phone = phoneField.getText().trim();
//    String address = addressField.getText().trim();
//    Object birthDateObj = parseDateTime(birthDateField.getText().trim());
//    LocalDate birthDate = birthDateObj instanceof LocalDate ? (LocalDate) birthDateObj : null;
//    String identityNumber = identityNumberField.getText().trim();
//
//    if (email.isEmpty() || fullName.isEmpty()) {
//        JOptionPane.showMessageDialog(dialog, 
//            "Email and Full Name are required", 
//            "Input Error", 
//            JOptionPane.ERROR_MESSAGE);
//        return;
//    }
//
//    Task newUser = new Task();
//    newUser.setEmail(email);
//    newUser.setFullName(fullName);
//    newUser.setRole(role);
//    newUser.setStatus(status);
//    newUser.setPhone(phone);
//    newUser.setAddress(address);
//    newUser.setBirthDate(birthDate);
//    newUser.setIdentityNumber(identityNumber);
//    newUser.setCreateDate(LocalDateTime.now());
//    newUser.setUpdateDate(LocalDateTime.now());
//
//    createUser(newUser);
//    dialog.dispose();
//});
//        
//        cancelButton.addActionListener(e -> dialog.dispose());
//        
//        formPanel.add(emailLabel);
//        formPanel.add(emailField);
//        formPanel.add(fullNameLabel);
//        formPanel.add(fullNameField);
//        formPanel.add(roleLabel);
//        formPanel.add(roleField);
//        formPanel.add(statusLabel);
//        formPanel.add(statusField);
//        formPanel.add(phoneLabel);
//        formPanel.add(phoneField);
//        formPanel.add(addressLabel);
//        formPanel.add(addressField);
//        formPanel.add(birthDateLabel);
//        formPanel.add(birthDateField);
//        formPanel.add(identityNumberLabel);
//        formPanel.add(identityNumberField);
//        
//        buttonPanel.add(saveButton);
//        buttonPanel.add(cancelButton);
//        
//        dialog.add(formPanel, BorderLayout.CENTER);
//        dialog.add(buttonPanel, BorderLayout.SOUTH);
//        dialog.setVisible(true);
//    }
    private void showAddUserDialog() {
    JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this), "Add New User", Dialog.ModalityType.APPLICATION_MODAL);
    dialog.setLayout(new BorderLayout(10, 10));
    dialog.setSize(550, 350); // Tăng chiều rộng dialog
    dialog.getContentPane().setBackground(new Color(240, 248, 255));
    dialog.setLocationRelativeTo(this);

    JPanel formPanel = new JPanel(new GridBagLayout());
    formPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
    formPanel.setBackground(new Color(245, 245, 245));

    GridBagConstraints gbc = new GridBagConstraints();
    gbc.insets = new Insets(5, 5, 5, 5);
    gbc.fill = GridBagConstraints.HORIZONTAL;

    JLabel emailLabel = new JLabel("Email:");
    emailLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
    JTextField emailField = new JTextField(20);
    emailField.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createLineBorder(new Color(173, 216, 230), 1),
        BorderFactory.createEmptyBorder(2, 2, 2, 2)
    ));
    emailField.setEnabled(true);
    emailField.setBackground(Color.WHITE);
    emailField.setPreferredSize(new Dimension(250, 30)); // Đặt kích thước ưu tiên
    emailField.setFont(new Font("Segoe UI", Font.PLAIN, 12));

    JLabel fullNameLabel = new JLabel("Full Name:");
    fullNameLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
    JTextField fullNameField = new JTextField(20);
    fullNameField.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createLineBorder(new Color(173, 216, 230), 1),
        BorderFactory.createEmptyBorder(2, 2, 2, 2)
    ));
    fullNameField.setEnabled(true);
    fullNameField.setBackground(Color.WHITE);
    fullNameField.setPreferredSize(new Dimension(250, 30));
    fullNameField.setFont(new Font("Segoe UI", Font.PLAIN, 12));

    JLabel roleLabel = new JLabel("Role:");
    roleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
    JTextField roleField = new JTextField("customer", 20);
    roleField.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createLineBorder(new Color(173, 216, 230), 1),
        BorderFactory.createEmptyBorder(2, 2, 2, 2)
    ));
    roleField.setEnabled(true);
    roleField.setBackground(Color.WHITE);
    roleField.setPreferredSize(new Dimension(250, 30));
    roleField.setFont(new Font("Segoe UI", Font.PLAIN, 12));

    JLabel statusLabel = new JLabel("Status:");
    statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
    JTextField statusField = new JTextField("Active", 20);
    statusField.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createLineBorder(new Color(173, 216, 230), 1),
        BorderFactory.createEmptyBorder(2, 2, 2, 2)
    ));
    statusField.setEnabled(true);
    statusField.setBackground(Color.WHITE);
    statusField.setPreferredSize(new Dimension(250, 30));
    statusField.setFont(new Font("Segoe UI", Font.PLAIN, 12));

    JLabel phoneLabel = new JLabel("Phone:");
    phoneLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
    JTextField phoneField = new JTextField(20);
    phoneField.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createLineBorder(new Color(173, 216, 230), 1),
        BorderFactory.createEmptyBorder(2, 2, 2, 2)
    ));
    phoneField.setEnabled(true);
    phoneField.setBackground(Color.WHITE);
    phoneField.setPreferredSize(new Dimension(250, 30));
    phoneField.setFont(new Font("Segoe UI", Font.PLAIN, 12));

    JLabel addressLabel = new JLabel("Address:");
    addressLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
    JTextField addressField = new JTextField(20);
    addressField.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createLineBorder(new Color(173, 216, 230), 1),
        BorderFactory.createEmptyBorder(2, 2, 2, 2)
    ));
    addressField.setEnabled(true);
    addressField.setBackground(Color.WHITE);
    addressField.setPreferredSize(new Dimension(250, 30));
    addressField.setFont(new Font("Segoe UI", Font.PLAIN, 12));

    JLabel birthDateLabel = new JLabel("Birth Date (yyyy-MM-dd):");
    birthDateLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
    JTextField birthDateField = new JTextField(20);
    birthDateField.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createLineBorder(new Color(173, 216, 230), 1),
        BorderFactory.createEmptyBorder(2, 2, 2, 2)
    ));
    birthDateField.setEnabled(true);
    birthDateField.setBackground(Color.WHITE);
    birthDateField.setPreferredSize(new Dimension(250, 30));
    birthDateField.setFont(new Font("Segoe UI", Font.PLAIN, 12));

    JLabel identityNumberLabel = new JLabel("Identity Number:");
    identityNumberLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
    JTextField identityNumberField = new JTextField(20);
    identityNumberField.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createLineBorder(new Color(173, 216, 230), 1),
        BorderFactory.createEmptyBorder(2, 2, 2, 2)
    ));
    identityNumberField.setEnabled(true);
    identityNumberField.setBackground(Color.WHITE);
    identityNumberField.setPreferredSize(new Dimension(250, 30));
    identityNumberField.setFont(new Font("Segoe UI", Font.PLAIN, 12));

    gbc.weightx = 0.3; // Label chiếm 30% chiều rộng
    gbc.gridx = 0;
    gbc.gridy = 0;
    formPanel.add(emailLabel, gbc);
    gbc.weightx = 0.7; // Text field chiếm 70% chiều rộng
    gbc.gridx = 1;
    formPanel.add(emailField, gbc);

    gbc.weightx = 0.3;
    gbc.gridx = 0;
    gbc.gridy = 1;
    formPanel.add(fullNameLabel, gbc);
    gbc.weightx = 0.7;
    gbc.gridx = 1;
    formPanel.add(fullNameField, gbc);

    gbc.weightx = 0.3;
    gbc.gridx = 0;
    gbc.gridy = 2;
    formPanel.add(roleLabel, gbc);
    gbc.weightx = 0.7;
    gbc.gridx = 1;
    formPanel.add(roleField, gbc);

    gbc.weightx = 0.3;
    gbc.gridx = 0;
    gbc.gridy = 3;
    formPanel.add(statusLabel, gbc);
    gbc.weightx = 0.7;
    gbc.gridx = 1;
    formPanel.add(statusField, gbc);

    gbc.weightx = 0.3;
    gbc.gridx = 0;
    gbc.gridy = 4;
    formPanel.add(phoneLabel, gbc);
    gbc.weightx = 0.7;
    gbc.gridx = 1;
    formPanel.add(phoneField, gbc);

    gbc.weightx = 0.3;
    gbc.gridx = 0;
    gbc.gridy = 5;
    formPanel.add(addressLabel, gbc);
    gbc.weightx = 0.7;
    gbc.gridx = 1;
    formPanel.add(addressField, gbc);

    gbc.weightx = 0.3;
    gbc.gridx = 0;
    gbc.gridy = 6;
    formPanel.add(birthDateLabel, gbc);
    gbc.weightx = 0.7;
    gbc.gridx = 1;
    formPanel.add(birthDateField, gbc);

    gbc.weightx = 0.3;
    gbc.gridx = 0;
    gbc.gridy = 7;
    formPanel.add(identityNumberLabel, gbc);
    gbc.weightx = 0.7;
    gbc.gridx = 1;
    formPanel.add(identityNumberField, gbc);

    JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

    JButton saveButton = new JButton("Save");
    saveButton.setBackground(new Color(46, 139, 87));
    saveButton.setForeground(Color.BLACK);
    saveButton.setFocusPainted(false);
    saveButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
    saveButton.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createLineBorder(new Color(34, 139, 34), 1),
        BorderFactory.createEmptyBorder(5, 10, 5, 10)
    ));

    JButton cancelButton = new JButton("Cancel");
    cancelButton.setBackground(new Color(255, 99, 71));
    cancelButton.setForeground(Color.BLACK);
    cancelButton.setFocusPainted(false);
    cancelButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
    cancelButton.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createLineBorder(new Color(255, 69, 0), 1),
        BorderFactory.createEmptyBorder(5, 10, 5, 10)
    ));

    saveButton.addActionListener(e -> {
        String email = emailField.getText().trim();
        String fullName = fullNameField.getText().trim();
        String role = roleField.getText().trim();
        String status = statusField.getText().trim();
        String phone = phoneField.getText().trim();
        String address = addressField.getText().trim();
        Object birthDateObj = parseDateTime(birthDateField.getText().trim());
        LocalDate birthDate = birthDateObj instanceof LocalDate ? (LocalDate) birthDateObj : null;
        String identityNumber = identityNumberField.getText().trim();

        if (email.isEmpty() || fullName.isEmpty()) {
            JOptionPane.showMessageDialog(dialog, 
                "Email and Full Name are required", 
                "Input Error", 
                JOptionPane.ERROR_MESSAGE);
            return;
        }

        Task newUser = new Task();
        newUser.setEmail(email);
        newUser.setFullName(fullName);
        newUser.setRole(role);
        newUser.setStatus(status);
        newUser.setPhone(phone);
        newUser.setAddress(address);
        newUser.setBirthDate(birthDate);
        newUser.setIdentityNumber(identityNumber);
        newUser.setCreateDate(LocalDateTime.now());
        newUser.setUpdateDate(LocalDateTime.now());

        createUser(newUser);
        dialog.dispose();
    });

    cancelButton.addActionListener(e -> dialog.dispose());

    buttonPanel.add(saveButton);
    buttonPanel.add(cancelButton);

    dialog.add(formPanel, BorderLayout.CENTER);
    dialog.add(buttonPanel, BorderLayout.SOUTH);

    dialog.setFocusable(true);
    emailField.requestFocusInWindow();
    dialog.setVisible(true);
}
//    private void showEditUserDialog(Task user) {
//        if (user == null) return;
//        
//        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this), "Edit User", Dialog.ModalityType.APPLICATION_MODAL);
//        dialog.setLayout(new BorderLayout(10, 10));
//        dialog.setSize(400, 400);
//        dialog.setLocationRelativeTo(this);
//        
//        JPanel formPanel = new JPanel(new GridLayout(7, 2, 5, 10));
//        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
//        
//        JLabel emailLabel = new JLabel("Email:");
//        JTextField emailField = new JTextField(user.getEmail(), 20);
//        
//        JLabel fullNameLabel = new JLabel("Full Name:");
//        JTextField fullNameField = new JTextField(user.getFullName(), 20);
//        
//        JLabel roleLabel = new JLabel("Role:");
//        JTextField roleField = new JTextField(user.getRole(), 20);
//        
//        JLabel statusLabel = new JLabel("Status:");
//        JTextField statusField = new JTextField(user.getStatus(), 20);
//        
//        JLabel phoneLabel = new JLabel("Phone:");
//        JTextField phoneField = new JTextField(user.getPhone(), 20);
//        
//        JLabel addressLabel = new JLabel("Address:");
//        JTextField addressField = new JTextField(user.getAddress(), 20);
//        
//        JLabel birthDateLabel = new JLabel("Birth Date (yyyy-MM-dd HH:mm):");
//        JTextField birthDateField = new JTextField(user.getBirthDate() != null ? user.getBirthDate().format(DATE_FORMATTER) : "", 20);
//        
//        JLabel identityNumberLabel = new JLabel("Identity Number:");
//        JTextField identityNumberField = new JTextField(user.getIdentityNumber(), 20);
//        
//        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
//        JButton saveButton = new JButton("Save");
//        JButton cancelButton = new JButton("Cancel");
//        
//        saveButton.addActionListener(e -> {
//    String email = emailField.getText().trim();
//    String fullName = fullNameField.getText().trim();
//    String role = roleField.getText().trim();
//    String status = statusField.getText().trim();
//    String phone = phoneField.getText().trim();
//    String address = addressField.getText().trim();
//    Object birthDateObj = parseDateTime(birthDateField.getText().trim());
//    LocalDate birthDate = birthDateObj instanceof LocalDate ? (LocalDate) birthDateObj : null;
//    String identityNumber = identityNumberField.getText().trim();
//
//    if (email.isEmpty() || fullName.isEmpty()) {
//        JOptionPane.showMessageDialog(dialog, 
//            "Email and Full Name are required", 
//            "Input Error", 
//            JOptionPane.ERROR_MESSAGE);
//        return;
//    }
//
//    user.setEmail(email);
//    user.setFullName(fullName);
//    user.setRole(role);
//    user.setStatus(status);
//    user.setPhone(phone);
//    user.setAddress(address);
//    user.setBirthDate(birthDate);
//    user.setIdentityNumber(identityNumber);
//    user.setUpdateDate(LocalDateTime.now());
//
//    updateUser(user);
//    dialog.dispose();
//});
//        
//        cancelButton.addActionListener(e -> dialog.dispose());
//        
//        formPanel.add(emailLabel);
//        formPanel.add(emailField);
//        formPanel.add(fullNameLabel);
//        formPanel.add(fullNameField);
//        formPanel.add(roleLabel);
//        formPanel.add(roleField);
//        formPanel.add(statusLabel);
//        formPanel.add(statusField);
//        formPanel.add(phoneLabel);
//        formPanel.add(phoneField);
//        formPanel.add(addressLabel);
//        formPanel.add(addressField);
//        formPanel.add(birthDateLabel);
//        formPanel.add(birthDateField);
//        formPanel.add(identityNumberLabel);
//        formPanel.add(identityNumberField);
//        
//        buttonPanel.add(saveButton);
//        buttonPanel.add(cancelButton);
//        
//        dialog.add(formPanel, BorderLayout.CENTER);
//        dialog.add(buttonPanel, BorderLayout.SOUTH);
//        dialog.setVisible(true);
//    }
    private void showEditUserDialog(Task user) {
    if (user == null) return;

    JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this), "Edit User", Dialog.ModalityType.APPLICATION_MODAL);
    dialog.setLayout(new BorderLayout(10, 10));
    dialog.setSize(550, 350); // Tăng chiều rộng dialog
    dialog.getContentPane().setBackground(new Color(240, 248, 255)); // Màu nền nhẹ (AliceBlue)
    dialog.setLocationRelativeTo(this);

    JPanel formPanel = new JPanel(new GridBagLayout());
    formPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
    formPanel.setBackground(new Color(245, 245, 245)); // Màu nền xám nhạt

    GridBagConstraints gbc = new GridBagConstraints();
    gbc.insets = new Insets(5, 5, 5, 5);
    gbc.fill = GridBagConstraints.HORIZONTAL;

    JLabel emailLabel = new JLabel("Email:");
    emailLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
    JTextField emailField = new JTextField(user.getEmail() != null ? user.getEmail() : "", 20);
    emailField.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createLineBorder(new Color(173, 216, 230), 1),
        BorderFactory.createEmptyBorder(2, 2, 2, 2)
    ));
    emailField.setEnabled(true);
    emailField.setBackground(Color.WHITE);
    emailField.setPreferredSize(new Dimension(250, 30));
    emailField.setFont(new Font("Segoe UI", Font.PLAIN, 12));

    JLabel fullNameLabel = new JLabel("Full Name:");
    fullNameLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
    JTextField fullNameField = new JTextField(user.getFullName() != null ? user.getFullName() : "", 20);
    fullNameField.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createLineBorder(new Color(173, 216, 230), 1),
        BorderFactory.createEmptyBorder(2, 2, 2, 2)
    ));
    fullNameField.setEnabled(true);
    fullNameField.setBackground(Color.WHITE);
    fullNameField.setPreferredSize(new Dimension(250, 30));
    fullNameField.setFont(new Font("Segoe UI", Font.PLAIN, 12));

    JLabel roleLabel = new JLabel("Role:");
    roleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
    JTextField roleField = new JTextField(user.getRole() != null ? user.getRole() : "", 20);
    roleField.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createLineBorder(new Color(173, 216, 230), 1),
        BorderFactory.createEmptyBorder(2, 2, 2, 2)
    ));
    roleField.setEnabled(true);
    roleField.setBackground(Color.WHITE);
    roleField.setPreferredSize(new Dimension(250, 30));
    roleField.setFont(new Font("Segoe UI", Font.PLAIN, 12));

    JLabel statusLabel = new JLabel("Status:");
    statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
    JTextField statusField = new JTextField(user.getStatus() != null ? user.getStatus() : "", 20);
    statusField.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createLineBorder(new Color(173, 216, 230), 1),
        BorderFactory.createEmptyBorder(2, 2, 2, 2)
    ));
    statusField.setEnabled(true);
    statusField.setBackground(Color.WHITE);
    statusField.setPreferredSize(new Dimension(250, 30));
    statusField.setFont(new Font("Segoe UI", Font.PLAIN, 12));

    JLabel phoneLabel = new JLabel("Phone:");
    phoneLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
    JTextField phoneField = new JTextField(user.getPhone() != null ? user.getPhone() : "", 20);
    phoneField.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createLineBorder(new Color(173, 216, 230), 1),
        BorderFactory.createEmptyBorder(2, 2, 2, 2)
    ));
    phoneField.setEnabled(true);
    phoneField.setBackground(Color.WHITE);
    phoneField.setPreferredSize(new Dimension(250, 30));
    phoneField.setFont(new Font("Segoe UI", Font.PLAIN, 12));

    JLabel addressLabel = new JLabel("Address:");
    addressLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
    JTextField addressField = new JTextField(user.getAddress() != null ? user.getAddress() : "", 20);
    addressField.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createLineBorder(new Color(173, 216, 230), 1),
        BorderFactory.createEmptyBorder(2, 2, 2, 2)
    ));
    addressField.setEnabled(true);
    addressField.setBackground(Color.WHITE);
    addressField.setPreferredSize(new Dimension(250, 30));
    addressField.setFont(new Font("Segoe UI", Font.PLAIN, 12));

    JLabel birthDateLabel = new JLabel("Birth Date (yyyy-MM-dd):");
    birthDateLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
    JTextField birthDateField = new JTextField(user.getBirthDate() != null ? user.getBirthDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) : "", 20);
    birthDateField.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createLineBorder(new Color(173, 216, 230), 1),
        BorderFactory.createEmptyBorder(2, 2, 2, 2)
    ));
    birthDateField.setEnabled(true);
    birthDateField.setBackground(Color.WHITE);
    birthDateField.setPreferredSize(new Dimension(250, 30));
    birthDateField.setFont(new Font("Segoe UI", Font.PLAIN, 12));

    JLabel identityNumberLabel = new JLabel("Identity Number:");
    identityNumberLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
    JTextField identityNumberField = new JTextField(user.getIdentityNumber() != null ? user.getIdentityNumber() : "", 20);
    identityNumberField.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createLineBorder(new Color(173, 216, 230), 1),
        BorderFactory.createEmptyBorder(2, 2, 2, 2)
    ));
    identityNumberField.setEnabled(true);
    identityNumberField.setBackground(Color.WHITE);
    identityNumberField.setPreferredSize(new Dimension(250, 30));
    identityNumberField.setFont(new Font("Segoe UI", Font.PLAIN, 12));

    gbc.weightx = 0.3; // Label chiếm 30% chiều rộng
    gbc.gridx = 0;
    gbc.gridy = 0;
    formPanel.add(emailLabel, gbc);
    gbc.weightx = 0.7; // Text field chiếm 70% chiều rộng
    gbc.gridx = 1;
    formPanel.add(emailField, gbc);

    gbc.weightx = 0.3;
    gbc.gridx = 0;
    gbc.gridy = 1;
    formPanel.add(fullNameLabel, gbc);
    gbc.weightx = 0.7;
    gbc.gridx = 1;
    formPanel.add(fullNameField, gbc);

    gbc.weightx = 0.3;
    gbc.gridx = 0;
    gbc.gridy = 2;
    formPanel.add(roleLabel, gbc);
    gbc.weightx = 0.7;
    gbc.gridx = 1;
    formPanel.add(roleField, gbc);

    gbc.weightx = 0.3;
    gbc.gridx = 0;
    gbc.gridy = 3;
    formPanel.add(statusLabel, gbc);
    gbc.weightx = 0.7;
    gbc.gridx = 1;
    formPanel.add(statusField, gbc);

    gbc.weightx = 0.3;
    gbc.gridx = 0;
    gbc.gridy = 4;
    formPanel.add(phoneLabel, gbc);
    gbc.weightx = 0.7;
    gbc.gridx = 1;
    formPanel.add(phoneField, gbc);

    gbc.weightx = 0.3;
    gbc.gridx = 0;
    gbc.gridy = 5;
    formPanel.add(addressLabel, gbc);
    gbc.weightx = 0.7;
    gbc.gridx = 1;
    formPanel.add(addressField, gbc);

    gbc.weightx = 0.3;
    gbc.gridx = 0;
    gbc.gridy = 6;
    formPanel.add(birthDateLabel, gbc);
    gbc.weightx = 0.7;
    gbc.gridx = 1;
    formPanel.add(birthDateField, gbc);

    gbc.weightx = 0.3;
    gbc.gridx = 0;
    gbc.gridy = 7;
    formPanel.add(identityNumberLabel, gbc);
    gbc.weightx = 0.7;
    gbc.gridx = 1;
    formPanel.add(identityNumberField, gbc);

    JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

    JButton saveButton = new JButton("Save");
    saveButton.setBackground(new Color(46, 139, 87)); // Màu nền xanh lá
    saveButton.setForeground(Color.BLACK); // Màu chữ đen
    saveButton.setFocusPainted(false);
    saveButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
    saveButton.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createLineBorder(new Color(34, 139, 34), 1),
        BorderFactory.createEmptyBorder(5, 10, 5, 10)
    ));

    JButton cancelButton = new JButton("Cancel");
    cancelButton.setBackground(new Color(255, 99, 71)); // Màu nền đỏ cam
    cancelButton.setForeground(Color.BLACK); // Màu chữ đen
    cancelButton.setFocusPainted(false);
    cancelButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
    cancelButton.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createLineBorder(new Color(255, 69, 0), 1),
        BorderFactory.createEmptyBorder(5, 10, 5, 10)
    ));

    saveButton.addActionListener(e -> {
        String email = emailField.getText().trim();
        String fullName = fullNameField.getText().trim();
        String role = roleField.getText().trim();
        String status = statusField.getText().trim();
        String phone = phoneField.getText().trim();
        String address = addressField.getText().trim();
        Object birthDateObj = parseDateTime(birthDateField.getText().trim());
        LocalDate birthDate = birthDateObj instanceof LocalDate ? (LocalDate) birthDateObj : null;
        String identityNumber = identityNumberField.getText().trim();

        if (email.isEmpty() || fullName.isEmpty()) {
            JOptionPane.showMessageDialog(dialog, 
                "Email and Full Name are required", 
                "Input Error", 
                JOptionPane.ERROR_MESSAGE);
            return;
        }

        user.setEmail(email);
        user.setFullName(fullName);
        user.setRole(role);
        user.setStatus(status);
        user.setPhone(phone);
        user.setAddress(address);
        user.setBirthDate(birthDate);
        user.setIdentityNumber(identityNumber);
        user.setUpdateDate(LocalDateTime.now());

        updateUser(user);
        dialog.dispose();
    });

    cancelButton.addActionListener(e -> dialog.dispose());

    buttonPanel.add(saveButton);
    buttonPanel.add(cancelButton);

    dialog.add(formPanel, BorderLayout.CENTER);
    dialog.add(buttonPanel, BorderLayout.SOUTH);

    dialog.setFocusable(true);
    emailField.requestFocusInWindow();
    dialog.setVisible(true);
}
    private void confirmAndDeleteUser() {
        Task user = getUserFromSelectedRow();
        if (user == null) return;
        
        int option = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to delete user: " + user.getFullName() + "?",
            "Confirm Delete",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );
        
        if (option == JOptionPane.YES_OPTION) {
            deleteUser(user.getId());
        }
    }
    
    private void createUser(Task user) {
        SwingWorker<Task, Void> worker = new SwingWorker<>() {
            @Override
            protected Task doInBackground() {
                return apiService.createUser(user);
            }
            
            @Override
            protected void done() {
                try {
                    Task createdUser = get();
                    if (createdUser != null) {
                        refreshUsers();
                        JOptionPane.showMessageDialog(TaskPanel.this, 
                            "User created successfully", 
                            "Success", 
                            JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(TaskPanel.this, 
                            "Failed to create user", 
                            "Error", 
                            JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(TaskPanel.this, 
                        "Error creating user: " + e.getMessage(), 
                        "Error", 
                        JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        
        worker.execute();
    }
    
    private void updateUser(Task user) {
        SwingWorker<Task, Void> worker = new SwingWorker<>() {
            @Override
            protected Task doInBackground() {
                return apiService.updateUser(user);
            }
            
            @Override
            protected void done() {
                try {
                    Task updatedUser = get();
                    if (updatedUser != null) {
                        refreshUsers();
                        JOptionPane.showMessageDialog(TaskPanel.this, 
                            "User updated successfully", 
                            "Success", 
                            JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(TaskPanel.this, 
                            "Failed to update user", 
                            "Error", 
                            JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(TaskPanel.this, 
                        "Error updating user: " + e.getMessage(), 
                        "Error", 
                        JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        
        worker.execute();
    }
    
    private void deleteUser(Long userId) {
        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            @Override
            protected Boolean doInBackground() {
                return apiService.deleteUser(userId);
            }
            
            @Override
            protected void done() {
                try {
                    boolean success = get();
                    if (success) {
                        refreshUsers();
                        JOptionPane.showMessageDialog(TaskPanel.this, 
                            "User deleted successfully", 
                            "Success", 
                            JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(TaskPanel.this, 
                            "Failed to delete user", 
                            "Error", 
                            JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(TaskPanel.this, 
                        "Error deleting user: " + e.getMessage(), 
                        "Error", 
                        JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        
        worker.execute();
    }
}