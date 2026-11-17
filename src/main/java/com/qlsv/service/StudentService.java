package com.qlsv.service;

import com.qlsv.dao.StudentDAO;
import com.qlsv.dao.StudentDAOImpl;
import com.qlsv.exception.BusinessException;
import com.qlsv.exception.ValidationException;
import com.qlsv.model.Student;
import com.qlsv.model.StudentStatus;
import com.qlsv.util.Validator;

import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

/**
 * Quy tắc nghiệp vụ của sinh viên (BR-01, BR-14, BR-17, BR-20).
 * Không chứa câu SQL nào: mọi truy cập CSDL đi qua {@link StudentDAO}.
 */
public class StudentService {

    /** Mã lỗi PostgreSQL khi vi phạm ràng buộc UNIQUE. */
    private static final String SQLSTATE_UNIQUE_VIOLATION = "23505";

    private static final int CLASS_NAME_MAX_LENGTH = 20;
    private static final int EMAIL_MAX_LENGTH = 100;

    /** Kết quả của {@link #delete(int)} để giao diện báo đúng cho người dùng. */
    public enum DeleteResult {
        /** Sinh viên chưa có điểm nên đã bị xóa hẳn, kèm các đăng ký. */
        DELETED,
        /** Sinh viên đã có điểm nên chỉ chuyển trạng thái sang Thôi học (BR-14). */
        DROPPED
    }

    private final StudentDAO studentDAO;

    public StudentService() {
        this(new StudentDAOImpl());
    }

    /** Cho phép truyền DAO khác, ví dụ DAO giả trong test. */
    public StudentService(StudentDAO studentDAO) {
        this.studentDAO = Objects.requireNonNull(studentDAO, "studentDAO");
    }

    /**
     * Thêm sinh viên mới sau khi chuẩn hóa và kiểm tra dữ liệu.
     *
     * @throws ValidationException dữ liệu sai định dạng hoặc thiếu (BR-01, BR-17, BR-20)
     * @throws BusinessException   mã sinh viên đã tồn tại (BR-01) hoặc lỗi CSDL
     */
    public void add(Student student) {
        requireStudent(student);
        normalize(student);
        validate(student);

        try {
            if (studentDAO.findByCode(student.getStudentCode()) != null) {
                throw duplicateCode(student.getStudentCode());
            }
            studentDAO.insert(student);
        } catch (SQLException e) {
            // Hai người thêm cùng mã cùng lúc thì lần kiểm tra trên vẫn lọt, CSDL sẽ chặn
            if (SQLSTATE_UNIQUE_VIOLATION.equals(e.getSQLState())) {
                throw duplicateCode(student.getStudentCode());
            }
            throw databaseError("thêm sinh viên", e);
        }
    }

    /**
     * Cập nhật thông tin sinh viên theo id. Không cho đổi mã sinh viên (BR-01).
     *
     * @throws ValidationException dữ liệu sai định dạng hoặc thiếu
     * @throws BusinessException   không tìm thấy sinh viên, đổi mã sinh viên hoặc lỗi CSDL
     */
    public void update(Student student) {
        requireStudent(student);
        if (student.getId() == null) {
            throw new ValidationException("Chưa chọn sinh viên cần sửa");
        }
        normalize(student);
        validate(student);

        try {
            Student current = studentDAO.findById(student.getId());
            if (current == null) {
                throw notFound(student.getId());
            }
            if (!current.getStudentCode().equals(student.getStudentCode())) {
                throw new BusinessException("Không được sửa mã sinh viên (đang là "
                        + current.getStudentCode() + ", định đổi thành "
                        + student.getStudentCode() + ") (BR-01)");
            }
            if (!studentDAO.update(student)) {
                throw notFound(student.getId());
            }
        } catch (SQLException e) {
            throw databaseError("cập nhật sinh viên", e);
        }
    }

    /**
     * Xóa sinh viên theo BR-14: sinh viên đã có ít nhất một điểm thành phần thì
     * không xóa mà chuyển trạng thái sang Thôi học; sinh viên chưa có điểm thì xóa
     * hẳn, các đăng ký của sinh viên bị xóa theo (ON DELETE CASCADE).
     *
     * @return {@link DeleteResult#DELETED} nếu đã xóa hẳn,
     *         {@link DeleteResult#DROPPED} nếu chỉ chuyển trạng thái
     * @throws BusinessException không tìm thấy sinh viên hoặc lỗi CSDL
     */
    public DeleteResult delete(int id) {
        try {
            Student current = studentDAO.findById(id);
            if (current == null) {
                throw notFound(id);
            }
            if (studentDAO.hasGrades(id)) {
                if (current.getStatus() != StudentStatus.DROPPED) {
                    current.setStatus(StudentStatus.DROPPED);
                    if (!studentDAO.update(current)) {
                        throw notFound(id);
                    }
                }
                return DeleteResult.DROPPED;
            }
            if (!studentDAO.delete(id)) {
                throw notFound(id);
            }
            return DeleteResult.DELETED;
        } catch (SQLException e) {
            throw databaseError("xóa sinh viên", e);
        }
    }

    /** @return sinh viên có id này, hoặc null nếu không có */
    public Student findById(int id) {
        try {
            return studentDAO.findById(id);
        } catch (SQLException e) {
            throw databaseError("tìm sinh viên", e);
        }
    }

    /** @return toàn bộ sinh viên, sắp theo mã */
    public List<Student> findAll() {
        try {
            return studentDAO.findAll();
        } catch (SQLException e) {
            throw databaseError("tải danh sách sinh viên", e);
        }
    }

    /**
     * Tìm theo từ khóa (mã hoặc họ tên, khớp một phần, không phân biệt hoa thường)
     * và lớp sinh hoạt. Tham số null hoặc rỗng thì bỏ qua điều kiện đó.
     */
    public List<Student> search(String keyword, String className) {
        try {
            return studentDAO.search(keyword, className);
        } catch (SQLException e) {
            throw databaseError("tìm kiếm sinh viên", e);
        }
    }

    private static void requireStudent(Student student) {
        if (student == null) {
            throw new ValidationException("Chưa nhập thông tin sinh viên");
        }
    }

    /**
     * Chuẩn hóa trước khi kiểm tra: mã in hoa và bỏ khoảng trắng hai đầu (BR-01),
     * bỏ khoảng trắng hai đầu các trường chữ, email/số điện thoại để trống thì lưu null.
     */
    private static void normalize(Student s) {
        if (s.getStudentCode() != null) {
            s.setStudentCode(s.getStudentCode().trim().toUpperCase());
        }
        if (s.getFullName() != null) {
            s.setFullName(s.getFullName().trim());
        }
        if (s.getClassName() != null) {
            s.setClassName(s.getClassName().trim());
        }
        s.setEmail(trimToNull(s.getEmail()));
        s.setPhone(trimToNull(s.getPhone()));
        if (s.getStatus() == null) {
            s.setStatus(StudentStatus.STUDYING);
        }
    }

    private static void validate(Student s) {
        Validator.requireStudentCode(s.getStudentCode());
        Validator.requireFullName(s.getFullName());
        Validator.requireDateOfBirth(s.getDob());
        if (s.getGender() == null) {
            throw new ValidationException("Giới tính là bắt buộc");
        }
        Validator.requireEmail(s.getEmail());
        if (s.getEmail() != null && s.getEmail().length() > EMAIL_MAX_LENGTH) {
            throw new ValidationException("Email tối đa " + EMAIL_MAX_LENGTH + " ký tự (BR-17)");
        }
        Validator.requirePhone(s.getPhone());
        if (s.getClassName() == null || s.getClassName().isEmpty()) {
            throw new ValidationException("Lớp sinh hoạt là bắt buộc");
        }
        if (s.getClassName().length() > CLASS_NAME_MAX_LENGTH) {
            throw new ValidationException("Lớp sinh hoạt tối đa " + CLASS_NAME_MAX_LENGTH + " ký tự");
        }
        // BR-01: 4 số sau "SV" là năm nhập học, CSDL cũng CHECK điều này
        int codeYear = Integer.parseInt(s.getStudentCode().substring(2, 6));
        if (s.getIntakeYear() != codeYear) {
            throw new ValidationException("Khóa (năm nhập học) " + s.getIntakeYear()
                    + " không khớp với năm " + codeYear + " trong mã sinh viên "
                    + s.getStudentCode() + " (BR-01)");
        }
    }

    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private static BusinessException duplicateCode(String code) {
        return new BusinessException("Mã sinh viên " + code + " đã tồn tại (BR-01)");
    }

    private static BusinessException notFound(int id) {
        return new BusinessException("Không tìm thấy sinh viên có id " + id
                + ", có thể đã bị xóa trước đó");
    }

    private static BusinessException databaseError(String action, SQLException e) {
        return new BusinessException("Lỗi CSDL khi " + action + ": " + e.getMessage(), e);
    }
}
