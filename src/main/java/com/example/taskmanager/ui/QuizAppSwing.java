/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.example.taskmanager.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.UIManager;
import static javax.swing.WindowConstants.EXIT_ON_CLOSE;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import com.example.taskmanager.service.ApiService;
import com.example.taskmanager.service.AuthService;
import com.example.taskmanager.service.StudentInfoService;
import com.formdev.flatlaf.FlatLightLaf;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.Date;

import com.example.taskmanager.ui.StudentDashboard;

/**
 *
 * @author PC
 */
public class QuizAppSwing extends JFrame {
    private JPanel questionPanel;
    private JPanel navPanel;
    private JLabel timerLabel;
    private JButton submitButton;
    private Map<Integer, Integer> selectedAnswers = new HashMap<>();
    private int currentPage = 1;
    private int perPage = 10;
    private int totalQuestions;
    private Timer timer;
    private int duration = 15 * 60;
    private List<Question> questions = new ArrayList<>();
    private ApiService apiService;
    private AuthService authService;
    private StudentInfoService studentInfoService;
    private int examId = -1;
    private int classId = -1;
    private int numberQuestion = 0;
    private int studentId = -1; // ✅ Thêm field để lưu StudentId
    private String studentEmail = null; // ✅ Thêm field để lưu email

    // ------------------------- Question Class -------------------------
    private static class Question {
        int id;
        String questionText;
        List<String> options;
        List<Integer> answerIds;

        Question(int id, String questionText, List<String> options, List<Integer> answerIds) {
            this.id = id;
            this.questionText = questionText;
            this.options = options;
            this.answerIds = answerIds;
        }
    }

    // ------------------------- Constructor -------------------------
    public QuizAppSwing(ApiService apiService, AuthService authService, MainWindow mainWindow) {
        this(apiService, authService, -1, -1, 0);
    }

    public QuizAppSwing(ApiService apiService, AuthService authService, int examId, int classId, int numberQuestion) {
        this.apiService = apiService;
        this.authService = authService;
        this.studentInfoService = new StudentInfoService(apiService);
        this.examId = examId;
        this.classId = classId;
        this.numberQuestion = numberQuestion;
        
        // ✅ Lấy email từ authService
        try {
            this.studentEmail = (String) authService.getClass().getMethod("getUserEmail").invoke(authService);
            System.out.println("📧 Student email: " + studentEmail);
        } catch (Exception e) {
            System.err.println("⚠️ Cannot get user email from authService: " + e.getMessage());
        }
        
        // ✅ Lấy StudentId từ email
        if (studentEmail != null && !studentEmail.isEmpty()) {
            this.studentId = getStudentIdByEmail(studentEmail);
            System.out.println("👤 Student ID: " + studentId);
            
            if (studentId <= 0) {
                JOptionPane.showMessageDialog(null,
                    "⚠️ Không tìm thấy thông tin học sinh!\nEmail: " + studentEmail,
                    "Cảnh báo",
                    JOptionPane.WARNING_MESSAGE);
            }
        }
        
        System.out.println("🎯 QuizAppSwing initialized:");
        System.out.println("   ExamId: " + examId);
        System.out.println("   ClassId: " + classId);
        System.out.println("   NumberQuestion: " + numberQuestion);
        System.out.println("   StudentId: " + studentId);
        System.out.println("   Email: " + studentEmail);

        setTitle("Bài kiểm tra trắc nghiệm");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(new Color(245, 245, 245));
        setAlwaysOnTop(true);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowIconified(WindowEvent e) {
                setState(JFrame.NORMAL);
                toFront();
                JOptionPane.showMessageDialog(QuizAppSwing.this,
                        "Không được thu nhỏ bài kiểm tra!",
                        "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            }
        });

        // --------- LEFT: Thông tin người làm bài ---------
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setBackground(Color.WHITE);
        infoPanel.setPreferredSize(new Dimension(180, getHeight()));
        infoPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        infoPanel.add(new JLabel("Thông tin người làm bài:", SwingConstants.CENTER));
        infoPanel.add(Box.createVerticalStrut(10));
        
        // ✅ Sử dụng studentEmail đã lấy ở trên
        List<Map<String, Object>> studentExamData = studentInfoService.fetchProfileByEmail(studentEmail);
        System.out.println("Loading student exam data..." + studentExamData);
        
        infoPanel.add(new JLabel("Họ và tên: " + studentExamData.stream()
                .map(m -> m.get("FullName"))
                .filter(Objects::nonNull)
                .map(Object::toString)
                .findFirst().orElse("N/A")));
        infoPanel.add(new JLabel("Lớp: " + studentExamData.stream()
                .map(m -> m.get("ClassName"))
                .filter(Objects::nonNull)
                .map(Object::toString)
                .findFirst().orElse("N/A")));
        infoPanel.add(new JLabel("Môn: " + studentExamData.stream()
                .map(m -> m.get("ExamName"))
                .filter(Objects::nonNull)
                .map(Object::toString)
                .findFirst().orElse("N/A")));
        infoPanel.add(new JLabel("Ngày tháng: "));
        infoPanel.add(new JLabel("Thời gian: "));
        infoPanel.add(Box.createVerticalGlue());
        add(infoPanel, BorderLayout.WEST);

        // --------- CENTER: Câu hỏi ---------
        questionPanel = new JPanel();
        questionPanel.setLayout(new BoxLayout(questionPanel, BoxLayout.Y_AXIS));
        questionPanel.setBackground(Color.WHITE);
        questionPanel.setBorder(new EmptyBorder(20, 5, 20, 20));
        JScrollPane scrollPane = new JScrollPane(questionPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        add(scrollPane, BorderLayout.CENTER);

        // --------- RIGHT: Sidebar ---------
        JPanel sidebar = new JPanel(new BorderLayout(10, 10));
        sidebar.setPreferredSize(new Dimension(280, getHeight()));
        sidebar.setBackground(new Color(240, 248, 255));
        sidebar.setBorder(new EmptyBorder(20, 10, 20, 10));

        JPanel timerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        timerPanel.setBackground(new Color(240, 248, 255));
        JLabel timeLabel = new JLabel("⏰ Thời gian còn lại:");
        timeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        timeLabel.setForeground(Color.BLUE);
        timerPanel.add(timeLabel);
        
        timerLabel = new JLabel("15:00");
        timerLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        timerLabel.setForeground(Color.RED);
        timerPanel.add(timerLabel);
        sidebar.add(timerPanel, BorderLayout.NORTH);

        JPanel navSection = new JPanel(new FlowLayout(FlowLayout.LEFT));
        navSection.setBackground(new Color(240, 248, 255));
        JLabel navLabel = new JLabel("📋 Danh sách câu hỏi:");
        navLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        navLabel.setForeground(Color.BLUE);
        navSection.add(navLabel);
        sidebar.add(navSection, BorderLayout.CENTER);

        navPanel = new JPanel(new GridLayout(0, 5, 8, 8));
        navPanel.setBackground(new Color(240, 248, 255));
        sidebar.add(new JScrollPane(navPanel), BorderLayout.CENTER);

        submitButton = new JButton("Nộp bài");
        submitButton.setBackground(new Color(220, 50, 50));
        submitButton.setForeground(Color.WHITE);
        submitButton.setFont(new Font("Segoe UI", Font.BOLD, 16));
        submitButton.setFocusPainted(false);
        submitButton.setPreferredSize(new Dimension(200, 40));
        submitButton.addActionListener(e -> submitExam());
        sidebar.add(submitButton, BorderLayout.SOUTH);

        add(sidebar, BorderLayout.EAST);

        startTimer();
        loadQuestionsAndAnswersFromAPI();
        setVisible(true);
    }

    // ------------------------- Render UI -------------------------
    private void renderQuestions() {
        questionPanel.removeAll();

        if (questions.isEmpty()) {
            questionPanel.add(new JLabel("Không có dữ liệu câu hỏi!"));
            questionPanel.revalidate();
            questionPanel.repaint();
            return;
        }

        int start = (currentPage - 1) * perPage;
        int end = Math.min(start + perPage, totalQuestions);

        for (int i = start; i < end; i++) {
            Question q = questions.get(i);
            JPanel qBox = new JPanel();
            qBox.setLayout(new BoxLayout(qBox, BoxLayout.Y_AXIS));
            qBox.setBackground(Color.WHITE);
            qBox.setBorder(BorderFactory.createTitledBorder(
                    new LineBorder(Color.GRAY, 1),
                    "Câu hỏi " + (i + 1),
                    0, 0, new Font("Segoe UI", Font.BOLD, 14))
            );

            JLabel qLabel = new JLabel(q.questionText);
            qLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
            qBox.add(qLabel);
            qBox.add(Box.createVerticalStrut(5));

            ButtonGroup group = new ButtonGroup();

            for (int j = 0; j < q.options.size(); j++) {
                String optText = q.options.get(j);
                int answerId = q.answerIds.get(j);
                JRadioButton option = new JRadioButton(optText);
                option.setFont(new Font("Segoe UI", Font.PLAIN, 13));
                option.setBackground(Color.WHITE);
                if (selectedAnswers.get(q.id) != null && selectedAnswers.get(q.id) == answerId) {
                    option.setSelected(true);
                }
                option.addActionListener(e -> {
                    selectedAnswers.put(q.id, answerId);
                    refreshNavPanel();
                    saveAnswerToApi(q.id, answerId);
                });
                group.add(option);
                qBox.add(option);
            }

            questionPanel.add(qBox);
            questionPanel.add(Box.createVerticalStrut(15));
        }

        questionPanel.revalidate();
        questionPanel.repaint();
    }

    private void refreshNavPanel() {
        Component[] comps = navPanel.getComponents();
        for (Component c : comps) {
            if (c instanceof JButton btn) {
                int qIndex = Integer.parseInt(btn.getText()) - 1;
                if (qIndex >= 0 && qIndex < questions.size()) {
                    int qid = questions.get(qIndex).id;
                    if (selectedAnswers.containsKey(qid)) {
                        btn.setBackground(new Color(50, 150, 50));
                        btn.setForeground(Color.WHITE);
                    } else {
                        btn.setBackground(new Color(220, 220, 220));
                        btn.setForeground(Color.BLACK);
                    }
                }
            }
        }
        navPanel.repaint();
    }

    // ------------------------- Lưu đáp án mỗi khi chọn -------------------------
    private void saveAnswerToApi(int questionId, int answerId) {
        // ✅ Kiểm tra StudentId hợp lệ
        if (studentId <= 0) {
            System.err.println("❌ Cannot save answer: Invalid StudentId = " + studentId);
            return;
        }
        
        if (examId <= 0) {
            System.err.println("❌ Cannot save answer: Invalid ExamId = " + examId);
            return;
        }

        System.out.println("💾 Saving answer: ExamId=" + examId + ", StudentId=" + studentId + 
                          ", QuestionId=" + questionId + ", AnswerId=" + answerId);

        try {
            // ✅ Lấy IsCorrect từ bảng answers
            Integer isCorrect = getIsCorrectFromAnswer(questionId, answerId);
            
            Map<String, Object> record = new HashMap<>();
            record.put("StudentId", studentId);
            record.put("QuestionId", questionId);
            record.put("AnswerId", answerId);
            record.put("IsCorrect", isCorrect != null ? isCorrect : 0); // ✅ Thêm IsCorrect

            Map<String, Object> params = new HashMap<>();
            params.put("action", "update");
            params.put("method", "UPSERT");
            params.put("table", "exam_answers");
            params.put("data", List.of(record));

            System.out.println("📤 API Request: " + params);

            // Gọi API trong thread riêng
            new Thread(() -> {
                try {
                    List<Map<String, Object>> response = apiService.postApiGetList("/autoUpdate", params);
                    System.out.println("✅ Saved successfully! IsCorrect=" + isCorrect + ", Response: " + response);
                } catch (Exception ex) {
                    System.err.println("❌ API Error: " + ex.getMessage());
                    ex.printStackTrace();
                }
            }).start();

        } catch (Exception ex) {
            System.err.println("❌ Error preparing data: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    // ------------------------- Nộp bài -------------------------
    private void submitExam() {
        // ✅ Kiểm tra StudentId hợp lệ
        if (studentId <= 0) {
            JOptionPane.showMessageDialog(this,
                "❌ Không tìm thấy thông tin học sinh!\nVui lòng đăng nhập lại.",
                "Lỗi",
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        if (examId <= 0) {
            JOptionPane.showMessageDialog(this,
                "❌ Thông tin bài thi không hợp lệ!",
                "Lỗi",
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        System.out.println("📝 Submitting exam for StudentId=" + studentId + ", ExamId=" + examId);
        
        List<Map<String, Object>> submitData = new ArrayList<>();

        // Duyệt toàn bộ câu hỏi
        for (Question q : questions) {
            Integer ansId = selectedAnswers.get(q.id);
            
            // ✅ Lấy IsCorrect từ bảng answers
            Integer isCorrect = 0;
            if (ansId != null) {
                isCorrect = getIsCorrectFromAnswer(q.id, ansId);
            }

            Map<String, Object> record = new HashMap<>();
            record.put("StudentId", studentId);
            record.put("QuestionId", q.id);
            record.put("AnswerId", ansId != null ? ansId : null);
            record.put("IsCorrect", isCorrect != null ? isCorrect : 0); // ✅ Thêm IsCorrect

            submitData.add(record);
        }

        // Gói dữ liệu JSON
        Map<String, Object> params = new HashMap<>();
        params.put("action", "update");
        params.put("method", "UPSERT");
        params.put("table", "exam_answers");
        params.put("data", submitData);

        System.out.println("📤 Submitting data: " + submitData);

        // Gửi request đến API
        try {
            List<Map<String, Object>> response = apiService.postApiGetList("/autoUpdate", params);
            
            System.out.println("📥 API Response: " + response);
            
            // ✅ Tính điểm và lưu vào exam_results
            saveExamResult();
            
            JOptionPane.showMessageDialog(this,
                "✅ Bạn đã nộp bài thành công!\nDữ liệu đã gửi đến server.",
                "Nộp bài thành công",
                JOptionPane.INFORMATION_MESSAGE);
            
            // Đóng cửa sổ
            timer.stop();
            dispose();
            
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                "❌ Lỗi khi nộp bài: " + ex.getMessage(),
                "Lỗi",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void startTimer() {
        timer = new Timer(1000, e -> {
            int minutes = duration / 60;
            int seconds = duration % 60;
            timerLabel.setText(String.format("%02d:%02d", minutes, seconds));
            duration--;
            if (duration < 0) {
                timer.stop();
                JOptionPane.showMessageDialog(this, "⏰ Hết giờ, tự động nộp bài!", "Hết thời gian", JOptionPane.WARNING_MESSAGE);
                submitExam();
            }
        });
        timer.start();
    }

    // ------------------------- Load API -------------------------
    private void loadQuestionsAndAnswersFromAPI() {
        Map<String, Object> params = new HashMap<>();
        params.put("action", "get");
        params.put("method", "SELECT");
        
        params.put("table", List.of("questions", "answers"));
        
        params.put("columns", List.of(
            "questions.id as QuestionId",
            "questions.Question as QuestionText",
            "questions.ClassId",
            "answers.id as AnswerId",
            "answers.Answer as AnswerText",
            "answers.IsCorrect"
        ));

        Map<String, Object> join = new HashMap<>();
        join.put("type", "inner");
        join.put("on", List.of("questions.id = answers.QuestionId"));
        params.put("join", List.of(join));

        Map<String, Object> where = new HashMap<>();
        if (classId > 0) {
            where.put("questions.ClassId", classId);
        }
        params.put("where", where);
        
        params.put("order", "RAND()");
        
        if (numberQuestion > 0) {
            params.put("limit", numberQuestion * 4);
        }
        
        System.out.println("📡 Loading questions with params: " + params);
        
        List<Map<String, Object>> apiData = apiService.postApiGetList("/autoGet", params);
        
        System.out.println("📦 API Response: " + (apiData != null ? apiData.size() + " rows" : "null"));

        if (apiData == null || apiData.isEmpty()) {
            System.err.println("⚠️ No questions found!");
            JOptionPane.showMessageDialog(this, 
                "Không tìm thấy câu hỏi cho bài kiểm tra này!", 
                "Lỗi", 
                JOptionPane.ERROR_MESSAGE);
            questions.clear();
            totalQuestions = 0;
            renderQuestions();
            return;
        }

        if (!apiData.isEmpty()) {
            System.out.println("📋 Sample data: " + apiData.get(0));
        }

        Map<Integer, String> questionTextMap = new LinkedHashMap<>();
        Map<Integer, List<String>> optionsMap = new HashMap<>();
        Map<Integer, List<Integer>> answerIdMap = new HashMap<>();

        for (Map<String, Object> item : apiData) {
            Integer questionId = getFirstInteger(item, "QuestionId");
            Integer answerId = getFirstInteger(item, "AnswerId");
            String questionText = getFirstString(item, "QuestionText", "Question");
            String answerText = getFirstString(item, "AnswerText", "Answer");

            if (questionId == null || questionText == null || answerText == null || answerId == null) {
                System.err.println("⚠️ Skipping invalid row: " + item);
                continue;
            }

            System.out.println("✅ Processing Q" + questionId + ": " + questionText.substring(0, Math.min(30, questionText.length())) + "... | A" + answerId);

            questionTextMap.putIfAbsent(questionId, questionText);
            optionsMap.computeIfAbsent(questionId, k -> new ArrayList<>()).add(answerText);
            answerIdMap.computeIfAbsent(questionId, k -> new ArrayList<>()).add(answerId);
        }

        questions.clear();
        for (Map.Entry<Integer, String> e : questionTextMap.entrySet()) {
            int qId = e.getKey();
            List<String> opts = optionsMap.get(qId);
            List<Integer> aids = answerIdMap.get(qId);
            
            System.out.println("✅ Adding question Q" + qId + " with " + opts.size() + " answers");
            
            questions.add(new Question(qId, e.getValue(), opts, aids));
        }

        if (numberQuestion > 0 && questions.size() > numberQuestion) {
            questions = questions.subList(0, numberQuestion);
        }

        totalQuestions = questions.size();
        
        System.out.println("✅ Final: Loaded " + totalQuestions + " questions");
        
        navPanel.removeAll();
        for (int i = 1; i <= totalQuestions; i++) {
            final int index = i;
            JButton btn = new JButton(String.valueOf(i));
            btn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            btn.setPreferredSize(new Dimension(40, 40));
            btn.setBackground(new Color(220, 220, 220));
            btn.addActionListener(e -> {
                currentPage = (int) Math.ceil((double) index / perPage);
                renderQuestions();
            });
            navPanel.add(btn);
        }
        navPanel.revalidate();
        navPanel.repaint();

        System.out.println("🎨 Rendering questions...");
        renderQuestions();
    }

    // ✅ Method mới: Lấy StudentId từ email
    private int getStudentIdByEmail(String email) {
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("action", "get");
            params.put("method", "SELECT");
            
            // Join student với account
            params.put("table", List.of("student", "account"));
            
            params.put("columns", List.of(
                "student.Id as StudentId",
                "account.email"
            ));
            
            // Join condition
            Map<String, Object> join = new HashMap<>();
            join.put("type", "inner");
            join.put("on", List.of("student.IdAccount = account.id"));
            params.put("join", List.of(join));
            
            // WHERE: Lọc theo email
            Map<String, Object> where = new HashMap<>();
            where.put("account.email", email);
            params.put("where", where);
            
            System.out.println("📡 Getting StudentId for email: " + email);
            
            List<Map<String, Object>> result = apiService.postApiGetList("/autoGet", params);
            
            System.out.println("📥 Response: " + result);
            
            if (result != null && !result.isEmpty()) {
                Object studentIdObj = result.get(0).get("StudentId");
                if (studentIdObj == null) {
                    studentIdObj = result.get(0).get("student.Id");
                }
                if (studentIdObj == null) {
                    studentIdObj = result.get(0).get("Id");
                }
                
                if (studentIdObj instanceof Number) {
                    int id = ((Number) studentIdObj).intValue();
                    System.out.println("✅ Found StudentId: " + id);
                    return id;
                }
            }
            
            System.err.println("⚠️ Student not found for email: " + email);
            return -1;
            
        } catch (Exception e) {
            System.err.println("❌ Error getting student ID: " + e.getMessage());
            e.printStackTrace();
            return -1;
        }
    }
    // ...existing code...

// ✅ Method mới: Lấy IsCorrect từ bảng answers (GIỮ NGUYÊN - đúng rồi)
private Integer getIsCorrectFromAnswer(int questionId, int answerId) {
    try {
        Map<String, Object> params = new HashMap<>();
        params.put("action", "get");
        params.put("method", "SELECT");
        params.put("table", "answers");
        params.put("columns", List.of("IsCorrect"));
        
        Map<String, Object> where = new HashMap<>();
        where.put("QuestionId", questionId);
        where.put("id", answerId);
        params.put("where", where);
        
        System.out.println("🔍 Checking IsCorrect for Q" + questionId + ", A" + answerId);
        
        List<Map<String, Object>> result = apiService.postApiGetList("/autoGet", params);
        
        if (result != null && !result.isEmpty()) {
            Object isCorrectObj = result.get(0).get("IsCorrect");
            
            System.out.println("   Raw IsCorrect value: " + isCorrectObj + " (type: " + (isCorrectObj != null ? isCorrectObj.getClass().getName() : "null") + ")");
            
            if (isCorrectObj instanceof Number) {
                int value = ((Number) isCorrectObj).intValue();
                System.out.println("   ✅ IsCorrect = " + value);
                return value;
            } else if (isCorrectObj instanceof Boolean) {
                int value = ((Boolean) isCorrectObj) ? 1 : 0;
                System.out.println("   ✅ IsCorrect = " + value);
                return value;
            } else if (isCorrectObj instanceof String) {
                int value = ("1".equals(isCorrectObj) || "true".equalsIgnoreCase((String) isCorrectObj)) ? 1 : 0;
                System.out.println("   ✅ IsCorrect = " + value);
                return value;
            }
        }
        
        System.err.println("   ⚠️ IsCorrect not found, defaulting to 0");
        return 0;
        
    } catch (Exception e) {
        System.err.println("❌ Error getting IsCorrect for Q" + questionId + " A" + answerId + ": " + e.getMessage());
        e.printStackTrace();
        return 0;
    }
}


    // ✅ Method mới: Tính điểm và lưu vào exam_results
    private void saveExamResult() {
        try {
            int correctCount = 0;
            
            for (Question q : questions) {
                Integer selectedAnswerId = selectedAnswers.get(q.id);
                if (selectedAnswerId != null) {
                    if (isCorrectAnswer(q.id, selectedAnswerId)) {
                        correctCount++;
                    }
                }
            }
            
            double score = (double) correctCount / totalQuestions * 10;
            score = Math.round(score * 100.0) / 100.0;
            
            System.out.println("📊 Score: " + correctCount + "/" + totalQuestions + " = " + score + " điểm");
            
            Map<String, Object> resultRecord = new HashMap<>();
            resultRecord.put("ExamId", examId);
            resultRecord.put("StudentId", studentId);
            resultRecord.put("Score", score);
            resultRecord.put("SubmittedDate", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
            
            Map<String, Object> params = new HashMap<>();
            params.put("action", "update");
            params.put("method", "UPSERT");
            params.put("table", "exam_results");
            params.put("data", List.of(resultRecord));
            
            List<Map<String, Object>> response = apiService.postApiGetList("/autoUpdate", params);
            System.out.println("✅ Saved exam result: " + response);
            
        } catch (Exception e) {
            System.err.println("❌ Error saving exam result: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ✅ Method kiểm tra đáp án đúng
    private boolean isCorrectAnswer(int questionId, int answerId) {
        Integer isCorrect = getIsCorrectFromAnswer(questionId, answerId);
        return isCorrect != null && isCorrect == 1;
    }

    private Integer getFirstInteger(Map<String, Object> map, String... keys) {
        for (String key : keys) {
            Object val = map.get(key);
            if (val instanceof Number) {
                return ((Number) val).intValue();
            }
        }
        return null;
    }

    private String getFirstString(Map<String, Object> map, String... keys) {
        for (String key : keys) {
            Object val = map.get(key);
            if (val != null) {
                return val.toString();
            }
        }
        return null;
    }

    private boolean isExamTimeValid(String publishDate, String expireDate) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date now = new Date();
            
            if (publishDate != null && !publishDate.isEmpty() && !publishDate.equals("null")) {
                try {
                    Date pubDate = sdf.parse(publishDate);
                    if (now.before(pubDate)) {
                        System.out.println("❌ Exam not yet published. PublishDate: " + publishDate);
                        return false;
                    }
                } catch (ParseException e) {
                    System.err.println("⚠️ Cannot parse PublishDate: " + publishDate);
                }
            }
            
            if (expireDate != null && !expireDate.isEmpty() && !expireDate.equals("null")) {
                try {
                    Date expDate = sdf.parse(expireDate);
                    if (now.after(expDate)) {
                        System.out.println("❌ Exam expired. ExpireDate: " + expireDate);
                        return false;
                    }
                } catch (ParseException e) {
                    System.err.println("⚠️ Cannot parse ExpireDate: " + expireDate);
                }
            }
            
            System.out.println("✅ Exam time is valid");
            return true;
        } catch (Exception e) {
            System.err.println("⚠️ Error checking exam time: " + e.getMessage());
            return true;
        }
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}