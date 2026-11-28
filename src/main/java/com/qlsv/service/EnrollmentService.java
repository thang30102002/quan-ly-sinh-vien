package com.qlsv.service;

import static com.qlsv.service.ServiceSupport.databaseError;
import static com.qlsv.service.ServiceSupport.isUniqueViolation;

import com.qlsv.dao.ClassSectionDAO;
import com.qlsv.dao.ClassSectionDAOImpl;
import com.qlsv.dao.EnrollmentDAO;
import com.qlsv.dao.EnrollmentDAOImpl;
import com.qlsv.dao.ScheduleDAO;
import com.qlsv.dao.ScheduleDAOImpl;
import com.qlsv.dao.StudentDAO;
import com.qlsv.dao.StudentDAOImpl;
import com.qlsv.exception.BusinessException;
import com.qlsv.exception.ValidationException;
import com.qlsv.model.ClassSection;
import com.qlsv.model.Enrollment;
import com.qlsv.model.Schedule;
import com.qlsv.model.Semester;
import com.qlsv.model.Student;
import com.qlsv.model.StudentStatus;
import com.qlsv.util.DBConnection;

import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

/**
 * Quy tắc nghiệp vụ đăng ký và hủy học phần (BR-04, BR-05, BR-07, BR-15, BR-22, BR-25, BR-26).
 */
public class EnrollmentService {

    public static final int MAX_CREDITS_PER_SEMESTER = 12;

    @FunctionalInterface
    interface ConnectionSupplier {
        Connection get() throws SQLException;
    }

    @FunctionalInterface
    private interface SqlTransaction<T> {
        T execute(Connection conn) throws SQLException;
    }

    private final StudentDAO studentDAO;
    private final ClassSectionDAO classSectionDAO;
    private final ScheduleDAO scheduleDAO;
    private final EnrollmentDAO enrollmentDAO;
    private final ConnectionSupplier connectionSupplier;

    public EnrollmentService() {
        this(new StudentDAOImpl(), new ClassSectionDAOImpl(), new ScheduleDAOImpl(),
                new EnrollmentDAOImpl(), DBConnection::getConnection);
    }

    public EnrollmentService(StudentDAO studentDAO, ClassSectionDAO classSectionDAO,
                             ScheduleDAO scheduleDAO, EnrollmentDAO enrollmentDAO) {
        this(studentDAO, classSectionDAO, scheduleDAO, enrollmentDAO,
                EnrollmentService::createNoOpConnection);
    }

    EnrollmentService(StudentDAO studentDAO, ClassSectionDAO classSectionDAO,
                      ScheduleDAO scheduleDAO, EnrollmentDAO enrollmentDAO,
                      ConnectionSupplier connectionSupplier) {
        this.studentDAO = Objects.requireNonNull(studentDAO, "studentDAO");
        this.classSectionDAO = Objects.requireNonNull(classSectionDAO, "classSectionDAO");
        this.scheduleDAO = Objects.requireNonNull(scheduleDAO, "scheduleDAO");
        this.enrollmentDAO = Objects.requireNonNull(enrollmentDAO, "enrollmentDAO");
        this.connectionSupplier = Objects.requireNonNull(connectionSupplier, "connectionSupplier");
    }

    public Enrollment register(int studentId, int sectionId) {
        Student student = findStudent(studentId);
        if (student.getStatus() != StudentStatus.STUDYING) {
            throw new BusinessException("Chỉ sinh viên đang học mới được đăng ký học phần (trạng thái hiện tại: "
                    + student.getStatus() + ") (BR-07)");
        }

        ClassSection section = findSection(sectionId);
        Semester semester = section.getSemester();
        if (semester == null || !semester.isOpen()) {
            String code = semester != null ? semester.getSemesterCode() : "";
            throw new BusinessException("Học kỳ " + code + " hiện không mở đăng ký (BR-26)");
        }

        return runInTransaction("đăng ký học phần", conn -> {
            List<Enrollment> currentEnrollments =
                    enrollmentDAO.findByStudentAndSemester(conn, studentId, semester.getId());
            requireCourseNotRegistered(currentEnrollments, section, semester);
            requireCreditsWithinLimit(currentEnrollments, section, semester);
            requireSectionHasSeat(conn, section);
            requireNoScheduleConflict(conn, studentId, section);

            Enrollment enrollment = new Enrollment(student, section);
            enrollmentDAO.register(conn, enrollment);
            return enrollment;
        });
    }

    public void cancel(int enrollmentId) {
        runInTransaction("hủy đăng ký học phần", conn -> {
            Enrollment existing = enrollmentDAO.findById(conn, enrollmentId);
            if (existing == null) {
                throw new BusinessException("Không tìm thấy lượt đăng ký có id " + enrollmentId);
            }
            Semester semester = existing.getSection() != null ? existing.getSection().getSemester() : null;
            if (semester == null || !semester.isOpen()) {
                throw new BusinessException("Chỉ được hủy đăng ký trong học kỳ đang mở (BR-26)");
            }
            if (hasAnyGrade(existing)) {
                throw new BusinessException("Không thể hủy đăng ký học phần đã có điểm (BR-15)");
            }
            if (!enrollmentDAO.cancel(conn, enrollmentId)) {
                throw new BusinessException("Không tìm thấy lượt đăng ký có id " + enrollmentId);
            }
            return null;
        });
    }

    public List<Enrollment> findBySemester(int semesterId) {
        try {
            return enrollmentDAO.findBySemester(semesterId);
        } catch (SQLException e) {
            throw databaseError("tải danh sách đăng ký học kỳ", e);
        }
    }

    public List<Enrollment> findByStudentAndSemester(int studentId, int semesterId) {
        try {
            return enrollmentDAO.findByStudentAndSemester(studentId, semesterId);
        } catch (SQLException e) {
            throw databaseError("tải danh sách đăng ký của sinh viên", e);
        }
    }

    public List<Enrollment> findBySection(int sectionId) {
        try {
            return enrollmentDAO.findBySection(sectionId);
        } catch (SQLException e) {
            throw databaseError("tải danh sách sinh viên của lớp học phần", e);
        }
    }

    public int countBySection(int sectionId) {
        try {
            return enrollmentDAO.countBySection(sectionId);
        } catch (SQLException e) {
            throw databaseError("đếm sĩ số lớp học phần", e);
        }
    }

    public List<Schedule> findSchedulesByStudentAndSemester(int studentId, int semesterId) {
        try {
            return enrollmentDAO.findSchedulesByStudentAndSemester(studentId, semesterId);
        } catch (SQLException e) {
            throw databaseError("tải thời khóa biểu sinh viên", e);
        }
    }

    private Student findStudent(int studentId) {
        try {
            Student student = studentDAO.findById(studentId);
            if (student == null) {
                throw new BusinessException("Không tìm thấy sinh viên có id " + studentId);
            }
            return student;
        } catch (SQLException e) {
            throw databaseError("tìm sinh viên", e);
        }
    }

    private ClassSection findSection(int sectionId) {
        try {
            ClassSection section = classSectionDAO.findById(sectionId);
            if (section == null) {
                throw new BusinessException("Không tìm thấy lớp học phần có id " + sectionId);
            }
            return section;
        } catch (SQLException e) {
            throw databaseError("tìm lớp học phần", e);
        }
    }

    private static void requireCourseNotRegistered(List<Enrollment> currentEnrollments,
                                                   ClassSection targetSection,
                                                   Semester semester) {
        Integer targetCourseId = targetSection.getCourse().getId();
        for (Enrollment existing : currentEnrollments) {
            ClassSection existingSection = existing.getSection();
            if (existingSection.getCourse().getId().equals(targetCourseId)) {
                if (existingSection.getId().equals(targetSection.getId())) {
                    throw new BusinessException("Sinh viên đã đăng ký lớp " + targetSection.getSectionCode()
                            + " trong học kỳ " + semester.getSemesterCode() + " (BR-05)");
                }
                throw new BusinessException("Sinh viên đã đăng ký học phần "
                        + targetSection.getCourse().getCourseCode()
                        + " ở lớp " + existingSection.getSectionCode()
                        + " trong học kỳ " + semester.getSemesterCode() + " (BR-05)");
            }
        }
    }

    private static void requireCreditsWithinLimit(List<Enrollment> currentEnrollments,
                                                  ClassSection targetSection,
                                                  Semester semester) {
        int currentCredits = 0;
        for (Enrollment existing : currentEnrollments) {
            currentCredits += existing.getSection().getCourse().getCredits();
        }
        int addingCredits = targetSection.getCourse().getCredits();
        if (currentCredits + addingCredits > MAX_CREDITS_PER_SEMESTER) {
            throw new BusinessException("Vượt quá " + MAX_CREDITS_PER_SEMESTER
                    + " tín chỉ trong học kỳ " + semester.getSemesterCode()
                    + " (hiện có " + currentCredits + ", thêm " + addingCredits + ") (BR-04)");
        }
    }

    private void requireSectionHasSeat(Connection conn, ClassSection section) throws SQLException {
        int currentCount = enrollmentDAO.countBySection(conn, section.getId());
        if (currentCount >= section.getMaxStudents()) {
            throw new BusinessException("Lớp học phần " + section.getSectionCode()
                    + " đã đủ sĩ số (" + currentCount + "/" + section.getMaxStudents() + ") (BR-25)");
        }
    }

    private void requireNoScheduleConflict(Connection conn, int studentId, ClassSection targetSection)
            throws SQLException {
        List<Schedule> targetSchedules = scheduleDAO.findBySection(targetSection.getId());
        if (targetSchedules.isEmpty()) {
            return;
        }
        List<Schedule> studentSchedules = enrollmentDAO.findSchedulesByStudentAndSemester(
                conn, studentId, targetSection.getSemester().getId());
        for (Schedule target : targetSchedules) {
            for (Schedule existing : studentSchedules) {
                if (target.overlaps(existing)) {
                    String sectionCode = existing.getSection() != null
                            && existing.getSection().getSectionCode() != null
                            ? existing.getSection().getSectionCode()
                            : "#" + existing.getSection().getId();
                    throw new BusinessException("Trùng lịch với lớp " + sectionCode
                            + ": " + existing + " (BR-22)");
                }
            }
        }
    }

    private static boolean hasAnyGrade(Enrollment enrollment) {
        return enrollment.getAttendance() != null
                || enrollment.getMidterm() != null
                || enrollment.getFinalExam() != null
                || enrollment.getTotal() != null;
    }

    private <T> T runInTransaction(String action, SqlTransaction<T> work) {
        try (Connection conn = connectionSupplier.get()) {
            conn.setAutoCommit(false);
            try {
                T result = work.execute(conn);
                conn.commit();
                return result;
            } catch (BusinessException | ValidationException e) {
                rollbackQuietly(conn, e);
                throw e;
            } catch (SQLException e) {
                rollbackQuietly(conn, e);
                if (isUniqueViolation(e)) {
                    throw new BusinessException("Sinh viên đã đăng ký lớp học phần này (BR-05)", e);
                }
                throw databaseError(action, e);
            } catch (RuntimeException e) {
                rollbackQuietly(conn, e);
                throw e;
            } finally {
                restoreAutoCommitQuietly(conn);
            }
        } catch (SQLException e) {
            throw databaseError(action, e);
        }
    }

    private static void rollbackQuietly(Connection conn, Throwable primary) {
        try {
            conn.rollback();
        } catch (SQLException e) {
            primary.addSuppressed(e);
        }
    }

    private static void restoreAutoCommitQuietly(Connection conn) {
        try {
            conn.setAutoCommit(true);
        } catch (SQLException ignored) {
        }
    }

    private static Connection createNoOpConnection() {
        return (Connection) Proxy.newProxyInstance(
                Connection.class.getClassLoader(),
                new Class<?>[]{Connection.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "getAutoCommit" -> true;
                    case "isClosed" -> false;
                    case "equals" -> args != null && args.length == 1 && proxy == args[0];
                    case "hashCode" -> System.identityHashCode(proxy);
                    case "toString" -> "NoOpConnection";
                    default -> null;
                });
    }
}
