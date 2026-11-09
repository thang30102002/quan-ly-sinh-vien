package com.qlsv.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.qlsv.model.Gender;
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

class StudentDAOImplTest {

    private final StudentDAOImpl dao = new StudentDAOImpl();

    @BeforeEach
    @AfterEach
    void cleanUp() throws SQLException {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM students")) {
            ps.executeUpdate();
        }
    }

    @Test
    void insert_ghiDuCacCotVaGanIdMoi() throws SQLException {
        Student s = newStudent("SV2026901", "Nguyễn Thị Ánh Dương", "CNTT-K26A");
        dao.insert(s);

        assertNotNull(s.getId());
        Student saved = dao.findById(s.getId());
        assertNotNull(saved);
        assertEquals("SV2026901", saved.getStudentCode());
        assertEquals("Nguyễn Thị Ánh Dương", saved.getFullName());
        assertEquals(LocalDate.of(2006, 5, 1), saved.getDob());
        assertEquals(Gender.FEMALE, saved.getGender());
        assertNull(saved.getEmail());
        assertEquals("0912345678", saved.getPhone());
        assertEquals("CNTT-K26A", saved.getClassName());
        assertEquals(2026, saved.getIntakeYear());
        assertEquals(StudentStatus.STUDYING, saved.getStatus());
    }

    @Test
    void insert_trungMaSinhVien_nemSQLException() throws SQLException {
        dao.insert(newStudent("SV2026902", "Trần Văn Bình", "CNTT-K26A"));
        assertThrows(SQLException.class, () -> dao.insert(newStudent("SV2026902", "Lê Văn Cường", "CNTT-K26B")));
    }

    @Test
    void update_suaCacCot() throws SQLException {
        Student s = newStudent("SV2026903", "Trần Văn Bình", "CNTT-K26A");
        dao.insert(s);

        s.setFullName("Trần Văn Bình An");
        s.setGender(Gender.MALE);
        s.setEmail("binhan@example.com");
        s.setPhone(null);
        s.setStatus(StudentStatus.RESERVED);
        assertTrue(dao.update(s));

        Student saved = dao.findById(s.getId());
        assertEquals("Trần Văn Bình An", saved.getFullName());
        assertEquals(Gender.MALE, saved.getGender());
        assertEquals("binhan@example.com", saved.getEmail());
        assertNull(saved.getPhone());
        assertEquals(StudentStatus.RESERVED, saved.getStatus());
    }

    @Test
    void update_idKhongTonTai_traVeFalse() throws SQLException {
        Student s = newStudent("SV2026904", "Phạm Hoàng Long", "CNTT-K26A");
        s.setId(-1);
        assertFalse(dao.update(s));
    }

    @Test
    void delete_xoaDuocVaIdKhongTonTaiTraVeFalse() throws SQLException {
        Student s = newStudent("SV2026905", "Đỗ Minh Tuấn", "CNTT-K26A");
        dao.insert(s);

        assertTrue(dao.delete(s.getId()));
        assertNull(dao.findById(s.getId()));
        assertFalse(dao.delete(s.getId()));
    }

    @Test
    void findById_tonTai_traVeSinhVien() throws SQLException {
        Student s = newStudent("SV2026906", "Vũ Mai Phương", "CNTT-K26B");
        dao.insert(s);

        Student found = dao.findById(s.getId());
        assertNotNull(found);
        assertEquals(s.getId(), found.getId());
        assertEquals("SV2026906", found.getStudentCode());
        assertEquals("Vũ Mai Phương", found.getFullName());
    }

    @Test
    void findById_khongTonTai_traVeNull() throws SQLException {
        assertNull(dao.findById(999999));
        assertNull(dao.findById(-1));
    }

    @Test
    void findByCode_chuanHoaVaTimDung() throws SQLException {
        Student s = newStudent("SV2026907", "Huỳnh Quốc Bảo", "CNTT-K26B");
        dao.insert(s);

        Student found1 = dao.findByCode("SV2026907");
        assertNotNull(found1);
        assertEquals(s.getId(), found1.getId());

        Student found2 = dao.findByCode("  sv2026907  ");
        assertNotNull(found2);
        assertEquals(s.getId(), found2.getId());
    }

    @Test
    void findByCode_khongTonTaiHoacRong_traVeNull() throws SQLException {
        assertNull(dao.findByCode(null));
        assertNull(dao.findByCode(""));
        assertNull(dao.findByCode("   "));
        assertNull(dao.findByCode("SV2026999"));
    }

    @Test
    void findAll_traVeDanhSachSapXepTheoMa() throws SQLException {
        dao.insert(newStudent("SV2026910", "Bùi Tiến Dũng", "CNTT-K26A"));
        dao.insert(newStudent("SV2026908", "An Đình Trọng", "CNTT-K26A"));
        dao.insert(newStudent("SV2026909", "Cao Văn Thắng", "CNTT-K26A"));

        List<Student> list = dao.findAll();
        assertEquals(3, list.size());
        assertEquals("SV2026908", list.get(0).getStudentCode());
        assertEquals("SV2026909", list.get(1).getStudentCode());
        assertEquals("SV2026910", list.get(2).getStudentCode());
    }

    @Test
    void search_theoTenTiengVietCoDauVaKhongPhanBietHoaThuong() throws SQLException {
        dao.insert(newStudent("SV2026911", "Nguyễn Thị Ánh Dương", "CNTT-K26A"));
        dao.insert(newStudent("SV2026912", "Huỳnh Văn Đạt", "CNTT-K26A"));
        dao.insert(newStudent("SV2026913", "Lê Ánh Nguyệt", "CNTT-K26B"));

        List<Student> res1 = dao.search("ánh", null);
        assertEquals(2, res1.size());
        assertEquals("SV2026911", res1.get(0).getStudentCode());
        assertEquals("SV2026913", res1.get(1).getStudentCode());

        List<Student> res2 = dao.search("DƯƠNG", null);
        assertEquals(1, res2.size());
        assertEquals("SV2026911", res2.get(0).getStudentCode());

        List<Student> res3 = dao.search("huỳnh", null);
        assertEquals(1, res3.size());
        assertEquals("SV2026912", res3.get(0).getStudentCode());

        List<Student> res4 = dao.search("Huy\u0300nh", null);
        assertEquals(1, res4.size());
        assertEquals("SV2026912", res4.get(0).getStudentCode());
    }

    @Test
    void search_theoMaSinhVien() throws SQLException {
        dao.insert(newStudent("SV2026914", "Nguyễn Văn An", "CNTT-K26A"));
        dao.insert(newStudent("SV2026915", "Trần Văn Bình", "CNTT-K26A"));

        List<Student> list = dao.search("26915", null);
        assertEquals(1, list.size());
        assertEquals("SV2026915", list.get(0).getStudentCode());

        List<Student> list2 = dao.search("sv2026", null);
        assertEquals(2, list2.size());
    }

    @Test
    void search_ketHopTuKhoaVaLop() throws SQLException {
        dao.insert(newStudent("SV2026916", "Nguyễn Văn Toàn", "CNTT-K26A"));
        dao.insert(newStudent("SV2026917", "Nguyễn Văn Toàn", "CNTT-K26B"));

        List<Student> list = dao.search("Toàn", "cntt-k26a");
        assertEquals(1, list.size());
        assertEquals("SV2026916", list.get(0).getStudentCode());
    }

    @Test
    void search_chiTheoLopKhiTuKhoaRong() throws SQLException {
        dao.insert(newStudent("SV2026918", "Phan Văn Đức", "CNTT-K26A"));
        dao.insert(newStudent("SV2026919", "Đoàn Văn Hậu", "CNTT-K26B"));

        List<Student> list = dao.search(null, "CNTT-K26A");
        assertEquals(1, list.size());
        assertEquals("SV2026918", list.get(0).getStudentCode());

        List<Student> list2 = dao.search("   ", "CNTT-K26B");
        assertEquals(1, list2.size());
        assertEquals("SV2026919", list2.get(0).getStudentCode());
    }

    @Test
    void search_kyTuDacBietLike() throws SQLException {
        dao.insert(newStudent("SV2026920", "Nguyễn Văn A", "CNTT-K26A"));

        assertTrue(dao.search("%", null).isEmpty());
        assertTrue(dao.search("_", null).isEmpty());
        assertTrue(dao.search("' OR 1=1 --", null).isEmpty());
    }

    @Test
    void search_khongKhop_traVeDanhSachRong() throws SQLException {
        dao.insert(newStudent("SV2026921", "Nguyễn Văn A", "CNTT-K26A"));

        List<Student> list = dao.search("KhôngCóTênNày", null);
        assertTrue(list.isEmpty());
    }

    private static Student newStudent(String code, String name, String className) {
        return new Student(code, name, LocalDate.of(2006, 5, 1), Gender.FEMALE,
                null, "0912345678", className, 2026, StudentStatus.STUDYING);
    }
}
