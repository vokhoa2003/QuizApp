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
import com.example.taskmanager.service.StudentInfoService;

public class StudentExamDetailWindow extends JFrame {
    private ApiService apiService;
    private AuthService authService;
    private String studentName;
    private String className;
    
    private JTable examTable;
    private DefaultTableModel tableModel;
    private JLabel studentInfoLabel;
    private JLabel summaryLabel;
    
    public StudentExamDetailWindow(ApiService apiService, AuthService authService,
                                  String studentName, String className) {
        this.apiService = apiService;
        this.authService = authService;
        this.studentName = studentName;
        this.className = className;
        
        setTitle("Bài Thi Của: " + studentName);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1000, 600);
        setLocationRelativeTo(null);
        
        initUI();
        loadExamData();
        
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
        
        // Table
        JPanel tablePanel = createTablePanel();
        contentPanel.add(tablePanel, BorderLayout.CENTER);
        
        // Summary stats
        JPanel summaryPanel = createSummaryPanel();
        contentPanel.add(summaryPanel, BorderLayout.SOUTH);
        
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
        
        JLabel titleLabel = new JLabel("📊 Chi Tiết Bài Thi Học Sinh");
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
        
        panel.add(studentInfoLabel);
        panel.add(Box.createVerticalStrut(5));
        
        return panel;
    }
    
    private JPanel createTablePanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 15));
        panel.setOpaque(false);
        
        // Title and actions
        JPanel titleBar = new JPanel(new BorderLayout());
        titleBar.setOpaque(false);
        
        JLabel tableTitle = new JLabel("📝 Danh Sách Bài Thi Đã Làm");
        tableTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        tableTitle.setForeground(new Color(0x1F2937));
        
        JButton refreshBtn = createActionButton("🔄 Làm Mới");
        refreshBtn.addActionListener(e -> loadExamData());
        
        titleBar.add(tableTitle, BorderLayout.WEST);
        titleBar.add(refreshBtn, BorderLayout.EAST);
        
        panel.add(titleBar, BorderLayout.NORTH);
        
        // Create table
        String[] columns = {"STT", "Mã Đề Thi", "Môn Học", "Ngày Làm", "Điểm Số", "Trạng Thái", "Chi Tiết"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 6; // Only detail button
            }
            
            @Override
            public Class<?> getColumnClass(int column) {
                if (column == 0) return Integer.class;
                if (column == 4) return Double.class;
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
        for (int i = 0; i < 4; i++) {
            examTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }
        
        // Score renderer
        examTable.getColumnModel().getColumn(4).setCellRenderer(new ScoreRenderer());
        
        // Status renderer
        examTable.getColumnModel().getColumn(5).setCellRenderer(new StatusRenderer());
        
        // Button column
        examTable.getColumnModel().getColumn(6).setCellRenderer(new ButtonRenderer());
        examTable.getColumnModel().getColumn(6).setCellEditor(new ButtonEditor(new JCheckBox()));
        
        // Column widths
        examTable.getColumnModel().getColumn(0).setPreferredWidth(50);
        examTable.getColumnModel().getColumn(1).setPreferredWidth(120);
        examTable.getColumnModel().getColumn(2).setPreferredWidth(150);
        examTable.getColumnModel().getColumn(3).setPreferredWidth(150);
        examTable.getColumnModel().getColumn(4).setPreferredWidth(100);
        examTable.getColumnModel().getColumn(5).setPreferredWidth(120);
        examTable.getColumnModel().getColumn(6).setPreferredWidth(150);
        
        JScrollPane scrollPane = new JScrollPane(examTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(0xE5E7EB), 1));
        scrollPane.getViewport().setBackground(Color.WHITE);
        
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createSummaryPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 4, 15, 0));
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(15, 0, 0, 0));
        
        summaryLabel = new JLabel();
        
        // Will be updated when data loads
        panel.add(createStatCard("Tổng bài thi", "0", new Color(0x3B82F6)));
        panel.add(createStatCard("Điểm trung bình", "0.0", new Color(0x10B981)));
        panel.add(createStatCard("Điểm cao nhất", "0.0", new Color(0x8B5CF6)));
        panel.add(createStatCard("Điểm thấp nhất", "0.0", new Color(0xEF4444)));
        
        return panel;
    }
    
    private JPanel createStatCard(String title, String value, Color color) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0xE5E7EB), 1),
            new EmptyBorder(15, 15, 15, 15)
        ));
        
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        titleLabel.setForeground(new Color(0x6B7280));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        valueLabel.setForeground(color);
        valueLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        card.add(titleLabel);
        card.add(Box.createVerticalStrut(8));
        card.add(valueLabel);
        
        return card;
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
    
    private void loadExamData() {
        SwingWorker<List<Map<String, Object>>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Map<String, Object>> doInBackground() {
                try {
                    StudentInfoService sis = new StudentInfoService(apiService);
                    // Nếu có email từ authService thì dùng, nếu không cần sửa constructor để truyền email hoặc accountId
                    String email = null;
                    if (authService != null) {
                        try {
                            // Thử lấy email từ authService nếu phương thức tồn tại
                            email = (String) authService.getClass().getMethod("getUserEmail").invoke(authService);
                        } catch (Exception ignored) {}
                    }
                    if (email != null) {
                        return sis.fetchProfileByEmail(email);
                    }
                    // fallback: trả về rỗng và log để developer sửa nơi gọi
                    System.err.println("StudentExamDetailWindow: missing email/accountId to load profile");
                    return Collections.emptyList();
                } catch (Exception ex) {
                    ex.printStackTrace();
                    return Collections.emptyList();
                }
            }

            @Override
            protected void done() {
                try {
                    List<Map<String, Object>> data = get();
                    System.out.println("✅ StudentExamDetailWindow profile: " + data);
                    
                    // Xóa dữ liệu cũ
                    tableModel.setRowCount(0);
                    
                    // TODO: Map data vào table - cần điều chỉnh theo cấu trúc API thực tế
                    // Ví dụ mock để test UI:
                    if (data != null && !data.isEmpty()) {
                        // Giả sử API trả về list exam của student
                        for (int i = 0; i < data.size(); i++) {
                            Map<String, Object> row = data.get(i);
                            tableModel.addRow(new Object[]{
                                i + 1,
                                row.getOrDefault("ExamCode", "N/A"),
                                row.getOrDefault("Subject", "N/A"),
                                row.getOrDefault("SubmittedDate", "N/A"),
                                row.getOrDefault("Score", 0.0),
                                row.getOrDefault("Status", "Chưa chấm"),
                                null // button column
                            });
                        }
                        
                        // Cập nhật summary nếu có dữ liệu
                        double total = data.size();
                        double avg = data.stream()
                            .mapToDouble(r -> ((Number)r.getOrDefault("Score", 0.0)).doubleValue())
                            .average().orElse(0.0);
                        double max = data.stream()
                            .mapToDouble(r -> ((Number)r.getOrDefault("Score", 0.0)).doubleValue())
                            .max().orElse(0.0);
                        double min = data.stream()
                            .mapToDouble(r -> ((Number)r.getOrDefault("Score", 0.0)).doubleValue())
                            .min().orElse(0.0);
                        
                        updateSummaryStats((int)total, avg, max, min);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(StudentExamDetailWindow.this,
                        "Lỗi khi tải dữ liệu bài thi!",
                        "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }
    
    private void updateSummaryStats(int total, double avg, double max, double min) {
        Component[] cards = ((JPanel)((JPanel)getContentPane().getComponent(0))
            .getComponent(1)).getComponents();
        
        if (cards.length >= 3) {
            JPanel panel = (JPanel)((JPanel)getContentPane().getComponent(0)).getComponent(1);
            panel.removeAll();
            
            panel.add(createStatCard("Tổng bài thi", String.valueOf(total), new Color(0x3B82F6)));
            panel.add(createStatCard("Điểm trung bình", String.format("%.1f", avg), new Color(0x10B981)));
            panel.add(createStatCard("Điểm cao nhất", String.format("%.1f", max), new Color(0x8B5CF6)));
            panel.add(createStatCard("Điểm thấp nhất", String.format("%.1f", min), new Color(0xEF4444)));
            
            panel.revalidate();
            panel.repaint();
        }
    }
    
    private void viewExamDetail(int row) {
        String examCode = (String) tableModel.getValueAt(row, 1);
        String subject = (String) tableModel.getValueAt(row, 2);
        double score = (Double) tableModel.getValueAt(row, 4);
        
        JOptionPane.showMessageDialog(this,
            "Chi tiết bài thi:\n\n" +
            "Mã đề: " + examCode + "\n" +
            "Môn: " + subject + "\n" +
            "Điểm: " + score + "\n" +
            "Học sinh: " + studentName,
            "Chi Tiết Bài Thi",
            JOptionPane.INFORMATION_MESSAGE);
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
            
            if (value != null) {
                String status = value.toString();
                setText(status);
                
                if (!isSelected) {
                    if (status.equals("Đã chấm")) {
                        setForeground(new Color(0x059669));
                    } else if (status.equals("Đang chấm")) {
                        setForeground(new Color(0xD97706));
                    } else {
                        setForeground(new Color(0x6B7280));
                    }
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
                viewExamDetail(row);
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