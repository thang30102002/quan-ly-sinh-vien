package com.qlsv.dao;

import com.qlsv.model.ClassSection;
import com.qlsv.model.Schedule;
import com.qlsv.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Cài đặt {@link ScheduleDAO} bằng JDBC.
 */
public class ScheduleDAOImpl implements ScheduleDAO {

    private static final String INSERT_SQL =
            "INSERT INTO schedules (section_id, day_of_week, start_period, end_period, room)"
            + " VALUES (?, ?, ?, ?, ?)";

    private static final String UPDATE_SQL =
            "UPDATE schedules SET section_id = ?, day_of_week = ?, start_period = ?,"
            + " end_period = ?, room = ? WHERE id = ?";

    private static final String DELETE_SQL = "DELETE FROM schedules WHERE id = ?";

    private static final String SELECT_SQL =
            "SELECT id, section_id, day_of_week, start_period, end_period, room FROM schedules";

    @Override
    public void insert(Schedule schedule) throws SQLException {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {
            setFields(ps, schedule);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    schedule.setId(keys.getInt("id"));
                }
            }
        }
    }

    @Override
    public boolean update(Schedule schedule) throws SQLException {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(UPDATE_SQL)) {
            setFields(ps, schedule);
            ps.setInt(6, schedule.getId());
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
    public Schedule findById(int id) throws SQLException {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_SQL + " WHERE id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        }
    }

    @Override
    public List<Schedule> findBySection(int sectionId) throws SQLException {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     SELECT_SQL + " WHERE section_id = ? ORDER BY day_of_week, start_period")) {
            ps.setInt(1, sectionId);
            List<Schedule> result = new ArrayList<>();
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(mapRow(rs));
                }
            }
            return result;
        }
    }

    /** Chuyển dòng hiện tại thành Schedule; lớp học phần chỉ mang id. */
    Schedule mapRow(ResultSet rs) throws SQLException {
        ClassSection section = new ClassSection();
        section.setId(rs.getInt("section_id"));
        return new Schedule(
                rs.getInt("id"),
                section,
                rs.getInt("day_of_week"),
                rs.getInt("start_period"),
                rs.getInt("end_period"),
                rs.getString("room"));
    }

    /** Gán 5 tham số đầu, dùng chung cho INSERT và UPDATE. */
    private void setFields(PreparedStatement ps, Schedule schedule) throws SQLException {
        ps.setInt(1, schedule.getSection().getId());
        ps.setInt(2, schedule.getDayOfWeek());
        ps.setInt(3, schedule.getStartPeriod());
        ps.setInt(4, schedule.getEndPeriod());
        ps.setString(5, schedule.getRoom());
    }
}
