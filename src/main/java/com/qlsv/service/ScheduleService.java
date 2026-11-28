package com.qlsv.service;

import static com.qlsv.service.ServiceSupport.databaseError;
import static com.qlsv.service.ServiceSupport.isForeignKeyViolation;
import static com.qlsv.service.ServiceSupport.isUniqueViolation;
import static com.qlsv.service.ServiceSupport.trimToNull;

import com.qlsv.dao.EnrollmentDAO;
import com.qlsv.dao.EnrollmentDAOImpl;
import com.qlsv.dao.ScheduleDAO;
import com.qlsv.dao.ScheduleDAOImpl;
import com.qlsv.exception.BusinessException;
import com.qlsv.exception.ValidationException;
import com.qlsv.model.Enrollment;
import com.qlsv.model.Schedule;
import com.qlsv.model.Student;

import java.sql.SQLException;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Quy tắc nghiệp vụ của buổi học trong thời khóa biểu lớp học phần (BR-21, BR-23).
 * Lớp mới mở có thể chưa có buổi nào; khi đã có thì không xóa được buổi cuối cùng,
 * nên lớp luôn còn ít nhất 1 buổi.
 * Không chứa câu SQL nào: mọi truy cập CSDL đi qua {@link ScheduleDAO} và {@link EnrollmentDAO}.
 */
public class ScheduleService {

    public static final int MIN_DAY = 2;      // Thứ 2
    public static final int MAX_DAY = 8;      // Chủ nhật
    public static final int MIN_PERIOD = 1;
    public static final int MAX_PERIOD = 12;
    static final int ROOM_MAX_LENGTH = 20;

    private final ScheduleDAO scheduleDAO;
    private final EnrollmentDAO enrollmentDAO;

    public ScheduleService() {
        this(new ScheduleDAOImpl(), new EnrollmentDAOImpl());
    }

    /** Cho phép truyền DAO khác, ví dụ DAO giả trong test. */
    public ScheduleService(ScheduleDAO scheduleDAO) {
        this(scheduleDAO, null);
    }

    public ScheduleService(ScheduleDAO scheduleDAO, EnrollmentDAO enrollmentDAO) {
        this.scheduleDAO = Objects.requireNonNull(scheduleDAO, "scheduleDAO");
        this.enrollmentDAO = enrollmentDAO;
    }

    /**
     * Thêm buổi học cho lớp học phần.
     *
     * @throws ValidationException thứ, tiết sai hoặc thiếu lớp (BR-21)
     * @throws BusinessException   trùng với buổi khác của cùng lớp (BR-21), trùng lịch sinh viên (BR-23) hoặc lỗi CSDL
     */
    public void add(Schedule schedule) {
        requireSchedule(schedule);
        normalize(schedule);
        validate(schedule);

        try {
            requireNoOverlap(schedule);
            requireNoStudentScheduleConflict(schedule);
            scheduleDAO.insert(schedule);
        } catch (SQLException e) {
            throw translate(e, "thêm buổi học");
        }
    }

    /**
     * Sửa buổi học. Không chuyển buổi học sang lớp khác.
     *
     * @throws ValidationException thứ, tiết sai hoặc thiếu dữ liệu (BR-21)
     * @throws BusinessException   không tìm thấy, đổi lớp, trùng buổi khác (BR-21), trùng lịch sinh viên (BR-23) hoặc lỗi CSDL
     */
    public void update(Schedule schedule) {
        requireSchedule(schedule);
        if (schedule.getId() == null) {
            throw new ValidationException("Chưa chọn buổi học cần sửa");
        }
        normalize(schedule);
        validate(schedule);

        try {
            Schedule current = scheduleDAO.findById(schedule.getId());
            if (current == null) {
                throw notFound(schedule.getId());
            }
            if (!current.getSection().getId().equals(schedule.getSection().getId())) {
                throw new BusinessException("Không chuyển được buổi học sang lớp học phần khác");
            }
            requireNoOverlap(schedule);
            requireNoStudentScheduleConflict(schedule);
            if (!scheduleDAO.update(schedule)) {
                throw notFound(schedule.getId());
            }
        } catch (SQLException e) {
            throw translate(e, "cập nhật buổi học");
        }
    }

    /**
     * Xóa buổi học. Không xóa buổi cuối cùng của lớp (BR-21: mỗi lớp ít nhất 1 buổi);
     * muốn bỏ hết lịch thì xóa cả lớp học phần.
     *
     * @throws BusinessException không tìm thấy, là buổi cuối cùng hoặc lỗi CSDL
     */
    public void delete(int id) {
        try {
            Schedule current = scheduleDAO.findById(id);
            if (current == null) {
                throw notFound(id);
            }
            if (scheduleDAO.findBySection(current.getSection().getId()).size() <= 1) {
                throw new BusinessException("Không xóa được buổi học cuối cùng (" + current
                        + "): mỗi lớp học phần phải có ít nhất 1 buổi học (BR-21)");
            }
            if (!scheduleDAO.delete(id)) {
                throw notFound(id);
            }
        } catch (SQLException e) {
            throw databaseError("xóa buổi học", e);
        }
    }

    /** @return các buổi học của lớp, sắp theo thứ rồi tiết bắt đầu */
    public List<Schedule> findBySection(int sectionId) {
        try {
            return scheduleDAO.findBySection(sectionId);
        } catch (SQLException e) {
            throw databaseError("tải thời khóa biểu của lớp", e);
        }
    }

    /** BR-21: các buổi của cùng một lớp không được trùng nhau (bỏ qua chính buổi đang sửa). */
    private void requireNoOverlap(Schedule schedule) throws SQLException {
        for (Schedule other : scheduleDAO.findBySection(schedule.getSection().getId())) {
            if (other.getId().equals(schedule.getId())) {
                continue;
            }
            if (schedule.overlaps(other)) {
                throw new BusinessException("Buổi học " + describe(schedule)
                        + " trùng với buổi đã có của lớp: " + other + " (BR-21)");
            }
        }
    }

    private void requireNoStudentScheduleConflict(Schedule schedule) throws SQLException {
        if (enrollmentDAO == null) {
            return;
        }
        int sectionId = schedule.getSection().getId();
        List<Enrollment> enrollments = enrollmentDAO.findBySection(sectionId);
        if (enrollments.isEmpty()) {
            return;
        }
        Integer semesterId = resolveSemesterId(schedule, enrollments);
        if (semesterId == null) {
            return;
        }
        Set<String> conflictedStudents = new LinkedHashSet<>();
        for (Enrollment enrollment : enrollments) {
            Student student = enrollment.getStudent();
            if (student == null || student.getId() == null) {
                continue;
            }
            List<Schedule> studentSchedules =
                    enrollmentDAO.findSchedulesByStudentAndSemester(student.getId(), semesterId);
            for (Schedule other : studentSchedules) {
                if (other.getSection() != null
                        && Objects.equals(other.getSection().getId(), sectionId)) {
                    continue;
                }
                if (schedule.overlaps(other)) {
                    conflictedStudents.add(formatStudent(student));
                    break;
                }
            }
        }
        if (!conflictedStudents.isEmpty()) {
            throw new BusinessException("Buổi học " + describe(schedule)
                    + " làm trùng lịch của sinh viên: "
                    + String.join(", ", conflictedStudents) + " (BR-23)");
        }
    }

    private static Integer resolveSemesterId(Schedule schedule, List<Enrollment> enrollments) {
        if (schedule.getSection().getSemester() != null
                && schedule.getSection().getSemester().getId() != null) {
            return schedule.getSection().getSemester().getId();
        }
        for (Enrollment enrollment : enrollments) {
            if (enrollment.getSection() != null
                    && enrollment.getSection().getSemester() != null
                    && enrollment.getSection().getSemester().getId() != null) {
                return enrollment.getSection().getSemester().getId();
            }
        }
        return null;
    }

    private static String formatStudent(Student student) {
        if (student.getFullName() != null && !student.getFullName().isBlank()) {
            return student.getStudentCode() + " - " + student.getFullName();
        }
        return student.getStudentCode() != null ? student.getStudentCode() : "#" + student.getId();
    }

    private static void requireSchedule(Schedule schedule) {
        if (schedule == null) {
            throw new ValidationException("Chưa nhập thông tin buổi học");
        }
    }

    private static void normalize(Schedule s) {
        s.setRoom(trimToNull(s.getRoom()));
    }

    private static void validate(Schedule s) {
        if (s.getSection() == null || s.getSection().getId() == null) {
            throw new ValidationException("Chưa chọn lớp học phần cho buổi học");
        }
        if (s.getDayOfWeek() < MIN_DAY || s.getDayOfWeek() > MAX_DAY) {
            throw new ValidationException("Thứ phải từ Thứ 2 đến Chủ nhật (BR-21)");
        }
        if (s.getStartPeriod() < MIN_PERIOD || s.getStartPeriod() > MAX_PERIOD
                || s.getEndPeriod() < MIN_PERIOD || s.getEndPeriod() > MAX_PERIOD) {
            throw new ValidationException("Tiết học phải từ " + MIN_PERIOD + " đến " + MAX_PERIOD + " (BR-21)");
        }
        if (s.getStartPeriod() > s.getEndPeriod()) {
            throw new ValidationException("Tiết bắt đầu (" + s.getStartPeriod()
                    + ") không được lớn hơn tiết kết thúc (" + s.getEndPeriod() + ") (BR-21)");
        }
        if (s.getRoom() != null && s.getRoom().length() > ROOM_MAX_LENGTH) {
            throw new ValidationException("Phòng học tối đa " + ROOM_MAX_LENGTH + " ký tự");
        }
    }

    /** "Thứ 2, tiết 1–3" (không kèm phòng) để thông báo ngắn gọn. */
    private static String describe(Schedule s) {
        String periods = s.getStartPeriod() == s.getEndPeriod()
                ? "tiết " + s.getStartPeriod()
                : "tiết " + s.getStartPeriod() + "–" + s.getEndPeriod();
        return Schedule.dayLabel(s.getDayOfWeek()) + ", " + periods;
    }

    private static BusinessException translate(SQLException e, String action) {
        if (isUniqueViolation(e)) {
            // uq_schedules_slot: cùng lớp, cùng thứ, cùng tiết bắt đầu (hai người thao tác cùng lúc)
            return new BusinessException("Lớp đã có buổi học bắt đầu cùng thứ và cùng tiết (BR-21)", e);
        }
        if (isForeignKeyViolation(e)) {
            return new BusinessException("Lớp học phần không còn tồn tại, có thể đã bị xóa", e);
        }
        return databaseError(action, e);
    }

    private static BusinessException notFound(int id) {
        return new BusinessException("Không tìm thấy buổi học có id " + id
                + ", có thể đã bị xóa trước đó");
    }
}
