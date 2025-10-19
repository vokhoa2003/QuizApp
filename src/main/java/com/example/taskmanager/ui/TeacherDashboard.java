package com.example.taskmanager.ui;

import java.awt.*;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

import com.example.taskmanager.model.Task;
import com.example.taskmanager.service.ApiService;
import com.example.taskmanager.service.AuthService;
import com.formdev.flatlaf.FlatLightLaf;

public class TeacherDashboard extends JFrame {
    private ApiService apiService;
    private AuthService authService;
    private Task currentTeacher;
    
    private JLabel teacherNameLabel;
    private JTable classTable;
    private DefaultTableModel tableModel;
    
    public TeacherDashboard(ApiService apiService, AuthService authService, Task teacher) {
        this.apiService = apiService;
        this.authService = authService;
        this.currentTeacher = teacher;
        
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
    
    private void loadTeacherClasses() {
        // Mock data - replace with actual API call
        tableModel.setRowCount(0);
        
        SwingWorker<List<Object[]>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Object[]> doInBackground() {
                // TODO: Call API to get teacher's classes
                // apiService.getTeacherClasses(currentTeacher.getId());
                
                // Mock data for demonstration
                List<Object[]> data = new ArrayList<>();
                data.add(new Object[]{currentTeacher.getFullName(), "Lớp 10A1", "35", "detail"});
                data.add(new Object[]{currentTeacher.getFullName(), "Lớp 10A2", "32", "detail"});
                data.add(new Object[]{currentTeacher.getFullName(), "Lớp 11B1", "30", "detail"});
                
                return data;
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
            // TODO: Call logout API
            dispose();
            // Open login window
        }
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
            setText("👁️ Xem Chi Tiết");
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
            label = "👁️ Xem Chi Tiết";
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
            
            new TeacherDashboard(null, null, teacher);
        });
    }
}