package com.qlsv.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.qlsv.model.ClassSection;
import com.qlsv.model.Course;
import com.qlsv.model.Enrollment;
import com.qlsv.model.Gender;
import com.qlsv.model.Schedule;
import com.qlsv.model.Semester;
import com.qlsv.model.Student;
import com.qlsv.model.StudentStatus;
import com.qlsv.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class EnrollmentDAOImplTest {

    private final StudentDAOImpl studentDAO = new StudentDAOImpl();
    private final CourseDAOImpl courseDAO = new CourseDAOImpl();
    private final SemesterDAOImpl semesterDAO = new SemesterDAOImpl();
    private final ClassSectionDAOImpl sectionDAO = new ClassSectionDAOImpl();
    private final ScheduleDAOImpl scheduleDAO = new ScheduleDAOImpl();
    private final EnrollmentDAOImpl enrollmentDAO = new EnrollmentDAOImpl();

    @BeforeEach
    @AfterEach
    void cleanUp() throws SQLException {
        try (Connection conn = DBConnection.getConnection()) {
            execute(conn, "DELETE FROM students WHERE student_code LIKE 'SV2097%'");
            execute(conn, "DELETE FROM class_sections WHERE course_id IN"
                    + " (SELECT id FROM courses WHERE course_code LIKE 'ZX9%')");
            execute(conn, "DELETE FROM courses WHERE course_code LIKE 'ZX9%'");
            execute(conn, "DELETE FROM semesters WHERE semester_code LIKE '2097-%'");
        }
    }

    @Test
    void registerFindCountAndCancel_thucThiDayDu() throws SQLException {
        Student st1 = createStudent("SV2097001", "Nguyễn Văn Test");
        Student st2 = createStudent("SV2097002", "Trần Thị Test");
        Semester sem = createSemester("2097-1", false);
        Course course = createCourse("ZX901", 3);
        ClassSection section = createSection("ZX901-01", course, sem, 30);
        scheduleDAO.insert(new Schedule(section, 2, 1, 3, "P101"));

        Enrollment e1 = new Enrollment(st1, section);
        Enrollment e2 = new Enrollment(st2, section);
        enrollmentDAO.register(e1);
        enrollmentDAO.register(e2);

        assertNotNull(e1.getId());
        assertEquals(2, enrollmentDAO.countBySection(section.getId()));
        assertEquals(2, enrollmentDAO.findBySemester(sem.getId()).size());
        assertEquals(2, enrollmentDAO.findBySection(section.getId()).size());

        Enrollment found = enrollmentDAO.findById(e1.getId());
        assertNotNull(found);
        assertEquals("SV2097001", found.getStudent().getStudentCode());
        assertEquals("ZX901-01", found.getSection().getSectionCode());

        List<Enrollment> byStudent = enrollmentDAO.findByStudentAndSemester(st1.getId(), sem.getId());
        assertEquals(1, byStudent.size());

        List<Schedule> schedules = enrollmentDAO.findSchedulesByStudentAndSemester(st1.getId(), sem.getId());
        assertEquals(1, schedules.size());
        assertEquals("ZX901-01", schedules.get(0).getSection().getSectionCode());

        assertTrue(enrollmentDAO.cancel(e1.getId()));
        assertNull(enrollmentDAO.findById(e1.getId()));
        assertFalse(enrollmentDAO.cancel(e1.getId()));
        assertEquals(1, enrollmentDAO.countBySection(section.getId()));
    }

    @Test
    void transaction_rollbackKhiKhongCommit() throws SQLException {
        Student st = createStudent("SV2097003", "Lê Văn Rollback");
        Semester sem = createSemester("2097-2", false);
        Course course = createCourse("ZX902", 3);
        ClassSection section = createSection("ZX902-01", course, sem, 30);

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            enrollmentDAO.register(conn, new Enrollment(st, section));
            assertEquals(1, enrollmentDAO.countBySection(conn, section.getId()));
            conn.rollback();
            conn.setAutoCommit(true);
        }

        assertEquals(0, enrollmentDAO.countBySection(section.getId()));
    }

    private Student createStudent(String code, String name) throws SQLException {
        Student s = new Student(code, name, LocalDate.of(2005, 5, 5), Gender.MALE,
                null, null, "CNTT-K25", 2097, StudentStatus.STUDYING);
        studentDAO.insert(s);
        return s;
    }

    private Semester createSemester(String code, boolean open) throws SQLException {
        Semester sem = new Semester(code, "Học kỳ test " + code,
                LocalDate.of(2097, 9, 1), LocalDate.of(2098, 1, 15), open);
        semesterDAO.insert(sem);
        return sem;
    }

    private Course createCourse(String code, int credits) throws SQLException {
        Course c = new Course(code, "Môn " + code, credits, null);
        courseDAO.insert(c);
        return c;
    }

    private ClassSection createSection(String code, Course c, Semester s, int max) throws SQLException {
        ClassSection cs = new ClassSection(code, c, s, "GV Test", max);
        sectionDAO.insert(cs);
        return cs;
    }

    private static void execute(Connection conn, String sql) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.executeUpdate();
        }
    }
}
