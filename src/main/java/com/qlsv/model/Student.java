package com.qlsv.model;

import java.time.LocalDate;

/**
 * Sinh viên, ứng với bảng students.
 */
public class Student {

    private Integer id;
    private String studentCode;   // BR-01, vd SV2026001
    private String fullName;      // BR-17, tối đa 100 ký tự
    private LocalDate dob;        // BR-17, đủ 16 tuổi
    private Gender gender;
    private String email;         // có thể null
    private String phone;         // có thể null
    private String className;     // lớp sinh hoạt, vd CNTT-K24A
    private int intakeYear;       // khóa (năm nhập học)
    private StudentStatus status = StudentStatus.STUDYING;

    public Student() {
    }

    /** Dùng khi thêm mới (chưa có id). */
    public Student(String studentCode, String fullName, LocalDate dob, Gender gender,
                   String email, String phone, String className, int intakeYear,
                   StudentStatus status) {
        this.studentCode = studentCode;
        this.fullName = fullName;
        this.dob = dob;
        this.gender = gender;
        this.email = email;
        this.phone = phone;
        this.className = className;
        this.intakeYear = intakeYear;
        this.status = status != null ? status : StudentStatus.STUDYING;
    }

    /** Dùng khi đọc từ CSDL (đã có id). */
    public Student(Integer id, String studentCode, String fullName, LocalDate dob, Gender gender,
                   String email, String phone, String className, int intakeYear,
                   StudentStatus status) {
        this(studentCode, fullName, dob, gender, email, phone, className, intakeYear, status);
        this.id = id;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getStudentCode() { return studentCode; }
    public void setStudentCode(String studentCode) { this.studentCode = studentCode; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public LocalDate getDob() { return dob; }
    public void setDob(LocalDate dob) { this.dob = dob; }

    public Gender getGender() { return gender; }
    public void setGender(Gender gender) { this.gender = gender; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }

    public int getIntakeYear() { return intakeYear; }
    public void setIntakeYear(int intakeYear) { this.intakeYear = intakeYear; }

    public StudentStatus getStatus() { return status; }
    public void setStatus(StudentStatus status) { this.status = status; }

    @Override
    public String toString() {
        return studentCode + " - " + fullName;
    }
}
