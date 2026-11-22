package com.qlsv.view;

import com.qlsv.model.Schedule;

/**
 * Dữ liệu cho bảng buổi học của lớp học phần đang chọn. Cột thứ hiển thị nhãn tiếng Việt.
 */
public class ScheduleTableModel extends ListTableModel<Schedule> {

    public static final int COL_DAY = 0;
    public static final int COL_START = 1;
    public static final int COL_END = 2;
    public static final int COL_ROOM = 3;

    public ScheduleTableModel() {
        super(new String[] {"Thứ", "Tiết bắt đầu", "Tiết kết thúc", "Phòng"},
              new Class<?>[] {String.class, Integer.class, Integer.class, String.class});
    }

    @Override
    protected Object valueAt(Schedule s, int column) {
        return switch (column) {
            case COL_DAY -> Schedule.dayLabel(s.getDayOfWeek());
            case COL_START -> s.getStartPeriod();
            case COL_END -> s.getEndPeriod();
            case COL_ROOM -> s.getRoom();
            default -> throw new IndexOutOfBoundsException("Không có cột " + column);
        };
    }
}
