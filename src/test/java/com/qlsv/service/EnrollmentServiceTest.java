package com.qlsv.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.qlsv.exception.BusinessException;
import com.qlsv.model.ClassSection;
import com.qlsv.model.Course;
import com.qlsv.model.Enrollment;
import com.qlsv.model.Gender;
import com.qlsv.model.Schedule;
import com.qlsv.model.Semester;
import com.qlsv.model.Student;
import com.qlsv.model.StudentStatus;
import com.qlsv.testutil.FakeClassSectionDAO;
import com.qlsv.testutil.FakeEnrollmentDAO;
import com.qlsv.testutil.FakeScheduleDAO;
import com.qlsv.testutil.FakeStudentDAO;

import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class EnrollmentServiceTest {

    private FakeStudentDAO studentDAO;
    private FakeClassSectionDAO sectionDAO;
    private FakeScheduleDAO scheduleDAO;
    private FakeEnrollmentDAO enrollmentDAO;
    private RecordingTransaction tx;
    private EnrollmentService service;

    private Student studyingStudent;
    private Semester openSemester;
    private Semester closedSemester;
    private Course course3A;
    private Course course3B;
    private Course course4;
    private Course course2;

    @BeforeEach
    void setUp() throws SQLException {
        studentDAO = new FakeStudentDAO();
        sectionDAO = new FakeClassSectionDAO();
        scheduleDAO = new FakeScheduleDAO();
        enrollmentDAO = new FakeEnrollmentDAO(scheduleDAO);
        tx = new RecordingTransaction();
        service = new EnrollmentService(studentDAO, sectionDAO, scheduleDAO, enrollmentDAO, tx::connection);

        studyingStudent = insertStudent("SV2026001", "Nguyễn Văn An", StudentStatus.STUDYING);
        openSemester = semester(1, "2026-1", true);
        closedSemester = semester(2, "2025-2", false);

        course3A = course(1, "IT101", 3);
        course3B = course(2, "IT102", 3);
        course4 = course(3, "MATH101", 4);
        course2 = course(4, "ENG101", 2);
    }

    @Test
    void register_hopLe_luuVaCommitTransaction() throws SQLException {
        ClassSection section = insertSection("IT101-01", course3A, openSemester, 40);
        scheduleDAO.insert(new Schedule(section, 2, 1, 3, "A101"));

        Enrollment created = service.register(studyingStudent.getId(), section.getId());

        assertNotNull(created.getId());
        assertEquals(1, enrollmentDAO.byId.size());
        assertEquals(1, service.countBySection(section.getId()));
        assertTrue(tx.autoCommitDisabled);
        assertTrue(tx.committed);
        assertFalse(tx.rolledBack);
        assertTrue(tx.closed);
    }

    @Test
    void register_sinhVienKhongTonTai_nemBusinessException() {
        ClassSection section = insertSectionUnchecked("IT101-01", course3A, openSemester, 40);
        assertThrows(BusinessException.class, () -> service.register(999, section.getId()));
        assertFalse(tx.committed);
    }

    @ParameterizedTest
    @EnumSource(value = StudentStatus.class, names = {"RESERVED", "DROPPED", "GRADUATED"})
    void register_sinhVienKhongDangHoc_nemBusinessExceptionBR07(StudentStatus status) throws SQLException {
        Student student = insertStudent("SV2026002", "Trần Thị B", status);
        ClassSection section = insertSection("IT101-01", course3A, openSemester, 40);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.register(student.getId(), section.getId()));
        assertTrue(ex.getMessage().contains("BR-07"));
        assertEquals(0, enrollmentDAO.byId.size());
    }

    @Test
    void register_lopKhongTonTai_nemBusinessException() {
        assertThrows(BusinessException.class, () -> service.register(studyingStudent.getId(), 999));
    }

    @Test
    void register_hocKyDaDong_nemBusinessExceptionBR26() throws SQLException {
        ClassSection closedSection = insertSection("IT101-01", course3A, closedSemester, 40);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.register(studyingStudent.getId(), closedSection.getId()));
        assertTrue(ex.getMessage().contains("BR-26"));
        assertEquals(0, enrollmentDAO.byId.size());
    }

    @Test
    void register_trungLopDaDangKy_nemBusinessExceptionBR05() throws SQLException {
        ClassSection section = insertSection("IT101-01", course3A, openSemester, 40);
        service.register(studyingStudent.getId(), section.getId());

        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.register(studyingStudent.getId(), section.getId()));
        assertTrue(ex.getMessage().contains("BR-05"));
        assertTrue(tx.rolledBack);
        assertEquals(1, enrollmentDAO.byId.size());
    }

    @Test
    void register_trungMonKhacLopTrongHocKy_nemBusinessExceptionBR05() throws SQLException {
        ClassSection sec1 = insertSection("IT101-01", course3A, openSemester, 40);
        ClassSection sec2 = insertSection("IT101-02", course3A, openSemester, 40);
        scheduleDAO.insert(new Schedule(sec1, 2, 1, 3, "A101"));
        scheduleDAO.insert(new Schedule(sec2, 3, 1, 3, "A102"));
        service.register(studyingStudent.getId(), sec1.getId());

        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.register(studyingStudent.getId(), sec2.getId()));
        assertTrue(ex.getMessage().contains("BR-05"));
        assertTrue(ex.getMessage().contains("IT101-01"));
    }

    @Test
    void register_hocLaiMonOHocKyKhac_hopLeBR06() throws SQLException {
        ClassSection oldSection = insertSection("IT101-01", course3A, closedSemester, 40);
        enrollmentDAO.register(new Enrollment(studyingStudent, oldSection));

        ClassSection newSection = insertSection("IT101-01", course3A, openSemester, 40);
        Enrollment created = service.register(studyingStudent.getId(), newSection.getId());

        assertNotNull(created.getId());
        assertEquals(1, service.findByStudentAndSemester(studyingStudent.getId(), openSemester.getId()).size());
    }

    @Test
    void register_dung12TinChi_hopLeBR04() throws SQLException {
        ClassSection s1 = insertSection("IT101-01", course3A, openSemester, 40);
        ClassSection s2 = insertSection("IT102-01", course3B, openSemester, 40);
        ClassSection s3 = insertSection("MATH101-01", course4, openSemester, 40);
        ClassSection s4 = insertSection("ENG101-01", course2, openSemester, 40);

        service.register(studyingStudent.getId(), s1.getId());
        service.register(studyingStudent.getId(), s2.getId());
        service.register(studyingStudent.getId(), s3.getId());
        service.register(studyingStudent.getId(), s4.getId());

        assertEquals(4, service.findByStudentAndSemester(studyingStudent.getId(), openSemester.getId()).size());
    }

    @Test
    void register_dang10TinChiThem3_nemBusinessExceptionBR04() throws SQLException {
        ClassSection s1 = insertSection("IT101-01", course3A, openSemester, 40);
        ClassSection s2 = insertSection("IT102-01", course3B, openSemester, 40);
        ClassSection s3 = insertSection("MATH101-01", course4, openSemester, 40);
        ClassSection sExtra = insertSection("PHY101-01", course(5, "PHY101", 3), openSemester, 40);

        service.register(studyingStudent.getId(), s1.getId());
        service.register(studyingStudent.getId(), s2.getId());
        service.register(studyingStudent.getId(), s3.getId());

        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.register(studyingStudent.getId(), sExtra.getId()));
        assertTrue(ex.getMessage().contains("BR-04"));
        assertTrue(ex.getMessage().contains("10"));
        assertTrue(ex.getMessage().contains("3"));
    }

    @Test
    void register_lopDuSiSo_nemBusinessExceptionBR25() throws SQLException {
        Student other = insertStudent("SV2026003", "Lê Văn C", StudentStatus.STUDYING);
        ClassSection fullSection = insertSection("IT101-01", course3A, openSemester, 1);
        service.register(other.getId(), fullSection.getId());

        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.register(studyingStudent.getId(), fullSection.getId()));
        assertTrue(ex.getMessage().contains("BR-25"));
        assertEquals(1, service.countBySection(fullSection.getId()));
    }

    @Test
    void register_trungLichThuHaiTiet13Va35_nemBusinessExceptionBR22() throws SQLException {
        ClassSection sec1 = insertSection("IT101-01", course3A, openSemester, 40);
        ClassSection sec2 = insertSection("IT102-01", course3B, openSemester, 40);
        scheduleDAO.insert(new Schedule(sec1, 2, 1, 3, "A101"));
        scheduleDAO.insert(new Schedule(sec2, 2, 3, 5, "A102"));

        service.register(studyingStudent.getId(), sec1.getId());

        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.register(studyingStudent.getId(), sec2.getId()));
        assertTrue(ex.getMessage().contains("BR-22"));
        assertTrue(ex.getMessage().contains("IT101-01"));
    }

    @Test
    void register_keLichVaKhacThu_hopLeBR22() throws SQLException {
        ClassSection sec1 = insertSection("IT101-01", course3A, openSemester, 40);
        ClassSection secAdjacent = insertSection("IT102-01", course3B, openSemester, 40);
        ClassSection secOtherDay = insertSection("ENG101-01", course2, openSemester, 40);
        scheduleDAO.insert(new Schedule(sec1, 2, 1, 3, "A101"));
        scheduleDAO.insert(new Schedule(secAdjacent, 2, 4, 6, "A102"));
        scheduleDAO.insert(new Schedule(secOtherDay, 3, 1, 3, "A103"));

        service.register(studyingStudent.getId(), sec1.getId());
        service.register(studyingStudent.getId(), secAdjacent.getId());
        service.register(studyingStudent.getId(), secOtherDay.getId());

        assertEquals(3, service.findSchedulesByStudentAndSemester(
                studyingStudent.getId(), openSemester.getId()).size());
    }

    @Test
    void cancel_chuaCoDiem_xoaVaCommit() throws SQLException {
        ClassSection section = insertSection("IT101-01", course3A, openSemester, 40);
        Enrollment enrollment = service.register(studyingStudent.getId(), section.getId());

        tx.reset();
        service.cancel(enrollment.getId());

        assertEquals(0, enrollmentDAO.byId.size());
        assertTrue(tx.autoCommitDisabled);
        assertTrue(tx.committed);
        assertFalse(tx.rolledBack);
        assertTrue(tx.closed);
    }

    @Test
    void cancel_khongTimThay_nemBusinessException() {
        assertThrows(BusinessException.class, () -> service.cancel(999));
        assertTrue(tx.rolledBack);
    }

    @Test
    void cancel_hocKyDaDong_nemBusinessExceptionBR26() throws SQLException {
        ClassSection closedSection = insertSection("IT101-01", course3A, closedSemester, 40);
        Enrollment enrollment = new Enrollment(studyingStudent, closedSection);
        enrollmentDAO.register(enrollment);

        BusinessException ex = assertThrows(BusinessException.class, () -> service.cancel(enrollment.getId()));
        assertTrue(ex.getMessage().contains("BR-26"));
        assertEquals(1, enrollmentDAO.byId.size());
    }

    @Test
    void cancel_daCoDiemThanhPhan_nemBusinessExceptionBR15() throws SQLException {
        ClassSection section = insertSection("IT101-01", course3A, openSemester, 40);
        Enrollment enrollment = new Enrollment(studyingStudent, section);
        enrollment.setAttendance(new BigDecimal("8.0"));
        enrollmentDAO.register(enrollment);

        BusinessException ex = assertThrows(BusinessException.class, () -> service.cancel(enrollment.getId()));
        assertTrue(ex.getMessage().contains("BR-15"));
        assertTrue(tx.rolledBack);
        assertEquals(1, enrollmentDAO.byId.size());
    }

    @Test
    void register_loiSqlKhiGhi_rollbackVaNemBusinessException() throws SQLException {
        ClassSection section = insertSection("IT101-01", course3A, openSemester, 40);
        enrollmentDAO.failOnRegister = new SQLException("connection reset", "08006");

        assertThrows(BusinessException.class,
                () -> service.register(studyingStudent.getId(), section.getId()));
        assertTrue(tx.autoCommitDisabled);
        assertFalse(tx.committed);
        assertTrue(tx.rolledBack);
        assertTrue(tx.closed);
    }

    @Test
    void queries_delegateCorrectly() throws SQLException {
        ClassSection section = insertSection("IT101-01", course3A, openSemester, 40);
        service.register(studyingStudent.getId(), section.getId());

        List<Enrollment> bySemester = service.findBySemester(openSemester.getId());
        List<Enrollment> bySection = service.findBySection(section.getId());

        assertEquals(1, bySemester.size());
        assertEquals(1, bySection.size());
    }

    private Student insertStudent(String code, String name, StudentStatus status) throws SQLException {
        Student s = new Student(code, name, LocalDate.of(2006, 1, 1), Gender.MALE,
                null, null, "CNTT-K24A", 2024, status);
        studentDAO.insert(s);
        return s;
    }

    private ClassSection insertSection(String code, Course course, Semester semester, int maxStudents)
            throws SQLException {
        ClassSection s = new ClassSection(code, course, semester, "GV A", maxStudents);
        sectionDAO.insert(s);
        return s;
    }

    private ClassSection insertSectionUnchecked(String code, Course course, Semester semester, int maxStudents) {
        ClassSection s = new ClassSection(code, course, semester, "GV A", maxStudents);
        s.setId(999);
        return s;
    }

    private static Course course(int id, String code, int credits) {
        return new Course(id, code, "Học phần " + code, credits, null);
    }

    private static Semester semester(int id, String code, boolean open) {
        return new Semester(id, code, "Học kỳ " + code,
                LocalDate.of(2026, 9, 1), LocalDate.of(2027, 1, 15), open);
    }

    private static final class RecordingTransaction {
        boolean autoCommitDisabled;
        boolean committed;
        boolean rolledBack;
        boolean closed;

        void reset() {
            autoCommitDisabled = false;
            committed = false;
            rolledBack = false;
            closed = false;
        }

        Connection connection() {
            return (Connection) Proxy.newProxyInstance(
                    Connection.class.getClassLoader(),
                    new Class<?>[]{Connection.class},
                    (proxy, method, args) -> switch (method.getName()) {
                        case "setAutoCommit" -> {
                            if (Boolean.FALSE.equals(args[0])) {
                                autoCommitDisabled = true;
                            }
                            yield null;
                        }
                        case "commit" -> {
                            committed = true;
                            yield null;
                        }
                        case "rollback" -> {
                            rolledBack = true;
                            yield null;
                        }
                        case "close" -> {
                            closed = true;
                            yield null;
                        }
                        case "getAutoCommit" -> !autoCommitDisabled;
                        case "isClosed" -> closed;
                        default -> null;
                    });
        }
    }
}
