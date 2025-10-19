package com.example.taskmanager.ui;

import java.awt.*;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

import com.example.taskmanager.service.ApiService;
import com.example.taskmanager.service.AuthService;

public class ClassDetailWindow extends JFrame {
    private ApiService apiService;
    private AuthService authService;
    private String className;
    private String teacherName;
    
    private JTable studentTable;
    private DefaultTableModel tableModel;
    private JLabel classInfoLabel;
    private JLabel studentCountLabel;
    
    public ClassDetailWindow(ApiService apiService, AuthService authService, 
                            String className, String teacherName) {
        this.apiService = apiService;
        this.authService = authService;
        this.className = className;
        this.teacherName = teacherName;
        
        setTitle("Chi Tiết Lớp: " + className);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1100, 650);
        setLocationRelativeTo(null);
        
        initUI();
        loadStudentData();
        
        setVisible(true);
    }
    
    private void initUI() {
        JPanel mainPanel = new JPanel(new BorderLayout(0, 0));
        mainPanel.setBackground(new Color(0xF8F9FA));
        
        // Header
        JPanel headerPanel = createHeaderPanel();
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        
        // Content
        JPanel contentPanel = new JPanel(new BorderLayout(20, 20));
        contentPanel.setBackground(new Color(0xF8F9FA));
        contentPanel.setBorder(new EmptyBorder(25, 30, 25, 30));
        
        // Info panel
        JPanel infoPanel = createInfoPanel();
        contentPanel.add(infoPanel, BorderLayout.NORTH);
        
        // Table panel
        JPanel tablePanel = createTablePanel();
        contentPanel.add(tablePanel, BorderLayout.CENTER);
        
        mainPanel.add(contentPanel, BorderLayout.CENTER);
        
        add(mainPanel);
    }
    
    private JPanel createHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout());
        header.setPreferredSize(new Dimension(0, 70));
        header.setBackground(new Color(0x3B82F6));
        header.setBorder(new EmptyBorder(15, 30, 15, 30));
        
        // Back button and title
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        leftPanel.setOpaque(false);
        
        JButton backBtn = new JButton("← Quay Lại");
        backBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        backBtn.setForeground(new Color(0x3B82F6));
        backBtn.setBackground(Color.WHITE);
        backBtn.setBorderPainted(false);
        backBtn.setFocusPainted(false);
        backBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        backBtn.setPreferredSize(new Dimension(110, 35));
        backBtn.addActionListener(e -> dispose());
        leftPanel.add(backBtn);
        
        JLabel titleLabel = new JLabel("📚 Chi Tiết Lớp Học");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLabel.setForeground(Color.WHITE);
        leftPanel.add(titleLabel);
        
        header.add(leftPanel, BorderLayout.WEST);
        
        return header;
    }
    
    private JPanel createInfoPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);
        
        // Class info
        classInfoLabel = new JLabel("Lớp: " + className + " | Giáo viên: " + teacherName);
        classInfoLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        classInfoLabel.setForeground(new Color(0x1F2937));
        classInfoLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        studentCountLabel = new JLabel("Tổng số học sinh: 0");
        studentCountLabel.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        studentCountLabel.setForeground(new Color(0x6B7280));
        studentCountLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        panel.add(classInfoLabel);
        panel.add(Box.createVerticalStrut(8));
        panel.add(studentCountLabel);
        
        return panel;
    }
    
    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 15));
        panel.setOpaque(false);
        
        // Action bar
        JPanel actionBar = new JPanel(new BorderLayout());
        actionBar.setOpaque(false);
        
        JLabel tableTitle = new JLabel("👥 Danh Sách Học Sinh");
        tableTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        tableTitle.setForeground(new Color(0x1F2937));
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setOpaque(false);
        
        JButton refreshBtn = createActionButton("🔄 Làm Mới", new Color(0x6B7280));
        refreshBtn.addActionListener(e -> loadStudentData());
        
        JButton exportBtn = createActionButton("📊 Xuất Excel", new Color(0x059669));
        exportBtn.addActionListener(e -> exportToExcel());
        
        buttonPanel.add(refreshBtn);
        buttonPanel.add(exportBtn);
        
        actionBar.add(tableTitle, BorderLayout.WEST);
        actionBar.add(buttonPanel, BorderLayout.EAST);
        
        panel.add(actionBar, BorderLayout.NORTH);
        
        // Create table
        String[] columns = {"STT", "Họ và Tên", "Lớp", "Số Bài Đã Làm", "Điểm TB", "Chi Tiết Bài Thi"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 5; // Only button column
            }
            
            @Override
            public Class<?> getColumnClass(int column) {
                if (column == 0 || column == 3) return Integer.class;
                if (column == 4) return Double.class;
                return String.class;
            }
        };
        
        studentTable = new JTable(tableModel);
        studentTable.setRowHeight(55);
        studentTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        studentTable.setSelectionBackground(new Color(0xDCEEFE));
        studentTable.setSelectionForeground(new Color(0x1F2937));
        studentTable.setShowVerticalLines(false);
        studentTable.setIntercellSpacing(new Dimension(0, 0));
        
        // Style header
        JTableHeader header = studentTable.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 14));
        header.setBackground(new Color(0xF3F4F6));
        header.setForeground(new Color(0x374151));
        header.setPreferredSize(new Dimension(0, 45));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(0xE5E7EB)));
        
        // Center align columns
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        for (int i = 0; i < studentTable.getColumnCount() - 1; i++) {
            studentTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }
        
        // Custom renderer for score column (with color coding)
        studentTable.getColumnModel().getColumn(4).setCellRenderer(new ScoreRenderer());
        
        // Button column
        studentTable.getColumnModel().getColumn(5).setCellRenderer(new ButtonRenderer());
        studentTable.getColumnModel().getColumn(5).setCellEditor(new ButtonEditor(new JCheckBox()));
        
        // Set column widths
        studentTable.getColumnModel().getColumn(0).setPreferredWidth(60);   // STT
        studentTable.getColumnModel().getColumn(1).setPreferredWidth(200);  // Họ tên
        studentTable.getColumnModel().getColumn(2).setPreferredWidth(100);  // Lớp
        studentTable.getColumnModel().getColumn(3).setPreferredWidth(120);  // Số bài
        studentTable.getColumnModel().getColumn(4).setPreferredWidth(100);  // Điểm TB
        studentTable.getColumnModel().getColumn(5).setPreferredWidth(180);  // Chi tiết
        
        JScrollPane scrollPane = new JScrollPane(studentTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(0xE5E7EB), 1));
        scrollPane.getViewport().setBackground(Color.WHITE);
        
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JButton createActionButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setForeground(Color.WHITE);
        btn.setBackground(bg);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(140, 38));
        
        Color hoverColor = bg.darker();
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setBackground(hoverColor);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBackground(bg);
            }
        });
        
        return btn;
    }
    
    private void loadStudentData() {
        SwingWorker<List<Object[]>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Object[]> doInBackground() {
                // TODO: Call API to get students in this class
                // apiService.getStudentsByClass(className);
                
                // Mock data
                List<Object[]> data = new ArrayList<>();
                data.add(new Object[]{1, "Nguyễn Văn A", className, 5, 8.5, "detail"});
                data.add(new Object[]{2, "Trần Thị B", className, 4, 7.8, "detail"});
                data.add(new Object[]{3, "Lê Văn C", className, 6, 9.2, "detail"});
                data.add(new Object[]{4, "Phạm Thị D", className, 3, 6.5, "detail"});
                data.add(new Object[]{5, "Hoàng Văn E", className, 7, 8.9, "detail"});
                data.add(new Object[]{6, "Vũ Thị F", className, 5, 7.5, "detail"});
                data.add(new Object[]{7, "Đỗ Văn G", className, 4, 8.0, "detail"});
                data.add(new Object[]{8, "Bùi Thị H", className, 6, 9.0, "detail"});
                data.add(new Object[]{9, "Đinh Văn I", className, 5, 7.2, "detail"});
                data.add(new Object[]{10, "Mai Thị K", className, 8, 9.5, "detail"});
                
                return data;
            }
            
            @Override
            protected void done() {
                try {
                    List<Object[]> data = get();
                    tableModel.setRowCount(0);
                    for (Object[] row : data) {
                        tableModel.addRow(row);
                    }
                    studentCountLabel.setText("Tổng số học sinh: " + data.size());
                } catch (Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(ClassDetailWindow.this,
                        "Lỗi khi tải danh sách học sinh!",
                        "Lỗi",
                        JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }
    
    private void openExamDetail(int row) {
        String studentName = (String) tableModel.getValueAt(row, 1);
        int studentId = (int) tableModel.getValueAt(row, 0); // Assuming STT is studentId
        
        // Open student exam list window
        new StudentExamListWindow(apiService, authService, studentId, studentName, className);
    }
    
    private void exportToExcel() {
        JOptionPane.showMessageDialog(this,
            "Chức năng xuất Excel đang được phát triển!",
            "Thông báo",
            JOptionPane.INFORMATION_MESSAGE);
    }
    
    // Score Renderer with color coding
    class ScoreRenderer extends DefaultTableCellRenderer {
        public ScoreRenderer() {
            setHorizontalAlignment(JLabel.CENTER);
            setFont(new Font("Segoe UI", Font.BOLD, 14));
        }
        
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            
            if (value instanceof Double) {
                double score = (Double) value;
                setText(String.format("%.1f", score));
                
                if (!isSelected) {
                    if (score >= 9.0) {
                        setForeground(new Color(0x059669)); // Excellent - Green
                    } else if (score >= 8.0) {
                        setForeground(new Color(0x0284C7)); // Good - Blue
                    } else if (score >= 6.5) {
                        setForeground(new Color(0xD97706)); // Average - Orange
                    } else {
                        setForeground(new Color(0xDC2626)); // Poor - Red
                    }
                } else {
                    setForeground(table.getSelectionForeground());
                }
            }
            
            return this;
        }
    }
    
    // Button Renderer
    class ButtonRenderer extends JButton implements javax.swing.table.TableCellRenderer {
        public ButtonRenderer() {
            setOpaque(true);
            setFont(new Font("Segoe UI", Font.BOLD, 13));
            setForeground(Color.WHITE);
            setBackground(new Color(0x8B5CF6));
            setBorderPainted(false);
            setFocusPainted(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
        }
        
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            setText("📋 Xem Bài Thi");
            return this;
        }
    }
    
    // Button Editor
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
            button.setBackground(new Color(0x8B5CF6));
            button.setBorderPainted(false);
            button.setFocusPainted(false);
            button.setCursor(new Cursor(Cursor.HAND_CURSOR));
            
            button.addActionListener(e -> fireEditingStopped());
            
            button.addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseEntered(java.awt.event.MouseEvent evt) {
                    button.setBackground(new Color(0x7C3AED));
                }
                public void mouseExited(java.awt.event.MouseEvent evt) {
                    button.setBackground(new Color(0x8B5CF6));
                }
            });
        }
        
        public Component getTableCellEditorComponent(JTable table, Object value,
                boolean isSelected, int row, int column) {
            this.row = row;
            label = "📋 Xem Bài Thi";
            button.setText(label);
            clicked = true;
            return button;
        }
        
        public Object getCellEditorValue() {
            if (clicked) {
                openExamDetail(row);
            }
            clicked = false;
            return label;
        }
        
        public boolean stopCellEditing() {
            clicked = false;
            return super.stopCellEditing();
        }
    }
}