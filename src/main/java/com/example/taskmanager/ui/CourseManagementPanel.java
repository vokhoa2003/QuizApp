package com.example.taskmanager.ui;

import com.example.taskmanager.model.*;
import com.example.taskmanager.service.ApiService;
import com.example.taskmanager.service.AuthService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CourseManagementPanel extends JFrame {
    private final ApiService apiService;
    private final AuthService authService;
    private final MainWindow mainWindow;
    private final AdminDashboard adminDashboard;

    private JTable classTable;
    private DefaultTableModel classModel;
    private JButton refreshBtn, addBtn, editBtn, deleteBtn, viewMembersBtn, logoutBtn;
    private List<ClassRoom> allClasses = new ArrayList<>();
    private List<Teacher> allTeachers = new ArrayList<>();
    private List<Student> allStudents = new ArrayList<>();
    private Point dragStart = null;

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    // Colors
    private static final Color BG = new Color(248, 250, 252);
    private static final Color CARD = Color.WHITE;
    private static final Color BORDER = new Color(226, 232, 240);
    private static final Color PRIMARY = new Color(59, 130, 246);
    private static final Color SUCCESS = new Color(34, 197, 94);
    private static final Color DANGER = new Color(239, 68, 68);
    private static final Color WARNING = new Color(251, 146, 60);
    private static final Color TEXT = new Color(15, 23, 42);
    private static final Color TEXT_LIGHT = new Color(100, 116, 139);

    public CourseManagementPanel(ApiService apiService, AuthService authService, MainWindow mainWindow, AdminDashboard adminDashboard) {
        this.apiService = apiService;
        this.authService = authService;
        this.mainWindow = mainWindow;
        this.adminDashboard = adminDashboard;

        setupFrame();
        initComponents();
        addWindowDrag();
    }

    private void setupFrame() {
        setTitle("Quản Lý Lớp Học - SecureStudy");
        setSize(1300, 750);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        setUndecorated(true);

        JPanel root = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2.setColor(new Color(0, 0, 0, 20));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
                g2.dispose();
            }
        };
        root.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        add(root);
    }

    private void initComponents() {
        JPanel main = (JPanel) getContentPane().getComponent(0);
        main.add(createTitleBar(), BorderLayout.NORTH);

        JPanel content = new JPanel(new BorderLayout(15, 15));
        content.setOpaque(false);

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JLabel title = new JLabel("QUẢN LÝ LỚP HỌC");
        title.setFont(new Font("Segoe UI", Font.BOLD, 26));
        title.setForeground(TEXT);
        JLabel subtitle = new JLabel("Thêm, sửa, xóa lớp học và quản lý giáo viên - học sinh");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitle.setForeground(TEXT_LIGHT);

        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setOpaque(false);
        textPanel.add(title);
        textPanel.add(Box.createVerticalStrut(6));
        textPanel.add(subtitle);
        header.add(textPanel, BorderLayout.WEST);

        // Buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        btnPanel.setOpaque(false);
        refreshBtn = btn("Làm mới", PRIMARY, e -> loadData());
        addBtn = btn("Thêm lớp", SUCCESS, e -> showClassDialog(null));
        editBtn = btn("Sửa lớp", WARNING, e -> editSelectedClass());
        deleteBtn = btn("Xóa lớp", DANGER, e -> deleteSelectedClass());
        viewMembersBtn = btn("Xem thành viên", PRIMARY, e -> viewClassMembers());
        logoutBtn = btn("Đăng xuất", TEXT_LIGHT, e -> {
            authService.logout();
            setVisible(false);
            mainWindow.showLoginPanel();
        });

        btnPanel.add(refreshBtn); btnPanel.add(addBtn); btnPanel.add(editBtn);
        btnPanel.add(deleteBtn); btnPanel.add(viewMembersBtn);
        btnPanel.add(Box.createHorizontalStrut(20)); btnPanel.add(logoutBtn);

        // Table
        String[] cols = {"ID", "Tên Lớp", "Mô Tả", "Ngày Tạo", "Cập Nhật"};
        classModel = new DefaultTableModel(cols, 0) { @Override public boolean isCellEditable(int r, int c) { return false; } };
        classTable = new JTable(classModel);
        styleTable();

        JScrollPane scroll = new JScrollPane(classTable);
        scroll.setBorder(BorderFactory.createLineBorder(BORDER, 1));
        scroll.getViewport().setBackground(CARD);

        content.add(header, BorderLayout.NORTH);
        content.add(btnPanel, BorderLayout.CENTER);
        content.add(scroll, BorderLayout.SOUTH);

        main.add(content, BorderLayout.CENTER);
    }

    private JPanel createTitleBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setOpaque(false);
        bar.setBorder(new EmptyBorder(10, 15, 10, 15));

        JLabel title = new JLabel("Quản Lý Lớp Học");
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        title.setForeground(TEXT);
        bar.add(title, BorderLayout.WEST);

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        controls.setOpaque(false);
        JButton min = controlBtn("–", e -> setState(Frame.ICONIFIED));
        JButton close = controlBtn("×", e -> {
            setVisible(false);
            if (adminDashboard != null) adminDashboard.setVisible(true);
        });
        controls.add(min); controls.add(close);
        bar.add(controls, BorderLayout.EAST);

        return bar;
    }

    private JButton controlBtn(String text, ActionListener action) {
        JButton b = new JButton(text);
        b.setFont(new Font("Arial", Font.BOLD, 20));
        b.setForeground(new Color(71, 85, 105));
        b.setBorderPainted(false);
        b.setContentAreaFilled(false);
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.addActionListener(action);
        return b;
    }

    private void addWindowDrag() {
    addMouseListener(new MouseAdapter() {
        @Override
        public void mousePressed(MouseEvent e) {
            dragStart = e.getPoint();
        }
    });

    addMouseMotionListener(new MouseMotionAdapter() {
        @Override
        public void mouseDragged(MouseEvent e) {
            if (dragStart != null) {
                Point loc = getLocation();
                setLocation(loc.x + e.getX() - dragStart.x, loc.y + e.getY() - dragStart.y);
            }
        }
    });
}
    private JButton btn(String text, Color bg, ActionListener action) {
        JButton b = new JButton(text);
        b.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        b.setForeground(Color.BLACK);
        b.setBackground(bg);
        b.setBorder(new EmptyBorder(10, 22, 10, 22));
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.addActionListener(action);
        b.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { b.setBackground(bg.brighter()); }
            public void mouseExited(MouseEvent e) { b.setBackground(bg); }
        });
        return b;
    }

    private void styleTable() {
        classTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        classTable.setRowHeight(42);
        classTable.setShowGrid(false);
        classTable.setSelectionBackground(new Color(219, 234, 254));
        classTable.setSelectionForeground(TEXT);

        JTableHeader h = classTable.getTableHeader();
        h.setFont(new Font("Segoe UI", Font.BOLD, 13));
        h.setBackground(new Color(241, 245, 249));
        h.setForeground(TEXT);
        h.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, BORDER));
        h.setPreferredSize(new Dimension(0, 45));

        DefaultTableCellRenderer r = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int col) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
                if (!isSelected) c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(249, 250, 251));
                setBorder(new EmptyBorder(5, 12, 5, 12));
                return c;
            }
        };
        for (int i = 0; i < classTable.getColumnCount(); i++) {
            classTable.getColumnModel().getColumn(i).setCellRenderer(r);
        }
    }

    public void loadData() {
        new SwingWorker<Void, Void>() {
            @Override protected Void doInBackground() throws Exception {
                allClasses = apiService.getClasses();
                allTeachers = apiService.getTeachers();
                allStudents = apiService.getStudents();
                return null;
            }
            @Override protected void done() {
                try {
                    get();
                    updateClassTable();
                } catch (Exception e) {
                    e.printStackTrace();
                    msg("Lỗi tải dữ liệu: " + e.getMessage(), "Lỗi");
                }
            }
        }.execute();
    }

    private void updateClassTable() {
        classModel.setRowCount(0);
        for (ClassRoom c : allClasses) {
            classModel.addRow(new Object[]{
                    c.getId(),
                    c.getName(),
                    c.getDescription() != null ? c.getDescription() : "N/A",
                    formatDate(c.getCreateDate()),
                    formatDate(c.getUpdateDate())
            });
        }
    }

    private String formatDate(LocalDateTime date) {
        return date != null ? date.format(DATE_FORMAT) : "N/A";
    }

    private void showClassDialog(ClassRoom classObj) {
        boolean edit = classObj != null;
        JDialog d = new JDialog(this, edit ? "Sửa Lớp Học" : "Thêm Lớp Học", true);
        d.setSize(600, 400);
        d.setLocationRelativeTo(this);

        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(CARD);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1),
                new EmptyBorder(25, 25, 25, 25)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        JTextField name = field(p, "Tên lớp", gbc, 0, edit ? classObj.getName() : "");
        JTextArea desc = new JTextArea(edit ? classObj.getDescription() : "", 4, 30);
        desc.setLineWrap(true); desc.setWrapStyleWord(true);
        JScrollPane descScroll = new JScrollPane(desc);
        descScroll.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1),
                new EmptyBorder(8, 8, 8, 8)
        ));
        addField(p, "Mô tả", descScroll, gbc, 1);

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btns.setBackground(BG);
        JButton save = btn("Lưu", SUCCESS, e -> {
            if (name.getText().trim().isEmpty()) {
                msg("Tên lớp không được để trống!", "Lỗi");
                return;
            }
            ClassRoom c = edit ? classObj : new ClassRoom();
            c.setName(name.getText().trim());
            c.setDescription(desc.getText().trim());
            if (!edit) {
                c.setCreateDate(LocalDateTime.now());
            }
            c.setUpdateDate(LocalDateTime.now());

            if (edit) updateClass(c); else createClass(c);
            d.dispose();
        });
        JButton cancel = btn("Hủy", TEXT_LIGHT, e -> d.dispose());
        btns.add(save); btns.add(cancel);

        d.setLayout(new BorderLayout());
        d.add(p, BorderLayout.CENTER);
        d.add(btns, BorderLayout.SOUTH);
        d.setVisible(true);
    }

    private void editSelectedClass() {
        int row = classTable.getSelectedRow();
        if (row >= 0) {
            ClassRoom c = allClasses.get(row);
            showClassDialog(c);
        } else {
            msg("Vui lòng chọn lớp để sửa!", "Thông báo");
        }
    }

    private void deleteSelectedClass() {
        int row = classTable.getSelectedRow();
        if (row >= 0) {
            ClassRoom c = allClasses.get(row);
            int opt = JOptionPane.showConfirmDialog(this,
                    "Xóa lớp: " + c.getName() + "?\nTất cả GV & HS sẽ bị xóa!", "Xác nhận", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (opt == JOptionPane.YES_OPTION) {
                deleteClass(c.getId());
            }
        } else {
            msg("Vui lòng chọn lớp để xóa!", "Thông báo");
        }
    }

    private void viewClassMembers() {
        int row = classTable.getSelectedRow();
        if (row < 0) {
            msg("Vui lòng chọn lớp để xem thành viên!", "Thông báo");
            return;
        }
        ClassRoom selectedClass = allClasses.get(row);

        JDialog d = new JDialog(this, "Thành Viên Lớp: " + selectedClass.getName(), true);
        d.setSize(900, 600);
        d.setLocationRelativeTo(this);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        // Giáo viên
        List<Teacher> teachers = allTeachers.stream()
                .filter(t -> t.getClassId() != null && t.getClassId().equals(selectedClass.getId()))
                .collect(Collectors.toList());
        JTable teacherTable = createMemberTable(teachers, true);
        JScrollPane teacherScroll = new JScrollPane(teacherTable);
        JPanel teacherPanel = createMemberPanel(teacherScroll, selectedClass.getId(), true);
        tabs.addTab("Giáo Viên (" + teachers.size() + ")", teacherPanel);

        // Học sinh
        List<Student> students = allStudents.stream()
                .filter(s -> s.getClassId() != null && s.getClassId().equals(selectedClass.getId()))
                .collect(Collectors.toList());
        JTable studentTable = createMemberTable(students, false);
        JScrollPane studentScroll = new JScrollPane(studentTable);
        JPanel studentPanel = createMemberPanel(studentScroll, selectedClass.getId(), false);
        tabs.addTab("Học Sinh (" + students.size() + ")", studentPanel);

        d.add(tabs);
        d.setVisible(true);
    }

    private JTable createMemberTable(List<?> members, boolean isTeacher) {
        String[] cols = isTeacher ?
                new String[]{"ID", "Tên GV", "Tài khoản ID", "Ngày tạo"} :
                new String[]{"ID", "Tên HS", "Tài khoản ID", "Ngày tạo"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) { @Override public boolean isCellEditable(int r, int c) { return false; } };
        JTable table = new JTable(model);
        styleTable(table);

        for (Object m : members) {
            if (isTeacher) {
                Teacher t = (Teacher) m;
                model.addRow(new Object[]{t.getId(), t.getName(), t.getIdAccount(), formatDate(t.getCreateDate())});
            } else {
                Student s = (Student) m;
                model.addRow(new Object[]{s.getId(), s.getName(), s.getIdAccount(), formatDate(s.getCreateDate())});
            }
        }
        return table;
    }

    private JPanel createMemberPanel(JScrollPane scroll, Long classId, boolean isTeacher) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(CARD);
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        top.setOpaque(false);
        JButton addBtn = btn("Thêm " + (isTeacher ? "GV" : "HS"), SUCCESS, e -> addMember(classId, isTeacher));
        top.add(addBtn);
        panel.add(top, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }

    private void addMember(Long classId, boolean isTeacher) {
        JDialog dialog = new JDialog(this, "Thêm " + (isTeacher ? "Giáo Viên" : "Học Sinh"), true);
        dialog.setSize(500, 300);
        dialog.setLocationRelativeTo(this);

        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(CARD);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1),
                new EmptyBorder(25, 25, 25, 25)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        JTextField nameField = field(p, "Tên " + (isTeacher ? "GV" : "HS"), gbc, 0, "");
        JTextField accountIdField = field(p, "ID Tài khoản", gbc, 1, "");

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btns.setBackground(BG);
        JButton save = btn("Lưu", SUCCESS, e -> {
            String name = nameField.getText().trim();
            String idStr = accountIdField.getText().trim();
            if (name.isEmpty() || idStr.isEmpty()) {
                msg("Vui lòng nhập đầy đủ!", "Lỗi");
                return;
            }
            try {
                Long idAccount = Long.parseLong(idStr);
                if (isTeacher) {
                    Teacher t = new Teacher(idAccount, name, classId);
                    createTeacher(t);
                } else {
                    Student s = new Student(idAccount, name, classId);
                    createStudent(s);
                }
                dialog.dispose();
            } catch (NumberFormatException ex) {
                msg("ID tài khoản phải là số!", "Lỗi");
            }
        });
        JButton cancel = btn("Hủy", TEXT_LIGHT, e -> dialog.dispose());
        btns.add(save); btns.add(cancel);

        dialog.setLayout(new BorderLayout());
        dialog.add(p, BorderLayout.CENTER);
        dialog.add(btns, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    // CRUD Methods
    private void createClass(ClassRoom c) { saveClass(c, "Thêm lớp thành công!", false); }
    private void updateClass(ClassRoom c) { saveClass(c, "Cập nhật thành công!", true); }
    private void saveClass(ClassRoom c, String msg, boolean update) {
        new SwingWorker<Boolean, Void>() {
            @Override protected Boolean doInBackground() throws Exception {
                return update ? apiService.updateClass(c) : apiService.createClass(c);
            }
            @Override protected void done() {
                try { if (get()) { loadData(); msg(msg, "Thành công"); } else msg("Thất bại!", "Lỗi"); }
                catch (Exception e) { e.printStackTrace(); msg("Lỗi: " + e.getMessage(), "Lỗi"); }
            }
        }.execute();
    }

    private void deleteClass(Long id) {
        new SwingWorker<Boolean, Void>() {
            @Override protected Boolean doInBackground() throws Exception { return apiService.deleteClass(id); }
            @Override protected void done() {
                try { if (get()) { loadData(); msg("Xóa lớp thành công!", "Thành công"); } else msg("Xóa thất bại!", "Lỗi"); }
                catch (Exception e) { e.printStackTrace(); msg("Lỗi: " + e.getMessage(), "Lỗi"); }
            }
        }.execute();
    }

    private void createTeacher(Teacher t) { saveMember(t, "Thêm GV thành công!", true, false); }
    private void createStudent(Student s) { saveMember(s, "Thêm HS thành công!", false, false); }

    private <T> void saveMember(T member, String msg, boolean isTeacher, boolean update) {
        new SwingWorker<Boolean, Void>() {
            @Override protected Boolean doInBackground() throws Exception {
                if (isTeacher) {
                    Teacher t = (Teacher) member;
                    return update ? apiService.updateTeacher(t) : apiService.createTeacher(t);
                } else {
                    Student s = (Student) member;
                    return update ? apiService.updateStudent(s) : apiService.createStudent(s);
                }
            }
            @Override protected void done() {
                try { if (get()) { loadData(); msg(msg, "Thành công"); } else msg("Thất bại!", "Lỗi"); }
                catch (Exception e) { e.printStackTrace(); msg("Lỗi: " + e.getMessage(), "Lỗi"); }
            }
        }.execute();
    }

    private JTextField field(JPanel p, String label, GridBagConstraints gbc, int row, String val) {
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.3;
        p.add(new JLabel(label + ":"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.7;
        JTextField f = new JTextField(val);
        f.setPreferredSize(new Dimension(280, 38));
        f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER, 1),
                new EmptyBorder(8, 12, 8, 12)
        ));
        p.add(f, gbc);
        return f;
    }

    private void addField(JPanel p, String label, JComponent comp, GridBagConstraints gbc, int row) {
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.3;
        p.add(new JLabel(label + ":"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.7;
        p.add(comp, gbc);
    }

    private void styleTable(JTable table) {
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setRowHeight(40);
        table.setShowGrid(false);
        table.setSelectionBackground(new Color(219, 234, 254));
        table.setSelectionForeground(TEXT);

        JTableHeader h = table.getTableHeader();
        h.setFont(new Font("Segoe UI", Font.BOLD, 13));
        h.setBackground(new Color(241, 245, 249));
        h.setForeground(TEXT);
        h.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, BORDER));
        h.setPreferredSize(new Dimension(0, 45));

        DefaultTableCellRenderer r = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
                Component comp = super.getTableCellRendererComponent(t, v, s, f, r, c);
                if (!s) comp.setBackground(r % 2 == 0 ? Color.WHITE : new Color(249, 250, 251));
                setBorder(new EmptyBorder(5, 12, 5, 12));
                return comp;
            }
        };
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(r);
        }
    }

    private void msg(String m, String t) {
        JOptionPane.showMessageDialog(this, m, t,
                t.contains("Lỗi") ? JOptionPane.ERROR_MESSAGE : JOptionPane.INFORMATION_MESSAGE);
    }

}