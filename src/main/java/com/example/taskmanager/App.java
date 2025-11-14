/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.example.taskmanager;

import com.example.taskmanager.ui.MainWindow;

import javax.swing.*;

public class App {
    public static void main(String[] args) {
        // Đặt look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // ⚙️ Cấu hình proxy để gửi request qua OWASP ZAP
        System.setProperty("http.proxyHost", "localhost");
        System.setProperty("http.proxyPort", "8080");
        System.setProperty("https.proxyHost", "localhost");
        System.setProperty("https.proxyPort", "8080");
        
        // Khởi chạy ứng dụng trong EDT
        SwingUtilities.invokeLater(() -> {
            MainWindow mainWindow = new MainWindow();
            mainWindow.setVisible(true);
        });
    }
}
