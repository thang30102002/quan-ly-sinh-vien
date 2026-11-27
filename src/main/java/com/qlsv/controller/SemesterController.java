package com.qlsv.controller;

import com.qlsv.exception.BusinessException;
import com.qlsv.exception.ValidationException;
import com.qlsv.model.Semester;
import com.qlsv.service.SemesterService;
import com.qlsv.view.SemesterPanel;
import com.qlsv.view.SemesterTableModel;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

/**
 * Xử lý sự kiện của {@link SemesterPanel}: thêm, sửa, xóa, mở/đóng đăng ký học kỳ.
 */
public class SemesterController {

    private final SemesterPanel view;
    private final SemesterService service;
    private final SemesterTableModel tableModel = new SemesterTableModel();
    private DialogHandler dialogs = DialogHandler.swing();
    private Integer selectedId;

    public SemesterController(SemesterPanel view, SemesterService service) {
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
        view.getTblSemesters().getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                onRowSelected();
            }
        });
    }

    public void onRowSelected() {
        Semester s = selectedSemester();
        selectedId = s != null ? s.getId() : null;
        if (s != null) {
            view.setSemesterToForm(s);
        }
    }

    public void onAdd() {
        try {
            service.add(readForm(null));
            info("Thêm học kỳ thành công!");
            resetForm();
            loadSemesters();
        } catch (ValidationException | BusinessException ex) {
            error(ex.getMessage());
        }
    }

    public void onUpdate() {
        if (selectedId == null) {
            warning("Vui lòng chọn một học kỳ từ bảng để sửa!");
            return;
        }
        try {
            int id = selectedId;
            service.update(readForm(id));
            info("Cập nhật học kỳ thành công!");
            loadSemesters();
            selectSemester(id);
        } catch (ValidationException | BusinessException ex) {
            error(ex.getMessage());
        }
    }

    public void onDelete() {
        Semester s = selectedSemester();
        if (s == null) {
            warning("Vui lòng chọn một học kỳ từ bảng để xóa!");
            return;
        }
        if (!dialogs.confirmDelete(view,
                "Bạn có chắc chắn muốn xóa học kỳ " + s.getSemesterCode() + " - " + s.getName() + "?")) {
            return;
        }
        try {
            service.delete(s.getId());
            info("Đã xóa học kỳ " + s.getSemesterCode() + ".");
            resetForm();
            loadSemesters();
        } catch (BusinessException ex) {
            error(ex.getMessage());
        }
    }

    public void onRefresh() {
        resetForm();
        loadSemesters();
    }

    public void loadSemesters() {
        try {
            List<Semester> semesters = service.findAll();
            tableModel.setItems(semesters);
            view.setStatusText("Đã tải " + semesters.size() + " học kỳ");
        } catch (BusinessException ex) {
            view.setStatusText("Không tải được danh sách");
            error("Không tải được danh sách học kỳ.\n" + ex.getMessage());
        }
    }

    public SemesterTableModel getTableModel() {
        return tableModel;
    }

    public Integer getSelectedId() {
        return selectedId;
    }

    private Semester readForm(Integer id) {
        // mã, tên, ngày bắt đầu, ngày kết thúc có dấu * trên form
        FormInputs.requireFilled(view.getTxtSemesterCode().getText(), view.getTxtName().getText(),
                view.getTxtStartDate().getText(), view.getTxtEndDate().getText());
        LocalDate start = FormInputs.readDate(view.getTxtStartDate().getText(), "Ngày bắt đầu", "BR-19");
        LocalDate end = FormInputs.readDate(view.getTxtEndDate().getText(), "Ngày kết thúc", "BR-19");
        Semester s = new Semester(view.getTxtSemesterCode().getText(), view.getTxtName().getText(),
                start, end, view.getChkOpen().isSelected());
        s.setId(id);
        return s;
    }

    private Semester selectedSemester() {
        return tableModel.getSelectedItem(view.getTblSemesters());
    }

    /** Chọn lại dòng của học kỳ có id này (tải lại bảng làm JTable bỏ chọn). */
    private void selectSemester(int id) {
        tableModel.selectFirst(view.getTblSemesters(), s -> s.getId() == id);
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
