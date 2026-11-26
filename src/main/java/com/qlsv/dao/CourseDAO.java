package com.qlsv.dao;

import com.qlsv.model.Course;

import java.sql.SQLException;
import java.util.List;

/**
 * Truy cập bảng courses. Không kiểm tra quy tắc nghiệp vụ, việc đó thuộc Service.
 */
public interface CourseDAO {

    /** Thêm học phần mới và gán id vừa sinh vào {@code course}. */
    void insert(Course course) throws SQLException;

    /**
     * Cập nhật học phần theo {@code course.getId()}.
     *
     * @return true nếu có dòng được sửa, false nếu không tìm thấy id
     */
    boolean update(Course course) throws SQLException;

    /**
     * Xóa học phần theo id. CSDL chặn (RESTRICT) nếu học phần đã mở lớp,
     * nên Service phải kiểm tra BR-16 trước khi gọi.
     *
     * @return true nếu có dòng bị xóa, false nếu không tìm thấy id
     */
    boolean delete(int id) throws SQLException;

    /** @return học phần có id này, hoặc null nếu không có */
    Course findById(int id) throws SQLException;

    /** @return học phần có mã này (vd IT101), hoặc null nếu không có */
    Course findByCode(String courseCode) throws SQLException;

    /** @return toàn bộ học phần, sắp theo mã */
    List<Course> findAll() throws SQLException;

    /**
     * Tìm học phần theo mã hoặc tên (khớp một phần, không phân biệt hoa thường).
     * Từ khóa null hoặc rỗng thì trả về toàn bộ.
     */
    List<Course> search(String keyword) throws SQLException;

    /** Học phần đã được mở ít nhất một lớp học phần hay chưa. */
    boolean hasSections(int courseId) throws SQLException;

    /** BR-16: có sinh viên nào đăng ký một lớp bất kỳ của học phần này hay chưa. */
    boolean hasEnrollments(int courseId) throws SQLException;
}
