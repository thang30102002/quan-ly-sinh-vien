package com.qlsv.model;

/**
 * Một buổi học lặp lại hằng tuần của lớp học phần, ứng với bảng schedules.
 * Khóa ngoại section_id được biểu diễn bằng đối tượng {@link ClassSection}.
 *
 * <p>Quy ước thứ giống CSDL, KHÔNG phải {@link java.time.DayOfWeek}:
 * 2 = Thứ 2 … 7 = Thứ 7, 8 = Chủ nhật.
 */
public class Schedule {

    private Integer id;
    private ClassSection section; // cột section_id
    private int dayOfWeek;        // BR-21, từ 2 (Thứ 2) đến 8 (Chủ nhật)
    private int startPeriod;      // BR-21, tiết bắt đầu 1–12
    private int endPeriod;        // BR-21, tiết kết thúc 1–12, không nhỏ hơn startPeriod
    private String room;          // phòng học, có thể null

    public Schedule() {
    }

    /** Dùng khi thêm mới (chưa có id). */
    public Schedule(ClassSection section, int dayOfWeek, int startPeriod, int endPeriod,
                    String room) {
        this.section = section;
        this.dayOfWeek = dayOfWeek;
        this.startPeriod = startPeriod;
        this.endPeriod = endPeriod;
        this.room = room;
    }

    /** Dùng khi đọc từ CSDL (đã có id). */
    public Schedule(Integer id, ClassSection section, int dayOfWeek, int startPeriod,
                    int endPeriod, String room) {
        this(section, dayOfWeek, startPeriod, endPeriod, room);
        this.id = id;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public ClassSection getSection() { return section; }
    public void setSection(ClassSection section) { this.section = section; }

    public int getDayOfWeek() { return dayOfWeek; }
    public void setDayOfWeek(int dayOfWeek) { this.dayOfWeek = dayOfWeek; }

    public int getStartPeriod() { return startPeriod; }
    public void setStartPeriod(int startPeriod) { this.startPeriod = startPeriod; }

    public int getEndPeriod() { return endPeriod; }
    public void setEndPeriod(int endPeriod) { this.endPeriod = endPeriod; }

    public String getRoom() { return room; }
    public void setRoom(String room) { this.room = room; }

    /** Dạng "Thứ 2, tiết 1–3, phòng A101"; không in lớp học phần. */
    @Override
    public String toString() {
        String day = dayOfWeek == 8 ? "Chủ nhật" : "Thứ " + dayOfWeek;
        String periods = startPeriod == endPeriod
                ? "tiết " + startPeriod
                : "tiết " + startPeriod + "–" + endPeriod;
        String roomText = room != null && !room.isBlank() ? ", phòng " + room : "";
        return day + ", " + periods + roomText;
    }
}
