package com.qlsv.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.qlsv.exception.BusinessException;
import com.qlsv.exception.ValidationException;
import com.qlsv.model.ClassSection;
import com.qlsv.model.Course;
import com.qlsv.model.Enrollment;
import com.qlsv.model.Gender;
import com.qlsv.model.Schedule;
import com.qlsv.model.Semester;
import com.qlsv.model.Student;
import com.qlsv.model.StudentStatus;
import com.qlsv.testutil.FakeEnrollmentDAO;
import com.qlsv.testutil.FakeScheduleDAO;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/**
 * Test ScheduleService (BR-21) bằng DAO giả lưu trong bộ nhớ, không cần CSDL.
 */
class ScheduleServiceTest {

    private FakeScheduleDAO dao;
    private ScheduleService service;
    private ClassSection sectionA;
    private ClassSection sectionB;

    @BeforeEach
    void setUp() {
        dao = new FakeScheduleDAO();
        service = new ScheduleService(dao);
        sectionA = section(1);
        sectionB = section(2);
    }

    // ---------- add ----------

    @Test
    void add_hopLe_luuVaChuanHoaPhong() {
        Schedule s = new Schedule(sectionA, 2, 1, 3, "  A101 ");
        service.add(s);

        assertNotNull(s.getId());
        assertEquals("A101", dao.byId.get(s.getId()).getRoom());
    }

    @Test
    void add_phongTrong_luuNull() {
        Schedule s = new Schedule(sectionA, 2, 1, 3, "   ");
        service.add(s);
        assertNull(dao.byId.get(s.getId()).getRoom());
    }

    @ParameterizedTest
    @CsvSource({"1,1,3", "9,1,3", "0,1,3"})
    void add_thuNgoaiKhoang_nemValidationException(int day, int start, int end) {
        ValidationException ex = assertThrows(ValidationException.class,
                () -> service.add(new Schedule(sectionA, day, start, end, null)));
        assertTrue(ex.getMessage().contains("BR-21"));
    }

    @ParameterizedTest
    @CsvSource({"2,0,3", "2,1,13", "2,13,13", "2,5,4"})
    void add_tietSai_nemValidationException(int day, int start, int end) {
        ValidationException ex = assertThrows(ValidationException.class,
                () -> service.add(new Schedule(sectionA, day, start, end, null)));
        assertTrue(ex.getMessage().contains("BR-21"));
    }

    @ParameterizedTest
    @CsvSource({"2,1,12", "8,12,12", "8,1,1"})
    void add_bienHopLe(int day, int start, int end) {
        service.add(new Schedule(sectionA, day, start, end, null));
        assertEquals(1, dao.byId.size());
    }

    @Test
    void add_thieuLop_nemValidationException() {
        assertThrows(ValidationException.class, () -> service.add(new Schedule(null, 2, 1, 3, null)));
    }

    @Test
    void add_trungBuoiCungLop_nemBusinessException() {
        service.add(new Schedule(sectionA, 2, 1, 3, "A101"));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.add(new Schedule(sectionA, 2, 3, 5, "A102")));
        assertTrue(ex.getMessage().contains("Thứ 2, tiết 1–3"));
        assertTrue(ex.getMessage().contains("BR-21"));
        assertEquals(1, dao.byId.size());
    }

    @Test
    void add_keNhauCungLop_hopLe() {
        service.add(new Schedule(sectionA, 2, 1, 3, null));
        service.add(new Schedule(sectionA, 2, 4, 6, null));
        assertEquals(2, dao.byId.size());
    }

    @Test
    void add_khacThuCungLop_hopLe() {
        service.add(new Schedule(sectionA, 2, 1, 3, null));
        service.add(new Schedule(sectionA, 3, 1, 3, null));
        assertEquals(2, dao.byId.size());
    }

    @Test
    void add_trungGioNhungKhacLop_hopLe() {
        // BR-21 chỉ xét các buổi trong cùng một lớp; trùng phòng/giảng viên ngoài phạm vi
        service.add(new Schedule(sectionA, 2, 1, 3, "A101"));
        service.add(new Schedule(sectionB, 2, 1, 3, "A101"));
        assertEquals(2, dao.byId.size());
    }

    // ---------- update ----------

    @Test
    void update_doiGioKhongTrungChinhNo_hopLe() {
        Schedule s = addSchedule(sectionA, 2, 1, 3);
        // Ca sửa chồng lên chính khoảng cũ của nó: không được tính là trùng
        service.update(new Schedule(s.getId(), sectionA, 2, 2, 4, "B201"));

        Schedule saved = dao.byId.get(s.getId());
        assertEquals(2, saved.getStartPeriod());
        assertEquals(4, saved.getEndPeriod());
        assertEquals("B201", saved.getRoom());
    }

    @Test
    void update_trungBuoiKhacCuaLop_nemBusinessException() {
        addSchedule(sectionA, 2, 1, 3);
        Schedule s2 = addSchedule(sectionA, 4, 1, 3);

        assertThrows(BusinessException.class,
                () -> service.update(new Schedule(s2.getId(), sectionA, 2, 2, 2, null)));
        assertEquals(4, dao.byId.get(s2.getId()).getDayOfWeek());
    }

    @Test
    void update_chuyenSangLopKhac_nemBusinessException() {
        Schedule s = addSchedule(sectionA, 2, 1, 3);
        assertThrows(BusinessException.class,
                () -> service.update(new Schedule(s.getId(), sectionB, 2, 1, 3, null)));
    }

    @Test
    void update_tietSai_nemValidationException() {
        Schedule s = addSchedule(sectionA, 2, 1, 3);
        assertThrows(ValidationException.class,
                () -> service.update(new Schedule(s.getId(), sectionA, 2, 4, 3, null)));
    }

    @Test
    void update_khongTimThay_nemBusinessException() {
        assertThrows(BusinessException.class,
                () -> service.update(new Schedule(99, sectionA, 2, 1, 3, null)));
    }

    // ---------- delete ----------

    @Test
    void delete_conBuoiKhac_xoaDuoc() {
        Schedule s1 = addSchedule(sectionA, 2, 1, 3);
        addSchedule(sectionA, 4, 1, 3);

        service.delete(s1.getId());
        assertEquals(1, service.findBySection(sectionA.getId()).size());
    }

    @Test
    void delete_buoiCuoiCung_nemBusinessExceptionBR21() {
        Schedule only = addSchedule(sectionA, 2, 1, 3);
        // Lớp khác có nhiều buổi không ảnh hưởng
        addSchedule(sectionB, 2, 1, 3);
        addSchedule(sectionB, 3, 1, 3);

        BusinessException ex = assertThrows(BusinessException.class, () -> service.delete(only.getId()));
        assertTrue(ex.getMessage().contains("ít nhất 1 buổi"));
        assertTrue(ex.getMessage().contains("BR-21"));
        assertEquals(3, dao.byId.size());
    }

    @Test
    void delete_khongTimThay_nemBusinessException() {
        assertThrows(BusinessException.class, () -> service.delete(123));
    }

    @Test
    void findBySection_sapTheoThuVaTiet() {
        addSchedule(sectionA, 5, 7, 9);
        addSchedule(sectionA, 2, 4, 6);
        addSchedule(sectionA, 2, 1, 3);

        List<Schedule> list = service.findBySection(sectionA.getId());
        assertEquals(3, list.size());
        assertEquals(1, list.get(0).getStartPeriod());
        assertEquals(4, list.get(1).getStartPeriod());
        assertEquals(5, list.get(2).getDayOfWeek());
    }

    @Test
    void add_trungLichSinhVienDaDangKy_nemBusinessExceptionBR23VaLietKeSinhVien() throws Exception {
        FakeEnrollmentDAO enrollmentDAO = new FakeEnrollmentDAO(dao);
        ScheduleService br23Service = new ScheduleService(dao, enrollmentDAO);
        Semester sem = new Semester(1, "2026-1", "HK1",
                LocalDate.of(2026, 9, 1), LocalDate.of(2027, 1, 15), true);
        ClassSection sec1 = fullSection(1, "IT101-01", sem);
        ClassSection sec2 = fullSection(2, "IT102-01", sem);
        Student sv1 = student(1, "SV2026001", "Nguyễn Văn An");
        Student sv2 = student(2, "SV2026002", "Trần Thị Bình");

        dao.insert(new Schedule(sec2, 2, 1, 3, "A102"));
        enrollmentDAO.register(new Enrollment(sv1, sec1));
        enrollmentDAO.register(new Enrollment(sv1, sec2));
        enrollmentDAO.register(new Enrollment(sv2, sec1));
        enrollmentDAO.register(new Enrollment(sv2, sec2));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> br23Service.add(new Schedule(sec1, 2, 2, 4, "A101")));
        assertTrue(ex.getMessage().contains("BR-23"));
        assertTrue(ex.getMessage().contains("SV2026001"));
        assertTrue(ex.getMessage().contains("SV2026002"));
    }

    @Test
    void update_trungLichSinhVienDaDangKy_nemBusinessExceptionBR23() throws Exception {
        FakeEnrollmentDAO enrollmentDAO = new FakeEnrollmentDAO(dao);
        ScheduleService br23Service = new ScheduleService(dao, enrollmentDAO);
        Semester sem = new Semester(1, "2026-1", "HK1",
                LocalDate.of(2026, 9, 1), LocalDate.of(2027, 1, 15), true);
        ClassSection sec1 = fullSection(1, "IT101-01", sem);
        ClassSection sec2 = fullSection(2, "IT102-01", sem);
        Student sv1 = student(1, "SV2026001", "Nguyễn Văn An");

        Schedule target = new Schedule(sec1, 3, 1, 3, "A101");
        dao.insert(target);
        dao.insert(new Schedule(sec2, 2, 1, 3, "A102"));
        enrollmentDAO.register(new Enrollment(sv1, sec1));
        enrollmentDAO.register(new Enrollment(sv1, sec2));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> br23Service.update(new Schedule(target.getId(), sec1, 2, 3, 5, "A101")));
        assertTrue(ex.getMessage().contains("BR-23"));
        assertTrue(ex.getMessage().contains("SV2026001"));
    }

    @Test
    void add_keLichSinhVienDaDangKy_hopLe() throws Exception {
        FakeEnrollmentDAO enrollmentDAO = new FakeEnrollmentDAO(dao);
        ScheduleService br23Service = new ScheduleService(dao, enrollmentDAO);
        Semester sem = new Semester(1, "2026-1", "HK1",
                LocalDate.of(2026, 9, 1), LocalDate.of(2027, 1, 15), true);
        ClassSection sec1 = fullSection(1, "IT101-01", sem);
        ClassSection sec2 = fullSection(2, "IT102-01", sem);
        Student sv1 = student(1, "SV2026001", "Nguyễn Văn An");

        dao.insert(new Schedule(sec2, 2, 1, 3, "A102"));
        enrollmentDAO.register(new Enrollment(sv1, sec1));
        enrollmentDAO.register(new Enrollment(sv1, sec2));

        br23Service.add(new Schedule(sec1, 2, 4, 6, "A101"));
        assertEquals(2, dao.byId.size());
    }

    private Schedule addSchedule(ClassSection section, int day, int start, int end) {
        Schedule s = new Schedule(section, day, start, end, null);
        service.add(s);
        return s;
    }

    private static ClassSection section(int id) {
        ClassSection s = new ClassSection();
        s.setId(id);
        return s;
    }

    private static ClassSection fullSection(int id, String code, Semester semester) {
        return new ClassSection(id, code, new Course(id, "IT10" + id, "Môn " + id, 3, null),
                semester, "GV", 40);
    }

    private static Student student(int id, String code, String name) {
        return new Student(id, code, name, LocalDate.of(2006, 1, 1), Gender.MALE,
                null, null, "CNTT-K24A", 2024, StudentStatus.STUDYING);
    }
}
