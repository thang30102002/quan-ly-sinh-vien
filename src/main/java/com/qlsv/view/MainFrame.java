package com.qlsv.view;

import com.qlsv.controller.ClassSectionController;
import com.qlsv.controller.CourseController;
import com.qlsv.controller.SemesterController;
import com.qlsv.controller.StudentController;
import com.qlsv.service.ClassSectionService;
import com.qlsv.service.CourseService;
import com.qlsv.service.ScheduleService;
import com.qlsv.service.SemesterService;
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

        CourseService courseService = new CourseService();
        SemesterService semesterService = new SemesterService();

        StudentPanel studentPanel = new StudentPanel();
        StudentController studentController = new StudentController(studentPanel, new StudentService());

        CoursePanel coursePanel = new CoursePanel();
        CourseController courseController = new CourseController(coursePanel, courseService);

        SemesterPanel semesterPanel = new SemesterPanel();
        SemesterController semesterController = new SemesterController(semesterPanel, semesterService);

        ClassSectionPanel sectionPanel = new ClassSectionPanel();
        ClassSectionController sectionController = new ClassSectionController(sectionPanel,
                semesterService, courseService, new ClassSectionService(), new ScheduleService());

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Sinh viên", studentPanel);
        tabs.addTab("Học phần", coursePanel);
        tabs.addTab("Học kỳ", semesterPanel);
        tabs.addTab("Lớp học phần", sectionPanel);
        // Các tab dưới đây sẽ được thay bằng Panel thật ở #12, #14, #16
        tabs.addTab("Đăng ký", placeholder("Đăng ký học phần"));
        tabs.addTab("Nhập điểm", placeholder("Nhập điểm"));
        tabs.addTab("Báo cáo", placeholder("Thống kê và báo cáo"));
        add(tabs, BorderLayout.CENTER);

        // Học phần, học kỳ có thể vừa đổi ở tab khác nên nạp lại khi mở tab Lớp học phần
        tabs.addChangeListener(e -> {
            if (tabs.getSelectedComponent() == sectionPanel) {
                sectionController.reloadLookups();
            }
        });

        setMinimumSize(new Dimension(1100, 600));
        setSize(1320, 720);
        setLocationRelativeTo(null);

        studentController.loadStudents();
        courseController.loadCourses();
        semesterController.loadSemesters();
    }

    private static JPanel placeholder(String feature) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(new JLabel(feature + ": chức năng đang phát triển", SwingConstants.CENTER),
                BorderLayout.CENTER);
        return panel;
    }
}
