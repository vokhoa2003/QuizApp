package com.example.taskmanager.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;

import com.example.taskmanager.model.Task;
import com.example.taskmanager.service.ApiService;
import com.example.taskmanager.service.AuthService;
import com.example.taskmanager.service.ExamService;
import com.example.taskmanager.service.StudentInfoService;
import com.formdev.flatlaf.FlatLightLaf;

public class StudentDashboard extends JFrame {
    private ApiService apiService;
    private AuthService authService;
    private Task currentStudent;
    private ExamService examService;
    private MainWindow mainWindow;  // Thêm reference đến MainWindow
    private QuizAppSwing quizAppSwing;
    
    private JLabel studentNameLabel;
    private JPanel classesPanel;
    private JPanel examsPanel;
    private JLabel examsTitle;
    private String selectedClassName = null;
    

    public StudentDashboard(ApiService apiService, AuthService authService, Task teacher) {
        this(apiService, authService, teacher, null, null);
    }

    //constructor mới với MainWindow
    public StudentDashboard(ApiService apiService, AuthService authService, Task student, QuizAppSwing quizAppSwing, MainWindow mainWindow) {
        this.apiService = apiService;
        this.authService = authService;
        this.currentStudent = student;
        this.mainWindow = mainWindow;
        this.examService = new ExamService(apiService); // Khởi tạo ExamService
        this.quizAppSwing = quizAppSwing;
        
        setTitle("Trang Chủ Học Sinh - SecureStudy");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1300, 750);
        setLocationRelativeTo(null);
        
        initUI();
        loadStudentClasses();
        
        setVisible(true);
    }
    
    private void initUI() {
        JPanel mainPanel = new JPanel(new BorderLayout(0, 0));
        mainPanel.setBackground(new Color(0xF8F9FA));
        
        // Header
        JPanel headerPanel = createHeaderPanel();
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        
        // Content area with split view
        JPanel contentPanel = new JPanel(new BorderLayout(20, 0));
        contentPanel.setBackground(new Color(0xF8F9FA));
        contentPanel.setBorder(new EmptyBorder(25, 30, 25, 30));
        
        // Left side - Classes
        JPanel leftPanel = createClassesPanel();
        contentPanel.add(leftPanel, BorderLayout.WEST);
        
        // Right side - Exams
        JPanel rightPanel = createExamsPanel();
        contentPanel.add(rightPanel, BorderLayout.CENTER);
        
        mainPanel.add(contentPanel, BorderLayout.CENTER);
        
        add(mainPanel);
    }
    
    private JPanel createHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout());
        header.setPreferredSize(new Dimension(0, 80));
        header.setBackground(new Color(0x0EA5E9));
        header.setBorder(new EmptyBorder(15, 30, 15, 30));
        
        // Left side - Logo and title
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        leftPanel.setOpaque(false);
        
        JLabel logoLabel = new JLabel("🎓");
        logoLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 36));
        leftPanel.add(logoLabel);
        
        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setOpaque(false);
        
        JLabel titleLabel = new JLabel("SecureStudy");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);
        
        JLabel subtitleLabel = new JLabel("Hệ thống thi trực tuyến");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitleLabel.setForeground(new Color(0xBAE6FD));
        
        titlePanel.add(titleLabel);
        titlePanel.add(subtitleLabel);
        leftPanel.add(titlePanel);
        
        // Right side - Student info
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        rightPanel.setOpaque(false);
        
        studentNameLabel = new JLabel("👤 " + (currentStudent != null ? currentStudent.getFullName() : "Học sinh"));
        studentNameLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        studentNameLabel.setForeground(Color.WHITE);
        rightPanel.add(studentNameLabel);
        
        JButton logoutBtn = createStyledButton("Đăng xuất", new Color(0xEF4444), new Color(0xDC2626));
        logoutBtn.addActionListener(e -> logout());
        rightPanel.add(logoutBtn);
        
        header.add(leftPanel, BorderLayout.WEST);
        header.add(rightPanel, BorderLayout.EAST);
        
        return header;
    }
    
    private JPanel createClassesPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 15));
        panel.setBackground(new Color(0xF8F9FA));
        panel.setPreferredSize(new Dimension(380, 0));
        
        // Title
        JLabel titleLabel = new JLabel("📚 Các Lớp Học Của Tôi");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(new Color(0x1F2937));
        panel.add(titleLabel, BorderLayout.NORTH);
        
        // Classes container
        classesPanel = new JPanel();
        classesPanel.setLayout(new BoxLayout(classesPanel, BoxLayout.Y_AXIS));
        classesPanel.setBackground(new Color(0xF8F9FA));
        
        JScrollPane scrollPane = new JScrollPane(classesPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createExamsPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 15));
        panel.setBackground(new Color(0xF8F9FA));
        
        // Title
        examsTitle = new JLabel("📝 Chọn một lớp để xem bài kiểm tra");
        examsTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        examsTitle.setForeground(new Color(0x6B7280));
        panel.add(examsTitle, BorderLayout.NORTH);
        
        // Exams container
        examsPanel = new JPanel();
        examsPanel.setLayout(new BoxLayout(examsPanel, BoxLayout.Y_AXIS));
        examsPanel.setBackground(Color.WHITE);
        examsPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        // Empty state
        JLabel emptyLabel = new JLabel("Chọn một lớp học bên trái để xem các bài kiểm tra");
        emptyLabel.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        emptyLabel.setForeground(new Color(0x9CA3AF));
        emptyLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        examsPanel.add(Box.createVerticalGlue());
        examsPanel.add(emptyLabel);
        examsPanel.add(Box.createVerticalGlue());
        
        JScrollPane scrollPane = new JScrollPane(examsPanel);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(0xE5E7EB), 1));
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
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
    
    private void loadStudentClasses() {
        classesPanel.removeAll();

        SwingWorker<List<Map<String, Object>>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Map<String, Object>> doInBackground() {
                try {
                    // Chỉ sử dụng accountId lấy từ token (không dùng email)
                    Integer accountId = getAccountIdFromAuth();
                    System.out.println("DEBUG: resolved accountId = " + accountId);
                    if (accountId == null) {
                        System.err.println("StudentDashboard: accountId missing. Aborting class load.");
                        return Collections.emptyList();
                    }

                    // Lấy dữ liệu lớp bằng JOIN từ student -> classes theo IdAccount
                    List<Map<String, Object>> classes = fetchStudentClassesForAccount(accountId);
                    //System.out.println("DEBUG: fetched classes by join = " + classes);

                    if (classes == null || classes.isEmpty()) {
                        return Collections.emptyList();
                    }

                    // Dedupe bằng Id (giữ thứ tự)
                    LinkedHashMap<Integer, Map<String, Object>> unique = new LinkedHashMap<>();
                    for (Map<String, Object> row : classes) {
                        Integer idKey = null;
                        Object oid = row.getOrDefault("Id", row.getOrDefault("id", row.get("classes.Id")));
                        if (oid instanceof Number) idKey = ((Number) oid).intValue();
                        else if (oid != null) {
                            try { idKey = Integer.parseInt(oid.toString()); } catch (Exception ignored) {}
                        }
                        if (idKey != null && !unique.containsKey(idKey)) {
                            unique.put(idKey, row);
                        }
                    }

                    return new ArrayList<>(unique.values());
                } catch (Exception ex) {
                    ex.printStackTrace();
                    return Collections.emptyList();
                }
            }

            @Override
            protected void done() {
                try {
                    List<Map<String, Object>> classes = get();
                    displayClasses(classes);
                } catch (Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(StudentDashboard.this,
                        "Lỗi khi tải danh sách lớp học!",
                        "Lỗi",
                        JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    // Resolve account id from email (calls API)
    private Integer resolveAccountIdByEmail(String email) {
        if (email == null) return null;
        try {
            Map<String,Object> p = new HashMap<>();
            p.put("action", "get");
            p.put("method", "SELECT");
            p.put("table", "account");
            p.put("columns", List.of("id as AccountId"));
            p.put("where", Map.of("email", email));
            p.put("limit", 1);
            System.out.println("DEBUG: resolveAccountIdByEmail payload=" + p);
            List<Map<String,Object>> resp = apiService.postApiGetList("/autoGet", p);
            System.out.println("DEBUG: resolveAccountIdByEmail resp=" + resp);
            if (resp != null && !resp.isEmpty()) {
                Object v = resp.get(0).get("AccountId");
                if (v == null) v = resp.get(0).get("id");
                if (v instanceof Number) return ((Number)v).intValue();
                if (v != null) return Integer.parseInt(v.toString());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    // Get ClassId list from student table for given account id
    private Set<Integer> fetchStudentClassIdsForAccount(Integer accountId) {
        Set<Integer> out = new HashSet<>();
        if (accountId == null) return out;
        try {
            Map<String,Object> p = new HashMap<>();
            p.put("action", "get");
            p.put("method", "SELECT");
            p.put("table", "student");
            p.put("columns", List.of("ClassId"));
            Map<String,Object> where = new HashMap<>();
            where.put("IdAccount", accountId);
            p.put("where", where);
            p.put("groupBy", List.of("ClassId"));
            System.out.println("DEBUG: fetchStudentClassIdsForAccount payload=" + accountId);
            List<Map<String,Object>> resp = apiService.postApiGetList("/autoGet", p);
            System.out.println("DEBUG: fetchStudentClassIdsForAccount resp=" + resp);
            if (resp != null) {
                for (Map<String,Object> r : resp) {
                    Object c = r.get("ClassId");
                    if (c == null) c = r.get("classid");
                    if (c instanceof Number) out.add(((Number)c).intValue());
                    else if (c != null) {
                        try { out.add(Integer.parseInt(c.toString())); } catch (Exception ignored) {}
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return out;
    }

    // Lấy danh sách classes theo tập Ids từ API
    private List<Map<String, Object>> fetchClassesByIds(Set<Integer> ids) {
        if (ids == null || ids.isEmpty()) return Collections.emptyList();
        try {
            Map<String,Object> p = new HashMap<>();
            p.put("action", "get");
            p.put("method", "SELECT");
            p.put("table", "classes");
            p.put("columns", List.of("Id", "Name", "TeacherName", "StudentCount"));
            Map<String,Object> where = new HashMap<>();
            where.put("Id", ids);
            p.put("where", where);
            System.out.println("DEBUG: fetchClassesByIds payload=" + p);
            List<Map<String,Object>> resp = apiService.postApiGetList("/autoGet", p);
            if (resp == null) return Collections.emptyList();

            // Chuẩn hoá key ClassName nếu cần
            List<Map<String,Object>> out = new ArrayList<>();
            for (Map<String,Object> r : resp) {
                Map<String,Object> m = new HashMap<>(r);
                Object name = r.getOrDefault("Name", r.get("name"));
                if (name != null) m.put("ClassName", name.toString());
                out.add(m);
            }
            return out;
        } catch (Exception ex) {
            ex.printStackTrace();
            return Collections.emptyList();
        }
    }
    
    private void displayClasses(List<Map<String, Object>> classes) {
        classesPanel.removeAll();

        if (classes == null || classes.isEmpty()) {
            JLabel emptyLabel = new JLabel("Không có lớp học nào");
            emptyLabel.setFont(new Font("Segoe UI", Font.PLAIN, 15));
            emptyLabel.setForeground(new Color(0x9CA3AF));
            emptyLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            classesPanel.add(Box.createVerticalGlue());
            classesPanel.add(emptyLabel);
            classesPanel.add(Box.createVerticalGlue());
            classesPanel.revalidate();
            classesPanel.repaint();
            return;
        }

        for (Map<String, Object> classData : classes) {
            String className = String.valueOf(
                classData.getOrDefault("classesName", classData.getOrDefault("classes.Name", classData.getOrDefault("ClassName", "Lớp học")))
            );

            String teacherName = String.valueOf(classData.getOrDefault("TeacherName", classData.getOrDefault("teacher.Name", "Đang cập nhật")));
            Object sc = classData.getOrDefault("StudentCount", 0);
            int studentCount = (sc instanceof Number) ? ((Number) sc).intValue() : 0;

            // Lấy ClassId an toàn
            Integer classId = null;
            Object idObj = classData.getOrDefault("Id", classData.get("classes.Id"));
            if (idObj instanceof Number) classId = ((Number) idObj).intValue();
            else if (idObj != null) {
                try { classId = Integer.parseInt(idObj.toString()); } catch (Exception ignored) {}
            }

            // Gọi createClassCard với classId (sử dụng id để fetch exam)
            JPanel classCard = createClassCard(classId, className, teacherName, studentCount);
            classesPanel.add(classCard);
            classesPanel.add(Box.createVerticalStrut(12));
        }

        classesPanel.revalidate();
        classesPanel.repaint();
    }
    
    // cập nhật: nhận classId để select chính xác
    private Integer currentClassId = null; // THÊM FIELD
    private void selectClass(Integer classId, String className, JPanel selectedCard) {
        this.currentClassId = classId; // LƯU LẠI
        selectedClassName = className;

        // Update all cards appearance
        for (Component comp : classesPanel.getComponents()) {
            if (comp instanceof JPanel) {
                JPanel card = (JPanel) comp;
                card.setBackground(Color.WHITE);
                card.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(0xE5E7EB), 1),
                    new EmptyBorder(15, 15, 15, 15)
                ));
            }
        }

        // Highlight selected card
        selectedCard.setBackground(new Color(0xF0F9FF));
        selectedCard.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0x0EA5E9), 2),
            new EmptyBorder(14, 14, 14, 14)
        ));

        // Load exams for this class by ClassId (important)
        loadClassExams(classId, className);
    }
    
    private void loadClassExams(Integer classId, String className) {
        examsTitle.setText("📝 Bài Kiểm Tra - " + className);
        examsPanel.removeAll();

        // Loading indicator
        JLabel loadingLabel = new JLabel("Đang tải...");
        loadingLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        loadingLabel.setForeground(new Color(0x6B7280));
        loadingLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        examsPanel.add(loadingLabel);
        examsPanel.revalidate();
        examsPanel.repaint();

        SwingWorker<List<Map<String, Object>>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Map<String, Object>> doInBackground() {
                try {
                    // Lấy exams theo ClassId — dùng điều kiện số để tránh nhầm lẫn tên
                    Map<String, Object> params = new HashMap<>();
                    params.put("action", "get");
                    params.put("method", "SELECT");
                    params.put("table", List.of("exams", "classes"));
                    params.put("columns", List.of(
                        "exams.id as ExamId",
                        "exams.ExamName",
                        "exams.NumberQuestion",
                        "exams.Description",
                        "exams.PublishDate",
                        "exams.ExpireDate",
                        "exams.ClassId",
                        "classes.Name as ClassName"
                    ));

                    Map<String, Object> join1 = new HashMap<>();
                    join1.put("type", "INNER");
                    join1.put("on", List.of("exams.ClassId = classes.Id"));
                    params.put("join", List.of(join1));

                    Map<String, Object> conditions = new HashMap<>();
                    // dùng key rõ ràng "exams.ClassId"
                    conditions.put("exams.ClassId", classId);
                    params.put("conditions", conditions);

                    System.out.println("DEBUG: fetchExamsByClassId payload=" + params);
                    List<Map<String, Object>> exams = apiService.postApiGetList("/autoGet", params);
                    if (exams == null) exams = Collections.emptyList();

                    // Chuẩn hoá trường tên và ngày (để processExams / displayExams dùng được)
                    List<Map<String, Object>> normalized = new ArrayList<>();
                    for (Map<String, Object> r : exams) {
                        Map<String, Object> m = new HashMap<>(r);
                        // ensure PublishDate/ExpireDate keys exist
                        if (!m.containsKey("PublishDate") && m.containsKey("PublicDate")) {
                            m.put("PublishDate", m.get("PublicDate"));
                        }
                        if (!m.containsKey("ExpireDate") && m.containsKey("Expire")) {
                            m.put("ExpireDate", m.get("Expire"));
                        }
                        // ensure ExamId key
                        if (!m.containsKey("ExamId")) {
                            Object id = m.getOrDefault("exams.id", m.get("id"));
                            if (id != null) m.put("ExamId", id);
                        }
                        normalized.add(m);
                    }

                    // Lấy kết quả làm bài của học sinh
                    List<Map<String, Object>> results = examService.fetchExamResults(currentStudent != null ? currentStudent.getEmail() : null);

                    // xử lý (processExams sẽ so sánh ExamId)
                    return processExams(normalized, results);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    return Collections.emptyList();
                }
            }

            @Override
            protected void done() {
                try {
                    List<Map<String, Object>> exams = get();
                    System.out.println("✅ Exams loaded: " + exams);
                    displayExams(exams);
                } catch (Exception e) {
                    e.printStackTrace();
                    examsPanel.removeAll();
                    JLabel errorLabel = new JLabel("Lỗi khi tải bài kiểm tra!");
                    errorLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
                    errorLabel.setForeground(new Color(0xDC2626));
                    errorLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
                    examsPanel.add(errorLabel);
                    examsPanel.revalidate();
                    examsPanel.repaint();
                }
            }
        };
        worker.execute();
    }

    private List<Map<String, Object>> processExams(List<Map<String, Object>> exams, List<Map<String, Object>> results) {
        List<Map<String, Object>> processedExams = new ArrayList<>();
        
        if (currentStudent == null) {
            System.err.println("❌ currentStudent is null");
            return processedExams;
        }

        String studentEmail = currentStudent.getEmail();
        
        for (Map<String, Object> exam : exams) {
            try {
                // Lấy PublishDate và ExpireDate
                String publishDate = exam.get("PublicDate") != null ? exam.get("PublicDate").toString() : null;
                String expireDate = exam.get("ExpireDate") != null ? exam.get("ExpireDate").toString() : null;
                
                // Lấy ExamId - an toàn
                Object examIdObj = exam.get("ExamId");
                if (examIdObj == null) {
                    examIdObj = exam.get("exams.id");
                }
                if (examIdObj == null) {
                    examIdObj = exam.get("id");
                }
                
                Integer examId = null;
                if (examIdObj instanceof Number) {
                    examId = ((Number) examIdObj).intValue();
                }
                
                if (examId == null) {
                    System.err.println("⚠️ Skipping exam without valid ExamId: " + exam);
                    continue;
                }

                // Kiểm tra điều kiện hiển thị theo thời gian
                if (isExamVisible(publishDate, expireDate)) {
                    Map<String, Object> processedExam = new HashMap<>(exam);
                    
                    // Kiểm tra xem học sinh đã làm bài chưa
                    final Integer finalExamId = examId;
                    boolean hasResult = results.stream().anyMatch(r -> {
                        Object rExamId = r.get("ExamId");
                        Object rStudentEmail = r.get("StudentEmail");
                        
                        boolean examMatch = false;
                        if (rExamId instanceof Number) {
                            examMatch = finalExamId.equals(((Number) rExamId).intValue());
                        }
                        
                        boolean studentMatch = studentEmail != null && studentEmail.equals(rStudentEmail);
                        
                        return examMatch && studentMatch;
                    });

                    // Xác định action: nếu đã có kết quả => "Xem Chi Tiết"
                    // nếu chưa làm và đã quá hạn => "Hết hạn", nếu chưa làm và còn hạn => "Làm Kiểm Tra"
                    String action = computeExamAction(publishDate, expireDate, hasResult);
                    processedExam.put("Action", action);
                    processedExams.add(processedExam);
                }
            } catch (Exception e) {
                System.err.println("❌ Error processing exam: " + e.getMessage());
                e.printStackTrace();
            }
        }
        
        System.out.println("✅ Processed " + processedExams.size() + " exams");
        return processedExams;
    }
    
    // Trả về Action cho exam: nếu hasResult -> Xem Chi Tiết
    // nếu chưa làm and expireDate đã qua -> Hết hạn, ngược lại -> Làm Kiểm Tra
    private String computeExamAction(String publishDate, String expireDate, boolean hasResult) {
        if (hasResult) return "Xem Chi Tiết";
        if (expireDate != null && !expireDate.isEmpty()) {
            Date now = new Date();
            Date exp = parseDate(expireDate);
            if (exp != null && now.after(exp)) {
                return "Hết hạn";
            }
        }
        return "Làm Kiểm Tra";
    }
    
    private boolean isExamVisible(String publicDate, String expireDate) {
        // Kiểm tra thời gian hiện tại so với PublicDate và ExpireDate
        Date now = new Date();
        return (publicDate == null || now.after(parseDate(publicDate))) && (expireDate == null || now.before(parseDate(expireDate)));
    }

    private Date parseDate(String dateStr) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            return sdf.parse(dateStr);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    private void displayExams(List<Map<String, Object>> exams) {
        examsPanel.removeAll();
        examsPanel.setLayout(new BoxLayout(examsPanel, BoxLayout.Y_AXIS));
        
        if (exams.isEmpty()) {
            JLabel emptyLabel = new JLabel("Chưa có bài kiểm tra nào");
            emptyLabel.setFont(new Font("Segoe UI", Font.PLAIN, 15));
            emptyLabel.setForeground(new Color(0x9CA3AF));
            emptyLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            examsPanel.add(Box.createVerticalGlue());
            examsPanel.add(emptyLabel);
            examsPanel.add(Box.createVerticalGlue());
        } else {
            for (Map<String, Object> exam : exams) {
                try {
                    // Lấy ExamId
                    Object examIdObj = exam.get("ExamId");
                    if (examIdObj == null) examIdObj = exam.get("exams.id");
                    if (examIdObj == null) examIdObj = exam.get("id");
                    
                    int examId = 0;
                    if (examIdObj instanceof Number) {
                        examId = ((Number) examIdObj).intValue();
                    }
                    
                    // Lấy ExamName
                    String examName = "Bài kiểm tra";
                    Object examNameObj = exam.get("ExamName");
                    if (examNameObj == null) examNameObj = exam.get("exams.ExamName");
                    if (examNameObj != null) examName = examNameObj.toString();
                    
                    // Lấy PublishDate - format đẹp hơn
                    String publishDate = "Chưa công bố";
                    Object publishDateObj = exam.get("PublishDate");
                    if (publishDateObj == null) publishDateObj = exam.get("exams.PublishDate");
                    if (publishDateObj != null) {
                        publishDate = formatDate(publishDateObj.toString());
                    }
                    
                    // Lấy ExpireDate - format đẹp hơn
                    String expireDate = "Không giới hạn";
                    Object expireDateObj = exam.get("ExpireDate");
                    if (expireDateObj == null) expireDateObj = exam.get("exams.ExpireDate");
                    if (expireDateObj != null) {
                        expireDate = formatDate(expireDateObj.toString());
                    }
                    
                    // Lấy Action
                    String action = exam.getOrDefault("Action", "Làm Kiểm Tra").toString();
                    
                    JPanel examCard = createExamCard(examId, examName, publishDate, expireDate, action);
                    examsPanel.add(examCard);
                    examsPanel.add(Box.createVerticalStrut(12));
                } catch (Exception e) {
                    System.err.println("❌ Error displaying exam: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }
        
        examsPanel.revalidate();
        examsPanel.repaint();
    }
    
    private JPanel createExamCard(int examId, String examName, String publishDate, String expireDate, String action) {
        JPanel card = new JPanel(new BorderLayout(15, 0));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0xE5E7EB), 1),
            new EmptyBorder(18, 20, 18, 20)
        ));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));
        
        // Left - Exam info
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setOpaque(false);
        
        JLabel nameLabel = new JLabel(examName);
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        nameLabel.setForeground(new Color(0x1F2937));
        nameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel publishLabel = new JLabel("📅 Công bố: " + publishDate);
        publishLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        publishLabel.setForeground(new Color(0x6B7280));
        publishLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel expireLabel = new JLabel("⏰ Hết hạn: " + expireDate);
        expireLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        expireLabel.setForeground(new Color(0x9CA3AF));
        expireLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        infoPanel.add(nameLabel);
        infoPanel.add(Box.createVerticalStrut(6));
        infoPanel.add(publishLabel);
        infoPanel.add(Box.createVerticalStrut(4));
        infoPanel.add(expireLabel);
        
        card.add(infoPanel, BorderLayout.WEST);
        
        // Right - Action button
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        rightPanel.setOpaque(false);
        
        JButton actionButton;
        if ("Xem Chi Tiết".equals(action)) {
            actionButton = new JButton("Xem Chi Tiết");
            actionButton.setBackground(new Color(0x0EA5E9));
        } else {
            actionButton = new JButton("Làm Kiểm Tra");
            actionButton.setBackground(new Color(0x10B981));
        }
        
        actionButton.setFont(new Font("Segoe UI", Font.BOLD, 13));
        actionButton.setForeground(Color.WHITE);
        actionButton.setBorderPainted(false);
        actionButton.setFocusPainted(false);
        actionButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        actionButton.setPreferredSize(new Dimension(150, 38));
        
        Color originalColor = actionButton.getBackground();
        actionButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                if ("Xem Chi Tiết".equals(action)) {
                    actionButton.setBackground(new Color(0x0284C7));
                } else {
                    actionButton.setBackground(new Color(0x059669));
                }
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                actionButton.setBackground(originalColor);
            }
        });
        
        actionButton.addActionListener(e -> {
            if ("Xem Chi Tiết".equals(action)) {
                // Mở cửa sổ xem chi tiết: lấy studentId và score, rồi mở ExamDetailWindow
                openExamDetailForStudent(examId);
            } else {
                // ✅ Kiểm tra thời gian trước khi vào thi
                if (!isExamAvailable(publishDate, expireDate)) {
                    JOptionPane.showMessageDialog(StudentDashboard.this,
                        "Bài kiểm tra chưa được công bố hoặc đã hết hạn!",
                        "Không thể làm bài",
                        JOptionPane.WARNING_MESSAGE);
                    return;
                }
                
                // ✅ Chuyển đến QuizAppSwing với đầy đủ thông tin
                startExamWithFullInfo(examId);
            }
        });
        
        rightPanel.add(actionButton);
        
        card.add(rightPanel, BorderLayout.EAST);
        
        return card;
    }
    
    // ✅ Method mới: Kiểm tra thời gian hợp lệ
    private boolean isExamAvailable(String publishDate, String expireDate) {
        Date now = new Date();
        
        // Kiểm tra PublishDate
        if (publishDate != null && !publishDate.isEmpty()) {
            Date pubDate = parseDate(publishDate);
            if (pubDate != null && now.before(pubDate)) {
                System.out.println("❌ Exam not yet published");
                return false;
            }
        }
        
        // Kiểm tra ExpireDate
        if (expireDate != null && !expireDate.isEmpty()) {
            Date expDate = parseDate(expireDate);
            if (expDate != null && now.after(expDate)) {
                System.out.println("❌ Exam expired");
                return false;
            }
        }
        
        return true;
    }

    // ✅ Method mới: Bắt đầu thi với đầy đủ thông tin
    private void startExamWithFullInfo(int examId) {
        // Lấy thông tin exam đầy đủ từ API
        SwingWorker<Map<String, Object>, Void> worker = new SwingWorker<>() {
            @Override
            protected Map<String, Object> doInBackground() {
                try {
                    Map<String, Object> params = new HashMap<>();
                    params.put("action", "get");
                    params.put("method", "SELECT");
                    params.put("table", List.of("exams"));
                    params.put("columns", List.of("id", "ClassId", "NumberQuestion", "PublishDate", "ExpireDate"));
                    
                    Map<String, Object> where = new HashMap<>();
                    where.put("id", examId);
                    params.put("where", where);
                    
                    List<Map<String, Object>> result = apiService.postApiGetList("/autoGet", params);
                    
                    if (result != null && !result.isEmpty()) {
                        return result.get(0);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                return null;
            }

            @Override
            protected void done() {
                try {
                    Map<String, Object> examInfo = get();
                    
                    if (examInfo == null) {
                        JOptionPane.showMessageDialog(StudentDashboard.this,
                            "Không tìm thấy thông tin bài kiểm tra!",
                            "Lỗi",
                            JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    
                    // Lấy ClassId và NumberQuestion
                    Object classIdObj = examInfo.get("ClassId");
                    Object numberQuestionObj = examInfo.get("NumberQuestion");
                    
                    int classId = (classIdObj instanceof Number) ? ((Number) classIdObj).intValue() : 0;
                    int numberQuestion = (numberQuestionObj instanceof Number) ? ((Number) numberQuestionObj).intValue() : 0;
                    
                    System.out.println("🎯 Starting exam:");
                    System.out.println("   ExamId: " + examId);
                    System.out.println("   ClassId: " + classId);
                    System.out.println("   NumberQuestion: " + numberQuestion);
                    
                    StudentDashboard.this.setVisible(false);  // Ẩn thay vì dispose()
                    // ✅ Mở QuizAppSwing với đầy đủ thông tin
                    new QuizAppSwing(apiService, authService, examId, classId, numberQuestion, StudentDashboard.this);
                    //dispose(); // Đóng dashboard
                    
                    
                    
                } catch (Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(StudentDashboard.this,
                        "Lỗi khi tải thông tin bài kiểm tra!",
                        "Lỗi",
                        JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }
    
    // private void openExamDetail(int examId, String studentName, String examName) {
    //     new ExamDetailWindow(apiService, authService, examId, currentStudent.getEmail(), 
    //                        studentName, examName, 0);
    // }
    
    private void startExam(int examId) {
        // TODO: Implement start exam functionality
        JOptionPane.showMessageDialog(this,
            "Chức năng làm bài kiểm tra chưa được triển khai.",
            "Thông báo", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
 * Gọi từ QuizAppSwing sau khi nộp bài để reload danh sách exams
 */
public void refreshCurrentClassExams() {
    if (currentClassId == null || selectedClassName == null) {
        System.out.println("No class selected");
        return;
    }
    System.out.println("Refreshing exams for class ID: " + currentClassId);
    loadClassExams(currentClassId, selectedClassName);
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
    
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        SwingUtilities.invokeLater(() -> {
            // Mock student data
            Task student = new Task();
            student.setFullName("Nguyễn Văn B");
            student.setEmail("student@example.com");
            
            new StudentDashboard(null, null, student, null, null);
        });
    }

    // ✅ Helper method: Format date đẹp hơn
    private String formatDate(String dateStr) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
            Date date = inputFormat.parse(dateStr);
            return outputFormat.format(date);
        } catch (ParseException e) {
            return dateStr;
        }
    }

    // Mở cửa sổ xem chi tiết: lấy studentId và score, rồi mở ExamDetailWindow
    private void openExamDetailForStudent(int examId) {
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            private Integer studentId = null;
            private Double score = 0.0;
            private String examName = "Bài kiểm tra";

            @Override
            protected Void doInBackground() {
                try {
                    StudentInfoService sis = new StudentInfoService(apiService);
                    String email = currentStudent != null ? currentStudent.getEmail() : null;
                    if (email == null || email.isEmpty()) return null;

                    // Lấy student profile -> tìm Student.Id
                    List<Map<String, Object>> profile = sis.fetchProfileByEmail(email);
                    if (profile != null && !profile.isEmpty()) {
                        Object sid = profile.get(0).getOrDefault("StudentId", profile.get(0).get("Id"));
                        if (sid instanceof Number) studentId = ((Number) sid).intValue();
                        else if (sid != null) studentId = Integer.parseInt(sid.toString());
                    }

                    // Lấy exam name & score (nếu có) từ exam_results
                    Map<String, Object> p = new HashMap<>();
                    p.put("action", "get");
                    p.put("method", "SELECT");
                    p.put("table", "exams");
                    p.put("columns", List.of("ExamName"));
                    Map<String, Object> w = new HashMap<>();
                    w.put("id", examId);
                    p.put("where", w);
                    List<Map<String, Object>> er = apiService.postApiGetList("/autoGet", p);
                    if (er != null && !er.isEmpty()) {
                        Object en = er.get(0).get("ExamName");
                        if (en != null) examName = en.toString();
                    }

                    if (studentId != null) {
                        Map<String, Object> q = new HashMap<>();
                        q.put("action", "get");
                        q.put("method", "SELECT");
                        q.put("table", "exam_results");
                        q.put("columns", List.of("Score"));
                        Map<String, Object> wh = new HashMap<>();
                        wh.put("ExamId", examId);
                        wh.put("StudentId", studentId);
                        q.put("where", wh);
                        List<Map<String, Object>> res = apiService.postApiGetList("/autoGet", q);
                        if (res != null && !res.isEmpty()) {
                            Object sc = res.get(0).get("Score");
                            if (sc instanceof Number) score = ((Number) sc).doubleValue();
                            else if (sc != null) score = Double.parseDouble(sc.toString());
                        }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                return null;
            }

            @Override
            protected void done() {
                if (studentId == null) {
                    JOptionPane.showMessageDialog(StudentDashboard.this,
                        "Không xác định được StudentId để xem chi tiết.",
                        "Lỗi", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                // Mở cửa sổ chi tiết bài thi (ExamDetailWindow expects studentId and score)
                new ExamDetailWindow(apiService, authService, examId, studentId, currentStudent.getFullName(), examName, score);
            }
        };
        worker.execute();
    }
    
    // Thử lấy account id trực tiếp từ authService bằng hàm đã có trong AuthService
    private Integer getAccountIdFromAuth() {
        if (authService == null) return null;
        try {
            String token = authService.getAccessToken();
            if (token == null || token.isEmpty()) return null;
            int id = authService.getUserIdFromToken(token);
            if (id <= 0) return null;
            return Integer.valueOf(id);
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    // New: fetch classes for an account using JOIN student -> classes
    private List<Map<String, Object>> fetchStudentClassesForAccount(Integer accountId) {
        if (accountId == null) return Collections.emptyList();
        try {
            Map<String, Object> p = new HashMap<>();
            p.put("action", "get");
            p.put("method", "SELECT");
            // gửi table như mảng để API xây JOIN
            p.put("table", List.of("student", "classes", "teacher"));
            // lấy thông tin lớp cần hiển thị
            p.put("columns", List.of("classes.Id", "classes.Name as classesName", "teacher.Name as TeacherName"));
            // cấu trúc join phù hợp với ModelSQL.autoQuery
            Map<String, Object> join1 = new HashMap<>();
            join1.put("type", "INNER");
            join1.put("on", List.of("student.ClassId = classes.Id"));

            Map<String, Object> join2 = new HashMap<>();
            join2.put("type", "Left");
            join2.put("on", List.of("classes.Id = teacher.ClassId"));

            p.put("join", List.of(join1, join2));

            Map<String, Object> conditions = new HashMap<>();
            conditions.put("student.IdAccount", accountId);
            p.put("conditions", conditions);
            

            System.out.println("DEBUG: fetchStudentClassesForAccount payload=" + p);
            List<Map<String, Object>> resp = apiService.postApiGetList("/autoGet", p);
            System.out.println("DEBUG: fetchStudentClassesForAccount resp=" + resp);
            if (resp == null) return Collections.emptyList();

            // Chuẩn hoá key: đảm bảo có ClassName/Id/TeacherName
            List<Map<String, Object>> out = new ArrayList<>();
            for (Map<String, Object> r : resp) {
                Map<String, Object> m = new HashMap<>(r);
                Object name = r.getOrDefault("Name", r.get("classes.Name"));
                if (name != null) m.put("ClassName", name.toString());
                // normalize Id key
                if (!m.containsKey("Id")) {
                    Object cid = r.getOrDefault("classes.Id", r.get("ClassId"));
                    if (cid != null) m.put("Id", cid);
                }
                out.add(m);
            }
            return out;
        } catch (Exception ex) {
            ex.printStackTrace();
            return Collections.emptyList();
        }
    }
    
    // Tạo card hiển thị lớp (đã bao gồm classId) — gọi selectClass khi click
    private JPanel createClassCard(Integer classId, String className, String teacherName, int studentCount) {
        JPanel card = new JPanel(new BorderLayout(12, 0));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0xE5E7EB), 1),
            new EmptyBorder(15, 15, 15, 15)
        ));
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));

        // Icon
        JLabel iconLabel = new JLabel("📖");
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 32));
        card.add(iconLabel, BorderLayout.WEST);

        // Info
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setOpaque(false);

        JLabel nameLabel = new JLabel(className != null ? className : "Lớp học");
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));
        nameLabel.setForeground(new Color(0x1F2937));

        JLabel teacherLabel = new JLabel("👨‍🏫 " + (teacherName != null ? teacherName : "Đang cập nhật"));
        teacherLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        teacherLabel.setForeground(new Color(0x6B7280));

        JLabel studentLabel = new JLabel("👥 " + studentCount + " học sinh");
        studentLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        studentLabel.setForeground(new Color(0x9CA3AF));

        infoPanel.add(nameLabel);
        infoPanel.add(Box.createVerticalStrut(4));
        infoPanel.add(teacherLabel);
        infoPanel.add(Box.createVerticalStrut(2));
        infoPanel.add(studentLabel);

        card.add(infoPanel, BorderLayout.CENTER);

        // Hover/Click: truyền classId để load exam chính xác
        card.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                card.setBackground(new Color(0xF0F9FF));
                card.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(0x0EA5E9), 2),
                    new EmptyBorder(14, 14, 14, 14)
                ));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                if (!className.equals(selectedClassName)) {
                    card.setBackground(Color.WHITE);
                    card.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(0xE5E7EB), 1),
                        new EmptyBorder(15, 15, 15, 15)
                    ));
                }
            }
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                // Nếu classId null vẫn cho phép chọn theo tên (fallback)
                selectClass(classId, className, card);
            }
        });

        return card;
    }
}