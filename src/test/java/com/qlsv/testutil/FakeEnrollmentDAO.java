package com.qlsv.testutil;

import com.qlsv.dao.EnrollmentDAO;
import com.qlsv.model.Enrollment;
import com.qlsv.model.Schedule;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class FakeEnrollmentDAO implements EnrollmentDAO {

    public final Map<Integer, Enrollment> byId = new LinkedHashMap<>();
    private final FakeScheduleDAO scheduleDAO;
    private int nextId = 1;
    public SQLException failOnRegister;

    public FakeEnrollmentDAO(FakeScheduleDAO scheduleDAO) {
        this.scheduleDAO = scheduleDAO;
    }

    private static Enrollment copy(Enrollment e) {
        return new Enrollment(e.getId(), e.getStudent(), e.getSection(), e.getRegisteredAt(),
                e.getAttendance(), e.getMidterm(), e.getFinalExam(),
                e.getTotal(), e.getLetter(), e.getGpa4());
    }

    @Override
    public void register(Enrollment enrollment) throws SQLException {
        if (failOnRegister != null) {
            throw failOnRegister;
        }
        for (Enrollment other : byId.values()) {
            if (other.getStudent().getId().equals(enrollment.getStudent().getId())
                    && other.getSection().getId().equals(enrollment.getSection().getId())) {
                throw new SQLException("duplicate key uq_enrollments", "23505");
            }
        }
        enrollment.setId(nextId++);
        byId.put(enrollment.getId(), copy(enrollment));
    }

    @Override
    public void register(Connection conn, Enrollment enrollment) throws SQLException {
        register(enrollment);
    }

    @Override
    public boolean cancel(int enrollmentId) {
        return byId.remove(enrollmentId) != null;
    }

    @Override
    public boolean cancel(Connection conn, int enrollmentId) {
        return cancel(enrollmentId);
    }

    @Override
    public Enrollment findById(int id) {
        Enrollment e = byId.get(id);
        return e != null ? copy(e) : null;
    }

    @Override
    public Enrollment findById(Connection conn, int id) {
        return findById(id);
    }

    @Override
    public List<Enrollment> findBySemester(int semesterId) {
        List<Enrollment> result = new ArrayList<>();
        byId.values().stream()
                .filter(e -> e.getSection().getSemester().getId().equals(semesterId))
                .forEach(e -> result.add(copy(e)));
        result.sort(Comparator.comparing((Enrollment e) -> e.getStudent().getStudentCode())
                .thenComparing(e -> e.getSection().getSectionCode()));
        return result;
    }

    @Override
    public List<Enrollment> findBySemester(Connection conn, int semesterId) {
        return findBySemester(semesterId);
    }

    @Override
    public List<Enrollment> findByStudentAndSemester(int studentId, int semesterId) {
        List<Enrollment> result = new ArrayList<>();
        byId.values().stream()
                .filter(e -> e.getStudent().getId().equals(studentId)
                        && e.getSection().getSemester().getId().equals(semesterId))
                .forEach(e -> result.add(copy(e)));
        result.sort(Comparator.comparing(e -> e.getSection().getSectionCode()));
        return result;
    }

    @Override
    public List<Enrollment> findByStudentAndSemester(Connection conn, int studentId, int semesterId) {
        return findByStudentAndSemester(studentId, semesterId);
    }

    @Override
    public List<Enrollment> findBySection(int sectionId) {
        List<Enrollment> result = new ArrayList<>();
        byId.values().stream()
                .filter(e -> e.getSection().getId().equals(sectionId))
                .forEach(e -> result.add(copy(e)));
        result.sort(Comparator.comparing(e -> e.getStudent().getStudentCode()));
        return result;
    }

    @Override
    public List<Enrollment> findBySection(Connection conn, int sectionId) {
        return findBySection(sectionId);
    }

    @Override
    public int countBySection(int sectionId) {
        return (int) byId.values().stream()
                .filter(e -> e.getSection().getId().equals(sectionId))
                .count();
    }

    @Override
    public int countBySection(Connection conn, int sectionId) {
        return countBySection(sectionId);
    }

    @Override
    public List<Schedule> findSchedulesByStudentAndSemester(int studentId, int semesterId) {
        List<Schedule> result = new ArrayList<>();
        for (Enrollment e : findByStudentAndSemester(studentId, semesterId)) {
            for (Schedule s : scheduleDAO.findBySection(e.getSection().getId())) {
                result.add(new Schedule(s.getId(), e.getSection(), s.getDayOfWeek(),
                        s.getStartPeriod(), s.getEndPeriod(), s.getRoom()));
            }
        }
        result.sort(Comparator.comparingInt(Schedule::getDayOfWeek)
                .thenComparingInt(Schedule::getStartPeriod));
        return result;
    }

    @Override
    public List<Schedule> findSchedulesByStudentAndSemester(Connection conn, int studentId, int semesterId) {
        return findSchedulesByStudentAndSemester(studentId, semesterId);
    }
}
