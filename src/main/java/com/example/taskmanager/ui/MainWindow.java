package com.example.taskmanager.ui;

import com.example.taskmanager.service.ApiService;
import com.example.taskmanager.service.AuthService;
import com.google.api.services.oauth2.model.Userinfo;
import com.example.taskmanager.auth.GoogleLoginHelper;

import javax.swing.*;
import javax.swing.plaf.basic.BasicButtonUI;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import javax.imageio.ImageIO;

public class MainWindow extends JFrame {
    
    private static final Color PRIMARY_COLOR = new Color(79, 70, 229); // Indigo color like in your web design
    private static final Color BACKGROUND_COLOR = new Color(250, 250, 252);
    private static final Color TEXT_COLOR = new Color(71, 85, 105);
    private static final Font MAIN_FONT = new Font("Segoe UI", Font.PLAIN, 14);
    private static final Font WELCOME_FONT = new Font("Segoe UI", Font.BOLD, 24);
    private static final String WEB_APP_URL = "http://localhost:8080/customer"; // URL của ứng dụng web
    
    // Services
    private final AuthService authService;
    private final ApiService apiService;
    
    // Layout
    private final CardLayout cardLayout;
    private final JPanel contentPanel;
    
    // Panels
    private final TaskPanel taskPanel;
    
    // Constants
    private static final String LOGIN_PANEL = "LOGIN";
    private static final String TASK_PANEL = "TASKS";
    
    public MainWindow() {
        // Khởi tạo services
        this.authService = new AuthService();
        this.apiService = new ApiService(this.authService);
        
        setTitle("Task Manager");
        setSize(400, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setUndecorated(true); // Remove default window border
        setShape(new RoundRectangle2D.Double(0, 0, 400, 500, 15, 15)); // Rounded corners
        
        // Sử dụng CardLayout để chuyển đổi giữa các panel
        this.cardLayout = new CardLayout();
        this.contentPanel = new JPanel(cardLayout);
        
        // Panel chính với hiệu ứng bóng đổ
        JPanel mainPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Paint background
                g2.setColor(BACKGROUND_COLOR);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                
                // Shadow effect (top border)
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
        
        // Tạo modern login panel
        JPanel modernLoginPanel = createModernLoginPanel();
        
        // Tạo panel nhiệm vụ
        this.taskPanel = new TaskPanel(apiService, authService, this);
        
        // Thêm các panel vào CardLayout
        contentPanel.add(modernLoginPanel, LOGIN_PANEL);
        contentPanel.add(taskPanel, TASK_PANEL);
        
        mainPanel.add(contentPanel, BorderLayout.CENTER);
        add(mainPanel);
        
        // Make the window draggable
        addWindowDragListener();
    }
    
    private JPanel createTitleBar() {
        JPanel titleBar = new JPanel();
        titleBar.setLayout(new BorderLayout());
        titleBar.setOpaque(false);
        
        // Close button
        JButton closeButton = new JButton("×");
        closeButton.setForeground(TEXT_COLOR);
        closeButton.setBorderPainted(false);
        closeButton.setContentAreaFilled(false);
        closeButton.setFocusPainted(false);
        closeButton.setFont(new Font("Arial", Font.BOLD, 20));
        closeButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        closeButton.addActionListener(e -> {
            authService.logout(); // Đảm bảo logout khi đóng ứng dụng
            System.exit(0);
        });
        
        titleBar.add(closeButton, BorderLayout.EAST);
        return titleBar;
    }
    
    private JLabel createLogoLabel() {
        JLabel logoLabel = new JLabel();
        try {
            // Use a placeholder logo (you should replace this with your actual logo)
            ImageIcon icon = new ImageIcon(getClass().getResource("/com/example/taskmanager/resources/logo.png"));
            Image img = icon.getImage().getScaledInstance(120, 120, Image.SCALE_SMOOTH);
            logoLabel.setIcon(new ImageIcon(img));
        } catch (Exception e) {
            // If logo not found, use text instead
            logoLabel.setText("TASK MANAGER");
            logoLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
            logoLabel.setForeground(PRIMARY_COLOR);
        }
        return logoLabel;
    }
    
    private JButton createGoogleLoginButton() {
        JButton loginButton = new JButton("Đăng nhập bằng Google");
        loginButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        loginButton.setForeground(Color.BLACK);
        loginButton.setBackground(Color.WHITE);
        loginButton.setFocusPainted(false);
        loginButton.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(226, 232, 240), 1, true),
            BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));
        loginButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Try to load Google icon
        try {
            ImageIcon googleIcon = new ImageIcon(getClass().getResource("/com/example/taskmanager/resources/google.png"));
            Image img = googleIcon.getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH);
            loginButton.setIcon(new ImageIcon(img));
            loginButton.setIconTextGap(10);
        } catch (Exception e) {
            // If icon not found, continue without it
            System.out.println("Google icon not found");
        }
        
        // Custom button UI
        loginButton.setUI(new BasicButtonUI() {
            @Override
            public void paint(Graphics g, JComponent c) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                AbstractButton b = (AbstractButton) c;
                ButtonModel model = b.getModel();
                
                // Paint button background
                if (model.isPressed()) {
                    g2.setColor(new Color(241, 245, 249));
                } else if (model.isRollover()) {
                    g2.setColor(new Color(248, 250, 252));
                } else {
                    g2.setColor(Color.WHITE);
                }
                
                g2.fillRoundRect(0, 0, c.getWidth(), c.getHeight(), 8, 8);
                
                // Paint button border
                g2.setColor(new Color(226, 232, 240));
                g2.drawRoundRect(0, 0, c.getWidth()-1, c.getHeight()-1, 8, 8);
                
                // Paint text and icon
                super.paint(g2, c);
                g2.dispose();
            }
        });
        
        // Hover effect
        loginButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                loginButton.setBackground(new Color(248, 250, 252));
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                loginButton.setBackground(Color.WHITE);
            }
        });
        
        // Add action - sử dụng GoogleLoginHelper thật
        loginButton.addActionListener(this::handleGoogleLogin);
        
        return loginButton;
    }
    
    private void handleGoogleLogin(ActionEvent e) {
        JButton googleLoginButton = (JButton)e.getSource();
        googleLoginButton.setEnabled(false);
        googleLoginButton.setText("Đang đăng nhập...");
        
        SwingWorker<Userinfo, Void> worker = new SwingWorker<>() {
            @Override
            protected Userinfo doInBackground() throws Exception {
                return GoogleLoginHelper.login();
            }
            
            @Override
            protected void done() {
                try {
                    Userinfo userInfo = get();
                    if (userInfo != null) {
                        boolean success = authService.loginWithGoogle(userInfo);
                        if (success) {
                            // Kiểm tra role
                            String userRole = authService.getLastLoginRole();
                            String userEmail = userInfo.getEmail();
                            if (userRole == null || !userRole.equals("admin")) {
                                String errorMessage = "Bạn chưa được cấp quyền để truy cập ứng dụng này.\n";
                                if (userRole != null && userRole.equals("customer")) {
                                    errorMessage += "Vui lòng truy cập ứng dụng web tại: " + WEB_APP_URL;
                                } else {
                                    errorMessage += "Tài khoản của bạn (" + userEmail + ") không có quyền truy cập.";
                                }
                                JOptionPane.showMessageDialog(MainWindow.this, 
                                    errorMessage, 
                                    "Access Denied", 
                                    JOptionPane.ERROR_MESSAGE);
                                authService.logout();
                            } else {
                                // Role là admin, chuyển sang TaskPanel
                                JOptionPane.showMessageDialog(MainWindow.this, 
                                    "Xin chào " + userInfo.getName() + "\nEmail: " + userInfo.getEmail(), 
                                    "Đăng nhập thành công", 
                                    JOptionPane.INFORMATION_MESSAGE);
                                showTaskPanel();
                            }
                        } else {
                            JOptionPane.showMessageDialog(MainWindow.this, 
                                "Không thể đăng nhập với tài khoản Google này", 
                                "Lỗi đăng nhập", 
                                JOptionPane.ERROR_MESSAGE);
                            authService.logout();
                        }
                    } else {
                        JOptionPane.showMessageDialog(MainWindow.this, 
                            "Đăng nhập Google thất bại", 
                            "Lỗi đăng nhập", 
                            JOptionPane.ERROR_MESSAGE);
                        authService.logout();
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(MainWindow.this, 
                        "Lỗi đăng nhập Google: " + ex.getMessage(), 
                        "Lỗi đăng nhập", 
                        JOptionPane.ERROR_MESSAGE);
                    authService.logout();
                } finally {
                    googleLoginButton.setEnabled(true);
                    googleLoginButton.setText("Đăng nhập bằng Google");
                }
            }
        };
        worker.execute();
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
    
    private JPanel createModernLoginPanel() {
        // Center panel for login components
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setOpaque(false);
        
        // Logo
        JLabel logoLabel = createLogoLabel();
        logoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        centerPanel.add(logoLabel);
        centerPanel.add(Box.createVerticalStrut(30));
        
        // Welcome text
        JLabel welcomeLabel = new JLabel("Welcome!");
        welcomeLabel.setFont(WELCOME_FONT);
        welcomeLabel.setForeground(new Color(30, 41, 59));
        welcomeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        centerPanel.add(welcomeLabel);
        
        JLabel subtitleLabel = new JLabel("Please login using your Google account.");
        subtitleLabel.setFont(MAIN_FONT);
        subtitleLabel.setForeground(TEXT_COLOR);
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        centerPanel.add(subtitleLabel);
        centerPanel.add(Box.createVerticalStrut(40));
        
        // Google Login button
        JButton googleLoginButton = createGoogleLoginButton();
        googleLoginButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        centerPanel.add(googleLoginButton);
        centerPanel.add(Box.createVerticalStrut(30));
        
        // Footer text
        JLabel footerLabel = new JLabel("Only Google login is supported.");
        footerLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        footerLabel.setForeground(new Color(148, 163, 184));
        footerLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        centerPanel.add(footerLabel);
        
        return centerPanel;
    }
    
    public void showLoginPanel() {
        cardLayout.show(contentPanel, LOGIN_PANEL);
    }
    
    public void showTaskPanel() {
        // Resize window for task panel
        setSize(800, 600);
        setLocationRelativeTo(null);
        
        // Remove rounded shape for task panel
        setShape(null);
        
        cardLayout.show(contentPanel, TASK_PANEL);
        // Refresh tasks khi hiển thị panel
        taskPanel.refreshUsers();
    }
    
    // Main method for testing
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        SwingUtilities.invokeLater(() -> {
            MainWindow mainWindow = new MainWindow();
            mainWindow.setVisible(true);
        });
    }
}