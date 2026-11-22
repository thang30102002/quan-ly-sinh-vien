package com.qlsv.view;

import com.qlsv.model.ClassSection;
import com.qlsv.model.Course;
import com.qlsv.model.Schedule;
import com.qlsv.model.Semester;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.TitledBorder;

/**
 * Giao diện lớp học phần và thời khóa biểu: chọn học kỳ ở trên, nửa trên là các lớp
 * của học kỳ, nửa dưới là các buổi học của lớp đang chọn. Chỉ dựng giao diện; xử lý
 * sự kiện nằm ở {@code ClassSectionController}.
 */
public class ClassSectionPanel extends JPanel {

    private static final int FIELD_COLUMNS = 14;
    /** Thứ 2 … Chủ nhật; chỉ số trong combo + 2 = giá trị day_of_week. */
    private static final int FIRST_DAY = 2;
    private static final String NO_SECTION_TITLE = "Buổi học (chọn một lớp ở bảng trên)";

    private final JComboBox<Semester> cboSemester = new JComboBox<>();
    private final JLabel lblStatus = new JLabel(" ");

    // Lớp học phần
    private final JTextField txtSectionCode = new JTextField(FIELD_COLUMNS);
    private final JComboBox<Course> cboCourse = new JComboBox<>();
    private final JTextField txtLecturer = new JTextField(FIELD_COLUMNS);
    private final JSpinner spnMaxStudents = new JSpinner(new SpinnerNumberModel(40, 1, Short.MAX_VALUE, 1));
    private final JTable tblSections = UiStyles.createTable();
    private final JButton btnAddSection = new JButton("Thêm lớp");
    private final JButton btnUpdateSection = new JButton("Sửa lớp");
    private final JButton btnDeleteSection = new JButton("Xóa lớp");
    private final JButton btnRefresh = new JButton("Làm mới");

    // Buổi học
    private final JComboBox<String> cboDay = new JComboBox<>();
    private final JSpinner spnStartPeriod = new JSpinner(new SpinnerNumberModel(1, 1, 12, 1));
    private final JSpinner spnEndPeriod = new JSpinner(new SpinnerNumberModel(3, 1, 12, 1));
    private final JTextField txtRoom = new JTextField(FIELD_COLUMNS);
    private final JTable tblSchedules = UiStyles.createTable();
    private final JButton btnAddSchedule = new JButton("Thêm buổi");
    private final JButton btnUpdateSchedule = new JButton("Sửa buổi");
    private final JButton btnDeleteSchedule = new JButton("Xóa buổi");
    private final JPanel schedulePanel = new JPanel(new BorderLayout(8, 4));

    public ClassSectionPanel() {
        super(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        for (int day = FIRST_DAY; day <= 8; day++) {
            cboDay.addItem(Schedule.dayLabel(day));
        }

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, buildSectionArea(), buildScheduleArea());
        split.setResizeWeight(0.55);
        split.setBorder(null);

        add(buildTopBar(), BorderLayout.NORTH);
        add(split, BorderLayout.CENTER);
    }

    private JPanel buildTopBar() {
        cboSemester.setToolTipText("Chọn học kỳ để xem các lớp học phần mở trong học kỳ đó");
        // Đủ rộng cho tên học kỳ dài như "2026-1 - Học kỳ 1 năm học 2026–2027"
        cboSemester.setPrototypeDisplayValue(
                new Semester("0000-0", "Học kỳ 1 năm học 2026–2027 (dự phòng)", null, null, false));
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        left.add(new JLabel("Học kỳ:"));
        left.add(cboSemester);
        left.add(btnRefresh);

        JPanel bar = new JPanel(new BorderLayout());
        bar.add(left, BorderLayout.WEST);
        bar.add(lblStatus, BorderLayout.EAST);
        return bar;
    }

    private JPanel buildSectionArea() {
        txtSectionCode.setToolTipText("<mã học phần>-<2 chữ số>, ví dụ IT101-01 (BR-24)");
        spnMaxStudents.setToolTipText("Số nguyên lớn hơn 0 (BR-25)");

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createTitledBorder("Thông tin lớp học phần"));
        int row = 0;
        UiStyles.addFormRow(form, row++, "Học phần *", cboCourse);
        UiStyles.addFormRow(form, row++, "Mã lớp *", txtSectionCode);
        UiStyles.addFormRow(form, row++, "Giảng viên", txtLecturer);
        UiStyles.addFormRow(form, row++, "Sĩ số tối đa *", spnMaxStudents);
        UiStyles.addFormFiller(form, row);

        UiStyles.stylePrimary(btnAddSection);
        UiStyles.styleDanger(btnDeleteSection);
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        buttons.add(btnAddSection);
        buttons.add(btnUpdateSection);
        buttons.add(btnDeleteSection);

        JPanel tablePanel = new JPanel(new BorderLayout(0, 4));
        tablePanel.setBorder(BorderFactory.createTitledBorder("Lớp học phần của học kỳ"));
        tablePanel.add(new JScrollPane(tblSections), BorderLayout.CENTER);
        tablePanel.add(buttons, BorderLayout.SOUTH);

        JPanel area = new JPanel(new BorderLayout(8, 0));
        area.add(form, BorderLayout.WEST);
        area.add(tablePanel, BorderLayout.CENTER);
        return area;
    }

    private JPanel buildScheduleArea() {
        spnStartPeriod.setToolTipText("Tiết 1–12; tiết 1–6 buổi sáng, 7–12 buổi chiều (BR-21)");
        spnEndPeriod.setToolTipText("Không nhỏ hơn tiết bắt đầu (BR-21)");

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createTitledBorder("Thông tin buổi học"));
        int row = 0;
        UiStyles.addFormRow(form, row++, "Thứ *", cboDay);
        UiStyles.addFormRow(form, row++, "Tiết bắt đầu *", spnStartPeriod);
        UiStyles.addFormRow(form, row++, "Tiết kết thúc *", spnEndPeriod);
        UiStyles.addFormRow(form, row++, "Phòng", txtRoom);
        UiStyles.addFormFiller(form, row);

        UiStyles.stylePrimary(btnAddSchedule);
        UiStyles.styleDanger(btnDeleteSchedule);
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        buttons.add(btnAddSchedule);
        buttons.add(btnUpdateSchedule);
        buttons.add(btnDeleteSchedule);

        JPanel tablePanel = new JPanel(new BorderLayout(0, 4));
        tablePanel.add(new JScrollPane(tblSchedules), BorderLayout.CENTER);
        tablePanel.add(buttons, BorderLayout.SOUTH);

        schedulePanel.setBorder(BorderFactory.createTitledBorder(NO_SECTION_TITLE));
        schedulePanel.add(form, BorderLayout.WEST);
        schedulePanel.add(tablePanel, BorderLayout.CENTER);
        return schedulePanel;
    }

    public void setTableModels(ClassSectionTableModel sectionModel, ScheduleTableModel scheduleModel) {
        tblSections.setModel(sectionModel);
        UiStyles.setColumnWidths(tblSections, 90, 220, 60, 180, 90);
        UiStyles.centerColumns(tblSections, ClassSectionTableModel.COL_CREDITS,
                ClassSectionTableModel.COL_MAX_STUDENTS);

        tblSchedules.setModel(scheduleModel);
        UiStyles.setColumnWidths(tblSchedules, 100, 100, 100, 120);
        UiStyles.centerColumns(tblSchedules, ScheduleTableModel.COL_DAY,
                ScheduleTableModel.COL_START, ScheduleTableModel.COL_END);
    }

    /** Nạp lại danh sách học kỳ, giữ học kỳ đang chọn nếu còn (so theo id). */
    public void setSemesters(List<Semester> semesters, Integer selectedId) {
        cboSemester.removeAllItems();
        for (Semester s : semesters) {
            cboSemester.addItem(s);
            if (selectedId != null && selectedId.equals(s.getId())) {
                cboSemester.setSelectedItem(s);
            }
        }
    }

    /** Nạp lại danh sách học phần cho form lớp. */
    public void setCourses(List<Course> courses) {
        cboCourse.removeAllItems();
        for (Course c : courses) {
            cboCourse.addItem(c);
        }
    }

    public Semester getSelectedSemester() {
        return (Semester) cboSemester.getSelectedItem();
    }

    public Course getSelectedCourse() {
        return (Course) cboCourse.getSelectedItem();
    }

    /** Chọn học phần trong combo theo id (đối tượng trong combo khác đối tượng của lớp). */
    private void selectCourse(Course course) {
        if (course == null) {
            return;
        }
        for (int i = 0; i < cboCourse.getItemCount(); i++) {
            if (course.getId().equals(cboCourse.getItemAt(i).getId())) {
                cboCourse.setSelectedIndex(i);
                return;
            }
        }
    }

    public void setStatusText(String text) {
        lblStatus.setText(text == null || text.isEmpty() ? " " : text);
    }

    /** Tiêu đề khung buổi học, cho biết đang xem lớp nào. */
    public void setScheduleTitle(ClassSection section) {
        String title = section == null ? NO_SECTION_TITLE : "Buổi học của lớp " + section;
        ((TitledBorder) schedulePanel.getBorder()).setTitle(title);
        schedulePanel.repaint();
    }

    public void clearSectionForm() {
        txtSectionCode.setText("");
        txtLecturer.setText("");
        spnMaxStudents.setValue(40);
        if (cboCourse.getItemCount() > 0) {
            cboCourse.setSelectedIndex(0);
        }
        tblSections.clearSelection();
    }

    public void setSectionToForm(ClassSection s) {
        if (s == null) {
            clearSectionForm();
            return;
        }
        selectCourse(s.getCourse());
        txtSectionCode.setText(s.getSectionCode());
        txtLecturer.setText(s.getLecturerName() != null ? s.getLecturerName() : "");
        spnMaxStudents.setValue(s.getMaxStudents());
    }

    public void clearScheduleForm() {
        cboDay.setSelectedIndex(0);
        spnStartPeriod.setValue(1);
        spnEndPeriod.setValue(3);
        txtRoom.setText("");
        tblSchedules.clearSelection();
    }

    public void setScheduleToForm(Schedule s) {
        if (s == null) {
            clearScheduleForm();
            return;
        }
        cboDay.setSelectedIndex(s.getDayOfWeek() - FIRST_DAY);
        spnStartPeriod.setValue(s.getStartPeriod());
        spnEndPeriod.setValue(s.getEndPeriod());
        txtRoom.setText(s.getRoom() != null ? s.getRoom() : "");
    }

    /** @return thứ đang chọn theo quy ước CSDL (2 = Thứ 2 … 8 = Chủ nhật) */
    public int getSelectedDayOfWeek() {
        return cboDay.getSelectedIndex() + FIRST_DAY;
    }

    public JComboBox<Semester> getCboSemester() { return cboSemester; }
    public JButton getBtnRefresh() { return btnRefresh; }

    public JTextField getTxtSectionCode() { return txtSectionCode; }
    public JComboBox<Course> getCboCourse() { return cboCourse; }
    public JTextField getTxtLecturer() { return txtLecturer; }
    public JSpinner getSpnMaxStudents() { return spnMaxStudents; }
    public JTable getTblSections() { return tblSections; }
    public JButton getBtnAddSection() { return btnAddSection; }
    public JButton getBtnUpdateSection() { return btnUpdateSection; }
    public JButton getBtnDeleteSection() { return btnDeleteSection; }

    public JComboBox<String> getCboDay() { return cboDay; }
    public JSpinner getSpnStartPeriod() { return spnStartPeriod; }
    public JSpinner getSpnEndPeriod() { return spnEndPeriod; }
    public JTextField getTxtRoom() { return txtRoom; }
    public JTable getTblSchedules() { return tblSchedules; }
    public JButton getBtnAddSchedule() { return btnAddSchedule; }
    public JButton getBtnUpdateSchedule() { return btnUpdateSchedule; }
    public JButton getBtnDeleteSchedule() { return btnDeleteSchedule; }
}
