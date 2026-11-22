package com.qlsv.dao;

import com.qlsv.model.ClassSection;

import java.sql.SQLException;
import java.util.List;

/**
 * Truy cập bảng class_sections. Các hàm đọc trả về lớp kèm đầy đủ {@code Course}
 * và {@code Semester}. Không kiểm tra quy tắc nghiệp vụ, việc đó thuộc Service.
 */
public interface ClassSectionDAO {

    /** Thêm lớp học phần mới và gán id vừa sinh vào {@code section}. */
    void insert(ClassSection section) throws SQLException;

    /**
     * Cập nhật lớp học phần theo {@code section.getId()}.
     *
     * @return true nếu có dòng được sửa, false nếu không tìm thấy id
     */
    boolean update(ClassSection section) throws SQLException;

    /**
     * Xóa lớp học phần theo id; các buổi học bị xóa theo (CASCADE). CSDL chặn
     * (RESTRICT) nếu lớp đã có sinh viên đăng ký, nên Service phải kiểm tra BR-16 trước.
     *
     * @return true nếu có dòng bị xóa, false nếu không tìm thấy id
     */
    boolean delete(int id) throws SQLException;

    /** @return lớp học phần có id này, hoặc null nếu không có */
    ClassSection findById(int id) throws SQLException;

    /** BR-24: @return lớp có mã này trong học kỳ, hoặc null nếu không có */
    ClassSection findByCode(int semesterId, String sectionCode) throws SQLException;

    /** @return các lớp học phần mở trong học kỳ, sắp theo mã lớp */
    List<ClassSection> findBySemester(int semesterId) throws SQLException;

    /** BR-16, BR-25: số sinh viên đã đăng ký lớp. */
    int countEnrollments(int sectionId) throws SQLException;
}
