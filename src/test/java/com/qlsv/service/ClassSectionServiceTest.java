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
import com.qlsv.model.Semester;
import com.qlsv.testutil.FakeClassSectionDAO;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Test ClassSectionService bằng DAO giả lưu trong bộ nhớ, không cần CSDL.
 */
class ClassSectionServiceTest {

    private static final Course IT101 = new Course(1, "IT101", "Nhập môn lập trình", 3, null);
    private static final Course MATH201 = new Course(2, "MATH201", "Giải tích", 4, null);
    private static final Semester HK1 = new Semester(1, "2026-1", "HK1",
            LocalDate.of(2026, 9, 7), LocalDate.of(2027, 1, 15), true);
    private static final Semester HK2 = new Semester(2, "2026-2", "HK2",
            LocalDate.of(2027, 2, 15), LocalDate.of(2027, 6, 15), false);

    private FakeClassSectionDAO dao;
    private ClassSectionService service;

    @BeforeEach
    void setUp() {
        dao = new FakeClassSectionDAO();
        service = new ClassSectionService(dao);
    }

    // ---------- add ----------

    @Test
    void add_hopLe_chuanHoaVaLuu() {
        ClassSection s = new ClassSection(" it101-01 ", IT101, HK1, "  ThS. Nguyễn Văn A ", 40);
        service.add(s);

        assertNotNull(s.getId());
        ClassSection saved = dao.byId.get(s.getId());
        assertEquals("IT101-01", saved.getSectionCode());
        assertEquals("ThS. Nguyễn Văn A", saved.getLecturerName());
    }

    @Test
    void add_giangVienTrong_luuNull() {
        ClassSection s = new ClassSection("IT101-01", IT101, HK1, "   ", 40);
        service.add(s);
        assertNull(dao.byId.get(s.getId()).getLecturerName());
    }

    @Test
    void add_motHocPhanNhieuLopTrongHocKy_hopLe() {
        service.add(new ClassSection("IT101-01", IT101, HK1, null, 40));
        service.add(new ClassSection("IT101-02", IT101, HK1, null, 40));
        assertEquals(2, service.findBySemester(HK1.getId()).size());
    }

    @Test
    void add_maTrungTrongHocKy_nemBusinessExceptionBR24() {
        service.add(new ClassSection("IT101-01", IT101, HK1, null, 40));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.add(new ClassSection("it101-01", IT101, HK1, null, 30)));
        assertTrue(ex.getMessage().contains("IT101-01"));
        assertTrue(ex.getMessage().contains("2026-1"));
        assertTrue(ex.getMessage().contains("BR-24"));
    }

    @Test
    void add_cungMaKhacHocKy_hopLe() {
        service.add(new ClassSection("IT101-01", IT101, HK1, null, 40));
        service.add(new ClassSection("IT101-01", IT101, HK2, null, 40));
        assertEquals(2, dao.byId.size());
    }

    @ParameterizedTest
    @ValueSource(strings = {"IT101", "IT101-1", "IT101-001", "IT101_01", ""})
    void add_maSaiDinhDang_nemValidationException(String code) {
        ValidationException ex = assertThrows(ValidationException.class,
                () -> service.add(new ClassSection(code, IT101, HK1, null, 40)));
        assertTrue(ex.getMessage().contains("BR-24"));
    }

    @Test
    void add_tienToKhongKhopMaHocPhan_nemValidationException() {
        ValidationException ex = assertThrows(ValidationException.class,
                () -> service.add(new ClassSection("MATH201-01", IT101, HK1, null, 40)));
        assertTrue(ex.getMessage().contains("IT101"));
        assertTrue(ex.getMessage().contains("BR-24"));
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -5})
    void add_siSoKhongDuong_nemValidationException(int max) {
        ValidationException ex = assertThrows(ValidationException.class,
                () -> service.add(new ClassSection("IT101-01", IT101, HK1, null, max)));
        assertTrue(ex.getMessage().contains("BR-25"));
    }

    @Test
    void add_siSo1_hopLe() {
        service.add(new ClassSection("IT101-01", IT101, HK1, null, 1));
        assertEquals(1, dao.byId.size());
    }

    @Test
    void add_thieuHocPhanHoacHocKy_nemValidationException() {
        assertThrows(ValidationException.class,
                () -> service.add(new ClassSection("IT101-01", null, HK1, null, 40)));
        assertThrows(ValidationException.class,
                () -> service.add(new ClassSection("IT101-01", IT101, null, null, 40)));
    }

    @Test
    void add_tenGiangVienQuaDai_nemValidationException() {
        assertThrows(ValidationException.class,
                () -> service.add(new ClassSection("IT101-01", IT101, HK1, "a".repeat(101), 40)));
    }

    // ---------- update ----------

    @Test
    void update_doiGiangVienVaSiSo_hopLe() {
        ClassSection s = addSection("IT101-01", IT101, HK1);
        service.update(new ClassSection(s.getId(), "IT101-01", IT101, HK1, "GV mới", 60));

        ClassSection saved = dao.byId.get(s.getId());
        assertEquals("GV mới", saved.getLecturerName());
        assertEquals(60, saved.getMaxStudents());
    }

    @Test
    void update_doiMaTrungLopKhac_nemBusinessException() {
        addSection("IT101-01", IT101, HK1);
        ClassSection s2 = addSection("IT101-02", IT101, HK1);

        assertThrows(BusinessException.class,
                () -> service.update(new ClassSection(s2.getId(), "IT101-01", IT101, HK1, null, 40)));
    }

    @Test
    void update_siSoNhoHonSoDaDangKy_nemBusinessExceptionBR25() {
        ClassSection s = addSection("IT101-01", IT101, HK1);
        dao.enrollments.put(s.getId(), 30);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.update(new ClassSection(s.getId(), "IT101-01", IT101, HK1, null, 29)));
        assertTrue(ex.getMessage().contains("BR-25"));
        assertTrue(ex.getMessage().contains("30"));
    }

    @Test
    void update_siSoBangSoDaDangKy_hopLe() {
        ClassSection s = addSection("IT101-01", IT101, HK1);
        dao.enrollments.put(s.getId(), 30);
        service.update(new ClassSection(s.getId(), "IT101-01", IT101, HK1, null, 30));
        assertEquals(30, dao.byId.get(s.getId()).getMaxStudents());
    }

    @Test
    void update_doiHocKyKhiDaCoDangKy_nemBusinessExceptionBR16() {
        ClassSection s = addSection("IT101-01", IT101, HK1);
        dao.enrollments.put(s.getId(), 1);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.update(new ClassSection(s.getId(), "IT101-01", IT101, HK2, null, 40)));
        assertTrue(ex.getMessage().contains("BR-16"));
    }

    @Test
    void update_doiHocPhanKhiChuaCoDangKy_hopLe() {
        ClassSection s = addSection("IT101-01", IT101, HK1);
        service.update(new ClassSection(s.getId(), "MATH201-01", MATH201, HK1, null, 40));
        assertEquals("MATH201-01", dao.byId.get(s.getId()).getSectionCode());
    }

    @Test
    void update_khongTimThay_nemBusinessException() {
        assertThrows(BusinessException.class,
                () -> service.update(new ClassSection(77, "IT101-01", IT101, HK1, null, 40)));
    }

    // ---------- delete ----------

    @Test
    void delete_chuaCoDangKy_xoaHan() {
        ClassSection s = addSection("IT101-01", IT101, HK1);
        service.delete(s.getId());
        assertTrue(dao.byId.isEmpty());
    }

    @Test
    void delete_daCoDangKy_nemBusinessExceptionBR16() {
        ClassSection s = addSection("IT101-01", IT101, HK1);
        dao.enrollments.put(s.getId(), 2);

        BusinessException ex = assertThrows(BusinessException.class, () -> service.delete(s.getId()));
        assertTrue(ex.getMessage().contains("BR-16"));
        assertEquals(1, dao.byId.size());
    }

    @Test
    void delete_khongTimThay_nemBusinessException() {
        assertThrows(BusinessException.class, () -> service.delete(5));
    }

    private ClassSection addSection(String code, Course course, Semester semester) {
        ClassSection s = new ClassSection(code, course, semester, null, 40);
        service.add(s);
        return s;
    }
}
