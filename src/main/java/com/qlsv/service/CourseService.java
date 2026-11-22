package com.qlsv.service;

import static com.qlsv.service.ServiceSupport.databaseError;
import static com.qlsv.service.ServiceSupport.isForeignKeyViolation;
import static com.qlsv.service.ServiceSupport.isUniqueViolation;
import static com.qlsv.service.ServiceSupport.trimToNull;

import com.qlsv.dao.CourseDAO;
import com.qlsv.dao.CourseDAOImpl;
import com.qlsv.exception.BusinessException;
import com.qlsv.exception.ValidationException;
import com.qlsv.model.Course;
import com.qlsv.util.Validator;

import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

/**
 * Quy tắc nghiệp vụ của học phần (BR-02, BR-16, BR-18).
 * Không chứa câu SQL nào: mọi truy cập CSDL đi qua {@link CourseDAO}.
 */
public class CourseService {

    static final int NAME_MAX_LENGTH = 150;
    static final int MIN_CREDITS = 1;
    static final int MAX_CREDITS = 6;

    private final CourseDAO courseDAO;

    public CourseService() {
        this(new CourseDAOImpl());
    }

    /** Cho phép truyền DAO khác, ví dụ DAO giả trong test. */
    public CourseService(CourseDAO courseDAO) {
        this.courseDAO = Objects.requireNonNull(courseDAO, "courseDAO");
    }

    /**
     * Thêm học phần mới.
     *
     * @throws ValidationException dữ liệu sai định dạng hoặc thiếu (BR-02, BR-18)
     * @throws BusinessException   mã học phần đã tồn tại (BR-02) hoặc lỗi CSDL
     */
    public void add(Course course) {
        requireCourse(course);
        normalize(course);
        validate(course);

        try {
            if (courseDAO.findByCode(course.getCourseCode()) != null) {
                throw duplicateCode(course.getCourseCode());
            }
            courseDAO.insert(course);
        } catch (SQLException e) {
            if (isUniqueViolation(e)) {
                throw duplicateCode(course.getCourseCode());
            }
            throw databaseError("thêm học phần", e);
        }
    }

    /**
     * Cập nhật học phần theo id. Học phần đã mở lớp thì không đổi được mã (mã lớp
     * bắt đầu bằng mã học phần, BR-24); đã có sinh viên đăng ký thì không đổi được
     * số tín chỉ (làm sai tổng tín chỉ đã đăng ký, BR-04).
     *
     * @throws ValidationException dữ liệu sai định dạng hoặc thiếu
     * @throws BusinessException   không tìm thấy, trùng mã hoặc vi phạm điều kiện trên
     */
    public void update(Course course) {
        requireCourse(course);
        if (course.getId() == null) {
            throw new ValidationException("Chưa chọn học phần cần sửa");
        }
        normalize(course);
        validate(course);

        try {
            Course current = courseDAO.findById(course.getId());
            if (current == null) {
                throw notFound(course.getId());
            }
            boolean codeChanged = !current.getCourseCode().equals(course.getCourseCode());
            if (codeChanged) {
                Course sameCode = courseDAO.findByCode(course.getCourseCode());
                if (sameCode != null && !sameCode.getId().equals(course.getId())) {
                    throw duplicateCode(course.getCourseCode());
                }
                if (courseDAO.hasSections(course.getId())) {
                    throw new BusinessException("Không đổi được mã học phần " + current.getCourseCode()
                            + " vì đã mở lớp học phần, mã lớp phải bắt đầu bằng mã học phần (BR-24)");
                }
            }
            if (current.getCredits() != course.getCredits() && courseDAO.hasEnrollments(course.getId())) {
                throw new BusinessException("Không đổi được số tín chỉ của học phần "
                        + current.getCourseCode() + " vì đã có sinh viên đăng ký (BR-04)");
            }
            if (!courseDAO.update(course)) {
                throw notFound(course.getId());
            }
        } catch (SQLException e) {
            if (isUniqueViolation(e)) {
                throw duplicateCode(course.getCourseCode());
            }
            throw databaseError("cập nhật học phần", e);
        }
    }

    /**
     * Xóa học phần. Không xóa học phần đã có sinh viên đăng ký (BR-16); học phần đã
     * mở lớp (chưa ai đăng ký) thì phải xóa các lớp trước.
     *
     * @throws BusinessException không tìm thấy, vi phạm BR-16 hoặc lỗi CSDL
     */
    public void delete(int id) {
        try {
            Course current = courseDAO.findById(id);
            if (current == null) {
                throw notFound(id);
            }
            if (courseDAO.hasEnrollments(id)) {
                throw new BusinessException("Không xóa được học phần " + current.getCourseCode()
                        + " vì đã có sinh viên đăng ký (BR-16)");
            }
            if (courseDAO.hasSections(id)) {
                throw hasSections(current);
            }
            if (!courseDAO.delete(id)) {
                throw notFound(id);
            }
        } catch (SQLException e) {
            if (isForeignKeyViolation(e)) {
                throw new BusinessException("Không xóa được học phần vì đang được lớp học phần sử dụng (BR-16)", e);
            }
            throw databaseError("xóa học phần", e);
        }
    }

    /** @return học phần có id này, hoặc null nếu không có */
    public Course findById(int id) {
        try {
            return courseDAO.findById(id);
        } catch (SQLException e) {
            throw databaseError("tìm học phần", e);
        }
    }

    /** @return toàn bộ học phần, sắp theo mã */
    public List<Course> findAll() {
        try {
            return courseDAO.findAll();
        } catch (SQLException e) {
            throw databaseError("tải danh sách học phần", e);
        }
    }

    /** Tìm theo mã hoặc tên (khớp một phần); từ khóa rỗng thì trả về toàn bộ. */
    public List<Course> search(String keyword) {
        try {
            return courseDAO.search(keyword);
        } catch (SQLException e) {
            throw databaseError("tìm kiếm học phần", e);
        }
    }

    private static void requireCourse(Course course) {
        if (course == null) {
            throw new ValidationException("Chưa nhập thông tin học phần");
        }
    }

    /** Mã in hoa, bỏ khoảng trắng hai đầu (BR-02); mô tả để trống thì lưu null. */
    private static void normalize(Course c) {
        if (c.getCourseCode() != null) {
            c.setCourseCode(c.getCourseCode().trim().toUpperCase());
        }
        if (c.getName() != null) {
            c.setName(c.getName().trim());
        }
        c.setDescription(trimToNull(c.getDescription()));
    }

    private static void validate(Course c) {
        Validator.requireCourseCode(c.getCourseCode());
        if (c.getName() == null || c.getName().isEmpty()) {
            throw new ValidationException("Tên học phần là bắt buộc");
        }
        if (c.getName().length() > NAME_MAX_LENGTH) {
            throw new ValidationException("Tên học phần tối đa " + NAME_MAX_LENGTH + " ký tự");
        }
        if (c.getCredits() < MIN_CREDITS || c.getCredits() > MAX_CREDITS) {
            throw new ValidationException("Số tín chỉ phải là số nguyên từ " + MIN_CREDITS
                    + " đến " + MAX_CREDITS + " (BR-18)");
        }
    }

    private static BusinessException duplicateCode(String code) {
        return new BusinessException("Mã học phần " + code + " đã tồn tại (BR-02)");
    }

    private static BusinessException hasSections(Course c) {
        return new BusinessException("Không xóa được học phần " + c.getCourseCode()
                + " vì đã mở lớp học phần, hãy xóa các lớp của học phần này trước (BR-16)");
    }

    private static BusinessException notFound(int id) {
        return new BusinessException("Không tìm thấy học phần có id " + id
                + ", có thể đã bị xóa trước đó");
    }
}
