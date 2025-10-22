package com.example.taskmanager.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map; // add import
import java.util.Objects;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

import com.example.taskmanager.model.Task;
import com.example.taskmanager.service.ApiService;
import com.example.taskmanager.service.AuthService;
import com.example.taskmanager.service.TeacherService;
import com.formdev.flatlaf.FlatLightLaf;

public class TeacherDashboard extends JFrame {
    private ApiService apiService;
    private AuthService authService;
    private Task currentTeacher;
    private TeacherService teacherService; // new
    private MainWindow mainWindow;  // Thêm reference đến MainWindow
    
    private JLabel teacherNameLabel;
    private JTable classTable;
    private DefaultTableModel tableModel;
    
    public TeacherDashboard(ApiService apiService, AuthService authService, Task teacher) {
        this(apiService, authService, teacher, null);
    }

    //constructor mới với MainWindow
    public TeacherDashboard(ApiService apiService, AuthService authService, Task teacher, MainWindow mainWindow) {
        this.apiService = apiService;
        this.authService = authService;
        this.currentTeacher = teacher;
        this.teacherService = new TeacherService(apiService); // init service
        this.mainWindow = mainWindow;
        
        setTitle("Trang Chủ Giáo Viên - SecureStudy");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 700);
        setLocationRelativeTo(null);
        
        initUI();
        loadTeacherClasses();
        
        setVisible(true);
    }
    
    private void initUI() {
        JPanel mainPanel = new JPanel(new BorderLayout(0, 0));
        mainPanel.setBackground(new Color(0xF8F9FA));
        
        // Header với gradient background
        JPanel headerPanel = createHeaderPanel();
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        
        // Content area
        JPanel contentPanel = new JPanel(new BorderLayout(20, 20));
        contentPanel.setBackground(new Color(0xF8F9FA));
        contentPanel.setBorder(new EmptyBorder(25, 30, 25, 30));
        
        // Welcome section
        JPanel welcomePanel = createWelcomePanel();
        contentPanel.add(welcomePanel, BorderLayout.NORTH);
        
        // Classes table section
        JPanel tablePanel = createTablePanel();
        contentPanel.add(tablePanel, BorderLayout.CENTER);
        
        mainPanel.add(contentPanel, BorderLayout.CENTER);
        
        add(mainPanel);
    }
    
    private JPanel createHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout());
        header.setPreferredSize(new Dimension(0, 80));
        header.setBackground(new Color(0x2563EB));
        header.setBorder(new EmptyBorder(15, 30, 15, 30));
        
        // Left side - Logo and title
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        leftPanel.setOpaque(false);
        
        JLabel logoLabel = new JLabel("📚");
        logoLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 36));
        leftPanel.add(logoLabel);
        
        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setOpaque(false);
        
        JLabel titleLabel = new JLabel("SecureStudy");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);
        
        JLabel subtitleLabel = new JLabel("Hệ thống quản lý đề thi trực tuyến");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitleLabel.setForeground(new Color(0xBFDBFE));
        
        titlePanel.add(titleLabel);
        titlePanel.add(subtitleLabel);
        leftPanel.add(titlePanel);
        
        // Right side - Teacher info
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        rightPanel.setOpaque(false);
        
        teacherNameLabel = new JLabel("👤 " + (currentTeacher != null ? currentTeacher.getFullName() : "Giáo viên"));
        teacherNameLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        teacherNameLabel.setForeground(Color.WHITE);
        rightPanel.add(teacherNameLabel);
        
        JButton logoutBtn = createStyledButton("Đăng xuất", new Color(0xEF4444), new Color(0xDC2626));
        logoutBtn.addActionListener(e -> logout());
        rightPanel.add(logoutBtn);
        
        header.add(leftPanel, BorderLayout.WEST);
        header.add(rightPanel, BorderLayout.EAST);
        
        return header;
    }
    
    private JPanel createWelcomePanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);
        
        JLabel welcomeLabel = new JLabel("Xin chào, " + (currentTeacher != null ? currentTeacher.getFullName() : "Giáo viên") + "! 👋");
        welcomeLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        welcomeLabel.setForeground(new Color(0x1F2937));
        welcomeLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel descLabel = new JLabel("Quản lý các lớp học và tạo đề thi của bạn");
        descLabel.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        descLabel.setForeground(new Color(0x6B7280));
        descLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        panel.add(welcomeLabel);
        panel.add(Box.createVerticalStrut(8));
        panel.add(descLabel);
        panel.add(Box.createVerticalStrut(20));
        
        // Action buttons
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        actionPanel.setOpaque(false);
        actionPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JButton createExamBtn = createPrimaryButton("➕ Tạo Đề Thi Mới");
        createExamBtn.addActionListener(e -> openQuizCreator());
        
        JButton refreshBtn = createSecondaryButton("🔄 Làm Mới");
        refreshBtn.addActionListener(e -> loadTeacherClasses());
        
        actionPanel.add(createExamBtn);
        actionPanel.add(refreshBtn);
        
        panel.add(actionPanel);
        
        return panel;
    }
    
    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 15));
        panel.setOpaque(false);
        
        JLabel tableTitle = new JLabel("📋 Danh Sách Lớp Học");
        tableTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        tableTitle.setForeground(new Color(0x1F2937));
        panel.add(tableTitle, BorderLayout.NORTH);
        
        // Create table
        String[] columns = {"Giáo Viên", "Lớp Học", "Sĩ Số", "Chi Tiết"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 3; // Only button column is "editable"
            }
        };
        
        classTable = new JTable(tableModel);
        classTable.setRowHeight(60);
        classTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        classTable.setSelectionBackground(new Color(0xDCEEFE));
        classTable.setSelectionForeground(new Color(0x1F2937));
        classTable.setShowVerticalLines(false);
        classTable.setIntercellSpacing(new Dimension(0, 0));
        
        // Style header
        JTableHeader header = classTable.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 14));
        header.setBackground(new Color(0xF3F4F6));
        header.setForeground(new Color(0x374151));
        header.setPreferredSize(new Dimension(0, 45));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(0xE5E7EB)));
        
        // Center align columns
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        for (int i = 0; i < classTable.getColumnCount() - 1; i++) {
            classTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }
        
        // Button column
        classTable.getColumnModel().getColumn(3).setCellRenderer(new ButtonRenderer());
        classTable.getColumnModel().getColumn(3).setCellEditor(new ButtonEditor(new JCheckBox()));
        
        // Set column widths
        classTable.getColumnModel().getColumn(0).setPreferredWidth(250);
        classTable.getColumnModel().getColumn(1).setPreferredWidth(200);
        classTable.getColumnModel().getColumn(2).setPreferredWidth(100);
        classTable.getColumnModel().getColumn(3).setPreferredWidth(150);
        
        JScrollPane scrollPane = new JScrollPane(classTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(0xE5E7EB), 1));
        scrollPane.getViewport().setBackground(Color.WHITE);
        
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JButton createPrimaryButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setForeground(Color.WHITE);
        btn.setBackground(new Color(0x2563EB));
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(180, 42));
        
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setBackground(new Color(0x1D4ED8));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBackground(new Color(0x2563EB));
            }
        });
        
        return btn;
    }
    
    private JButton createSecondaryButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setForeground(new Color(0x374151));
        btn.setBackground(Color.WHITE);
        btn.setBorder(BorderFactory.createLineBorder(new Color(0xD1D5DB), 1));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(140, 42));
        
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setBackground(new Color(0xF9FAFB));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBackground(Color.WHITE);
            }
        });
        
        return btn;
    }
    
    private JButton createStyledButton(String text, Color bg, Color hoverBg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setForeground(Color.WHITE);
        btn.setBackground(bg);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(110, 35));
        
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setBackground(hoverBg);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBackground(bg);
            }
        });
        
        return btn;
    }
    
    // Replace previous mock implementation with real API calls + fallback to resolve teacherId by email
    private void loadTeacherClasses() {
        tableModel.setRowCount(0);

        SwingWorker<List<Object[]>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Object[]> doInBackground() throws Exception {
                List<Object[]> rows = new ArrayList<>();
                try {
                    int teacherId = 0;
                    if (currentTeacher != null) {
                        Long idLong = null;
                        try {
                            idLong = currentTeacher.getId();
                        } catch (Throwable t) {
                            // ignore
                        }
                        if (idLong != null) {
                            teacherId = idLong.intValue();
                        }
                    }

                    // Fallback: nếu không có teacherId, thử lấy từ authService (email -> lookup)
                    if (teacherId <= 0 && authService != null) {
                        try {
                            String email = null;
                            try {
                                email = (String) authService.getClass().getMethod("getUserEmail").invoke(authService);
                            } catch (NoSuchMethodException nm) {
                                // possibly different API - try getEmail
                                try { email = (String) authService.getClass().getMethod("getEmail").invoke(authService); } catch (Exception ignore) {}
                            }
                            if (email != null && !email.isEmpty()) {
                                // Query account + teacher join to find teacher.Id
                                Map<String, Object> p = new HashMap<>();
                                p.put("action", "get");
                                p.put("method", "SELECT");
                                p.put("table", List.of("account", "teacher"));
                                p.put("columns", List.of("teacher.Id as TeacherId"));
                                Map<String, Object> where = new HashMap<>();
                                where.put("account.email", email);
                                p.put("where", where);
                                Map<String, Object> join = new HashMap<>();
                                join.put("type", "inner");
                                join.put("on", List.of("account.id = teacher.IdAccount"));
                                p.put("join", List.of(join));
                                p.put("limit", 1);

                                Object resp = apiService.postApiGetList("/autoGet", p);
                                List<Map<String, Object>> list = normalizeApiList(resp);
                                if (list != null && !list.isEmpty()) {
                                    Object tid = firstNonNull(list.get(0), "TeacherId", "teacher.Id", "Id");
                                    if (tid instanceof Number) teacherId = ((Number) tid).intValue();
                                    else if (tid != null) {
                                        try { teacherId = Integer.parseInt(tid.toString()); } catch (Exception ignored) {}
                                    }
                                    System.out.println("DEBUG: resolved teacherId by email=" + email + " -> " + teacherId);
                                } else {
                                    System.out.println("DEBUG: no teacher record found for email=" + email);
                                }
                            }
                        } catch (Exception e) {
                            System.err.println("WARN: fallback resolve teacherId error: " + e.getMessage());
                        }
                    }

                    if (teacherId <= 0) {
                        System.out.println("INFO: No teacherId available - cannot load classes");
                        return rows;
                    }

                    List<Map<String,Object>> classes = teacherService.getClassesForTeacher(teacherId);
                    for (Map<String,Object> c : classes) {
                        Object className = c.getOrDefault("ClassName", c.get("Name"));
                        Object studentCount = c.getOrDefault("StudentCount", 0);
                        rows.add(new Object[]{ currentTeacher != null ? currentTeacher.getFullName() : "Giáo viên",
                                               String.valueOf(className),
                                               String.valueOf(studentCount),
                                               "detail" });
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                return rows;
            }

            @Override
            protected void done() {
                try {
                    List<Object[]> data = get();
                    for (Object[] row : data) {
                        tableModel.addRow(row);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(TeacherDashboard.this,
                        "Lỗi khi tải danh sách lớp học!",
                        "Lỗi",
                        JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }
    
    private void openQuizCreator() {
        new QuizCreatorAppSwing(apiService, authService, null);
    }
    
    private void openClassDetail(int row) {
        String className = (String) tableModel.getValueAt(row, 1);
        String teacherName = (String) tableModel.getValueAt(row, 0);
        
        // Open class detail window
        new ClassDetailWindow(apiService, authService, className, teacherName);
    }
    
    private void logout() {
        int confirm = JOptionPane.showConfirmDialog(this,
            "Bạn có chắc muốn đăng xuất?",
            "Xác nhận",
            JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            // Gọi logout API
            authService.logout();
            // Đóng TeacherDashboard
            dispose();
            // Open login window
            mainWindow.setVisible(true);
            mainWindow.showLoginPanel();
        }
    }
    
    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> normalizeApiList(Object resp) {
        if (resp == null) return Collections.emptyList();
        if (resp instanceof List) {
            return (List<Map<String, Object>>) resp;
        }
        if (resp instanceof Map) {
            Map<?,?> m = (Map<?,?>) resp;
            Object data = m.get("data");
            if (data instanceof List) return (List<Map<String, Object>>) data;
            List<Map<String, Object>> list = new ArrayList<>();
            for (Object key : m.keySet()) {
                if (key == null) continue;
                String ks = key.toString();
                if (ks.matches("\\d+")) {
                    Object val = m.get(key);
                    if (val instanceof Map) list.add((Map<String, Object>) val);
                }
            }
            if (!list.isEmpty()) return list;
        }
        System.out.println("WARN: cannot normalize API response, resp=" + Objects.toString(resp));
        return Collections.emptyList();
    }

    private Object firstNonNull(Map<String, Object> row, String... keys) {
        if (row == null) return null;
        for (String k : keys) {
            if (k == null) continue;
            Object v = row.get(k);
            if (v == null) v = row.get(k.toLowerCase());
            if (v != null) return v;
        }
        return null;
    }
    
    // Button Renderer for table
    class ButtonRenderer extends JButton implements javax.swing.table.TableCellRenderer {
        public ButtonRenderer() {
            setOpaque(true);
            setFont(new Font("Segoe UI", Font.BOLD, 13));
            setForeground(Color.WHITE);
            setBackground(new Color(0x10B981));
            setBorderPainted(false);
            setFocusPainted(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
        }
        
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            setText("Xem Chi Tiết");
            return this;
        }
    }
    
    // Button Editor for table
    class ButtonEditor extends DefaultCellEditor {
        private JButton button;
        private String label;
        private boolean clicked;
        private int row;
        
        public ButtonEditor(JCheckBox checkBox) {
            super(checkBox);
            button = new JButton();
            button.setOpaque(true);
            button.setFont(new Font("Segoe UI", Font.BOLD, 13));
            button.setForeground(Color.WHITE);
            button.setBackground(new Color(0x10B981));
            button.setBorderPainted(false);
            button.setFocusPainted(false);
            button.setCursor(new Cursor(Cursor.HAND_CURSOR));
            
            button.addActionListener(e -> {
                fireEditingStopped();
            });
            
            button.addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseEntered(java.awt.event.MouseEvent evt) {
                    button.setBackground(new Color(0x059669));
                }
                public void mouseExited(java.awt.event.MouseEvent evt) {
                    button.setBackground(new Color(0x10B981));
                }
            });
        }
        
        public Component getTableCellEditorComponent(JTable table, Object value,
                boolean isSelected, int row, int column) {
            this.row = row;
            label = "Xem Chi Tiết";
            button.setText(label);
            clicked = true;
            return button;
        }
        
        public Object getCellEditorValue() {
            if (clicked) {
                openClassDetail(row);
            }
            clicked = false;
            return label;
        }
        
        public boolean stopCellEditing() {
            clicked = false;
            return super.stopCellEditing();
        }
    }
    
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        SwingUtilities.invokeLater(() -> {
            // Mock teacher data
            Task teacher = new Task();
            teacher.setFullName("Nguyễn Văn A");
            
            new TeacherDashboard(null, null, teacher, null);
        });
    }
}