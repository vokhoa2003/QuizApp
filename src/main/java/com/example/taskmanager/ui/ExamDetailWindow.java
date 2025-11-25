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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;

import com.example.taskmanager.service.ApiService;
import com.example.taskmanager.service.AuthService;

public class ExamDetailWindow extends JFrame {
    private ApiService apiService;
    private AuthService authService;
    private int examId;
    private int studentId;
    private String studentName;
    private String examName;
    private double finalScore;
    
    private JPanel questionsContainer;
    private JScrollPane scrollPane;
    private JLabel scoreLabel;
    private JLabel summaryLabel;
    
    public ExamDetailWindow(ApiService apiService, AuthService authService,
                           int examId, int studentId, String studentName,
                           String examName, double finalScore) {
        this.apiService = apiService;
        this.authService = authService;
        this.examId = examId;
        this.studentId = studentId;
        this.studentName = studentName;
        this.examName = examName;
        this.finalScore = finalScore;
        
        setTitle("Chi Tiết Bài Kiểm Tra - " + examName);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1100, 750);
        setLocationRelativeTo(null);
        System.out.println("DEBUG: Opening ExamDetailWindow for examId=" + examId + ", studentId=" + studentId);
        
        initUI();
        loadExamDetail();
        
        setVisible(true);
    }
    
    private void initUI() {
        JPanel mainPanel = new JPanel(new BorderLayout(0, 0));
        mainPanel.setBackground(new Color(0xF8F9FA));
        
        // Header
        JPanel headerPanel = createHeaderPanel();
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        
        // Content
        JPanel contentPanel = new JPanel(new BorderLayout(15, 15));
        contentPanel.setBackground(new Color(0xF8F9FA));
        contentPanel.setBorder(new EmptyBorder(20, 30, 20, 30));
        
        // Info panel
        JPanel infoPanel = createInfoPanel();
        contentPanel.add(infoPanel, BorderLayout.NORTH);
        
        // Questions container
        questionsContainer = new JPanel();
        questionsContainer.setLayout(new BoxLayout(questionsContainer, BoxLayout.Y_AXIS));
        questionsContainer.setBackground(Color.WHITE);
        questionsContainer.setBorder(new EmptyBorder(15, 15, 15, 15));
        
        scrollPane = new JScrollPane(questionsContainer);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(0xE5E7EB), 1));
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        contentPanel.add(scrollPane, BorderLayout.CENTER);
        
        mainPanel.add(contentPanel, BorderLayout.CENTER);
        
        add(mainPanel);
    }
    
    private JPanel createHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout());
        header.setPreferredSize(new Dimension(0, 70));
        header.setBackground(new Color(0x0284C7));
        header.setBorder(new EmptyBorder(15, 30, 15, 30));
        
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        leftPanel.setOpaque(false);
        
        JButton backBtn = new JButton("← Quay Lại");
        backBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        backBtn.setForeground(new Color(0x0284C7));
        backBtn.setBackground(Color.WHITE);
        backBtn.setBorderPainted(false);
        backBtn.setFocusPainted(false);
        backBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        backBtn.setPreferredSize(new Dimension(110, 35));
        backBtn.addActionListener(e -> dispose());
        leftPanel.add(backBtn);
        
        JLabel titleLabel = new JLabel("Chi Tiết Bài Kiểm Tra");
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
        
        JLabel examLabel = new JLabel("Bài kiểm tra: " + examName);
        examLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        examLabel.setForeground(new Color(0x1F2937));
        examLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel studentLabel = new JLabel("Học sinh: " + studentName);
        studentLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        studentLabel.setForeground(new Color(0x6B7280));
        studentLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        scoreLabel = new JLabel(String.format("Điểm số: %.1f/10", finalScore));
        scoreLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        scoreLabel.setForeground(getScoreColor(finalScore));
        scoreLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        summaryLabel = new JLabel("Đang tải...");
        summaryLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        summaryLabel.setForeground(new Color(0x6B7280));
        summaryLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        panel.add(examLabel);
        panel.add(Box.createVerticalStrut(5));
        panel.add(studentLabel);
        panel.add(Box.createVerticalStrut(8));
        panel.add(scoreLabel);
        panel.add(Box.createVerticalStrut(5));
        panel.add(summaryLabel);
        
        return panel;
    }
    
    private Color getScoreColor(double score) {
        if (score >= 9.0) return new Color(0x059669);
        if (score >= 8.0) return new Color(0x0284C7);
        if (score >= 6.5) return new Color(0xD97706);
        return new Color(0xDC2626);
    }
    
    private void loadExamDetail() {
        SwingWorker<Map<String, Object>, Void> worker = new SwingWorker<>() {
            @Override
            protected Map<String, Object> doInBackground() {
                Map<String, Object> result = new HashMap<>();
                try {
                    // 1) Lấy tất cả câu hỏi + đáp án của đề thi
                    Map<String, Object> params1 = new HashMap<>();
                    params1.put("action", "get");
                    params1.put("method", "SELECT");
                    params1.put("table", List.of("questions", "answers", "exam_answers", "exams", "exam_attempts"));
                    params1.put("columns", List.of(
                        "questions.id as QuestionId",
                        "questions.Question as QuestionText",
                        "answers.id as AnswerId",
                        "answers.Answer as AnswerText",
                        "answers.IsCorrect as IsCorrect"
                    ));
                    Map<String, Object> join1 = new HashMap<>();
                    join1.put("type", "inner");
                    join1.put("on", List.of("questions.id = answers.QuestionId"));


                    Map<String, Object> join2 = new HashMap<>();
                    join2.put("type", "inner");
                    join2.put("on", List.of("answers.id = exam_answers.AnswerId"));

                    Map<String, Object> join3 = new HashMap<>();
                    join3.put("type", "inner");
                    join3.put("on", List.of("exam_answers.AttemptId = exam_attempts.id"));

                    Map<String, Object> join4 = new HashMap<>();
                    join4.put("type", "inner");
                    join4.put("on", List.of("exam_attempts.ExamId = exams.id"));

                    params1.put("join", List.of(join1, join2, join3, join4));

                    Map<String, Object> where1 = new HashMap<>();
                    where1.put("exam_attempts.ExamId", examId);
                    where1.put("exam_attempts.StudentId", studentId);
                    params1.put("where", where1);

                    Object qResp = apiService.postApiGetList("/autoGet", params1);
                    System.out.println("DEBUG: raw questions response = " + Objects.toString(qResp));
                    List<Map<String, Object>> questionsData = normalizeApiList(qResp);
                    System.out.println("DEBUG: normalized questions count = " + (questionsData != null ? questionsData.size() : 0));
                    if (questionsData != null && !questionsData.isEmpty()) {
                        System.out.println("DEBUG: sample question row = " + questionsData.get(0));
                    }
                    result.put("questions", questionsData != null ? questionsData : Collections.emptyList());

                    // 2) Tìm attempt/latest của học sinh cho exam này
                    Integer attemptId = null;
                    try {
                        Map<String, Object> p = new HashMap<>();
                        p.put("action", "get");
                        p.put("method", "SELECT");
                        p.put("table", "exam_attempts");
                        p.put("columns", List.of("id", "Status"));
                        Map<String, Object> w = new HashMap<>();
                        w.put("ExamId", examId);
                        w.put("StudentId", studentId);
                        p.put("where", w);
                        p.put("order", "id DESC");
                        p.put("limit", 1);

                        Object aResp = apiService.postApiGetList("/autoGet", p);
                        System.out.println("DEBUG: raw attempts response = " + Objects.toString(aResp));
                        List<Map<String, Object>> attempts = normalizeApiList(aResp);
                        if (attempts != null && !attempts.isEmpty()) {
                            System.out.println("DEBUG: normalized attempts = " + attempts);
                            Object idObj = firstNonNull(attempts.get(0), "id", "Id");
                            if (idObj instanceof Number) attemptId = ((Number) idObj).intValue();
                            else if (idObj != null) attemptId = Integer.parseInt(idObj.toString());
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }

                    // 3) Lấy đáp án của học sinh theo AttemptId (nếu không có, fallback bằng ExamId+StudentId)
                    List<Map<String, Object>> answersData = Collections.emptyList();
                    if (attemptId != null) {
                        Map<String, Object> params2 = new HashMap<>();
                        params2.put("action", "get");
                        params2.put("method", "SELECT");
                        params2.put("table", List.of("exam_answers"));
                        params2.put("columns", List.of("exam_answers.QuestionId as QuestionId", "exam_answers.AnswerId as StudentAnswerId"));
                        Map<String, Object> where2 = new HashMap<>();
                        where2.put("exam_answers.AttemptId", attemptId);
                        where2.put("exam_answers.StudentId", studentId);
                        params2.put("where", where2);
                        Object a2Resp = apiService.postApiGetList("/autoGet", params2);
                        System.out.println("DEBUG: raw answers-by-attempt response = " + Objects.toString(a2Resp));
                        answersData = normalizeApiList(a2Resp);
                    }

                    if (answersData == null || answersData.isEmpty()) {
                        // fallback legacy
                        Map<String, Object> params2 = new HashMap<>();
                        params2.put("action", "get");
                        params2.put("method", "SELECT");
                        params2.put("table", List.of("exam_answers"));
                        params2.put("columns", List.of("exam_answers.QuestionId as QuestionId", "exam_answers.AnswerId as StudentAnswerId"));
                        Map<String, Object> where2 = new HashMap<>();
                        where2.put("exam_answers.ExamId", examId);
                        where2.put("exam_answers.StudentId", studentId);
                        params2.put("where", where2);

                        Object a2Resp = apiService.postApiGetList("/autoGet", params2);
                        System.out.println("DEBUG: raw answers-legacy response = " + Objects.toString(a2Resp));
                        answersData = normalizeApiList(a2Resp);
                    }

                    System.out.println("DEBUG: normalized studentAnswers count = " + (answersData != null ? answersData.size() : 0));
                    if (answersData != null && !answersData.isEmpty()) System.out.println("DEBUG: sample answer row = " + answersData.get(0));
                    result.put("studentAnswers", answersData != null ? answersData : Collections.emptyList());

                    // FALLBACK: nếu questionsData rỗng nhưng đã có studentAnswers -> load questions theo danh sách QuestionId
                    if ((questionsData == null || questionsData.isEmpty()) && answersData != null && !answersData.isEmpty()) {
                        try {
                            // Lấy danh sách questionId từ answersData
                            List<Integer> qIds = new ArrayList<>();
                            for (Map<String,Object> ar : answersData) {
                                Object qObj = firstNonNull(ar, "QuestionId", "questionid", "exam_answers.QuestionId");
                                if (qObj == null) continue;
                                try {
                                    int qid = (qObj instanceof Number) ? ((Number) qObj).intValue() : Integer.parseInt(qObj.toString());
                                    if (!qIds.contains(qid)) qIds.add(qid);
                                } catch (Exception ignored) {}
                            }

                            if (!qIds.isEmpty()) {
                                Map<String, Object> paramsQ = new HashMap<>();
                                paramsQ.put("action", "get");
                                paramsQ.put("method", "SELECT");
                                paramsQ.put("table", List.of("questions", "answers"));
                                paramsQ.put("columns", List.of(
                                    "questions.id as QuestionId",
                                    "questions.Question as QuestionText",
                                    "answers.id as AnswerId",
                                    "answers.Answer as AnswerText",
                                    "answers.IsCorrect as IsCorrect"
                                ));

                                Map<String, Object> joinQ = new HashMap<>();
                                joinQ.put("type", "inner");
                                joinQ.put("on", List.of("questions.id = answers.QuestionId"));
                                paramsQ.put("join", List.of(joinQ));

                                Map<String, Object> whereQ = new HashMap<>();
                                // backend của bạn chấp nhận list để làm IN
                                whereQ.put("questions.id", qIds);
                                paramsQ.put("where", whereQ);

                                Object q2Resp = apiService.postApiGetList("/autoGet", paramsQ);
                                System.out.println("DEBUG: fallback raw questions-by-ids response = " + Objects.toString(q2Resp));
                                List<Map<String,Object>> q2List = normalizeApiList(q2Resp);
                                System.out.println("DEBUG: fallback normalized questions count = " + (q2List != null ? q2List.size() : 0));
                                if (q2List != null && !q2List.isEmpty()) {
                                    questionsData = q2List;
                                    // cập nhật result để dùng sau
                                    result.put("questions", questionsData);
                                }
                            }
                        } catch (Exception fe) {
                            System.err.println("⚠️ fallback fetch questions error: " + fe.getMessage());
                        }
                    }
                    
                    // 4) Lấy điểm đã lưu (nếu có) từ exam_results và trả về
                    try {
                        Map<String, Object> rparams = new HashMap<>();
                        rparams.put("action", "get");
                        rparams.put("method", "SELECT");
                        rparams.put("table", "exam_results");
                        rparams.put("columns", List.of("Score"));
                        Map<String, Object> w2 = new HashMap<>();
                        w2.put("ExamId", examId);
                        w2.put("StudentId", studentId);
                        rparams.put("where", w2);
                        Object rResp = apiService.postApiGetList("/autoGet", rparams);
                        System.out.println("DEBUG: raw exam_results response = " + Objects.toString(rResp));
                        List<Map<String, Object>> rlist = normalizeApiList(rResp);
                        if (rlist != null && !rlist.isEmpty()) {
                            Object sc = firstNonNull(rlist.get(0), "Score", "score");
                            if (sc != null) {
                                double savedScore = 0.0;
                                if (sc instanceof Number) savedScore = ((Number) sc).doubleValue();
                                else savedScore = Double.parseDouble(sc.toString());
                                result.put("savedScore", savedScore);
                            }
                        }
                    } catch (Exception ex) {
                        System.err.println("Cannot read saved score: " + ex.getMessage());
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
                return result;
            }

            @Override
            protected void done() {
                try {
                    Map<String, Object> data = get();
                    List<Map<String, Object>> questionsData =
                        (List<Map<String, Object>>) data.getOrDefault("questions", Collections.emptyList());
                    List<Map<String, Object>> studentAnswers =
                        (List<Map<String, Object>>) data.getOrDefault("studentAnswers", Collections.emptyList());

                    // Nếu có savedScore thì override finalScore trước khi hiển thị
                    Object savedScoreObj = data.get("savedScore");
                    if (savedScoreObj instanceof Number) {
                        finalScore = ((Number) savedScoreObj).doubleValue();
                        scoreLabel.setText(String.format("⭐ Điểm số: %.1f/10", finalScore));
                        scoreLabel.setForeground(getScoreColor(finalScore));
                    }

                    if (questionsData == null || questionsData.isEmpty()) {
                        questionsContainer.removeAll();
                        JLabel empty = new JLabel("Không có dữ liệu câu hỏi để hiển thị.");
                        empty.setFont(new Font("Segoe UI", Font.PLAIN, 14));
                        empty.setForeground(new Color(0x6B7280));
                        questionsContainer.add(empty);
                        questionsContainer.revalidate();
                        questionsContainer.repaint();
                        System.out.println("INFO: No questions to display.");
                        return;
                    }

                    displayQuestions(questionsData, studentAnswers);
                } catch (Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(ExamDetailWindow.this,
                        "Lỗi khi tải chi tiết bài kiểm tra!\n" + e.getMessage(),
                        "Lỗi",
                        JOptionPane.ERROR_MESSAGE);
                }
            }

            // Thêm helper chuẩn hoá response API ở cấp lớp (nếu đã có trong inner class thì có thể giữ, nhưng tốt hơn là có 1 method chung)
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
                    // support numeric-keyed map { "0": {...}, "1": {...} }
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
        };
        worker.execute();
    }

    private void displayQuestions(List<Map<String, Object>> questionsData,
                                  List<Map<String, Object>> studentAnswers) {
        questionsContainer.removeAll();

        // 1) Group raw rows by questionId
        Map<Integer, List<Map<String, Object>>> rowsByQuestion = new LinkedHashMap<>();
        for (Map<String, Object> row : questionsData) {
            int qid = getIntFromRow(row, "QuestionId", "questions.id", "questions.QuestionId");
            if (qid == 0) continue;
            rowsByQuestion.computeIfAbsent(qid, k -> new ArrayList<>()).add(row);
        }

        // 2) Build studentAnswer map and collect questionIds from answers (preserve order)
        Map<Integer, Integer> studentAnswerMap = new LinkedHashMap<>();
        for (Map<String, Object> row : studentAnswers) {
            int qId = getIntFromRow(row, "QuestionId", "exam_answers.QuestionId", "exam_answers.questionid");
            Integer aId = null;
            Object aObj = firstNonNull(row, "StudentAnswerId", "AnswerId", "exam_answers.AnswerId");
            if (aObj instanceof Number) aId = ((Number) aObj).intValue();
            else if (aObj != null) {
                try { aId = Integer.parseInt(aObj.toString()); } catch (Exception ignored) {}
            }
            if (qId != 0) {
                studentAnswerMap.put(qId, aId); // aId may be null -> "chưa làm"
            }
        }

        // Debug
        System.out.println("DEBUG: questionsData distinct QIDs = " + rowsByQuestion.keySet());
        System.out.println("DEBUG: studentAnswers QIDs = " + studentAnswerMap.keySet());

        // 3) Determine questionIds to display:
        // Prefer ONLY the studentAnswerMap keys (this avoids extra phantom questions).
        List<Integer> questionIdsToDisplay = new ArrayList<>(studentAnswerMap.keySet());

        // Fallback: if studentAnswerMap empty, use rowsByQuestion keys
        if (questionIdsToDisplay.isEmpty()) {
            questionIdsToDisplay.addAll(rowsByQuestion.keySet());
        }

        // 4) Ensure we have answer rows for each qid; try to fetch missing ones
        for (int i = 0; i < questionIdsToDisplay.size(); i++) {
            Integer qid = questionIdsToDisplay.get(i);
            if (!rowsByQuestion.containsKey(qid)) {
                try {
                    Map<String, Object> paramsQ = new HashMap<>();
                    paramsQ.put("action", "get");
                    paramsQ.put("method", "SELECT");
                    paramsQ.put("table", List.of("questions", "answers"));
                    paramsQ.put("columns", List.of(
                        "questions.id as QuestionId",
                        "questions.Question as QuestionText",
                        "answers.id as AnswerId",
                        "answers.Answer as AnswerText",
                        "answers.IsCorrect as IsCorrect"
                    ));
                    Map<String, Object> joinQ = new HashMap<>();
                    joinQ.put("type", "inner");
                    joinQ.put("on", List.of("questions.id = answers.QuestionId"));
                    paramsQ.put("join", List.of(joinQ));
                    Map<String, Object> whereQ = new HashMap<>();
                    whereQ.put("questions.id", qid);
                    paramsQ.put("where", whereQ);

                    Object resp = apiService.postApiGetList("/autoGet", paramsQ);
                    List<Map<String, Object>> fetched = normalizeApiList(resp);
                    if (fetched != null && !fetched.isEmpty()) {
                        rowsByQuestion.put(qid, fetched);
                        System.out.println("DEBUG: fetched answers for qid=" + qid + " rows=" + fetched.size());
                    } else {
                        System.out.println("WARN: no answer rows fetched for qid=" + qid);
                    }
                } catch (Exception ex) {
                    System.err.println("⚠️ Error fetching answers for qid=" + qid + ": " + ex.getMessage());
                }
            }
        }

        // 5) Build QuestionData only for selected ids (preserve student order)
        List<QuestionData> orderedQuestions = new ArrayList<>();
        for (Integer qid : questionIdsToDisplay) {
            List<Map<String, Object>> rows = rowsByQuestion.get(qid);
            if (rows == null || rows.isEmpty()) {
                // If still missing, skip to avoid phantom entries
                System.out.println("WARN: skipping qid (no rows): " + qid);
                continue;
            }
            QuestionData qData = new QuestionData();
            qData.questionId = qid;
            qData.questionText = getStringFromRow(rows.get(0), "QuestionText", "Question", "questions.Question");
            qData.answers = new ArrayList<>();
            for (Map<String, Object> r : rows) {
                AnswerData aData = new AnswerData();
                aData.answerId = getIntFromRow(r, "AnswerId", "answers.id");
                aData.answerText = getStringFromRow(r, "AnswerText", "Answer", "answers.Answer");
                Object isCorrectObj = firstNonNull(r, "IsCorrect", "answers.IsCorrect", "answers.IsCorrect");
                aData.isCorrect = isCorrectObj != null && (isCorrectObj.equals(1) || isCorrectObj.equals(true) || "1".equals(isCorrectObj.toString()));
                qData.answers.add(aData);
            }
            orderedQuestions.add(qData);
        }

        // 6) Render and compute stats
        int questionNumber = 1;
        int correctCount = 0;
        int unansweredCount = 0;
        int totalQuestions = orderedQuestions.size(); // should match studentAnswerMap.size() ideally

        for (QuestionData qData : orderedQuestions) {
            Integer studentAnswerId = studentAnswerMap.get(qData.questionId);
            boolean isQuestionCorrect = false;

            if (studentAnswerId == null) {
                unansweredCount++;
            } else {
                for (AnswerData aData : qData.answers) {
                    if (aData.answerId == studentAnswerId && aData.isCorrect) {
                        isQuestionCorrect = true;
                        break;
                    }
                }
                if (isQuestionCorrect) correctCount++;
            }

            JPanel questionPanel = createQuestionPanel(
                questionNumber++, qData, studentAnswerId, isQuestionCorrect
            );
            questionsContainer.add(questionPanel);
            questionsContainer.add(Box.createVerticalStrut(15));
        }

        int wrong = totalQuestions - correctCount - unansweredCount;
        summaryLabel.setText(String.format("Đúng: %d | Sai: %d | Chưa làm: %d (Tổng: %d câu)", correctCount, wrong, unansweredCount, totalQuestions));
        double score = totalQuestions > 0 ? Math.round((correctCount * 10.0 / totalQuestions) * 10.0) / 10.0 : 0.0;
        scoreLabel.setText(String.format("Điểm số: %.1f/10", score));
        scoreLabel.setForeground(getScoreColor(score));

        questionsContainer.revalidate();
        questionsContainer.repaint();
    }

    // Utility helpers (thêm vào để tránh lỗi "cannot find symbol")
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
            // support numeric-keyed map { "0": {...}, "1": {...} }
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

    private String getStringFromRow(Map<String, Object> row, String... keys) {
        if (row == null) return "";
        for (String k : keys) {
            if (k == null) continue;
            Object v = row.get(k);
            if (v == null) v = row.get(k.toLowerCase());
            if (v != null) return v.toString();
        }
        return "";
    }

    private int getIntFromRow(Map<String, Object> row, String... keys) {
        if (row == null) return 0;
        for (String k : keys) {
            if (k == null) continue;
            Object v = row.get(k);
            if (v == null) v = row.get(k.toLowerCase());
            if (v instanceof Number) return ((Number) v).intValue();
            if (v != null) {
                try { return Integer.parseInt(v.toString()); } catch (Exception ignored) {}
            }
        }
        return 0;
    }

    private Object firstNonNull(Map<String, Object> row, String... keys) {
        if (row == null) return null;
        for (String k : keys) {
            if (k == null) continue;
            Object v = row.get(k);
            if (v == null) v = row.get(k.toLowerCase());
            if (v != null) return v;
        }
        return null;
    }
    
    private JPanel createQuestionPanel(int questionNumber, QuestionData qData,
                                       Integer studentAnswerId, boolean isCorrect) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0xE5E7EB), 1),
            new EmptyBorder(15, 15, 15, 15)
        ));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Question header with result indicator
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JLabel questionLabel = new JLabel(
            String.format("Câu %d: %s", questionNumber, qData.questionText)
        );
        questionLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));
        questionLabel.setForeground(new Color(0x1F2937));

        JLabel resultLabel;
        if (studentAnswerId == null) {
            resultLabel = new JLabel("Chưa làm");
            resultLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
            resultLabel.setForeground(new Color(0x6B7280)); // gray
        } else {
            resultLabel = new JLabel(isCorrect ? "Đúng" : "Sai");
            resultLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
            resultLabel.setForeground(isCorrect ? new Color(0x059669) : new Color(0xDC2626));
        }

        headerPanel.add(questionLabel, BorderLayout.WEST);
        headerPanel.add(resultLabel, BorderLayout.EAST);

        panel.add(headerPanel);
        panel.add(Box.createVerticalStrut(12));

        // Answers
        char[] labels = {'A', 'B', 'C', 'D'};
        int answerIndex = 0;

        for (AnswerData aData : qData.answers) {
            boolean isStudentAnswer = (studentAnswerId != null && studentAnswerId == aData.answerId);
            JPanel answerPanel = createAnswerPanel(
                labels[Math.min(answerIndex++, labels.length - 1)],
                aData,
                isStudentAnswer
            );
            panel.add(answerPanel);
            panel.add(Box.createVerticalStrut(8));
        }

        return panel;
    }
    
    private JPanel createAnswerPanel(char label, AnswerData aData, boolean isStudentAnswer) {
        JPanel panel = new JPanel(new BorderLayout(10, 0));
        panel.setOpaque(false);
        
        // Left: Label + Icon
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        leftPanel.setOpaque(false);
        
        JLabel labelText = new JLabel(label + ".");
        labelText.setFont(new Font("Segoe UI", Font.BOLD, 14));
        labelText.setPreferredSize(new Dimension(25, 20));
        leftPanel.add(labelText);
        
        // Show icon for correct answer or student's wrong answer
        String icon = "";
        Color iconColor = Color.BLACK;
        
        if (aData.isCorrect) {
            icon = "✓"; // This is the correct answer
            iconColor = new Color(0x059669);
        } else if (isStudentAnswer) {
            icon = "✗"; // Student selected this wrong answer
            iconColor = new Color(0xDC2626);
        }
        
        if (!icon.isEmpty()) {
            JLabel iconLabel = new JLabel(icon);
            iconLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
            iconLabel.setForeground(iconColor);
            leftPanel.add(iconLabel);
        }
        
        panel.add(leftPanel, BorderLayout.WEST);
        
        // Center: Answer text
        JLabel answerText = new JLabel(aData.answerText);
        answerText.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        
        // Highlight styling
        if (aData.isCorrect) {
            answerText.setForeground(new Color(0x059669));
            panel.setBackground(new Color(0xD1FAE5));
            panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0x059669), 2),
                new EmptyBorder(8, 10, 8, 10)
            ));
        } else if (isStudentAnswer) {
            answerText.setForeground(new Color(0xDC2626));
            panel.setBackground(new Color(0xFEE2E2));
            panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xDC2626), 2),
                new EmptyBorder(8, 10, 8, 10)
            ));
        } else {
            answerText.setForeground(new Color(0x6B7280));
            panel.setBackground(new Color(0xF9FAFB));
            panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xE5E7EB), 1),
                new EmptyBorder(8, 10, 8, 10)
            ));
        }
        
        panel.add(answerText, BorderLayout.CENTER);
        
        return panel;
    }
    
    // Helper classes
    private static class QuestionData {
        int questionId;
        String questionText;
        List<AnswerData> answers;
    }
    
    private static class AnswerData {
        int answerId;
        String answerText;
        boolean isCorrect;
    }
}