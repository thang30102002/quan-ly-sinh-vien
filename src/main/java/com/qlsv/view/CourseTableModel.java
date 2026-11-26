package com.qlsv.view;

import com.qlsv.model.Course;

/**
 * Dữ liệu cho bảng danh sách học phần.
 */
public class CourseTableModel extends ListTableModel<Course> {

    public static final int COL_CODE = 0;
    public static final int COL_NAME = 1;
    public static final int COL_CREDITS = 2;
    public static final int COL_DESCRIPTION = 3;

    public CourseTableModel() {
        super(new String[] {"Mã HP", "Tên học phần", "Tín chỉ", "Mô tả"},
              new Class<?>[] {String.class, String.class, Integer.class, String.class});
    }

    @Override
    protected Object valueAt(Course c, int column) {
        return switch (column) {
            case COL_CODE -> c.getCourseCode();
            case COL_NAME -> c.getName();
            case COL_CREDITS -> c.getCredits();
            case COL_DESCRIPTION -> c.getDescription();
            default -> throw new IndexOutOfBoundsException("Không có cột " + column);
        };
    }
}
