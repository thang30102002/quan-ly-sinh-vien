package com.qlsv.dao;

import com.qlsv.model.Course;
import com.qlsv.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;

/**
 * Cài đặt {@link CourseDAO} bằng JDBC.
 */
public class CourseDAOImpl implements CourseDAO {

    private static final String INSERT_SQL =
            "INSERT INTO courses (course_code, name, credits, description) VALUES (?, ?, ?, ?)";

    private static final String UPDATE_SQL =
            "UPDATE courses SET course_code = ?, name = ?, credits = ?, description = ? WHERE id = ?";

    private static final String DELETE_SQL = "DELETE FROM courses WHERE id = ?";

    private static final String SELECT_SQL =
            "SELECT id, course_code, name, credits, description FROM courses";

    private static final String ORDER_BY_CODE = " ORDER BY course_code";

    private static final String HAS_SECTIONS_SQL =
            "SELECT EXISTS (SELECT 1 FROM class_sections WHERE course_id = ?)";

    private static final String HAS_ENROLLMENTS_SQL =
            "SELECT EXISTS (SELECT 1 FROM enrollments e"
            + " JOIN class_sections cs ON cs.id = e.section_id WHERE cs.course_id = ?)";

    @Override
    public void insert(Course course) throws SQLException {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {
            setFields(ps, course);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    course.setId(keys.getInt("id"));
                }
            }
        }
    }

    @Override
    public boolean update(Course course) throws SQLException {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(UPDATE_SQL)) {
            setFields(ps, course);
            ps.setInt(5, course.getId());
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean delete(int id) throws SQLException {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(DELETE_SQL)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public Course findById(int id) throws SQLException {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_SQL + " WHERE id = ?")) {
            ps.setInt(1, id);
            return queryOne(ps);
        }
    }

    @Override
    public Course findByCode(String courseCode) throws SQLException {
        if (courseCode == null || courseCode.isBlank()) {
            return null;
        }
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_SQL + " WHERE course_code = ?")) {
            // Mã lưu dạng in hoa (BR-02, CHECK phân biệt hoa thường)
            ps.setString(1, courseCode.trim().toUpperCase());
            return queryOne(ps);
        }
    }

    @Override
    public List<Course> findAll() throws SQLException {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_SQL + ORDER_BY_CODE)) {
            return queryList(ps);
        }
    }

    @Override
    public List<Course> search(String keyword) throws SQLException {
        if (keyword == null || keyword.isBlank()) {
            return findAll();
        }
        String sql = SELECT_SQL
                + " WHERE course_code ILIKE ? ESCAPE '\\' OR name ILIKE ? ESCAPE '\\'" + ORDER_BY_CODE;
        String pattern = "%" + escapeLike(Normalizer.normalize(keyword.trim(), Normalizer.Form.NFC)) + "%";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, pattern);
            ps.setString(2, pattern);
            return queryList(ps);
        }
    }

    @Override
    public boolean hasSections(int courseId) throws SQLException {
        return exists(HAS_SECTIONS_SQL, courseId);
    }

    @Override
    public boolean hasEnrollments(int courseId) throws SQLException {
        return exists(HAS_ENROLLMENTS_SQL, courseId);
    }

    /** Chuyển dòng hiện tại của ResultSet thành Course; không gọi {@code rs.next()}. */
    Course mapRow(ResultSet rs) throws SQLException {
        return new Course(
                rs.getInt("id"),
                rs.getString("course_code"),
                rs.getString("name"),
                rs.getInt("credits"),
                rs.getString("description"));
    }

    private boolean exists(String sql, int id) throws SQLException {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getBoolean(1);
            }
        }
    }

    private Course queryOne(PreparedStatement ps) throws SQLException {
        try (ResultSet rs = ps.executeQuery()) {
            return rs.next() ? mapRow(rs) : null;
        }
    }

    private List<Course> queryList(PreparedStatement ps) throws SQLException {
        List<Course> result = new ArrayList<>();
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                result.add(mapRow(rs));
            }
        }
        return result;
    }

    /** Thoát ký tự đặc biệt của LIKE để từ khóa được hiểu theo nghĩa đen. */
    private static String escapeLike(String value) {
        return value.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }

    /** Gán 4 tham số đầu, dùng chung cho INSERT và UPDATE. */
    private void setFields(PreparedStatement ps, Course course) throws SQLException {
        ps.setString(1, course.getCourseCode());
        ps.setString(2, course.getName());
        ps.setInt(3, course.getCredits());
        ps.setString(4, course.getDescription());
    }
}
