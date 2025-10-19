package com.example.taskmanager.ui;

import java.awt.*;
import java.util.*;
import java.util.List;
import javax.swing.*;
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
        
        JLabel titleLabel = new JLabel("📋 Chi Tiết Bài Kiểm Tra");
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
        
        JLabel examLabel = new JLabel("📝 Bài kiểm tra: " + examName);
        examLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        examLabel.setForeground(new Color(0x1F2937));
        examLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel studentLabel = new JLabel("👤 Học sinh: " + studentName);
        studentLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        studentLabel.setForeground(new Color(0x6B7280));
        studentLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        scoreLabel = new JLabel(String.format("⭐ Điểm số: %.1f/10", finalScore));
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
                
                // Step 1: Get all questions in this exam with their answers
                Map<String, Object> params1 = new HashMap<>();
                params1.put("action", "get");
                params1.put("method", "SELECT");
                params1.put("table", List.of("exam_questions", "questions", "answers"));
                params1.put("columns", List.of(
                    "questions.id as QuestionId",
                    "questions.Question",
                    "answers.id as AnswerId",
                    "answers.Answer",
                    "answers.IsCorrect"
                ));
                
                Map<String, Object> join1 = new HashMap<>();
                join1.put("type", "inner");
                join1.put("on", List.of("exam_questions.QuestionId = questions.id"));
                
                Map<String, Object> join2 = new HashMap<>();
                join2.put("type", "inner");
                join2.put("on", List.of("questions.id = answers.QuestionId"));
                
                params1.put("join", List.of(join1, join2));
                
                Map<String, Object> where1 = new HashMap<>();
                where1.put("exam_questions.ExamId", examId);
                params1.put("where", where1);
                
                List<Map<String, Object>> questionsData = apiService.postApiGetList("/autoGet", params1);
                result.put("questions", questionsData);
                
                // Step 2: Get student's selected answers
                Map<String, Object> params2 = new HashMap<>();
                params2.put("action", "get");
                params2.put("method", "SELECT");
                params2.put("table", List.of("exam_answers"));
                params2.put("columns", List.of(
                    "exam_answers.QuestionId",
                    "exam_answers.AnswerId as StudentAnswerId"
                ));
                
                Map<String, Object> where2 = new HashMap<>();
                where2.put("exam_answers.ExamId", examId);
                where2.put("exam_answers.StudentId", studentId);
                params2.put("where", where2);
                
                List<Map<String, Object>> answersData = apiService.postApiGetList("/autoGet", params2);
                result.put("studentAnswers", answersData);
                
                return result;
            }
            
            @Override
            protected void done() {
                try {
                    Map<String, Object> data = get();
                    List<Map<String, Object>> questionsData = 
                        (List<Map<String, Object>>) data.get("questions");
                    List<Map<String, Object>> studentAnswers = 
                        (List<Map<String, Object>>) data.get("studentAnswers");
                    
                    displayQuestions(questionsData, studentAnswers);
                    
                } catch (Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(ExamDetailWindow.this,
                        "Lỗi khi tải chi tiết bài kiểm tra!\n" + e.getMessage(),
                        "Lỗi",
                        JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }
    
    private void displayQuestions(List<Map<String, Object>> questionsData,
                                  List<Map<String, Object>> studentAnswers) {
        questionsContainer.removeAll();
        
        // Group data by question
        Map<Integer, QuestionData> questionsMap = new LinkedHashMap<>();
        
        for (Map<String, Object> row : questionsData) {
            Object qIdObj = row.get("QuestionId");
            int questionId = qIdObj != null ? Integer.parseInt(qIdObj.toString()) : 0;
            
            QuestionData qData = questionsMap.get(questionId);
            if (qData == null) {
                qData = new QuestionData();
                qData.questionId = questionId;
                qData.questionText = row.get("questions.Question") != null ? 
                    row.get("questions.Question").toString() : "";
                qData.answers = new ArrayList<>();
                questionsMap.put(questionId, qData);
            }
            
            AnswerData aData = new AnswerData();
            Object aIdObj = row.get("AnswerId");
            aData.answerId = aIdObj != null ? Integer.parseInt(aIdObj.toString()) : 0;
            aData.answerText = row.get("answers.Answer") != null ? 
                row.get("answers.Answer").toString() : "";
            Object isCorrectObj = row.get("answers.IsCorrect");
            aData.isCorrect = isCorrectObj != null && 
                (isCorrectObj.equals(1) || isCorrectObj.equals(true) || isCorrectObj.equals("1"));
            
            qData.answers.add(aData);
        }
        
        // Get student's selected answers
        Map<Integer, Integer> studentAnswerMap = new HashMap<>();
        for (Map<String, Object> row : studentAnswers) {
            Object qIdObj = row.get("exam_answers.QuestionId");
            Object aIdObj = row.get("StudentAnswerId");
            
            if (qIdObj != null && aIdObj != null) {
                int qId = Integer.parseInt(qIdObj.toString());
                int aId = Integer.parseInt(aIdObj.toString());
                studentAnswerMap.put(qId, aId);
            }
        }
        
        // Display questions
        int questionNumber = 1;
        int correctCount = 0;
        int totalQuestions = questionsMap.size();
        
        for (QuestionData qData : questionsMap.values()) {
            Integer studentAnswerId = studentAnswerMap.get(qData.questionId);
            boolean isQuestionCorrect = false;
            
            // Check if student's answer is correct
            if (studentAnswerId != null) {
                for (AnswerData aData : qData.answers) {
                    if (aData.answerId == studentAnswerId && aData.isCorrect) {
                        isQuestionCorrect = true;
                        correctCount++;
                        break;
                    }
                }
            }
            
            JPanel questionPanel = createQuestionPanel(
                questionNumber++, qData, studentAnswerId, isQuestionCorrect
            );
            questionsContainer.add(questionPanel);
            questionsContainer.add(Box.createVerticalStrut(15));
        }
        
        // Update summary
        summaryLabel.setText(String.format(
            "✅ Đúng: %d/%d câu | ❌ Sai: %d/%d câu", 
            correctCount, totalQuestions, 
            totalQuestions - correctCount, totalQuestions
        ));
        
        questionsContainer.revalidate();
        questionsContainer.repaint();
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
        
        JLabel resultLabel = new JLabel(isCorrect ? "✅ Đúng" : "❌ Sai");
        resultLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        resultLabel.setForeground(isCorrect ? new Color(0x059669) : new Color(0xDC2626));
        
        headerPanel.add(questionLabel, BorderLayout.WEST);
        headerPanel.add(resultLabel, BorderLayout.EAST);
        
        panel.add(headerPanel);
        panel.add(Box.createVerticalStrut(12));
        
        // Answers
        char[] labels = {'A', 'B', 'C', 'D'};
        int answerIndex = 0;
        
        for (AnswerData aData : qData.answers) {
            JPanel answerPanel = createAnswerPanel(
                labels[Math.min(answerIndex++, labels.length - 1)],
                aData,
                studentAnswerId != null && studentAnswerId == aData.answerId
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