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
    private int classId;
    
    private JTable studentTable;
    private DefaultTableModel tableModel;
    private JLabel classInfoLabel;
    private JLabel studentCountLabel;
    private JPanel examsPanel; // Panel chứa các card bài kiểm tra
    
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

        // Tải classId
        loadClassId();

        initUI();
        
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

        // examsPanel: we'll put it inside a panel that uses BorderLayout so it can expand to full width
        examsPanel = new JPanel();
        examsPanel.setLayout(new BoxLayout(examsPanel, BoxLayout.Y_AXIS));
        examsPanel.setOpaque(false);
        // ensure examsPanel can expand horizontally
        examsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        examsPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        // optional debug border
        // examsPanel.setBorder(BorderFactory.createLineBorder(Color.GREEN, 2));

        // Wrap examsPanel in a container so it stretches across available width
        JPanel examsContainer = new JPanel(new BorderLayout());
        examsContainer.setOpaque(false);
        examsContainer.add(examsPanel, BorderLayout.CENTER);
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

        // Add to main panel (center)
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        // add mainPanel to frame
        add(mainPanel);


        // Tải dữ liệu
        loadStudentData();
        loadExamData();
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
        backBtn.addActionListener(e -> dispose());
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
            loadStudentData();
            loadExamData();
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
        tableModel = new DefaultTableModel(columns, 0) {
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
        
        studentTable = new JTable(tableModel);
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
    
    private void loadClassId() {
        SwingWorker<Integer, Void> worker = new SwingWorker<>() {
            @Override
            protected Integer doInBackground() {
                Map<String, Object> params = new HashMap<>();
                params.put("action", "get");
                params.put("method", "SELECT");
                params.put("table", List.of("classes"));
                params.put("columns", List.of("classes.id"));

                Map<String, Object> where = new HashMap<>();
                where.put("classes.Name", className);
                params.put("where", where);

                try {
                    List<Map<String, Object>> classData = apiService.postApiGetList("/autoGet", params);
                    if (!classData.isEmpty()) {
                        Object idObj = classData.get(0).get("classes.id");
                        return idObj != null ? Integer.parseInt(idObj.toString()) : 0;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return 0;
            }

            @Override
            protected void done() {
                try {
                    classId = get();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        worker.execute();
    }

    private void loadStudentData() {
        SwingWorker<List<Object[]>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Object[]> doInBackground() {
                List<Object[]> data = new ArrayList<>();
                // Gọi API để lấy danh sách học sinh
                // apiService.getStudentsByClass(className);
                
                // Dữ liệu mẫu
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

    private void loadExamData() {
        SwingWorker<List<Object[]>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Object[]> doInBackground() {
                List<Object[]> data = new ArrayList<>();

                // Gọi API để lấy danh sách bài kiểm tra
                Map<String, Object> params = new HashMap<>();
                params.put("action", "get");
                params.put("method", "SELECT");
                params.put("table", List.of("exams"));
                params.put("columns", List.of("exams.id", "exams.Name", "exams.PublishDate", 
                                            "exams.ExpireDate", "exams.NumberQuestion"));
                
                Map<String, Object> where = new HashMap<>();
                where.put("exams.ClassId", classId);
                params.put("where", where);

                try {
                    List<Map<String, Object>> examData = apiService.postApiGetList("/autoGet", params);
                    int stt = 1;
                    for (Map<String, Object> exam : examData) {
                        data.add(new Object[]{
                            stt++,
                            exam.get("exams.Name"),
                            exam.get("exams.PublishDate"),
                            exam.get("exams.ExpireDate"),
                            exam.get("exams.NumberQuestion"),
                            exam.get("exams.id")
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                // Dữ liệu mẫu nếu API chưa sẵn sàng
                if (data.isEmpty()) {
                    data.add(new Object[]{1, "Kiểm tra 1", "2025-10-20 08:00:00", "2025-10-20 10:00:00", 10, 1});
                    data.add(new Object[]{2, "Kiểm tra 2", "2025-10-21 09:00:00", "2025-10-21 11:00:00", 15, 2});
                }

                return data;
            }

            @Override
            protected void done() {
                try {
                    List<Object[]> data = get();
                    examsPanel.removeAll();
                    for (Object[] row : data) {
                        JPanel card = createExamCard(
                            (int) row[0], 
                            (String) row[1], 
                            (String) row[2], 
                            (String) row[3], 
                            (int) row[4], 
                            (int) row[5]
                        );
                        examsPanel.add(card);
                        examsPanel.add(Box.createVerticalStrut(10));
                    }
                    examsPanel.revalidate();
                    examsPanel.repaint();
                } catch (Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(ClassDetailWindow.this,
                        "Lỗi khi tải danh sách bài kiểm tra!",
                        "Lỗi",
                        JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private JPanel createExamCard(int stt, String examName, String publishDate, String expireDate, int numQuestions, int examId) {
        JPanel card = new JPanel();
        card.setLayout(new BorderLayout(10, 10));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0xE5E7EB), 1),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 140)); // max width large, height cố định
        card.setAlignmentX(Component.LEFT_ALIGNMENT);


        // Nội dung card
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setOpaque(false);

        JLabel nameLabel = new JLabel("📝 " + examName);
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        nameLabel.setForeground(new Color(0x1F2937));
        contentPanel.add(nameLabel);
        contentPanel.add(Box.createVerticalStrut(5));

        JLabel publishLabel = new JLabel("Ngày công bố: " + publishDate);
        publishLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        publishLabel.setForeground(new Color(0x6B7280));
        contentPanel.add(publishLabel);

        JLabel expireLabel = new JLabel("Ngày kết thúc: " + expireDate);
        expireLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        expireLabel.setForeground(new Color(0x6B7280));
        contentPanel.add(expireLabel);

        JLabel questionsLabel = new JLabel("Số câu hỏi: " + numQuestions);
        questionsLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        questionsLabel.setForeground(new Color(0x6B7280));
        contentPanel.add(questionsLabel);

        card.add(contentPanel, BorderLayout.CENTER);

        // Nút hành động
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setOpaque(false);

        JButton detailBtn = new JButton("Xem Chi Tiết");
        detailBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        detailBtn.setForeground(Color.WHITE);
        detailBtn.setBackground(new Color(0x8B5CF6));
        detailBtn.setBorderPainted(false);
        detailBtn.setFocusPainted(false);
        detailBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        detailBtn.addActionListener(e -> openExamDetailForExam(examId, examName));
        detailBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                detailBtn.setBackground(new Color(0x7C3AED));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                detailBtn.setBackground(new Color(0x8B5CF6));
            }
        });

        buttonPanel.add(detailBtn);
        card.add(buttonPanel, BorderLayout.SOUTH);

        return card;
    }
    
    private void openExamDetail(int row) {
        String studentName = (String) tableModel.getValueAt(row, 1);
        int studentId = (int) tableModel.getValueAt(row, 0);
        
        new StudentExamListWindow(apiService, authService, studentId, studentName, className);
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
            setText("📋 Xem Bài Thi");
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