package com.example.taskmanager.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;

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

public class QuizCreatorAppSwing extends JFrame {
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

    // Data
    private List<Question> questions;
    private int questionCounter = 1;

    public QuizCreatorAppSwing() {
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
        deleteBtn.addActionListener(e -> removeQuestion(question, questionBox));
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

        // answers with radio buttons
        JLabel ansLabel = new JLabel("Các đáp án (chọn đáp án đúng):");
        questionBox.add(ansLabel);
        ButtonGroup bg = new ButtonGroup();
        JPanel answersPanel = new JPanel();
        answersPanel.setLayout(new GridLayout(4,1,6,6));
        String[] labels = {"A","B","C","D"};
        JTextField[] answerFields = new JTextField[4];
        for (int i = 0; i < 4; i++) {
            JPanel row = new JPanel(new BorderLayout(6,6));
            row.setOpaque(false);
            JRadioButton rb = new JRadioButton();
            final int idx = i;
            rb.addActionListener(e -> question.setCorrectAnswer(idx));
            bg.add(rb);
            row.add(rb, BorderLayout.WEST);

            JLabel lbl = new JLabel(labels[i]);
            lbl.setPreferredSize(new Dimension(20, 20));
            row.add(lbl, BorderLayout.CENTER);

            JTextField af = new JTextField();
            af.getDocument().addDocumentListener(
                (SimpleDocumentListener) () -> question.getAnswers().set(idx, af.getText())
            );

            answerFields[i] = af;
            row.add(af, BorderLayout.EAST);

            answersPanel.add(row);
        }
        // default select first
        ((JRadioButton) ((JPanel)answersPanel.getComponent(0)).getComponent(0)).setSelected(true);

        questionBox.add(answersPanel);
        questionBox.add(Box.createVerticalStrut(10));

        questionsContainer.add(questionBox);
        questionsContainer.add(Box.createVerticalStrut(10));
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

    private void removeQuestion(Question question, JPanel questionBox) {
        if (questions.size() <= 1) return;
        int idx = questions.indexOf(question);
        if (idx >= 0) {
            questions.remove(idx);
            questionsContainer.remove(questionBox);
            renumberQuestions();
            refreshUI();
            updateQuestionCount();
        }
    }

    private void renumberQuestions() {
        for (int i = 0; i < questions.size(); i++) {
            Question q = questions.get(i);
            q.setQuestionNumber(i + 1);
            // update UI label
            Component comp = questionsContainer.getComponent(i*2); // question panels and spacing
            if (comp instanceof JPanel) {
                JPanel panel = (JPanel) comp;
                JPanel header = (JPanel) panel.getComponent(0);
                JLabel lbl = (JLabel) header.getComponent(0);
                lbl.setText("Câu hỏi " + (i + 1));
            }
        }
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
            for (String a : q.getAnswers()) {
                if (a == null || a.trim().isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Vui lòng điền đầy đủ các đáp án!", "Lỗi", JOptionPane.WARNING_MESSAGE);
                    return;
                }
            }
        }
        // TODO: persist exam to backend using existing ApiService
        System.out.println("Saving exam:");
        System.out.println("Code: " + examCodeField.getText());
        System.out.println("Grade: " + gradeComboBox.getSelectedItem());
        System.out.println("Description: " + descriptionArea.getText());
        System.out.println("Questions count: " + questions.size());
        JOptionPane.showMessageDialog(this, "Lưu đề thi thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
    }

    // Simple helper: document listener lambda
    // @FunctionalInterface
    // private interface SimpleDocumentListener extends javax.swing.event.DocumentListener {
    //     void update();
    //     @Override default void insertUpdate(javax.swing.event.DocumentEvent e) { update(); }
    //     @Override default void removeUpdate(javax.swing.event.DocumentEvent e) { update(); }
    //     @Override default void changedUpdate(javax.swing.event.DocumentEvent e) { update(); }
    // }
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
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {}
            QuizCreatorAppSwing app = new QuizCreatorAppSwing();
            app.setVisible(true);
        });
    }
}