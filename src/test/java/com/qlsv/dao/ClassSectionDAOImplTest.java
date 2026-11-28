package com.qlsv.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.qlsv.model.ClassSection;
import com.qlsv.model.Course;
import com.qlsv.model.Schedule;
import com.qlsv.model.Semester;
import com.qlsv.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test CourseDAOImpl, SemesterDAOImpl, ClassSectionDAOImpl và ScheduleDAOImpl với CSDL thật
 * (qlsv_test). Chỉ dùng dữ liệu riêng: học phần ZY9xx, học kỳ 2098-x, sinh viên SV2098xxx.
 */
class ClassSectionDAOImplTest {

    private final CourseDAOImpl courseDAO = new CourseDAOImpl();
    private final SemesterDAOImpl semesterDAO = new SemesterDAOImpl();
    private final ClassSectionDAOImpl sectionDAO = new ClassSectionDAOImpl();
    private final ScheduleDAOImpl scheduleDAO = new ScheduleDAOImpl();

    @BeforeEach
    @AfterEach
    void cleanUp() throws SQLException {
        try (Connection conn = DBConnection.getConnection()) {
            execute(conn, "DELETE FROM students WHERE student_code LIKE 'SV2098%'");
            execute(conn, "DELETE FROM class_sections WHERE course_id IN"
                    + " (SELECT id FROM courses WHERE course_code LIKE 'ZY9%')");
            execute(conn, "DELETE FROM courses WHERE course_code LIKE 'ZY9%'");
            execute(conn, "DELETE FROM semesters WHERE semester_code LIKE '2098-%'");
        }
    }

    // ---------- CourseDAO ----------

    @Test
    void course_insertFindUpdateDelete() throws SQLException {
        Course c = new Course("ZY901", "Môn thử nghiệm", 3, "Mô tả có dấu");
        courseDAO.insert(c);
        assertNotNull(c.getId());

        Course found = courseDAO.findByCode(" zy901 ");
        assertEquals(c.getId(), found.getId());
        assertEquals("Môn thử nghiệm", found.getName());
        assertEquals("Mô tả có dấu", found.getDescription());

        found.setCredits(5);
        found.setDescription(null);
        assertTrue(courseDAO.update(found));
        Course updated = courseDAO.findById(c.getId());
        assertEquals(5, updated.getCredits());
        assertNull(updated.getDescription());

        assertTrue(courseDAO.delete(c.getId()));
        assertNull(courseDAO.findById(c.getId()));
        assertFalse(courseDAO.delete(c.getId()));
    }

    @Test
    void course_search_theoMaHoacTenKhongPhanBietHoaThuong() throws SQLException {
        courseDAO.insert(new Course("ZY901", "Lập trình hướng đối tượng", 3, null));
        courseDAO.insert(new Course("ZY902", "Cơ sở dữ liệu", 3, null));

        assertEquals(1, courseDAO.search("HƯỚNG ĐỐI").size());
        assertEquals(2, courseDAO.search("zy90").size());
        assertEquals(0, courseDAO.search("ZY90%_").size());
    }

    @Test
    void course_tinChiNgoaiKhoang_csdlChan() {
        assertThrows(SQLException.class, () -> courseDAO.insert(new Course("ZY901", "Môn", 7, null)));
    }

    // ---------- SemesterDAO ----------

    @Test
    void semester_insertFindOpenUpdate() throws SQLException {
        Semester s = newSemester("2098-1", true);
        semesterDAO.insert(s);
        assertNotNull(s.getId());

        Semester open = semesterDAO.findOpen();
        assertNotNull(open);
        assertEquals(s.getId(), open.getId());
        assertEquals(LocalDate.of(2098, 9, 1), open.getStartDate());

        s.setOpen(false);
        s.setName("Đã đổi tên");
        assertTrue(semesterDAO.update(s));
        Semester updated = semesterDAO.findByCode("2098-1");
        assertFalse(updated.isOpen());
        assertEquals("Đã đổi tên", updated.getName());
    }

    @Test
    void semester_haiHocKyCungMo_csdlChanBR26() throws SQLException {
        semesterDAO.insert(newSemester("2098-1", true));
        SQLException ex = assertThrows(SQLException.class,
                () -> semesterDAO.insert(newSemester("2098-2", true)));
        assertEquals("23505", ex.getSQLState());
        assertTrue(ex.getMessage().contains("uq_semesters_open"));
    }

    // ---------- ClassSectionDAO ----------

    @Test
    void section_insert_findBySemester_napDuHocPhanVaHocKy() throws SQLException {
        Course course = insertCourse("ZY901");
        Semester semester = insertSemester("2098-1");

        ClassSection s1 = new ClassSection("ZY901-02", course, semester, "ThS. Lê Văn B", 30);
        ClassSection s2 = new ClassSection("ZY901-01", course, semester, null, 40);
        sectionDAO.insert(s1);
        sectionDAO.insert(s2);

        List<ClassSection> list = sectionDAO.findBySemester(semester.getId());
        assertEquals(2, list.size());
        assertEquals("ZY901-01", list.get(0).getSectionCode());
        ClassSection loaded = list.get(1);
        assertEquals("ThS. Lê Văn B", loaded.getLecturerName());
        assertEquals(30, loaded.getMaxStudents());
        assertEquals("ZY901", loaded.getCourse().getCourseCode());
        assertEquals(3, loaded.getCourse().getCredits());
        assertEquals("2098-1", loaded.getSemester().getSemesterCode());

        assertEquals(s2.getId(), sectionDAO.findByCode(semester.getId(), "zy901-01").getId());
        assertNull(sectionDAO.findByCode(semester.getId(), "ZY901-09"));
    }

    @Test
    void section_trungMaTrongHocKy_csdlChan() throws SQLException {
        Course course = insertCourse("ZY901");
        Semester semester = insertSemester("2098-1");
        sectionDAO.insert(new ClassSection("ZY901-01", course, semester, null, 40));

        SQLException ex = assertThrows(SQLException.class,
                () -> sectionDAO.insert(new ClassSection("ZY901-01", course, semester, null, 40)));
        assertEquals("23505", ex.getSQLState());
    }

    @Test
    void section_update_va_delete_xoaLuonBuoiHoc() throws SQLException {
        Course course = insertCourse("ZY901");
        Semester semester = insertSemester("2098-1");
        ClassSection s = new ClassSection("ZY901-01", course, semester, null, 40);
        sectionDAO.insert(s);
        scheduleDAO.insert(new Schedule(s, 2, 1, 3, "A101"));

        s.setMaxStudents(55);
        s.setLecturerName("GV mới");
        assertTrue(sectionDAO.update(s));
        assertEquals(55, sectionDAO.findById(s.getId()).getMaxStudents());

        assertTrue(sectionDAO.delete(s.getId()));
        assertTrue(scheduleDAO.findBySection(s.getId()).isEmpty());
    }

    @Test
    void hasEnrollments_countEnrollments_hasSections() throws SQLException {
        Course course = insertCourse("ZY901");
        Semester semester = insertSemester("2098-1");
        assertFalse(courseDAO.hasSections(course.getId()));
        assertFalse(semesterDAO.hasSections(semester.getId()));

        ClassSection s = new ClassSection("ZY901-01", course, semester, null, 40);
        sectionDAO.insert(s);
        assertTrue(courseDAO.hasSections(course.getId()));
        assertTrue(semesterDAO.hasSections(semester.getId()));
        assertFalse(courseDAO.hasEnrollments(course.getId()));
        assertFalse(semesterDAO.hasEnrollments(semester.getId()));
        assertEquals(0, sectionDAO.countEnrollments(s.getId()));

        enroll("SV2098001", s.getId());
        enroll("SV2098002", s.getId());
        assertTrue(courseDAO.hasEnrollments(course.getId()));
        assertTrue(semesterDAO.hasEnrollments(semester.getId()));
        assertEquals(2, sectionDAO.countEnrollments(s.getId()));

        // BR-16: CSDL cũng chặn xóa lớp đã có đăng ký và xóa học phần đã mở lớp
        SQLException ex = assertThrows(SQLException.class, () -> sectionDAO.delete(s.getId()));
        assertEquals("23503", ex.getSQLState());
        assertThrows(SQLException.class, () -> courseDAO.delete(course.getId()));
    }

    // ---------- ScheduleDAO ----------

    @Test
    void schedule_crud_vaSapTheoThuTiet() throws SQLException {
        Course course = insertCourse("ZY901");
        Semester semester = insertSemester("2098-1");
        ClassSection section = new ClassSection("ZY901-01", course, semester, null, 40);
        sectionDAO.insert(section);

        Schedule late = new Schedule(section, 5, 7, 9, null);
        Schedule early = new Schedule(section, 2, 1, 3, "A101");
        scheduleDAO.insert(late);
        scheduleDAO.insert(early);
        assertNotNull(late.getId());

        List<Schedule> list = scheduleDAO.findBySection(section.getId());
        assertEquals(2, list.size());
        assertEquals(early.getId(), list.get(0).getId());
        assertEquals(section.getId(), list.get(0).getSection().getId());
        assertEquals("A101", list.get(0).getRoom());

        late.setStartPeriod(8);
        late.setRoom("B202");
        assertTrue(scheduleDAO.update(late));
        Schedule updated = scheduleDAO.findById(late.getId());
        assertEquals(8, updated.getStartPeriod());
        assertEquals("B202", updated.getRoom());

        assertTrue(scheduleDAO.delete(early.getId()));
        assertEquals(1, scheduleDAO.findBySection(section.getId()).size());
    }

    @Test
    void schedule_tietSai_csdlChan() throws SQLException {
        Course course = insertCourse("ZY901");
        Semester semester = insertSemester("2098-1");
        ClassSection section = new ClassSection("ZY901-01", course, semester, null, 40);
        sectionDAO.insert(section);

        assertThrows(SQLException.class, () -> scheduleDAO.insert(new Schedule(section, 2, 5, 4, null)));
        assertThrows(SQLException.class, () -> scheduleDAO.insert(new Schedule(section, 9, 1, 3, null)));
    }

    // ---------- bổ sung ở #10 ----------

    /** ScheduleService đổi lỗi này thành thông báo BR-21, nên phải đúng mã 23505. */
    @Test
    void schedule_trungThuVaTietBatDau_csdlChanUqSchedulesSlot() throws SQLException {
        Course course = insertCourse("ZY901");
        Semester semester = insertSemester("2098-1");
        ClassSection section = new ClassSection("ZY901-01", course, semester, null, 40);
        sectionDAO.insert(section);
        scheduleDAO.insert(new Schedule(section, 2, 1, 3, null));

        SQLException ex = assertThrows(SQLException.class,
                () -> scheduleDAO.insert(new Schedule(section, 2, 1, 2, null)));
        assertEquals("23505", ex.getSQLState());
        assertTrue(ex.getMessage().contains("uq_schedules_slot"));
    }

    @Test
    void schedule_findById_khongCo_traVeNull() throws SQLException {
        assertNull(scheduleDAO.findById(-1));
    }

    @Test
    void course_search_kyTuPhanTramTrongTen_timDungChuoi() throws SQLException {
        courseDAO.insert(new Course("ZY901", "Thực tập 100% doanh nghiệp", 3, null));
        courseDAO.insert(new Course("ZY902", "Thực tập 1000 giờ", 3, null));

        List<Course> found = courseDAO.search("100%");
        assertEquals(1, found.size());
        assertEquals("ZY901", found.get(0).getCourseCode());
    }

    @Test
    void course_findAll_sapTheoMa() throws SQLException {
        insertCourse("ZY903");
        insertCourse("ZY901");
        insertCourse("ZY902");

        List<String> codes = courseDAO.findAll().stream().map(Course::getCourseCode)
                .filter(c -> c.startsWith("ZY9")).toList();
        assertEquals(List.of("ZY901", "ZY902", "ZY903"), codes);
    }

    @Test
    void semester_findAll_moiNhatTruoc() throws SQLException {
        semesterDAO.insert(new Semester("2098-1", "HK1", LocalDate.of(2098, 9, 1), LocalDate.of(2099, 1, 15), false));
        semesterDAO.insert(new Semester("2098-2", "HK2", LocalDate.of(2099, 2, 1), LocalDate.of(2099, 6, 30), false));

        List<String> codes = semesterDAO.findAll().stream().map(Semester::getSemesterCode)
                .filter(c -> c.startsWith("2098-")).toList();
        assertEquals(List.of("2098-2", "2098-1"), codes);
    }

    // ---------- tiện ích ----------

    private Course insertCourse(String code) throws SQLException {
        Course c = new Course(code, "Môn " + code, 3, null);
        courseDAO.insert(c);
        return c;
    }

    private Semester insertSemester(String code) throws SQLException {
        Semester s = newSemester(code, false);
        semesterDAO.insert(s);
        return s;
    }

    private static Semester newSemester(String code, boolean open) {
        return new Semester(code, "Học kỳ " + code, LocalDate.of(2098, 9, 1), LocalDate.of(2099, 1, 15), open);
    }

    private static void enroll(String studentCode, int sectionId) throws SQLException {
        try (Connection conn = DBConnection.getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO students (student_code, full_name, dob, gender, class_name, intake_year)"
                    + " VALUES (?, 'Sinh viên thử', DATE '2080-01-01', 'MALE', 'TEST', 2098)")) {
                ps.setString(1, studentCode);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO enrollments (student_id, section_id)"
                    + " SELECT id, ? FROM students WHERE student_code = ?")) {
                ps.setInt(1, sectionId);
                ps.setString(2, studentCode);
                ps.executeUpdate();
            }
        }
    }

    private static void execute(Connection conn, String sql) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.executeUpdate();
        }
    }
}
