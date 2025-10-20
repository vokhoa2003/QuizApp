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
    private StudentInfoService studentInfoService; // ✅ Thêm service

    // ------------------------- Question Class -------------------------
    private static class Question {
        int id;                     // questions.id
        String questionText;        // nội dung câu hỏi
        List<String> options;       // 4 đáp án A/B/C/D
        List<Integer> answerIds;    // id tương ứng của từng đáp án trong DB

        Question(int id, String questionText, List<String> options, List<Integer> answerIds) {
            this.id = id;
            this.questionText = questionText;
            this.options = options;
            this.answerIds = answerIds;
        }
    }

    // ------------------------- Constructor -------------------------
    public QuizAppSwing(ApiService apiService, AuthService authService, MainWindow mainWindow) {
        this.apiService = apiService;
        this.authService = authService;
        this.studentInfoService = new StudentInfoService(apiService); // ✅ Khởi tạo

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
        
        // ✅ Lấy email từ authService (giả sử có method getUserEmail())
        String userEmail = null;
        try {
            userEmail = (String) authService.getClass().getMethod("getUserEmail").invoke(authService);
        } catch (Exception ignored) {
            System.err.println("⚠️ Cannot get user email from authService");
        }
        
        List<Map<String, Object>> studentExamData = studentInfoService.fetchProfileByEmail(userEmail);
        System.out.println("Loading student exam data..." + studentExamData);
        
        infoPanel.add(new JLabel("Họ và tên: " + studentExamData.stream()
                .map(m -> m.get("FullName"))
                .filter(Objects::nonNull)
                .map(Object::toString)
                .findFirst().orElse("N/A")));
        infoPanel.add(new JLabel("Lớp: " + studentExamData.stream()
                .map(m -> m.get("ClassName")) // ✅ Sửa key cho đúng với StudentInfoService
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

        // Timer section
        JPanel timerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        timerPanel.setBackground(new Color(240, 248, 255));
        JLabel timeLabel = new JLabel("⏰ Thời gian còn lại:");
        timeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        timeLabel.setForeground(Color.BLUE);
        timerPanel.add(timeLabel);
        
        timerLabel = new JLabel("15:00"); // ✅ Đã có khai báo ở trên, chỉ cần gán
        timerLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        timerLabel.setForeground(Color.RED);
        timerPanel.add(timerLabel);
        sidebar.add(timerPanel, BorderLayout.NORTH);

        // Danh sách câu hỏi (nav)
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

        // Nút nộp bài
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
    //-------------------------- Lấy thông tin sinh viên------------
    // private List<Map<String, Object>> loadStudentExamData() {
    //     try {
    //         Map<String, Object> params = new HashMap<>();
    //         params.put("action", "get");
    //         //params.put("method", "SELECT");

    //         // Liệt kê các bảng cần join
    //         params.put("table", List.of("account", "student", "classes", "exams"));

    //         // Thiết lập join theo thứ tự (INNER JOIN)
    //         List<Map<String, Object>> joinList = new ArrayList<>();

    //         Map<String, Object> join1 = new HashMap<>();
    //         join1.put("type", "inner");
    //         join1.put("on", List.of("account.id = student.IdAccount"));
    //         joinList.add(join1);

    //         Map<String, Object> join2 = new HashMap<>();
    //         join2.put("type", "inner");
    //         join2.put("on", List.of("student.ClassId = classes.id"));
    //         joinList.add(join2);

    //         Map<String, Object> join3 = new HashMap<>();
    //         join3.put("type", "inner");
    //         join3.put("on", List.of("classes.Id = exams.ClassId"));
    //         joinList.add(join3);

    //         params.put("join", joinList);

    //         // Cột muốn lấy (ở đây lấy toàn bộ)
    //         params.put("columns", List.of("account.FullName", "classes.Name", "exams.ExamName"));

    //         // Gọi API
    //         List<Map<String, Object>> result = apiService.postApiGetList("/autoGet", params);

    //         // Debug
    //         System.out.println("✅ Data loaded from /autoGet: " + result);
    //         return result;

    //     } catch (Exception ex) {
    //         ex.printStackTrace();
    //         JOptionPane.showMessageDialog(this,
    //             "Lỗi khi tải thông tin học sinh và bài kiểm tra!",
    //             "Lỗi API", JOptionPane.ERROR_MESSAGE);
    //         return Collections.emptyList();
    //     }
    // }


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

    // ------------------------- Nav Panel Refresh -------------------------
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

    // ------------------------- Nộp bài -------------------------
    private void submitExam() {
        List<Map<String, Object>> submitData = new ArrayList<>();

        // Duyệt toàn bộ câu hỏi
        for (Question q : questions) {
            Integer ansId = selectedAnswers.get(q.id); // Lấy đáp án mà người dùng chọn (nếu có)

            // Mỗi câu hỏi là 1 bản ghi JSON: có question.id và answers.id (null nếu chưa chọn)
            Map<String, Object> record = new HashMap<>();
            record.put("QuestionId", q.id);
            record.put("AnswerId", ansId != null ? ansId : null); // nếu chưa chọn thì để null

            // Thêm vào danh sách gửi đi
            submitData.add(record);
        }

        // Gói dữ liệu JSON theo định dạng API autoUpdate yêu cầu
        Map<String, Object> params = new HashMap<>();
        params.put("action", "update");
        params.put("method", "UPSERT"); // hoặc "INSERT"/"UPDATE" tuỳ API backend bạn hỗ trợ
        params.put("table", "exam_answers"); // 👈 tự thay tên bảng cần update
        params.put("data", submitData);

        // Gửi request đến API
        List<Map<String, Object>> response = apiService.postApiGetList("/autoUpdate", params);

        // Hiển thị kết quả sau khi nộp
        JOptionPane.showMessageDialog(this,
                "✅ Bạn đã nộp bài thành công!\nDữ liệu đã gửi đến server.",
                "Nộp bài thành công",
                JOptionPane.INFORMATION_MESSAGE);

        // Debug xem dữ liệu gửi đi
        System.out.println("Submitted JSON: " + submitData);
        System.out.println("API Response: " + response.toString());
    }
    // ------------------------- Lưu đáp án mỗi khi chọn -------------------------
    private void saveAnswerToApi(int questionId, int answerId) {
        try {
            Map<String, Object> record = new HashMap<>();
            record.put("QuestionId", questionId);
            record.put("AnswerId", answerId);
            // Nếu có user_id thì thêm vào (từ authService) 

            Map<String, Object> params = new HashMap<>();
            params.put("action", "update");
            params.put("method", "UPSERT"); // hoặc INSERT/UPDATE tùy logic backend
            params.put("table", "exam_answers");
            params.put("data", List.of(record));

            // Gọi API không cần chờ kết quả lớn (chỉ để cập nhật nhanh)
            new Thread(() -> {
                try {
                    List<Map<String, Object>> response = apiService.postApiGetList("/autoUpdate", params);
                    System.out.println("✅ Saved: Q=" + questionId + ", A=" + answerId + " | Response=" + response);
                } catch (Exception ex) {
                    System.err.println("⚠️ Error saving Q" + questionId + ": " + ex.getMessage());
                }
            }).start(); // gọi trong thread riêng để không block UI

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Lỗi khi lưu câu trả lời!", "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ------------------------- Đếm ngược -------------------------
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
        params.put("columns", List.of("questions.id", "questions.Question", "answers.Answer", "answers.id", "answers.IsCorrect"));
        Map<String, Object> join = new HashMap<>();
        join.put("type", "inner");
        join.put("on", List.of("questions.id = answers.QuestionId"));
        params.put("join", List.of(join));
        System.out.println("Loading questions from API with params: " + params);
        List<Map<String, Object>> apiData = apiService.postApiGetList("/autoGet", params);
        System.out.println(apiData);

        if (apiData == null || apiData.isEmpty()) {
            questions.clear();
            totalQuestions = 0;
            renderQuestions();
            return;
        }

        Map<Integer, String> questionTextMap = new LinkedHashMap<>();
        Map<Integer, List<String>> optionsMap = new HashMap<>();
        Map<Integer, List<Integer>> answerIdMap = new HashMap<>();

        for (Map<String, Object> item : apiData) {
            // API trả key là "id", nhưng đây là id của ANSWER => ta cần ánh xạ đúng
            Integer answerId = getFirstInteger(item, "answers.id", "id", "AnswerId");
            String questionText = getFirstString(item, "questions.Question", "Question");
            String answerText = getFirstString(item, "answers.Answer", "Answer");

            // ✅ Lấy QuestionId từ dòng (API không trả rõ, nên nhóm theo Question text)
            Integer questionId = extractQuestionId(item);

            if (questionId == null || questionText == null || answerText == null || answerId == null)
                continue;

            questionTextMap.putIfAbsent(questionId, questionText);
            optionsMap.computeIfAbsent(questionId, k -> new ArrayList<>()).add(answerText);
            answerIdMap.computeIfAbsent(questionId, k -> new ArrayList<>()).add(answerId);
            }

        questions.clear();
        for (Map.Entry<Integer, String> e : questionTextMap.entrySet()) {
            int qId = e.getKey();
            questions.add(new Question(qId, e.getValue(), optionsMap.get(qId), answerIdMap.get(qId)));
        }

        totalQuestions = questions.size();
        Collections.shuffle(questions, new Random());

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

        renderQuestions();
    }

    // ------------------------- Helpers -------------------------
    private Integer extractQuestionId(Map<String, Object> item) {
        // API chỉ có "id" (thực tế là answer id), ta có thể lấy QuestionId từ join
        Object qIdObj = item.get("questions.id");
        if (qIdObj != null) return Integer.parseInt(qIdObj.toString());

        // Một số API chỉ trả "id" và "Question" mà không rõ key -> gán tạm theo AnswerId /4 (có thể sửa)
        Object idObj = item.get("id");
        if (idObj instanceof Number) {
            int id = ((Number) idObj).intValue();
            // Nếu mỗi câu hỏi có 4 đáp án thì chia 4 để nhóm, ví dụ: 81–84 = 1 question
            return ((id - 81) / 4) + 1; 
        }
        return null;
    }

    // ------------------------- Helpers -------------------------
    private String getFirstString(Map<String, Object> map, String... keys) {
        for (String k : keys) {
            Object v = map.get(k);
            if (v != null) return v.toString();
        }
        return null;
    }

    private Integer getFirstInteger(Map<String, Object> map, String... keys) {
        for (String k : keys) {
            Object v = map.get(k);
            if (v == null) continue;
            if (v instanceof Number) return ((Number) v).intValue();
            try {
                return Integer.parseInt(v.toString());
            } catch (NumberFormatException ignored) {}
        }
        return null;
    }

    // ------------------------- Main -------------------------
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (Exception e) {
            e.printStackTrace();
        }
        //SwingUtilities.invokeLater(QuizAppSwing::new);
        // ApiService apiService = new ApiService();
        // AuthService authService = new AuthService();
        // MainWindow mainWindow = new MainWindow(apiService, authService); // nếu có

        // SwingUtilities.invokeLater(() -> new QuizAppSwing(apiService, authService, mainWindow));
    }
}