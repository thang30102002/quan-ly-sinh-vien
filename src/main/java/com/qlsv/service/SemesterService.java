package com.qlsv.service;

import static com.qlsv.service.ServiceSupport.databaseError;
import static com.qlsv.service.ServiceSupport.isForeignKeyViolation;
import static com.qlsv.service.ServiceSupport.isUniqueViolation;
import static com.qlsv.service.ServiceSupport.mentions;

import com.qlsv.dao.SemesterDAO;
import com.qlsv.dao.SemesterDAOImpl;
import com.qlsv.exception.BusinessException;
import com.qlsv.exception.ValidationException;
import com.qlsv.model.Semester;
import com.qlsv.util.Validator;

import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

/**
 * Quy tắc nghiệp vụ của học kỳ (BR-03, BR-16, BR-19, BR-26).
 * Không chứa câu SQL nào: mọi truy cập CSDL đi qua {@link SemesterDAO}.
 */
public class SemesterService {

    static final int NAME_MAX_LENGTH = 100;

    /** Tên partial unique index chặn hai học kỳ cùng mở (schema.sql). */
    private static final String OPEN_INDEX = "uq_semesters_open";

    private final SemesterDAO semesterDAO;

    public SemesterService() {
        this(new SemesterDAOImpl());
    }

    /** Cho phép truyền DAO khác, ví dụ DAO giả trong test. */
    public SemesterService(SemesterDAO semesterDAO) {
        this.semesterDAO = Objects.requireNonNull(semesterDAO, "semesterDAO");
    }

    /**
     * Thêm học kỳ mới. Nếu học kỳ mới ở trạng thái mở đăng ký mà đang có học kỳ
     * khác mở thì từ chối (BR-26), phải đóng học kỳ kia trước.
     *
     * @throws ValidationException dữ liệu sai định dạng hoặc thiếu (BR-03, BR-19)
     * @throws BusinessException   trùng mã (BR-03), đã có học kỳ mở (BR-26) hoặc lỗi CSDL
     */
    public void add(Semester semester) {
        requireSemester(semester);
        normalize(semester);
        validate(semester);

        try {
            if (semesterDAO.findByCode(semester.getSemesterCode()) != null) {
                throw duplicateCode(semester.getSemesterCode());
            }
            requireNoOtherOpen(semester);
            semesterDAO.insert(semester);
        } catch (SQLException e) {
            throw translate(e, semester, "thêm học kỳ");
        }
    }

    /**
     * Cập nhật học kỳ theo id, kể cả bật/tắt trạng thái mở đăng ký (BR-26).
     *
     * @throws ValidationException dữ liệu sai định dạng hoặc thiếu
     * @throws BusinessException   không tìm thấy, trùng mã, đã có học kỳ khác mở hoặc lỗi CSDL
     */
    public void update(Semester semester) {
        requireSemester(semester);
        if (semester.getId() == null) {
            throw new ValidationException("Chưa chọn học kỳ cần sửa");
        }
        normalize(semester);
        validate(semester);

        try {
            if (semesterDAO.findById(semester.getId()) == null) {
                throw notFound(semester.getId());
            }
            Semester sameCode = semesterDAO.findByCode(semester.getSemesterCode());
            if (sameCode != null && !sameCode.getId().equals(semester.getId())) {
                throw duplicateCode(semester.getSemesterCode());
            }
            requireNoOtherOpen(semester);
            if (!semesterDAO.update(semester)) {
                throw notFound(semester.getId());
            }
        } catch (SQLException e) {
            throw translate(e, semester, "cập nhật học kỳ");
        }
    }

    /**
     * Xóa học kỳ. Không xóa học kỳ đã có sinh viên đăng ký (BR-16); học kỳ đã mở lớp
     * (chưa ai đăng ký) thì phải xóa các lớp trước.
     *
     * @throws BusinessException không tìm thấy, vi phạm BR-16 hoặc lỗi CSDL
     */
    public void delete(int id) {
        try {
            Semester current = semesterDAO.findById(id);
            if (current == null) {
                throw notFound(id);
            }
            if (semesterDAO.hasEnrollments(id)) {
                throw new BusinessException("Không xóa được học kỳ " + current.getSemesterCode()
                        + " vì đã có sinh viên đăng ký (BR-16)");
            }
            if (semesterDAO.hasSections(id)) {
                throw new BusinessException("Không xóa được học kỳ " + current.getSemesterCode()
                        + " vì đã mở lớp học phần, hãy xóa các lớp của học kỳ này trước (BR-16)");
            }
            if (!semesterDAO.delete(id)) {
                throw notFound(id);
            }
        } catch (SQLException e) {
            if (isForeignKeyViolation(e)) {
                throw new BusinessException("Không xóa được học kỳ vì đang được lớp học phần sử dụng (BR-16)", e);
            }
            throw databaseError("xóa học kỳ", e);
        }
    }

    /** @return học kỳ có id này, hoặc null nếu không có */
    public Semester findById(int id) {
        try {
            return semesterDAO.findById(id);
        } catch (SQLException e) {
            throw databaseError("tìm học kỳ", e);
        }
    }

    /** @return toàn bộ học kỳ, mới nhất trước */
    public List<Semester> findAll() {
        try {
            return semesterDAO.findAll();
        } catch (SQLException e) {
            throw databaseError("tải danh sách học kỳ", e);
        }
    }

    /** BR-26: @return học kỳ đang mở đăng ký, hoặc null nếu không có */
    public Semester findOpen() {
        try {
            return semesterDAO.findOpen();
        } catch (SQLException e) {
            throw databaseError("tìm học kỳ đang mở", e);
        }
    }

    /** BR-26: chỉ một học kỳ mở; học kỳ đang lưu có mở thì không được có học kỳ khác mở. */
    private void requireNoOtherOpen(Semester semester) throws SQLException {
        if (!semester.isOpen()) {
            return;
        }
        Semester open = semesterDAO.findOpen();
        if (open != null && !open.getId().equals(semester.getId())) {
            throw anotherOpen(open.getSemesterCode());
        }
    }

    private static void requireSemester(Semester semester) {
        if (semester == null) {
            throw new ValidationException("Chưa nhập thông tin học kỳ");
        }
    }

    private static void normalize(Semester s) {
        if (s.getSemesterCode() != null) {
            s.setSemesterCode(s.getSemesterCode().trim().toUpperCase());
        }
        if (s.getName() != null) {
            s.setName(s.getName().trim());
        }
    }

    private static void validate(Semester s) {
        Validator.requireSemesterCode(s.getSemesterCode());
        if (s.getName() == null || s.getName().isEmpty()) {
            throw new ValidationException("Tên học kỳ là bắt buộc");
        }
        if (s.getName().length() > NAME_MAX_LENGTH) {
            throw new ValidationException("Tên học kỳ tối đa " + NAME_MAX_LENGTH + " ký tự");
        }
        if (s.getStartDate() == null) {
            throw new ValidationException("Ngày bắt đầu là bắt buộc");
        }
        if (s.getEndDate() == null) {
            throw new ValidationException("Ngày kết thúc là bắt buộc");
        }
        if (!s.getEndDate().isAfter(s.getStartDate())) {
            throw new ValidationException("Ngày kết thúc học kỳ phải sau ngày bắt đầu (BR-19)");
        }
    }

    /** Đổi lỗi UNIQUE của CSDL (khi hai người thao tác cùng lúc) thành thông báo dễ hiểu. */
    private BusinessException translate(SQLException e, Semester semester, String action) {
        if (isUniqueViolation(e)) {
            if (mentions(e, OPEN_INDEX)) {
                return anotherOpen(null);
            }
            return duplicateCode(semester.getSemesterCode());
        }
        return databaseError(action, e);
    }

    private static BusinessException duplicateCode(String code) {
        return new BusinessException("Mã học kỳ " + code + " đã tồn tại (BR-03)");
    }

    private static BusinessException anotherOpen(String openCode) {
        String which = openCode != null ? "Học kỳ " + openCode : "Một học kỳ khác";
        return new BusinessException(which + " đang mở đăng ký. Mỗi lúc chỉ có một học kỳ mở đăng ký,"
                + " hãy đóng học kỳ đó trước (BR-26)");
    }

    private static BusinessException notFound(int id) {
        return new BusinessException("Không tìm thấy học kỳ có id " + id
                + ", có thể đã bị xóa trước đó");
    }
}
