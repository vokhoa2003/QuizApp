package com.example.taskmanager.ui;

import java.util.ArrayList;
import java.util.List;

import com.formdev.flatlaf.FlatLightLaf;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

public class QuizCreatorApp extends Application {
    
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
            for (int i = 0; i < 4; i++) {
                answers.add("");
            }
            this.correctAnswer = 0;
        }
        // Getters and setters
        public String getQuestionText() { return questionText; }
        public void setQuestionText(String questionText) { this.questionText = questionText; }
        public List<String> getAnswers() { return answers; }
        public int getCorrectAnswer() { return correctAnswer; }
        public void setCorrectAnswer(int correctAnswer) { this.correctAnswer = correctAnswer; }
        public int getQuestionNumber() { return questionNumber; }
    }
    
    public static class ExamInfo {
        private String examCode;
        private String description;
        private String grade;
        
        // Getters and setters
        public String getExamCode() { return examCode; }
        public void setExamCode(String examCode) { this.examCode = examCode; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getGrade() { return grade; }
        public void setGrade(String grade) { this.grade = grade; }
    }
    
    // UI Components
    private TextField examCodeField;
    private TextArea descriptionArea;
    private ComboBox<String> gradeComboBox;
    private VBox questionsContainer;
    private ScrollPane questionsScrollPane;
    private Label questionCountLabel;
    private Button addQuestionBtn;
    private Button saveExamBtn;
    
    // Data
    private ExamInfo examInfo;
    private List<Question> questions;
    private int questionCounter = 1;
    
    @Override
    public void start(Stage primaryStage) {
        // Set FlatLaf Look and Feel
        try {
            FlatLightLaf.setup();
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // Initialize data
        examInfo = new ExamInfo();
        questions = new ArrayList<>();
        questions.add(new Question(questionCounter));
        
        // Create main layout
        HBox mainLayout = createMainLayout();
        
        // Wrap mainLayout in a ScrollPane
        ScrollPane scrollPane = new ScrollPane(mainLayout);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        
        // Create scene
        Scene scene = new Scene(scrollPane, 900, 700);
        scene.getStylesheets().add(getClass().getResource("/styles/quiz-creator.css").toExternalForm());
        
        // Setup stage
        primaryStage.setTitle("Tạo Đề Thi Trắc Nghiệm - SecureStudy");
        primaryStage.setScene(scene);
        primaryStage.setResizable(true);
        primaryStage.show();
    }
    
    private HBox createMainLayout() {
        HBox mainLayout = new HBox(20);
        mainLayout.setPadding(new Insets(20));
        mainLayout.setStyle("-fx-background-color: #f5f7fa;");
        
        // Header
        Label headerLabel = new Label("📚 TẠO ĐỀ THI TRẮC NGHIỆM");
        headerLabel.setFont(Font.font("System", FontWeight.BOLD, 24));
        headerLabel.setStyle("-fx-text-fill: #2563eb;");
        
        // Exam info section
        VBox examInfoSection = createExamInfoSection();
        HBox.setHgrow(examInfoSection, Priority.NEVER); // Fixed width, no growth
        
        // Questions section
        VBox questionsSection = createQuestionsSection();
        HBox.setHgrow(questionsSection, Priority.ALWAYS); // Grows to fill remaining space
        
        // Create a container for header to span full width
        VBox topContainer = new VBox(20, headerLabel);
        HBox.setHgrow(topContainer, Priority.ALWAYS);
        
        VBox sectionsContainer = new VBox(20, topContainer, new HBox(20, examInfoSection, questionsSection));
        mainLayout.getChildren().addAll(sectionsContainer);
        
        return mainLayout;
    }
    
    private VBox createExamInfoSection() {
        VBox examInfoBox = new VBox(15);
        examInfoBox.setStyle("""
            -fx-background-color: white;
            -fx-background-radius: 10;
            -fx-padding: 20;
            -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2);
            -fx-max-width: 270; // Maximum width to maintain a reasonable size
        """);
        
        Label sectionTitle = new Label("Thông tin đề thi");
        sectionTitle.setFont(Font.font("System", FontWeight.BOLD, 18));
        sectionTitle.setStyle("-fx-text-fill: #374151;");
        
        // First row: Exam Code, Grade, Question Count
        VBox topRow = new VBox(15); // Changed to VBox for vertical stacking in narrow space
        topRow.setAlignment(Pos.TOP_LEFT);
        
        VBox examCodeBox = new VBox(5);
        Label examCodeLabel = new Label("📄 Mã đề thi:");
        examCodeLabel.setFont(Font.font("System", FontWeight.SEMI_BOLD, 14));
        examCodeField = new TextField();
        examCodeField.setPromptText("VD: DE001");
        examCodeField.setPrefWidth(200);
        examCodeBox.getChildren().addAll(examCodeLabel, examCodeField);
        
        VBox gradeBox = new VBox(5);
        Label gradeLabel = new Label("👥 Lớp:");
        gradeLabel.setFont(Font.font("System", FontWeight.SEMI_BOLD, 14));
        gradeComboBox = new ComboBox<>();
        gradeComboBox.setItems(FXCollections.observableArrayList(
            "Lớp 6", "Lớp 7", "Lớp 8", "Lớp 9", "Lớp 10", "Lớp 11", "Lớp 12"
        ));
        gradeComboBox.setPromptText("Chọn lớp");
        gradeComboBox.setPrefWidth(150);
        gradeBox.getChildren().addAll(gradeLabel, gradeComboBox);
        
        VBox countBox = new VBox(5);
        Label countLabel = new Label("📊 Tổng số câu hỏi:");
        countLabel.setFont(Font.font("System", FontWeight.SEMI_BOLD, 14));
        questionCountLabel = new Label("1 câu hỏi");
        questionCountLabel.setStyle("""
            -fx-background-color: #e0e7ff;
            -fx-text-fill: #3730a3;
            -fx-padding: 8 12;
            -fx-background-radius: 6;
            -fx-font-weight: bold;
        """);
        countBox.getChildren().addAll(countLabel, questionCountLabel);
        
        topRow.getChildren().addAll(examCodeBox, gradeBox, countBox);
        
        // Description
        VBox descriptionBox = new VBox(5);
        Label descLabel = new Label("📝 Mô tả đề thi:");
        descLabel.setFont(Font.font("System", FontWeight.SEMI_BOLD, 14));
        descriptionArea = new TextArea();
        descriptionArea.setPromptText("Nhập mô tả về đề thi...");
        descriptionArea.setPrefRowCount(3);
        descriptionBox.getChildren().addAll(descLabel, descriptionArea);
        
        examInfoBox.getChildren().addAll(sectionTitle, topRow, descriptionBox);
        
        return examInfoBox;
    }
    
    private VBox createQuestionsSection() {
        VBox questionsSection = new VBox(15);
        questionsSection.getStyleClass().add("questions-section"); // Thêm class CSS
        
        Label sectionTitle = new Label("Danh sách câu hỏi");
        sectionTitle.setFont(Font.font("System", FontWeight.BOLD, 18));
        sectionTitle.setStyle("-fx-text-fill: #374151;");
        
        questionsContainer = new VBox(15);
        questionsContainer.setPadding(new Insets(10));
        
        questionsScrollPane = new ScrollPane(questionsContainer);
        questionsScrollPane.setFitToWidth(true);
        questionsScrollPane.setPrefHeight(500); // Reduced height to make room for buttons
        questionsScrollPane.setStyle("""
            -fx-background: transparent;
            -fx-background-color: transparent;
            -fx-border-color: transparent;
        """);
        
        // Add initial question
        addQuestionUI(questions.get(0));
        
        // Add action buttons below the scroll pane
        VBox actionButtons = createActionButtons();
        
        questionsSection.getChildren().addAll(sectionTitle, questionsScrollPane, actionButtons);
        
        return questionsSection;
    }
    
    private void addQuestionUI(Question question) {
        VBox questionBox = new VBox(15);
        questionBox.setStyle("""
            -fx-background-color: white;
            -fx-background-radius: 10;
            -fx-padding: 20;
            -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2);
        """);
        
        // Question header
        HBox questionHeader = new HBox();
        questionHeader.setAlignment(Pos.CENTER_LEFT);
        
        Label questionTitle = new Label("Câu hỏi " + question.getQuestionNumber());
        questionTitle.setFont(Font.font("System", FontWeight.BOLD, 16));
        questionTitle.setStyle("-fx-text-fill: #1f2937;");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Button deleteBtn = new Button("🗑️");
        deleteBtn.setStyle("""
            -fx-background-color: #fef2f2;
            -fx-text-fill: #dc2626;
            -fx-background-radius: 20;
            -fx-padding: 5 10;
            -fx-border: none;
        """);
        deleteBtn.setOnAction(e -> removeQuestion(question, questionBox));
        
        if (questions.size() <= 1) {
            deleteBtn.setDisable(true);
            deleteBtn.setOpacity(0.5);
        }
        
        questionHeader.getChildren().addAll(questionTitle, spacer, deleteBtn);
        
        // Question text
        Label questionLabel = new Label("Nội dung câu hỏi:");
        questionLabel.setFont(Font.font("System", FontWeight.SEMI_BOLD, 14));
        
        TextArea questionTextArea = new TextArea();
        questionTextArea.setPromptText("Nhập câu hỏi...");
        questionTextArea.setPrefRowCount(3);
        questionTextArea.textProperty().addListener((obs, oldText, newText) -> {
            question.setQuestionText(newText);
        });
        
        // Answers
        Label answersLabel = new Label("Các đáp án (chọn đáp án đúng):");
        answersLabel.setFont(Font.font("System", FontWeight.SEMI_BOLD, 14));
        
        VBox answersBox = new VBox(10);
        ToggleGroup answerGroup = new ToggleGroup();
        
        char[] labels = {'A', 'B', 'C', 'D'};
        for (int i = 0; i < 4; i++) {
            HBox answerRow = createAnswerRow(question, i, labels[i], answerGroup);
            answersBox.getChildren().add(answerRow);
        }
        
        questionBox.getChildren().addAll(
            questionHeader, 
            questionLabel, 
            questionTextArea, 
            answersLabel, 
            answersBox
        );
        
        questionsContainer.getChildren().add(questionBox);
        updateQuestionCount();
    }
    
    private HBox createAnswerRow(Question question, int index, char label, ToggleGroup group) {
        HBox answerRow = new HBox(10);
        answerRow.setAlignment(Pos.CENTER_LEFT);
        
        RadioButton radioButton = new RadioButton();
        radioButton.setToggleGroup(group);
        radioButton.setSelected(index == question.getCorrectAnswer());
        radioButton.setOnAction(e -> question.setCorrectAnswer(index));
        
        Label answerLabel = new Label(String.valueOf(label));
        answerLabel.setStyle("""
            -fx-background-color: #e0e7ff;
            -fx-text-fill: #3730a3;
            -fx-padding: 8;
            -fx-background-radius: 50%;
            -fx-font-weight: bold;
            -fx-min-width: 30;
            -fx-alignment: center;
        """);
        
        TextField answerField = new TextField();
        answerField.setPromptText("Đáp án " + label);
        answerField.setPrefWidth(400); // Adjusted for dynamic width, can be increased if needed
        answerField.textProperty().addListener((obs, oldText, newText) -> {
            question.getAnswers().set(index, newText);
        });
        
        answerRow.getChildren().addAll(radioButton, answerLabel, answerField);
        
        return answerRow;
    }
    
    private VBox createActionButtons() {
        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setStyle("""
            -fx-background-color: white;
            -fx-background-radius: 10;
            -fx-padding: 20;
            -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2);
        """);
        
        addQuestionBtn = new Button("➕ Thêm câu hỏi");
        addQuestionBtn.setStyle("""
            -fx-background-color: #3b82f6;
            -fx-text-fill: white;
            -fx-padding: 12 24;
            -fx-background-radius: 8;
            -fx-font-size: 14;
            -fx-font-weight: bold;
        """);
        addQuestionBtn.setOnAction(e -> addNewQuestion());
        
        saveExamBtn = new Button("💾 Lưu đề thi");
        saveExamBtn.setStyle("""
            -fx-background-color: #10b981;
            -fx-text-fill: white;
            -fx-padding: 12 32;
            -fx-background-radius: 8;
            -fx-font-size: 14;
            -fx-font-weight: bold;
        """);
        saveExamBtn.setOnAction(e -> saveExam());
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        // Help text
        Label helpLabel = new Label("💡 Hướng dẫn: Nhập đầy đủ thông tin đề thi, thêm câu hỏi và chọn đáp án đúng");
        helpLabel.setStyle("""
            -fx-text-fill: #6366f1;
            -fx-font-size: 12;
            -fx-font-style: italic;
        """);
        
        VBox helpBox = new VBox(10);
        helpBox.setAlignment(Pos.CENTER);
        helpBox.getChildren().addAll(buttonBox, helpLabel);
        
        buttonBox.getChildren().addAll(addQuestionBtn, spacer, saveExamBtn);
        
        return helpBox;
    }
    
    private void addNewQuestion() {
        questionCounter++;
        Question newQuestion = new Question(questionCounter);
        questions.add(newQuestion);
        addQuestionUI(newQuestion);
        
        // Scroll to bottom
        questionsScrollPane.setVvalue(1.0);
    }
    
    private void removeQuestion(Question question, VBox questionBox) {
        if (questions.size() > 1) {
            questions.remove(question);
            questionsContainer.getChildren().remove(questionBox);
            updateQuestionCount();
            renumberQuestions();
        }
    }
    
    private void renumberQuestions() {
        for (int i = 0; i < questions.size(); i++) {
            // Update question numbers in UI
            VBox questionBox = (VBox) questionsContainer.getChildren().get(i);
            HBox header = (HBox) questionBox.getChildren().get(0);
            Label titleLabel = (Label) header.getChildren().get(0);
            titleLabel.setText("Câu hỏi " + (i + 1));
        }
    }
    
    private void updateQuestionCount() {
        questionCountLabel.setText(questions.size() + " câu hỏi");
    }
    
    private void saveExam() {
        // Validation
        if (examCodeField.getText().trim().isEmpty() || 
            descriptionArea.getText().trim().isEmpty() || 
            gradeComboBox.getValue() == null) {
            showAlert("Lỗi", "Vui lòng điền đầy đủ thông tin đề thi!", Alert.AlertType.WARNING);
            return;
        }
        
        // Check if all questions and answers are filled
        for (Question q : questions) {
            if (q.getQuestionText().trim().isEmpty()) {
                showAlert("Lỗi", "Vui lòng điền đầy đủ nội dung câu hỏi!", Alert.AlertType.WARNING);
                return;
            }
            for (String answer : q.getAnswers()) {
                if (answer.trim().isEmpty()) {
                    showAlert("Lỗi", "Vui lòng điền đầy đủ các đáp án!", Alert.AlertType.WARNING);
                    return;
                }
            }
        }
        
        // Update exam info
        examInfo.setExamCode(examCodeField.getText());
        examInfo.setDescription(descriptionArea.getText());
        examInfo.setGrade(gradeComboBox.getValue());
        
        // TODO: Save to database using Spring Boot service
        System.out.println("Saving exam: " + examInfo.getExamCode());
        System.out.println("Description: " + examInfo.getDescription());
        System.out.println("Grade: " + examInfo.getGrade());
        System.out.println("Questions count: " + questions.size());
        
        showAlert("Thành công", "Lưu đề thi thành công!", Alert.AlertType.INFORMATION);
    }
    
    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}
