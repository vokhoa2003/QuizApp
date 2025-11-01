package com.example.taskmanager.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;

import com.example.taskmanager.service.ApiService;
import com.example.taskmanager.service.AuthService;

public class CreateExamWindow extends JFrame {

    private ApiService apiService;
    private AuthService authService;
    private String className;
    private String teacherName;
    private int teacherId;
    private int classId;
    
    // UI Components
    private JTextField examNameField;
    private JSpinner numberOfQuestionsSpinner;
    private JTextArea descriptionArea;
    private JLabel teacherInfoLabel;
    private JLabel classInfoLabel;
    private DateTimePickerPanel publishDatePicker;
    private DateTimePickerPanel expireDatePicker;
    private JLabel templateInfoLabel;
    private JComboBox<Map<String, Object>> periodComboBox;
private JTextField numberOfQuestionsField;
private JTextField timeLimitField;
    
    public CreateExamWindow(ApiService apiService, AuthService authService,
                           String className, String teacherName) {
        this.apiService = apiService;
        this.authService = authService;
        this.className = className;
        this.teacherName = teacherName;
        
        // Get teacher and class info
        loadTeacherAndClassInfo();
        
        setTitle("Tạo Bài Kiểm Tra - " + className);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(800, 700);
        setLocationRelativeTo(null);
        
        initUI();
        
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
        
        // Form panel
        JPanel formPanel = createFormPanel();
        JScrollPane scrollPane = new JScrollPane(formPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        contentPanel.add(scrollPane, BorderLayout.CENTER);
        
        // Button panel
        JPanel buttonPanel = createButtonPanel();
        contentPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        mainPanel.add(contentPanel, BorderLayout.CENTER);
        
        add(mainPanel);
    }
    
    private JPanel createHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout());
        header.setPreferredSize(new Dimension(0, 70));
        header.setBackground(new Color(0x2563EB));
        header.setBorder(new EmptyBorder(15, 30, 15, 30));
        
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        leftPanel.setOpaque(false);
        
        JButton backBtn = new JButton("← Quay Lại");
        backBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        backBtn.setForeground(new Color(0x2563EB));
        backBtn.setBackground(Color.WHITE);
        backBtn.setBorderPainted(false);
        backBtn.setFocusPainted(false);
        backBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        backBtn.setPreferredSize(new Dimension(110, 35));
        backBtn.addActionListener(e -> dispose());
        leftPanel.add(backBtn);
        
        JLabel titleLabel = new JLabel("📝 Tạo Bài Kiểm Tra Mới");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLabel.setForeground(Color.WHITE);
        leftPanel.add(titleLabel);
        
        header.add(leftPanel, BorderLayout.WEST);
        
        return header;
    }
    
    private JPanel createFormPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(25, 25, 25, 25));
        
        // Section: Thông tin chung
        addSectionTitle(panel, "📋 Thông Tin Chung");
        
        // Teacher info (read-only)
        teacherInfoLabel = new JLabel("👨‍🏫 Giáo viên: " + teacherName);
        teacherInfoLabel.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        teacherInfoLabel.setForeground(new Color(0x6B7280));
        teacherInfoLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(teacherInfoLabel);
        panel.add(Box.createVerticalStrut(8));
        
        // Class info (read-only)
        classInfoLabel = new JLabel("🏫 Lớp: " + className);
        classInfoLabel.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        classInfoLabel.setForeground(new Color(0x6B7280));
        classInfoLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(classInfoLabel);
        panel.add(Box.createVerticalStrut(20));
        
        // Exam name
        addLabel(panel, "Tên Bài Kiểm Tra:", true);
        examNameField = new JTextField();
        examNameField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        examNameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        examNameField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0xD1D5DB)),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        panel.add(examNameField);
        panel.add(Box.createVerticalStrut(15));
        
        // Number of questions
        addLabel(panel, "Số Lượng Câu Hỏi:", true);
numberOfQuestionsField = new JTextField("10");
numberOfQuestionsField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
numberOfQuestionsField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
numberOfQuestionsField.setBorder(BorderFactory.createCompoundBorder(
    BorderFactory.createLineBorder(new Color(0xD1D5DB)),
    BorderFactory.createEmptyBorder(5, 10, 5, 10)
));
panel.add(numberOfQuestionsField);
panel.add(Box.createVerticalStrut(15));
        
        // Thêm PeriodId
addLabel(panel, "Kỳ Thi (Period):", true);
periodComboBox = new JComboBox<>();
periodComboBox.setFont(new Font("Segoe UI", Font.PLAIN, 14));
periodComboBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
periodComboBox.setRenderer(new PeriodComboBoxRenderer());
loadExamPeriods(); // Tải danh sách kỳ thi
panel.add(periodComboBox);
panel.add(Box.createVerticalStrut(15));

// Thêm TimeLimit
addLabel(panel, "Thời Gian Làm Bài (phút):", true);
timeLimitField = new JTextField("120");
timeLimitField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
timeLimitField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
timeLimitField.setBorder(BorderFactory.createCompoundBorder(
    BorderFactory.createLineBorder(new Color(0xD1D5DB)),
    BorderFactory.createEmptyBorder(5, 10, 5, 10)
));
panel.add(timeLimitField);
panel.add(Box.createVerticalStrut(25));
        // Description
        addLabel(panel, "Mô Tả:", false);
        descriptionArea = new JTextArea(4, 20);
        descriptionArea.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        descriptionArea.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0xD1D5DB)),
            BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        JScrollPane descScroll = new JScrollPane(descriptionArea);
        descScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
        panel.add(descScroll);
        panel.add(Box.createVerticalStrut(25));
        
        // Section: Thời gian
        addSectionTitle(panel, "⏰ Thời Gian Làm Bài");
        

        // Publish date
        addLabel(panel, "Ngày Giờ Công Bố:", true);
        publishDatePicker = new DateTimePickerPanel(new Date());
        publishDatePicker.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(publishDatePicker);
        panel.add(Box.createVerticalStrut(15));
        
        // Expire date
        addLabel(panel, "Ngày Giờ Kết Thúc:", true);
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.HOUR_OF_DAY, 2);
        expireDatePicker = new DateTimePickerPanel(cal.getTime());
        expireDatePicker.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(expireDatePicker);
        panel.add(Box.createVerticalStrut(15));
        
        // Quick time buttons
        JPanel timeButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        timeButtonPanel.setOpaque(false);
        timeButtonPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        // JButton nowBtn = createSmallButton("⏱️ Bắt đầu ngay");
        // nowBtn.addActionListener(e -> {
        //     publishDatePicker.setDateTime(new Date());
        //     Calendar c = Calendar.getInstance();
        //     c.add(Calendar.HOUR_OF_DAY, 2);
        //     expireDatePicker.setDateTime(c.getTime());
        // });
        
        JButton add1HourBtn = createSmallButton("+1 giờ");
        add1HourBtn.addActionListener(e -> {
            Date current = expireDatePicker.getDateTime();
            Calendar c = Calendar.getInstance();
            c.setTime(current);
            c.add(Calendar.HOUR_OF_DAY, 1);
            expireDatePicker.setDateTime(c.getTime());
        });
        
        JButton add1DayBtn = createSmallButton("+1 ngày");
        add1DayBtn.addActionListener(e -> {
            Date current = expireDatePicker.getDateTime();
            Calendar c = Calendar.getInstance();
            c.setTime(current);
            c.add(Calendar.DAY_OF_MONTH, 1);
            expireDatePicker.setDateTime(c.getTime());
        });
        
        //timeButtonPanel.add(nowBtn);
        timeButtonPanel.add(add1HourBtn);
        timeButtonPanel.add(add1DayBtn);
        panel.add(timeButtonPanel);
        
        return panel;
    }
    
    private void addSectionTitle(JPanel panel, String title) {
        JLabel label = new JLabel(title);
        label.setFont(new Font("Segoe UI", Font.BOLD, 18));
        label.setForeground(new Color(0x1F2937));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(label);
        panel.add(Box.createVerticalStrut(15));
    }
    
    private void addLabel(JPanel panel, String text, boolean required) {
        JLabel label = new JLabel(text + (required ? " *" : ""));
        label.setFont(new Font("Segoe UI", Font.BOLD, 14));
        label.setForeground(new Color(0x374151));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        if (required) {
            label.setForeground(new Color(0x1F2937));
        }
        panel.add(label);
        panel.add(Box.createVerticalStrut(6));
    }
    
    private JButton createSmallButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btn.setForeground(new Color(0x374151));
        btn.setBackground(Color.WHITE);
        btn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0xD1D5DB)),
            BorderFactory.createEmptyBorder(5, 12, 5, 12)
        ));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setBackground(new Color(0xF3F4F6));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBackground(Color.WHITE);
            }
        });
        
        return btn;
    }
    
    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        panel.setOpaque(false);
        
        JButton cancelBtn = new JButton("Hủy");
        cancelBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        cancelBtn.setForeground(new Color(0x6B7280));
        cancelBtn.setBackground(Color.WHITE);
        cancelBtn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0xD1D5DB)),
            BorderFactory.createEmptyBorder(10, 20, 10, 20)
        ));
        cancelBtn.setFocusPainted(false);
        cancelBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        cancelBtn.addActionListener(e -> dispose());
        
        JButton saveBtn = new JButton("Tạo Bài Kiểm Tra");
        saveBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        saveBtn.setForeground(Color.WHITE);
        saveBtn.setBackground(new Color(0x2563EB));
        saveBtn.setBorderPainted(false);
        saveBtn.setFocusPainted(false);
        saveBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        saveBtn.setBorder(BorderFactory.createEmptyBorder(10, 25, 10, 25));
        saveBtn.addActionListener(e -> saveExam());
        
        saveBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                saveBtn.setBackground(new Color(0x1D4ED8));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                saveBtn.setBackground(new Color(0x2563EB));
            }
        });
        
        panel.add(cancelBtn);
        panel.add(saveBtn);
        
        return panel;
    }
    private void loadTeacherAndClassInfo() {
        Map<String, Object> params = new HashMap<>();
        params.put("action", "get");
        params.put("method", "SELECT");
        params.put("table", List.of("teacher", "classes"));
        params.put("columns", List.of("teacher.id as teacherId", "classes.id as classId"));
        List<Map<String, Object>> join = new ArrayList<>();
        Map<String, Object> j1 = new HashMap<>();
        j1.put("type", "inner");
        j1.put("on", List.of("teacher.ClassId = classes.Id"));
        join.add(j1);
        params.put("join", join);
        Map<String, Object> where = new HashMap<>();
        where.put("teacher.Name", teacherName);
        where.put("classes.Name", className);
        params.put("where", where);
        System.out.println("Request params for teacher and class info: " + params);

        List<Map<String, Object>> result = apiService.postApiGetList("/autoGet", params);
        System.out.println("API Response for teacher and class info: " + result);

        if (!result.isEmpty()) {
            Map<String, Object> record = result.get(0);
            Object teacherIdObj = record.get("teachers.id");
            Object classIdObj = record.get("classes.id");
            teacherId = teacherIdObj != null ? Integer.parseInt(teacherIdObj.toString()) : 0;
            classId = classIdObj != null ? Integer.parseInt(classIdObj.toString()) : 0;
        }
    }
    
    // private void loadTeacherAndClassInfo() {
    //     // TODO: Load teacher ID and class ID from API
    //     // For now, using mock data
    //     SwingWorker<Map<String, Integer>, Void> worker = new SwingWorker<>() {
    //         @Override
    //         protected Map<String, Integer> doInBackground() {
    //             Map<String, Integer> result = new HashMap<>();
                
    //             // Get teacher ID by name
    //             Map<String, Object> teacherParams = new HashMap<>();
    //             teacherParams.put("action", "get");
    //             teacherParams.put("method", "SELECT");
    //             teacherParams.put("table", List.of("teachers"));
    //             teacherParams.put("columns", List.of("teachers.id"));
                
    //             Map<String, Object> whereTeacher = new HashMap<>();
    //             whereTeacher.put("teachers.FullName", teacherName);
    //             teacherParams.put("where", whereTeacher);
                
    //             List<Map<String, Object>> teacherData = apiService.postApiGetList("/autoGet", teacherParams);
    //             if (!teacherData.isEmpty()) {
    //                 Object idObj = teacherData.get(0).get("teachers.id");
    //                 result.put("teacherId", idObj != null ? Integer.parseInt(idObj.toString()) : 0);
    //             }
                
    //             // Get class ID by name
    //             Map<String, Object> classParams = new HashMap<>();
    //             classParams.put("action", "get");
    //             classParams.put("method", "SELECT");
    //             classParams.put("table", List.of("classes"));
    //             classParams.put("columns", List.of("classes.id"));
                
    //             Map<String, Object> whereClass = new HashMap<>();
    //             whereClass.put("classes.Name", className);
    //             classParams.put("where", whereClass);
                
    //             List<Map<String, Object>> classData = apiService.postApiGetList("/autoGet", classParams);
    //             if (!classData.isEmpty()) {
    //                 Object idObj = classData.get(0).get("classes.id");
    //                 result.put("classId", idObj != null ? Integer.parseInt(idObj.toString()) : 0);
    //             }
                
    //             return result;
    //         }
            
    //         @Override
    //         protected void done() {
    //             try {
    //                 Map<String, Integer> ids = get();
    //                 teacherId = ids.getOrDefault("teacherId", 0);
    //                 classId = ids.getOrDefault("classId", 0);
    //             } catch (Exception e) {
    //                 e.printStackTrace();
    //             }
    //         }
    //     };
    //     worker.execute();
    // }
    
    private void loadExamPeriods() {
    SwingWorker<Void, Void> worker = new SwingWorker<>() {
        @Override
        protected Void doInBackground() {
            Map<String, Object> params = new HashMap<>();
            params.put("action", "get");
            params.put("method", "SELECT");
            params.put("table", List.of("exam_period"));
            params.put("columns", List.of("Id", "Name", "Description"));

            List<Map<String, Object>> periods = apiService.postApiGetList("/autoGet", params);
            javax.swing.SwingUtilities.invokeLater(() -> {
                periodComboBox.removeAllItems();
                if (periods != null && !periods.isEmpty()) {
                    for (Map<String, Object> period : periods) {
                        periodComboBox.addItem(period);
                    }
                } else {
                    periodComboBox.addItem(createEmptyPeriodItem());
                }
                periodComboBox.setSelectedIndex(0);
            });
            return null;
        }
    };
    worker.execute();
}

private Map<String, Object> createEmptyPeriodItem() {
    Map<String, Object> empty = new HashMap<>();
    empty.put("Id", 0);
    empty.put("Name", "-- Chọn kỳ thi --");
    empty.put("Description", "");
    return empty;
}

private static class PeriodComboBoxRenderer extends javax.swing.DefaultListCellRenderer {
    @Override
    public Component getListCellRendererComponent(javax.swing.JList<?> list, Object value,
                                                  int index, boolean isSelected, boolean cellHasFocus) {
        super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        if (value instanceof Map) {
            Map<String, Object> period = (Map<String, Object>) value;
            String name = (String) period.get("Name");
            String desc = (String) period.get("Description");
            if (desc != null && !desc.isEmpty()) {
                setText(name + " - " + desc);
            } else {
                setText(name);
            }
        }
        return this;
    }
}

    private void saveExam() {
    // === Validation ở đây (giữ nguyên) ===
    String examName = examNameField.getText().trim();
    if (examName.isEmpty()) {
        JOptionPane.showMessageDialog(this, "Vui lòng nhập tên bài kiểm tra!", "Lỗi", JOptionPane.WARNING_MESSAGE);
        examNameField.requestFocus();
        return;
    }

    Date publishDate = publishDatePicker.getDateTime();
    Date expireDate = expireDatePicker.getDateTime();
    if (expireDate.before(publishDate)) {
        JOptionPane.showMessageDialog(this, "Ngày kết thúc phải sau ngày công bố!", "Lỗi", JOptionPane.WARNING_MESSAGE);
        return;
    }

    // === Chuyển toàn bộ logic vào SwingWorker ===
    SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
        @Override
        protected Boolean doInBackground() {
            try {
                // === Lấy dữ liệu từ UI (phải trong EDT hoặc sau khi validate) ===
                String examName = examNameField.getText().trim();
                String description = descriptionArea.getText().trim();

                // Number of questions
                int numberOfQuestions = Integer.parseInt(numberOfQuestionsField.getText().trim());
                if (numberOfQuestions < 1 || numberOfQuestions > 200) return false;

                // Time limit
                int timeLimit = Integer.parseInt(timeLimitField.getText().trim());
                if (timeLimit < 1 || timeLimit > 1440) return false;

                // PeriodId
                Map<String, Object> selectedPeriod = (Map<String, Object>) periodComboBox.getSelectedItem();
                int periodId = 0;
                if (selectedPeriod != null) {
                    Object idObj = selectedPeriod.get("Id");
                    if (idObj != null) {
                        periodId = Integer.parseInt(idObj.toString());
                    }
                }
                if (periodId == 0) return false;

                // Dates
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String publishDateStr = sdf.format(publishDate);
                String expireDateStr = sdf.format(expireDate);
                String createDateStr = sdf.format(new Date());

                // === Gửi API ===
                Map<String, Object> params = new HashMap<>();
                params.put("action", "insert");
                params.put("table", "exams");

                Map<String, Object> data = new HashMap<>();
                data.put("ClassId", classId);
                data.put("Name", examName);
                data.put("NumberQuestion", numberOfQuestions);
                data.put("Description", description);
                data.put("CreateDate", createDateStr);
                data.put("PublishDate", publishDateStr);
                data.put("ExpireDate", expireDateStr);
                data.put("TeacherId", teacherId);
                data.put("PeriodId", periodId);
                data.put("TimeLimit", timeLimit);

                params.put("data", data);

                apiService.postApiGetList("/autoUpdate", params);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }

        @Override
        protected void done() {
            try {
                boolean success = get();
                if (success) {
                    JOptionPane.showMessageDialog(CreateExamWindow.this,
                        "Tạo bài kiểm tra thành công!",
                        "Thành công",
                        JOptionPane.INFORMATION_MESSAGE);
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(CreateExamWindow.this,
                        "Có lỗi xảy ra khi tạo bài kiểm tra!",
                        "Lỗi",
                        JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(CreateExamWindow.this,
                    "Lỗi: " + e.getMessage(),
                    "Lỗi",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    };
    worker.execute();
}
    // Custom DateTime Picker Panel
    private static class DateTimePickerPanel extends JPanel {
        private JComboBox<Integer> dayCombo;
        private JComboBox<Integer> monthCombo;
        private JComboBox<Integer> yearCombo;
        private JComboBox<Integer> hourCombo;
        private JComboBox<Integer> minuteCombo;
        private Calendar calendar;
        
        public DateTimePickerPanel(Date initialDate) {
            calendar = Calendar.getInstance();
            calendar.setTime(initialDate);
            
            setLayout(new FlowLayout(FlowLayout.LEFT, 8, 0));
            setOpaque(false);
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
            
            // Day combo
            dayCombo = createStyledCombo();
            for (int i = 1; i <= 31; i++) {
                dayCombo.addItem(i);
            }
            dayCombo.setSelectedItem(calendar.get(Calendar.DAY_OF_MONTH));
            
            // Month combo
            monthCombo = createStyledCombo();
            String[] months = {"Tháng 1", "Tháng 2", "Tháng 3", "Tháng 4", "Tháng 5", "Tháng 6",
                              "Tháng 7", "Tháng 8", "Tháng 9", "Tháng 10", "Tháng 11", "Tháng 12"};
            JComboBox<String> monthNameCombo = new JComboBox<>(months);
            styleComboBox(monthNameCombo);
            monthNameCombo.setSelectedIndex(calendar.get(Calendar.MONTH));
            monthNameCombo.addActionListener(e -> {
                updateDaysInMonth();
            });
            
            // Year combo
            yearCombo = createStyledCombo();
            int currentYear = Calendar.getInstance().get(Calendar.YEAR);
            for (int i = currentYear - 1; i <= currentYear + 5; i++) {
                yearCombo.addItem(i);
            }
            yearCombo.setSelectedItem(calendar.get(Calendar.YEAR));
            yearCombo.addActionListener(e -> {
                updateDaysInMonth();
            });
            
            // Hour combo
            hourCombo = createStyledCombo();
            for (int i = 0; i <= 23; i++) {
                hourCombo.addItem(i);
            }
            hourCombo.setSelectedItem(calendar.get(Calendar.HOUR_OF_DAY));
            
            // Minute combo
            minuteCombo = createStyledCombo();
            for (int i = 0; i <= 59; i += 5) {
                minuteCombo.addItem(i);
            }
            int minute = calendar.get(Calendar.MINUTE);
            minuteCombo.setSelectedItem((minute / 5) * 5);
            
            // Add components
            add(dayCombo);
            add(monthNameCombo);
            add(yearCombo);
            add(new JLabel("  "));
            add(hourCombo);
            add(new JLabel(":"));
            add(minuteCombo);
            
            // Store month combo reference for updateDaysInMonth
            putClientProperty("monthNameCombo", monthNameCombo);
        }
        
        private JComboBox<Integer> createStyledCombo() {
            JComboBox<Integer> combo = new JComboBox<>();
            styleComboBox(combo);
            return combo;
        }
        
        private void styleComboBox(JComboBox<?> combo) {
            combo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            combo.setBackground(Color.WHITE);
            combo.setFocusable(false);
            combo.setCursor(new Cursor(Cursor.HAND_CURSOR));
        }
        
        private void updateDaysInMonth() {
            @SuppressWarnings("unchecked")
            JComboBox<String> monthNameCombo = (JComboBox<String>) getClientProperty("monthNameCombo");
            
            int month = monthNameCombo.getSelectedIndex();
            Integer year = (Integer) yearCombo.getSelectedItem();
            
            if (year != null) {
                Calendar cal = Calendar.getInstance();
                cal.set(year, month, 1);
                int maxDays = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
                
                Integer currentDay = (Integer) dayCombo.getSelectedItem();
                dayCombo.removeAllItems();
                for (int i = 1; i <= maxDays; i++) {
                    dayCombo.addItem(i);
                }
                
                if (currentDay != null && currentDay <= maxDays) {
                    dayCombo.setSelectedItem(currentDay);
                } else {
                    dayCombo.setSelectedItem(maxDays);
                }
            }
        }
        
        public Date getDateTime() {
            @SuppressWarnings("unchecked")
            JComboBox<String> monthNameCombo = (JComboBox<String>) getClientProperty("monthNameCombo");
            
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.YEAR, (Integer) yearCombo.getSelectedItem());
            cal.set(Calendar.MONTH, monthNameCombo.getSelectedIndex());
            cal.set(Calendar.DAY_OF_MONTH, (Integer) dayCombo.getSelectedItem());
            cal.set(Calendar.HOUR_OF_DAY, (Integer) hourCombo.getSelectedItem());
            cal.set(Calendar.MINUTE, (Integer) minuteCombo.getSelectedItem());
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            
            return cal.getTime();
        }
        
        public void setDateTime(Date date) {
            @SuppressWarnings("unchecked")
            JComboBox<String> monthNameCombo = (JComboBox<String>) getClientProperty("monthNameCombo");
            
            calendar.setTime(date);
            dayCombo.setSelectedItem(calendar.get(Calendar.DAY_OF_MONTH));
            monthNameCombo.setSelectedIndex(calendar.get(Calendar.MONTH));
            yearCombo.setSelectedItem(calendar.get(Calendar.YEAR));
            hourCombo.setSelectedItem(calendar.get(Calendar.HOUR_OF_DAY));
            
            int minute = calendar.get(Calendar.MINUTE);
            minuteCombo.setSelectedItem((minute / 5) * 5);
        }
    }
}