package com.qlsv.dao;

import com.qlsv.model.Semester;

import java.sql.SQLException;
import java.util.List;

/**
 * Truy cập bảng semesters. Không kiểm tra quy tắc nghiệp vụ, việc đó thuộc Service.
 */
public interface SemesterDAO {

    /** Thêm học kỳ mới và gán id vừa sinh vào {@code semester}. */
    void insert(Semester semester) throws SQLException;

    /**
     * Cập nhật học kỳ theo {@code semester.getId()}.
     *
     * @return true nếu có dòng được sửa, false nếu không tìm thấy id
     */
    boolean update(Semester semester) throws SQLException;

    /**
     * Xóa học kỳ theo id. CSDL chặn (RESTRICT) nếu học kỳ đã mở lớp,
     * nên Service phải kiểm tra BR-16 trước khi gọi.
     *
     * @return true nếu có dòng bị xóa, false nếu không tìm thấy id
     */
    boolean delete(int id) throws SQLException;

    /** @return học kỳ có id này, hoặc null nếu không có */
    Semester findById(int id) throws SQLException;

    /** @return học kỳ có mã này (vd 2026-1), hoặc null nếu không có */
    Semester findByCode(String semesterCode) throws SQLException;

    /** @return toàn bộ học kỳ, mới nhất trước (theo ngày bắt đầu giảm dần) */
    List<Semester> findAll() throws SQLException;

    /** BR-26: @return học kỳ đang mở đăng ký, hoặc null nếu không có */
    Semester findOpen() throws SQLException;

    /** Học kỳ đã có ít nhất một lớp học phần hay chưa. */
    boolean hasSections(int semesterId) throws SQLException;

    /** BR-16: có sinh viên nào đăng ký một lớp bất kỳ trong học kỳ này hay chưa. */
    boolean hasEnrollments(int semesterId) throws SQLException;
}
