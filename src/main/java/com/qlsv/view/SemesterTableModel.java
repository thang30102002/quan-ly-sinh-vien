package com.qlsv.view;

import com.qlsv.model.Semester;

import java.time.LocalDate;

/**
 * Dữ liệu cho bảng danh sách học kỳ. Ngày trả về {@link LocalDate} để sắp xếp đúng.
 */
public class SemesterTableModel extends ListTableModel<Semester> {

    public static final int COL_CODE = 0;
    public static final int COL_NAME = 1;
    public static final int COL_START = 2;
    public static final int COL_END = 3;
    public static final int COL_STATUS = 4;

    public static final String OPEN_LABEL = "Đang mở đăng ký";
    public static final String CLOSED_LABEL = "Đã đóng";

    public SemesterTableModel() {
        super(new String[] {"Mã HK", "Tên học kỳ", "Ngày bắt đầu", "Ngày kết thúc", "Trạng thái"},
              new Class<?>[] {String.class, String.class, LocalDate.class, LocalDate.class, String.class});
    }

    @Override
    protected Object valueAt(Semester s, int column) {
        return switch (column) {
            case COL_CODE -> s.getSemesterCode();
            case COL_NAME -> s.getName();
            case COL_START -> s.getStartDate();
            case COL_END -> s.getEndDate();
            case COL_STATUS -> s.isOpen() ? OPEN_LABEL : CLOSED_LABEL;
            default -> throw new IndexOutOfBoundsException("Không có cột " + column);
        };
    }
}
