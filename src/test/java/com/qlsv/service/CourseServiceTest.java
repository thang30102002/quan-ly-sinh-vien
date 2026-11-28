package com.qlsv.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.qlsv.exception.BusinessException;
import com.qlsv.exception.ValidationException;
import com.qlsv.model.Course;
import com.qlsv.testutil.FakeCourseDAO;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Test CourseService bằng DAO giả lưu trong bộ nhớ, không cần CSDL.
 */
class CourseServiceTest {

    private FakeCourseDAO dao;
    private CourseService service;

    @BeforeEach
    void setUp() {
        dao = new FakeCourseDAO();
        service = new CourseService(dao);
    }

    // ---------- add ----------

    @Test
    void add_hopLe_chuanHoaVaLuu() {
        Course c = new Course("  it101 ", "  Nhập môn lập trình ", 3, "   ");
        service.add(c);

        assertNotNull(c.getId());
        Course saved = dao.byId.get(c.getId());
        assertEquals("IT101", saved.getCourseCode());
        assertEquals("Nhập môn lập trình", saved.getName());
        assertNull(saved.getDescription());
    }

    @Test
    void add_maTrung_nemBusinessException() {
        service.add(new Course("IT101", "Nhập môn lập trình", 3, null));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.add(new Course("it101", "Môn khác", 2, null)));
        assertTrue(ex.getMessage().contains("IT101"));
        assertTrue(ex.getMessage().contains("BR-02"));
        assertEquals(1, dao.byId.size());
    }

    @ParameterizedTest
    @ValueSource(strings = {"I101", "IT10", "ABCDE101", "IT-101", ""})
    void add_maSaiDinhDang_nemValidationException(String code) {
        ValidationException ex = assertThrows(ValidationException.class,
                () -> service.add(new Course(code, "Môn học", 3, null)));
        assertTrue(ex.getMessage().contains("BR-02"));
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 7, -1})
    void add_tinChiNgoai1Den6_nemValidationException(int credits) {
        ValidationException ex = assertThrows(ValidationException.class,
                () -> service.add(new Course("IT101", "Môn học", credits, null)));
        assertTrue(ex.getMessage().contains("BR-18"));
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 6})
    void add_tinChiBien1Va6_hopLe(int credits) {
        assertDoesNotThrow(() -> service.add(new Course("IT10" + credits, "Môn học", credits, null)));
    }

    @Test
    void add_thieuTen_nemValidationException() {
        assertThrows(ValidationException.class, () -> service.add(new Course("IT101", "   ", 3, null)));
    }

    @Test
    void add_tenQua150KyTu_nemValidationException() {
        assertThrows(ValidationException.class,
                () -> service.add(new Course("IT101", "a".repeat(151), 3, null)));
    }

    @Test
    void add_null_nemValidationException() {
        assertThrows(ValidationException.class, () -> service.add(null));
    }

    // ---------- update ----------

    @Test
    void update_hopLe_luuThayDoi() {
        Course c = addCourse("IT101", 3);
        service.update(new Course(c.getId(), "IT101", "Tên mới", 4, "Mô tả"));

        Course saved = dao.byId.get(c.getId());
        assertEquals("Tên mới", saved.getName());
        assertEquals(4, saved.getCredits());
        assertEquals("Mô tả", saved.getDescription());
    }

    @Test
    void update_doiMaSangMaDaCo_nemBusinessException() {
        addCourse("IT101", 3);
        Course c2 = addCourse("IT102", 3);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.update(new Course(c2.getId(), "IT101", "Môn 2", 3, null)));
        assertTrue(ex.getMessage().contains("BR-02"));
    }

    @Test
    void update_doiMaKhiChuaMoLop_duocPhep() {
        Course c = addCourse("IT101", 3);
        service.update(new Course(c.getId(), "IT199", "Môn học", 3, null));
        assertEquals("IT199", dao.byId.get(c.getId()).getCourseCode());
    }

    @Test
    void update_doiMaKhiDaMoLop_nemBusinessException() {
        Course c = addCourse("IT101", 3);
        dao.withSections.add(c.getId());

        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.update(new Course(c.getId(), "IT199", "Môn học", 3, null)));
        assertTrue(ex.getMessage().contains("BR-24"));
        assertEquals("IT101", dao.byId.get(c.getId()).getCourseCode());
    }

    @Test
    void update_doiTinChiKhiDaCoDangKy_nemBusinessException() {
        Course c = addCourse("IT101", 3);
        dao.withEnrollments.add(c.getId());

        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.update(new Course(c.getId(), "IT101", "Môn học", 4, null)));
        assertTrue(ex.getMessage().contains("BR-04"));
    }

    @Test
    void update_doiTenKhiDaCoDangKy_duocPhep() {
        Course c = addCourse("IT101", 3);
        dao.withEnrollments.add(c.getId());
        service.update(new Course(c.getId(), "IT101", "Tên mới", 3, null));
        assertEquals("Tên mới", dao.byId.get(c.getId()).getName());
    }

    @Test
    void update_khongTimThay_nemBusinessException() {
        assertThrows(BusinessException.class,
                () -> service.update(new Course(999, "IT101", "Môn học", 3, null)));
    }

    @Test
    void update_chuaChon_nemValidationException() {
        assertThrows(ValidationException.class,
                () -> service.update(new Course("IT101", "Môn học", 3, null)));
    }

    // ---------- delete ----------

    @Test
    void delete_chuaMoLop_xoaHan() {
        Course c = addCourse("IT101", 3);
        service.delete(c.getId());
        assertTrue(dao.byId.isEmpty());
    }

    @Test
    void delete_daCoDangKy_nemBusinessExceptionBR16() {
        Course c = addCourse("IT101", 3);
        dao.withSections.add(c.getId());
        dao.withEnrollments.add(c.getId());

        BusinessException ex = assertThrows(BusinessException.class, () -> service.delete(c.getId()));
        assertTrue(ex.getMessage().contains("đã có sinh viên đăng ký"));
        assertTrue(ex.getMessage().contains("BR-16"));
        assertEquals(1, dao.byId.size());
    }

    @Test
    void delete_daMoLopChuaAiDangKy_nemBusinessException() {
        Course c = addCourse("IT101", 3);
        dao.withSections.add(c.getId());

        BusinessException ex = assertThrows(BusinessException.class, () -> service.delete(c.getId()));
        assertTrue(ex.getMessage().contains("đã mở lớp"));
        assertEquals(1, dao.byId.size());
    }

    @Test
    void delete_khongTimThay_nemBusinessException() {
        assertThrows(BusinessException.class, () -> service.delete(42));
    }

    @Test
    void loiCSDL_boc_thanhBusinessException() {
        dao.failNext = true;
        BusinessException ex = assertThrows(BusinessException.class, () -> service.findAll());
        assertTrue(ex.getMessage().contains("Lỗi CSDL"));
    }

    // ---------- tiện ích ----------

    private Course addCourse(String code, int credits) {
        Course c = new Course(code, "Môn " + code, credits, null);
        service.add(c);
        return c;
    }
}
