package com.qlsv.service;

import static com.qlsv.service.ServiceSupport.databaseError;
import static com.qlsv.service.ServiceSupport.isForeignKeyViolation;
import static com.qlsv.service.ServiceSupport.isUniqueViolation;
import static com.qlsv.service.ServiceSupport.trimToNull;

import com.qlsv.dao.ClassSectionDAO;
import com.qlsv.dao.ClassSectionDAOImpl;
import com.qlsv.exception.BusinessException;
import com.qlsv.exception.ValidationException;
import com.qlsv.model.ClassSection;
import com.qlsv.util.Validator;

import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

/**
 * Quy tắc nghiệp vụ của lớp học phần (BR-16, BR-24, BR-25). Một học phần mở được
 * nhiều lớp trong cùng học kỳ, mỗi lớp có mã riêng.
 * Không chứa câu SQL nào: mọi truy cập CSDL đi qua {@link ClassSectionDAO}.
 */
public class ClassSectionService {

    static final int LECTURER_MAX_LENGTH = 100;

    /** Cột max_students là SMALLINT. */
    static final int MAX_STUDENTS_LIMIT = Short.MAX_VALUE;

    private final ClassSectionDAO sectionDAO;

    public ClassSectionService() {
        this(new ClassSectionDAOImpl());
    }

    /** Cho phép truyền DAO khác, ví dụ DAO giả trong test. */
    public ClassSectionService(ClassSectionDAO sectionDAO) {
        this.sectionDAO = Objects.requireNonNull(sectionDAO, "sectionDAO");
    }

    /**
     * Mở lớp học phần mới. Buổi học thêm sau qua {@link ScheduleService}.
     *
     * @throws ValidationException dữ liệu sai định dạng hoặc thiếu (BR-24, BR-25)
     * @throws BusinessException   mã lớp đã có trong học kỳ (BR-24) hoặc lỗi CSDL
     */
    public void add(ClassSection section) {
        requireSection(section);
        normalize(section);
        validate(section);

        try {
            if (sectionDAO.findByCode(section.getSemester().getId(), section.getSectionCode()) != null) {
                throw duplicateCode(section);
            }
            sectionDAO.insert(section);
        } catch (SQLException e) {
            if (isUniqueViolation(e)) {
                throw duplicateCode(section);
            }
            throw databaseError("thêm lớp học phần", e);
        }
    }

    /**
     * Cập nhật lớp học phần. Lớp đã có sinh viên đăng ký thì không đổi được học phần,
     * học kỳ (BR-16) và không hạ sĩ số tối đa xuống dưới số đã đăng ký (BR-25).
     *
     * @throws ValidationException dữ liệu sai định dạng hoặc thiếu
     * @throws BusinessException   không tìm thấy, trùng mã hoặc vi phạm điều kiện trên
     */
    public void update(ClassSection section) {
        requireSection(section);
        if (section.getId() == null) {
            throw new ValidationException("Chưa chọn lớp học phần cần sửa");
        }
        normalize(section);
        validate(section);

        try {
            ClassSection current = sectionDAO.findById(section.getId());
            if (current == null) {
                throw notFound(section.getId());
            }
            ClassSection sameCode = sectionDAO.findByCode(section.getSemester().getId(), section.getSectionCode());
            if (sameCode != null && !sameCode.getId().equals(section.getId())) {
                throw duplicateCode(section);
            }
            int enrolled = sectionDAO.countEnrollments(section.getId());
            if (enrolled > 0) {
                boolean courseChanged = !current.getCourse().getId().equals(section.getCourse().getId());
                boolean semesterChanged = !current.getSemester().getId().equals(section.getSemester().getId());
                if (courseChanged || semesterChanged) {
                    throw new BusinessException("Không đổi được học phần hoặc học kỳ của lớp "
                            + current.getSectionCode() + " vì đã có " + enrolled + " sinh viên đăng ký (BR-16)");
                }
                if (section.getMaxStudents() < enrolled) {
                    throw new BusinessException("Sĩ số tối đa " + section.getMaxStudents()
                            + " nhỏ hơn số sinh viên đã đăng ký lớp " + current.getSectionCode()
                            + " (" + enrolled + ") (BR-25)");
                }
            }
            if (!sectionDAO.update(section)) {
                throw notFound(section.getId());
            }
        } catch (SQLException e) {
            if (isUniqueViolation(e)) {
                throw duplicateCode(section);
            }
            throw databaseError("cập nhật lớp học phần", e);
        }
    }

    /**
     * Xóa lớp học phần cùng các buổi học của lớp. Không xóa lớp đã có sinh viên
     * đăng ký (BR-16).
     *
     * @throws BusinessException không tìm thấy, vi phạm BR-16 hoặc lỗi CSDL
     */
    public void delete(int id) {
        try {
            ClassSection current = sectionDAO.findById(id);
            if (current == null) {
                throw notFound(id);
            }
            int enrolled = sectionDAO.countEnrollments(id);
            if (enrolled > 0) {
                throw new BusinessException("Không xóa được lớp " + current.getSectionCode()
                        + " vì đã có " + enrolled + " sinh viên đăng ký (BR-16)");
            }
            if (!sectionDAO.delete(id)) {
                throw notFound(id);
            }
        } catch (SQLException e) {
            if (isForeignKeyViolation(e)) {
                throw new BusinessException("Không xóa được lớp học phần vì đã có sinh viên đăng ký (BR-16)", e);
            }
            throw databaseError("xóa lớp học phần", e);
        }
    }

    /** @return lớp học phần có id này, hoặc null nếu không có */
    public ClassSection findById(int id) {
        try {
            return sectionDAO.findById(id);
        } catch (SQLException e) {
            throw databaseError("tìm lớp học phần", e);
        }
    }

    /** @return các lớp học phần mở trong học kỳ, sắp theo mã lớp */
    public List<ClassSection> findBySemester(int semesterId) {
        try {
            return sectionDAO.findBySemester(semesterId);
        } catch (SQLException e) {
            throw databaseError("tải danh sách lớp học phần", e);
        }
    }

    /** @return số sinh viên đã đăng ký lớp */
    public int countEnrollments(int sectionId) {
        try {
            return sectionDAO.countEnrollments(sectionId);
        } catch (SQLException e) {
            throw databaseError("đếm số sinh viên đăng ký", e);
        }
    }

    private static void requireSection(ClassSection section) {
        if (section == null) {
            throw new ValidationException("Chưa nhập thông tin lớp học phần");
        }
    }

    private static void normalize(ClassSection s) {
        if (s.getSectionCode() != null) {
            s.setSectionCode(s.getSectionCode().trim().toUpperCase());
        }
        s.setLecturerName(trimToNull(s.getLecturerName()));
    }

    private static void validate(ClassSection s) {
        if (s.getCourse() == null || s.getCourse().getId() == null) {
            throw new ValidationException("Chưa chọn học phần cho lớp");
        }
        if (s.getSemester() == null || s.getSemester().getId() == null) {
            throw new ValidationException("Chưa chọn học kỳ cho lớp");
        }
        Validator.requireClassSectionCode(s.getSectionCode());
        String prefix = s.getCourse().getCourseCode() + "-";
        if (!s.getSectionCode().startsWith(prefix)) {
            throw new ValidationException("Mã lớp " + s.getSectionCode() + " phải bắt đầu bằng mã học phần "
                    + s.getCourse().getCourseCode() + ", ví dụ " + prefix + "01 (BR-24)");
        }
        if (s.getMaxStudents() <= 0) {
            throw new ValidationException("Sĩ số tối đa phải là số nguyên lớn hơn 0 (BR-25)");
        }
        if (s.getMaxStudents() > MAX_STUDENTS_LIMIT) {
            throw new ValidationException("Sĩ số tối đa không được vượt quá " + MAX_STUDENTS_LIMIT + " (BR-25)");
        }
        if (s.getLecturerName() != null && s.getLecturerName().length() > LECTURER_MAX_LENGTH) {
            throw new ValidationException("Tên giảng viên tối đa " + LECTURER_MAX_LENGTH + " ký tự");
        }
    }

    private static BusinessException duplicateCode(ClassSection s) {
        String semesterCode = s.getSemester().getSemesterCode();
        return new BusinessException("Mã lớp " + s.getSectionCode() + " đã tồn tại trong học kỳ "
                + (semesterCode != null ? semesterCode : "này") + " (BR-24)");
    }

    private static BusinessException notFound(int id) {
        return new BusinessException("Không tìm thấy lớp học phần có id " + id
                + ", có thể đã bị xóa trước đó");
    }
}
