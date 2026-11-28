package com.qlsv.testutil;

import com.qlsv.dao.SemesterDAO;
import com.qlsv.model.Semester;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** DAO giả: giả lập UNIQUE semester_code và partial unique index uq_semesters_open. */
public class FakeSemesterDAO implements SemesterDAO {
    public final Map<Integer, Semester> byId = new LinkedHashMap<>();
    public final Set<Integer> withSections = new HashSet<>();
    public final Set<Integer> withEnrollments = new HashSet<>();
    private int nextId = 1;

    private static Semester copy(Semester s) {
        return new Semester(s.getId(), s.getSemesterCode(), s.getName(), s.getStartDate(),
                s.getEndDate(), s.isOpen());
    }

    private void checkConstraints(Semester s) throws SQLException {
        for (Semester other : byId.values()) {
            if (other.getId().equals(s.getId())) {
                continue;
            }
            if (other.getSemesterCode().equals(s.getSemesterCode())) {
                throw new SQLException("duplicate key uq_semesters_code", "23505");
            }
            if (s.isOpen() && other.isOpen()) {
                throw new SQLException("duplicate key uq_semesters_open", "23505");
            }
        }
    }

    @Override
    public void insert(Semester semester) throws SQLException {
        checkConstraints(semester);
        semester.setId(nextId++);
        byId.put(semester.getId(), copy(semester));
    }

    @Override
    public boolean update(Semester semester) throws SQLException {
        if (!byId.containsKey(semester.getId())) {
            return false;
        }
        checkConstraints(semester);
        byId.put(semester.getId(), copy(semester));
        return true;
    }

    @Override
    public boolean delete(int id) {
        return byId.remove(id) != null;
    }

    @Override
    public Semester findById(int id) {
        Semester s = byId.get(id);
        return s != null ? copy(s) : null;
    }

    @Override
    public Semester findByCode(String semesterCode) {
        return byId.values().stream().filter(s -> s.getSemesterCode().equals(semesterCode.trim()))
                .findFirst().map(FakeSemesterDAO::copy).orElse(null);
    }

    @Override
    public List<Semester> findAll() {
        List<Semester> list = new ArrayList<>();
        byId.values().forEach(s -> list.add(copy(s)));
        list.sort(Comparator.comparing(Semester::getStartDate).reversed());
        return list;
    }

    @Override
    public Semester findOpen() {
        return byId.values().stream().filter(Semester::isOpen)
                .findFirst().map(FakeSemesterDAO::copy).orElse(null);
    }

    @Override
    public boolean hasSections(int semesterId) {
        return withSections.contains(semesterId);
    }

    @Override
    public boolean hasEnrollments(int semesterId) {
        return withEnrollments.contains(semesterId);
    }
}
