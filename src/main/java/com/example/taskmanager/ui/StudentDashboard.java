package com.example.taskmanager.ui;

import java.awt.*;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

import com.example.taskmanager.model.Task;
import com.example.taskmanager.service.ApiService;
import com.example.taskmanager.service.AuthService;
import com.formdev.flatlaf.FlatLightLaf;

public class StudentDashboard extends JFrame {
    private ApiService apiService;
    private AuthService authService;
    private Task currentStudent;
    
    private JLabel studentNameLabel;
    private JPanel classesPanel;
    private JPanel examsPanel;
    private JLabel examsTitle;
    private String selectedClassName = null;
    
    public StudentDashboard(ApiService apiService, AuthService authService, Task student) {
        this.apiService = apiService;
        this.authService = authService;
        this.currentStudent = student;
        
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
                // TODO: Call API to get student's classes
                // For now using mock data
                List<Map<String, Object>> classes = new ArrayList<>();
                
                Map<String, Object> class1 = new HashMap<>();
                class1.put("ClassName", "Lớp 10A1 - Toán");
                class1.put("TeacherName", "Nguyễn Văn A");
                class1.put("StudentCount", 35);
                classes.add(class1);
                
                Map<String, Object> class2 = new HashMap<>();
                class2.put("ClassName", "Lớp 10A1 - Lý");
                class2.put("TeacherName", "Trần Thị B");
                class2.put("StudentCount", 33);
                classes.add(class2);
                
                Map<String, Object> class3 = new HashMap<>();
                class3.put("ClassName", "Lớp 10A1 - Hóa");
                class3.put("TeacherName", "Lê Văn C");
                class3.put("StudentCount", 32);
                classes.add(class3);
                
                return classes;
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
    
    private void displayClasses(List<Map<String, Object>> classes) {
        classesPanel.removeAll();
        
        for (Map<String, Object> classData : classes) {
            String className = classData.get("ClassName").toString();
            String teacherName = classData.get("TeacherName").toString();
            int studentCount = (int) classData.get("StudentCount");
            
            JPanel classCard = createClassCard(className, teacherName, studentCount);
            classesPanel.add(classCard);
            classesPanel.add(Box.createVerticalStrut(12));
        }
        
        classesPanel.revalidate();
        classesPanel.repaint();
    }
    
    private JPanel createClassCard(String className, String teacherName, int studentCount) {
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
        
        JLabel nameLabel = new JLabel(className);
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));
        nameLabel.setForeground(new Color(0x1F2937));
        
        JLabel teacherLabel = new JLabel("👨‍🏫 " + teacherName);
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
        
        // Hover effect
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
                selectClass(className, card);
            }
        });
        
        return card;
    }
    
    private void selectClass(String className, JPanel selectedCard) {
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
        
        // Load exams for this class
        loadClassExams(className);
    }
    
    private void loadClassExams(String className) {
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
                // TODO: Call API to get exams for this class
                // For now using mock data
                List<Map<String, Object>> exams = new ArrayList<>();
                
                Map<String, Object> exam1 = new HashMap<>();
                exam1.put("ExamId", 1);
                exam1.put("ExamName", "Kiểm tra giữa kỳ");
                exam1.put("SubmittedDate", "2024-01-15");
                exam1.put("Score", 8.5);
                exam1.put("StudentId", 1);
                exams.add(exam1);
                
                Map<String, Object> exam2 = new HashMap<>();
                exam2.put("ExamId", 2);
                exam2.put("ExamName", "Kiểm tra 15 phút - Chương 1");
                exam2.put("SubmittedDate", "2024-01-10");
                exam2.put("Score", 9.0);
                exam2.put("StudentId", 1);
                exams.add(exam2);
                
                Map<String, Object> exam3 = new HashMap<>();
                exam3.put("ExamId", 3);
                exam3.put("ExamName", "Kiểm tra cuối kỳ");
                exam3.put("SubmittedDate", "2024-01-20");
                exam3.put("Score", 7.5);
                exam3.put("StudentId", 1);
                exams.add(exam3);
                
                return exams;
            }
            
            @Override
            protected void done() {
                try {
                    List<Map<String, Object>> exams = get();
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
                int examId = (int) exam.get("ExamId");
                int studentId = (int) exam.get("StudentId");
                String examName = exam.get("ExamName").toString();
                String submittedDate = exam.get("SubmittedDate").toString();
                double score = Double.parseDouble(exam.get("Score").toString());
                
                JPanel examCard = createExamCard(examId, studentId, examName, submittedDate, score);
                examsPanel.add(examCard);
                examsPanel.add(Box.createVerticalStrut(12));
            }
        }
        
        examsPanel.revalidate();
        examsPanel.repaint();
    }
    
    private JPanel createExamCard(int examId, int studentId, String examName, 
                                  String submittedDate, double score) {
        JPanel card = new JPanel(new BorderLayout(15, 0));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0xE5E7EB), 1),
            new EmptyBorder(18, 20, 18, 20)
        ));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
        
        // Left - Exam info
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setOpaque(false);
        
        JLabel nameLabel = new JLabel(examName);
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        nameLabel.setForeground(new Color(0x1F2937));
        nameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel dateLabel = new JLabel("📅 Ngày nộp: " + submittedDate);
        dateLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        dateLabel.setForeground(new Color(0x6B7280));
        dateLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        infoPanel.add(nameLabel);
        infoPanel.add(Box.createVerticalStrut(6));
        infoPanel.add(dateLabel);
        
        card.add(infoPanel, BorderLayout.WEST);
        
        // Right - Score and button
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        rightPanel.setOpaque(false);
        
        // Score
        JLabel scoreLabel = new JLabel(String.format("%.1f", score));
        scoreLabel.setFont(new Font("Segoe UI", Font.BOLD, 32));
        scoreLabel.setForeground(getScoreColor(score));
        rightPanel.add(scoreLabel);
        
        JLabel scoreMaxLabel = new JLabel("/10");
        scoreMaxLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        scoreMaxLabel.setForeground(new Color(0x9CA3AF));
        rightPanel.add(scoreMaxLabel);
        
        // View detail button
        JButton detailBtn = new JButton("👁️ Xem Chi Tiết");
        detailBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        detailBtn.setForeground(Color.WHITE);
        detailBtn.setBackground(new Color(0x0EA5E9));
        detailBtn.setBorderPainted(false);
        detailBtn.setFocusPainted(false);
        detailBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        detailBtn.setPreferredSize(new Dimension(140, 38));
        
        detailBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                detailBtn.setBackground(new Color(0x0284C7));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                detailBtn.setBackground(new Color(0x0EA5E9));
            }
        });
        
        detailBtn.addActionListener(e -> openExamDetail(
            examId, studentId, currentStudent.getFullName(), examName, score
        ));
        
        rightPanel.add(detailBtn);
        
        card.add(rightPanel, BorderLayout.EAST);
        
        return card;
    }
    
    private Color getScoreColor(double score) {
        if (score >= 9.0) return new Color(0x059669);
        if (score >= 8.0) return new Color(0x0EA5E9);
        if (score >= 6.5) return new Color(0xF59E0B);
        return new Color(0xEF4444);
    }
    
    private void openExamDetail(int examId, int studentId, String studentName, 
                                String examName, double score) {
        new ExamDetailWindow(apiService, authService, examId, studentId, 
                           studentName, examName, score);
    }
    
    private void logout() {
        int confirm = JOptionPane.showConfirmDialog(this,
            "Bạn có chắc muốn đăng xuất?",
            "Xác nhận",
            JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            authService.logout();
            dispose();
            // TODO: Open login window
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
            
            new StudentDashboard(null, null, student);
        });
    }
}