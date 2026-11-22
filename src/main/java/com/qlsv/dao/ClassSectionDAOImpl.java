package com.qlsv.dao;

import com.qlsv.model.ClassSection;
import com.qlsv.model.Course;
import com.qlsv.model.Semester;
import com.qlsv.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Cài đặt {@link ClassSectionDAO} bằng JDBC.
 */
public class ClassSectionDAOImpl implements ClassSectionDAO {

    private static final String INSERT_SQL =
            "INSERT INTO class_sections (section_code, course_id, semester_id, lecturer_name,"
            + " max_students) VALUES (?, ?, ?, ?, ?)";

    private static final String UPDATE_SQL =
            "UPDATE class_sections SET section_code = ?, course_id = ?, semester_id = ?,"
            + " lecturer_name = ?, max_students = ? WHERE id = ?";

    private static final String DELETE_SQL = "DELETE FROM class_sections WHERE id = ?";

    /** Nối học phần và học kỳ để dựng đủ đối tượng; đặt bí danh cho các cột trùng tên. */
    private static final String SELECT_SQL =
            "SELECT cs.id, cs.section_code, cs.lecturer_name, cs.max_students,"
            + " c.id AS course_id, c.course_code, c.name AS course_name, c.credits, c.description,"
            + " s.id AS semester_id, s.semester_code, s.name AS semester_name, s.start_date,"
            + " s.end_date, s.is_open"
            + " FROM class_sections cs"
            + " JOIN courses c ON c.id = cs.course_id"
            + " JOIN semesters s ON s.id = cs.semester_id";

    private static final String COUNT_ENROLLMENTS_SQL =
            "SELECT COUNT(*) FROM enrollments WHERE section_id = ?";

    @Override
    public void insert(ClassSection section) throws SQLException {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {
            setFields(ps, section);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    section.setId(keys.getInt("id"));
                }
            }
        }
    }

    @Override
    public boolean update(ClassSection section) throws SQLException {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(UPDATE_SQL)) {
            setFields(ps, section);
            ps.setInt(6, section.getId());
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
    public ClassSection findById(int id) throws SQLException {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_SQL + " WHERE cs.id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        }
    }

    @Override
    public ClassSection findByCode(int semesterId, String sectionCode) throws SQLException {
        if (sectionCode == null || sectionCode.isBlank()) {
            return null;
        }
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     SELECT_SQL + " WHERE cs.semester_id = ? AND cs.section_code = ?")) {
            ps.setInt(1, semesterId);
            ps.setString(2, sectionCode.trim().toUpperCase());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        }
    }

    @Override
    public List<ClassSection> findBySemester(int semesterId) throws SQLException {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     SELECT_SQL + " WHERE cs.semester_id = ? ORDER BY cs.section_code")) {
            ps.setInt(1, semesterId);
            List<ClassSection> result = new ArrayList<>();
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(mapRow(rs));
                }
            }
            return result;
        }
    }

    @Override
    public int countEnrollments(int sectionId) throws SQLException {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(COUNT_ENROLLMENTS_SQL)) {
            ps.setInt(1, sectionId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    /** Chuyển dòng hiện tại (câu SELECT có JOIN ở trên) thành ClassSection. */
    ClassSection mapRow(ResultSet rs) throws SQLException {
        Course course = new Course(
                rs.getInt("course_id"),
                rs.getString("course_code"),
                rs.getString("course_name"),
                rs.getInt("credits"),
                rs.getString("description"));
        Semester semester = new Semester(
                rs.getInt("semester_id"),
                rs.getString("semester_code"),
                rs.getString("semester_name"),
                rs.getObject("start_date", LocalDate.class),
                rs.getObject("end_date", LocalDate.class),
                rs.getBoolean("is_open"));
        return new ClassSection(
                rs.getInt("id"),
                rs.getString("section_code"),
                course,
                semester,
                rs.getString("lecturer_name"),
                rs.getInt("max_students"));
    }

    /** Gán 5 tham số đầu, dùng chung cho INSERT và UPDATE. */
    private void setFields(PreparedStatement ps, ClassSection section) throws SQLException {
        ps.setString(1, section.getSectionCode());
        ps.setInt(2, section.getCourse().getId());
        ps.setInt(3, section.getSemester().getId());
        ps.setString(4, section.getLecturerName());
        ps.setInt(5, section.getMaxStudents());
    }
}
