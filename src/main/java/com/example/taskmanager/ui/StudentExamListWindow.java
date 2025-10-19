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

public class StudentExamListWindow extends JFrame {
    private ApiService apiService;
    private AuthService authService;
    private String studentName;
    private int studentId;
    private String className;
    
    private JTable examTable;
    private DefaultTableModel tableModel;
    private JLabel studentInfoLabel;
    private JLabel totalExamsLabel;
    
    public StudentExamListWindow(ApiService apiService, AuthService authService,
                                 int studentId, String studentName, String className) {
        this.apiService = apiService;
        this.authService = authService;
        this.studentId = studentId;
        this.studentName = studentName;
        this.className = className;
        
        setTitle("Danh Sách Bài Kiểm Tra - " + studentName);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1000, 600);
        setLocationRelativeTo(null);
        
        initUI();
        loadExamList();
        
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
        
        // Student info
        JPanel infoPanel = createInfoPanel();
        contentPanel.add(infoPanel, BorderLayout.NORTH);
        
        // Exam table
        JPanel tablePanel = createTablePanel();
        contentPanel.add(tablePanel, BorderLayout.CENTER);
        
        mainPanel.add(contentPanel, BorderLayout.CENTER);
        
        add(mainPanel);
    }
    
    private JPanel createHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout());
        header.setPreferredSize(new Dimension(0, 70));
        header.setBackground(new Color(0x8B5CF6));
        header.setBorder(new EmptyBorder(15, 30, 15, 30));
        
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        leftPanel.setOpaque(false);
        
        JButton backBtn = new JButton("← Quay Lại");
        backBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        backBtn.setForeground(new Color(0x8B5CF6));
        backBtn.setBackground(Color.WHITE);
        backBtn.setBorderPainted(false);
        backBtn.setFocusPainted(false);
        backBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        backBtn.setPreferredSize(new Dimension(110, 35));
        backBtn.addActionListener(e -> dispose());
        leftPanel.add(backBtn);
        
        JLabel titleLabel = new JLabel("📝 Danh Sách Bài Kiểm Tra");
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
        
        studentInfoLabel = new JLabel("👤 Học sinh: " + studentName + " | Lớp: " + className);
        studentInfoLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        studentInfoLabel.setForeground(new Color(0x1F2937));
        studentInfoLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        totalExamsLabel = new JLabel("Tổng số bài kiểm tra: 0");
        totalExamsLabel.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        totalExamsLabel.setForeground(new Color(0x6B7280));
        totalExamsLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        panel.add(studentInfoLabel);
        panel.add(Box.createVerticalStrut(8));
        panel.add(totalExamsLabel);
        
        return panel;
    }
    
    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 15));
        panel.setOpaque(false);
        
        // Title bar
        JPanel titleBar = new JPanel(new BorderLayout());
        titleBar.setOpaque(false);
        
        JLabel tableTitle = new JLabel("📊 Các Bài Đã Làm");
        tableTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        tableTitle.setForeground(new Color(0x1F2937));
        
        JButton refreshBtn = createActionButton("🔄 Làm Mới");
        refreshBtn.addActionListener(e -> loadExamList());
        
        titleBar.add(tableTitle, BorderLayout.WEST);
        titleBar.add(refreshBtn, BorderLayout.EAST);
        
        panel.add(titleBar, BorderLayout.NORTH);
        
        // Create table
        String[] columns = {"STT", "Tên Bài Kiểm Tra", "Ngày Làm", "Điểm Số", "Trạng Thái", "Chi Tiết"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 5; // Only detail button
            }
            
            @Override
            public Class<?> getColumnClass(int column) {
                if (column == 0) return Integer.class;
                if (column == 3) return Double.class;
                return String.class;
            }
        };
        
        examTable = new JTable(tableModel);
        examTable.setRowHeight(55);
        examTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        examTable.setSelectionBackground(new Color(0xDCEEFE));
        examTable.setSelectionForeground(new Color(0x1F2937));
        examTable.setShowVerticalLines(false);
        examTable.setIntercellSpacing(new Dimension(0, 0));
        
        // Style header
        JTableHeader header = examTable.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 14));
        header.setBackground(new Color(0xF3F4F6));
        header.setForeground(new Color(0x374151));
        header.setPreferredSize(new Dimension(0, 45));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(0xE5E7EB)));
        
        // Center align
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        for (int i = 0; i < 3; i++) {
            examTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }
        
        // Score renderer with color
        examTable.getColumnModel().getColumn(3).setCellRenderer(new ScoreRenderer());
        
        // Status renderer
        examTable.getColumnModel().getColumn(4).setCellRenderer(new StatusRenderer());
        
        // Button column
        examTable.getColumnModel().getColumn(5).setCellRenderer(new ButtonRenderer());
        examTable.getColumnModel().getColumn(5).setCellEditor(new ButtonEditor(new JCheckBox()));
        
        // Column widths
        examTable.getColumnModel().getColumn(0).setPreferredWidth(50);
        examTable.getColumnModel().getColumn(1).setPreferredWidth(300);
        examTable.getColumnModel().getColumn(2).setPreferredWidth(150);
        examTable.getColumnModel().getColumn(3).setPreferredWidth(100);
        examTable.getColumnModel().getColumn(4).setPreferredWidth(120);
        examTable.getColumnModel().getColumn(5).setPreferredWidth(150);
        
        JScrollPane scrollPane = new JScrollPane(examTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(0xE5E7EB), 1));
        scrollPane.getViewport().setBackground(Color.WHITE);
        
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JButton createActionButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setForeground(Color.WHITE);
        btn.setBackground(new Color(0x6B7280));
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(130, 38));
        
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setBackground(new Color(0x4B5563));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBackground(new Color(0x6B7280));
            }
        });
        
        return btn;
    }
    
    private void loadExamList() {
        SwingWorker<List<Map<String, Object>>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Map<String, Object>> doInBackground() {
                // Call API to get student's exams
                Map<String, Object> params = new HashMap<>();
                params.put("action", "get");
                params.put("method", "SELECT");
                params.put("table", List.of("exam_question", "exams"));
                params.put("columns", List.of(
                    "exam_question.id",
                    "exam_question.ExamId",
                    "exams.Name",
                    "exams.PublishDate",
                    "exam_question.Score"
                ));
                
                Map<String, Object> join = new HashMap<>();
                join.put("type", "inner");
                join.put("on", List.of("exam_question.ExamId = exams.id"));
                params.put("join", List.of(join));
                
                Map<String, Object> where = new HashMap<>();
                where.put("exam_question.StudentId", studentId);
                params.put("where", where);
                
                return apiService.postApiGetList("/autoGet", params);
            }
            
            @Override
            protected void done() {
                try {
                    List<Map<String, Object>> data = get();
                    tableModel.setRowCount(0);
                    
                    int index = 1;
                    for (Map<String, Object> row : data) {
                        Object examIdObj = row.get("exam_question.ExamId");
                        Object nameObj = row.get("exams.Name");
                        Object dateObj = row.get("exams.PublishDate");
                        Object scoreObj = row.get("exam_question.Score");
                        
                        int examId = examIdObj != null ? (int) examIdObj : 0;
                        String examName = nameObj != null ? nameObj.toString() : "N/A";
                        String examDate = dateObj != null ? dateObj.toString() : "N/A";
                        double score = scoreObj != null ? Double.parseDouble(scoreObj.toString()) : 0.0;
                        
                        tableModel.addRow(new Object[]{
                            index++,
                            examName,
                            examDate,
                            score,
                            "Đã chấm",
                            examId // Store examId for detail view
                        });
                    }
                    
                    totalExamsLabel.setText("Tổng số bài kiểm tra: " + data.size());
                    
                } catch (Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(StudentExamListWindow.this,
                        "Lỗi khi tải danh sách bài kiểm tra!\n" + e.getMessage(),
                        "Lỗi",
                        JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }
    
    private void openExamDetail(int row) {
        Object examIdObj = tableModel.getValueAt(row, 5);
        String examName = (String) tableModel.getValueAt(row, 1);
        double score = (Double) tableModel.getValueAt(row, 3);
        
        int examId = examIdObj instanceof Integer ? (int) examIdObj : 0;
        
        // Open exam detail window
        new ExamDetailWindow(apiService, authService, examId, studentId, 
                            studentName, examName, score);
    }
    
    // Score Renderer
    class ScoreRenderer extends DefaultTableCellRenderer {
        public ScoreRenderer() {
            setHorizontalAlignment(JLabel.CENTER);
            setFont(new Font("Segoe UI", Font.BOLD, 15));
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
                        setForeground(new Color(0x059669));
                    } else if (score >= 8.0) {
                        setForeground(new Color(0x0284C7));
                    } else if (score >= 6.5) {
                        setForeground(new Color(0xD97706));
                    } else {
                        setForeground(new Color(0xDC2626));
                    }
                }
            }
            
            return this;
        }
    }
    
    // Status Renderer
    class StatusRenderer extends DefaultTableCellRenderer {
        public StatusRenderer() {
            setHorizontalAlignment(JLabel.CENTER);
            setFont(new Font("Segoe UI", Font.BOLD, 13));
        }
        
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            
            if (value != null && !isSelected) {
                String status = value.toString();
                setText(status);
                setForeground(status.equals("Đã chấm") ? new Color(0x059669) : new Color(0xD97706));
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
            setBackground(new Color(0x0284C7));
            setBorderPainted(false);
            setFocusPainted(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
        }
        
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            setText("🔍 Xem Chi Tiết");
            return this;
        }
    }
    
    // Button Editor
    class ButtonEditor extends DefaultCellEditor {
        private JButton button;
        private boolean clicked;
        private int row;
        
        public ButtonEditor(JCheckBox checkBox) {
            super(checkBox);
            button = new JButton();
            button.setOpaque(true);
            button.setFont(new Font("Segoe UI", Font.BOLD, 13));
            button.setForeground(Color.WHITE);
            button.setBackground(new Color(0x0284C7));
            button.setBorderPainted(false);
            button.setFocusPainted(false);
            button.setCursor(new Cursor(Cursor.HAND_CURSOR));
            
            button.addActionListener(e -> fireEditingStopped());
            
            button.addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseEntered(java.awt.event.MouseEvent evt) {
                    button.setBackground(new Color(0x0369A1));
                }
                public void mouseExited(java.awt.event.MouseEvent evt) {
                    button.setBackground(new Color(0x0284C7));
                }
            });
        }
        
        public Component getTableCellEditorComponent(JTable table, Object value,
                boolean isSelected, int row, int column) {
            this.row = row;
            button.setText("🔍 Xem Chi Tiết");
            clicked = true;
            return button;
        }
        
        public Object getCellEditorValue() {
            if (clicked) {
                openExamDetail(row);
            }
            clicked = false;
            return "🔍 Xem Chi Tiết";
        }
        
        public boolean stopCellEditing() {
            clicked = false;
            return super.stopCellEditing();
        }
    }
}