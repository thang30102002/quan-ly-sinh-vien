package com.qlsv.view;

import com.qlsv.model.Gender;
import com.qlsv.model.Student;
import com.qlsv.model.StudentStatus;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

/**
 * Dữ liệu cho bảng danh sách sinh viên. Bảng chỉ để xem, sửa thông tin qua form.
 * <p>
 * Ngày sinh trả về {@link LocalDate} để sắp xếp đúng theo thời gian; giới tính và
 * trạng thái trả về enum, khi hiển thị ra nhãn tiếng Việt qua {@code toString()}.
 */
public class StudentTableModel extends AbstractTableModel {

    public static final int COL_CODE = 0;
    public static final int COL_FULL_NAME = 1;
    public static final int COL_DOB = 2;
    public static final int COL_GENDER = 3;
    public static final int COL_CLASS_NAME = 4;
    public static final int COL_INTAKE_YEAR = 5;
    public static final int COL_EMAIL = 6;
    public static final int COL_PHONE = 7;
    public static final int COL_STATUS = 8;

    private static final String[] COLUMN_NAMES = {
        "Mã SV", "Họ tên", "Ngày sinh", "Giới tính", "Lớp", "Khóa", "Email", "SĐT", "Trạng thái"
    };

    private static final Class<?>[] COLUMN_CLASSES = {
        String.class, String.class, LocalDate.class, Gender.class, String.class,
        Integer.class, String.class, String.class, StudentStatus.class
    };

    private final List<Student> students = new ArrayList<>();

    /** Thay toàn bộ dữ liệu của bảng. Danh sách truyền vào được sao chép lại. */
    public void setStudents(List<Student> list) {
        students.clear();
        if (list != null) {
            students.addAll(list);
        }
        fireTableDataChanged();
    }

    /**
     * @param modelRow chỉ số dòng trong model; nếu lấy từ JTable thì đổi bằng
     *                 {@code table.convertRowIndexToModel(viewRow)} trước
     */
    public Student getStudentAt(int modelRow) {
        return students.get(modelRow);
    }

    @Override
    public int getRowCount() {
        return students.size();
    }

    @Override
    public int getColumnCount() {
        return COLUMN_NAMES.length;
    }

    @Override
    public String getColumnName(int column) {
        return COLUMN_NAMES[column];
    }

    @Override
    public Class<?> getColumnClass(int column) {
        return COLUMN_CLASSES[column];
    }

    @Override
    public boolean isCellEditable(int row, int column) {
        return false;
    }

    @Override
    public Object getValueAt(int row, int column) {
        Student s = students.get(row);
        return switch (column) {
            case COL_CODE -> s.getStudentCode();
            case COL_FULL_NAME -> s.getFullName();
            case COL_DOB -> s.getDob();
            case COL_GENDER -> s.getGender();
            case COL_CLASS_NAME -> s.getClassName();
            case COL_INTAKE_YEAR -> s.getIntakeYear();
            case COL_EMAIL -> s.getEmail();
            case COL_PHONE -> s.getPhone();
            case COL_STATUS -> s.getStatus();
            default -> throw new IndexOutOfBoundsException("Không có cột " + column);
        };
    }
}
