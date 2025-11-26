package com.example.taskmanager.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Collections;
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
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.Timer;

import com.example.taskmanager.model.Student;
import com.example.taskmanager.service.ApiService;
import com.example.taskmanager.service.AuthService;

public class ClassDetailWindow extends JFrame {
    private ApiService apiService;
    private AuthService authService;
    private String className;
    private String teacherName;
    private int classId;
    private MainWindow mainWindow;
    private TeacherDashboard teacherDashboard;  
    
    private JTable studentTable;
    private DefaultTableModel tableModel;
    private JLabel classInfoLabel;
    private JLabel studentCountLabel;
    private JPanel examsPanel; // Panel chứa các card bài kiểm tra
    private JLabel loadingLabel;  
    private DefaultTableModel studentTableModel;

    
    public ClassDetailWindow(ApiService apiService, AuthService authService, 
                            String className, String teacherName, int classId, MainWindow mainWindow, TeacherDashboard teacherDashboard) {
        this.apiService = apiService;
        this.authService = authService;
        this.className = className;
        this.teacherName = teacherName;
        this.classId = classId; 
        this.mainWindow = mainWindow;
        this.teacherDashboard = teacherDashboard;

        setTitle("Chi Tiết Lớp: " + className);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1100, 650);
        setLocationRelativeTo(null);

        // Tải classId

        initUI();
        loadData();
        
        setVisible(true);
    }
    
    private void initUI() {
        JPanel mainPanel = new JPanel(new BorderLayout(0, 0));
        mainPanel.setBackground(new Color(0xF8F9FA));
        
        // Header
        JPanel headerPanel = createHeaderPanel();
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        
        // // Content
        // JPanel contentPanel = new JPanel();
        // contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        // contentPanel.setBackground(new Color(0xF8F9FA));
        // contentPanel.setBorder(new EmptyBorder(25, 30, 25, 30));
        // contentPanel.setBorder(BorderFactory.createLineBorder(Color.RED, 2)); // Thêm viền đỏ quanh contentPanel
        
        // // Info panel
        // JPanel infoPanel = createInfoPanel();
        // contentPanel.add(infoPanel);
        // contentPanel.add(Box.createVerticalStrut(20));
        
        // // Exams panel
        // examsPanel = new JPanel();
        // examsPanel.setLayout(new BoxLayout(examsPanel, BoxLayout.Y_AXIS));
        // examsPanel.setOpaque(false);
        // examsPanel.setBorder(BorderFactory.createLineBorder(Color.GREEN, 2)); // Viền xanh lá để kiểm tra
        // examsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        // examsPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, examsPanel.getPreferredSize().height));

        // JLabel examsTitle = new JLabel("📝 Danh Sách Bài Kiểm Tra");
        // examsTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        // examsTitle.setForeground(new Color(0x1F2937));
        // examsTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        // contentPanel.add(examsTitle);
        // contentPanel.add(Box.createVerticalStrut(10));
        // contentPanel.add(examsPanel);
        // contentPanel.add(Box.createVerticalStrut(20));
        
        // // Student table panel
        // JPanel studentTablePanel = createStudentTablePanel();
        // contentPanel.add(studentTablePanel);
        
        // // Thêm JScrollPane bao quanh contentPanel
        // JScrollPane scrollPane = new JScrollPane(contentPanel);
        // scrollPane.setBorder(null);
        // scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        // scrollPane.setBackground(new Color(0xF8F9FA));
        // mainPanel.add(scrollPane, BorderLayout.CENTER);
        
        // add(mainPanel);

        // --- Replace current contentPanel + examsPanel block with this ---

        // Content wrapper: dùng BorderLayout để dễ ép full-width
        JPanel contentWrapper = new JPanel(new BorderLayout());
        contentWrapper.setBackground(new Color(0xF8F9FA));
        // padding xung quanh nếu muốn
        contentWrapper.setBorder(new EmptyBorder(25, 30, 25, 30));

        // Inner panel chứa đầu mục theo chiều dọc (BoxLayout.Y_AXIS)
        // giữ innerPanel nhỏ để BoxLayout xử lý vertical stacking
        JPanel innerPanel = new JPanel();
        innerPanel.setLayout(new BoxLayout(innerPanel, BoxLayout.Y_AXIS));
        innerPanel.setBackground(new Color(0xF8F9FA));
        // IMPORTANT: make innerPanel fill horizontally in the viewport
        innerPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Info panel
        JPanel infoPanel = createInfoPanel();
        infoPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        innerPanel.add(infoPanel);
        innerPanel.add(Box.createVerticalStrut(20));

        // Title for exams
        JLabel examsTitle = new JLabel("Danh Sách Bài Kiểm Tra");
        examsTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        examsTitle.setForeground(new Color(0x1F2937));
        examsTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        innerPanel.add(examsTitle);
        innerPanel.add(Box.createVerticalStrut(10));

        // examsPanel
        examsPanel = new JPanel();
        examsPanel.setLayout(new BoxLayout(examsPanel, BoxLayout.Y_AXIS));
        examsPanel.setOpaque(false);
        // ensure examsPanel can expand horizontally
        examsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        //examsPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        // optional debug border
        // examsPanel.setBorder(BorderFactory.createLineBorder(Color.GREEN, 2));

        // BỌC TRONG examsContainer
JPanel examsContainer = new JPanel(new BorderLayout());
examsContainer.setOpaque(false);
examsContainer.add(examsPanel, BorderLayout.NORTH);  // NORTH để tự động xuống dòng
examsContainer.setAlignmentX(Component.LEFT_ALIGNMENT);

        // set examsContainer to expand
        examsContainer.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

        innerPanel.add(examsContainer);
        innerPanel.add(Box.createVerticalStrut(20));

        // student table panel (align left)
        JPanel studentTablePanel = createStudentTablePanel();
        studentTablePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        innerPanel.add(studentTablePanel);

        // 🌟 Thêm padding cho các vùng nội dung
        contentWrapper.setBorder(new EmptyBorder(30, 40, 30, 40)); // vùng ngoài
        infoPanel.setBorder(new EmptyBorder(10, 10, 10, 10));       // vùng info
        examsPanel.setBorder(new EmptyBorder(10, 15, 20, 15));      // danh sách bài
        studentTablePanel.setBorder(new EmptyBorder(10, 15, 20, 15)); // bảng học sinh


        // Put innerPanel into a JScrollPane viewport
        JScrollPane scrollPane = new JScrollPane(innerPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setBackground(new Color(0xF8F9FA));

        // THÊM LOADING LABEL
    loadingLabel = new JLabel("Đang tải dữ liệu...");
    loadingLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
    loadingLabel.setForeground(new Color(0x2563EB));
    loadingLabel.setHorizontalAlignment(SwingConstants.CENTER);
    loadingLabel.setVisible(false);

        // Add to main panel (center)
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(loadingLabel, BorderLayout.SOUTH);

        // add mainPanel to frame
        add(mainPanel);


        // // Tải dữ liệu
        // loadStudentData();
        // loadExamData();
        // CHỜ classId TRƯỚC KHI LOAD
    }
    
    private JPanel createHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout());
        header.setPreferredSize(new Dimension(0, 70));
        header.setBackground(new Color(0x3B82F6));
        header.setBorder(new EmptyBorder(15, 30, 15, 30));
        
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        leftPanel.setOpaque(false);
        
        JButton backBtn = new JButton("Quay Lại");
        backBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        backBtn.setForeground(new Color(0x3B82F6));
        backBtn.setBackground(Color.WHITE);
        backBtn.setBorderPainted(false);
        backBtn.setFocusPainted(false);
        backBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        backBtn.setPreferredSize(new Dimension(110, 35));
        backBtn.addActionListener(e -> {
    dispose();
    teacherDashboard.refresh();
    //mainWindow.showTeacherDashboard(); // GỌI HÀM TRONG MainWindow
});
        leftPanel.add(backBtn);
        
        JLabel titleLabel = new JLabel("Chi Tiết Lớp Học");
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
    
    private JPanel createStudentTablePanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 15));
        panel.setOpaque(false);
        
        // Action bar
        JPanel actionBar = new JPanel(new BorderLayout());
        actionBar.setOpaque(false);
        
        JLabel tableTitle = new JLabel("Danh Sách Học Sinh");
        tableTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        tableTitle.setForeground(new Color(0x1F2937));
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setOpaque(false);
        
        JButton refreshBtn = createActionButton("Làm Mới", new Color(0x6B7280));
        refreshBtn.addActionListener(e -> {
            loadStudentData(classId);
            loadExamData(classId);
        });
        
        JButton createExamBtn = createActionButton("Tạo Bài Kiểm Tra", new Color(0x2563EB));
        createExamBtn.addActionListener(e -> openCreateExam());
        
        JButton exportBtn = createActionButton("Xuất Excel", new Color(0x059669));
        exportBtn.addActionListener(e -> exportToExcel());
        
        buttonPanel.add(createExamBtn);
        buttonPanel.add(refreshBtn);
        buttonPanel.add(exportBtn);
        
        actionBar.add(tableTitle, BorderLayout.WEST);
        actionBar.add(buttonPanel, BorderLayout.EAST);
        
        panel.add(actionBar, BorderLayout.NORTH);
        
        // Create table
        String[] columns = {"STT", "Họ và Tên", "Lớp", "Số Bài Đã Làm", "Điểm TB", "Chi Tiết Bài Thi"};
        studentTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 5;
            }
            
            @Override
            public Class<?> getColumnClass(int column) {
                if (column == 0 || column == 3) return Integer.class;
                if (column == 4) return Double.class;
                return String.class;
            }
        };
        
        studentTable = new JTable(studentTableModel);
        studentTable.setRowHeight(55);
        studentTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        studentTable.setSelectionBackground(new Color(0xDCEEFE));
        studentTable.setSelectionForeground(new Color(0x1F2937));
        studentTable.setShowVerticalLines(false);
        studentTable.setIntercellSpacing(new Dimension(0, 0));
        
        JTableHeader header = studentTable.getTableHeader();
        header.setFont(new Font("Segoe UI", Font.BOLD, 14));
        header.setBackground(new Color(0xF3F4F6));
        header.setForeground(new Color(0x374151));
        header.setPreferredSize(new Dimension(0, 45));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(0xE5E7EB)));
        
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        for (int i = 0; i < studentTable.getColumnCount() - 1; i++) {
            studentTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }
        
        studentTable.getColumnModel().getColumn(4).setCellRenderer(new ScoreRenderer());
        studentTable.getColumnModel().getColumn(5).setCellRenderer(new ButtonRenderer());
        studentTable.getColumnModel().getColumn(5).setCellEditor(new ButtonEditor(new JCheckBox()));
        
        studentTable.getColumnModel().getColumn(0).setPreferredWidth(60);
        studentTable.getColumnModel().getColumn(1).setPreferredWidth(200);
        studentTable.getColumnModel().getColumn(2).setPreferredWidth(100);
        studentTable.getColumnModel().getColumn(3).setPreferredWidth(120);
        studentTable.getColumnModel().getColumn(4).setPreferredWidth(100);
        studentTable.getColumnModel().getColumn(5).setPreferredWidth(180);
        
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
    
    private void loadData() {
    if (classId <= 0) {
        JOptionPane.showMessageDialog(this, "Lỗi: Không có ID lớp!", "Lỗi", JOptionPane.ERROR_MESSAGE);
        return;
    }

    loadingLabel.setText("Đang tải dữ liệu...");
    loadingLabel.setVisible(true);

    loadStudentData(classId);  // ← DÙNG classId TRỰC TIẾP
    loadExamData(classId);     // ← DÙNG classId TRỰC TIẾP

    // Ẩn loading sau 2s
    new Timer(2000, e -> {
        SwingUtilities.invokeLater(() -> loadingLabel.setVisible(false));
        ((Timer)e.getSource()).stop();
    }).start();
}

    private void loadStudentData(int classId) {
    SwingWorker<List<Object[]>, Void> worker = new SwingWorker<>() {
        @Override
        protected List<Object[]> doInBackground() throws Exception {
            List<Object[]> rows = new ArrayList<>();

            Map<String, Object> payload = new HashMap<>();
            payload.put("method", "SELECT");
            payload.put("action", "get");
            
            // 4 BẢNG: student → account, student → student_class → classes
            payload.put("table", List.of("student", "account", "student_class", "classes"));
            
            payload.put("columns", List.of(
                "student.Id as StudentId",
                "account.FullName",
                "student.Name as StudentName",
                "classes.Name as ClassName"
            ));

            // JOIN 1: student → account (lấy tên từ account)
            // JOIN 2: student → student_class (quan hệ student-class)
            // JOIN 3: student_class → classes (lấy tên lớp)
            List<Map<String, Object>> joins = new ArrayList<>();
            joins.add(Map.of(
                "type", "inner",
                "on", List.of("student.IdAccount = account.id")
            ));
            joins.add(Map.of(
                "type", "inner",
                "on", List.of("student.Id = student_class.StudentId")
            ));
            joins.add(Map.of(
                "type", "inner",
                "on", List.of("student_class.ClassId = classes.Id")
            ));
            payload.put("join", joins);

            // WHERE: lọc theo classId trong bảng student_class
            Map<String, Object> where = new HashMap<>();
            where.put("student_class.ClassId", classId);
            payload.put("where", where);

            System.out.println("DEBUG: loadStudentData payload = " + payload);

            Object resp = apiService.postApiGetList("/autoGet", payload);
            List<Map<String, Object>> data = normalizeApiList(resp);

            for (Map<String, Object> row : data) {
                rows.add(new Object[]{
                    row.get("StudentId"),
                    row.get("FullName") != null ? row.get("FullName") : row.get("StudentName"),
                    row.get("ClassName"),
                    0,  // Số bài đã làm (TODO: tính sau)
                    0.0, // Điểm TB (TODO: tính sau)
                    ""   // Placeholder cho button
                });
            }
            return rows;
        }

        @Override
        protected void done() {
            try {
                List<Object[]> data = get();
                SwingUtilities.invokeLater(() -> {
                    studentTableModel.setRowCount(0);
                    int stt = 1;
                    for (Object[] row : data) {
                        // Thêm cột STT vào đầu
                        Object[] rowWithSTT = new Object[row.length + 1];
                        rowWithSTT[0] = stt++;
                        System.arraycopy(row, 0, rowWithSTT, 1, row.length);
                        studentTableModel.addRow(rowWithSTT);
                    }
                    studentCountLabel.setText("Tổng số học sinh: " + data.size());
                    System.out.println("UI: Loaded " + data.size() + " students");
                });
            } catch (Exception e) {
                e.printStackTrace();
                SwingUtilities.invokeLater(() -> 
                    JOptionPane.showMessageDialog(ClassDetailWindow.this, 
                        "Lỗi khi tải danh sách học sinh: " + e.getMessage(),
                        "Lỗi", JOptionPane.ERROR_MESSAGE)
                );
            }
        }
    };
    worker.execute();
}

    private void loadExamData(int classId) {
    SwingWorker<List<Object[]>, Void> worker = new SwingWorker<>() {
        @Override
        protected List<Object[]> doInBackground() throws Exception {
            List<Object[]> rows = new ArrayList<>();

            Map<String, Object> payload = new HashMap<>();
            payload.put("method", "SELECT");
            payload.put("action", "get");
            payload.put("table", List.of("exams"));
            payload.put("columns", List.of(
                "id", "ExamName", "NumberQuestion", "Description",
                "PublishDate", "ExpireDate"
            ));
            Map<String, Object> where = new HashMap<>();
            where.put("ClassId", classId);
            payload.put("where", where);

            Object resp = apiService.postApiGetList("/autoGet", payload);
            List<Map<String, Object>> data = normalizeApiList(resp);

            for (Map<String, Object> row : data) {
                rows.add(new Object[]{
                    row.get("id"),
                    row.get("ExamName"),
                    row.get("NumberQuestion"),
                    formatDate(row.get("PublishDate")),
                    formatDate(row.get("ExpireDate")),
                    row.get("Description")
                });
            }
            return rows;
        }

        @Override
protected void done() {
    try {
        List<Object[]> data = get();
        SwingUtilities.invokeLater(() -> {
            examsPanel.removeAll();
            if (data.isEmpty()) {
                JLabel emptyLabel = new JLabel("Chưa có bài kiểm tra nào.");
                emptyLabel.setFont(new Font("Segoe UI", Font.ITALIC, 14));
                emptyLabel.setForeground(new Color(0x6B7280));
                examsPanel.add(emptyLabel);
            } else {
                for (Object[] row : data) {
                    JPanel card = createExamCard(
                        (int)row[0],
                        (String)row[1],
                        row[2] + " câu hỏi",  // NumberQuestion
                        (String)row[3],       // PublishDate
                        (String)row[4],       // ExpireDate
                        (String)row[5]        // Description
                    );
                    card.addMouseListener(new java.awt.event.MouseAdapter() {
                        public void mouseClicked(java.awt.event.MouseEvent e) {
                            openExamDetailForExam((int)row[0], (String)row[1]);
                        }
                        public void mouseEntered(java.awt.event.MouseEvent e) {
                            card.setBackground(new Color(0xF3F4F6));
                        }
                        public void mouseExited(java.awt.event.MouseEvent e) {
                            card.setBackground(Color.WHITE);
                        }
                    });
                    card.setCursor(new Cursor(Cursor.HAND_CURSOR));
                    examsPanel.add(card);
                }
            }
            examsPanel.revalidate();
            examsPanel.repaint();
        });
    } catch (Exception e) {
        e.printStackTrace();
    }
}
    };
    worker.execute();
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

private String formatDate(Object dateObj) {
    if (dateObj == null) return "Chưa công bố";
    String date = dateObj.toString().trim();
    if (date.isEmpty() || "null".equalsIgnoreCase(date)) return "Chưa công bố";
    if (date.contains("Không giới hạn")) return "Không giới hạn";
    return date;
}
    private JPanel createExamCard(int examId, String examName, String numQuestions,
                              String publishDate, String expireDate, String description) {
    JPanel card = new JPanel();
    card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
    card.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createLineBorder(new Color(0xE5E7EB), 1),
        BorderFactory.createEmptyBorder(15, 15, 15, 15)
    ));
    card.setBackground(Color.WHITE);
    card.setPreferredSize(new Dimension(280, 160));
    card.setMaximumSize(new Dimension(280, 160));

    JLabel nameLabel = new JLabel(examName);
    nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
    nameLabel.setForeground(new Color(0x1F2937));

    JLabel questionLabel = new JLabel(numQuestions);
    questionLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
    questionLabel.setForeground(new Color(0x6B7280));

    JLabel dateLabel = new JLabel("Từ: " + publishDate + " → " + expireDate);
    dateLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
    dateLabel.setForeground(new Color(0x374151));

    card.add(nameLabel);
    card.add(Box.createVerticalStrut(5));
    card.add(questionLabel);
    card.add(Box.createVerticalStrut(5));
    card.add(dateLabel);
    if (description != null && !description.isEmpty()) {
        JLabel descLabel = new JLabel("<html><div width=240>" + description + "</div></html>");
        descLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        descLabel.setForeground(new Color(0x4B5563));
        card.add(Box.createVerticalStrut(8));
        card.add(descLabel);
    }

    return card;
}
    
    private void openExamDetail(int row) {
    Object studentIdObj = studentTableModel.getValueAt(row, 1); // StudentId
    String studentName = (String) studentTableModel.getValueAt(row, 2); // FullName
    
    int studentId = 0;
    if (studentIdObj instanceof Number) {
        studentId = ((Number) studentIdObj).intValue();
    } else if (studentIdObj != null) {
        studentId = Integer.parseInt(studentIdObj.toString());
    }
    
    // ← TRUYỀN THÊM classId VÀO CONSTRUCTOR
    new StudentExamListWindow(apiService, authService, studentId, studentName, className, classId);
}

    private void openExamDetailForExam(int examId, String examName) {
        JOptionPane.showMessageDialog(this,
            "Mở chi tiết bài kiểm tra: " + examName + " (ID: " + examId + ")",
            "Thông báo",
            JOptionPane.INFORMATION_MESSAGE);
        // TODO: new ExamDetailWindow(apiService, authService, examId, examName, className);
    }
    
    private void openCreateExam() {
        new CreateExamWindow(apiService, authService, className, teacherName);
    }
    
    private void exportToExcel() {
        JOptionPane.showMessageDialog(this,
            "Chức năng xuất Excel đang được phát triển!",
            "Thông báo",
            JOptionPane.INFORMATION_MESSAGE);
    }
    
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
                        setForeground(new Color(0x059669));
                    } else if (score >= 8.0) {
                        setForeground(new Color(0x0284C7));
                    } else if (score >= 6.5) {
                        setForeground(new Color(0xD97706));
                    } else {
                        setForeground(new Color(0xDC2626));
                    }
                } else {
                    setForeground(table.getSelectionForeground());
                }
            }
            
            return this;
        }
    }
    
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
            setText("Xem Chi Tiết");
            return this;
        }
    }
    
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
            label = "Xem Bài Thi";
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