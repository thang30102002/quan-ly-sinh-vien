package com.qlsv.controller;

import com.qlsv.exception.BusinessException;
import com.qlsv.exception.ValidationException;
import com.qlsv.model.Gender;
import com.qlsv.model.Student;
import com.qlsv.model.StudentStatus;
import com.qlsv.service.StudentService;
import com.qlsv.view.StudentPanel;
import com.qlsv.view.StudentTableModel;

import java.awt.Component;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

public class StudentController {

    public interface DialogHandler {
        void showMessage(Component parent, Object message, String title, int messageType);
        int showConfirm(Component parent, Object message, String title, int optionType);
    }

    private final StudentPanel view;
    private final StudentService service;
    private final StudentTableModel tableModel = new StudentTableModel();
    private Integer selectedStudentId = null;
    private boolean isUpdatingFilter = false;

    private DialogHandler dialogHandler = new DialogHandler() {
        @Override
        public void showMessage(Component parent, Object message, String title, int messageType) {
            JOptionPane.showMessageDialog(parent, message, title, messageType);
        }

        @Override
        public int showConfirm(Component parent, Object message, String title, int optionType) {
            return JOptionPane.showConfirmDialog(parent, message, title, optionType, JOptionPane.QUESTION_MESSAGE);
        }
    };

    public StudentController(StudentPanel view, StudentService service) {
        this.view = Objects.requireNonNull(view, "view");
        this.service = Objects.requireNonNull(service, "service");

        view.setTableModel(tableModel);
        initEvents();
    }

    public void setDialogHandler(DialogHandler dialogHandler) {
        this.dialogHandler = Objects.requireNonNull(dialogHandler, "dialogHandler");
    }

    private void initEvents() {
        view.getBtnAdd().addActionListener(e -> onAddStudent());
        view.getBtnUpdate().addActionListener(e -> onUpdateStudent());
        view.getBtnDelete().addActionListener(e -> onDeleteStudent());
        view.getBtnRefresh().addActionListener(e -> onRefresh());

        view.getBtnSearch().addActionListener(e -> onSearch());
        view.getTxtSearch().addActionListener(e -> onSearch());
        view.getCboClassFilter().addActionListener(e -> {
            if (!isUpdatingFilter) {
                onSearch();
            }
        });

        view.getTblStudents().getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                onRowSelected();
            }
        });
    }

    public void onRowSelected() {
        int viewRow = view.getTblStudents().getSelectedRow();
        if (viewRow >= 0) {
            int modelRow = view.getTblStudents().convertRowIndexToModel(viewRow);
            Student s = tableModel.getStudentAt(modelRow);
            selectedStudentId = s.getId();
            view.setStudentToForm(s);
        } else {
            selectedStudentId = null;
        }
    }

    private Student readStudentFromForm(boolean isNew) {
        String code = view.getTxtStudentCode().getText();
        String fullName = view.getTxtFullName().getText();
        String dobStr = view.getTxtDob().getText().trim();
        if (dobStr.isEmpty()) {
            throw new ValidationException("Ngày sinh là bắt buộc (BR-17)");
        }
        LocalDate dob;
        try {
            dob = LocalDate.parse(dobStr, StudentPanel.DATE_FORMAT);
        } catch (DateTimeParseException ex) {
            throw new ValidationException("Ngày sinh không đúng định dạng " + StudentPanel.DATE_PATTERN + ", ví dụ 01/05/2006 (BR-17)");
        }

        Gender gender = (Gender) view.getCboGender().getSelectedItem();
        String email = view.getTxtEmail().getText();
        String phone = view.getTxtPhone().getText();
        String className = view.getTxtClassName().getText();

        int intakeYear;
        Object yearVal = view.getSpnIntakeYear().getValue();
        if (yearVal instanceof Number num) {
            intakeYear = num.intValue();
        } else {
            throw new ValidationException("Khóa (năm nhập học) không hợp lệ");
        }

        StudentStatus status = (StudentStatus) view.getCboStatus().getSelectedItem();

        if (isNew) {
            return new Student(code, fullName, dob, gender, email, phone, className, intakeYear, status);
        } else {
            if (selectedStudentId == null) {
                throw new ValidationException("Chưa chọn sinh viên cần sửa");
            }
            return new Student(selectedStudentId, code, fullName, dob, gender, email, phone, className, intakeYear, status);
        }
    }

    public void onAddStudent() {
        try {
            Student s = readStudentFromForm(true);
            service.add(s);
            dialogHandler.showMessage(view, "Thêm sinh viên thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            view.clearForm();
            selectedStudentId = null;
            loadStudents();
        } catch (ValidationException | BusinessException ex) {
            showError(ex.getMessage());
        } catch (Exception ex) {
            showError("Đã xảy ra lỗi: " + ex.getMessage());
        }
    }

    public void onUpdateStudent() {
        if (selectedStudentId == null) {
            showWarning("Vui lòng chọn một sinh viên từ bảng để sửa!");
            return;
        }
        try {
            Student s = readStudentFromForm(false);
            service.update(s);
            dialogHandler.showMessage(view, "Cập nhật sinh viên thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            loadStudents();
        } catch (ValidationException | BusinessException ex) {
            showError(ex.getMessage());
        } catch (Exception ex) {
            showError("Đã xảy ra lỗi: " + ex.getMessage());
        }
    }

    public void onDeleteStudent() {
        int viewRow = view.getTblStudents().getSelectedRow();
        if (viewRow < 0 || selectedStudentId == null) {
            showWarning("Vui lòng chọn một sinh viên từ bảng để xóa!");
            return;
        }
        int modelRow = view.getTblStudents().convertRowIndexToModel(viewRow);
        Student student = tableModel.getStudentAt(modelRow);

        int confirm = dialogHandler.showConfirm(
            view,
            "Bạn có chắc chắn muốn xóa sinh viên " + student.getFullName() + " (" + student.getStudentCode() + ")?",
            "Xác nhận xóa",
            JOptionPane.YES_NO_OPTION
        );
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            StudentService.DeleteResult result = service.delete(student.getId());
            if (result == StudentService.DeleteResult.DELETED) {
                dialogHandler.showMessage(view, "Đã xóa sinh viên thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            } else {
                dialogHandler.showMessage(view, "Sinh viên đã có điểm nên không thể xóa hẳn. Trạng thái đã được chuyển sang \"Thôi học\" (BR-14).", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            }
            view.clearForm();
            selectedStudentId = null;
            loadStudents();
        } catch (BusinessException ex) {
            showError(ex.getMessage());
        } catch (Exception ex) {
            showError("Đã xảy ra lỗi: " + ex.getMessage());
        }
    }

    public void onRefresh() {
        view.clearForm();
        selectedStudentId = null;
        view.getTxtSearch().setText("");
        if (view.getCboClassFilter().getItemCount() > 0) {
            isUpdatingFilter = true;
            view.getCboClassFilter().setSelectedIndex(0);
            isUpdatingFilter = false;
        }
        loadStudents();
    }

    public void onSearch() {
        String keyword = view.getTxtSearch().getText().trim();
        String selectedClass = (String) view.getCboClassFilter().getSelectedItem();
        if ("Tất cả các lớp".equals(selectedClass)) {
            selectedClass = null;
        }
        String kw = keyword.isEmpty() ? null : keyword;
        String cls = (selectedClass == null || selectedClass.isBlank()) ? null : selectedClass.trim();

        try {
            List<Student> results = service.search(kw, cls);
            tableModel.setStudents(results);
            view.setStatusText("Tìm thấy " + results.size() + " sinh viên");
        } catch (Exception ex) {
            showError("Không tìm kiếm được sinh viên: " + ex.getMessage());
        }
    }

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
                    List<String> classes = students.stream()
                            .map(Student::getClassName)
                            .filter(Objects::nonNull)
                            .map(String::trim)
                            .filter(s -> !s.isEmpty())
                            .distinct()
                            .sorted()
                            .toList();
                    isUpdatingFilter = true;
                    view.updateClassFilter(classes);
                    isUpdatingFilter = false;
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

    public Integer getSelectedStudentId() {
        return selectedStudentId;
    }

    public void setSelectedStudentId(Integer id) {
        this.selectedStudentId = id;
    }

    private void showError(String message) {
        dialogHandler.showMessage(view, message, "Lỗi", JOptionPane.ERROR_MESSAGE);
    }

    private void showWarning(String message) {
        dialogHandler.showMessage(view, message, "Cảnh báo", JOptionPane.WARNING_MESSAGE);
    }
}
