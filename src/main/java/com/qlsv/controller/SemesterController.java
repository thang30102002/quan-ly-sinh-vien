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

import javax.swing.JOptionPane;
import javax.swing.JTable;

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
            service.update(readForm(selectedId));
            info("Cập nhật học kỳ thành công!");
            loadSemesters();
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
        int confirm = dialogs.showConfirm(view,
                "Bạn có chắc chắn muốn xóa học kỳ " + s.getSemesterCode() + " - " + s.getName() + "?",
                "Xác nhận xóa", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) {
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
        LocalDate start = FormInputs.readDate(view.getTxtStartDate().getText(), "Ngày bắt đầu");
        LocalDate end = FormInputs.readDate(view.getTxtEndDate().getText(), "Ngày kết thúc");
        Semester s = new Semester(view.getTxtSemesterCode().getText(), view.getTxtName().getText(),
                start, end, view.getChkOpen().isSelected());
        s.setId(id);
        return s;
    }

    private Semester selectedSemester() {
        JTable table = view.getTblSemesters();
        int viewRow = table.getSelectedRow();
        return viewRow >= 0 ? tableModel.getItemAt(table.convertRowIndexToModel(viewRow)) : null;
    }

    private void resetForm() {
        view.clearForm();
        selectedId = null;
    }

    private void info(String message) {
        dialogs.showMessage(view, message, "Thông báo", JOptionPane.INFORMATION_MESSAGE);
    }

    private void warning(String message) {
        dialogs.showMessage(view, message, "Cảnh báo", JOptionPane.WARNING_MESSAGE);
    }

    private void error(String message) {
        dialogs.showMessage(view, message, "Lỗi", JOptionPane.ERROR_MESSAGE);
    }
}
