package com.example.taskmanager.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
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
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;

import com.example.taskmanager.model.Task;
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
            for (int i = 0; i < 4; i++)
                answers.add("");
            this.correctAnswer = 0;
        }

        public String getQuestionText() {
            return questionText;
        }

        public void setQuestionText(String questionText) {
            this.questionText = questionText;
        }

        public List<String> getAnswers() {
            return answers;
        }

        public int getCorrectAnswer() {
            return correctAnswer;
        }

        public void setCorrectAnswer(int correctAnswer) {
            this.correctAnswer = correctAnswer;
        }

        public int getQuestionNumber() {
            return questionNumber;
        }

        public void setQuestionNumber(int n) {
            this.questionNumber = n;
        }
    }

    // UI Components
    private JTextField examCodeField;
    private JTextArea descriptionArea;
    private JComboBox<ClassItem> gradeComboBox;
    private JPanel questionsContainer;
    private JScrollPane questionsScrollPane;
    private JLabel questionCountLabel;
    private JButton addQuestionBtn;
    private JButton saveExamBtn;
    private JComboBox<Map<String, Object>> periodComboBox;
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
        loadUserInfo(users -> {
            // Xử lý danh sách người dùng sau khi tải xong (nếu cần)
            if (users != null && !users.isEmpty() && users.get(0) != null) {
                System.out.println("Loaded user fullName = " + users.get(0).getFullName());
            }
            // System.out.println("Loaded " + users.get(0));
        });
        loadInfoForData();
        setVisible(true);

    }

    private void loadUserInfo(Consumer<List<Task>> Callback) {
        SwingWorker<List<Task>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Task> doInBackground() {
                return apiService.getUsers();
            }

            @Override
            protected void done() {
                try {
                    List<Task> users = get();
                    Callback.accept(users);
                    // for (Task user : users) {
                    // System.out.println("User: " + user.getFullName());
                    // }
                    // System.out.println("Loaded users: " + users);
                } catch (Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(QuizCreatorAppSwing.this, "Lỗi khi tải thông tin người dùng!", "Lỗi",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
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
        JLabel header = new JLabel("TẠO ĐỀ THI TRẮC NGHIỆM");
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

    private List<Map<String, Object>> loadInfoForData() {

        Map<String, Object> params = new HashMap<>();
        params.put("action", "get");
        params.put("method", "SELECT");
        params.put("table", List.of("questions", "answers", "classes"));
        params.put("columns", List.of("questions.id", "questions.Question", "answers.Answer", "answers.id",
                "answers.IsCorrect", "classes.Name"));
        Map<String, Object> join1 = new HashMap<>();
        join1.put("type", "inner");
        join1.put("on", List.of("questions.id = answers.QuestionId"));

        Map<String, Object> join2 = new HashMap<>();
        join2.put("type", "inner");
        join2.put("on", List.of("questions.ClassId = classes.id"));

        params.put("join", List.of(join1, join2));
        System.out.println("Loading questions from API with params: " + params);
        List<Map<String, Object>> apiData = apiService.postApiGetList("/autoGet", params);
        // System.out.println(apiData);
        return apiData;

    }

    private JPanel createExamInfoPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(0, 0, 0, 10),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)));

        JLabel title = new JLabel("Thông tin đề thi");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 16f));
        panel.add(title);
        panel.add(Box.createVerticalStrut(10));

        // Exam code
        JLabel codeLabel = new JLabel("Mã đề thi:");
        examCodeField = new JTextField();
        examCodeField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        panel.add(codeLabel);
        panel.add(examCodeField);
        panel.add(Box.createVerticalStrut(10));

        // Grade
        // List<Map<String, Object>> data = loadInfoForData();
        // Set<String> classNames = new LinkedHashSet<>(); // tránh trùng tên lớp
        // for (Map<String, Object> row : data) {
        // Object nameObj = row.get("classes.Name");
        // if (nameObj != null) {
        // classNames.add(nameObj.toString());
        // }
        // }
        // JLabel gradeLabel = new JLabel("👥 Lớp:");
        // DefaultComboBoxModel<String> comboModel = new DefaultComboBoxModel<>(
        // classNames.toArray(new String[0])
        // );
        // JComboBox<String> gradeComboBox = new JComboBox<>(comboModel);
        // Lấy danh sách class từ API (robust: thử nhiều key để tránh mismatch)
        // List<Map<String, Object>> data = loadInfoForData();
        //
        // // DEBUG: in toàn bộ dữ liệu trả về để map key chính xác
        // System.out.println("DEBUG: loadInfoForData raw =>");
        // if (data == null || data.isEmpty()) {
        // System.out.println("DEBUG: loadInfoForData returned empty or null");
        // } else {
        // for (int i = 0; i < data.size(); i++) {
        // Map<String, Object> row = data.get(i);
        // System.out.println("DEBUG: row[" + i + "] keys=" + row.keySet() + " values=" + row);
        // }
        // }
        //
        // // extract class names using multiple possible keys, keep order and distinct
        // LinkedHashSet<String> classNamesSet = new LinkedHashSet<>();
        // if (data != null) {
        // for (Map<String, Object> m : data) {
        // Object v = null;
        // if (m.containsKey("ClassName")) v = m.get("ClassName");
        // if (v == null && m.containsKey("classes.Name")) v = m.get("classes.Name");
        // if (v == null && m.containsKey("classes.name")) v = m.get("classes.name");
        // if (v == null && m.containsKey("Name")) v = m.get("Name");
        // if (v == null && m.containsKey("name")) v = m.get("name");
        // if (v == null && m.containsKey("class_name")) v = m.get("class_name");
        // if (v == null) {
        // // last resort: try to read first String-like value
        // for (Object val : m.values()) {
        // if (val instanceof String && ((String) val).trim().length() > 0) { v = val; break; }
        // }
        // }
        // if (v != null) classNamesSet.add(v.toString());
        // }
        // }
        // String[] classNames = classNamesSet.isEmpty() ? new String[] { "Chưa có lớp" } : classNamesSet.toArray(new String[0]);
        // Lấy danh sách lớp giáo viên quản lý (đúng mục đích tạo đề)
        List<ClassItem> teacherClasses = loadTeacherClassesForCombo();
        DefaultComboBoxModel<ClassItem> comboModel = new DefaultComboBoxModel<>();
        if (teacherClasses != null && !teacherClasses.isEmpty()) {
            for (ClassItem ci : teacherClasses) comboModel.addElement(ci);
        } else {
            // fallback: try extract names from question data
            List<Map<String, Object>> data = loadInfoForData();
            LinkedHashSet<String> classNamesSet = new LinkedHashSet<>();
            if (data != null) {
                for (Map<String, Object> m : data) {
                    Object v = null;
                    if (m.containsKey("ClassName")) v = m.get("ClassName");
                    if (v == null && m.containsKey("classes.Name")) v = m.get("classes.Name");
                    if (v == null && m.containsKey("Name")) v = m.get("Name");
                    if (v == null) {
                        for (Object val : m.values()) {
                            if (val instanceof String && ((String) val).trim().length() > 0) { v = val; break; }
                        }
                    }
                    if (v != null) classNamesSet.add(v.toString());
                }
            }
            if (classNamesSet.isEmpty()) comboModel.addElement(new ClassItem(null, "Chưa có lớp"));
            else for (String n : classNamesSet) comboModel.addElement(new ClassItem(null, n));
        }

        JLabel gradeLabel = new JLabel("Lớp:");
        this.gradeComboBox = new JComboBox<>(comboModel);
        this.gradeComboBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        panel.add(gradeLabel);
        panel.add(this.gradeComboBox);
        panel.add(Box.createVerticalStrut(10));

        // Thêm khu vực đếm số câu hỏi (khởi tạo label trước khi dùng)
        JLabel countLabel = new JLabel("Tổng số câu hỏi:");
        this.questionCountLabel = new JLabel(); // khởi tạo
        updateQuestionCount(); // set giá trị ban đầu
        panel.add(countLabel);
        panel.add(this.questionCountLabel);
        panel.add(Box.createVerticalStrut(10));

        // Description
        JLabel descLabel = new JLabel("Mô tả đề thi:");
        descriptionArea = new JTextArea(4, 20);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        JScrollPane descScroll = new JScrollPane(descriptionArea);
        descScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
        panel.add(descLabel);
        panel.add(descScroll);

        // --- NEW: publish date/time pickers (below description) ---
        panel.add(Box.createVerticalStrut(8));
        JLabel publishLabel = new JLabel("Ngày giờ công bố (Start Publish):");
        publishLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(publishLabel);

        // Start date/time spinner
        javax.swing.SpinnerDateModel startModel = new javax.swing.SpinnerDateModel(new java.util.Date(), null, null,
                java.util.Calendar.MINUTE);
        publishDateSpinner = new javax.swing.JSpinner(startModel);
        publishDateSpinner.setEditor(new javax.swing.JSpinner.DateEditor(publishDateSpinner, "yyyy-MM-dd HH:mm:ss"));
        publishDateSpinner.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        panel.add(publishDateSpinner);

        panel.add(Box.createVerticalStrut(8));
        JLabel endLabel = new JLabel("Ngày giờ kết thúc (End Publish):");
        endLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(endLabel);

        // End date/time spinner (default = start + 1 hour)
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.add(java.util.Calendar.HOUR_OF_DAY, 1);
        javax.swing.SpinnerDateModel endModel = new javax.swing.SpinnerDateModel(cal.getTime(), null, null,
                java.util.Calendar.MINUTE);
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

        // --- NEW: Period Selection ---
panel.add(Box.createVerticalStrut(10));
JLabel periodLabel = new JLabel("Kỳ thi (Period):");
periodLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
panel.add(periodLabel);

periodComboBox = new JComboBox<>();
periodComboBox.setFont(new Font("Segoe UI", Font.PLAIN, 14));
periodComboBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
periodComboBox.setRenderer(new PeriodComboBoxRenderer());
panel.add(periodComboBox);
panel.add(Box.createVerticalStrut(8));

// Tải dữ liệu kỳ thi
loadExamPeriods();
        // --- END new UI ---

        return panel;
    }

    private JPanel createQuestionsSection() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setOpaque(false);

        JLabel title = new JLabel("Danh sách câu hỏi");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 16f));
        panel.add(title, BorderLayout.NORTH);

        questionsContainer = new JPanel();
        questionsContainer.setLayout(new BoxLayout(questionsContainer, BoxLayout.Y_AXIS));
        questionsContainer.setBackground(new Color(0xFFFFFF));
        questionsContainer.setBorder(new EmptyBorder(10, 10, 10, 10));

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
                new EmptyBorder(10, 10, 10, 10)));
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
        qArea.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                question.setQuestionText(qArea.getText());
            }
            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                question.setQuestionText(qArea.getText());
            }
            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                question.setQuestionText(qArea.getText());
            }
        });
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

    // Modified removeQuestion: update model then rebuild UI so numbering is
    // contiguous
    private void removeQuestion(Question question) {
        if (questions.size() <= 1)
            return;
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
        addQuestionBtn = new JButton("Thêm câu hỏi");
        addQuestionBtn.addActionListener(e -> addNewQuestion());

        saveExamBtn = new JButton("Lưu đề thi");
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
        SwingUtilities.invokeLater(() -> questionsScrollPane.getVerticalScrollBar()
                .setValue(questionsScrollPane.getVerticalScrollBar().getMaximum()));
    }

    private void renumberQuestions() {
        for (int i = 0; i < questions.size(); i++) {
            Question q = questions.get(i);
            q.setQuestionNumber(i + 1);
        }
        // UI labels will be refreshed by refreshQuestionsUI()
    }

    private void updateQuestionCount() {
        if (questionCountLabel != null) {
            questionCountLabel.setText(questions.size() + " câu hỏi");
        }
    }

    private void refreshUI() {
        questionsContainer.revalidate();
        questionsContainer.repaint();
    }

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

            SwingUtilities.invokeLater(() -> {
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

    private void saveExam() {
    // === VALIDATION ===
    if (examCodeField.getText().trim().isEmpty() ||
            descriptionArea.getText().trim().isEmpty() ||
            gradeComboBox.getSelectedItem() == null) {
        JOptionPane.showMessageDialog(this, "Vui lòng điền đầy đủ thông tin đề thi!", "Lỗi", JOptionPane.WARNING_MESSAGE);
        return;
    }

    // Validate Period
    Map<String, Object> selectedPeriod = (Map<String, Object>) periodComboBox.getSelectedItem();
    int periodId = 0;
    if (selectedPeriod != null) {
        Object idObj = selectedPeriod.get("Id");
        if (idObj != null) {
            periodId = Integer.parseInt(idObj.toString());
        }
    }
    if (periodId == 0) {
        JOptionPane.showMessageDialog(this, "Vui lòng chọn kỳ thi!", "Lỗi", JOptionPane.WARNING_MESSAGE);
        return;
    }

    for (Question q : questions) {
        if (q.getQuestionText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng điền đầy đủ nội dung câu hỏi!", "Lỗi", JOptionPane.WARNING_MESSAGE);
            return;
        }
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

    java.util.Date start = (java.util.Date) publishDateSpinner.getValue();
    java.util.Date end = (java.util.Date) endDateSpinner.getValue();
    if (end.before(start)) {
        JOptionPane.showMessageDialog(this, "Thời gian kết thúc phải lớn hơn thời gian bắt đầu!", "Lỗi", JOptionPane.WARNING_MESSAGE);
        return;
    }

    // === RESOLVE IDs ===
    ClassItem selectedClass = null;
    try { selectedClass = (ClassItem) gradeComboBox.getSelectedItem(); } catch (Exception ignored) {}
    String className = selectedClass == null ? String.valueOf(gradeComboBox.getSelectedItem()) : selectedClass.getName();

    Integer teacherId = getTeacherIdFromAuth();
    Integer classId = selectedClass == null ? resolveClassIdByName(className, teacherId) : selectedClass.getId();

    if (classId == null) {
        int ok = JOptionPane.showConfirmDialog(this, "Không tìm thấy lớp '" + className + "'. Tiếp tục lưu không kèm ClassId?", "Xác nhận", JOptionPane.YES_NO_OPTION);
        if (ok != JOptionPane.YES_OPTION) return;
    }

    // === GỌI LƯU ===
    final int finalPeriodId = periodId; // final để dùng trong worker
    insertExamWithQuestionsAndAnswers(
        teacherId == null ? 0L : teacherId.longValue(),
        classId == null ? -1 : classId,
        examCodeField.getText(),
        finalPeriodId  // ← Thêm PeriodId
    );
}

    // Try resolve teacher id from authService -> account->teacher
    private Integer getTeacherIdFromAuth() {
        try {
            String email = null;
            try { email = (String) authService.getClass().getMethod("getUserEmail").invoke(authService); } catch (Exception ignore) {}
            if (email == null) {
                try { email = (String) authService.getClass().getMethod("getEmail").invoke(authService); } catch (Exception ignore) {}
            }
            if (email == null || email.isEmpty()) return null;

            Map<String,Object> p = new HashMap<>();
            p.put("action", "get");
            p.put("method", "SELECT");
            p.put("table", List.of("account", "teacher"));
            p.put("columns", List.of("teacher.Id as TeacherId"));
            p.put("where", Map.of("account.email", email));
            p.put("limit", 1);
            Map<String,Object> j = new HashMap<>();
            j.put("type", "inner");
            j.put("on", List.of("account.id = teacher.IdAccount"));
            p.put("join", List.of(j));

            System.out.println("DEBUG: getTeacherIdFromAuth payload=" + p);
            List<Map<String,Object>> resp = apiService.postApiGetList("/autoGet", p);
            System.out.println("DEBUG: getTeacherIdFromAuth resp=" + resp);
            if (resp != null && !resp.isEmpty()) {
                Object v = resp.get(0).get("TeacherId");
                if (v == null) v = resp.get(0).get("teacher.Id");
                if (v == null) v = resp.get(0).get("Id");
                if (v instanceof Number) return ((Number)v).intValue();
                if (v != null) return Integer.parseInt(v.toString());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    // Resolve class id by class name and optional teacherId
    private Integer resolveClassIdByName(String className, Integer teacherId) {
        if (className == null) return null;
        try {
            Map<String,Object> params = new HashMap<>();
            params.put("action", "get");
            params.put("method", "SELECT");
            params.put("table", "classes");
            params.put("columns", List.of("id as ClassId", "Name as ClassName"));
            Map<String,Object> where = new HashMap<>();
            where.put("Name", className);
            if (teacherId != null) where.put("TeacherId", teacherId);
            params.put("where", where);
            System.out.println("DEBUG: resolveClassIdByName payload=" + params);
            List<Map<String,Object>> resp = apiService.postApiGetList("/autoGet", params);
            System.out.println("DEBUG: resolveClassIdByName resp=" + resp);
            if (resp != null && !resp.isEmpty()) {
                Object v = resp.get(0).get("ClassId");
                if (v == null) v = resp.get(0).get("id");
                if (v instanceof Number) return ((Number)v).intValue();
                if (v != null) return Integer.parseInt(v.toString());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }
    // Fetch classes for the logged-in teacher and return distinct class names (preserve order)
    private List<ClassItem> loadTeacherClassesForCombo() {
        List<ClassItem> out = new ArrayList<>();
        try {
            // get teacher email from authService (try common method names)
            String email = null;
            try { email = (String) authService.getClass().getMethod("getUserEmail").invoke(authService); } catch (Exception ignore) {}
            if (email == null) {
                try { email = (String) authService.getClass().getMethod("getEmail").invoke(authService); } catch (Exception ignore) {}
            }
            System.out.println("DEBUG: teacher email for classes lookup = " + email);
            if (email == null || email.isEmpty()) return out;

            Map<String,Object> params = new HashMap<>();
            params.put("action", "get");
            params.put("method", "SELECT");

            // join account -> teacher -> teacher_class -> classes
            params.put("table", List.of("account", "teacher", "teacher_class", "classes"));

            params.put("columns", List.of(
                    "classes.Id AS ClassId",
                    "classes.Name AS ClassName"
            ));

            // WHERE email = ?
            Map<String,Object> where = new HashMap<>();
            where.put("account.email", email);
            params.put("where", where);

            // JOIN 1: account.id = teacher.IdAccount
            Map<String,Object> j1 = new HashMap<>();
            j1.put("type", "inner");
            j1.put("on", List.of("account.id = teacher.IdAccount"));

            // JOIN 2: teacher.Id = teacher_class.TeacherId
            Map<String,Object> j2 = new HashMap<>();
            j2.put("type", "inner");
            j2.put("on", List.of("teacher.Id = teacher_class.TeacherId"));

            // JOIN 3: teacher_class.ClassId = classes.Id
            Map<String,Object> j3 = new HashMap<>();
            j3.put("type", "inner");
            j3.put("on", List.of("teacher_class.ClassId = classes.Id"));

            // Put all joins
            params.put("join", List.of(j1, j2, j3));

            System.out.println("DEBUG: fetching teacher classes with params=" + params);
            List<Map<String,Object>> resp = apiService.postApiGetList("/autoGet", params);
            System.out.println("DEBUG: teacher classes raw => " + resp);

            LinkedHashSet<ClassItem> set = new LinkedHashSet<>();
            if (resp != null) {
                for (Map<String,Object> row : resp) {
                    Object nameObj = null;
                    if (row.containsKey("ClassName")) nameObj = row.get("ClassName");
                    if (nameObj == null && row.containsKey("classes.Name")) nameObj = row.get("classes.Name");
                    if (nameObj == null && row.containsKey("Name")) nameObj = row.get("Name");
                    Integer id = null;
                    Object idObj = row.get("ClassId");
                    if (idObj == null) idObj = row.get("Id");
                    if (idObj instanceof Number) id = ((Number) idObj).intValue();
                    else if (idObj != null) {
                        try { id = Integer.parseInt(idObj.toString()); } catch (Exception ignored) {}
                    }
                    String name = nameObj == null ? null : nameObj.toString();
                    if (name == null || name.isEmpty()) continue;
                    set.add(new ClassItem(id, name));
                }
            }
            out.addAll(set);
         } catch (Exception ex) {
             ex.printStackTrace();
         }
         return out;
     }

    // Format date helper
    private String formatDate(java.util.Date d) {
        if (d == null) return null;
        java.text.SimpleDateFormat fmt = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return fmt.format(d);
    }

    private String getPublishDateTimeString() {
        try {
            return formatDate((java.util.Date) publishDateSpinner.getValue());
        } catch (Exception e) {
            return formatDate(new java.util.Date());
        }
    }

    private String getEndPublishDateTimeString() {
        try {
            return formatDate((java.util.Date) endDateSpinner.getValue());
        } catch (Exception e) {
            java.util.Calendar c = java.util.Calendar.getInstance();
            c.add(java.util.Calendar.HOUR_OF_DAY, 1);
            return formatDate(c.getTime());
        }
    }

    // Call server multiInsert: operations = [{table, rows: [ {...}, ... ]}, ...]
    private List<Map<String,Object>> callMultiInsert(List<Map<String,Object>> operations) {
        try {
            Map<String,Object> payload = new HashMap<>();
            payload.put("operations", operations);

            // DEBUG: log full payload before sending
            System.out.println("DEBUG: callMultiInsert -> POST /multiInsert payload = " + payload);

            // try /multiInsert first
            List<Map<String,Object>> resp = null;
            try {
                resp = apiService.postApiGetList("/multiInsert", payload);
            } catch (Exception ex) {
                System.err.println("WARN: /multiInsert call failed: " + ex.getMessage());
            }

            System.out.println("DEBUG: callMultiInsert -> response = " + resp);
            return resp;
        } catch (Exception ex) {
            ex.printStackTrace();
            return Collections.emptyList();
        }
    }

    // Orchestrator: insert questions -> insert answers (uses callMultiInsert)
    private void insertExamWithQuestionsAndAnswers(long teacherId, int classId, String examCode, int periodId) {
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                try {
                    // Parse TestNumber from examCode (take digits). If none, fallback to per-question number.
                    Integer parsedTestNumber = null;
                    if (examCode != null) {
                        String digits = examCode.replaceAll("\\D+", "");
                        if (!digits.isEmpty()) {
                            try { parsedTestNumber = Integer.parseInt(digits); } catch (NumberFormatException ignore) { parsedTestNumber = null; }
                        }
                    }

                    // Build question rows (no exams table insert)
                    List<Map<String,Object>> questionRows = new ArrayList<>();
                    for (Question q : questions) {
                        Map<String,Object> qr = new HashMap<>();
                        // TestNumber is from examCode (same for all questions) or fallback to question number
                        if (parsedTestNumber != null) qr.put("TestNumber", parsedTestNumber);
                        else qr.put("TestNumber", q.getQuestionNumber());
                        if (classId > 0) qr.put("ClassId", classId);
                        qr.put("Question", q.getQuestionText());
                        qr.put("PeriodId", periodId);
                        qr.put("PublishDate", getPublishDateTimeString());
                        qr.put("ExpireDate", getEndPublishDateTimeString());
                        qr.put("TeacherId", teacherId);
                        questionRows.add(qr);
                    }

                    List<Integer> questionIds = new ArrayList<>();
                    if (!questionRows.isEmpty()) {
                        Map<String,Object> opQuestions = new HashMap<>();
                        opQuestions.put("table", "questions");
                        opQuestions.put("rows", questionRows);
                        System.out.println("DEBUG: will insert questions op = (rows=" + questionRows.size() + ")");
                        List<Map<String,Object>> resQuestions = callMultiInsert(List.of(opQuestions));
                        System.out.println("DEBUG: resQuestions = " + resQuestions);

                        // parse inserted question ids (in order)
                        if (resQuestions != null && !resQuestions.isEmpty()) {
                            Object maybe = resQuestions.get(0).get("insert_ids");
                            if (maybe instanceof List) {
                                for (Object o : (List<?>) maybe) {
                                    if (o instanceof Number) questionIds.add(((Number)o).intValue());
                                    else if (o instanceof String) {
                                        try { questionIds.add(Integer.parseInt((String)o)); } catch (Exception ignored) {}
                                    }
                                }
                            } else {
                                Object lid = resQuestions.get(0).get("last_insert_id");
                                if (lid instanceof Number) questionIds.add(((Number)lid).intValue());
                                else if (lid instanceof String) {
                                    try { questionIds.add(Integer.parseInt((String)lid)); } catch (Exception ignored) {}
                                }
                            }
                        }
                    }

                    // Insert answers, mapping to created question ids if available
                    List<Map<String,Object>> answerRows = new ArrayList<>();
                    int qIndex = 0;
                    for (Question q : questions) {
                        Integer qId = (qIndex < questionIds.size()) ? questionIds.get(qIndex) : null;
                        for (int ai = 0; ai < q.getAnswers().size(); ai++) {
                            Map<String,Object> ar = new HashMap<>();
                            if (qId != null) ar.put("QuestionId", qId);
                            ar.put("Answer", q.getAnswers().get(ai));
                            ar.put("IsCorrect", (ai == q.getCorrectAnswer()) ? 1 : 0);
                            answerRows.add(ar);
                        }
                        qIndex++;
                    }

                    if (!answerRows.isEmpty()) {
                        Map<String,Object> opAnswers = new HashMap<>();
                        opAnswers.put("table", "answers");
                        opAnswers.put("rows", answerRows);
                        System.out.println("DEBUG: will insert answers op (rows=" + answerRows.size() + ")");
                        List<Map<String,Object>> resAnswers = callMultiInsert(List.of(opAnswers));
                        System.out.println("DEBUG: resAnswers = " + resAnswers);
                    }

                    SwingUtilities.invokeLater(() -> {
                        JOptionPane.showMessageDialog(QuizCreatorAppSwing.this, "Đã lưu câu hỏi và đáp án.", "Thành công", JOptionPane.INFORMATION_MESSAGE);
                    });
                } catch (Exception ex) {
                    ex.printStackTrace();
                    SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(QuizCreatorAppSwing.this, "Lỗi khi lưu đề: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE));
                }
                return null;
            }
        };
        worker.execute();
    }

    // Build answers UI for a question (4 answers + radio group)
    private JPanel buildAnswersPanel(Question question, JPanel container) {
        JPanel panel = new JPanel(new GridLayout(4, 1, 6, 6));
        ButtonGroup bg = new ButtonGroup();
        for (int i = 0; i < 4; i++) {
            int idx = i;
            JPanel row = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
            row.setOpaque(false);
            JRadioButton rb = new JRadioButton();
            rb.setSelected(question.getCorrectAnswer() == idx);
            rb.addActionListener(e -> question.setCorrectAnswer(idx));
            bg.add(rb);
            JTextField tf = new JTextField();
            tf.setColumns(40);
            String existing = "";
            try {
                existing = question.getAnswers().get(idx);
            } catch (Exception ignored) {}
            tf.setText(existing == null ? "" : existing);
            tf.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
                private void update() {
                    String t = tf.getText();
                    // ensure list size
                    while (question.getAnswers().size() <= idx) question.getAnswers().add("");
                    question.getAnswers().set(idx, t);
                }
                @Override public void insertUpdate(javax.swing.event.DocumentEvent e) { update(); }
                @Override public void removeUpdate(javax.swing.event.DocumentEvent e) { update(); }
                @Override public void changedUpdate(javax.swing.event.DocumentEvent e) { update(); }
            });

            row.add(rb);
            row.add(tf);
            panel.add(row);
        }
        return panel;
    }

    // add this small holder class (inside QuizCreatorAppSwing)
    public static class ClassItem {
        private final Integer id;
        private final String name;
        public ClassItem(Integer id, String name) { this.id = id; this.name = name; }
        public Integer getId() { return id; }
        public String getName() { return name; }
        @Override public String toString() { return name == null ? "" : name; } // displayed in JComboBox
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
}