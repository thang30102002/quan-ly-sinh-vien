package com.qlsv.dao;

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
 * Cài đặt {@link SemesterDAO} bằng JDBC.
 */
public class SemesterDAOImpl implements SemesterDAO {

    private static final String INSERT_SQL =
            "INSERT INTO semesters (semester_code, name, start_date, end_date, is_open)"
            + " VALUES (?, ?, ?, ?, ?)";

    private static final String UPDATE_SQL =
            "UPDATE semesters SET semester_code = ?, name = ?, start_date = ?, end_date = ?,"
            + " is_open = ? WHERE id = ?";

    private static final String DELETE_SQL = "DELETE FROM semesters WHERE id = ?";

    private static final String SELECT_SQL =
            "SELECT id, semester_code, name, start_date, end_date, is_open FROM semesters";

    private static final String HAS_SECTIONS_SQL =
            "SELECT EXISTS (SELECT 1 FROM class_sections WHERE semester_id = ?)";

    private static final String HAS_ENROLLMENTS_SQL =
            "SELECT EXISTS (SELECT 1 FROM enrollments e"
            + " JOIN class_sections cs ON cs.id = e.section_id WHERE cs.semester_id = ?)";

    @Override
    public void insert(Semester semester) throws SQLException {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {
            setFields(ps, semester);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    semester.setId(keys.getInt("id"));
                }
            }
        }
    }

    @Override
    public boolean update(Semester semester) throws SQLException {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(UPDATE_SQL)) {
            setFields(ps, semester);
            ps.setInt(6, semester.getId());
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
    public Semester findById(int id) throws SQLException {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_SQL + " WHERE id = ?")) {
            ps.setInt(1, id);
            return queryOne(ps);
        }
    }

    @Override
    public Semester findByCode(String semesterCode) throws SQLException {
        if (semesterCode == null || semesterCode.isBlank()) {
            return null;
        }
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_SQL + " WHERE semester_code = ?")) {
            ps.setString(1, semesterCode.trim());
            return queryOne(ps);
        }
    }

    @Override
    public List<Semester> findAll() throws SQLException {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     SELECT_SQL + " ORDER BY start_date DESC, semester_code DESC")) {
            List<Semester> result = new ArrayList<>();
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(mapRow(rs));
                }
            }
            return result;
        }
    }

    @Override
    public Semester findOpen() throws SQLException {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_SQL + " WHERE is_open")) {
            return queryOne(ps);
        }
    }

    @Override
    public boolean hasSections(int semesterId) throws SQLException {
        return exists(HAS_SECTIONS_SQL, semesterId);
    }

    @Override
    public boolean hasEnrollments(int semesterId) throws SQLException {
        return exists(HAS_ENROLLMENTS_SQL, semesterId);
    }

    /** Chuyển dòng hiện tại của ResultSet thành Semester; không gọi {@code rs.next()}. */
    Semester mapRow(ResultSet rs) throws SQLException {
        return new Semester(
                rs.getInt("id"),
                rs.getString("semester_code"),
                rs.getString("name"),
                rs.getObject("start_date", LocalDate.class),
                rs.getObject("end_date", LocalDate.class),
                rs.getBoolean("is_open"));
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

    private Semester queryOne(PreparedStatement ps) throws SQLException {
        try (ResultSet rs = ps.executeQuery()) {
            return rs.next() ? mapRow(rs) : null;
        }
    }

    /** Gán 5 tham số đầu, dùng chung cho INSERT và UPDATE. */
    private void setFields(PreparedStatement ps, Semester semester) throws SQLException {
        ps.setString(1, semester.getSemesterCode());
        ps.setString(2, semester.getName());
        ps.setObject(3, semester.getStartDate());
        ps.setObject(4, semester.getEndDate());
        ps.setBoolean(5, semester.isOpen());
    }
}
