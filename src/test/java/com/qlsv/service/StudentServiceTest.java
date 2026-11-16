package com.qlsv.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.qlsv.dao.StudentDAO;
import com.qlsv.exception.BusinessException;
import com.qlsv.exception.ValidationException;
import com.qlsv.model.Gender;
import com.qlsv.model.Student;
import com.qlsv.model.StudentStatus;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test StudentService bằng DAO giả lưu trong bộ nhớ, không cần CSDL.
 */
class StudentServiceTest {

    private FakeStudentDAO dao;
    private StudentService service;

    @BeforeEach
    void setUp() {
        dao = new FakeStudentDAO();
        service = new StudentService(dao);
    }

    // ---------- add ----------

    @Test
    void add_hopLe_luuVaChuanHoaDuLieu() {
        Student s = newStudent("  sv2026001 ", "  Nguyễn Văn An  ");
        s.setEmail("   ");
        s.setPhone(" 0912345678 ");
        s.setStatus(null);

        service.add(s);

        assertNotNull(s.getId());
        Student saved = dao.byId.get(s.getId());
        assertEquals("SV2026001", saved.getStudentCode());
        assertEquals("Nguyễn Văn An", saved.getFullName());
        assertNull(saved.getEmail());
        assertEquals("0912345678", saved.getPhone());
        assertEquals(StudentStatus.STUDYING, saved.getStatus());
    }

    @Test
    void add_maTrung_nemBusinessException() {
        service.add(newStudent("SV2026001", "Nguyễn Văn An"));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.add(newStudent("sv2026001", "Trần Văn Bình")));
        assertTrue(ex.getMessage().contains("SV2026001"));
        assertTrue(ex.getMessage().contains("đã tồn tại"));
        assertTrue(ex.getMessage().contains("BR-01"));
        assertEquals(1, dao.byId.size());
    }

    @Test
    void add_emailSai_nemValidationException() {
        Student s = newStudent("SV2026002", "Lê Thị Cúc");
        s.setEmail("cuc@@example");

        ValidationException ex = assertThrows(ValidationException.class, () -> service.add(s));
        assertTrue(ex.getMessage().contains("Email"));
        assertTrue(ex.getMessage().contains("BR-17"));
        assertTrue(dao.byId.isEmpty());
    }

    @Test
    void add_maSaiDinhDang_nemValidationException() {
        ValidationException ex = assertThrows(ValidationException.class,
                () -> service.add(newStudent("SV26001", "Lê Thị Cúc")));
        assertTrue(ex.getMessage().contains("BR-01"));
    }

    @Test
    void add_hoTenRong_nemValidationException() {
        assertThrows(ValidationException.class, () -> service.add(newStudent("SV2026003", "   ")));
    }

    @Test
    void add_chuaDu16Tuoi_nemValidationException() {
        Student s = newStudent("SV2026004", "Phạm Minh Đức");
        s.setDob(LocalDate.now().minusYears(15));
        assertThrows(ValidationException.class, () -> service.add(s));
    }

    @Test
    void add_soDienThoaiSai_nemValidationException() {
        Student s = newStudent("SV2026005", "Phạm Minh Đức");
        s.setPhone("1234567890");
        assertThrows(ValidationException.class, () -> service.add(s));
    }

    @Test
    void add_thieuGioiTinhHoacLop_nemValidationException() {
        Student s1 = newStudent("SV2026006", "Phạm Minh Đức");
        s1.setGender(null);
        assertThrows(ValidationException.class, () -> service.add(s1));

        Student s2 = newStudent("SV2026007", "Phạm Minh Đức");
        s2.setClassName("  ");
        assertThrows(ValidationException.class, () -> service.add(s2));
    }

    @Test
    void add_khoaKhongKhopNamTrongMa_nemValidationException() {
        Student s = newStudent("SV2026008", "Phạm Minh Đức");
        s.setIntakeYear(2025);

        ValidationException ex = assertThrows(ValidationException.class, () -> service.add(s));
        assertTrue(ex.getMessage().contains("BR-01"));
    }

    @Test
    void add_null_nemValidationException() {
        assertThrows(ValidationException.class, () -> service.add(null));
    }

    @Test
    void add_loiCSDL_bocThanhBusinessException() {
        dao.failNext = true;
        BusinessException ex = assertThrows(BusinessException.class,
                () -> service.add(newStudent("SV2026009", "Phạm Minh Đức")));
        assertTrue(ex.getCause() instanceof SQLException);
    }

    // ---------- update ----------

    @Test
    void update_hopLe_luuThayDoi() {
        Student s = newStudent("SV2026010", "Nguyễn Văn An");
        service.add(s);

        Student edit = copy(s);
        edit.setFullName("Nguyễn Văn An Bình");
        edit.setEmail("anbinh@example.com");
        edit.setStatus(StudentStatus.RESERVED);
        service.update(edit);

        Student saved = dao.byId.get(s.getId());
        assertEquals("Nguyễn Văn An Bình", saved.getFullName());
        assertEquals("anbinh@example.com", saved.getEmail());
        assertEquals(StudentStatus.RESERVED, saved.getStatus());
    }

    @Test
    void update_maVietThuongCuaChinhNo_vanHopLe() {
        Student s = newStudent("SV2026011", "Nguyễn Văn An");
        service.add(s);

        Student edit = copy(s);
        edit.setStudentCode(" sv2026011 ");
        service.update(edit);

        assertEquals("SV2026011", dao.byId.get(s.getId()).getStudentCode());
    }

    @Test
    void update_doiSangMaDaTonTai_nemBusinessException() {
        Student a = newStudent("SV2026012", "Nguyễn Văn An");
        Student b = newStudent("SV2026013", "Trần Văn Bình");
        service.add(a);
        service.add(b);

        Student edit = copy(b);
        edit.setStudentCode("SV2026012");

        BusinessException ex = assertThrows(BusinessException.class, () -> service.update(edit));
        assertTrue(ex.getMessage().contains("BR-01"));
        assertEquals("SV2026013", dao.byId.get(b.getId()).getStudentCode());
    }

    @Test
    void update_doiSangMaChuaTonTai_cungBiChan() {
        Student s = newStudent("SV2026014", "Nguyễn Văn An");
        service.add(s);

        Student edit = copy(s);
        edit.setStudentCode("SV2026099");

        assertThrows(BusinessException.class, () -> service.update(edit));
        assertEquals("SV2026014", dao.byId.get(s.getId()).getStudentCode());
    }

    @Test
    void update_emailSai_nemValidationException() {
        Student s = newStudent("SV2026015", "Nguyễn Văn An");
        service.add(s);

        Student edit = copy(s);
        edit.setEmail("khong-phai-email");
        assertThrows(ValidationException.class, () -> service.update(edit));
        assertNull(dao.byId.get(s.getId()).getEmail());
    }

    @Test
    void update_khongTimThayHoacChuaCoId_nemLoi() {
        Student ghost = newStudent("SV2026016", "Nguyễn Văn An");
        assertThrows(ValidationException.class, () -> service.update(ghost));

        ghost.setId(999);
        assertThrows(BusinessException.class, () -> service.update(ghost));
    }

    // ---------- delete (BR-14) ----------

    @Test
    void delete_sinhVienDaCoDiem_chuyenDroppedKhongXoa() {
        Student s = newStudent("SV2026017", "Nguyễn Văn An");
        service.add(s);
        dao.withGrades.add(s.getId());

        assertEquals(StudentService.DeleteResult.DROPPED, service.delete(s.getId()));

        Student saved = dao.byId.get(s.getId());
        assertNotNull(saved);
        assertEquals(StudentStatus.DROPPED, saved.getStatus());
    }

    @Test
    void delete_sinhVienDaDroppedVaCoDiem_vanGiuNguyen() {
        Student s = newStudent("SV2026018", "Nguyễn Văn An");
        s.setStatus(StudentStatus.DROPPED);
        service.add(s);
        dao.withGrades.add(s.getId());

        assertEquals(StudentService.DeleteResult.DROPPED, service.delete(s.getId()));
        assertEquals(StudentStatus.DROPPED, dao.byId.get(s.getId()).getStatus());
    }

    @Test
    void delete_sinhVienChuaCoDiem_xoaHan() {
        Student s = newStudent("SV2026019", "Nguyễn Văn An");
        service.add(s);

        assertEquals(StudentService.DeleteResult.DELETED, service.delete(s.getId()));
        assertNull(dao.byId.get(s.getId()));
    }

    @Test
    void delete_khongTimThay_nemBusinessException() {
        assertThrows(BusinessException.class, () -> service.delete(999));
    }

    // ---------- findAll / search ----------

    @Test
    void findAllVaSearch_goiSangDAO() {
        service.add(newStudent("SV2026021", "Nguyễn Văn An"));
        service.add(newStudent("SV2026020", "Trần Văn Bình"));

        List<Student> all = service.findAll();
        assertEquals(2, all.size());
        assertEquals("SV2026020", all.get(0).getStudentCode());

        List<Student> found = service.search("bình", null);
        assertEquals(1, found.size());
        assertEquals("SV2026020", found.get(0).getStudentCode());
    }

    // ---------- tiện ích ----------

    private static Student newStudent(String code, String name) {
        return new Student(code, name, LocalDate.of(2006, 5, 1), Gender.MALE,
                null, "0912345678", "CNTT-K26A", 2026, StudentStatus.STUDYING);
    }

    private static Student copy(Student s) {
        return new Student(s.getId(), s.getStudentCode(), s.getFullName(), s.getDob(), s.getGender(),
                s.getEmail(), s.getPhone(), s.getClassName(), s.getIntakeYear(), s.getStatus());
    }

    /** DAO giả: lưu sinh viên trong Map, mô phỏng ràng buộc UNIQUE của mã sinh viên. */
    private static class FakeStudentDAO implements StudentDAO {

        final Map<Integer, Student> byId = new LinkedHashMap<>();
        final Set<Integer> withGrades = new HashSet<>();
        boolean failNext;
        private int nextId = 1;

        private void maybeFail() throws SQLException {
            if (failNext) {
                failNext = false;
                throw new SQLException("mất kết nối", "08006");
            }
        }

        @Override
        public void insert(Student student) throws SQLException {
            maybeFail();
            for (Student s : byId.values()) {
                if (s.getStudentCode().equals(student.getStudentCode())) {
                    throw new SQLException("duplicate key value", "23505");
                }
            }
            student.setId(nextId++);
            byId.put(student.getId(), copy(student));
        }

        @Override
        public boolean update(Student student) throws SQLException {
            maybeFail();
            if (!byId.containsKey(student.getId())) {
                return false;
            }
            byId.put(student.getId(), copy(student));
            return true;
        }

        @Override
        public boolean delete(int id) throws SQLException {
            maybeFail();
            withGrades.remove(id);
            return byId.remove(id) != null;
        }

        @Override
        public Student findById(int id) throws SQLException {
            maybeFail();
            Student s = byId.get(id);
            return s == null ? null : copy(s);
        }

        @Override
        public Student findByCode(String studentCode) throws SQLException {
            maybeFail();
            if (studentCode == null) {
                return null;
            }
            String code = studentCode.trim().toUpperCase();
            for (Student s : byId.values()) {
                if (s.getStudentCode().equals(code)) {
                    return copy(s);
                }
            }
            return null;
        }

        @Override
        public List<Student> findAll() throws SQLException {
            maybeFail();
            List<Student> result = new ArrayList<>();
            for (Student s : byId.values()) {
                result.add(copy(s));
            }
            result.sort(Comparator.comparing(Student::getStudentCode));
            return result;
        }

        @Override
        public List<Student> search(String keyword, String className) throws SQLException {
            List<Student> result = new ArrayList<>();
            for (Student s : findAll()) {
                boolean matchKeyword = keyword == null || keyword.isBlank()
                        || s.getStudentCode().toLowerCase().contains(keyword.trim().toLowerCase())
                        || s.getFullName().toLowerCase().contains(keyword.trim().toLowerCase());
                boolean matchClass = className == null || className.isBlank()
                        || s.getClassName().equalsIgnoreCase(className.trim());
                if (matchKeyword && matchClass) {
                    result.add(s);
                }
            }
            return result;
        }

        @Override
        public boolean hasGrades(int studentId) throws SQLException {
            maybeFail();
            return withGrades.contains(studentId);
        }
    }
}
