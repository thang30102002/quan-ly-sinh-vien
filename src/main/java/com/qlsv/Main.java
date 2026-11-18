package com.qlsv;

import com.qlsv.view.MainFrame;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * Điểm khởi động phần mềm: mở cửa sổ chính trên luồng giao diện của Swing.
 */
public class Main {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            useSystemLookAndFeel();
            new MainFrame().setVisible(true);
        });
    }

    /** Dùng giao diện của hệ điều hành; nếu không được thì giữ giao diện mặc định của Swing. */
    private static void useSystemLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            System.err.println("Không đặt được giao diện hệ thống: " + e.getMessage());
        }
    }
}
