package com.qlsv.dao;

import com.qlsv.model.Student;

import java.sql.SQLException;
import java.util.List;

/**
 * Truy cập bảng students. Không kiểm tra quy tắc nghiệp vụ, việc đó thuộc Service.
 */
public interface StudentDAO {

    /** Thêm sinh viên mới và gán id vừa sinh vào {@code student}. */
    void insert(Student student) throws SQLException;

    /**
     * Cập nhật sinh viên theo {@code student.getId()}.
     *
     * @return true nếu có dòng được sửa, false nếu không tìm thấy id
     */
    boolean update(Student student) throws SQLException;

    /**
     * Xóa sinh viên theo id. CSDL xóa CASCADE các enrollments của sinh viên,
     * nên Service phải kiểm tra BR-14 trước khi gọi.
     *
     * @return true nếu có dòng bị xóa, false nếu không tìm thấy id
     */
    boolean delete(int id) throws SQLException;

    /** @return sinh viên có id này, hoặc null nếu không có */
    Student findById(int id) throws SQLException;

    /** @return sinh viên có mã này (vd SV2026001), hoặc null nếu không có */
    Student findByCode(String studentCode) throws SQLException;

    /** @return toàn bộ sinh viên, sắp theo mã */
    List<Student> findAll() throws SQLException;

    /**
     * Tìm sinh viên theo từ khóa (mã hoặc họ tên, có dấu) và lớp sinh hoạt.
     * Tham số null hoặc rỗng thì bỏ qua điều kiện đó.
     */
    List<Student> search(String keyword, String className) throws SQLException;
}
