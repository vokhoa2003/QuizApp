package com.example.taskmanager.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Vector;
import java.util.stream.Collectors;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

import com.example.taskmanager.model.Task;
import com.example.taskmanager.service.ApiService;
import com.example.taskmanager.service.AuthService;

public class TaskPanel extends JPanel {
    private final ApiService apiService;
    private final AuthService authService;
    private final MainWindow mainWindow;
    
    private final JTable userTable;
    private final DefaultTableModel tableModel;
    private JButton refreshButton;
    private JButton addButton;
    private JButton editButton;
    private JButton deleteButton;
    private JButton logoutButton;
    private JButton searchButton;
    private JComboBox<String> roleFilterComboBox;
    private List<Task> allUsers;
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final int BASE_FONT_SIZE = 12;
    private double scaleFactor = 1.0;
    
    // Modern Color Palette
    private static final Color PRIMARY_COLOR = new Color(59, 130, 246);      // Blue
    private static final Color SUCCESS_COLOR = new Color(34, 197, 94);       // Green
    private static final Color DANGER_COLOR = new Color(239, 68, 68);        // Red
    private static final Color WARNING_COLOR = new Color(251, 146, 60);      // Orange
    private static final Color BACKGROUND = new Color(248, 250, 252);        // Light gray
    private static final Color CARD_BG = Color.WHITE;
    private static final Color BORDER_COLOR = new Color(226, 232, 240);
    private static final Color TEXT_PRIMARY = new Color(15, 23, 42);
    private static final Color TEXT_SECONDARY = new Color(100, 116, 139);
    private static final Color TABLE_HEADER_BG = new Color(241, 245, 249);
    private static final Color TABLE_ROW_EVEN = Color.WHITE;
    private static final Color TABLE_ROW_ODD = new Color(249, 250, 251);

    public TaskPanel(ApiService apiService, AuthService authService, MainWindow mainWindow) {
        this.apiService = apiService;
        this.authService = authService;
        this.mainWindow = mainWindow;
        
        setLayout(new BorderLayout(15, 15));
        setBackground(BACKGROUND);
        setBorder(new EmptyBorder(20, 20, 20, 20));
        
        String[] columnNames = {"ID", "Email", "Full Name", "Role", "Status", "Created Date", 
                                "Updated Date", "Phone", "Address", "Birth Date", "Identity Number"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        userTable = new JTable(tableModel);
        styleTable();
        
        JScrollPane scrollPane = new JScrollPane(userTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1));
        scrollPane.getViewport().setBackground(CARD_BG);
        
        // Top Panel
        JPanel topPanel = new JPanel(new BorderLayout(15, 15));
        topPanel.setBackground(BACKGROUND);
        topPanel.setBorder(new EmptyBorder(0, 0, 15, 0));
        
        // Header Panel
        JPanel headerPanel = createHeaderPanel();
        
        // Filter Panel
        JPanel filterPanel = createFilterPanel();
        
        // Button Panel
        JPanel buttonPanel = createButtonPanel();
        
        topPanel.add(headerPanel, BorderLayout.NORTH);
        topPanel.add(filterPanel, BorderLayout.CENTER);
        topPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        
        refreshUsers();
    }
    
    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(BACKGROUND);
        
        JLabel titleLabel = new JLabel("QUẢN LÝ NGƯỜI DÙNG");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, (int) (24 * scaleFactor)));
        titleLabel.setForeground(TEXT_PRIMARY);
        
        JLabel subtitleLabel = new JLabel("Quan lý thông tin người dùng trong hệ thống");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, (int) (13 * scaleFactor)));
        subtitleLabel.setForeground(TEXT_SECONDARY);
        
        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setBackground(BACKGROUND);
        textPanel.add(titleLabel);
        textPanel.add(Box.createVerticalStrut(5));
        textPanel.add(subtitleLabel);
        
        headerPanel.add(textPanel, BorderLayout.WEST);
        
        return headerPanel;
    }
    
    private JPanel createFilterPanel() {
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        filterPanel.setBackground(CARD_BG);
        filterPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1),
            new EmptyBorder(12, 15, 12, 15)
        ));
        
        JLabel roleFilterLabel = new JLabel("Lọc theo Vai trò (Role):");
        roleFilterLabel.setFont(new Font("Segoe UI", Font.PLAIN, (int) (13 * scaleFactor)));
        roleFilterLabel.setForeground(TEXT_PRIMARY);
        
        roleFilterComboBox = new JComboBox<>(new String[]{"Tất cả", "Quản trị viên", "Giáo viên", "Học sinh"});
        roleFilterComboBox.setFont(new Font("Segoe UI", Font.PLAIN, (int) (13 * scaleFactor)));
        roleFilterComboBox.setPreferredSize(new Dimension(180, 36));
        roleFilterComboBox.setBackground(Color.WHITE);
        roleFilterComboBox.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1));
        roleFilterComboBox.addActionListener(e -> filterUsersByRole());
        
        filterPanel.add(roleFilterLabel);
        filterPanel.add(Box.createHorizontalStrut(12));
        filterPanel.add(roleFilterComboBox);
        
        return filterPanel;
    }
    
    private JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        buttonPanel.setBackground(BACKGROUND);
        
        refreshButton = createModernButton("Refresh", PRIMARY_COLOR, "refresh");
        addButton = createModernButton("Add User", SUCCESS_COLOR, "add");
        editButton = createModernButton("Edit User", WARNING_COLOR, "edit");
        deleteButton = createModernButton("Delete User", DANGER_COLOR, "delete");
        searchButton = createModernButton("Search", PRIMARY_COLOR, "search");
        logoutButton = createModernButton("Logout", TEXT_SECONDARY, "logout");
        
        refreshButton.addActionListener(e -> refreshUsers());
        addButton.addActionListener(e -> showAddUserDialog());
        editButton.addActionListener(e -> handleEditAction());
        deleteButton.addActionListener(e -> handleDeleteAction());
        searchButton.addActionListener(e -> showSearchDialog());
        logoutButton.addActionListener(e -> {
            authService.logout();
            mainWindow.showLoginPanel();
        });
        
        buttonPanel.add(refreshButton);
        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(searchButton);
        buttonPanel.add(Box.createHorizontalStrut(10));
        buttonPanel.add(logoutButton);
        
        return buttonPanel;
    }
    
    private JButton createModernButton(String text, Color bgColor, String type) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.PLAIN, (int) (13 * scaleFactor)));
        button.setForeground(Color.BLACK);
        button.setBackground(bgColor);
        button.setBorder(new EmptyBorder(10, 20, 10, 20));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Hover effect
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(bgColor.brighter());
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(bgColor);
            }
        });
        
        return button;
    }
    
    private void styleTable() {
        userTable.setFont(new Font("Segoe UI", Font.PLAIN, (int) (13 * scaleFactor)));
        userTable.setRowHeight((int) (40 * scaleFactor));
        userTable.setShowVerticalLines(false);
        userTable.setShowHorizontalLines(true);
        userTable.setGridColor(BORDER_COLOR);
        userTable.setSelectionBackground(new Color(219, 234, 254));
        userTable.setSelectionForeground(TEXT_PRIMARY);
        userTable.setIntercellSpacing(new Dimension(0, 0));
        
        // Header styling
        JTableHeader header = userTable.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, (int) (13 * scaleFactor)));
        header.setBackground(TABLE_HEADER_BG);
        header.setForeground(TEXT_PRIMARY);
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, BORDER_COLOR));
        header.setPreferredSize(new Dimension(header.getWidth(), (int) (45 * scaleFactor)));
        
        // Cell renderer for alternating row colors
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? TABLE_ROW_EVEN : TABLE_ROW_ODD);
                }
                setBorder(new EmptyBorder(5, 10, 5, 10));
                return c;
            }
        };
        
        for (int i = 0; i < userTable.getColumnCount(); i++) {
            userTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }
        
        userTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        userTable.getTableHeader().setReorderingAllowed(false);
    }
    
    private void handleEditAction() {
        int selectedRow = userTable.getSelectedRow();
        if (selectedRow >= 0 && tableModel.getRowCount() > 0 && 
            !tableModel.getValueAt(0, 0).equals("Không có dữ liệu")) {
            showEditUserDialog(getUserFromSelectedRow());
        } else {
            showInfoDialog("Please select a user to edit", "Selection Required");
        }
    }
    
    private void handleDeleteAction() {
        int selectedRow = userTable.getSelectedRow();
        if (selectedRow >= 0 && tableModel.getRowCount() > 0 && 
            !tableModel.getValueAt(0, 0).equals("Không có dữ liệu")) {
            confirmAndDeleteUser();
        } else {
            showInfoDialog("Please select a user to delete", "Selection Required");
        }
    }
    
    private void showInfoDialog(String message, String title) {
        JOptionPane.showMessageDialog(this, message, title, JOptionPane.INFORMATION_MESSAGE);
    }

    public void updateFonts(double scaleFactor) {
        this.scaleFactor = scaleFactor;
        styleTable();
        
        Font buttonFont = new Font("Segoe UI", Font.PLAIN, (int) (13 * scaleFactor));
        refreshButton.setFont(buttonFont);
        addButton.setFont(buttonFont);
        editButton.setFont(buttonFont);
        deleteButton.setFont(buttonFont);
        logoutButton.setFont(buttonFont);
        searchButton.setFont(buttonFont);
        roleFilterComboBox.setFont(new Font("Segoe UI", Font.PLAIN, (int) (13 * scaleFactor)));
    }

    public void refreshUsers() {
        SwingWorker<List<Task>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Task> doInBackground() throws Exception {
                return apiService.getUsers();
            }

            @Override
            protected void done() {
                try {
                    List<Task> users = get();
                    System.out.println("📊 Tasks received in TaskPanel: " + (users != null ? users.size() : 0));
                    
                    if (users != null && !users.isEmpty()) {
                        // ✅ In ra chi tiết từng user để debug
                        for (Task user : users) {
                            System.out.println("  - ID: " + user.getId() + ", Email: " + user.getEmail() + 
                                             ", Role: " + user.getRole() + ", Name: " + user.getFullName());
                        }
                        
                        updateTable(users);
                    } else {
                        System.out.println("⚠️ No users to display");
                        tableModel.setRowCount(0);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(TaskPanel.this,
                        "Lỗi tải danh sách người dùng: " + e.getMessage(),
                        "Lỗi",
                        JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private void updateTable(List<Task> users) {
        tableModel.setRowCount(0);
        System.out.println("🔄 Updating table with " + users.size() + " users");
        
        for (Task user : users) {
            Vector<Object> row = new Vector<>();
            // ✅ Sắp xếp đúng thứ tự cột: ID, Email, Full Name, Role, Status, Created Date, Updated Date, Phone, Address, Birth Date, Identity Number
            row.add(user.getId() != null ? user.getId() : "N/A");
            row.add(user.getEmail() != null ? user.getEmail() : "N/A");
            row.add(user.getFullName() != null ? user.getFullName() : "N/A");
            row.add(user.getRole() != null ? user.getRole() : "N/A");
            row.add(user.getStatus() != null ? user.getStatus() : "N/A");
            row.add(formatDateTime(user.getCreateDate()));
            row.add(formatDateTime(user.getUpdateDate()));
            row.add(user.getPhone() != null ? user.getPhone() : "N/A");
            row.add(user.getAddress() != null ? user.getAddress() : "N/A");
            row.add(formatDateTime(user.getBirthDate()));
            row.add(user.getIdentityNumber() != null ? user.getIdentityNumber() : "N/A");
            
            System.out.println("  ➕ Adding row: " + row);
            tableModel.addRow(row);
        }
        
        System.out.println("✅ Table updated, total rows: " + tableModel.getRowCount());
        userTable.revalidate();
        userTable.repaint();
    }

    private void filterUsersByRole() {
        String selectedRole = (String) roleFilterComboBox.getSelectedItem();
        List<Task> filteredUsers;
        
        if (selectedRole == null || selectedRole.equals("Tất cả")) {
            filteredUsers = allUsers;
        } else {
            // ✅ Map role từ combo filter sang DB value trước khi filter
            String dbRole = mapDisplayToRole(selectedRole);
            filteredUsers = allUsers.stream()
                .filter(user -> user.getRole() != null && user.getRole().equalsIgnoreCase(dbRole))
                .collect(Collectors.toList());
        }
        
        updateUserTable(filteredUsers);
    }

    private void updateUserTable(List<Task> users) {
        tableModel.setRowCount(0);

        if (users.isEmpty()) {
            Vector<Object> row = new Vector<>();
            row.add("Không có dữ liệu");
            for (int i = 1; i < tableModel.getColumnCount(); i++) {
                row.add("");
            }
            tableModel.addRow(row);
        } else {
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
        }

        userTable.repaint();
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
    
    private Task getUserFromSelectedRow() {
        int selectedRow = userTable.getSelectedRow();
        if (selectedRow < 0 || tableModel.getValueAt(selectedRow, 0).equals("Không có dữ liệu")) {
            return null;
        }

        Task user = new Task();
        user.setId((Long) tableModel.getValueAt(selectedRow, 0));
        user.setEmail((String) tableModel.getValueAt(selectedRow, 1));
        user.setFullName((String) tableModel.getValueAt(selectedRow, 2));
        user.setRole((String) tableModel.getValueAt(selectedRow, 3));
        user.setStatus((String) tableModel.getValueAt(selectedRow, 4));
        
        Object createDateObj = parseDateTime((String) tableModel.getValueAt(selectedRow, 5));
        user.setCreateDate(createDateObj instanceof LocalDateTime ? (LocalDateTime) createDateObj : null);
        
        Object updateDateObj = parseDateTime((String) tableModel.getValueAt(selectedRow, 6));
        user.setUpdateDate(updateDateObj instanceof LocalDateTime ? (LocalDateTime) updateDateObj : null);
        
        user.setPhone((String) tableModel.getValueAt(selectedRow, 7));
        user.setAddress((String) tableModel.getValueAt(selectedRow, 8));
        
        Object birthDateObj = parseDateTime((String) tableModel.getValueAt(selectedRow, 9));
        user.setBirthDate(birthDateObj instanceof LocalDate ? (LocalDate) birthDateObj : null);
        
        user.setIdentityNumber((String) tableModel.getValueAt(selectedRow, 10));

        return user;
    }

    private Object parseDateTime(String dateTimeStr) {
        if (dateTimeStr == null || dateTimeStr.isEmpty()) return null;
        try {
            return LocalDateTime.parse(dateTimeStr, DATE_FORMATTER);
        } catch (Exception e) {
            try {
                return LocalDate.parse(dateTimeStr, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            } catch (Exception ex) {
                return null;
            }
        }
    }

    private void showAddUserDialog() {
        showUserDialog(null, "Add New User");
    }

    private void showEditUserDialog(Task user) {
        if (user == null) return;
        showUserDialog(user, "Edit User");
    }
    
    private void showUserDialog(Task user, String title) {
        JDialog dialog = createStyledDialog(title, 600, 520);
        boolean isEdit = (user != null);
        
        JPanel contentPanel = new JPanel(new BorderLayout(20, 20));
        contentPanel.setBackground(BACKGROUND);
        contentPanel.setBorder(new EmptyBorder(25, 25, 25, 25));
        
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(CARD_BG);
        formPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1),
            new EmptyBorder(20, 20, 20, 20)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        JTextField emailField = addFormField(formPanel, "Email", gbc, 0, 
            isEdit ? user.getEmail() : "");
        JTextField fullNameField = addFormField(formPanel, "Full Name", gbc, 1, 
            isEdit ? user.getFullName() : "");
        
        // ✅ Map role từ database sang hiển thị tiếng Việt
        String displayRole = isEdit ? mapRoleToDisplay(user.getRole()) : "Quản trị viên";
        JComboBox<String> roleComboBox = addComboField(formPanel, "Role", gbc, 2,
            new String[]{"Quản trị viên", "Giáo viên", "Học sinh"},
            displayRole);
            
        JComboBox<String> statusComboBox = addComboField(formPanel, "Status", gbc, 3,
            new String[]{"Active", "Blocked"},
            isEdit ? user.getStatus() : "Active");
        JTextField phoneField = addFormField(formPanel, "Phone", gbc, 4,
            isEdit ? user.getPhone() : "");
        JTextField addressField = addFormField(formPanel, "Address", gbc, 5,
            isEdit ? user.getAddress() : "");
        JTextField birthDateField = addFormField(formPanel, "Birth Date (yyyy-MM-dd)", gbc, 6,
            isEdit && user.getBirthDate() != null ? 
                user.getBirthDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) : "");
        JTextField identityNumberField = addFormField(formPanel, "Identity Number", gbc, 7,
            isEdit ? user.getIdentityNumber() : "");

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setBackground(BACKGROUND);
        buttonPanel.setBorder(new EmptyBorder(15, 0, 0, 0));

        JButton saveButton = createModernButton("Save", SUCCESS_COLOR, "save");
        JButton cancelButton = createModernButton("Cancel", TEXT_SECONDARY, "cancel");

        saveButton.addActionListener(e -> {
            String email = emailField.getText().trim();
            String fullName = fullNameField.getText().trim();
            
            if (email.isEmpty() || fullName.isEmpty()) {
                showErrorDialog("Email and Full Name are required");
                return;
            }

            Task userToSave = isEdit ? user : new Task();
            userToSave.setEmail(email);
            userToSave.setFullName(fullName);
            
            // ✅ Map role từ hiển thị tiếng Việt sang database value
            String selectedDisplayRole = (String) roleComboBox.getSelectedItem();
            userToSave.setRole(mapDisplayToRole(selectedDisplayRole));
            
            userToSave.setStatus((String) statusComboBox.getSelectedItem());
            userToSave.setPhone(phoneField.getText().trim());
            userToSave.setAddress(addressField.getText().trim());
            
            Object birthDateObj = parseDateTime(birthDateField.getText().trim());
            userToSave.setBirthDate(birthDateObj instanceof LocalDate ? (LocalDate) birthDateObj : null);
            
            userToSave.setIdentityNumber(identityNumberField.getText().trim());
            
            if (isEdit) {
                userToSave.setUpdateDate(LocalDateTime.now());
                updateUser(userToSave);
            } else {
                userToSave.setCreateDate(LocalDateTime.now());
                userToSave.setUpdateDate(LocalDateTime.now());
                createUser(userToSave);
            }
            
            dialog.dispose();
        });

        cancelButton.addActionListener(e -> dialog.dispose());

        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);

        contentPanel.add(formPanel, BorderLayout.CENTER);
        contentPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        dialog.add(contentPanel);
        dialog.setVisible(true);
    }
    
    // ✅ Helper method: Map từ DB role (admin/teacher/student) sang hiển thị tiếng Việt
    private String mapRoleToDisplay(String dbRole) {
        if (dbRole == null) return "Quản trị viên";
        switch (dbRole.toLowerCase()) {
            case "admin": return "Quản trị viên";
            case "teacher": return "Giáo viên";
            case "student": return "Học sinh";
            default: return "Quản trị viên";
        }
    }
    
    // ✅ Helper method: Map từ hiển thị tiếng Việt sang DB role
    private String mapDisplayToRole(String displayRole) {
        if (displayRole == null) return "admin";
        switch (displayRole) {
            case "Quản trị viên": return "admin";
            case "Giáo viên": return "teacher";
            case "Học sinh": return "student";
            default: return "admin";
        }
    }
    
    private JTextField addFormField(JPanel panel, String label, GridBagConstraints gbc, 
                                     int row, String value) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.3;
        
        JLabel jLabel = new JLabel(label + ":");
        jLabel.setFont(new Font("Segoe UI", Font.PLAIN, (int) (13 * scaleFactor)));
        jLabel.setForeground(TEXT_PRIMARY);
        panel.add(jLabel, gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 0.7;
        
        JTextField field = new JTextField(value);
        field.setFont(new Font("Segoe UI", Font.PLAIN, (int) (13 * scaleFactor)));
        field.setPreferredSize(new Dimension(250, 38));
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1),
            new EmptyBorder(8, 12, 8, 12)
        ));
        panel.add(field, gbc);
        
        return field;
    }
    
    private JComboBox<String> addComboField(JPanel panel, String label, GridBagConstraints gbc,
                                             int row, String[] items, String selected) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.3;
        
        JLabel jLabel = new JLabel(label + ":");
        jLabel.setFont(new Font("Segoe UI", Font.PLAIN, (int) (13 * scaleFactor)));
        jLabel.setForeground(TEXT_PRIMARY);
        panel.add(jLabel, gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 0.7;
        
        JComboBox<String> combo = new JComboBox<>(items);
        combo.setSelectedItem(selected);
        combo.setFont(new Font("Segoe UI", Font.PLAIN, (int) (13 * scaleFactor)));
        combo.setPreferredSize(new Dimension(250, 38));
        combo.setBackground(Color.WHITE);
        combo.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1));
        panel.add(combo, gbc);
        
        return combo;
    }

    private void showSearchDialog() {
        JDialog dialog = createStyledDialog("Search Users", 550, 300);
        
        JPanel contentPanel = new JPanel(new BorderLayout(20, 20));
        contentPanel.setBackground(BACKGROUND);
        contentPanel.setBorder(new EmptyBorder(25, 25, 25, 25));

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(CARD_BG);
        formPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1),
            new EmptyBorder(20, 20, 20, 20)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        JTextField fullNameField = addFormField(formPanel, "Full Name", gbc, 0, "");
        JTextField identityNumberField = addFormField(formPanel, "Identity Number", gbc, 1, "");

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setBackground(BACKGROUND);
        buttonPanel.setBorder(new EmptyBorder(15, 0, 0, 0));

        JButton searchButton = createModernButton("Search", PRIMARY_COLOR, "search");
        JButton cancelButton = createModernButton("Cancel", TEXT_SECONDARY, "cancel");

        searchButton.addActionListener(e -> {
            String fullName = fullNameField.getText().trim();
            String identityNumber = identityNumberField.getText().trim();

            List<Task> filteredUsers = allUsers.stream()
                .filter(u -> {
                    boolean matches = true;
                    if (!fullName.isEmpty() && (u.getFullName() == null || 
                        !u.getFullName().toLowerCase().contains(fullName.toLowerCase()))) {
                        matches = false;
                    }
                    if (!identityNumber.isEmpty() && (u.getIdentityNumber() == null || 
                        !u.getIdentityNumber().contains(identityNumber))) {
                        matches = false;
                    }
                    return matches;
                })
                .collect(Collectors.toList());

            updateUserTable(filteredUsers);
            dialog.dispose();
        });

        cancelButton.addActionListener(e -> dialog.dispose());

        buttonPanel.add(searchButton);
        buttonPanel.add(cancelButton);

        contentPanel.add(formPanel, BorderLayout.CENTER);
        contentPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        dialog.add(contentPanel);
        dialog.setVisible(true);
    }
    
    private JDialog createStyledDialog(String title, int width, int height) {
        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this), title, 
            Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setSize((int) (width * scaleFactor), (int) (height * scaleFactor));
        dialog.setLocationRelativeTo(this);
        dialog.getContentPane().setBackground(BACKGROUND);
        return dialog;
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
        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            @Override
            protected Boolean doInBackground() {
                return apiService.createUser(user);
            }
            
            @Override
            protected void done() {
                try {
                    boolean success = get();
                    if (success) {
                        refreshUsers();
                        showSuccessDialog("User added successfully!");
                    } else {
                        showErrorDialog("Failed to add user!");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    showErrorDialog("Error creating user: " + e.getMessage());
                }
            }
        };
        
        worker.execute();
    }
    
    public void updateUser(Task user) {
        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            @Override
            protected Boolean doInBackground() {
                return apiService.updateUser(user);
            }
            
            @Override
            protected void done() {
                try {
                    boolean success = get();
                    if (success) {
                        refreshUsers();
                        showSuccessDialog("User updated successfully!");
                    } else {
                        showErrorDialog("Failed to update user!");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    showErrorDialog("Error updating user: " + e.getMessage());
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
                        showSuccessDialog("User deleted successfully!");
                    } else {
                        showErrorDialog("Failed to delete user!");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    showErrorDialog("Error deleting user: " + e.getMessage());
                }
            }
        };
        
        worker.execute();
    }
    
    private void showSuccessDialog(String message) {
        JOptionPane.showMessageDialog(this, message, "Success", JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void showErrorDialog(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
}