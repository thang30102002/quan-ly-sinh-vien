package com.qlsv.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.qlsv.dao.StudentDAO;
import com.qlsv.model.Gender;
import com.qlsv.model.Student;
import com.qlsv.model.StudentStatus;
import com.qlsv.service.StudentService;
import com.qlsv.testutil.RecordingDialogHandler;
import com.qlsv.view.StudentPanel;
import com.qlsv.view.StudentTableModel;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JOptionPane;
import javax.swing.JSpinner;
import javax.swing.SwingUtilities;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class StudentControllerTest {

    private static class MemoryStudentDAO implements StudentDAO {
        final Map<Integer, Student> data = new LinkedHashMap<>();
        final Set<Integer> withGrades = new HashSet<>();
        int nextId = 1;

        private Student copy(Student s) {
            return new Student(s.getId(), s.getStudentCode(), s.getFullName(), s.getDob(),
                    s.getGender(), s.getEmail(), s.getPhone(), s.getClassName(),
                    s.getIntakeYear(), s.getStatus());
        }

        @Override
        public void insert(Student student) throws SQLException {
            for (Student s : data.values()) {
                if (s.getStudentCode().equals(student.getStudentCode())) {
                    throw new SQLException("duplicate key", "23505");
                }
            }
            student.setId(nextId++);
            data.put(student.getId(), copy(student));
        }

        @Override
        public boolean update(Student student) throws SQLException {
            if (!data.containsKey(student.getId())) {
                return false;
            }
            data.put(student.getId(), copy(student));
            return true;
        }

        @Override
        public boolean delete(int id) throws SQLException {
            withGrades.remove(id);
            return data.remove(id) != null;
        }

        @Override
        public Student findById(int id) throws SQLException {
            Student s = data.get(id);
            return s == null ? null : copy(s);
        }

        @Override
        public Student findByCode(String studentCode) throws SQLException {
            if (studentCode == null) return null;
            String code = studentCode.trim().toUpperCase();
            for (Student s : data.values()) {
                if (s.getStudentCode().equals(code)) {
                    return copy(s);
                }
            }
            return null;
        }

        @Override
        public List<Student> findAll() throws SQLException {
            List<Student> list = new ArrayList<>();
            for (Student s : data.values()) {
                list.add(copy(s));
            }
            list.sort(Comparator.comparing(Student::getStudentCode));
            return list;
        }

        @Override
        public List<Student> search(String keyword, String className) throws SQLException {
            List<Student> list = new ArrayList<>();
            for (Student s : findAll()) {
                boolean matchKeyword = keyword == null || keyword.isBlank()
                        || s.getStudentCode().toLowerCase().contains(keyword.trim().toLowerCase())
                        || s.getFullName().toLowerCase().contains(keyword.trim().toLowerCase());
                boolean matchClass = className == null || className.isBlank()
                        || s.getClassName().equalsIgnoreCase(className.trim());
                if (matchKeyword && matchClass) {
                    list.add(s);
                }
            }
            return list;
        }

        @Override
        public boolean hasGrades(int studentId) throws SQLException {
            return withGrades.contains(studentId);
        }
    }

    private MemoryStudentDAO dao;
    private StudentService service;
    private StudentPanel panel;
    private StudentController controller;
    private RecordingDialogHandler dialog;

    @BeforeEach
    void setUp() throws SQLException {
        dao = new MemoryStudentDAO();
        service = new StudentService(dao);
        panel = new StudentPanel();
        controller = new StudentController(panel, service);
        dialog = new RecordingDialogHandler();
        controller.setDialogHandler(dialog);

        Student s1 = new Student("SV2026001", "Nguyễn Văn An", LocalDate.of(2006, 5, 1),
                Gender.MALE, "an@example.com", "0912345678", "CNTT-K26A", 2026, StudentStatus.STUDYING);
        Student s2 = new Student("SV2026002", "Trần Thị Bình", LocalDate.of(2006, 8, 15),
                Gender.FEMALE, "binh@example.com", "0987654321", "CNTT-K26B", 2026, StudentStatus.STUDYING);
        service.add(s1);
        service.add(s2);
        controller.getTableModel().setItems(service.findAll());
    }

    @AfterEach
    void khongHienMaBR() {
        dialog.assertNoRuleCodes();
    }

    @Test
    void khoiTao_bangCoHaiSinhVien() {
        assertEquals(2, controller.getTableModel().getRowCount());
        assertNull(controller.getSelectedStudentId());
    }

    @Test
    void onRowSelected_khiChonDong_dienDuLieuVaKhoaMaSV() {
        panel.getTblStudents().setRowSelectionInterval(0, 0);
        controller.onRowSelected();

        assertNotNull(controller.getSelectedStudentId());
        assertEquals("SV2026001", panel.getTxtStudentCode().getText());
        assertFalse(panel.getTxtStudentCode().isEditable());
        assertEquals("Nguyễn Văn An", panel.getTxtFullName().getText());
        assertEquals("01/05/2006", panel.getTxtDob().getText());
        assertEquals(Gender.MALE, panel.getCboGender().getSelectedItem());
        assertEquals("CNTT-K26A", panel.getTxtClassName().getText());
    }

    @Test
    void onAddStudent_thanhCong() {
        panel.getTxtStudentCode().setText("SV2026003");
        panel.getTxtFullName().setText("Lê Văn Cường");
        panel.getTxtDob().setText("20/10/2006");
        panel.getCboGender().setSelectedItem(Gender.MALE);
        panel.getTxtEmail().setText("cuong@example.com");
        panel.getTxtPhone().setText("0901234567");
        panel.getTxtClassName().setText("CNTT-K26A");
        panel.getSpnIntakeYear().setValue(2026);
        panel.getCboStatus().setSelectedItem(StudentStatus.STUDYING);

        controller.onAddStudent();

        assertEquals(JOptionPane.INFORMATION_MESSAGE, dialog.lastMessageType);
        assertEquals("Thêm sinh viên thành công!", dialog.lastMessage);
        assertNotNull(dao.data.get(3));
        assertEquals("SV2026003", dao.data.get(3).getStudentCode());
        assertEquals("", panel.getTxtStudentCode().getText());
        assertTrue(panel.getTxtStudentCode().isEditable());
    }

    @Test
    void onAddStudent_saiNgaySinh_baoLoiKhongCrash() {
        panel.getTxtStudentCode().setText("SV2026003");
        panel.getTxtFullName().setText("Lê Văn Cường");
        panel.getTxtDob().setText("sai-dinh-dang");
        panel.getSpnIntakeYear().setValue(2026);
        panel.getTxtClassName().setText("CNTT-K26A");

        controller.onAddStudent();

        assertEquals(JOptionPane.ERROR_MESSAGE, dialog.lastMessageType);
        assertTrue(dialog.lastMessage.contains("Ngày sinh không đúng định dạng"));
    }

    @Test
    void onAddStudent_trungMaSV_baoLoi() {
        panel.getTxtStudentCode().setText("SV2026001");
        panel.getTxtFullName().setText("Người Khác");
        panel.getTxtDob().setText("01/01/2006");
        panel.getSpnIntakeYear().setValue(2026);
        panel.getTxtClassName().setText("CNTT-K26A");

        controller.onAddStudent();

        assertEquals(JOptionPane.ERROR_MESSAGE, dialog.lastMessageType);
        assertTrue(dialog.lastMessage.contains("đã tồn tại"));
    }

    /**
     * Bug B1: gõ năm vào ô Khóa rồi bấm Thêm ngay (chưa nhấn Enter) thì giá trị vừa gõ bị
     * bỏ qua, mã SV2025xxx bị báo không khớp khóa dù người dùng đã gõ 2025.
     */
    @Test
    void onAddStudent_khoaVuaGoChuaNhanEnter_vanLayGiaTriVuaGo() {
        panel.getTxtStudentCode().setText("SV2025003");
        panel.getTxtFullName().setText("Lê Văn Cường");
        panel.getTxtDob().setText("20/10/2006");
        panel.getTxtClassName().setText("CNTT-K25A");
        panel.getSpnIntakeYear().setValue(2026);
        ((JSpinner.DefaultEditor) panel.getSpnIntakeYear().getEditor()).getTextField().setText("2025");

        controller.onAddStudent();

        assertEquals("Thêm sinh viên thành công!", dialog.lastMessage);
        assertEquals(2025, dao.data.get(3).getIntakeYear());
    }

    @Test
    void onAddStudent_khoaKhongPhaiSo_baoLoi() {
        panel.getTxtStudentCode().setText("SV2026003");
        panel.getTxtFullName().setText("Lê Văn Cường");
        panel.getTxtDob().setText("20/10/2006");
        panel.getTxtClassName().setText("CNTT-K26A");
        ((JSpinner.DefaultEditor) panel.getSpnIntakeYear().getEditor()).getTextField().setText("abc");

        controller.onAddStudent();

        assertEquals(JOptionPane.ERROR_MESSAGE, dialog.lastMessageType);
        assertTrue(dialog.lastMessage.contains("Khóa"));
        assertNull(dao.data.get(3));
    }

    /** Bug B2: sửa xong bảng tải lại làm mất dòng đang chọn, bấm Sửa lần hai bị báo "chưa chọn". */
    @Test
    void onUpdateStudent_thanhCong_vanChonDongVuaSua_suaTiepDuoc() throws Exception {
        panel.getTblStudents().setRowSelectionInterval(0, 0);
        controller.onRowSelected();
        panel.getTxtFullName().setText("Nguyễn Văn An Cập Nhật");

        controller.onUpdateStudent();
        waitForLoad();

        assertEquals("Cập nhật sinh viên thành công!", dialog.lastMessage);
        assertEquals(1, controller.getSelectedStudentId());
        Student selected = controller.getTableModel().getSelectedItem(panel.getTblStudents());
        assertNotNull(selected);
        assertEquals("Nguyễn Văn An Cập Nhật", selected.getFullName());

        panel.getTxtEmail().setText("an_new@example.com");
        controller.onUpdateStudent();
        waitForLoad();

        assertEquals("Cập nhật sinh viên thành công!", dialog.lastMessage);
        assertEquals("an_new@example.com", dao.data.get(1).getEmail());
    }

    /** Chờ SwingWorker của loadStudents tải xong (nút Làm mới bật lại trong done()). */
    private void waitForLoad() throws Exception {
        long deadline = System.currentTimeMillis() + 5000;
        do {
            SwingUtilities.invokeAndWait(() -> { });
            if (panel.getBtnRefresh().isEnabled()) {
                return;
            }
            Thread.sleep(5);
        } while (System.currentTimeMillis() < deadline);
        throw new AssertionError("Quá 5 giây vẫn chưa tải xong danh sách sinh viên");
    }

    @Test
    void onUpdateStudent_khiChuaChon_canhBao() {
        controller.setSelectedStudentId(null);
        controller.onUpdateStudent();

        assertEquals(JOptionPane.WARNING_MESSAGE, dialog.lastMessageType);
        assertEquals("Vui lòng chọn một sinh viên từ bảng để sửa!", dialog.lastMessage);
    }

    @Test
    void onUpdateStudent_hopLe_thanhCong() {
        panel.getTblStudents().setRowSelectionInterval(0, 0);
        controller.onRowSelected();

        panel.getTxtFullName().setText("Nguyễn Văn An Cập Nhật");
        panel.getTxtEmail().setText("an_new@example.com");

        controller.onUpdateStudent();

        assertEquals(JOptionPane.INFORMATION_MESSAGE, dialog.lastMessageType);
        assertEquals("Cập nhật sinh viên thành công!", dialog.lastMessage);
        assertEquals("Nguyễn Văn An Cập Nhật", dao.data.get(1).getFullName());
        assertEquals("an_new@example.com", dao.data.get(1).getEmail());
    }

    @Test
    void onDeleteStudent_khiChuaChon_canhBao() {
        panel.getTblStudents().clearSelection();
        controller.setSelectedStudentId(null);
        controller.onDeleteStudent();

        assertEquals(JOptionPane.WARNING_MESSAGE, dialog.lastMessageType);
        assertEquals("Vui lòng chọn một sinh viên từ bảng để xóa!", dialog.lastMessage);
    }

    @Test
    void onDeleteStudent_tuChoiXacNhan_khongXoa() {
        panel.getTblStudents().setRowSelectionInterval(0, 0);
        controller.onRowSelected();
        dialog.confirmResponse = JOptionPane.NO_OPTION;

        controller.onDeleteStudent();

        assertNotNull(dao.data.get(1));
    }

    @Test
    void onDeleteStudent_xacNhan_chuaCoDiem_xoaHan() {
        panel.getTblStudents().setRowSelectionInterval(0, 0);
        controller.onRowSelected();
        dialog.confirmResponse = JOptionPane.YES_OPTION;

        controller.onDeleteStudent();

        assertEquals(JOptionPane.INFORMATION_MESSAGE, dialog.lastMessageType);
        assertEquals("Đã xóa sinh viên thành công!", dialog.lastMessage);
        assertNull(dao.data.get(1));
        assertNull(controller.getSelectedStudentId());
        assertEquals("", panel.getTxtStudentCode().getText());
    }

    @Test
    void onDeleteStudent_daCoDiem_chuyenThoiHoc() {
        dao.withGrades.add(1);
        panel.getTblStudents().setRowSelectionInterval(0, 0);
        controller.onRowSelected();
        dialog.confirmResponse = JOptionPane.YES_OPTION;

        controller.onDeleteStudent();

        assertEquals(JOptionPane.INFORMATION_MESSAGE, dialog.lastMessageType);
        assertTrue(dialog.lastMessage.contains("Thôi học"));
        assertEquals("Sinh viên đã có điểm nên không thể xóa hẳn."
                + " Trạng thái đã được chuyển sang \"Thôi học\".", dialog.lastMessage);
        assertNotNull(dao.data.get(1));
        assertEquals(StudentStatus.DROPPED, dao.data.get(1).getStatus());
    }

    @Test
    void onRefresh_xoaFormVaMoKhoaMaSV() {
        panel.getTblStudents().setRowSelectionInterval(0, 0);
        controller.onRowSelected();
        panel.getTxtSearch().setText("An");

        controller.onRefresh();

        assertEquals("", panel.getTxtStudentCode().getText());
        assertTrue(panel.getTxtStudentCode().isEditable());
        assertEquals("", panel.getTxtSearch().getText());
        assertNull(controller.getSelectedStudentId());
    }

    @Test
    void onSearch_theoTuKhoa_locDung() {
        panel.getTxtSearch().setText("Bình");
        controller.onSearch();

        assertEquals(1, controller.getTableModel().getRowCount());
        assertEquals("SV2026002", controller.getTableModel().getValueAt(0, StudentTableModel.COL_CODE));
    }

    @Test
    void onSearch_theoLop_locDung() {
        panel.getCboClassFilter().addItem("CNTT-K26A");
        panel.getCboClassFilter().setSelectedItem("CNTT-K26A");
        controller.onSearch();

        assertEquals(1, controller.getTableModel().getRowCount());
        assertEquals("SV2026001", controller.getTableModel().getValueAt(0, StudentTableModel.COL_CODE));
    }

    @Test
    void onSearch_tatCaLop_traVeDu() {
        panel.getCboClassFilter().setSelectedItem("Tất cả các lớp");
        panel.getTxtSearch().setText("");
        controller.onSearch();

        assertEquals(2, controller.getTableModel().getRowCount());
    }
}
