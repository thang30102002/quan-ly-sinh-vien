package com.qlsv.controller;

import com.qlsv.model.Student;
import com.qlsv.service.StudentService;
import com.qlsv.view.StudentPanel;
import com.qlsv.view.StudentTableModel;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

/**
 * Nối {@link StudentPanel} với {@link StudentService}: nhận sự kiện từ Panel,
 * gọi Service và đưa kết quả lên bảng.
 */
public class StudentController {

    private final StudentPanel view;
    private final StudentService service;
    private final StudentTableModel tableModel = new StudentTableModel();

    public StudentController(StudentPanel view, StudentService service) {
        this.view = Objects.requireNonNull(view, "view");
        this.service = Objects.requireNonNull(service, "service");

        view.setTableModel(tableModel);
        view.getBtnRefresh().addActionListener(e -> loadStudents());
    }

    /**
     * Tải lại toàn bộ danh sách sinh viên. Truy vấn CSDL chạy ở luồng nền để giao diện
     * không bị treo; kết quả được đưa lên bảng trên luồng giao diện.
     */
    public void loadStudents() {
        view.getBtnRefresh().setEnabled(false);
        view.setStatusText("Đang tải danh sách sinh viên...");

        new SwingWorker<List<Student>, Void>() {
            @Override
            protected List<Student> doInBackground() {
                return service.findAll();
            }

            @Override
            protected void done() {
                try {
                    List<Student> students = get();
                    tableModel.setStudents(students);
                    view.setStatusText("Đã tải " + students.size() + " sinh viên");
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    view.setStatusText("Đã hủy tải danh sách");
                } catch (ExecutionException e) {
                    view.setStatusText("Không tải được danh sách");
                    showError("Không tải được danh sách sinh viên.\n" + e.getCause().getMessage());
                } finally {
                    view.getBtnRefresh().setEnabled(true);
                }
            }
        }.execute();
    }

    public StudentTableModel getTableModel() {
        return tableModel;
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(view, message, "Lỗi", JOptionPane.ERROR_MESSAGE);
    }
}
