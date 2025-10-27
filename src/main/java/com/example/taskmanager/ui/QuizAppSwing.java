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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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
import static javax.swing.WindowConstants.EXIT_ON_CLOSE;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import com.example.taskmanager.model.Task;
import com.example.taskmanager.service.ApiService;
import com.example.taskmanager.service.AuthService;
import com.example.taskmanager.service.StudentInfoService;
import com.google.api.services.oauth2.model.Userinfo;

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
    private Integer attemptId = null; // ✅ Attempt hiện tại
    private Timer autoSubmitTimer = null; // Timer để tự nộp theo EndTime
    private StudentDashboard studentDashboard;  // Thêm reference đến MainWindow

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
    // Nếu cần constructor không truyền exam/class/number: sử dụng overload này
    public QuizAppSwing(ApiService apiService, AuthService authService) {
        this(apiService, authService, -1, -1, 0, null);
    }

    public QuizAppSwing(ApiService apiService, AuthService authService, int examId, int classId, int numberQuestion, StudentDashboard studentDashboard) {
        this.apiService = apiService;
        this.authService = authService;
        this.studentInfoService = new StudentInfoService(apiService);
        this.examId = examId;
        this.classId = classId;
        this.numberQuestion = numberQuestion;
        this.studentDashboard = studentDashboard;
        
        // ✅ Lấy AccountId từ AuthService (từ token) — dùng method có sẵn getUserIdFromToken
        int accountId = -1;
        try {
            String token = authService.getAccessToken();
            if (token != null && !token.isEmpty()) {
                accountId = authService.getUserIdFromToken(token);
                System.out.println("🔑 AccountId from AuthService token: " + accountId);
            } else {
                System.out.println("ℹ️ No access token available, cannot resolve AccountId");
            }
        } catch (Exception e) {
            System.err.println("⚠️ Cannot get account id from authService: " + e.getMessage());
        }

        // ✅ Lấy StudentId từ accountId (bắt buộc). Không sử dụng email nữa.
        if (accountId > 0) {
            this.studentId = getStudentIdByAccount(accountId, this.classId);
        } else {
            this.studentId = -1;
        }
        System.out.println("👤 Student ID: " + studentId);
        
        if (studentId <= 0) {
            JOptionPane.showMessageDialog(null,
                "⚠️ Không tìm thấy thông tin học sinh theo tài khoản hiện tại!",
                "Cảnh báo",
                JOptionPane.WARNING_MESSAGE);
        }
        
        // ✅ Lấy thông tin profile học sinh bằng StudentId (thay vì email)
        List<Map<String, Object>> studentExamData = List.of(new HashMap<>());
        try {
            if (studentId > 0) {
                Map<String, Object> params = new HashMap<>();
                params.put("action", "get");
                params.put("method", "SELECT");
                params.put("table", List.of("student", "account", "classes"));
                params.put("columns", List.of(
                    "student.Id as StudentId",
                    "student.FullName",
                    "student.ClassId",
                    "classes.Name as ClassName",
                    "account.email as Email"
                ));
                Map<String, Object> join = new HashMap<>();
                join.put("type", "inner");
                join.put("on", List.of("student.IdAccount = account.id", "student.ClassId = classes.Id"));
                params.put("join", List.of(join));
                Map<String, Object> where = new HashMap<>();
                where.put("student.Id", studentId);
                params.put("where", where);

                System.out.println("📡 Fetching student profile by StudentId=" + studentId);
                List<Map<String, Object>> resp = apiService.postApiGetList("/autoGet", params);
                if (resp != null && !resp.isEmpty()) studentExamData = resp;
                System.out.println("📥 Student profile: " + studentExamData);
            }
        } catch (Exception e) {
            System.err.println("⚠️ Error fetching student profile by StudentId: " + e.getMessage());
        }

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
        
        // ✅ Sử dụng dữ liệu profile đã lấy bằng StudentId; nếu không có -> fallback lấy theo accountId từ token
        // ✅ Sử dụng studentEmail đã lấy ở trên
        // Reuse studentExamData previously fetched by StudentId (if any); otherwise fetch by accountId
        List<Map<String, Object>> studentExamDataForInfo = studentExamData;
        if ((studentExamDataForInfo == null || studentExamDataForInfo.isEmpty()) && accountId > 0) {
            studentExamDataForInfo = studentInfoService.fetchProfileById(accountId);
        }
        System.out.println("Loading student exam data..." + studentExamDataForInfo);
        if (studentExamDataForInfo == null) studentExamDataForInfo = List.of(new HashMap<>());
        
        infoPanel.add(new JLabel("Họ và tên: " + studentExamDataForInfo.stream()
                .map(m -> m.getOrDefault("FullName", m.get("StudentName")))
                .filter(Objects::nonNull)
                .map(Object::toString)
                .findFirst().orElse("N/A")));
        infoPanel.add(new JLabel("Lớp: " + studentExamDataForInfo.stream()
                .map(m -> m.get("ClassName"))
                .filter(Objects::nonNull)
                .map(Object::toString)
                .findFirst().orElse("N/A")));
        infoPanel.add(new JLabel("Môn: " + studentExamDataForInfo.stream()
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
            final int qId = q.id;
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
                final int aid = answerId;
                final int qid = qId;
                JRadioButton option = new JRadioButton(optText);
                option.setFont(new Font("Segoe UI", Font.PLAIN, 13));
                option.setBackground(Color.WHITE);
                if (Objects.equals(selectedAnswers.get(qid), aid)) {
                    option.setSelected(true);
                }
                option.addActionListener(e -> {
                    selectedAnswers.put(qid, aid);
                    refreshNavPanel();
                    saveAnswerToApi(qid, aid);
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
 
        // Randomize question order before applying numberQuestion limit
        Collections.shuffle(questions);
        if (numberQuestion > 0 && questions.size() > numberQuestion) {
            questions = new ArrayList<>(questions.subList(0, numberQuestion));
        }
 
        totalQuestions = questions.size();
        System.out.println("✅ Final: Loaded " + totalQuestions + " questions");

        // ✅ Đảm bảo có Attempt và prefill exam_answers trước khi render
        ensureAttemptAndPrefill();

        // ✅ Khôi phục các lựa chọn đã lưu theo AttemptId
        loadPreviousSelections();

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
    // private int getStudentIdByEmail(String email) {
    //     try {
    //         Map<String, Object> params = new HashMap<>();
    //         params.put("action", "get");
    //         params.put("method", "SELECT");
            
    //         // Join student với account
    //         params.put("table", List.of("student", "account"));
            
    //         params.put("columns", List.of(
    //             "student.Id as StudentId",
    //             "account.email"
    //         ));
            
    //         // Join condition
    //         Map<String, Object> join = new HashMap<>();
    //         join.put("type", "inner");
    //         join.put("on", List.of("student.IdAccount = account.id"));
    //         params.put("join", List.of(join));
            
    //         // WHERE: Lọc theo email
    //         Map<String, Object> where = new HashMap<>();
    //         where.put("account.email", email);
    //         params.put("where", where);
            
    //         System.out.println("📡 Getting StudentId for email: " + email);
            
    //         List<Map<String, Object>> result = apiService.postApiGetList("/autoGet", params);
            
    //         System.out.println("📥 Response: " + result);
            
    //         if (result != null && !result.isEmpty()) {
    //             Object studentIdObj = result.get(0).get("StudentId");
    //             if (studentIdObj == null) {
    //                 studentIdObj = result.get(0).get("student.Id");
    //             }
    //             if (studentIdObj == null) {
    //                 studentIdObj = result.get(0).get("Id");
    //             }
                
    //             if (studentIdObj instanceof Number) {
    //                 int id = ((Number) studentIdObj).intValue();
    //                 System.out.println("✅ Found StudentId: " + id);
    //                 return id;
    //             }
    //         }
            
    //         System.err.println("⚠️ Student not found for email: " + email);
    //         return -1;
            
    //     } catch (Exception e) {
    //         System.err.println("❌ Error getting student ID: " + e.getMessage());
    //         e.printStackTrace();
    //         return -1;
    //     }
    // }

    // New: Lấy StudentId bằng AccountId + optional ClassId filter
    private int getStudentIdByAccount(int accountId, int classId) {
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("action", "get");
            params.put("method", "SELECT");
            params.put("table", "student");
            params.put("columns", List.of("student.Id as StudentId", "student.IdAccount", "student.ClassId"));

            Map<String, Object> where = new HashMap<>();
            where.put("student.IdAccount", accountId);
            if (classId > 0) {
                where.put("student.ClassId", classId);
            }
            params.put("where", where);

            System.out.println("📡 Getting StudentId for AccountId=" + accountId + " ClassId=" + classId);
            List<Map<String, Object>> result = apiService.postApiGetList("/autoGet", params);
            System.out.println("📥 Response: " + result);

            if (result != null && !result.isEmpty()) {
                Object studentIdObj = result.get(0).get("StudentId");
                if (studentIdObj instanceof Number) {
                    int id = ((Number) studentIdObj).intValue();
                    System.out.println("✅ Found StudentId (by account): " + id);
                    return id;
                } else if (studentIdObj instanceof String) {
                    try { return Integer.parseInt((String) studentIdObj); } catch (Exception ignored) {}
                }
            }
            System.err.println("⚠️ Student not found for AccountId: " + accountId);
            return -1;
        } catch (Exception e) {
            System.err.println("❌ Error getting student ID by account: " + e.getMessage());
            e.printStackTrace();
            return -1;
        }
    }

    // ✅ Method mới: Lấy IsCorrect từ bảng answers 
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
            int correctCount = countCorrectAnswersByAttempt(attemptId);
            double score = totalQuestions > 0 ? Math.round((correctCount * 10.0 / totalQuestions) * 100.0) / 100.0 : 0.0;

            System.out.println("📊 Score: " + correctCount + "/" + totalQuestions + " = " + score + " điểm");

            Map<String, Object> resultRecord = new HashMap<>();
            resultRecord.put("ExamId", examId);
            resultRecord.put("StudentId", studentId);
            resultRecord.put("AttemptId", attemptId);
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

    // Đếm số câu đúng theo AttemptId bằng cách JOIN answers.IsCorrect
    private int countCorrectAnswersByAttempt(Integer attemptId) {
        if (attemptId == null) return 0;
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("action", "get");
            params.put("method", "SELECT");
            params.put("table", List.of("exam_answers", "answers"));
            params.put("columns", List.of("answers.IsCorrect"));

            Map<String, Object> join = new HashMap<>();
            join.put("type", "inner");
            join.put("on", List.of("exam_answers.AnswerId = answers.id"));
            params.put("join", List.of(join));

            Map<String, Object> where = new HashMap<>();
            where.put("exam_answers.AttemptId", attemptId);
            params.put("where", where);

            List<Map<String, Object>> rs = apiService.postApiGetList("/autoGet", params);
            if (rs == null) return 0;

            int cnt = 0;
            for (Map<String, Object> row : rs) {
                Object v = row.get("IsCorrect");
                if (v instanceof Number) {
                    if (((Number) v).intValue() == 1) cnt++;
                } else if (v instanceof Boolean) {
                    if ((Boolean) v) cnt++;
                } else if (v instanceof String) {
                    if ("1".equals(v) || "true".equalsIgnoreCase((String) v)) cnt++;
                }
            }
            return cnt;
        } catch (Exception e) {
            System.err.println("⚠️ countCorrectAnswersByAttempt error: " + e.getMessage());
            return 0;
        }
    }


    // ✅ Method kiểm tra đáp án đúng
    // private boolean isCorrectAnswer(int questionId, int answerId) {
    //     Integer isCorrect = getIsCorrectFromAnswer(questionId, answerId);
    //     return isCorrect != null && isCorrect == 1;
    // }

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

    // Tạo/FIND attempt và prefill exam_answers (đã có)
    private synchronized void ensureAttemptAndPrefill() {
        System.out.println("check Student Id"+ studentId);
        if (studentId <= 0 || examId <= 0) {
            System.err.println("❌ Missing studentId/examId for attempt creation");
            return;
        }
        if (attemptId != null) {
            System.out.println("ℹ️ Attempt already initialized: " + attemptId);
            return;
        }

        Integer existing = findExistingAttemptId(examId, studentId);
        if (existing != null) {
            // Extra safety: verify attempt's StudentId matches current studentId
            System.out.println("🔁 Reusing existing attemptId=" + existing + " for StudentId=" + studentId);
            attemptId = existing;
        } else {
            attemptId = createAttempt(examId, studentId);
            System.out.println("🆕 Created attemptId=" + attemptId);
        }

        prefillExamAnswersForAttempt();

        // Sau khi có attemptId: schedule auto-submit nếu backend có EndTime
        scheduleAutoSubmit();
    }

    // Tìm attempt có thể resume: tìm các attempt gần nhất rồi kiểm tra EndTime/Status
    private Integer findExistingAttemptId(int examId, int studentId) {
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("action", "get");
            params.put("method", "SELECT");
            params.put("table", "exam_attempts");
            params.put("columns", List.of("id", "Status", "EndTime", "StartTime", "SubmitTime", "StudentId"));
            Map<String, Object> where = new HashMap<>();
            where.put("ExamId", examId);
            // IMPORTANT: always filter by StudentId to avoid cross-student reuse
            where.put("StudentId", studentId);
            params.put("where", where);
            params.put("order", "id DESC");
            params.put("limit", 10); // lấy vài bản ghi gần nhất để kiểm tra

            List<Map<String, Object>> rs = apiService.postApiGetList("/autoGet", params);
            if (rs == null || rs.isEmpty()) return null;

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date now = new Date();

            for (Map<String, Object> row : rs) {
                Object idObj = row.get("id");
                if (!(idObj instanceof Number)) continue;
                int id = ((Number) idObj).intValue();

                // verify the StudentId column just in case API returned extra rows
                Object sidObj = row.get("StudentId");
                if (sidObj instanceof Number) {
                    int sid = ((Number) sidObj).intValue();
                    if (sid != studentId) {
                        System.out.println("⚠️ Skipping attempt " + id + " because StudentId mismatch: " + sid + " != " + studentId);
                        continue;
                    }
                }

                String status = getFirstString(row, "Status");
                String endTimeStr = getFirstString(row, "EndTime");

                // Nếu đang in_progress thì resume
                if ("in_progress".equalsIgnoreCase(status)) {
                    System.out.println("🔁 Found in_progress attempt: " + id);
                    return id;
                }

                // Nếu chưa submit và EndTime null or in future => resume
                boolean submitFlag = "submitted".equalsIgnoreCase(status);
                if (!submitFlag) {
                    if (endTimeStr == null || endTimeStr.isEmpty() || "null".equalsIgnoreCase(endTimeStr)) {
                        System.out.println("🔁 Found resumable attempt (no EndTime): " + id + " status=" + status);
                        return id;
                    }
                    try {
                        Date endTime = sdf.parse(endTimeStr);
                        if (endTime.after(now)) {
                            System.out.println("🔁 Found resumable attempt (EndTime in future): " + id + " EndTime=" + endTimeStr);
                            return id;
                        }
                    } catch (Exception pe) {
                        System.err.println("⚠️ Cannot parse EndTime for attempt " + id + ": " + endTimeStr);
                        // nếu parse lỗi, để tiếp tục kiểm tra bản ghi khác
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("⚠️ findExistingAttemptId error: " + e.getMessage());
        }
        return null;
    }

    // Tạo attempt mới (Status=in_progress)
    private Integer createAttempt(int examId, int studentId) {
        try {
            // 1) Double-check existing to avoid duplicates (race-safe when combined with DB UNIQUE)
            Integer existing = findExistingAttemptId(examId, studentId);
            if (existing != null) {
                System.out.println("🔁 createAttempt: existing attempt found before insert: " + existing);
                return existing;
            }

            Map<String, Object> record = new HashMap<>();
            record.put("ExamId", examId);
            record.put("StudentId", studentId); // ensure StudentId stored on creation
            record.put("Status", "in_progress");
            record.put("StartTime", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));

            Map<String, Object> params = new HashMap<>();
            params.put("action", "update");
            params.put("method", "INSERT");
            params.put("table", "exam_attempts");
            params.put("data", List.of(record));

            List<Map<String, Object>> resp = null;
            try {
                resp = apiService.postApiGetList("/autoUpdate", params);
                System.out.println("🆕 createAttempt resp: " + resp);
            } catch (Exception ie) {
                // Nếu lỗi do duplicate unique constraint ở DB, re-query existing attempt
                System.err.println("⚠️ createAttempt insert error (will try to re-find existing): " + ie.getMessage());
            }

            Integer id = null;
            if (resp != null && !resp.isEmpty()) {
                Object idObj = resp.get(0).get("id");
                if (idObj instanceof Number) id = ((Number) idObj).intValue();
            }

            // fallback: nếu insert không trả về id (ví dụ do constraint) -> tìm existing
            if (id == null) {
                id = findExistingAttemptId(examId, studentId);
            }
            return id;
        } catch (Exception e) {
            System.err.println("❌ createAttempt error: " + e.getMessage());
            return null;
        }
    }

    // Prefill exam_answers cho toàn bộ câu hỏi của Attempt - CHỈ CHÈN các câu còn thiếu
    private void prefillExamAnswersForAttempt() {
        if (attemptId == null) {
            System.err.println("❌ Cannot prefill answers: attemptId is null");
            return;
        }
        try {
            // 1) Lấy danh sách QuestionId đã có trong exam_answers cho attempt này
            Map<String, Object> qparams = new HashMap<>();
            qparams.put("action", "get");
            qparams.put("method", "SELECT");
            qparams.put("table", "exam_answers");
            qparams.put("columns", List.of("QuestionId"));
            Map<String, Object> where = new HashMap<>();
            where.put("AttemptId", attemptId);
            // IMPORTANT: ensure we only query rows for the current StudentId as well
            if (studentId > 0) {
                where.put("StudentId", studentId);
            }
            qparams.put("where", where);

            List<Map<String, Object>> existing = apiService.postApiGetList("/autoGet", qparams);
            java.util.Set<Integer> existingQ = new java.util.HashSet<>();
            if (existing != null) {
                for (Map<String, Object> r : existing) {
                    Integer qid = getFirstInteger(r, "QuestionId");
                    if (qid != null) existingQ.add(qid);
                }
            }

            // 2) Chuẩn bị danh sách chèn cho các question chưa có
            List<Map<String, Object>> toInsert = new ArrayList<>();
            for (Question q : questions) {
                if (!existingQ.contains(q.id)) {
                    Map<String, Object> rec = new HashMap<>();
                    rec.put("AttemptId", attemptId);
                    rec.put("StudentId", studentId);
                    rec.put("QuestionId", q.id);
                    rec.put("AnswerId", null);   // chưa chọn
                    toInsert.add(rec);
                }
            }

            if (toInsert.isEmpty()) {
                System.out.println("No missing exam_answers to prefill for attemptId=" + attemptId);
                return;
            }

            Map<String, Object> params = new HashMap<>();
            params.put("action", "update");
            params.put("method", "INSERT"); // INSERT only: không override các hàng đã có
            params.put("table", "exam_answers");
            params.put("data", toInsert);

            System.out.println("Inserting missing exam_answers for attemptId=" + attemptId + " count=" + toInsert.size());
            List<Map<String, Object>> resp = apiService.postApiGetList("/autoUpdate", params);
            System.out.println("Prefill insert resp: " + resp);
        } catch (Exception e) {
            System.err.println("prefillExamAnswersForAttempt error: " + e.getMessage());
        }
    }

    // Khôi phục các lựa chọn đã lưu theo AttemptId
    private void loadPreviousSelections() {
        if (attemptId == null) return;
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("action", "get");
            params.put("method", "SELECT");
            params.put("table", "exam_answers");
            params.put("columns", List.of("QuestionId", "AnswerId"));
            Map<String, Object> where = new HashMap<>();
            where.put("AttemptId", attemptId);
            // add StudentId filter to be safe
            if (studentId > 0) where.put("StudentId", studentId);
            params.put("where", where);

            List<Map<String, Object>> rs = apiService.postApiGetList("/autoGet", params);
            if (rs != null) {
                for (Map<String, Object> row : rs) {
                    Integer qid = getFirstInteger(row, "QuestionId");
                    Integer aid = getFirstInteger(row, "AnswerId");
                    if (qid != null && aid != null) {
                        selectedAnswers.put(qid, aid);
                    }
                }
                System.out.println("Restored " + selectedAnswers.size() + " selections from DB");
                refreshNavPanel();
            }
        } catch (Exception e) {
            System.err.println("loadPreviousSelections error: " + e.getMessage());
        }
    }

    // Lưu đáp án mỗi khi chọn (onclick)
    private void saveAnswerToApi(int questionId, int answerId) {
        if (studentId <= 0 || examId <= 0) {
            System.err.println("Cannot save: invalid studentId/examId");
            return;
        }
        if (attemptId == null) {
            System.out.println("Attempt not ready. Initializing...");
            ensureAttemptAndPrefill();
            if (attemptId == null) {
                System.err.println("Cannot save: attemptId is null");
                return;
            }
        }

        System.out.println("Saving answer: AttemptId=" + attemptId + ", StudentId=" + studentId +
                ", QuestionId=" + questionId + ", AnswerId=" + answerId);

        try {
            Map<String, Object> record = new HashMap<>();
            record.put("AttemptId", attemptId);
            record.put("StudentId", studentId);
            record.put("QuestionId", questionId);
            record.put("AnswerId", answerId);

            Map<String, Object> params = new HashMap<>();
            params.put("action", "update");
            params.put("method", "UPSERT"); // UNIQUE (AttemptId, QuestionId)
            params.put("table", "exam_answers");
            params.put("data", List.of(record));

            List<Map<String, Object>> response = apiService.postApiGetList("/autoUpdate", params);
            System.out.println("✅ Saved: " + response);
        } catch (Exception ex) {
            System.err.println("❌ Error saving: " + ex.getMessage());
        }
    }

    // Nộp bài: lưu tất cả đáp án + đánh dấu attempt + tính điểm
    private void submitExam() {
    
    // ✅ Validate thông tin cơ bản
    if (studentId <= 0 || examId <= 0) {
        JOptionPane.showMessageDialog(this, 
            "Thiếu thông tin học sinh/bài thi", 
            "Lỗi", 
            JOptionPane.ERROR_MESSAGE);
        return;
    }
    
    // ✅ Đảm bảo có attemptId
    if (attemptId == null) {
        ensureAttemptAndPrefill();
        if (attemptId == null) {
            JOptionPane.showMessageDialog(this, 
                "Không khởi tạo được Attempt!", 
                "Lỗi", 
                JOptionPane.ERROR_MESSAGE);
            return;
        }
    }

    System.out.println("📝 Submitting exam for StudentId=" + studentId + 
                       ", ExamId=" + examId + 
                       ", AttemptId=" + attemptId);

    // Chuẩn bị dữ liệu submit

    List<Map<String, Object>> submitData = new ArrayList<>();
    for (Question q : questions) {
        Integer ansId = selectedAnswers.get(q.id);

        Map<String, Object> record = new HashMap<>();
        record.put("AttemptId", attemptId);
        record.put("StudentId", studentId);
        record.put("QuestionId", q.id);
        record.put("AnswerId", ansId != null ? ansId : null);

        submitData.add(record);
    }

    Map<String, Object> params = new HashMap<>();
    params.put("action", "update");
    params.put("method", "UPSERT");
    params.put("table", "exam_answers");
    params.put("data", submitData);

    try {
        List<Map<String, Object>> response = apiService.postApiGetList("/autoUpdate", params);
        System.out.println("Save-all answers response: " + response);

        markAttemptSubmitted();
        saveExamResult();

        JOptionPane.showMessageDialog(this, "Nộp bài thành công!", "OK", JOptionPane.INFORMATION_MESSAGE);
        
        if (timer != null) timer.stop();
        
        // Dừng auto-submit timer
        if (autoSubmitTimer != null) {
            autoSubmitTimer.stop();
        }
        
        // ✅ QUAN TRỌNG: Hiển thị dashboard TRƯỚC khi dispose
        if (studentDashboard != null) {
            studentDashboard.setVisible(true);
            studentDashboard.toFront();
            studentDashboard.requestFocus();
            System.out.println("✅ StudentDashboard shown");
        } else {
            System.err.println("⚠️ studentDashboard is null!");
        }
        
        // ✅ Dispose CUỐI CÙNG - sử dụng invokeLater để đảm bảo dashboard hiện trước
        javax.swing.SwingUtilities.invokeLater(() -> {
            this.setVisible(false);
            this.dispose();
            System.out.println("✅ QuizAppSwing disposed");
        });
        
    } catch (Exception e) {
        System.err.println("❌ Error submitting exam: " + e.getMessage());
        JOptionPane.showMessageDialog(this, "Lỗi khi nộp bài: " + e.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
    }

        
    }

    // Cập nhật trạng thái attempt -> submitted
    private void markAttemptSubmitted() {
        if (attemptId == null) return;
        try {
            Map<String, Object> rec = new HashMap<>();
            rec.put("id", attemptId);
            rec.put("Status", "submitted");
            rec.put("SubmitTime", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
            rec.put("EndTime", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));

            Map<String, Object> params = new HashMap<>();
            params.put("action", "update");
            params.put("method", "UPDATE");
            params.put("table", "exam_attempts");
            params.put("data", List.of(rec));

            List<Map<String, Object>> resp = apiService.postApiGetList("/autoUpdate", params);
            System.out.println("🧾 markAttemptSubmitted resp: " + resp);
        } catch (Exception e) {
            System.err.println("⚠️ markAttemptSubmitted error: " + e.getMessage());
        }
    }

    // Nếu thiếu method này trong file của bạn, thêm vào:
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

    // ------------------------- Schedule auto submit theo EndTime của attempt -------------------------
    private void scheduleAutoSubmit() {
        // clear cũ nếu có
        if (autoSubmitTimer != null) {
            autoSubmitTimer.stop();
            autoSubmitTimer = null;
        }
        if (attemptId == null) return;
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("action", "get");
            params.put("method", "SELECT");
            params.put("table", "exam_attempts");
            params.put("columns", List.of("EndTime", "Status"));
            Map<String, Object> where = new HashMap<>();
            where.put("id", attemptId);
            params.put("where", where);

            List<Map<String, Object>> rs = apiService.postApiGetList("/autoGet", params);
            if (rs == null || rs.isEmpty()) return;
            String status = getFirstString(rs.get(0), "Status");
            String endTimeStr = getFirstString(rs.get(0), "EndTime");
            if ("submitted".equalsIgnoreCase(status)) {
                System.out.println("ℹ️ Attempt already submitted: " + attemptId);
                return;
            }
            if (endTimeStr == null || endTimeStr.isEmpty() || "null".equalsIgnoreCase(endTimeStr)) {
                System.out.println("ℹ️ No EndTime set for attempt " + attemptId + ", auto-submit not scheduled");
                return;
            }
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date endTime = sdf.parse(endTimeStr);
            long delayMs = endTime.getTime() - System.currentTimeMillis();
            if (delayMs <= 0) {
                System.out.println("⌛ EndTime passed — submitting now for attempt " + attemptId);
                submitExam();
                return;
            }

            // Swing Timer dùng int ms (<= Integer.MAX_VALUE). Nếu quá lớn, schedule periodic check
            final int safeDelay = (delayMs > Integer.MAX_VALUE) ? Integer.MAX_VALUE : (int) delayMs;
            autoSubmitTimer = new Timer(safeDelay, ev -> {
                try {
                    // khi timeout: kiểm tra lại EndTime/Status trước khi nộp
                    Map<String, Object> check = new HashMap<>();
                    check.put("action", "get");
                    check.put("method", "SELECT");
                    check.put("table", "exam_attempts");
                    check.put("columns", List.of("EndTime", "Status"));
                    Map<String, Object> w = new HashMap<>();
                    w.put("id", attemptId);
                    check.put("where", w);
                    List<Map<String, Object>> r2 = apiService.postApiGetList("/autoGet", check);
                    if (r2 != null && !r2.isEmpty()) {
                        String s2 = getFirstString(r2.get(0), "Status");
                        String e2 = getFirstString(r2.get(0), "EndTime");
                        if (!"submitted".equalsIgnoreCase(s2)) {
                            System.out.println("⌛ Auto-submitting attempt " + attemptId + " due to EndTime=" + e2);
                            submitExam();
                        } else {
                            System.out.println("ℹ️ Attempt already submitted by other process: " + attemptId);
                        }
                    }
                } catch (Exception ex) {
                    System.err.println("⚠️ Error during autoSubmitTimer action: " + ex.getMessage());
                } finally {
                    Timer t = (Timer) ev.getSource();
                    t.stop();
                }
            });
            autoSubmitTimer.setRepeats(false);
            autoSubmitTimer.start();
            System.out.println("⏱️ Auto-submit scheduled in " + (delayMs / 1000) + "s for attempt " + attemptId);
        } catch (Exception e) {
            System.err.println("⚠️ scheduleAutoSubmit error: " + e.getMessage());
        }
    }
    public static void main(String[] args) {
        s
        SwingUtilities.invokeLater(() -> {
            QuizAppSwing app = new QuizAppSwing();
            app.setVisible(true);
        });
    }
}