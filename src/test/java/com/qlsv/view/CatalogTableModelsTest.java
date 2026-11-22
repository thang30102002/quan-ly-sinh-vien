package com.qlsv.view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.qlsv.model.ClassSection;
import com.qlsv.model.Course;
import com.qlsv.model.Schedule;
import com.qlsv.model.Semester;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * Test các TableModel của học phần, học kỳ, lớp học phần và buổi học.
 */
class CatalogTableModelsTest {

    private static final Course IT101 = new Course(1, "IT101", "Nhập môn lập trình", 3, "Mô tả");
    private static final Semester HK1 = new Semester(1, "2026-1", "HK1 2026-2027",
            LocalDate.of(2026, 9, 7), LocalDate.of(2027, 1, 15), true);

    @Test
    void courseTableModel_cotVaGiaTri() {
        CourseTableModel model = new CourseTableModel();
        model.setItems(List.of(IT101));

        assertEquals(1, model.getRowCount());
        assertEquals(4, model.getColumnCount());
        assertEquals("Mã HP", model.getColumnName(CourseTableModel.COL_CODE));
        assertEquals("IT101", model.getValueAt(0, CourseTableModel.COL_CODE));
        assertEquals(3, model.getValueAt(0, CourseTableModel.COL_CREDITS));
        assertEquals(Integer.class, model.getColumnClass(CourseTableModel.COL_CREDITS));
        assertSame(IT101, model.getItemAt(0));
        assertFalse(model.isCellEditable(0, 0));
    }

    @Test
    void semesterTableModel_trangThaiVaNgay() {
        SemesterTableModel model = new SemesterTableModel();
        Semester closed = new Semester(2, "2025-2", "HK2", LocalDate.of(2026, 2, 1),
                LocalDate.of(2026, 6, 1), false);
        model.setItems(List.of(HK1, closed));

        assertEquals(SemesterTableModel.OPEN_LABEL, model.getValueAt(0, SemesterTableModel.COL_STATUS));
        assertEquals(SemesterTableModel.CLOSED_LABEL, model.getValueAt(1, SemesterTableModel.COL_STATUS));
        assertEquals(LocalDate.of(2026, 9, 7), model.getValueAt(0, SemesterTableModel.COL_START));
        assertEquals(LocalDate.class, model.getColumnClass(SemesterTableModel.COL_END));
    }

    @Test
    void classSectionTableModel_layThongTinHocPhan() {
        ClassSectionTableModel model = new ClassSectionTableModel();
        model.setItems(List.of(new ClassSection(5, "IT101-01", IT101, HK1, "ThS. A", 40)));

        assertEquals("IT101-01", model.getValueAt(0, ClassSectionTableModel.COL_CODE));
        assertEquals("Nhập môn lập trình", model.getValueAt(0, ClassSectionTableModel.COL_COURSE_NAME));
        assertEquals(3, model.getValueAt(0, ClassSectionTableModel.COL_CREDITS));
        assertEquals("ThS. A", model.getValueAt(0, ClassSectionTableModel.COL_LECTURER));
        assertEquals(40, model.getValueAt(0, ClassSectionTableModel.COL_MAX_STUDENTS));
    }

    @Test
    void scheduleTableModel_nhanThuTiengViet() {
        ScheduleTableModel model = new ScheduleTableModel();
        model.setItems(List.of(new Schedule(1, null, 8, 7, 9, "B202")));

        assertEquals("Chủ nhật", model.getValueAt(0, ScheduleTableModel.COL_DAY));
        assertEquals(7, model.getValueAt(0, ScheduleTableModel.COL_START));
        assertEquals(9, model.getValueAt(0, ScheduleTableModel.COL_END));
        assertEquals("B202", model.getValueAt(0, ScheduleTableModel.COL_ROOM));
    }

    @Test
    void setItems_null_xoaHetDong() {
        CourseTableModel model = new CourseTableModel();
        model.setItems(List.of(IT101));
        model.setItems(null);
        assertEquals(0, model.getRowCount());
    }

    @Test
    void getValueAt_cotKhongTonTai_nemLoi() {
        CourseTableModel model = new CourseTableModel();
        model.setItems(List.of(IT101));
        assertThrows(IndexOutOfBoundsException.class, () -> model.getValueAt(0, 9));
    }
}
