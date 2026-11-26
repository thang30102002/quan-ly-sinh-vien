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

    /**
     * BR-22: hai buổi trùng nhau khi cùng thứ và khoảng tiết giao nhau
     * ({@code startA ≤ endB && startB ≤ endA}). Hai buổi chỉ kề nhau (tiết 1–3 và 4–6)
     * thì không trùng. Không so sánh lớp học phần; dùng cho BR-21 (các buổi trong một lớp)
     * và BR-22 (lịch của sinh viên, #11).
     *
     * @return false nếu {@code other} là null
     */
    public boolean overlaps(Schedule other) {
        if (other == null) {
            return false;
        }
        return dayOfWeek == other.dayOfWeek
                && startPeriod <= other.endPeriod
                && other.startPeriod <= endPeriod;
    }

    /** Nhãn tiếng Việt của thứ theo quy ước CSDL: 2 → "Thứ 2", 8 → "Chủ nhật". */
    public static String dayLabel(int dayOfWeek) {
        return dayOfWeek == 8 ? "Chủ nhật" : "Thứ " + dayOfWeek;
    }

    /** Dạng "Thứ 2, tiết 1–3, phòng A101"; không in lớp học phần. */
    @Override
    public String toString() {
        String day = dayLabel(dayOfWeek);
        String periods = startPeriod == endPeriod
                ? "tiết " + startPeriod
                : "tiết " + startPeriod + "–" + endPeriod;
        String roomText = room != null && !room.isBlank() ? ", phòng " + room : "";
        return day + ", " + periods + roomText;
    }
}
