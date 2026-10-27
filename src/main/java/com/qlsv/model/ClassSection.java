package com.qlsv.model;

/**
 * Lớp học phần: một học phần mở trong một học kỳ, ứng với bảng class_sections.
 * Khóa ngoại course_id và semester_id được biểu diễn bằng đối tượng {@link Course}
 * và {@link Semester}.
 */
public class ClassSection {

    private Integer id;
    private String sectionCode;   // BR-24, vd IT101-01, duy nhất trong một học kỳ
    private Course course;        // cột course_id
    private Semester semester;    // cột semester_id
    private String lecturerName;  // giảng viên phụ trách, có thể null
    private int maxStudents;      // BR-25, sĩ số tối đa, lớn hơn 0

    public ClassSection() {
    }

    /** Dùng khi thêm mới (chưa có id). */
    public ClassSection(String sectionCode, Course course, Semester semester,
                        String lecturerName, int maxStudents) {
        this.sectionCode = sectionCode;
        this.course = course;
        this.semester = semester;
        this.lecturerName = lecturerName;
        this.maxStudents = maxStudents;
    }

    /** Dùng khi đọc từ CSDL (đã có id). */
    public ClassSection(Integer id, String sectionCode, Course course, Semester semester,
                        String lecturerName, int maxStudents) {
        this(sectionCode, course, semester, lecturerName, maxStudents);
        this.id = id;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getSectionCode() { return sectionCode; }
    public void setSectionCode(String sectionCode) { this.sectionCode = sectionCode; }

    public Course getCourse() { return course; }
    public void setCourse(Course course) { this.course = course; }

    public Semester getSemester() { return semester; }
    public void setSemester(Semester semester) { this.semester = semester; }

    public String getLecturerName() { return lecturerName; }
    public void setLecturerName(String lecturerName) { this.lecturerName = lecturerName; }

    public int getMaxStudents() { return maxStudents; }
    public void setMaxStudents(int maxStudents) { this.maxStudents = maxStudents; }

    /** Chỉ in mã lớp và tên học phần, không in cả cây đối tượng. */
    @Override
    public String toString() {
        return course != null ? sectionCode + " - " + course.getName() : String.valueOf(sectionCode);
    }
}
