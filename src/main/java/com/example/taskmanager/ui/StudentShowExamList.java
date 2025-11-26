package com.example.taskmanager.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
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
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.border.EmptyBorder;

import com.example.taskmanager.service.ApiService;
import com.example.taskmanager.service.AuthService;

public class StudentShowExamList extends JFrame {
    private final ApiService apiService;
    private final AuthService authService;
    private final int studentId;
    private final String studentName;
    private final int classId; // optional: limit to class
    private final Runnable onBack; // optional callback when pressing Back

    private JPanel examsPanel;
    private JLabel headerTitle;
    private JLabel infoLabel;

    public StudentShowExamList(ApiService apiService, AuthService authService,
                               int studentId, String studentName, int classId,
                               Runnable onBack) {
        this.apiService = apiService;
        this.authService = authService;
        this.studentId = studentId;
        this.studentName = studentName;
        this.classId = classId;
        this.onBack = onBack;

        setTitle("Bài làm của: " + studentName);
        setSize(1000, 650);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        initUI();
        loadAttempts();

        setVisible(true);
    }

    private void initUI() {
        JPanel main = new JPanel(new BorderLayout());
        main.setBackground(new Color(0xF8F9FA));
        main.setBorder(new EmptyBorder(15, 20, 15, 20));

        // Header (match ClassDetailWindow look)
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(0x3B82F6));
        header.setPreferredSize(new Dimension(0, 70));
        header.setBorder(new EmptyBorder(12, 18, 12, 18));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        left.setOpaque(false);

        JButton back = new JButton("Quay Lại");
        back.setFont(new Font("Segoe UI", Font.BOLD, 13));
        back.setForeground(new Color(0x3B82F6));
        back.setBackground(Color.WHITE);
        back.setBorderPainted(false);
        back.setFocusPainted(false);
        back.setCursor(new Cursor(Cursor.HAND_CURSOR));
        back.setPreferredSize(new Dimension(110, 35));
        back.addActionListener(e -> {
            dispose();
            if (onBack != null) {
                try { onBack.run(); } catch (Exception ignored) {}
            }
        });
        left.add(back);

        headerTitle = new JLabel("Bài làm của học sinh");
        headerTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        headerTitle.setForeground(Color.WHITE);
        left.add(headerTitle);

        header.add(left, BorderLayout.WEST);
        main.add(header, BorderLayout.NORTH);

        // Content
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);
        content.setBorder(new EmptyBorder(18, 8, 18, 8));

        infoLabel = new JLabel("Học sinh: " + studentName + "  |  ID: " + studentId);
        infoLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        infoLabel.setForeground(new Color(0x374151));
        infoLabel.setAlignmentX(LEFT_ALIGNMENT);
        content.add(infoLabel);
        content.add(Box.createVerticalStrut(12));

        JLabel title = new JLabel("Danh Sách Bài Kiểm Tra / Bài Làm");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(new Color(0x1F2937));
        title.setAlignmentX(LEFT_ALIGNMENT);
        content.add(title);
        content.add(Box.createVerticalStrut(10));

        examsPanel = new JPanel();
        examsPanel.setLayout(new BoxLayout(examsPanel, BoxLayout.Y_AXIS));
        examsPanel.setOpaque(false);
        examsPanel.setBorder(new EmptyBorder(6, 6, 6, 6));
        examsPanel.setAlignmentX(LEFT_ALIGNMENT);

        JPanel container = new JPanel(new BorderLayout());
        container.setOpaque(false);
        container.add(examsPanel, BorderLayout.NORTH);

        JScrollPane sp = new JScrollPane(container);
        sp.setBorder(BorderFactory.createLineBorder(new Color(0xE5E7EB), 1));
        sp.getViewport().setBackground(new Color(0xF8F9FA));
        sp.setAlignmentX(LEFT_ALIGNMENT);
        sp.setPreferredSize(new Dimension(900, 420));

        content.add(sp);

        main.add(content, BorderLayout.CENTER);
        add(main);
    }

    private void loadAttempts() {
        SwingWorker<List<Map<String, Object>>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Map<String, Object>> doInBackground() throws Exception {
                Map<String, Object> payload = new HashMap<>();
                payload.put("action", "get");
                payload.put("method", "SELECT");
                // fetch attempts joined with exams
                payload.put("table", List.of("exam_attempts", "exams"));
                payload.put("columns", List.of(
                    "exam_attempts.Id as AttemptId",
                    "exam_attempts.ExamId as ExamId",
                    "exam_attempts.StudentId as StudentId",
                    "exam_attempts.AttemptDate as AttemptDate",
                    "exams.ExamName as ExamName"
                ));
                Map<String, Object> join = new HashMap<>();
                join.put("type", "inner");
                join.put("on", List.of("exam_attempts.ExamId = exams.id"));
                payload.put("join", List.of(join));

                Map<String, Object> where = new HashMap<>();
                where.put("exam_attempts.StudentId", studentId);
                if (classId > 0) where.put("exams.ClassId", classId);
                payload.put("where", where);

                payload.put("order", "exam_attempts.Id DESC");

                Object resp = apiService.postApiGetList("/autoGet", payload);
                List<Map<String, Object>> list = normalizeApiList(resp);
                return list;
            }

            @Override
            protected void done() {
                try {
                    List<Map<String, Object>> data = get();
                    SwingUtilities.invokeLater(() -> {
                        examsPanel.removeAll();
                        if (data == null || data.isEmpty()) {
                            JLabel lbl = new JLabel("Chưa có bài làm nào.");
                            lbl.setFont(new Font("Segoe UI", Font.ITALIC, 14));
                            lbl.setForeground(new Color(0x6B7280));
                            examsPanel.add(lbl);
                        } else {
                            for (Map<String, Object> r : data) {
                                int attemptId = toInt(firstNonNull(r, "AttemptId", "id"));
                                int examId = toInt(firstNonNull(r, "ExamId", "exam_attempts.ExamId"));
                                String examName = String.valueOf(firstNonNull(r, "ExamName", "exams.ExamName"));
                                String attemptDate = String.valueOf(firstNonNull(r, "AttemptDate", "attemptDate", "SubmittedDate"));
                                examsPanel.add(createAttemptCard(attemptId, examId, examName, attemptDate));
                                examsPanel.add(Box.createVerticalStrut(10));
                            }
                        }
                        examsPanel.revalidate();
                        examsPanel.repaint();
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(StudentShowExamList.this,
                            "Lỗi khi tải danh sách bài làm: " + e.getMessage(),
                            "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private JPanel createAttemptCard(int attemptId, int examId, String examName, String attemptDate) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0xE5E7EB), 1),
            BorderFactory.createEmptyBorder(12, 12, 12, 12)
        ));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));

        JLabel name = new JLabel(examName);
        name.setFont(new Font("Segoe UI", Font.BOLD, 15));
        name.setForeground(new Color(0x1F2937));

        JLabel meta = new JLabel("ID bài: " + examId + "  •  Thời gian nộp: " + formatDate(attemptDate));
        meta.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        meta.setForeground(new Color(0x6B7280));

        JButton viewBtn = new JButton("Xem chi tiết");
        viewBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        viewBtn.setBackground(new Color(0x8B5CF6));
        viewBtn.setForeground(Color.WHITE);
        viewBtn.setBorderPainted(false);
        viewBtn.setFocusPainted(false);
        viewBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        viewBtn.setPreferredSize(new Dimension(140, 36));
        viewBtn.addActionListener(e -> openExamDetail(attemptId, examId, examName));

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        bottom.setOpaque(false);
        bottom.add(viewBtn);

        card.add(name);
        card.add(Box.createVerticalStrut(6));
        card.add(meta);
        card.add(Box.createVerticalStrut(10));
        card.add(bottom);

        return card;
    }

    private void openExamDetail(int attemptId, int examId, String examName) {
        // First try to get saved score via exam_results by AttemptId
        SwingWorker<Double, Void> worker = new SwingWorker<>() {
            @Override
            protected Double doInBackground() throws Exception {
                try {
                    Map<String, Object> q = new HashMap<>();
                    q.put("action", "get");
                    q.put("method", "SELECT");
                    q.put("table", List.of("exam_results"));
                    q.put("columns", List.of("Score"));
                    Map<String, Object> w = new HashMap<>();
                    w.put("AttemptId", attemptId);
                    w.put("StudentId", studentId);
                    q.put("where", w);
                    q.put("limit", 1);
                    Object resp = apiService.postApiGetList("/autoGet", q);
                    List<Map<String, Object>> list = normalizeApiList(resp);
                    if (list != null && !list.isEmpty()) {
                        Object sc = firstNonNull(list.get(0), "Score", "score");
                        if (sc instanceof Number) return ((Number) sc).doubleValue();
                        if (sc != null) return Double.parseDouble(sc.toString());
                    }
                } catch (Exception ignored) {}
                return 0.0;
            }

            @Override
            protected void done() {
                try {
                    double score = get();
                    // open ExamDetailWindow (existing constructor expects examId, studentId, studentName, examName, finalScore)
                    new ExamDetailWindow(apiService, authService, examId, studentId, studentName, examName != null ? examName : "Bài kiểm tra", score);
                } catch (Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(StudentShowExamList.this,
                            "Không thể mở chi tiết: " + e.getMessage(),
                            "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    // Helpers (copied style from ClassDetailWindow)
    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> normalizeApiList(Object resp) {
        if (resp == null) return Collections.emptyList();
        if (resp instanceof List) return (List<Map<String, Object>>) resp;
        if (resp instanceof Map) {
            Map<?,?> m = (Map<?,?>) resp;
            Object data = m.get("data");
            if (data instanceof List) return (List<Map<String, Object>>) data;
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

    private String formatDate(Object dateObj) {
        if (dateObj == null) return "Chưa có";
        String s = dateObj.toString().trim();
        if (s.isEmpty() || "null".equalsIgnoreCase(s)) return "Chưa có";
        return s;
    }

    private Object firstNonNull(Map<String, Object> m, String... keys) {
        if (m == null) return null;
        for (String k : keys) {
            if (k == null) continue;
            Object v = m.get(k);
            if (v != null) return v;
        }
        return null;
    }

    private int toInt(Object o) {
        if (o == null) return 0;
        if (o instanceof Number) return ((Number)o).intValue();
        try { return Integer.parseInt(o.toString()); } catch (Exception e) { return 0; }
    }
}