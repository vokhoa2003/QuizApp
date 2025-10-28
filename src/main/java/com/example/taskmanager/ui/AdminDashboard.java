package com.example.taskmanager.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.RoundRectangle2D;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;

import com.example.taskmanager.model.Task;
import com.example.taskmanager.service.ApiService;
import com.example.taskmanager.service.AuthService;

public class AdminDashboard extends JFrame {
    
    private static final Color PRIMARY_COLOR = new Color(79, 70, 229);
    private static final Color BACKGROUND_COLOR = new Color(250, 250, 252);
    private static final Color TEXT_COLOR = new Color(71, 85, 105);
    private static final Color CARD_BG = Color.WHITE;
    private static final Color BORDER_COLOR = new Color(226, 232, 240);
    private static final Color HOVER_COLOR = new Color(99, 90, 249);
    
    private final AuthService authService;
    private final ApiService apiService;
    private final MainWindow mainWindow;
    //private final TaskPanel taskPanel;
    private Task currentAdmin;

    private boolean isMaximized = false;
    private double scaleFactor = 1.0;
    
    private JButton minimizeButton;
    private JButton maximizeButton;
    private JButton closeButton;
    private JLabel titleLabel;
    private JLabel welcomeLabel;

    public AdminDashboard(ApiService apiService, AuthService authService, Task admin, MainWindow mainWindow) {
        this.authService = authService;
        this.apiService = apiService;
        this.mainWindow = mainWindow;
        this.currentAdmin = admin;
        
        //setTitle("Trang Chủ Quản Trị Viên - SecureStudy");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 650);
        setLocationRelativeTo(null);
        
        setUndecorated(true);
        setShape(new RoundRectangle2D.Double(0, 0, 900, 650, 15, 15));
        
        JPanel mainPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BACKGROUND_COLOR);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                g2.setColor(new Color(0, 0, 0, 15));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 15, 15);
                g2.dispose();
            }
        };
        mainPanel.setLayout(new BorderLayout(0, 20));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));
        mainPanel.setBackground(BACKGROUND_COLOR);
        
        // Custom title bar
        JPanel titleBar = createTitleBar();
        mainPanel.add(titleBar, BorderLayout.NORTH);
        
        // Content panel
        JPanel contentPanel = createContentPanel();
        mainPanel.add(contentPanel, BorderLayout.CENTER);
        
        add(mainPanel);
        addWindowDragListener();
    }
    
    private JPanel createTitleBar() {
        JPanel titleBar = new JPanel();
        titleBar.setLayout(new BorderLayout());
        titleBar.setOpaque(false);
        
        // === LEFT: TIÊU ĐỀ ===
    JLabel windowTitle = new JLabel("Trang Chủ Quản Trị Viên - SecureStudy");
    windowTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
    windowTitle.setForeground(TEXT_COLOR);
    windowTitle.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
    titleBar.add(windowTitle, BorderLayout.WEST);
    
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        controlPanel.setOpaque(false);

        minimizeButton = new JButton("–");
        minimizeButton.setForeground(TEXT_COLOR);
        minimizeButton.setBorderPainted(false);
        minimizeButton.setContentAreaFilled(false);
        minimizeButton.setFocusPainted(false);
        minimizeButton.setFont(new Font("Arial", Font.BOLD, 20));
        minimizeButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        minimizeButton.addActionListener(e -> setState(Frame.ICONIFIED));
        
        maximizeButton = new JButton("□");
        maximizeButton.setForeground(TEXT_COLOR);
        maximizeButton.setBorderPainted(false);
        maximizeButton.setContentAreaFilled(false);
        maximizeButton.setFocusPainted(false);
        maximizeButton.setFont(new Font("Arial", Font.BOLD, 20));
        maximizeButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        maximizeButton.addActionListener(e -> toggleMaximizeRestore());

        closeButton = new JButton("×");
        closeButton.setForeground(TEXT_COLOR);
        closeButton.setBorderPainted(false);
        closeButton.setContentAreaFilled(false);
        closeButton.setFocusPainted(false);
        closeButton.setFont(new Font("Arial", Font.BOLD, 20));
        closeButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        closeButton.addActionListener(e -> {
            authService.logout();
            System.exit(0);
        });
        
        controlPanel.add(minimizeButton);
        controlPanel.add(maximizeButton);
        controlPanel.add(closeButton);
        
        titleBar.add(controlPanel, BorderLayout.EAST);
        return titleBar;
    }
    
    private void toggleMaximizeRestore() {
        if (isMaximized) {
            setExtendedState(Frame.NORMAL);
            maximizeButton.setText("□");
            scaleFactor = 1.0;
            setSize(900, 650);
            setShape(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 15, 15));
            setLocationRelativeTo(null);
        } else {
            setExtendedState(Frame.MAXIMIZED_BOTH);
            maximizeButton.setText("❐");
            setShape(null);
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            scaleFactor = Math.min(screenSize.getWidth() / 900, screenSize.getHeight() / 650);
        }
        isMaximized = !isMaximized;
        updateFonts();
    }
    
    private void updateFonts() {
        Font titleFont = new Font("Segoe UI", Font.BOLD, (int) (32 * scaleFactor));
        Font welcomeFont = new Font("Segoe UI", Font.PLAIN, (int) (16 * scaleFactor));
        Font buttonFont = new Font("Arial", Font.BOLD, (int) (20 * scaleFactor));
        
        if (titleLabel != null) titleLabel.setFont(titleFont);
        if (welcomeLabel != null) welcomeLabel.setFont(welcomeFont);
        if (minimizeButton != null) minimizeButton.setFont(buttonFont);
        if (maximizeButton != null) maximizeButton.setFont(buttonFont);
        if (closeButton != null) closeButton.setFont(buttonFont);
    }
    
    private JPanel createContentPanel() {
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setOpaque(false);
        
        // Header section
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setPreferredSize(new Dimension(0, 80));;
        headerPanel.setBackground(new Color(0x2563EB));
        headerPanel.setBorder(new EmptyBorder(15, 30, 15, 30));

        // Left side - Logo and title
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        leftPanel.setOpaque(false);
        
        JLabel logoLabel = new JLabel("📚");
        logoLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 36));
        leftPanel.add(logoLabel);
        
        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));
        titlePanel.setOpaque(false);
        
        titleLabel = new JLabel("SecureStudy");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);
        // titleLabel.setAlignmentX(LEFT_ALIGNMENT);
        
        welcomeLabel = new JLabel("Chào mừng Admin: " + (currentAdmin != null ? currentAdmin.getFullName() : "Quản trị viên"));
        welcomeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        welcomeLabel.setForeground(new Color(0xBFDBFE));
        //welcomeLabel.setAlignmentX(LEFT_ALIGNMENT);
        
        titlePanel.add(titleLabel);
titlePanel.add(welcomeLabel);
leftPanel.add(titlePanel);

headerPanel.add(leftPanel, BorderLayout.WEST);
        
        // Dashboard cards
        JPanel cardsPanel = new JPanel(new GridBagLayout());
        cardsPanel.setOpaque(false);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15);
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        
        // Card 1: Quản lý người dùng
        gbc.gridx = 0;
        gbc.gridy = 0;
        JPanel userManagementCard = createDashboardCard(
            "👥 Quản Lý Người Dùng",
            "Quản lý thông tin tài khoản, phân quyền và trạng thái người dùng",
            PRIMARY_COLOR,
            e -> openUserManagement()
        );
        cardsPanel.add(userManagementCard, gbc);
        
        // Card 2: Quản lý khóa học
        gbc.gridx = 1;
        gbc.gridy = 0;
        JPanel courseManagementCard = createDashboardCard(
            "📚 Quản Lý Khóa/Môn Học",
            "Tạo và quản lý các khóa học, môn học trong hệ thống",
            new Color(34, 197, 94),
            e -> openCourseManagement()
        );
        cardsPanel.add(courseManagementCard, gbc);
        
        // Card 3: Thống kê
        gbc.gridx = 0;
        gbc.gridy = 1;
        JPanel statisticsCard = createDashboardCard(
            "📊 Thống Kê Hệ Thống",
            "Xem báo cáo và thống kê về người dùng, khóa học",
            new Color(251, 146, 60),
            e -> {} // TODO: Implement statistics
        );
        cardsPanel.add(statisticsCard, gbc);
        
        // Card 4: Cài đặt
        gbc.gridx = 1;
        gbc.gridy = 1;
        JPanel settingsCard = createDashboardCard(
            "⚙️ Cài Đặt Hệ Thống",
            "Cấu hình và tùy chỉnh các thiết lập của hệ thống",
            new Color(168, 85, 247),
            e -> {} // TODO: Implement settings
        );
        cardsPanel.add(settingsCard, gbc);
        
        // Logout button
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        bottomPanel.setOpaque(false);
        bottomPanel.setBorder(new EmptyBorder(30, 0, 0, 0));
        
        JButton logoutButton = createModernButton("Đăng Xuất", new Color(239, 68, 68));
        logoutButton.addActionListener(e -> {
            authService.logout();
            this.dispose();
            mainWindow.showLoginPanel();
            mainWindow.setVisible(true);
        });
        bottomPanel.add(logoutButton);
        
        contentPanel.add(headerPanel);
        contentPanel.add(cardsPanel);
        contentPanel.add(bottomPanel);
        
        return contentPanel;
    }
    
    private JPanel createDashboardCard(String title, String description, Color accentColor, 
                                       java.awt.event.ActionListener action) {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(CARD_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.setColor(BORDER_COLOR);
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 12, 12);
                g2.dispose();
            }
        };
        card.setLayout(new BorderLayout(0, 15));
        card.setBorder(new EmptyBorder(25, 25, 25, 25));
        card.setOpaque(false);
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));
        card.setPreferredSize(new Dimension(350, 160));
        
        // Title with accent color bar
        JPanel titlePanel = new JPanel(new BorderLayout(10, 0));
        titlePanel.setOpaque(false);
        
        JPanel accentBar = new JPanel();
        accentBar.setBackground(accentColor);
        accentBar.setPreferredSize(new Dimension(4, 30));
        
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(new Color(30, 41, 59));
        
        titlePanel.add(accentBar, BorderLayout.WEST);
        titlePanel.add(titleLabel, BorderLayout.CENTER);
        
        // Description
        JLabel descLabel = new JLabel("<html>" + description + "</html>");
        descLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        descLabel.setForeground(TEXT_COLOR);
        
        // Action button
        JButton actionButton = new JButton("Mở →");
        actionButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        actionButton.setForeground(Color.BLACK);
        actionButton.setBackground(accentColor);
        actionButton.setBorder(new EmptyBorder(10, 20, 10, 20));
        actionButton.setFocusPainted(false);
        actionButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        actionButton.addActionListener(action);
        
        // Hover effect for button
        actionButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                actionButton.setBackground(accentColor.brighter());
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                actionButton.setBackground(accentColor);
            }
        });
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        buttonPanel.setOpaque(false);
        buttonPanel.add(actionButton);
        
        card.add(titlePanel, BorderLayout.NORTH);
        card.add(descLabel, BorderLayout.CENTER);
        card.add(buttonPanel, BorderLayout.SOUTH);
        
        // Card hover effect
        card.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                card.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(accentColor, 2),
                    new EmptyBorder(23, 23, 23, 23)
                ));
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                card.setBorder(new EmptyBorder(25, 25, 25, 25));
            }
        });
        
        return card;
    }
    
    private JButton createModernButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        button.setForeground(Color.BLACK);
        button.setBackground(bgColor);
        button.setBorder(new EmptyBorder(12, 30, 12, 30));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent evt) {
                button.setBackground(bgColor.brighter());
            }
            public void mouseExited(MouseEvent evt) {
                button.setBackground(bgColor);
            }
        });
        
        return button;
    }
    
    private void openUserManagement() {
    this.setVisible(false);
    try {
        TaskPanel taskPanel = new TaskPanel(apiService, authService, mainWindow, this);
        taskPanel.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                AdminDashboard.this.setVisible(true);
            }
        });
        taskPanel.setVisible(true);
        taskPanel.loadData(); // Gọi sau khi hiển thị
    } catch (Exception ex) {
        ex.printStackTrace();
        JOptionPane.showMessageDialog(this, "Lỗi: " + ex.getMessage());
        this.setVisible(true);
    }
}
    
    private void openCourseManagement() {
    this.setVisible(false);
    CourseManagementPanel panel = new CourseManagementPanel(apiService, authService, mainWindow, this);
    panel.addWindowListener(new java.awt.event.WindowAdapter() {
        public void windowClosing(java.awt.event.WindowEvent e) {
            AdminDashboard.this.setVisible(true);
        }
    });
    panel.setVisible(true);
    panel.loadData();
}
    
    private void addWindowDragListener() {
        final Point dragPoint = new Point();
        
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                dragPoint.x = e.getX();
                dragPoint.y = e.getY();
            }
        });
        
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                Point location = getLocation();
                setLocation(location.x + e.getX() - dragPoint.x,
                            location.y + e.getY() - dragPoint.y);
            }
        });
    }
    
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        SwingUtilities.invokeLater(() -> {
            // For testing only
            AdminDashboard dashboard = new AdminDashboard(null, null, null, null);
            dashboard.setVisible(true);
        });
    }
}