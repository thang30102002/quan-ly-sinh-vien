package com.qlsv.model;

import java.time.LocalDate;

/**
 * Học kỳ, ứng với bảng semesters.
 */
public class Semester {

    private Integer id;
    private String semesterCode;  // BR-03, vd 2026-1 (3 là học kỳ hè)
    private String name;          // tối đa 100 ký tự
    private LocalDate startDate;
    private LocalDate endDate;    // BR-19, phải sau startDate
    private boolean open;         // cột is_open: đang mở đăng ký (BR-26), mặc định đóng

    public Semester() {
    }

    /** Dùng khi thêm mới (chưa có id). */
    public Semester(String semesterCode, String name, LocalDate startDate, LocalDate endDate,
                    boolean open) {
        this.semesterCode = semesterCode;
        this.name = name;
        this.startDate = startDate;
        this.endDate = endDate;
        this.open = open;
    }

    /** Dùng khi đọc từ CSDL (đã có id). */
    public Semester(Integer id, String semesterCode, String name, LocalDate startDate,
                    LocalDate endDate, boolean open) {
        this(semesterCode, name, startDate, endDate, open);
        this.id = id;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getSemesterCode() { return semesterCode; }
    public void setSemesterCode(String semesterCode) { this.semesterCode = semesterCode; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public boolean isOpen() { return open; }
    public void setOpen(boolean open) { this.open = open; }

    @Override
    public String toString() {
        return semesterCode + " - " + name;
    }
}
