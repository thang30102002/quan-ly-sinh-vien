package com.qlsv.view;

import com.qlsv.model.ClassSection;

/**
 * Dữ liệu cho bảng lớp học phần của một học kỳ.
 */
public class ClassSectionTableModel extends ListTableModel<ClassSection> {

    public static final int COL_CODE = 0;
    public static final int COL_COURSE_NAME = 1;
    public static final int COL_CREDITS = 2;
    public static final int COL_LECTURER = 3;
    public static final int COL_MAX_STUDENTS = 4;

    public ClassSectionTableModel() {
        super(new String[] {"Mã lớp", "Học phần", "Tín chỉ", "Giảng viên", "Sĩ số tối đa"},
              new Class<?>[] {String.class, String.class, Integer.class, String.class, Integer.class});
    }

    @Override
    protected Object valueAt(ClassSection s, int column) {
        return switch (column) {
            case COL_CODE -> s.getSectionCode();
            case COL_COURSE_NAME -> s.getCourse() != null ? s.getCourse().getName() : null;
            case COL_CREDITS -> s.getCourse() != null ? s.getCourse().getCredits() : null;
            case COL_LECTURER -> s.getLecturerName();
            case COL_MAX_STUDENTS -> s.getMaxStudents();
            default -> throw new IndexOutOfBoundsException("Không có cột " + column);
        };
    }
}
