package com.qlsv.dao;

import com.qlsv.model.Gender;
import com.qlsv.model.Student;
import com.qlsv.model.StudentStatus;
import com.qlsv.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.List;

/**
 * Cài đặt {@link StudentDAO} bằng JDBC.
 */
public class StudentDAOImpl implements StudentDAO {

    private static final String INSERT_SQL =
            "INSERT INTO students (student_code, full_name, dob, gender, email, phone,"
            + " class_name, intake_year, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

    private static final String UPDATE_SQL =
            "UPDATE students SET student_code = ?, full_name = ?, dob = ?, gender = ?, email = ?,"
            + " phone = ?, class_name = ?, intake_year = ?, status = ? WHERE id = ?";

    private static final String DELETE_SQL = "DELETE FROM students WHERE id = ?";

    @Override
    public void insert(Student student) throws SQLException {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {
            setFields(ps, student);
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    student.setId(keys.getInt("id"));
                }
            }
        }
    }

    @Override
    public boolean update(Student student) throws SQLException {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(UPDATE_SQL)) {
            setFields(ps, student);
            ps.setInt(10, student.getId());
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
    public Student findById(int id) throws SQLException {
        throw new UnsupportedOperationException("Chưa cài đặt, xem #34");
    }

    @Override
    public Student findByCode(String studentCode) throws SQLException {
        throw new UnsupportedOperationException("Chưa cài đặt, xem #34");
    }

    @Override
    public List<Student> findAll() throws SQLException {
        throw new UnsupportedOperationException("Chưa cài đặt, xem #34");
    }

    @Override
    public List<Student> search(String keyword, String className) throws SQLException {
        throw new UnsupportedOperationException("Chưa cài đặt, xem #34");
    }

    /**
     * Chuyển dòng hiện tại của ResultSet thành Student. Không gọi {@code rs.next()}
     * và không đóng {@code rs}; câu SELECT phải có đủ các cột của bảng students.
     */
    Student mapRow(ResultSet rs) throws SQLException {
        return new Student(
                rs.getInt("id"),
                rs.getString("student_code"),
                rs.getString("full_name"),
                rs.getObject("dob", LocalDate.class),
                Gender.valueOf(rs.getString("gender")),
                rs.getString("email"),
                rs.getString("phone"),
                rs.getString("class_name"),
                rs.getInt("intake_year"),
                StudentStatus.valueOf(rs.getString("status")));
    }

    /** Gán 9 tham số đầu, dùng chung cho INSERT và UPDATE (cùng thứ tự cột). */
    private void setFields(PreparedStatement ps, Student student) throws SQLException {
        ps.setString(1, student.getStudentCode());
        ps.setString(2, student.getFullName());
        ps.setObject(3, student.getDob());
        // Dùng name() vì toString() của enum trả về nhãn tiếng Việt
        ps.setString(4, student.getGender().name());
        ps.setString(5, student.getEmail());
        ps.setString(6, student.getPhone());
        ps.setString(7, student.getClassName());
        ps.setInt(8, student.getIntakeYear());
        ps.setString(9, student.getStatus().name());
    }
}
