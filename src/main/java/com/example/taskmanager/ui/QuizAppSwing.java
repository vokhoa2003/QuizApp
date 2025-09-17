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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.UIManager;
import static javax.swing.WindowConstants.EXIT_ON_CLOSE;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import com.formdev.flatlaf.FlatLightLaf;

/**
 *
 * @author PC
 */
public class QuizAppSwing extends JFrame {
    private JPanel questionPanel;
    private JPanel navPanel;
    private JLabel timerLabel;
    private JButton submitButton;
    private Map<Integer, Integer> answers = new HashMap<>(); // lưu đáp án đã chọn
    private int currentPage = 1;
    private int perPage = 10;
    private int totalQuestions = 30;
    private Timer timer;
    private int duration = 15; // 15 phút
  
    // Thêm: Class để lưu câu hỏi và đáp án
    private static class Question {
        int id;
        String questionText; // Nội dung câu hỏi chính (có thể thêm sau)
        List<String> options; // 4 đáp án A/B/C/D

        Question(int id, String questionText, List<String> options) {
            this.id = id;
            this.questionText = questionText;
            this.options = options;
        }
    }

    // Thêm: Danh sách câu hỏi mẫu (mỗi câu có đáp án khác nhau)
    private List<Question> questions = new ArrayList<>();

    public QuizAppSwing() {
        // Khởi tạo dữ liệu câu hỏi mẫu (bạn có thể thay đổi hoặc load từ file)
        initQuestions();

        setTitle("Bài kiểm tra trắc nghiệm");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        //setSize(1000, 700); // Tăng size cho thoải mái hơn
        setLocationRelativeTo(null); // Center frame
        //setResizable(false); // Disable resize và maximize button
        setAlwaysOnTop(true); // Luôn ở trên cùng, không cho xem app khác
        setLayout(new BorderLayout(10, 10)); // Giảm gap để bớt khoảng trống
        getContentPane().setBackground(new Color(245, 245, 245)); // Nền xám nhạt nhẹ
        setExtendedState(JFrame.MAXIMIZED_BOTH);

        // Thêm: Prevent minimize - Listen event và restore ngay
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowIconified(WindowEvent e) {
                // Khi minimize xảy ra, restore ngay và cảnh báo
                setState(JFrame.NORMAL);
                toFront(); // Đưa lên trên cùng
                JOptionPane.showMessageDialog(QuizAppSwing.this, 
                    "Không được thu nhỏ bài kiểm tra! Hãy tập trung làm bài.", 
                    "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            }
        });

        // Panel thông tin (trái) - Set kích thước cố định để gọn hơn
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setBackground(Color.WHITE);
        infoPanel.setPreferredSize(new Dimension(180, getHeight())); // Giới hạn width 180px
        infoPanel.setBorder(new EmptyBorder(10, 10, 10, 10)); // Giảm padding
        infoPanel.add(new JLabel("Thông tin người làm bài:", SwingConstants.CENTER)); // Center title
        infoPanel.add(Box.createVerticalStrut(10));
        infoPanel.add(new JLabel("Họ và tên: Nguyễn Văn A"));
        infoPanel.add(new JLabel("Lớp: 12A1"));
        infoPanel.add(new JLabel("Môn: Toán"));
        infoPanel.add(new JLabel("Ngày tháng: 08/09/2025"));
        infoPanel.add(new JLabel("Thời gian: 14:00 - 14:15"));
        infoPanel.add(Box.createVerticalGlue());
        add(infoPanel, BorderLayout.WEST);

        // Panel câu hỏi (giữa) - Thêm border và padding
        questionPanel = new JPanel();
        questionPanel.setLayout(new BoxLayout(questionPanel, BoxLayout.Y_AXIS));
        questionPanel.setBackground(Color.WHITE);
        questionPanel.setBorder(new EmptyBorder(20, 5, 20, 20)); // Left padding nhỏ
        JScrollPane scrollPane = new JScrollPane(questionPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        add(scrollPane, BorderLayout.CENTER);

        // Sidebar (phải) - Cải thiện layout
        JPanel sidebar = new JPanel(new BorderLayout(10, 10));
        sidebar.setPreferredSize(new Dimension(280, getHeight()));
        sidebar.setBackground(new Color(240, 248, 255)); // Nền xanh nhạt
        sidebar.setBorder(new EmptyBorder(20, 10, 20, 10));

        // Timer section
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

        // Nav section
        JPanel navSection = new JPanel(new FlowLayout(FlowLayout.LEFT));
        navSection.setBackground(new Color(240, 248, 255));
        JLabel navLabel = new JLabel("📋 Danh sách câu hỏi:");
        navLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        navLabel.setForeground(Color.BLUE);
        navSection.add(navLabel);
        sidebar.add(navSection, BorderLayout.CENTER);

        navPanel = new JPanel(new GridLayout(0, 5, 8, 8)); // Tăng spacing giữa buttons
        navPanel.setBackground(new Color(240, 248, 255));
        for (int i = 1; i <= totalQuestions; i++) {
            final int questionId = i; // Fix lỗi: copy i vào final variable
            JButton btn = new JButton(String.valueOf(i));
            btn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            btn.setPreferredSize(new Dimension(40, 40)); // Làm vuông và nhỏ gọn
            btn.setFocusPainted(false); // Ẩn viền focus
            btn.setBackground(new Color(220, 220, 220)); // Màu xám mặc định
            btn.setForeground(Color.BLACK);
            btn.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    if (!answers.containsKey(questionId)) {
                        btn.setBackground(new Color(200, 220, 255)); // Hover xanh nhạt
                    }
                }
                @Override
                public void mouseExited(MouseEvent e) {
                    if (!answers.containsKey(questionId)) {
                        btn.setBackground(new Color(220, 220, 220));
                    }
                }
            });
            btn.addActionListener(e -> goToQuestion(questionId));
            navPanel.add(btn);
        }
        sidebar.add(new JScrollPane(navPanel), BorderLayout.CENTER); // Wrap nav in scroll nếu cần

        // Submit button - Làm nổi bật hơn
        submitButton = new JButton("Nộp bài");
        submitButton.setBackground(new Color(220, 50, 50)); // Đỏ đậm hơn
        submitButton.setForeground(Color.WHITE);
        submitButton.setFont(new Font("Segoe UI", Font.BOLD, 16));
        submitButton.setFocusPainted(false);
        submitButton.setPreferredSize(new Dimension(200, 40));
        submitButton.addActionListener(e -> submitExam());
        sidebar.add(submitButton, BorderLayout.SOUTH);

        add(sidebar, BorderLayout.EAST);

        // Hiển thị câu hỏi đầu tiên
        renderQuestions();

        // Bắt đầu đếm ngược
        startTimer();

        setVisible(true);
    }

    // Thêm: Phương thức khởi tạo dữ liệu câu hỏi mẫu
    private void initQuestions() {
        // Dữ liệu mẫu cho 30 câu (mỗi câu có đáp án khác nhau - bạn chỉnh sửa theo ý)
        for (int i = 1; i <= totalQuestions; i++) {
            List<String> options = new ArrayList<>();
            String qText = "Nội dung câu hỏi " + i + ": Bạn biết gì về chủ đề này?"; // Có thể thêm nội dung câu hỏi chính
            switch (i % 5) { // Để đa dạng, lặp theo nhóm 5
                case 1:
                    options.add("Lựa chọn A cho câu " + i + ": Đúng nhất");
                    options.add("Lựa chọn B cho câu " + i + ": Sai");
                    options.add("Lựa chọn C cho câu " + i + ": Có thể");
                    options.add("Lựa chọn D cho câu " + i + ": Không đúng");
                    break;
                case 2:
                    options.add("Đáp án A: Toán học cơ bản");
                    options.add("Đáp án B: Lịch sử Việt Nam");
                    options.add("Đáp án C: Vật lý lượng tử");
                    options.add("Đáp án D: Sinh học tế bào");
                    break;
                case 3:
                    options.add("Option A: 2 + 2 = 4");
                    options.add("Option B: Hà Nội là thủ đô");
                    options.add("Option C: Trái đất quay quanh mặt trời");
                    options.add("Option D: Nước sôi ở 100°C");
                    break;
                case 4:
                    options.add("Câu trả lời A: Yes");
                    options.add("Câu trả lời B: No");
                    options.add("Câu trả lời C: Maybe");
                    options.add("Câu trả lời D: Absolutely");
                    break;
                default:
                    options.add("A: Khác biệt 1");
                    options.add("B: Khác biệt 2");
                    options.add("C: Khác biệt 3");
                    options.add("D: Khác biệt 4");
                    break;
            }
            questions.add(new Question(i, qText, options));
        }
    }

    private void renderQuestions() {
        questionPanel.removeAll();
        int start = (currentPage - 1) * perPage + 1;
        int end = Math.min(start + perPage - 1, totalQuestions);

        for (int i = start; i <= end; i++) {
            Question q = questions.get(i - 1); // Lấy câu hỏi theo id
            JPanel qBox = new JPanel();
            qBox.setLayout(new BoxLayout(qBox, BoxLayout.Y_AXIS));
            qBox.setBorder(BorderFactory.createTitledBorder(new LineBorder(Color.GRAY, 1), "Câu hỏi " + i, 0, 0, new Font("Segoe UI", Font.BOLD, 14))); // Border đẹp hơn
            qBox.setBackground(Color.WHITE);
            qBox.setBorder(BorderFactory.createCompoundBorder(
                new EmptyBorder(15, 10, 15, 10), // Padding
                qBox.getBorder()
            ));

            // Thêm: Hiển thị nội dung câu hỏi chính (nếu có)
            JLabel qLabel = new JLabel(q.questionText);
            qLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
            qBox.add(qLabel);
            qBox.add(Box.createVerticalStrut(5));

            ButtonGroup group = new ButtonGroup();
            for (int j = 0; j < 4; j++) {
                JRadioButton option = new JRadioButton(q.options.get(j)); // <-- Chỉnh sửa: Dùng text từ options thực tế
                option.setFont(new Font("Segoe UI", Font.PLAIN, 13));
                option.setBackground(Color.WHITE);
                option.setPreferredSize(new Dimension(400, 30)); // To hơn, dễ click
                int questionId = i;
                int answerIndex = j;
                if (answers.get(questionId) != null && answers.get(questionId) == j) {
                    option.setSelected(true);
                }
                option.addActionListener(e -> {
                    answers.put(questionId, answerIndex);
                    refreshNavPanel();
                });
                group.add(option);
                qBox.add(option);
                qBox.add(Box.createVerticalStrut(5)); // Spacing giữa options
            }
            questionPanel.add(qBox);
            questionPanel.add(Box.createVerticalStrut(20)); // Spacing giữa questions
        }

        // Nav buttons - Cải thiện style
        JPanel navBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        navBtns.setBackground(Color.WHITE);
        if (currentPage > 1) {
            JButton prevBtn = new JButton("⬅ Quay lại");
            prevBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
            prevBtn.setBackground(new Color(100, 150, 255));
            prevBtn.setForeground(Color.WHITE);
            prevBtn.setFocusPainted(false);
            prevBtn.addActionListener(e -> { currentPage--; renderQuestions(); });
            navBtns.add(prevBtn);
        }
        if (end < totalQuestions) {
            JButton nextBtn = new JButton("Tiếp tục ➡");
            nextBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
            nextBtn.setBackground(new Color(100, 150, 255));
            nextBtn.setForeground(Color.WHITE);
            nextBtn.setFocusPainted(false);
            nextBtn.addActionListener(e -> { currentPage++; renderQuestions(); });
            navBtns.add(nextBtn);
        }
        questionPanel.add(navBtns);

        questionPanel.revalidate();
        questionPanel.repaint();
    }

    private void refreshNavPanel() {
        Component[] comps = navPanel.getComponents();
        for (Component c : comps) {
            if (c instanceof JButton btn) {
                int qid = Integer.parseInt(btn.getText());
                if (answers.containsKey(qid)) {
                    btn.setBackground(new Color(50, 150, 50)); // Xanh lá đậm
                    btn.setForeground(Color.WHITE);
                } else {
                    btn.setBackground(new Color(220, 220, 220)); // Xám mặc định
                    btn.setForeground(Color.BLACK);
                }
            }
        }
        navPanel.repaint();
    }

    private void goToQuestion(int qid) {
        currentPage = (int)Math.ceil((double)qid / perPage);
        renderQuestions();
    }

    private void submitExam() {
        JOptionPane.showMessageDialog(this, "Bạn đã nộp bài!\nĐáp án: " + answers, "Nộp bài thành công", JOptionPane.INFORMATION_MESSAGE);
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

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(QuizAppSwing::new);
    }
}