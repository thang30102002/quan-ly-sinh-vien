package com.qlsv.controller;

import com.qlsv.exception.BusinessException;
import com.qlsv.exception.ValidationException;
import com.qlsv.model.ClassSection;
import com.qlsv.model.Course;
import com.qlsv.model.Schedule;
import com.qlsv.model.Semester;
import com.qlsv.service.ClassSectionService;
import com.qlsv.service.CourseService;
import com.qlsv.service.ScheduleService;
import com.qlsv.service.SemesterService;
import com.qlsv.view.ClassSectionPanel;
import com.qlsv.view.ClassSectionTableModel;
import com.qlsv.view.ListTableModel;
import com.qlsv.view.ScheduleTableModel;

import java.util.List;
import java.util.Objects;

import javax.swing.JOptionPane;
import javax.swing.JTable;

/**
 * Xử lý sự kiện của {@link ClassSectionPanel}: chọn học kỳ, thêm/sửa/xóa lớp học phần
 * và các buổi học của lớp đang chọn.
 */
public class ClassSectionController {

    private final ClassSectionPanel view;
    private final SemesterService semesterService;
    private final CourseService courseService;
    private final ClassSectionService sectionService;
    private final ScheduleService scheduleService;

    private final ClassSectionTableModel sectionModel = new ClassSectionTableModel();
    private final ScheduleTableModel scheduleModel = new ScheduleTableModel();
    private DialogHandler dialogs = DialogHandler.swing();

    private ClassSection selectedSection;
    private Integer selectedScheduleId;
    /** Đang nạp lại combo học kỳ bằng code, bỏ qua sự kiện chọn học kỳ. */
    private boolean updatingSemesters;

    public ClassSectionController(ClassSectionPanel view, SemesterService semesterService,
                                  CourseService courseService, ClassSectionService sectionService,
                                  ScheduleService scheduleService) {
        this.view = Objects.requireNonNull(view, "view");
        this.semesterService = Objects.requireNonNull(semesterService, "semesterService");
        this.courseService = Objects.requireNonNull(courseService, "courseService");
        this.sectionService = Objects.requireNonNull(sectionService, "sectionService");
        this.scheduleService = Objects.requireNonNull(scheduleService, "scheduleService");
        view.setTableModels(sectionModel, scheduleModel);
        initEvents();
    }

    public void setDialogHandler(DialogHandler dialogs) {
        this.dialogs = Objects.requireNonNull(dialogs, "dialogs");
    }

    private void initEvents() {
        view.getCboSemester().addActionListener(e -> {
            if (!updatingSemesters) {
                loadSections();
            }
        });
        view.getBtnRefresh().addActionListener(e -> reloadLookups());

        view.getBtnAddSection().addActionListener(e -> onAddSection());
        view.getBtnUpdateSection().addActionListener(e -> onUpdateSection());
        view.getBtnDeleteSection().addActionListener(e -> onDeleteSection());
        view.getTblSections().getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                onSectionSelected();
            }
        });

        view.getBtnAddSchedule().addActionListener(e -> onAddSchedule());
        view.getBtnUpdateSchedule().addActionListener(e -> onUpdateSchedule());
        view.getBtnDeleteSchedule().addActionListener(e -> onDeleteSchedule());
        view.getTblSchedules().getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                onScheduleSelected();
            }
        });
    }

    // ---------------------------------------------------------------- tải dữ liệu

    /**
     * Nạp lại danh sách học kỳ và học phần (có thể đã đổi ở tab khác), rồi tải lớp của học kỳ
     * đang chọn. Lần đầu chọn sẵn học kỳ đang mở đăng ký, nếu không có thì học kỳ mới nhất.
     */
    public void reloadLookups() {
        try {
            Semester current = view.getSelectedSemester();
            Integer keepId = current != null ? current.getId() : null;
            List<Semester> semesters = semesterService.findAll();
            if (keepId == null) {
                keepId = semesters.stream().filter(Semester::isOpen).map(Semester::getId)
                        .findFirst().orElse(null);
            }
            updatingSemesters = true;
            try {
                view.setSemesters(semesters, keepId);
            } finally {
                updatingSemesters = false;
            }
            view.setCourses(courseService.findAll());
        } catch (BusinessException ex) {
            error("Không tải được danh sách học kỳ, học phần.\n" + ex.getMessage());
        }
        loadSections();
    }

    /** Tải các lớp của học kỳ đang chọn; xóa lựa chọn lớp và bảng buổi học. */
    public void loadSections() {
        clearSectionSelection();
        Semester semester = view.getSelectedSemester();
        if (semester == null) {
            sectionModel.setItems(List.of());
            view.setStatusText("Chưa có học kỳ nào, hãy thêm học kỳ ở tab Học kỳ");
            return;
        }
        try {
            List<ClassSection> sections = sectionService.findBySemester(semester.getId());
            sectionModel.setItems(sections);
            view.setStatusText("Học kỳ " + semester.getSemesterCode() + ": " + sections.size() + " lớp học phần");
        } catch (BusinessException ex) {
            sectionModel.setItems(List.of());
            error("Không tải được danh sách lớp học phần.\n" + ex.getMessage());
        }
    }

    private void loadSchedules() {
        if (selectedSection == null) {
            scheduleModel.setItems(List.of());
            return;
        }
        try {
            scheduleModel.setItems(scheduleService.findBySection(selectedSection.getId()));
        } catch (BusinessException ex) {
            scheduleModel.setItems(List.of());
            error("Không tải được buổi học của lớp.\n" + ex.getMessage());
        }
    }

    // ---------------------------------------------------------------- lớp học phần

    public void onSectionSelected() {
        ClassSection s = selectedRow(view.getTblSections(), sectionModel);
        if (s == null) {
            return;
        }
        selectedSection = s;
        view.setSectionToForm(s);
        view.setScheduleTitle(s);
        selectedScheduleId = null;
        view.clearScheduleForm();
        loadSchedules();
    }

    public void onAddSection() {
        Semester semester = view.getSelectedSemester();
        if (semester == null) {
            warning("Vui lòng chọn học kỳ trước khi mở lớp!");
            return;
        }
        try {
            ClassSection s = readSectionForm(semester);
            sectionService.add(s);
            info("Đã mở lớp " + s.getSectionCode() + ". Hãy thêm buổi học cho lớp ở khung bên dưới (BR-21).");
            loadSections();
            selectSection(s.getId());
        } catch (ValidationException | BusinessException ex) {
            error(ex.getMessage());
        }
    }

    public void onUpdateSection() {
        if (selectedSection == null) {
            warning("Vui lòng chọn một lớp học phần từ bảng để sửa!");
            return;
        }
        try {
            ClassSection s = readSectionForm(selectedSection.getSemester());
            s.setId(selectedSection.getId());
            sectionService.update(s);
            info("Cập nhật lớp " + s.getSectionCode() + " thành công!");
            loadSections();
            selectSection(s.getId());
        } catch (ValidationException | BusinessException ex) {
            error(ex.getMessage());
        }
    }

    public void onDeleteSection() {
        if (selectedSection == null) {
            warning("Vui lòng chọn một lớp học phần từ bảng để xóa!");
            return;
        }
        ClassSection s = selectedSection;
        int confirm = dialogs.showConfirm(view,
                "Bạn có chắc chắn muốn xóa lớp " + s + " cùng toàn bộ buổi học của lớp?",
                "Xác nhận xóa", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            sectionService.delete(s.getId());
            info("Đã xóa lớp " + s.getSectionCode() + ".");
            loadSections();
        } catch (BusinessException ex) {
            error(ex.getMessage());
        }
    }

    // ---------------------------------------------------------------- buổi học

    public void onScheduleSelected() {
        Schedule s = selectedRow(view.getTblSchedules(), scheduleModel);
        if (s == null) {
            return;
        }
        selectedScheduleId = s.getId();
        view.setScheduleToForm(s);
    }

    public void onAddSchedule() {
        if (selectedSection == null) {
            warning("Vui lòng chọn lớp học phần cần thêm buổi học!");
            return;
        }
        try {
            scheduleService.add(readScheduleForm(null));
            info("Thêm buổi học thành công!");
            selectedScheduleId = null;
            view.clearScheduleForm();
            loadSchedules();
        } catch (ValidationException | BusinessException ex) {
            error(ex.getMessage());
        }
    }

    public void onUpdateSchedule() {
        if (selectedSection == null || selectedScheduleId == null) {
            warning("Vui lòng chọn một buổi học từ bảng để sửa!");
            return;
        }
        try {
            scheduleService.update(readScheduleForm(selectedScheduleId));
            info("Cập nhật buổi học thành công!");
            loadSchedules();
            selectedScheduleId = null;
        } catch (ValidationException | BusinessException ex) {
            error(ex.getMessage());
        }
    }

    public void onDeleteSchedule() {
        Schedule s = selectedRow(view.getTblSchedules(), scheduleModel);
        if (selectedSection == null || s == null) {
            warning("Vui lòng chọn một buổi học từ bảng để xóa!");
            return;
        }
        int confirm = dialogs.showConfirm(view, "Bạn có chắc chắn muốn xóa buổi học " + s + "?",
                "Xác nhận xóa", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            scheduleService.delete(s.getId());
            info("Đã xóa buổi học " + s + ".");
            selectedScheduleId = null;
            view.clearScheduleForm();
            loadSchedules();
        } catch (BusinessException ex) {
            error(ex.getMessage());
        }
    }

    // ---------------------------------------------------------------- tiện ích

    public ClassSectionTableModel getSectionModel() {
        return sectionModel;
    }

    public ScheduleTableModel getScheduleModel() {
        return scheduleModel;
    }

    public ClassSection getSelectedSection() {
        return selectedSection;
    }

    private ClassSection readSectionForm(Semester semester) {
        Course course = view.getSelectedCourse();
        if (course == null) {
            throw new ValidationException("Chưa chọn học phần cho lớp, hãy thêm học phần ở tab Học phần");
        }
        int max = FormInputs.readInt(view.getSpnMaxStudents(), "Sĩ số tối đa");
        return new ClassSection(view.getTxtSectionCode().getText(), course, semester,
                view.getTxtLecturer().getText(), max);
    }

    private Schedule readScheduleForm(Integer id) {
        int start = FormInputs.readInt(view.getSpnStartPeriod(), "Tiết bắt đầu");
        int end = FormInputs.readInt(view.getSpnEndPeriod(), "Tiết kết thúc");
        Schedule s = new Schedule(selectedSection, view.getSelectedDayOfWeek(), start, end,
                view.getTxtRoom().getText());
        s.setId(id);
        return s;
    }

    /** Chọn lại dòng của lớp có id này trong bảng (sau khi thêm hoặc sửa). */
    private void selectSection(Integer id) {
        JTable table = view.getTblSections();
        for (int row = 0; row < sectionModel.getRowCount(); row++) {
            if (sectionModel.getItemAt(row).getId().equals(id)) {
                int viewRow = table.convertRowIndexToView(row);
                table.setRowSelectionInterval(viewRow, viewRow);
                onSectionSelected();
                return;
            }
        }
    }

    private void clearSectionSelection() {
        selectedSection = null;
        selectedScheduleId = null;
        view.clearSectionForm();
        view.clearScheduleForm();
        view.setScheduleTitle(null);
        scheduleModel.setItems(List.of());
    }

    private static <T> T selectedRow(JTable table, ListTableModel<T> model) {
        int viewRow = table.getSelectedRow();
        return viewRow >= 0 ? model.getItemAt(table.convertRowIndexToModel(viewRow)) : null;
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
