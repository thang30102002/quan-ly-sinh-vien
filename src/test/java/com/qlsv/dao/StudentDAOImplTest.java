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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Cần database Docker đang chạy: docker compose up -d (xem docs/docker.md).
 *
 * <p>Tạm chạy trên CSDL qlsv cho tới khi có qlsv_test (#35). Chỉ dùng mã SV20269xx
 * (seed.sql không dùng) và xóa sạch sau mỗi test.
 */
class StudentDAOImplTest {

    private static final String TEST_CODE_PATTERN = "SV20269%";

    private final StudentDAOImpl dao = new StudentDAOImpl();

    @BeforeEach
    @AfterEach
    void cleanUp() throws SQLException {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "DELETE FROM students WHERE student_code LIKE ?")) {
            ps.setString(1, TEST_CODE_PATTERN);
            ps.executeUpdate();
        }
    }

    @Test
    void insert_ghiDuCacCotVaGanIdMoi() throws SQLException {
        Student s = newStudent("SV2026901");

        dao.insert(s);

        assertNotNull(s.getId(), "insert phải gán id vừa sinh");
        Student saved = loadById(s.getId());
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
        dao.insert(newStudent("SV2026902"));

        assertThrows(SQLException.class, () -> dao.insert(newStudent("SV2026902")));
    }

    @Test
    void update_suaCacCot() throws SQLException {
        Student s = newStudent("SV2026903");
        dao.insert(s);

        s.setFullName("Trần Văn Bình");
        s.setGender(Gender.MALE);
        s.setEmail("binh@example.com");
        s.setPhone(null);
        s.setStatus(StudentStatus.RESERVED);
        assertTrue(dao.update(s));

        Student saved = loadById(s.getId());
        assertEquals("Trần Văn Bình", saved.getFullName());
        assertEquals(Gender.MALE, saved.getGender());
        assertEquals("binh@example.com", saved.getEmail());
        assertNull(saved.getPhone());
        assertEquals(StudentStatus.RESERVED, saved.getStatus());
    }

    @Test
    void update_idKhongTonTai_traVeFalse() throws SQLException {
        Student s = newStudent("SV2026904");
        s.setId(-1);

        assertFalse(dao.update(s));
    }

    @Test
    void delete_xoaDuocVaIdKhongTonTaiTraVeFalse() throws SQLException {
        Student s = newStudent("SV2026905");
        dao.insert(s);

        assertTrue(dao.delete(s.getId()));
        assertNull(loadById(s.getId()));
        assertFalse(dao.delete(s.getId()));
    }

    private static Student newStudent(String code) {
        return new Student(code, "Nguyễn Thị Ánh Dương", LocalDate.of(2006, 5, 1), Gender.FEMALE,
                null, "0912345678", "CNTT-K26A", 2026, StudentStatus.STUDYING);
    }

    /** Đọc lại bằng SQL trực tiếp (findById thuộc #34) và kiểm tra luôn mapRow. */
    private Student loadById(int id) throws SQLException {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT * FROM students WHERE id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? dao.mapRow(rs) : null;
            }
        }
    }
}
