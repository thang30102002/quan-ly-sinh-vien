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
import java.text.Normalizer;
import java.time.LocalDate;
import java.util.ArrayList;
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

    /** Liệt kê đủ cột mà {@link #mapRow(ResultSet)} cần. */
    private static final String SELECT_SQL =
            "SELECT id, student_code, full_name, dob, gender, email, phone,"
            + " class_name, intake_year, status FROM students";

    private static final String FIND_BY_ID_SQL = SELECT_SQL + " WHERE id = ?";

    private static final String FIND_BY_CODE_SQL = SELECT_SQL + " WHERE student_code = ?";

    private static final String ORDER_BY_CODE = " ORDER BY student_code";

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
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(FIND_BY_ID_SQL)) {
            ps.setInt(1, id);
            return queryOne(ps);
        }
    }

    @Override
    public Student findByCode(String studentCode) throws SQLException {
        if (studentCode == null || studentCode.isBlank()) {
            return null;
        }
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(FIND_BY_CODE_SQL)) {
            // Mã lưu dạng in hoa (BR-01, CHECK phân biệt hoa thường) nên chuẩn hóa trước khi so
            ps.setString(1, studentCode.trim().toUpperCase());
            return queryOne(ps);
        }
    }

    @Override
    public List<Student> findAll() throws SQLException {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_SQL + ORDER_BY_CODE)) {
            return queryList(ps);
        }
    }

    @Override
    public List<Student> search(String keyword, String className) throws SQLException {
        StringBuilder sql = new StringBuilder(SELECT_SQL).append(" WHERE TRUE");
        List<String> params = new ArrayList<>();

        if (keyword != null && !keyword.isBlank()) {
            // Khớp một phần, không phân biệt hoa thường theo mã SV hoặc họ tên (docs/yeu-cau.md).
            // ILIKE (locale en_US.utf8) xử lý đúng chữ có dấu; chuẩn hóa NFC để chữ gõ kiểu
            // tổ hợp vẫn khớp dữ liệu dựng sẵn.
            String pattern = "%" + escapeLike(Normalizer.normalize(keyword.trim(), Normalizer.Form.NFC)) + "%";
            sql.append(" AND (student_code ILIKE ? ESCAPE '\\' OR full_name ILIKE ? ESCAPE '\\')");
            params.add(pattern);
            params.add(pattern);
        }
        if (className != null && !className.isBlank()) {
            sql.append(" AND UPPER(class_name) = ?");
            params.add(className.trim().toUpperCase());
        }
        sql.append(ORDER_BY_CODE);

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setString(i + 1, params.get(i));
            }
            return queryList(ps);
        }
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

    /** Chạy câu SELECT và trả về dòng đầu tiên, hoặc null nếu không có dòng nào. */
    private Student queryOne(PreparedStatement ps) throws SQLException {
        try (ResultSet rs = ps.executeQuery()) {
            return rs.next() ? mapRow(rs) : null;
        }
    }

    /** Chạy câu SELECT và chuyển mọi dòng thành danh sách Student. */
    private List<Student> queryList(PreparedStatement ps) throws SQLException {
        List<Student> result = new ArrayList<>();
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                result.add(mapRow(rs));
            }
        }
        return result;
    }

    /** Thoát ký tự đặc biệt của LIKE để từ khóa như "50%" hay "a_b" được hiểu theo nghĩa đen. */
    private static String escapeLike(String value) {
        return value.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
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
