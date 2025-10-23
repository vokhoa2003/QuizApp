/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.example.taskmanager.ui;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;

import com.example.taskmanager.auth.GoogleLoginHelper;
import com.example.taskmanager.service.AuthService;
import com.google.api.services.oauth2.model.Userinfo;

public class LoginPanel extends JPanel {
    private final JTextField usernameField;
    private final JPasswordField passwordField;
    private final JButton loginButton;
    private final JButton googleLoginButton;
    private final AuthService authService;
    private final MainWindow mainWindow;
    
    public LoginPanel(AuthService authService, MainWindow mainWindow) {
        this.authService = authService;
        this.mainWindow = mainWindow;
        
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Title Label
        JLabel titleLabel = new JLabel("Task Manager");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setHorizontalAlignment(JLabel.CENTER);
        
        // Form Panel
        JPanel formPanel = new JPanel(new GridLayout(4, 2, 5, 10));
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));
        
        JLabel usernameLabel = new JLabel("Username:");
        usernameField = new JTextField(20);
        
        JLabel passwordLabel = new JLabel("Password:");
        passwordField = new JPasswordField(20);
        
        loginButton = new JButton("Login");
        //loginButton.addActionListener(this::handleLogin);
        
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> System.exit(0));
        
        // Google login button
        googleLoginButton = new JButton("Đăng nhập bằng Google");
        googleLoginButton.addActionListener(this::handleGoogleLogin);
        
        // Separator
        JSeparator separator = new JSeparator(SwingConstants.HORIZONTAL);
        
        formPanel.add(usernameLabel);
        formPanel.add(usernameField);
        formPanel.add(passwordLabel);
        formPanel.add(passwordField);
        formPanel.add(cancelButton);
        formPanel.add(loginButton);
        formPanel.add(new JLabel("Hoặc:"));
        formPanel.add(googleLoginButton);
        
        // Add components to panel
        add(titleLabel, BorderLayout.NORTH);
        add(formPanel, BorderLayout.CENTER);
    }
    
    // private void handleLogin(ActionEvent e) {
    //     String username = usernameField.getText();
    //     String password = new String(passwordField.getPassword());
        
    //     if (username.isEmpty() || password.isEmpty()) {
    //         JOptionPane.showMessageDialog(this, 
    //             "Username and password are required", 
    //             "Login Error", 
    //             JOptionPane.ERROR_MESSAGE);
    //         return;
    //     }
        
    //     loginButton.setEnabled(false);
    //     loginButton.setText("Logging in...");
        
    //     // Sử dụng SwingWorker để không block EDT
    //     SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
    //         @Override
    //         protected Boolean doInBackground() {
    //             return authService.login(username, password);
    //         }
            
    //         @Override
    //         protected void done() {
    //             try {
    //                 boolean success = get();
    //                 if (success) {
    //                     mainWindow.showTaskPanel();
    //                 } else {
    //                     JOptionPane.showMessageDialog(LoginPanel.this, 
    //                         "Invalid username or password", 
    //                         "Login Failed", 
    //                         JOptionPane.ERROR_MESSAGE);
    //                     loginButton.setEnabled(true);
    //                     loginButton.setText("Login");
    //                 }
    //             } catch (Exception ex) {
    //                 ex.printStackTrace();
    //                 JOptionPane.showMessageDialog(LoginPanel.this, 
    //                     "An error occurred: " + ex.getMessage(), 
    //                     "Login Error", 
    //                     JOptionPane.ERROR_MESSAGE);
    //                 loginButton.setEnabled(true);
    //                 loginButton.setText("Login");
    //             }
    //         }
    //     };
        
    //     worker.execute();
    // }
    
    private void handleGoogleLogin(ActionEvent e) {
        googleLoginButton.setEnabled(false);
        googleLoginButton.setText("Đang đăng nhập...");
        
        // Sử dụng SwingWorker để không block EDT
        SwingWorker<Userinfo, Void> worker = new SwingWorker<>() {
            @Override
            protected Userinfo doInBackground() throws Exception {
                // Gọi phương thức login từ GoogleLoginHelper
                return GoogleLoginHelper.login();
            }
            
            @Override
            protected void done() {
                try {
                    Userinfo userInfo = get();
                    if (userInfo != null) {
                        // Đăng nhập vào hệ thống với thông tin Google
                        boolean success = authService.loginWithGoogle(userInfo);
                        
                        if (success) {
                            JOptionPane.showMessageDialog(LoginPanel.this, 
                                "Xin chào " + userInfo.getName() + "\nEmail: " + userInfo.getEmail(), 
                                "Đăng nhập thành công", 
                                JOptionPane.INFORMATION_MESSAGE);
                            
                            // Chuyển tới màn hình tasks
                            mainWindow.showTaskPanel();
                        } else {
                            JOptionPane.showMessageDialog(LoginPanel.this, 
                                "Không thể đăng nhập với tài khoản Google này", 
                                "Lỗi đăng nhập", 
                                JOptionPane.ERROR_MESSAGE);
                        }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(LoginPanel.this, 
                        "Lỗi đăng nhập Google: " + ex.getMessage(), 
                        "Lỗi đăng nhập", 
                        JOptionPane.ERROR_MESSAGE);
                } finally {
                    googleLoginButton.setEnabled(true);
                    googleLoginButton.setText("Đăng nhập bằng Google");
                }
            }
        };
        
        worker.execute();
    }
}