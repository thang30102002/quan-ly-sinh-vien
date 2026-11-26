package com.qlsv.controller;

import com.qlsv.exception.BusinessException;
import com.qlsv.exception.ValidationException;
import com.qlsv.model.Course;
import com.qlsv.service.CourseService;
import com.qlsv.view.CoursePanel;
import com.qlsv.view.CourseTableModel;

import java.util.List;
import java.util.Objects;

/**
 * Xử lý sự kiện của {@link CoursePanel}: thêm, sửa, xóa, tìm kiếm học phần.
 */
public class CourseController {

    private final CoursePanel view;
    private final CourseService service;
    private final CourseTableModel tableModel = new CourseTableModel();
    private DialogHandler dialogs = DialogHandler.swing();
    private Integer selectedId;

    public CourseController(CoursePanel view, CourseService service) {
        this.view = Objects.requireNonNull(view, "view");
        this.service = Objects.requireNonNull(service, "service");
        view.setTableModel(tableModel);
        initEvents();
    }

    public void setDialogHandler(DialogHandler dialogs) {
        this.dialogs = Objects.requireNonNull(dialogs, "dialogs");
    }

    private void initEvents() {
        view.getBtnAdd().addActionListener(e -> onAdd());
        view.getBtnUpdate().addActionListener(e -> onUpdate());
        view.getBtnDelete().addActionListener(e -> onDelete());
        view.getBtnRefresh().addActionListener(e -> onRefresh());
        view.getBtnSearch().addActionListener(e -> onSearch());
        view.getTxtSearch().addActionListener(e -> onSearch());
        view.getTblCourses().getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                onRowSelected();
            }
        });
    }

    public void onRowSelected() {
        Course c = selectedCourse();
        selectedId = c != null ? c.getId() : null;
        if (c != null) {
            view.setCourseToForm(c);
        }
    }

    public void onAdd() {
        try {
            service.add(readForm(null));
            info("Thêm học phần thành công!");
            resetForm();
            loadCourses();
        } catch (ValidationException | BusinessException ex) {
            error(ex.getMessage());
        }
    }

    public void onUpdate() {
        if (selectedId == null) {
            warning("Vui lòng chọn một học phần từ bảng để sửa!");
            return;
        }
        try {
            int id = selectedId;
            service.update(readForm(id));
            info("Cập nhật học phần thành công!");
            loadCourses();
            selectCourse(id);
        } catch (ValidationException | BusinessException ex) {
            error(ex.getMessage());
        }
    }

    public void onDelete() {
        Course c = selectedCourse();
        if (c == null) {
            warning("Vui lòng chọn một học phần từ bảng để xóa!");
            return;
        }
        if (!dialogs.confirmDelete(view,
                "Bạn có chắc chắn muốn xóa học phần " + c.getCourseCode() + " - " + c.getName() + "?")) {
            return;
        }
        try {
            service.delete(c.getId());
            info("Đã xóa học phần " + c.getCourseCode() + ".");
            resetForm();
            loadCourses();
        } catch (BusinessException ex) {
            error(ex.getMessage());
        }
    }

    public void onRefresh() {
        view.getTxtSearch().setText("");
        resetForm();
        loadCourses();
    }

    public void onSearch() {
        try {
            List<Course> results = service.search(view.getTxtSearch().getText());
            tableModel.setItems(results);
            view.setStatusText("Tìm thấy " + results.size() + " học phần");
        } catch (BusinessException ex) {
            error("Không tìm kiếm được học phần: " + ex.getMessage());
        }
    }

    /** Tải lại bảng theo từ khóa đang nhập (rỗng thì tải toàn bộ). */
    public void loadCourses() {
        try {
            List<Course> courses = service.search(view.getTxtSearch().getText());
            tableModel.setItems(courses);
            view.setStatusText("Đã tải " + courses.size() + " học phần");
        } catch (BusinessException ex) {
            view.setStatusText("Không tải được danh sách");
            error("Không tải được danh sách học phần.\n" + ex.getMessage());
        }
    }

    public CourseTableModel getTableModel() {
        return tableModel;
    }

    public Integer getSelectedId() {
        return selectedId;
    }

    private Course readForm(Integer id) {
        int credits = FormInputs.readInt(view.getSpnCredits(), "Số tín chỉ", "BR-18");
        Course c = new Course(view.getTxtCourseCode().getText(), view.getTxtName().getText(),
                credits, view.getTxtDescription().getText());
        c.setId(id);
        return c;
    }

    private Course selectedCourse() {
        return tableModel.getSelectedItem(view.getTblCourses());
    }

    /** Chọn lại dòng của học phần có id này (tải lại bảng làm JTable bỏ chọn). */
    private void selectCourse(int id) {
        tableModel.selectFirst(view.getTblCourses(), c -> c.getId() == id);
    }

    private void resetForm() {
        view.clearForm();
        selectedId = null;
    }

    private void info(String message) {
        dialogs.info(view, message);
    }

    private void warning(String message) {
        dialogs.warning(view, message);
    }

    private void error(String message) {
        dialogs.error(view, message);
    }
}
