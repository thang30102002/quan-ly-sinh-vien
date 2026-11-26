package com.qlsv.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.qlsv.exception.BusinessException;
import com.qlsv.exception.ValidationException;
import com.qlsv.model.Semester;
import com.qlsv.testutil.FakeSemesterDAO;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Test SemesterService bằng DAO giả lưu trong bộ nhớ, không cần CSDL.
 */
class SemesterServiceTest {

    private static final LocalDate START = LocalDate.of(2026, 9, 7);
    private static final LocalDate END = LocalDate.of(2027, 1, 15);

    private FakeSemesterDAO dao;
    private SemesterService service;

    @BeforeEach
    void setUp() {
        dao = new FakeSemesterDAO();
        service = new SemesterService(dao);
    }

    // ---------- add ----------

    @Test
    void add_hopLe_luuVaChuanHoa() {
        Semester s = new Semester(" 2026-1 ", "  HK1 2026-2027 ", START, END, false);
        service.add(s);

        assertNotNull(s.getId());
        Semester saved = dao.byId.get(s.getId());
        assertEquals("2026-1", saved.getSemesterCode());
        assertEquals("HK1 2026-2027", saved.getName());
    }

    @ParameterizedTest
    @ValueSource(strings = {"2026-4", "26-1", "2026/1", "2026-", ""})
    void add_maSaiDinhDang_nemValidationException(String code) {
        ValidationException ex = assertThrows(ValidationException.class,
                () -> service.add(new Semester(code, "HK", START, END, false)));
        assertTrue(ex.getMessage().contains("BR-03"));
    }

    @Test
    void add_ngayKetThucTruocNgayBatDau_nemValidationException() {
        ValidationException ex = assertThrows(ValidationException.class,
                () -> service.add(new Semester("2026-1", "HK1", END, START, false)));
        assertTrue(ex.getMessage().contains("BR-19"));
    }

    @Test
    void add_ngayKetThucBangNgayBatDau_nemValidationException() {
        ValidationException ex = assertThrows(ValidationException.class,
                () -> service.add(new Semester("2026-1", "HK1", START, START, false)));
        assertTrue(ex.getMessage().contains("BR-19"));
    }

    @Test
    void add_ngayKetThucSauBatDauMotNgay_hopLe() {
        service.add(new Semester("2026-3", "Hè", START, START.plusDays(1), false));
        assertEquals(1, dao.byId.size());
    }

    @Test
    void add_thieuNgay_nemValidationException() {
        assertThrows(ValidationException.class,
                () -> service.add(new Semester("2026-1", "HK1", null, END, false)));
        assertThrows(ValidationException.class,
                () -> service.add(new Semester("2026-1", "HK1", START, null, false)));
    }

    @Test
    void add_thieuTen_nemValidationException() {
        assertThrows(ValidationException.class,
                () -> service.add(new Semester("2026-1", "  ", START, END, false)));
    }

    @Test
    void add_maTrung_nemBusinessException() {
        service.add(new Semester("2026-1", "HK1", START, END, false));
        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.add(new Semester("2026-1", "Khác", START, END, false)));
        assertTrue(ex.getMessage().contains("BR-03"));
    }

    @Test
    void add_moKhiChuaCoHocKyMo_hopLe() {
        Semester s = new Semester("2026-1", "HK1", START, END, true);
        service.add(s);
        assertTrue(dao.byId.get(s.getId()).isOpen());
    }

    @Test
    void add_moKhiDaCoHocKyKhacMo_nemBusinessExceptionBR26() {
        service.add(new Semester("2025-2", "HK2", START.minusYears(1), END.minusYears(1), true));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.add(new Semester("2026-1", "HK1", START, END, true)));
        assertTrue(ex.getMessage().contains("2025-2"));
        assertTrue(ex.getMessage().contains("BR-26"));
        assertEquals(1, dao.byId.size());
    }

    @Test
    void add_dongKhiDaCoHocKyKhacMo_hopLe() {
        service.add(new Semester("2025-2", "HK2", START.minusYears(1), END.minusYears(1), true));
        service.add(new Semester("2026-1", "HK1", START, END, false));
        assertEquals(2, dao.byId.size());
    }

    // ---------- update ----------

    @Test
    void update_moHocKyDangMoChinhNo_hopLe() {
        Semester s = new Semester("2026-1", "HK1", START, END, true);
        service.add(s);
        service.update(new Semester(s.getId(), "2026-1", "Tên mới", START, END, true));
        assertEquals("Tên mới", dao.byId.get(s.getId()).getName());
    }

    @Test
    void update_moKhiHocKyKhacDangMo_nemBusinessExceptionBR26() {
        service.add(new Semester("2025-2", "HK2", START.minusYears(1), END.minusYears(1), true));
        Semester s = new Semester("2026-1", "HK1", START, END, false);
        service.add(s);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.update(new Semester(s.getId(), "2026-1", "HK1", START, END, true)));
        assertTrue(ex.getMessage().contains("BR-26"));
        assertFalse(dao.byId.get(s.getId()).isOpen());
    }

    @Test
    void update_dongRoiMoHocKyKhac_hopLe() {
        Semester old = new Semester("2025-2", "HK2", START.minusYears(1), END.minusYears(1), true);
        service.add(old);
        Semester s = new Semester("2026-1", "HK1", START, END, false);
        service.add(s);

        service.update(new Semester(old.getId(), "2025-2", "HK2", old.getStartDate(), old.getEndDate(), false));
        service.update(new Semester(s.getId(), "2026-1", "HK1", START, END, true));

        assertEquals(s.getId(), service.findOpen().getId());
    }

    @Test
    void update_doiMaSangMaDaCo_nemBusinessException() {
        service.add(new Semester("2025-2", "HK2", START.minusYears(1), END.minusYears(1), false));
        Semester s = new Semester("2026-1", "HK1", START, END, false);
        service.add(s);

        assertThrows(BusinessException.class,
                () -> service.update(new Semester(s.getId(), "2025-2", "HK1", START, END, false)));
    }

    @Test
    void update_ngaySai_nemValidationException() {
        Semester s = new Semester("2026-1", "HK1", START, END, false);
        service.add(s);
        assertThrows(ValidationException.class,
                () -> service.update(new Semester(s.getId(), "2026-1", "HK1", END, START, false)));
    }

    @Test
    void update_khongTimThay_nemBusinessException() {
        assertThrows(BusinessException.class,
                () -> service.update(new Semester(99, "2026-1", "HK1", START, END, false)));
    }

    // ---------- delete ----------

    @Test
    void delete_chuaMoLop_xoaHan() {
        Semester s = new Semester("2026-1", "HK1", START, END, false);
        service.add(s);
        service.delete(s.getId());
        assertTrue(dao.byId.isEmpty());
    }

    @Test
    void delete_daCoDangKy_nemBusinessExceptionBR16() {
        Semester s = new Semester("2026-1", "HK1", START, END, false);
        service.add(s);
        dao.withSections.add(s.getId());
        dao.withEnrollments.add(s.getId());

        BusinessException ex = assertThrows(BusinessException.class, () -> service.delete(s.getId()));
        assertTrue(ex.getMessage().contains("BR-16"));
        assertTrue(ex.getMessage().contains("đã có sinh viên đăng ký"));
    }

    @Test
    void delete_daMoLop_nemBusinessException() {
        Semester s = new Semester("2026-1", "HK1", START, END, false);
        service.add(s);
        dao.withSections.add(s.getId());

        BusinessException ex = assertThrows(BusinessException.class, () -> service.delete(s.getId()));
        assertTrue(ex.getMessage().contains("đã mở lớp"));
        assertEquals(1, dao.byId.size());
    }

    @Test
    void findOpen_khongCoHocKyMo_traVeNull() {
        service.add(new Semester("2026-1", "HK1", START, END, false));
        assertNull(service.findOpen());
    }
}
