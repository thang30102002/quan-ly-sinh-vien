package com.qlsv.testutil;

import com.qlsv.dao.ClassSectionDAO;
import com.qlsv.model.ClassSection;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** DAO giả: giả lập UNIQUE (semester_id, section_code). */
public class FakeClassSectionDAO implements ClassSectionDAO {
    public final Map<Integer, ClassSection> byId = new LinkedHashMap<>();
    public final Map<Integer, Integer> enrollments = new HashMap<>();
    private int nextId = 1;

    private static ClassSection copy(ClassSection s) {
        return new ClassSection(s.getId(), s.getSectionCode(), s.getCourse(), s.getSemester(),
                s.getLecturerName(), s.getMaxStudents());
    }

    private void checkUnique(ClassSection s) throws SQLException {
        for (ClassSection other : byId.values()) {
            if (!other.getId().equals(s.getId())
                    && other.getSemester().getId().equals(s.getSemester().getId())
                    && other.getSectionCode().equals(s.getSectionCode())) {
                throw new SQLException("duplicate key uq_class_sections_code", "23505");
            }
        }
    }

    @Override
    public void insert(ClassSection section) throws SQLException {
        checkUnique(section);
        section.setId(nextId++);
        byId.put(section.getId(), copy(section));
    }

    @Override
    public boolean update(ClassSection section) throws SQLException {
        if (!byId.containsKey(section.getId())) {
            return false;
        }
        checkUnique(section);
        byId.put(section.getId(), copy(section));
        return true;
    }

    @Override
    public boolean delete(int id) {
        return byId.remove(id) != null;
    }

    @Override
    public ClassSection findById(int id) {
        ClassSection s = byId.get(id);
        return s != null ? copy(s) : null;
    }

    @Override
    public ClassSection findByCode(int semesterId, String sectionCode) {
        String code = sectionCode.trim().toUpperCase();
        return byId.values().stream()
                .filter(s -> s.getSemester().getId() == semesterId && s.getSectionCode().equals(code))
                .findFirst().map(FakeClassSectionDAO::copy).orElse(null);
    }

    @Override
    public List<ClassSection> findBySemester(int semesterId) {
        List<ClassSection> list = new ArrayList<>();
        byId.values().stream().filter(s -> s.getSemester().getId() == semesterId)
                .forEach(s -> list.add(copy(s)));
        list.sort(Comparator.comparing(ClassSection::getSectionCode));
        return list;
    }

    @Override
    public int countEnrollments(int sectionId) {
        return enrollments.getOrDefault(sectionId, 0);
    }
}
