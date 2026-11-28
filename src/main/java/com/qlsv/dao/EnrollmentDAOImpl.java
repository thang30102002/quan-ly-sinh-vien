package com.qlsv.dao;

import com.qlsv.model.ClassSection;
import com.qlsv.model.Course;
import com.qlsv.model.Enrollment;
import com.qlsv.model.Gender;
import com.qlsv.model.Schedule;
import com.qlsv.model.Semester;
import com.qlsv.model.Student;
import com.qlsv.model.StudentStatus;
import com.qlsv.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Cài đặt {@link EnrollmentDAO} bằng JDBC.
 */
public class EnrollmentDAOImpl implements EnrollmentDAO {

    private static final String INSERT_SQL =
            "INSERT INTO enrollments (student_id, section_id, registered_at) VALUES (?, ?, ?)";

    private static final String DELETE_SQL = "DELETE FROM enrollments WHERE id = ?";

    private static final String COUNT_BY_SECTION_SQL =
            "SELECT COUNT(*) FROM enrollments WHERE section_id = ?";

    private static final String SELECT_ENROLLMENT_SQL =
            "SELECT e.id, e.registered_at, e.attendance, e.midterm, e.final_exam, e.total, e.letter, e.gpa4,"
            + " st.id AS student_id, st.student_code, st.full_name, st.dob, st.gender,"
            + " st.email, st.phone, st.class_name, st.intake_year, st.status,"
            + " cs.id AS section_id, cs.section_code, cs.lecturer_name, cs.max_students,"
            + " c.id AS course_id, c.course_code, c.name AS course_name, c.credits, c.description,"
            + " sem.id AS semester_id, sem.semester_code, sem.name AS semester_name,"
            + " sem.start_date, sem.end_date, sem.is_open"
            + " FROM enrollments e"
            + " JOIN students st ON st.id = e.student_id"
            + " JOIN class_sections cs ON cs.id = e.section_id"
            + " JOIN courses c ON c.id = cs.course_id"
            + " JOIN semesters sem ON sem.id = cs.semester_id";

    private static final String SELECT_SCHEDULES_SQL =
            "SELECT sch.id, sch.day_of_week, sch.start_period, sch.end_period, sch.room,"
            + " cs.id AS section_id, cs.section_code, cs.lecturer_name, cs.max_students,"
            + " c.id AS course_id, c.course_code, c.name AS course_name, c.credits, c.description,"
            + " sem.id AS semester_id, sem.semester_code, sem.name AS semester_name,"
            + " sem.start_date, sem.end_date, sem.is_open"
            + " FROM schedules sch"
            + " JOIN class_sections cs ON cs.id = sch.section_id"
            + " JOIN courses c ON c.id = cs.course_id"
            + " JOIN semesters sem ON sem.id = cs.semester_id"
            + " JOIN enrollments e ON e.section_id = cs.id"
            + " WHERE e.student_id = ? AND cs.semester_id = ?"
            + " ORDER BY sch.day_of_week, sch.start_period, cs.section_code";

    @Override
    public void register(Enrollment enrollment) throws SQLException {
        try (Connection conn = DBConnection.getConnection()) {
            register(conn, enrollment);
        }
    }

    @Override
    public void register(Connection conn, Enrollment enrollment) throws SQLException {
        LocalDateTime registeredAt = enrollment.getRegisteredAt() != null
                ? enrollment.getRegisteredAt()
                : LocalDateTime.now();
        enrollment.setRegisteredAt(registeredAt);
        try (PreparedStatement ps = conn.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, enrollment.getStudent().getId());
            ps.setInt(2, enrollment.getSection().getId());
            ps.setObject(3, registeredAt);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    enrollment.setId(keys.getInt("id"));
                }
            }
        }
    }

    @Override
    public boolean cancel(int enrollmentId) throws SQLException {
        try (Connection conn = DBConnection.getConnection()) {
            return cancel(conn, enrollmentId);
        }
    }

    @Override
    public boolean cancel(Connection conn, int enrollmentId) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(DELETE_SQL)) {
            ps.setInt(1, enrollmentId);
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public Enrollment findById(int id) throws SQLException {
        try (Connection conn = DBConnection.getConnection()) {
            return findById(conn, id);
        }
    }

    @Override
    public Enrollment findById(Connection conn, int id) throws SQLException {
        List<Enrollment> list = queryEnrollments(conn, SELECT_ENROLLMENT_SQL + " WHERE e.id = ?", id);
        return list.isEmpty() ? null : list.get(0);
    }

    @Override
    public List<Enrollment> findBySemester(int semesterId) throws SQLException {
        try (Connection conn = DBConnection.getConnection()) {
            return findBySemester(conn, semesterId);
        }
    }

    @Override
    public List<Enrollment> findBySemester(Connection conn, int semesterId) throws SQLException {
        return queryEnrollments(conn,
                SELECT_ENROLLMENT_SQL + " WHERE cs.semester_id = ? ORDER BY st.student_code, cs.section_code",
                semesterId);
    }

    @Override
    public List<Enrollment> findByStudentAndSemester(int studentId, int semesterId) throws SQLException {
        try (Connection conn = DBConnection.getConnection()) {
            return findByStudentAndSemester(conn, studentId, semesterId);
        }
    }

    @Override
    public List<Enrollment> findByStudentAndSemester(Connection conn, int studentId, int semesterId)
            throws SQLException {
        return queryEnrollments(conn,
                SELECT_ENROLLMENT_SQL + " WHERE e.student_id = ? AND cs.semester_id = ? ORDER BY cs.section_code",
                studentId, semesterId);
    }

    @Override
    public List<Enrollment> findBySection(int sectionId) throws SQLException {
        try (Connection conn = DBConnection.getConnection()) {
            return findBySection(conn, sectionId);
        }
    }

    @Override
    public List<Enrollment> findBySection(Connection conn, int sectionId) throws SQLException {
        return queryEnrollments(conn,
                SELECT_ENROLLMENT_SQL + " WHERE e.section_id = ? ORDER BY st.student_code",
                sectionId);
    }

    @Override
    public int countBySection(int sectionId) throws SQLException {
        try (Connection conn = DBConnection.getConnection()) {
            return countBySection(conn, sectionId);
        }
    }

    @Override
    public int countBySection(Connection conn, int sectionId) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(COUNT_BY_SECTION_SQL)) {
            ps.setInt(1, sectionId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    @Override
    public List<Schedule> findSchedulesByStudentAndSemester(int studentId, int semesterId) throws SQLException {
        try (Connection conn = DBConnection.getConnection()) {
            return findSchedulesByStudentAndSemester(conn, studentId, semesterId);
        }
    }

    @Override
    public List<Schedule> findSchedulesByStudentAndSemester(Connection conn, int studentId, int semesterId)
            throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(SELECT_SCHEDULES_SQL)) {
            ps.setInt(1, studentId);
            ps.setInt(2, semesterId);
            List<Schedule> result = new ArrayList<>();
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(mapSchedule(rs));
                }
            }
            return result;
        }
    }

    private List<Enrollment> queryEnrollments(Connection conn, String sql, int... params) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                ps.setInt(i + 1, params[i]);
            }
            List<Enrollment> result = new ArrayList<>();
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(mapEnrollment(rs));
                }
            }
            return result;
        }
    }

    private static Enrollment mapEnrollment(ResultSet rs) throws SQLException {
        Student student = new Student(
                rs.getInt("student_id"),
                rs.getString("student_code"),
                rs.getString("full_name"),
                rs.getObject("dob", LocalDate.class),
                Gender.valueOf(rs.getString("gender")),
                rs.getString("email"),
                rs.getString("phone"),
                rs.getString("class_name"),
                rs.getInt("intake_year"),
                StudentStatus.valueOf(rs.getString("status")));
        return new Enrollment(
                rs.getInt("id"),
                student,
                mapSection(rs),
                rs.getObject("registered_at", LocalDateTime.class),
                rs.getBigDecimal("attendance"),
                rs.getBigDecimal("midterm"),
                rs.getBigDecimal("final_exam"),
                rs.getBigDecimal("total"),
                rs.getString("letter"),
                rs.getBigDecimal("gpa4"));
    }

    private static Schedule mapSchedule(ResultSet rs) throws SQLException {
        return new Schedule(
                rs.getInt("id"),
                mapSection(rs),
                rs.getInt("day_of_week"),
                rs.getInt("start_period"),
                rs.getInt("end_period"),
                rs.getString("room"));
    }

    private static ClassSection mapSection(ResultSet rs) throws SQLException {
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
                rs.getInt("section_id"),
                rs.getString("section_code"),
                course,
                semester,
                rs.getString("lecturer_name"),
                rs.getInt("max_students"));
    }
}
