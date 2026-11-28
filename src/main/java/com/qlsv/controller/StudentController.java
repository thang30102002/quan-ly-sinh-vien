package com.qlsv.controller;

import com.qlsv.exception.BusinessException;
import com.qlsv.exception.ValidationException;
import com.qlsv.model.Gender;
import com.qlsv.model.Student;
import com.qlsv.model.StudentStatus;
import com.qlsv.service.StudentService;
import com.qlsv.view.StudentPanel;
import com.qlsv.view.StudentTableModel;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

import javax.swing.SwingWorker;

/**
 * Xử lý sự kiện của {@link StudentPanel}: thêm, sửa, xóa, tìm kiếm sinh viên.
 */
public class StudentController {

    private final StudentPanel view;
    private final StudentService service;
    private final StudentTableModel tableModel = new StudentTableModel();
    private Integer selectedStudentId = null;
    private boolean isUpdatingFilter = false;

    private DialogHandler dialogHandler = DialogHandler.swing();

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
        Student s = tableModel.getSelectedItem(view.getTblStudents());
        if (s != null) {
            selectedStudentId = s.getId();
            view.setStudentToForm(s);
        } else {
            selectedStudentId = null;
        }
    }

    private Student readStudentFromForm(boolean isNew) {
        // mã SV, họ tên, ngày sinh, lớp sinh hoạt, khóa có dấu * trên form
        // (giới tính, trạng thái là danh sách chọn, luôn có giá trị)
        FormInputs.requireFilled(view.getTxtStudentCode().getText(), view.getTxtFullName().getText(),
                view.getTxtDob().getText(), view.getTxtClassName().getText(),
                FormInputs.text(view.getSpnIntakeYear()));
        String code = view.getTxtStudentCode().getText();
        String fullName = view.getTxtFullName().getText();
        LocalDate dob = FormInputs.readDate(view.getTxtDob().getText(), "Ngày sinh", "BR-17");

        Gender gender = (Gender) view.getCboGender().getSelectedItem();
        String email = view.getTxtEmail().getText();
        String phone = view.getTxtPhone().getText();
        String className = view.getTxtClassName().getText();

        // commitEdit trong readInt: lấy cả năm vừa gõ mà chưa nhấn Enter (#10)
        int intakeYear = FormInputs.readInt(view.getSpnIntakeYear(), "Khóa (năm nhập học)", "BR-01");

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
            dialogHandler.info(view, "Thêm sinh viên thành công!");
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
            dialogHandler.info(view, "Cập nhật sinh viên thành công!");
            // giữ điều kiện tìm kiếm và chọn lại dòng vừa sửa để sửa tiếp được (#10)
            loadStudents(s.getId(), true);
        } catch (ValidationException | BusinessException ex) {
            showError(ex.getMessage());
        } catch (Exception ex) {
            showError("Đã xảy ra lỗi: " + ex.getMessage());
        }
    }

    public void onDeleteStudent() {
        Student student = tableModel.getSelectedItem(view.getTblStudents());
        if (student == null || selectedStudentId == null) {
            showWarning("Vui lòng chọn một sinh viên từ bảng để xóa!");
            return;
        }
        if (!dialogHandler.confirmDelete(view, "Bạn có chắc chắn muốn xóa sinh viên "
                + student.getFullName() + " (" + student.getStudentCode() + ")?")) {
            return;
        }

        try {
            StudentService.DeleteResult result = service.delete(student.getId());
            if (result == StudentService.DeleteResult.DELETED) {
                dialogHandler.info(view, "Đã xóa sinh viên thành công!");
            } else {
                dialogHandler.info(view, "Sinh viên đã có điểm nên không thể xóa hẳn."
                        + " Trạng thái đã được chuyển sang \"Thôi học\" (BR-14).");
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
        try {
            List<Student> results = service.search(searchKeyword(), selectedClassFilter());
            tableModel.setItems(results);
            view.setStatusText("Tìm thấy " + results.size() + " sinh viên");
        } catch (Exception ex) {
            showError("Không tìm kiếm được sinh viên: " + ex.getMessage());
        }
    }

    /** @return từ khóa đang nhập, hoặc null nếu để trống */
    private String searchKeyword() {
        String keyword = view.getTxtSearch().getText().trim();
        return keyword.isEmpty() ? null : keyword;
    }

    /** @return lớp đang chọn ở bộ lọc, hoặc null nếu chọn "Tất cả các lớp" */
    private String selectedClassFilter() {
        String selectedClass = (String) view.getCboClassFilter().getSelectedItem();
        if (selectedClass == null || selectedClass.isBlank() || StudentPanel.ALL_CLASSES.equals(selectedClass)) {
            return null;
        }
        return selectedClass.trim();
    }

    /** Tải lại toàn bộ danh sách (như trước #10): dùng khi mở app, Thêm, Xóa, Làm mới. */
    public void loadStudents() {
        loadStudents(null, false);
    }

    /**
     * Tải lại danh sách ở luồng nền.
     *
     * @param selectId   id sinh viên cần chọn lại sau khi tải xong, hoặc null. Nếu sinh viên
     *                   này không còn trong bảng thì xóa form để không còn dữ liệu cũ.
     * @param keepSearch true: bảng vẫn theo từ khóa và bộ lọc lớp đang hiển thị (dùng sau
     *                   khi Sửa; trước đây sửa xong bảng hiện lại toàn bộ dù ô tìm kiếm còn
     *                   từ khóa, #10). false: hiện toàn bộ như trước.
     */
    private void loadStudents(Integer selectId, boolean keepSearch) {
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
                    List<String> classes = students.stream()
                            .map(Student::getClassName)
                            .filter(Objects::nonNull)
                            .map(String::trim)
                            .filter(s -> !s.isEmpty())
                            .distinct()
                            .sorted()
                            .toList();
                    isUpdatingFilter = true;
                    try {
                        view.updateClassFilter(classes);
                    } finally {
                        isUpdatingFilter = false;
                    }
                    // đọc điều kiện sau khi nạp lại bộ lọc: lớp đang lọc có thể không còn
                    if (!keepSearch || (searchKeyword() == null && selectedClassFilter() == null)) {
                        tableModel.setItems(students);
                        view.setStatusText("Đã tải " + students.size() + " sinh viên");
                    } else {
                        onSearch();
                    }
                    if (selectId != null
                            && !tableModel.selectFirst(view.getTblStudents(), s -> s.getId().equals(selectId))) {
                        view.clearForm();
                        selectedStudentId = null;
                    }
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
        dialogHandler.error(view, message);
    }

    private void showWarning(String message) {
        dialogHandler.warning(view, message);
    }
}
