package com.qlsv.view;

import com.qlsv.controller.StudentController;
import com.qlsv.service.StudentService;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;

/**
 * Cửa sổ chính, mỗi chức năng là một tab.
 */
public class MainFrame extends JFrame {

    public MainFrame() {
        super("Quản lý Sinh viên & Điểm số");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        StudentPanel studentPanel = new StudentPanel();
        StudentController studentController = new StudentController(studentPanel, new StudentService());

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Sinh viên", studentPanel);
        // Các tab dưới đây sẽ được thay bằng Panel thật ở #9, #12, #14, #16
        tabs.addTab("Học phần", placeholder("Học phần"));
        tabs.addTab("Đăng ký", placeholder("Đăng ký học phần"));
        tabs.addTab("Nhập điểm", placeholder("Nhập điểm"));
        tabs.addTab("Báo cáo", placeholder("Thống kê và báo cáo"));
        add(tabs, BorderLayout.CENTER);

        setMinimumSize(new Dimension(1000, 600));
        setSize(1200, 700);
        setLocationRelativeTo(null);

        studentController.loadStudents();
    }

    private static JPanel placeholder(String feature) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(new JLabel(feature + ": chức năng đang phát triển", SwingConstants.CENTER),
                BorderLayout.CENTER);
        return panel;
    }
}
