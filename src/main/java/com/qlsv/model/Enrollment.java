package com.qlsv.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Sinh viên đăng ký một lớp học phần, kèm điểm; ứng với bảng enrollments.
 * Khóa ngoại student_id và section_id được biểu diễn bằng đối tượng {@link Student}
 * và {@link ClassSection}. Học phần và học kỳ lấy qua {@code getSection()}.
 *
 * <p>Các cột điểm là NUMERIC nên dùng {@link BigDecimal}; {@code null} nghĩa là chưa có điểm
 * (BR-08), không được đổi thành 0. Model chỉ giữ giá trị; total, letter và gpa4 do
 * GpaCalculator tính (BR-09, BR-10) và phải được ghi cùng lúc, cùng null khi chưa đủ 3 điểm
 * thành phần.
 */
public class Enrollment {

    private Integer id;
    private Student student;            // cột student_id
    private ClassSection section;       // cột section_id
    private LocalDateTime registeredAt; // cột registered_at (TIMESTAMP)
    private BigDecimal attendance;      // chuyên cần 0–10, BR-08, null nếu chưa có
    private BigDecimal midterm;         // giữa kỳ 0–10, BR-08, null nếu chưa có
    private BigDecimal finalExam;       // cuối kỳ 0–10, BR-08, null nếu chưa có
    private BigDecimal total;           // tổng kết, BR-09, null nếu chưa đủ 3 điểm
    private String letter;              // điểm chữ A…F, BR-10, null nếu chưa có tổng kết
    private BigDecimal gpa4;            // điểm hệ 4, BR-10, null nếu chưa có tổng kết

    public Enrollment() {
    }

    /** Đăng ký mới: thời điểm đăng ký là lúc tạo, chưa có điểm nào. */
    public Enrollment(Student student, ClassSection section) {
        this.student = student;
        this.section = section;
        this.registeredAt = LocalDateTime.now();
    }

    /** Dùng khi thêm mới (chưa có id). */
    public Enrollment(Student student, ClassSection section, LocalDateTime registeredAt,
                      BigDecimal attendance, BigDecimal midterm, BigDecimal finalExam,
                      BigDecimal total, String letter, BigDecimal gpa4) {
        this.student = student;
        this.section = section;
        this.registeredAt = registeredAt;
        this.attendance = attendance;
        this.midterm = midterm;
        this.finalExam = finalExam;
        this.total = total;
        this.letter = letter;
        this.gpa4 = gpa4;
    }

    /** Dùng khi đọc từ CSDL (đã có id). */
    public Enrollment(Integer id, Student student, ClassSection section,
                      LocalDateTime registeredAt, BigDecimal attendance, BigDecimal midterm,
                      BigDecimal finalExam, BigDecimal total, String letter, BigDecimal gpa4) {
        this(student, section, registeredAt, attendance, midterm, finalExam, total, letter, gpa4);
        this.id = id;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Student getStudent() { return student; }
    public void setStudent(Student student) { this.student = student; }

    public ClassSection getSection() { return section; }
    public void setSection(ClassSection section) { this.section = section; }

    public LocalDateTime getRegisteredAt() { return registeredAt; }
    public void setRegisteredAt(LocalDateTime registeredAt) { this.registeredAt = registeredAt; }

    public BigDecimal getAttendance() { return attendance; }
    public void setAttendance(BigDecimal attendance) { this.attendance = attendance; }

    public BigDecimal getMidterm() { return midterm; }
    public void setMidterm(BigDecimal midterm) { this.midterm = midterm; }

    public BigDecimal getFinalExam() { return finalExam; }
    public void setFinalExam(BigDecimal finalExam) { this.finalExam = finalExam; }

    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }

    public String getLetter() { return letter; }
    public void setLetter(String letter) { this.letter = letter; }

    public BigDecimal getGpa4() { return gpa4; }
    public void setGpa4(BigDecimal gpa4) { this.gpa4 = gpa4; }

    /** Chỉ in mã sinh viên, mã lớp học phần và điểm tổng kết (nếu có). */
    @Override
    public String toString() {
        String studentCode = student != null ? student.getStudentCode() : null;
        String sectionCode = section != null ? section.getSectionCode() : null;
        return studentCode + " - " + sectionCode + (total != null ? " (" + total + ")" : "");
    }
}
