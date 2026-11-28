package com.qlsv.testutil;

import com.qlsv.dao.StudentDAO;
import com.qlsv.model.Student;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class FakeStudentDAO implements StudentDAO {

    public final Map<Integer, Student> byId = new LinkedHashMap<>();
    public final Set<Integer> gradedStudentIds = new HashSet<>();
    private int nextId = 1;

    private static Student copy(Student s) {
        return new Student(s.getId(), s.getStudentCode(), s.getFullName(), s.getDob(),
                s.getGender(), s.getEmail(), s.getPhone(), s.getClassName(),
                s.getIntakeYear(), s.getStatus());
    }

    @Override
    public void insert(Student student) throws SQLException {
        for (Student other : byId.values()) {
            if (other.getStudentCode().equals(student.getStudentCode())) {
                throw new SQLException("duplicate key uq_students_code", "23505");
            }
        }
        student.setId(nextId++);
        byId.put(student.getId(), copy(student));
    }

    @Override
    public boolean update(Student student) throws SQLException {
        if (!byId.containsKey(student.getId())) {
            return false;
        }
        byId.put(student.getId(), copy(student));
        return true;
    }

    @Override
    public boolean delete(int id) {
        return byId.remove(id) != null;
    }

    @Override
    public Student findById(int id) {
        Student s = byId.get(id);
        return s != null ? copy(s) : null;
    }

    @Override
    public Student findByCode(String studentCode) {
        String code = studentCode == null ? "" : studentCode.trim().toUpperCase();
        return byId.values().stream()
                .filter(s -> s.getStudentCode().equals(code))
                .findFirst().map(FakeStudentDAO::copy).orElse(null);
    }

    @Override
    public List<Student> findAll() {
        List<Student> list = new ArrayList<>();
        byId.values().forEach(s -> list.add(copy(s)));
        list.sort(Comparator.comparing(Student::getStudentCode));
        return list;
    }

    @Override
    public List<Student> search(String keyword, String className) {
        return findAll();
    }

    @Override
    public boolean hasGrades(int studentId) {
        return gradedStudentIds.contains(studentId);
    }
}
