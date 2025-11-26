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
import java.util.Map;
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
    private int classId;
    
    private JTable examTable;
    private DefaultTableModel tableModel;
    private JLabel studentInfoLabel;
    private JLabel totalExamsLabel;
    private Map<Integer, Integer> rowToExamIdMap = new HashMap<>();
    public StudentExamListWindow(ApiService apiService, AuthService authService,
                                 int studentId, String studentName, String className, int classId) {
        this.apiService = apiService;
        this.authService = authService;
        this.studentId = studentId;
        this.studentName = studentName;
        this.className = className;
        this.classId = classId;
        
        setTitle("Danh Sách Bài Kiểm Tra - " + studentName);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1000, 600);
        setLocationRelativeTo(null);
        
        initUI();
        loadExamList();  // ← BẬT LẠI
        
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
        
        JLabel titleLabel = new JLabel("Danh Sách Bài Kiểm Tra");
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
        
        studentInfoLabel = new JLabel("Học sinh: " + studentName + " | Lớp: " + className);
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
        
        JLabel tableTitle = new JLabel("Các Bài Đã Làm");
        tableTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        tableTitle.setForeground(new Color(0x1F2937));
        
        JButton refreshBtn = createActionButton("🔄 Làm Mới");
        refreshBtn.addActionListener(e -> loadExamList());  // ← BẬT LẠI
        
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
    
    // ========== LOAD DANH SÁCH BÀI THI CỦA HỌC SINH ==========
    // ========== LOAD DANH SÁCH BÀI THI GIỐNG PHP ==========
    // ========== LOAD DANH SÁCH BÀI THI GIỐNG PHP ==========
    private void loadExamList() {
    SwingWorker<List<Object[]>, Void> worker = new SwingWorker<>() {
        @Override
        protected List<Object[]> doInBackground() throws Exception {
            List<Object[]> rows = new ArrayList<>();
            
            try {
                // ====================================================================
                // BỎ BƯỚC 1 - DÙNG classId TRUYỀN VÀO TRỰC TIẾP
                // ====================================================================
                System.out.println("DEBUG: Using classId=" + classId + " for student=" + studentId);
                
                // ====================================================================
                // BƯỚC 2: Lấy TẤT CẢ EXAM của lớp CỤ THỂ (giống PHP)
                // ====================================================================
                Map<String, Object> examPayload = new HashMap<>();
                examPayload.put("method", "SELECT");
                examPayload.put("action", "get");
                examPayload.put("table", "exams");
                examPayload.put("columns", List.of("id", "ExamName", "NumberQuestion", "TimeLimit", "PublishDate", "ExpireDate"));
                Map<String, Object> examWhere = new HashMap<>();
                examWhere.put("ClassId", classId);  // ← LỌC THEO classId CỤ THỂ
                examPayload.put("where", examWhere);
                Map<String, String> orderBy = new HashMap<>();
                orderBy.put("PublishDate", "DESC");
                examPayload.put("orderBy", orderBy);
                
                Object examResp = apiService.postApiGetList("/autoGet", examPayload);
                List<Map<String, Object>> exams = normalizeApiList(examResp);
                
                System.out.println("DEBUG: Found " + exams.size() + " exams for class " + classId);
                
                // ====================================================================
                // BƯỚC 3: Với mỗi exam, kiểm tra xem học sinh CỤ THỂ có làm hay không
                // ====================================================================
                for (Map<String, Object> exam : exams) {
                    int examId = 0;
                    Object examIdObj = firstNonNull(exam, "id", "Id");
                    if (examIdObj instanceof Number) {
                        examId = ((Number) examIdObj).intValue();
                    } else if (examIdObj != null) {
                        try { examId = Integer.parseInt(examIdObj.toString()); } catch (Exception ignored) {}
                    }
                    
                    if (examId <= 0) continue;
                    
                    String examName = "Chưa rõ";
                    Object nameObj = firstNonNull(exam, "ExamName");
                    if (nameObj != null) {
                        String name = nameObj.toString().trim();
                        if (!name.isEmpty() && !name.equalsIgnoreCase("null")) {
                            examName = name;
                        }
                    }
                    
                    System.out.println("DEBUG: Processing exam - ID=" + examId + ", Name='" + examName + "'");
                    
                    // ----------------------------------------------------------------
                    // Tìm exam_attempts (Status='submitted') CỦA HỌC SINH CỤ THỂ
                    // ----------------------------------------------------------------
                    Map<String, Object> attemptPayload = new HashMap<>();
                    attemptPayload.put("method", "SELECT");
                    attemptPayload.put("action", "get");
                    attemptPayload.put("table", "exam_attempts");
                    attemptPayload.put("columns", List.of("id", "ExamId", "StudentId", "StartTime", "SubmitTime", "Status"));
                    Map<String, Object> attemptWhere = new HashMap<>();
                    attemptWhere.put("ExamId", examId);
                    attemptWhere.put("StudentId", studentId);  // ← LỌC THEO studentId
                    attemptWhere.put("Status", "submitted");
                    attemptPayload.put("where", attemptWhere);
                    Map<String, String> attemptOrder = new HashMap<>();
                    attemptOrder.put("SubmitTime", "DESC");
                    attemptPayload.put("orderBy", attemptOrder);
                    attemptPayload.put("limit", 1);
                    
                    Object attemptResp = apiService.postApiGetList("/autoGet", attemptPayload);
                    List<Map<String, Object>> attempts = normalizeApiList(attemptResp);
                    
                    if (attempts.isEmpty()) {
                        // KHÔNG CÓ ATTEMPT → "Chưa làm"
                        Object[] notStartedRow = new Object[5];
                        notStartedRow[0] = examName;
                        notStartedRow[1] = "Chưa làm";
                        notStartedRow[2] = null;
                        notStartedRow[3] = "Chưa làm";
                        notStartedRow[4] = examId;
                        rows.add(notStartedRow);
                        continue;
                    }
                    
                    // CÓ ATTEMPT → Tìm exam_results
                    Map<String, Object> attempt = attempts.get(0);
                    int attemptId = 0;
                    Object aidObj = firstNonNull(attempt, "id");
                    if (aidObj instanceof Number) {
                        attemptId = ((Number) aidObj).intValue();
                    } else if (aidObj != null) {
                        try { attemptId = Integer.parseInt(aidObj.toString()); } catch (Exception ignored) {}
                    }
                    
                    if (attemptId <= 0) {
                        Object[] noAttemptRow = new Object[5];
                        noAttemptRow[0] = examName;
                        noAttemptRow[1] = "Chưa làm";
                        noAttemptRow[2] = null;
                        noAttemptRow[3] = "Chưa làm";
                        noAttemptRow[4] = examId;
                        rows.add(noAttemptRow);
                        continue;
                    }
                    
                    Map<String, Object> resultPayload = new HashMap<>();
                    resultPayload.put("method", "SELECT");
                    resultPayload.put("action", "get");
                    resultPayload.put("table", "exam_results");
                    resultPayload.put("columns", List.of("id", "AttemptId", "Score", "SubmittedDate"));
                    Map<String, Object> resultWhere = new HashMap<>();
                    resultWhere.put("AttemptId", attemptId);
                    resultPayload.put("where", resultWhere);
                    
                    Object resultResp = apiService.postApiGetList("/autoGet", resultPayload);
                    List<Map<String, Object>> results = normalizeApiList(resultResp);
                    
                    if (results.isEmpty()) {
                        // CÓ ATTEMPT NHƯNG KHÔNG CÓ RESULT
                        Object[] noResultRow = new Object[5];
                        noResultRow[0] = examName;
                        noResultRow[1] = "Chưa làm";
                        noResultRow[2] = null;
                        noResultRow[3] = "Chưa làm";
                        noResultRow[4] = examId;
                        rows.add(noResultRow);
                        continue;
                    }
                    
                    // CÓ RESULT → Thêm vào danh sách
                    Map<String, Object> result = results.get(0);
                    
                    double score = 0.0;
                    Object scoreObj = firstNonNull(result, "Score");
                    if (scoreObj instanceof Number) {
                        score = ((Number) scoreObj).doubleValue();
                    } else if (scoreObj != null) {
                        try { score = Double.parseDouble(scoreObj.toString()); } catch (Exception ignored) {}
                    }
                    
                    String submitDate = formatDate(firstNonNull(result, "SubmittedDate"));
                    
                    Object[] completedRow = new Object[5];
                    completedRow[0] = examName;
                    completedRow[1] = submitDate;
                    completedRow[2] = score;
                    completedRow[3] = "Đã chấm";
                    completedRow[4] = examId;
                    rows.add(completedRow);
                }
                
                System.out.println("DEBUG: Total rows=" + rows.size() + " for class=" + classId);
                
            } catch (Exception ex) {
                ex.printStackTrace();
                System.err.println("ERROR in loadExamList: " + ex.getMessage());
            }
            
            return rows;
        }
        
        @Override
        protected void done() {
            try {
                List<Object[]> data = get();
                SwingUtilities.invokeLater(() -> {
                    tableModel.setRowCount(0);
                    rowToExamIdMap.clear();
                    
                    int stt = 1;
                    for (Object[] row : data) {
                        Object[] displayRow = new Object[]{
                            stt,
                            row[0],
                            row[1],
                            row[2],
                            row[3],
                            ""
                        };
                        
                        int examId = 0;
                        if (row[4] instanceof Number) {
                            examId = ((Number) row[4]).intValue();
                        } else if (row[4] != null) {
                            try { examId = Integer.parseInt(row[4].toString()); } catch (Exception ignored) {}
                        }
                        rowToExamIdMap.put(stt - 1, examId);
                        
                        tableModel.addRow(displayRow);
                        stt++;
                    }
                    totalExamsLabel.setText("Tổng số bài kiểm tra: " + data.size());
                    System.out.println("UI: Loaded " + data.size() + " exams for student " + studentId + " in class " + classId);
                });
            } catch (Exception e) {
                e.printStackTrace();
                SwingUtilities.invokeLater(() -> 
                    JOptionPane.showMessageDialog(StudentExamListWindow.this,
                        "Lỗi khi tải danh sách bài thi: " + e.getMessage(),
                        "Lỗi",
                        JOptionPane.ERROR_MESSAGE)
                );
            }
        }
    };
    worker.execute();
}
    
    // ========== HELPER METHODS ==========
    
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
    
    private Object firstNonNull(Map<String, Object> m, String... keys) {
        if (m == null) return null;
        for (String k : keys) {
            if (k == null) continue;
            Object v = m.get(k);
            if (v != null) return v;
            
            // Thử case-insensitive
            for (Map.Entry<String, Object> entry : m.entrySet()) {
                if (entry.getKey() != null && entry.getKey().equalsIgnoreCase(k)) {
                    return entry.getValue();
                }
            }
        }
        return null;
    }
    
    private String formatDate(Object dateObj) {
        if (dateObj == null) return "Chưa làm";
        String date = dateObj.toString().trim();
        if (date.isEmpty() || "null".equalsIgnoreCase(date)) return "Chưa làm";
        return date;
    }
    
    // ========== OPEN EXAM DETAIL ==========
    
    private void openExamDetail(int row) {
    try {
        // Lấy examId từ map
        Integer examId = rowToExamIdMap.get(row);
        if (examId == null || examId <= 0) {
            JOptionPane.showMessageDialog(this,
                "Không tìm thấy ID bài thi!",
                "Lỗi",
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        String examName = (String) tableModel.getValueAt(row, 1); // Cột 1: Tên Bài
        Object scoreObj = tableModel.getValueAt(row, 3); // Cột 3: Điểm
        
        double score = 0.0;
        if (scoreObj instanceof Number) {
            score = ((Number) scoreObj).doubleValue();
        } else if (scoreObj != null) {
            try {
                score = Double.parseDouble(scoreObj.toString());
            } catch (NumberFormatException e) {
                System.err.println("ERROR: Cannot parse score from " + scoreObj);
            }
        }
        
        System.out.println("DEBUG: Opening ExamDetailWindow for examId=" + examId + ", studentId=" + studentId);
        
        new ExamDetailWindow(apiService, authService, examId, studentId, 
                            studentName, examName, score);
        
    } catch (Exception e) {
        e.printStackTrace();
        JOptionPane.showMessageDialog(this,
            "Lỗi khi mở chi tiết bài thi: " + e.getMessage(),
            "Lỗi",
            JOptionPane.ERROR_MESSAGE);
    }
}
    
    // ========== RENDERERS ==========
    
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