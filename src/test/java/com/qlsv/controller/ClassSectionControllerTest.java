package com.qlsv.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.qlsv.model.ClassSection;
import com.qlsv.model.Course;
import com.qlsv.model.Schedule;
import com.qlsv.model.Semester;
import com.qlsv.service.ClassSectionService;
import com.qlsv.service.CourseService;
import com.qlsv.service.ScheduleService;
import com.qlsv.service.SemesterService;
import com.qlsv.testutil.FakeClassSectionDAO;
import com.qlsv.testutil.FakeCourseDAO;
import com.qlsv.testutil.FakeScheduleDAO;
import com.qlsv.testutil.FakeSemesterDAO;
import com.qlsv.testutil.RecordingDialogHandler;
import com.qlsv.view.ClassSectionPanel;

import java.time.LocalDate;

import javax.swing.JOptionPane;
import javax.swing.JSpinner;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test luồng giao diện lớp học phần và buổi học (#10) với DAO giả, không cần CSDL.
 * Lớp IT101-01 có hai buổi: Thứ 2 tiết 1–3 (dòng 0) và Thứ 4 tiết 1–3 (dòng 1).
 */
class ClassSectionControllerTest {

    private FakeScheduleDAO scheduleDAO;
    private ClassSectionPanel panel;
    private ClassSectionController controller;
    private RecordingDialogHandler dialog;
    private Schedule wednesday;

    @BeforeEach
    void setUp() {
        SemesterService semesterService = new SemesterService(new FakeSemesterDAO());
        Semester semester = new Semester("2026-1", "HK1 2026-2027", LocalDate.of(2026, 9, 7),
                LocalDate.of(2027, 1, 15), true);
        semesterService.add(semester);

        CourseService courseService = new CourseService(new FakeCourseDAO());
        Course course = new Course("IT101", "Nhập môn lập trình", 3, null);
        courseService.add(course);

        ClassSectionService sectionService = new ClassSectionService(new FakeClassSectionDAO());
        ClassSection section = new ClassSection("IT101-01", course, semester, null, 40);
        sectionService.add(section);

        scheduleDAO = new FakeScheduleDAO();
        ScheduleService scheduleService = new ScheduleService(scheduleDAO);
        scheduleService.add(new Schedule(section, 2, 1, 3, "A101"));
        wednesday = new Schedule(section, 4, 1, 3, "A102");
        scheduleService.add(wednesday);

        panel = new ClassSectionPanel();
        controller = new ClassSectionController(panel, semesterService, courseService,
                sectionService, scheduleService);
        dialog = new RecordingDialogHandler();
        controller.setDialogHandler(dialog);
        controller.reloadLookups();
        panel.getTblSections().setRowSelectionInterval(0, 0);
    }

    @AfterEach
    void khongHienMaBR() {
        dialog.assertNoRuleCodes();
    }

    @Test
    void reloadLookups_chonSanHocKyDangMo_vaHienBuoiCuaLop() {
        assertEquals("2026-1", panel.getSelectedSemester().getSemesterCode());
        assertEquals("IT101-01", controller.getSelectedSection().getSectionCode());
        assertEquals(2, controller.getScheduleModel().getRowCount());
    }

    @Test
    void onAddSchedule_trungLich_baoLoiBR21() {
        panel.getCboDay().setSelectedIndex(0); // Thứ 2
        panel.getSpnStartPeriod().setValue(3);
        panel.getSpnEndPeriod().setValue(5);

        controller.onAddSchedule();

        assertEquals(JOptionPane.ERROR_MESSAGE, dialog.lastMessageType);
        assertEquals("Buổi học Thứ 2, tiết 3–5 trùng với buổi đã có của lớp: Thứ 2, tiết 1–3, phòng A101",
                dialog.lastMessage);
        assertEquals(2, scheduleDAO.byId.size());
    }

    @Test
    void onAddSection_siSo0_baoLonHon0() {
        panel.getTxtSectionCode().setText("IT101-02");
        ((JSpinner.DefaultEditor) panel.getSpnMaxStudents().getEditor()).getTextField().setText("0");

        controller.onAddSection();

        assertEquals("Sĩ số tối đa phải lớn hơn 0", dialog.lastMessage);
        assertEquals(1, controller.getSectionModel().getRowCount());
    }

    @Test
    void onAddSchedule_keNhau_hopLe() {
        panel.getCboDay().setSelectedIndex(0); // Thứ 2
        panel.getSpnStartPeriod().setValue(4);
        panel.getSpnEndPeriod().setValue(6);

        controller.onAddSchedule();

        assertEquals("Thêm buổi học thành công!", dialog.lastMessage);
        assertEquals(3, controller.getScheduleModel().getRowCount());
    }

    /** Bug B2: sửa buổi học xong bảng tải lại làm mất dòng đang chọn, sửa lần hai bị báo "chưa chọn". */
    @Test
    void onUpdateSchedule_thanhCong_vanChonBuoiVuaSua_suaTiepDuoc() {
        panel.getTblSchedules().setRowSelectionInterval(1, 1);
        panel.getTxtRoom().setText("B201");

        controller.onUpdateSchedule();

        assertEquals("Cập nhật buổi học thành công!", dialog.lastMessage);
        Schedule selected = controller.getScheduleModel().getSelectedItem(panel.getTblSchedules());
        assertNotNull(selected);
        assertEquals(wednesday.getId(), selected.getId());
        assertEquals("B201", selected.getRoom());

        panel.getSpnEndPeriod().setValue(2);
        controller.onUpdateSchedule();

        assertEquals("Cập nhật buổi học thành công!", dialog.lastMessage);
        assertEquals(2, scheduleDAO.byId.get(wednesday.getId()).getEndPeriod());
    }

    @Test
    void onDeleteSchedule_buoiCuoiCung_baoLoi() {
        panel.getTblSchedules().setRowSelectionInterval(1, 1);
        controller.onDeleteSchedule();
        assertEquals(1, scheduleDAO.byId.size());

        panel.getTblSchedules().setRowSelectionInterval(0, 0);
        controller.onDeleteSchedule();

        assertEquals(JOptionPane.ERROR_MESSAGE, dialog.lastMessageType);
        assertTrue(dialog.lastMessage.contains("buổi học cuối cùng"));
        assertEquals(1, scheduleDAO.byId.size());
    }
}
