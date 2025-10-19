package com.example.taskmanager.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;

import com.example.taskmanager.service.ApiService;
import com.example.taskmanager.service.AuthService;


public class QuizCreatorAppSwing extends JFrame {
    private ApiService apiService;
    private AuthService authService;
    // Models

    public static class Question {
        private String questionText;
        private List<String> answers;
        private int correctAnswer;
        private int questionNumber;

        public Question(int questionNumber) {
            this.questionNumber = questionNumber;
            this.questionText = "";
            this.answers = new ArrayList<>();
            for (int i = 0; i < 4; i++) answers.add("");
            this.correctAnswer = 0;
        }
        public String getQuestionText() { return questionText; }
        public void setQuestionText(String questionText) { this.questionText = questionText; }
        public List<String> getAnswers() { return answers; }
        public int getCorrectAnswer() { return correctAnswer; }
        public void setCorrectAnswer(int correctAnswer) { this.correctAnswer = correctAnswer; }
        public int getQuestionNumber() { return questionNumber; }
        public void setQuestionNumber(int n) { this.questionNumber = n; }
    }

    // UI Components
    private JTextField examCodeField;
    private JTextArea descriptionArea;
    private JComboBox<String> gradeComboBox;
    private JPanel questionsContainer;
    private JScrollPane questionsScrollPane;
    private JLabel questionCountLabel;
    private JButton addQuestionBtn;
    private JButton saveExamBtn;
    // thêm spinner chọn ngày giờ publish
    private javax.swing.JSpinner publishDateSpinner;
    // thêm spinner cho ngày giờ kết thúc
    private javax.swing.JSpinner endDateSpinner;

    // Data
    private List<Question> questions;
    private int questionCounter = 1;

    public QuizCreatorAppSwing(ApiService apiService, AuthService authService, MainWindow mainWindow) {
        this.apiService = apiService;
        this.authService = authService;
        setTitle("Tạo Đề Thi Trắc Nghiệm - SecureStudy");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(900, 700);
        setLocationRelativeTo(null);
        initData();
        initUI();
    }

    private void initData() {
        questions = new ArrayList<>();
        questions.add(new Question(questionCounter));
    }

    private void initUI() {
        JPanel main = new JPanel(new BorderLayout(20, 20));
        main.setBorder(new EmptyBorder(20, 20, 20, 20));
        main.setBackground(new Color(0xF5F7FA));

        // Header
        JLabel header = new JLabel("📚 TẠO ĐỀ THI TRẮC NGHIỆM");
        header.setFont(header.getFont().deriveFont(Font.BOLD, 20f));
        header.setForeground(new Color(0x2563eb));
        main.add(header, BorderLayout.NORTH);

        // Left exam info
        JPanel examInfo = createExamInfoPanel();
        examInfo.setPreferredSize(new Dimension(300, 0));
        main.add(examInfo, BorderLayout.WEST);

        // Center questions section
        JPanel questionsSection = createQuestionsSection();
        main.add(questionsSection, BorderLayout.CENTER);

        add(main);
    }
    private void loadUserIsLoggedIn(){
        
    }
    private void loadInfoForData(){

        Map<String, Object> params = new HashMap<>();
        params.put("action", "get");
        params.put("method", "SELECT");
        params.put("table", List.of("account", "classes", ""));
        params.put("columns", List.of("questions.id", "questions.Question", "answers.Answer", "answers.id", "answers.IsCorrect"));
        Map<String, Object> join = new HashMap<>();
        join.put("type", "inner");
        join.put("on", List.of("questions.id = answers.QuestionId"));
        params.put("join", List.of(join));
        System.out.println("Loading questions from API with params: " + params);
        List<Map<String, Object>> apiData = apiService.postApiGetList("/autoGet", params);
        System.out.println(apiData);
    }
    private JPanel createExamInfoPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(0,0,0,10),
                BorderFactory.createEmptyBorder(10,10,10,10)
        ));

        JLabel title = new JLabel("Thông tin đề thi");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 16f));
        panel.add(title);
        panel.add(Box.createVerticalStrut(10));

        // Exam code
        JLabel codeLabel = new JLabel("📄 Mã đề thi:");
        examCodeField = new JTextField();
        examCodeField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        panel.add(codeLabel);
        panel.add(examCodeField);
        panel.add(Box.createVerticalStrut(10));

        // Grade
        JLabel gradeLabel = new JLabel("👥 Lớp:");
        gradeComboBox = new JComboBox<>(new String[] {
                "Lớp 6","Lớp 7","Lớp 8","Lớp 9","Lớp 10","Lớp 11","Lớp 12"
        });
        gradeComboBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        panel.add(gradeLabel);
        panel.add(gradeComboBox);
        panel.add(Box.createVerticalStrut(10));

        // Question count
        JLabel countLabel = new JLabel("📊 Tổng số câu hỏi:");
        questionCountLabel = new JLabel("1 câu hỏi");
        panel.add(countLabel);
        panel.add(questionCountLabel);
        panel.add(Box.createVerticalStrut(10));

        // Description
        JLabel descLabel = new JLabel("📝 Mô tả đề thi:");
        descriptionArea = new JTextArea(4, 20);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        JScrollPane descScroll = new JScrollPane(descriptionArea);
        descScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
        panel.add(descLabel);
        panel.add(descScroll);

        // --- NEW: publish date/time pickers (below description) ---
        panel.add(Box.createVerticalStrut(8));
        JLabel publishLabel = new JLabel("📅 Ngày giờ công bố (Start Publish):");
        publishLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(publishLabel);

        // Start date/time spinner
        javax.swing.SpinnerDateModel startModel = new javax.swing.SpinnerDateModel(new java.util.Date(), null, null, java.util.Calendar.MINUTE);
        publishDateSpinner = new javax.swing.JSpinner(startModel);
        publishDateSpinner.setEditor(new javax.swing.JSpinner.DateEditor(publishDateSpinner, "yyyy-MM-dd HH:mm:ss"));
        publishDateSpinner.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        panel.add(publishDateSpinner);

        panel.add(Box.createVerticalStrut(8));
        JLabel endLabel = new JLabel("⏱️ Ngày giờ kết thúc (End Publish):");
        endLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(endLabel);

        // End date/time spinner (default = start + 1 hour)
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.add(java.util.Calendar.HOUR_OF_DAY, 1);
        javax.swing.SpinnerDateModel endModel = new javax.swing.SpinnerDateModel(cal.getTime(), null, null, java.util.Calendar.MINUTE);
        endDateSpinner = new javax.swing.JSpinner(endModel);
        endDateSpinner.setEditor(new javax.swing.JSpinner.DateEditor(endDateSpinner, "yyyy-MM-dd HH:mm:ss"));
        endDateSpinner.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        panel.add(endDateSpinner);

        // Optional quick buttons (set now / add 1 hour)

        JPanel quickPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        quickPanel.setOpaque(false);
        JButton nowBtn = new JButton("Set now");
        nowBtn.addActionListener(e -> {
            publishDateSpinner.setValue(new java.util.Date());
            java.util.Calendar c = java.util.Calendar.getInstance();
            c.add(java.util.Calendar.HOUR_OF_DAY, 1);
            endDateSpinner.setValue(c.getTime());
        });
        JButton addHourBtn = new JButton("+1 hour");
        addHourBtn.addActionListener(e -> {
            java.util.Date cur = (java.util.Date) endDateSpinner.getValue();
            java.util.Calendar c = java.util.Calendar.getInstance();
            c.setTime(cur);
            c.add(java.util.Calendar.HOUR_OF_DAY, 1);
            endDateSpinner.setValue(c.getTime());
        });
        quickPanel.add(nowBtn);
        quickPanel.add(addHourBtn);
        panel.add(Box.createVerticalStrut(6));
        panel.add(quickPanel);
        panel.add(Box.createVerticalStrut(10));
        // --- END new UI ---

        return panel;
    }

    private JPanel createQuestionsSection() {
        JPanel panel = new JPanel(new BorderLayout(10,10));
        panel.setOpaque(false);

        JLabel title = new JLabel("Danh sách câu hỏi");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 16f));
        panel.add(title, BorderLayout.NORTH);

        questionsContainer = new JPanel();
        questionsContainer.setLayout(new BoxLayout(questionsContainer, BoxLayout.Y_AXIS));
        questionsContainer.setBackground(new Color(0xFFFFFF));
        questionsContainer.setBorder(new EmptyBorder(10,10,10,10));

        // initial question UI
        addQuestionUI(questions.get(0));

        questionsScrollPane = new JScrollPane(questionsContainer);
        questionsScrollPane.getVerticalScrollBar().setUnitIncrement(16);
        panel.add(questionsScrollPane, BorderLayout.CENTER);

        JPanel actions = createActionButtons();
        panel.add(actions, BorderLayout.SOUTH);

        return panel;
    }

    private void addQuestionUI(Question question) {
        JPanel questionBox = buildQuestionBox(question);
        questionsContainer.add(questionBox);
        questionsContainer.add(Box.createVerticalStrut(10));
        refreshUI();
        updateQuestionCount();
    }

    // Build a question panel (used by add and by refresh)
    private JPanel buildQuestionBox(Question question) {
        JPanel questionBox = new JPanel();
        questionBox.setLayout(new BoxLayout(questionBox, BoxLayout.Y_AXIS));
        questionBox.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xE5E7EB)),
                new EmptyBorder(10,10,10,10)
        ));
        questionBox.setBackground(Color.WHITE);
        questionBox.setAlignmentX(Component.LEFT_ALIGNMENT);

        // header with delete
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JLabel qLabel = new JLabel("Câu hỏi " + question.getQuestionNumber());
        qLabel.setFont(qLabel.getFont().deriveFont(Font.BOLD, 14f));
        header.add(qLabel, BorderLayout.WEST);

        JButton deleteBtn = new JButton("🗑️");
        deleteBtn.setFocusable(false);
        deleteBtn.addActionListener(e -> {
            // remove question from model and rebuild UI
            removeQuestion(question);
        });
        header.add(deleteBtn, BorderLayout.EAST);
        questionBox.add(header);
        questionBox.add(Box.createVerticalStrut(8));

        // question text
        JLabel qt = new JLabel("Nội dung câu hỏi:");
        JTextArea qArea = new JTextArea(3, 40);
        qArea.setLineWrap(true);
        qArea.setWrapStyleWord(true);
        qArea.getDocument().addDocumentListener(
            (SimpleDocumentListener) () -> question.setQuestionText(qArea.getText())
        );
        questionBox.add(qt);
        questionBox.add(new JScrollPane(qArea));
        questionBox.add(Box.createVerticalStrut(8));

        // answers with radio buttons (built by helper)
        JLabel ansLabel = new JLabel("Các đáp án (chọn đáp án đúng):");
        questionBox.add(ansLabel);
        JPanel answersPanel = buildAnswersPanel(question, questionBox);
        answersPanel.setName("answersPanel");
        questionBox.add(answersPanel);
        questionBox.add(Box.createVerticalStrut(10));

        return questionBox;
    }

    // Modified removeQuestion: update model then rebuild UI so numbering is contiguous
    private void removeQuestion(Question question) {
        if (questions.size() <= 1) return;
        int idx = questions.indexOf(question);
        if (idx >= 0) {
            questions.remove(idx);
            renumberQuestions();
            refreshQuestionsUI();
        }
    }

    // Completely rebuild questionsContainer from questions list
    private void refreshQuestionsUI() {
        questionsContainer.removeAll();
        for (Question q : questions) {
            JPanel qb = buildQuestionBox(q);
            questionsContainer.add(qb);
            questionsContainer.add(Box.createVerticalStrut(10));
        }
        refreshUI();
        updateQuestionCount();
    }

    private JPanel createActionButtons() {
        JPanel box = new JPanel(new BorderLayout());
        box.setOpaque(false);
        addQuestionBtn = new JButton("➕ Thêm câu hỏi");
        addQuestionBtn.addActionListener(e -> addNewQuestion());

        saveExamBtn = new JButton("💾 Lưu đề thi");
        saveExamBtn.addActionListener(e -> saveExam());

        JPanel left = new JPanel();
        left.setOpaque(false);
        left.add(addQuestionBtn);

        JPanel right = new JPanel();
        right.setOpaque(false);
        right.add(saveExamBtn);

        box.add(left, BorderLayout.WEST);
        box.add(right, BorderLayout.EAST);
        return box;
    }

    private void addNewQuestion() {
        questionCounter++;
        Question q = new Question(questionCounter);
        questions.add(q);
        addQuestionUI(q);
        SwingUtilities.invokeLater(() -> questionsScrollPane.getVerticalScrollBar().setValue(questionsScrollPane.getVerticalScrollBar().getMaximum()));
    }

    private void renumberQuestions() {
        for (int i = 0; i < questions.size(); i++) {
            Question q = questions.get(i);
            q.setQuestionNumber(i + 1);
        }
        // UI labels will be refreshed by refreshQuestionsUI()
    }

    private void updateQuestionCount() {
        questionCountLabel.setText(questions.size() + " câu hỏi");
    }

    private void refreshUI() {
        questionsContainer.revalidate();
        questionsContainer.repaint();
    }

    private void saveExam() {
        if (examCodeField.getText().trim().isEmpty() ||
            descriptionArea.getText().trim().isEmpty() ||
            gradeComboBox.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng điền đầy đủ thông tin đề thi!", "Lỗi", JOptionPane.WARNING_MESSAGE);
            return;
        }
        for (Question q : questions) {
            if (q.getQuestionText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Vui lòng điền đầy đủ nội dung câu hỏi!", "Lỗi", JOptionPane.WARNING_MESSAGE);
                return;
            }
            // bắt buộc phải có đủ 4 đáp án cho mỗi câu hỏi
            if (q.getAnswers() == null || q.getAnswers().size() < 4) {
                JOptionPane.showMessageDialog(this, "Mỗi câu hỏi phải có đủ 4 đáp án!", "Lỗi", JOptionPane.WARNING_MESSAGE);
                return;
            }
            for (String a : q.getAnswers()) {
                if (a == null || a.trim().isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Vui lòng điền đầy đủ các đáp án!", "Lỗi", JOptionPane.WARNING_MESSAGE);
                    return;
                }
            }
        }

        // Validate publish time range
        java.util.Date start = (java.util.Date) publishDateSpinner.getValue();
        java.util.Date end = (java.util.Date) endDateSpinner.getValue();
        if (end.before(start)) {
            JOptionPane.showMessageDialog(this, "Thời gian kết thúc phải lớn hơn thời gian bắt đầu!", "Lỗi", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // TODO: prepare payload and call apiService.autoUpdate (autoUpdate endpoint)
        // Example: build questions + answers payload, include PublishDate from getPublishDateTimeString() and EndDate
        System.out.println("Saving exam:");
        System.out.println("Code: " + examCodeField.getText());
        System.out.println("Grade: " + gradeComboBox.getSelectedItem());
        System.out.println("Description: " + descriptionArea.getText());
        System.out.println("Questions count: " + questions.size());
        System.out.println("Publish start: " + getPublishDateTimeString());
        System.out.println("Publish end: " + getEndPublishDateTimeString());

        JOptionPane.showMessageDialog(this, "Lưu đề thi thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
    }

    // build answers panel for a question (with radio buttons, no delete per answer)
    private JPanel buildAnswersPanel(Question question, JPanel questionBox) {
        ButtonGroup bg = new ButtonGroup();
        JPanel answersPanel = new JPanel(new GridLayout(question.getAnswers().size(), 1, 6, 6));
        answersPanel.setOpaque(false);

        String[] labels = {"A","B","C","D"};
        List<String> answers = question.getAnswers();
        for (int i = 0; i < answers.size(); i++) {
            final int idx = i;
            JPanel row = new JPanel(new BorderLayout(6,6));
            row.setOpaque(false);

            // left panel holds radio + label so radio is visible
            JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
            left.setOpaque(false);

            // radio button to select correct answer
            JRadioButton rb = new JRadioButton();
            rb.setSelected(idx == question.getCorrectAnswer());
            rb.addActionListener(e -> {
                question.setCorrectAnswer(idx);
            });
            bg.add(rb);
            left.add(rb);

            JLabel lbl = new JLabel(labels[Math.min(idx, labels.length-1)]);
            lbl.setPreferredSize(new Dimension(24, 20));
            left.add(lbl);

            row.add(left, BorderLayout.WEST);

            JTextField af = new JTextField(answers.get(idx));
            af.setPreferredSize(new Dimension(300, 28));
            af.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xD1D5DB)),
                BorderFactory.createEmptyBorder(4, 6, 4, 6)
            ));
            af.getDocument().addDocumentListener(
                (SimpleDocumentListener) () -> {
                    while (question.getAnswers().size() <= idx) question.getAnswers().add("");
                    question.getAnswers().set(idx, af.getText());
                }
            );
            row.add(af, BorderLayout.CENTER);

            answersPanel.add(row);
        }

        return answersPanel;
    }

    // helper to get selected publish date as formatted string
    private String getPublishDateTimeString() {
        Object val = (publishDateSpinner == null) ? null : publishDateSpinner.getValue();
        if (val instanceof java.util.Date) {
            java.text.SimpleDateFormat fmt = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            return fmt.format((java.util.Date) val);
        }
        return null;
    }

    // helper to get selected end publish date as formatted string
    private String getEndPublishDateTimeString() {
        Object val = (endDateSpinner == null) ? null : endDateSpinner.getValue();
        if (val instanceof java.util.Date) {
            java.text.SimpleDateFormat fmt = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            return fmt.format((java.util.Date) val);
        }
        return null;
    }

    @FunctionalInterface
    private interface SimpleDocumentListener extends javax.swing.event.DocumentListener {
        void update();

        @Override
        default void insertUpdate(javax.swing.event.DocumentEvent e) { update(); }

        @Override
        default void removeUpdate(javax.swing.event.DocumentEvent e) { update(); }

        @Override
        default void changedUpdate(javax.swing.event.DocumentEvent e) { update(); }
    }

    public static void main(String[] args) {
    javax.swing.SwingUtilities.invokeLater(() -> {
        try {
            javax.swing.UIManager.setLookAndFeel(javax.swing.UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}
        
        // Tạo AuthService trước
        AuthService authService = new AuthService();
        // Rồi truyền vào ApiService
        ApiService apiService = new ApiService(authService);
        
        QuizCreatorAppSwing app = new QuizCreatorAppSwing(apiService, authService, null);
        app.setVisible(true);
    });
}


}