package com.qlsv.dao;

import com.qlsv.model.Schedule;

import java.sql.SQLException;
import java.util.List;

/**
 * Truy cập bảng schedules. Các hàm đọc trả về buổi học có {@code getSection()} chỉ
 * mang id của lớp (không nạp học phần, học kỳ). Không kiểm tra quy tắc nghiệp vụ.
 */
public interface ScheduleDAO {

    /** Thêm buổi học mới và gán id vừa sinh vào {@code schedule}. */
    void insert(Schedule schedule) throws SQLException;

    /**
     * Cập nhật buổi học theo {@code schedule.getId()}.
     *
     * @return true nếu có dòng được sửa, false nếu không tìm thấy id
     */
    boolean update(Schedule schedule) throws SQLException;

    /** @return true nếu có dòng bị xóa, false nếu không tìm thấy id */
    boolean delete(int id) throws SQLException;

    /** @return buổi học có id này, hoặc null nếu không có */
    Schedule findById(int id) throws SQLException;

    /** @return các buổi học của lớp, sắp theo thứ rồi tiết bắt đầu */
    List<Schedule> findBySection(int sectionId) throws SQLException;
}
