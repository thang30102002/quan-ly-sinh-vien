package com.qlsv.view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.qlsv.model.Gender;
import com.qlsv.model.Student;
import com.qlsv.model.StudentStatus;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import javax.swing.event.TableModelEvent;

import org.junit.jupiter.api.Test;

/**
 * Không cần CSDL và không mở cửa sổ: chỉ kiểm tra dữ liệu bảng trả về.
 */
class StudentTableModelTest {

    private static Student sample() {
        return new Student(1, "SV2026001", "Nguyễn Văn An", LocalDate.of(2006, 5, 1), Gender.MALE,
                "an@example.com", "0912345678", "CNTT-K26A", 2026, StudentStatus.STUDYING);
    }

    @Test
    void bangMoi_khongCoDong() {
        assertEquals(0, new StudentTableModel().getRowCount());
    }

    @Test
    void coDuChinCotVoiTenTiengViet() {
        StudentTableModel model = new StudentTableModel();
        assertEquals(9, model.getColumnCount());
        assertEquals("Mã SV", model.getColumnName(StudentTableModel.COL_CODE));
        assertEquals("Họ tên", model.getColumnName(StudentTableModel.COL_FULL_NAME));
        assertEquals("Ngày sinh", model.getColumnName(StudentTableModel.COL_DOB));
        assertEquals("Giới tính", model.getColumnName(StudentTableModel.COL_GENDER));
        assertEquals("Lớp", model.getColumnName(StudentTableModel.COL_CLASS_NAME));
        assertEquals("Khóa", model.getColumnName(StudentTableModel.COL_INTAKE_YEAR));
        assertEquals("Email", model.getColumnName(StudentTableModel.COL_EMAIL));
        assertEquals("SĐT", model.getColumnName(StudentTableModel.COL_PHONE));
        assertEquals("Trạng thái", model.getColumnName(StudentTableModel.COL_STATUS));
    }

    @Test
    void getValueAt_traVeDungTungCot() {
        StudentTableModel model = new StudentTableModel();
        model.setStudents(List.of(sample()));

        assertEquals(1, model.getRowCount());
        assertEquals("SV2026001", model.getValueAt(0, StudentTableModel.COL_CODE));
        assertEquals("Nguyễn Văn An", model.getValueAt(0, StudentTableModel.COL_FULL_NAME));
        assertEquals(LocalDate.of(2006, 5, 1), model.getValueAt(0, StudentTableModel.COL_DOB));
        assertEquals(Gender.MALE, model.getValueAt(0, StudentTableModel.COL_GENDER));
        assertEquals("CNTT-K26A", model.getValueAt(0, StudentTableModel.COL_CLASS_NAME));
        assertEquals(2026, model.getValueAt(0, StudentTableModel.COL_INTAKE_YEAR));
        assertEquals("an@example.com", model.getValueAt(0, StudentTableModel.COL_EMAIL));
        assertEquals("0912345678", model.getValueAt(0, StudentTableModel.COL_PHONE));
        assertEquals(StudentStatus.STUDYING, model.getValueAt(0, StudentTableModel.COL_STATUS));
    }

    @Test
    void gioiTinhVaTrangThai_hienNhanTiengViet() {
        StudentTableModel model = new StudentTableModel();
        Student s = sample();
        s.setGender(Gender.FEMALE);
        s.setStatus(StudentStatus.DROPPED);
        model.setStudents(List.of(s));

        // JTable hiển thị giá trị bằng toString()
        assertEquals("Nữ", model.getValueAt(0, StudentTableModel.COL_GENDER).toString());
        assertEquals("Thôi học", model.getValueAt(0, StudentTableModel.COL_STATUS).toString());
    }

    @Test
    void emailVaSdtTrong_traVeNull() {
        StudentTableModel model = new StudentTableModel();
        Student s = sample();
        s.setEmail(null);
        s.setPhone(null);
        model.setStudents(List.of(s));

        assertNull(model.getValueAt(0, StudentTableModel.COL_EMAIL));
        assertNull(model.getValueAt(0, StudentTableModel.COL_PHONE));
    }

    @Test
    void kieuCot_dungDeSapXepDung() {
        StudentTableModel model = new StudentTableModel();
        assertEquals(LocalDate.class, model.getColumnClass(StudentTableModel.COL_DOB));
        assertEquals(Integer.class, model.getColumnClass(StudentTableModel.COL_INTAKE_YEAR));
        assertEquals(String.class, model.getColumnClass(StudentTableModel.COL_CODE));
    }

    @Test
    void khongChoSuaOTrongBang() {
        StudentTableModel model = new StudentTableModel();
        model.setStudents(List.of(sample()));
        for (int col = 0; col < model.getColumnCount(); col++) {
            assertFalse(model.isCellEditable(0, col), "Cột " + col + " không được sửa trực tiếp");
        }
    }

    @Test
    void setStudents_thayToanBoDuLieuVaBaoChoBang() {
        StudentTableModel model = new StudentTableModel();
        List<TableModelEvent> events = new ArrayList<>();
        model.addTableModelListener(events::add);

        model.setStudents(List.of(sample(), sample()));
        assertEquals(2, model.getRowCount());

        model.setStudents(List.of(sample()));
        assertEquals(1, model.getRowCount());
        assertEquals(2, events.size());
    }

    @Test
    void setStudents_null_xoaHetDong() {
        StudentTableModel model = new StudentTableModel();
        model.setStudents(List.of(sample()));
        model.setStudents(null);
        assertEquals(0, model.getRowCount());
    }

    @Test
    void setStudents_saoChepDanhSach_khongPhuThuocListGoc() {
        StudentTableModel model = new StudentTableModel();
        List<Student> source = new ArrayList<>(List.of(sample()));
        model.setStudents(source);

        source.clear();
        assertEquals(1, model.getRowCount());
    }

    @Test
    void getStudentAt_traVeDungDoiTuong() {
        StudentTableModel model = new StudentTableModel();
        Student s = sample();
        model.setStudents(List.of(s));
        assertSame(s, model.getStudentAt(0));
    }

    @Test
    void cotKhongTonTai_nemLoi() {
        StudentTableModel model = new StudentTableModel();
        model.setStudents(List.of(sample()));
        assertThrows(IndexOutOfBoundsException.class, () -> model.getValueAt(0, 9));
    }
}
