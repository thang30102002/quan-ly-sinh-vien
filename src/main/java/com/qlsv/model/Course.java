package com.qlsv.model;

/**
 * Học phần, ứng với bảng courses.
 */
public class Course {

    private Integer id;
    private String courseCode;    // BR-02, vd IT101, MATH201
    private String name;          // tối đa 150 ký tự
    private int credits;          // BR-18, từ 1 đến 6
    private String description;   // có thể null

    public Course() {
    }

    /** Dùng khi thêm mới (chưa có id). */
    public Course(String courseCode, String name, int credits, String description) {
        this.courseCode = courseCode;
        this.name = name;
        this.credits = credits;
        this.description = description;
    }

    /** Dùng khi đọc từ CSDL (đã có id). */
    public Course(Integer id, String courseCode, String name, int credits, String description) {
        this(courseCode, name, credits, description);
        this.id = id;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getCourseCode() { return courseCode; }
    public void setCourseCode(String courseCode) { this.courseCode = courseCode; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getCredits() { return credits; }
    public void setCredits(int credits) { this.credits = credits; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    @Override
    public String toString() {
        return courseCode + " - " + name;
    }
}
