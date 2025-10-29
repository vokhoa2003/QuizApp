package com.example.taskmanager.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dialog.ModalityType;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

import com.example.taskmanager.model.Task;
import com.example.taskmanager.service.ApiService;
import com.example.taskmanager.service.AuthService;

public class TaskPanel extends JFrame {
    private final ApiService apiService;
    private final AuthService authService;
    private final MainWindow mainWindow;
    private final AdminDashboard adminDashboard;

    private final JTable userTable;
    private final DefaultTableModel tableModel;
    private JButton refreshButton, addButton, editButton, deleteButton, logoutButton, searchButton;
    private JComboBox<String> roleFilterComboBox;
    private List<Task> allUsers = new ArrayList<>();

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private double scaleFactor = 1.0;

    // Modern Color Palette
    private static final Color PRIMARY_COLOR = new Color(59, 130, 246);
    private static final Color SUCCESS_COLOR = new Color(34, 197, 94);
    private static final Color DANGER_COLOR = new Color(239, 68, 68);
    private static final Color WARNING_COLOR = new Color(251, 146, 60);
    private static final Color BACKGROUND = new Color(248, 250, 252);
    private static final Color CARD_BG = Color.WHITE;
    private static final Color BORDER_COLOR = new Color(226, 232, 240);
    private static final Color TEXT_PRIMARY = new Color(15, 23, 42);
    private static final Color TEXT_SECONDARY = new Color(100, 116, 139);
    private static final Color TABLE_HEADER_BG = new Color(241, 245, 249);
    private static final Color TABLE_ROW_EVEN = Color.WHITE;
    private static final Color TABLE_ROW_ODD = new Color(249, 250, 251);

    public TaskPanel(ApiService apiService, AuthService authService, MainWindow mainWindow, AdminDashboard adminDashboard) {
        this.apiService = apiService;
        this.authService = authService;
        this.mainWindow = mainWindow;
        this.adminDashboard = adminDashboard;

        // CẤU HÌNH JFrame
        setTitle("Quản Lý Người Dùng - SecureStudy");
        setSize(1200, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        setUndecorated(true);

        // Main panel with rounded corners
        JPanel mainPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BACKGROUND);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                g2.setColor(new Color(0, 0, 0, 15));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 15, 15);
                g2.dispose();
            }
        };
        mainPanel.setLayout(new BorderLayout(15, 15));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        // Custom title bar
        mainPanel.add(createTitleBar(), BorderLayout.NORTH);

        // Content
        JPanel content = new JPanel(new BorderLayout(15, 15));
        content.setOpaque(false);

        String[] columnNames = {"ID", "Email", "Full Name", "Role", "Status", "Created Date",
                "Updated Date", "Phone", "Address", "Birth Date", "Identity Number"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };

        userTable = new JTable(tableModel);
        styleTable();

        JScrollPane scrollPane = new JScrollPane(userTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1));
        scrollPane.getViewport().setBackground(CARD_BG);

        JPanel topPanel = new JPanel(new BorderLayout(15, 15));
        topPanel.setOpaque(false);
        topPanel.setBorder(new EmptyBorder(0, 0, 15, 0));

        topPanel.add(createHeaderPanel(), BorderLayout.NORTH);
        topPanel.add(createFilterPanel(), BorderLayout.CENTER);
        topPanel.add(createButtonPanel(), BorderLayout.SOUTH);

        content.add(topPanel, BorderLayout.NORTH);
        content.add(scrollPane, BorderLayout.CENTER);

        mainPanel.add(content, BorderLayout.CENTER);
        add(mainPanel);

        addWindowDragListener();
        // KHÔNG GỌI refreshUsers() ở đây → Gọi từ AdminDashboard sau khi hiển thị
    }

    private JPanel createTitleBar() {
        JPanel titleBar = new JPanel(new BorderLayout());
        titleBar.setOpaque(false);
        titleBar.setBorder(new EmptyBorder(10, 15, 10, 15));

        JLabel titleLabel = new JLabel("Quản Lý Người Dùng");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(TEXT_PRIMARY);
        titleBar.add(titleLabel, BorderLayout.WEST);

        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        controlPanel.setOpaque(false);

        JButton minimizeBtn = createControlButton("–", e -> setState(Frame.ICONIFIED));
        JButton closeBtn = createControlButton("×", e -> {
            setVisible(false);
            if (adminDashboard != null) adminDashboard.setVisible(true);
        });

        controlPanel.add(minimizeBtn);
        controlPanel.add(closeBtn);
        titleBar.add(controlPanel, BorderLayout.EAST);

        return titleBar;
    }

    private JButton createControlButton(String text, ActionListener action) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Arial", Font.BOLD, 20));
        btn.setForeground(new Color(71, 85, 105));
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addActionListener(action);
        return btn;
    }

    private void addWindowDragListener() {
        final Point[] dragPoint = {null};
        addMouseListener(new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) {
                dragPoint[0] = e.getPoint();
            }
        });
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override public void mouseDragged(MouseEvent e) {
                if (dragPoint[0] != null) {
                    Point loc = getLocation();
                    setLocation(loc.x + e.getX() - dragPoint[0].x, loc.y + e.getY() - dragPoint[0].y);
                }
            }
        });
    }

    private JPanel createHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JLabel title = new JLabel("QUẢN LÝ NGƯỜI DÙNG");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setForeground(TEXT_PRIMARY);

        JLabel subtitle = new JLabel("Quản lý thông tin người dùng trong hệ thống");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitle.setForeground(TEXT_SECONDARY);

        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setOpaque(false);
        textPanel.add(title);
        textPanel.add(Box.createVerticalStrut(5));
        textPanel.add(subtitle);

        header.add(textPanel, BorderLayout.WEST);
        return header;
    }

    private JPanel createFilterPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        panel.setBackground(CARD_BG);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                new EmptyBorder(12, 15, 12, 15)
        ));

        JLabel label = new JLabel("Lọc theo Vai trò (Role):");
        label.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        label.setForeground(TEXT_PRIMARY);

        roleFilterComboBox = new JComboBox<>(new String[]{"Tất cả", "Quản trị viên", "Giáo viên", "Học sinh"});
        roleFilterComboBox.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        roleFilterComboBox.setPreferredSize(new Dimension(180, 36));
        roleFilterComboBox.setBackground(Color.WHITE);
        roleFilterComboBox.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1));
        roleFilterComboBox.addActionListener(e -> filterUsersByRole());

        panel.add(label);
        panel.add(Box.createHorizontalStrut(12));
        panel.add(roleFilterComboBox);
        return panel;
    }

    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        panel.setOpaque(false);

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
            setVisible(false);
            mainWindow.showLoginPanel();
        });

        panel.add(refreshButton);
        panel.add(addButton);
        panel.add(editButton);
        panel.add(deleteButton);
        panel.add(searchButton);
        panel.add(Box.createHorizontalStrut(10));
        panel.add(logoutButton);

        return panel;
    }

    private JButton createModernButton(String text, Color bgColor, String type) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btn.setForeground(Color.BLACK);
        btn.setBackground(bgColor);
        btn.setBorder(new EmptyBorder(10, 20, 10, 20));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btn.setBackground(bgColor.brighter()); }
            public void mouseExited(MouseEvent e) { btn.setBackground(bgColor); }
        });
        return btn;
    }

    private void styleTable() {
        userTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        userTable.setRowHeight(40);
        userTable.setShowGrid(false);
        userTable.setGridColor(BORDER_COLOR);
        userTable.setSelectionBackground(new Color(219, 234, 254));
        userTable.setSelectionForeground(TEXT_PRIMARY);

        JTableHeader header = userTable.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 13));
        header.setBackground(TABLE_HEADER_BG);
        header.setForeground(TEXT_PRIMARY);
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, BORDER_COLOR));
        header.setPreferredSize(new Dimension(0, 45));

        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer() {
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
            userTable.getColumnModel().getColumn(i).setCellRenderer(renderer);
        }
        userTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        userTable.getTableHeader().setReorderingAllowed(false);
    }

    public void refreshUsers() {
        new SwingWorker<List<Task>, Void>() {
            @Override protected List<Task> doInBackground() throws Exception {
                return apiService.getUsers();
            }
            @Override protected void done() {
                try {
                    List<Task> users = get();
                    System.out.println("Tasks received in TaskPanel: " + (users != null ? users.size() : 0));
                    if (users != null && !users.isEmpty()) {
                        allUsers = users;
                        updateTable(users);
                    } else {
                        allUsers = new ArrayList<>();
                        tableModel.setRowCount(0);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(TaskPanel.this,
                            "Lỗi tải danh sách người dùng: " + e.getMessage(),
                            "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            }
        }.execute();
    }

    private void updateTable(List<Task> users) {
        tableModel.setRowCount(0);
        for (Task u : users) {
            tableModel.addRow(new Object[]{
                    u.getId() != null ? u.getId() : "N/A",
                    u.getEmail() != null ? u.getEmail() : "N/A",
                    u.getFullName() != null ? u.getFullName() : "N/A",
                    u.getRole() != null ? u.getRole() : "N/A",
                    u.getStatus() != null ? u.getStatus() : "N/A",
                    formatDateTime(u.getCreateDate()),
                    formatDateTime(u.getUpdateDate()),
                    u.getPhone() != null ? u.getPhone() : "N/A",
                    u.getAddress() != null ? u.getAddress() : "N/A",
                    formatDateTime(u.getBirthDate()),
                    u.getIdentityNumber() != null ? u.getIdentityNumber() : "N/A"
            });
        }
        userTable.revalidate();
        userTable.repaint();
    }

    private void filterUsersByRole() {
        String selected = (String) roleFilterComboBox.getSelectedItem();
        List<Task> filtered = "Tất cả".equals(selected) ? allUsers :
                allUsers.stream()
                        .filter(u -> u.getRole() != null && u.getRole().equalsIgnoreCase(mapDisplayToRole(selected)))
                        .collect(Collectors.toList());
        updateTable(filtered);
    }

    private String mapDisplayToRole(String display) {
        return switch (display) {
            case "Quản trị viên" -> "admin";
            case "Giáo viên" -> "teacher";
            case "Học sinh" -> "student";
            default -> "admin";
        };
    }

    private String formatDateTime(Object date) {
        if (date == null) return "";
        if (date instanceof LocalDateTime) return ((LocalDateTime) date).format(DATE_FORMATTER);
        if (date instanceof LocalDate) return ((LocalDate) date).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        return "";
    }

    private Task getUserFromSelectedRow() {
        int row = userTable.getSelectedRow();
        if (row < 0 || "Không có dữ liệu".equals(tableModel.getValueAt(row, 0))) return null;
        Task u = new Task();
        u.setId((Long) tableModel.getValueAt(row, 0));
        u.setEmail((String) tableModel.getValueAt(row, 1));
        u.setFullName((String) tableModel.getValueAt(row, 2));
        u.setRole((String) tableModel.getValueAt(row, 3));
        u.setStatus((String) tableModel.getValueAt(row, 4));
        u.setPhone((String) tableModel.getValueAt(row, 7));
        u.setAddress((String) tableModel.getValueAt(row, 8));
        u.setIdentityNumber((String) tableModel.getValueAt(row, 10));
        return u;
    }

    private void handleEditAction() {
        Task user = getUserFromSelectedRow();
        if (user != null) showEditUserDialog(user);
        else showInfoDialog("Vui lòng chọn người dùng để chỉnh sửa", "Yêu cầu chọn");
    }

    private void handleDeleteAction() {
        Task user = getUserFromSelectedRow();
        if (user != null) confirmAndDeleteUser(user);
        else showInfoDialog("Vui lòng chọn người dùng để xóa", "Yêu cầu chọn");
    }

    private void confirmAndDeleteUser(Task user) {
        int opt = JOptionPane.showConfirmDialog(this,
                "Xóa người dùng: " + user.getFullName() + "?",
                "Xác nhận xóa", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (opt == JOptionPane.YES_OPTION) deleteUser(user.getId());
    }

    private void showAddUserDialog() { showUserDialog(null, "Thêm Người Dùng Mới"); }
    private void showEditUserDialog(Task user) { showUserDialog(user, "Chỉnh Sửa Người Dùng"); }

    private void showUserDialog(Task user, String title) {
        JDialog dialog = new JDialog(this, title, ModalityType.APPLICATION_MODAL);
        dialog.setSize(600, 520);
        dialog.setLocationRelativeTo(this);

        JPanel content = new JPanel(new BorderLayout(20, 20));
        content.setBackground(BACKGROUND);
        content.setBorder(new EmptyBorder(25, 25, 25, 25));

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(CARD_BG);
        form.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                new EmptyBorder(20, 20, 20, 20)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        boolean isEdit = user != null;
        JTextField email = addFormField(form, "Email", gbc, 0, isEdit ? user.getEmail() : "");
        JTextField fullName = addFormField(form, "Họ Tên", gbc, 1, isEdit ? user.getFullName() : "");
        JComboBox<String> roleBox = addComboField(form, "Vai trò", gbc, 2,
                new String[]{"Quản trị viên", "Giáo viên", "Học sinh"},
                isEdit ? mapRoleToDisplay(user.getRole()) : "Quản trị viên");
        JComboBox<String> statusBox = addComboField(form, "Trạng thái", gbc, 3,
                new String[]{"Active", "Blocked"}, isEdit ? user.getStatus() : "Active");
        JTextField phone = addFormField(form, "SĐT", gbc, 4, isEdit ? user.getPhone() : "");
        JTextField address = addFormField(form, "Địa chỉ", gbc, 5, isEdit ? user.getAddress() : "");
        JTextField birth = addFormField(form, "Ngày sinh (yyyy-MM-dd)", gbc, 6,
                isEdit && user.getBirthDate() != null ? user.getBirthDate().toString() : "");
        JTextField idNum = addFormField(form, "CMND/CCCD", gbc, 7, isEdit ? user.getIdentityNumber() : "");

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.setBackground(BACKGROUND);
 btnPanel.setBorder(new EmptyBorder(15, 0, 0, 0));

        JButton save = createModernButton("Lưu", SUCCESS_COLOR, "save");
        JButton cancel = createModernButton("Hủy", TEXT_SECONDARY, "cancel");

        save.addActionListener(e -> {
            if (email.getText().trim().isEmpty() || fullName.getText().trim().isEmpty()) {
                showErrorDialog("Email và Họ tên là bắt buộc");
                return;
            }
            Task u = isEdit ? user : new Task();
            u.setEmail(email.getText().trim());
            u.setFullName(fullName.getText().trim());
            u.setRole(mapDisplayToRole((String) roleBox.getSelectedItem()));
            u.setStatus((String) statusBox.getSelectedItem());
            u.setPhone(phone.getText().trim());
            u.setAddress(address.getText().trim());
            u.setIdentityNumber(idNum.getText().trim());
            Object bd = parseDate(birth.getText().trim());
            u.setBirthDate(bd instanceof LocalDate ? (LocalDate) bd : null);

            if (isEdit) {
                u.setUpdateDate(LocalDateTime.now());
                updateUser(u);
            } else {
                u.setCreateDate(LocalDateTime.now());
                u.setUpdateDate(LocalDateTime.now());
                createUser(u);
            }
            dialog.dispose();
        });

        cancel.addActionListener(e -> dialog.dispose());
        btnPanel.add(save);
        btnPanel.add(cancel);

        content.add(form, BorderLayout.CENTER);
        content.add(btnPanel, BorderLayout.SOUTH);
        dialog.add(content);
        dialog.setVisible(true);
    }

    private String mapRoleToDisplay(String dbRole) {
        return switch (dbRole != null ? dbRole.toLowerCase() : "") {
            case "admin" -> "Quản trị viên";
            case "teacher" -> "Giáo viên";
            case "student" -> "Học sinh";
            default -> "Quản trị viên";
        };
    }

    private Object parseDate(String s) {
        if (s == null || s.isBlank()) return null;
        try { return LocalDate.parse(s); }
        catch (Exception ex) { return null; }
    }

    private JTextField addFormField(JPanel p, String label, GridBagConstraints gbc, int row, String value) {
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.3;
        JLabel l = new JLabel(label + ":");
        l.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        l.setForeground(TEXT_PRIMARY);
        p.add(l, gbc);

        gbc.gridx = 1; gbc.weightx = 0.7;
        JTextField f = new JTextField(value);
        f.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        f.setPreferredSize(new Dimension(250, 38));
        f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                new EmptyBorder(8, 12, 8, 12)
        ));
        p.add(f, gbc);
        return f;
    }

    private JComboBox<String> addComboField(JPanel p, String label, GridBagConstraints gbc, int row, String[] items, String selected) {
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.3;
        JLabel l = new JLabel(label + ":");
        l.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        l.setForeground(TEXT_PRIMARY);
        p.add(l, gbc);

        gbc.gridx = 1; gbc.weightx = 0.7;
        JComboBox<String> c = new JComboBox<>(items);
        c.setSelectedItem(selected);
        c.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        c.setPreferredSize(new Dimension(250, 38));
        c.setBackground(Color.WHITE);
        c.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1));
        p.add(c, gbc);
        return c;
    }

    private void showSearchDialog() {
        JDialog d = new JDialog(this, "Tìm Kiếm Người Dùng", ModalityType.APPLICATION_MODAL);
        d.setSize(550, 300);
        d.setLocationRelativeTo(this);

        JPanel content = new JPanel(new BorderLayout(20, 20));
        content.setBackground(BACKGROUND);
        content.setBorder(new EmptyBorder(25, 25, 25, 25));

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(CARD_BG);
        form.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                new EmptyBorder(20, 20, 20, 20)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        JTextField name = addFormField(form, "Họ Tên", gbc, 0, "");
        JTextField idNum = addFormField(form, "CMND/CCCD", gbc, 1, "");

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btns.setBackground(BACKGROUND);
        btns.setBorder(new EmptyBorder(15, 0, 0, 0));

        JButton search = createModernButton("Tìm", PRIMARY_COLOR, "search");
        JButton cancel = createModernButton("Hủy", TEXT_SECONDARY, "cancel");

        search.addActionListener(e -> {
            String n = name.getText().trim().toLowerCase();
            String id = idNum.getText().trim();
            List<Task> filtered = allUsers.stream()
                    .filter(u -> (n.isEmpty() || (u.getFullName() != null && u.getFullName().toLowerCase().contains(n))) &&
                            (id.isEmpty() || (u.getIdentityNumber() != null && u.getIdentityNumber().contains(id))))
                    .collect(Collectors.toList());
            updateTable(filtered);
            d.dispose();
        });

        cancel.addActionListener(e -> d.dispose());
        btns.add(search);
        btns.add(cancel);

        content.add(form, BorderLayout.CENTER);
        content.add(btns, BorderLayout.SOUTH);
        d.add(content);
        d.setVisible(true);
    }

    private void createUser(Task user) {
        new SwingWorker<Boolean, Void>() {
            @Override protected Boolean doInBackground() { return apiService.createUser(user); }
            @Override protected void done() {
                try {
                    if (get()) { refreshUsers(); showSuccessDialog("Thêm người dùng thành công!"); }
                    else showErrorDialog("Thêm thất bại!");
                } catch (Exception e) { e.printStackTrace(); showErrorDialog("Lỗi: " + e.getMessage()); }
            }
        }.execute();
    }

    public void updateUser(Task user) {
        new SwingWorker<Boolean, Void>() {
            @Override protected Boolean doInBackground() { return apiService.updateUser(user); }
            @Override protected void done() {
                try {
                    if (get()) { refreshUsers(); showSuccessDialog("Cập nhật thành công!"); }
                    else showErrorDialog("Cập nhật thất bại!");
                } catch (Exception e) { e.printStackTrace(); showErrorDialog("Lỗi: " + e.getMessage()); }
            }
        }.execute();
    }

    private void deleteUser(Long id) {
        System.out.println("Deleting user with ID: " + id);
        new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() {
                boolean result = apiService.deleteUser(id);
                if (!result) {
                    System.out.println("API service returned false for deleteUser");
                } else {
                    System.out.println("User deleted successfully via API");
                }
                return result;
            }

            @Override
            protected void done() {
                try {
                    boolean success = get();
                    // Always refresh after the attempt
                    refreshUsers();
                    if (success) {
                        showSuccessDialog("Xóa thành công!");
                    } else {
                        showErrorDialog("Xóa thất bại!");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    showErrorDialog("Lỗi: " + e.getMessage());
                    // ensure UI refresh even on exception
                    refreshUsers();
                }
            }
        }.execute();
    }

    private void showInfoDialog(String msg, String title) {
        JOptionPane.showMessageDialog(this, msg, title, JOptionPane.INFORMATION_MESSAGE);
    }

    private void showSuccessDialog(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Thành công", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showErrorDialog(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Lỗi", JOptionPane.ERROR_MESSAGE);
    }

    // Getter để AdminDashboard gọi refreshUsers()
    public void loadData() {
        refreshUsers();
    }
}