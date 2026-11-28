package com.qlsv.testutil;

import com.qlsv.dao.CourseDAO;
import com.qlsv.model.Course;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/** DAO giả: lưu trong LinkedHashMap, giả lập UNIQUE trên course_code. */
public class FakeCourseDAO implements CourseDAO {
    public final Map<Integer, Course> byId = new LinkedHashMap<>();
    public final Set<Integer> withSections = new HashSet<>();
    public final Set<Integer> withEnrollments = new HashSet<>();
    public boolean failNext;
    private int nextId = 1;

    private void maybeFail() throws SQLException {
        if (failNext) {
            failNext = false;
            throw new SQLException("mất kết nối");
        }
    }

    private static Course copy(Course c) {
        return new Course(c.getId(), c.getCourseCode(), c.getName(), c.getCredits(), c.getDescription());
    }

    @Override
    public void insert(Course course) throws SQLException {
        maybeFail();
        if (findByCode(course.getCourseCode()) != null) {
            throw new SQLException("duplicate key", "23505");
        }
        course.setId(nextId++);
        byId.put(course.getId(), copy(course));
    }

    @Override
    public boolean update(Course course) throws SQLException {
        maybeFail();
        if (!byId.containsKey(course.getId())) {
            return false;
        }
        byId.put(course.getId(), copy(course));
        return true;
    }

    @Override
    public boolean delete(int id) throws SQLException {
        maybeFail();
        return byId.remove(id) != null;
    }

    @Override
    public Course findById(int id) throws SQLException {
        maybeFail();
        Course c = byId.get(id);
        return c != null ? copy(c) : null;
    }

    @Override
    public Course findByCode(String courseCode) throws SQLException {
        maybeFail();
        if (courseCode == null) {
            return null;
        }
        String code = courseCode.trim().toUpperCase(Locale.ROOT);
        return byId.values().stream().filter(c -> c.getCourseCode().equals(code))
                .findFirst().map(FakeCourseDAO::copy).orElse(null);
    }

    @Override
    public List<Course> findAll() throws SQLException {
        maybeFail();
        List<Course> list = new ArrayList<>();
        byId.values().forEach(c -> list.add(copy(c)));
        list.sort(Comparator.comparing(Course::getCourseCode));
        return list;
    }

    @Override
    public List<Course> search(String keyword) throws SQLException {
        List<Course> all = findAll();
        if (keyword == null || keyword.isBlank()) {
            return all;
        }
        String kw = keyword.trim().toLowerCase(Locale.ROOT);
        all.removeIf(c -> !c.getCourseCode().toLowerCase(Locale.ROOT).contains(kw)
                && !c.getName().toLowerCase(Locale.ROOT).contains(kw));
        return all;
    }

    @Override
    public boolean hasSections(int courseId) throws SQLException {
        maybeFail();
        return withSections.contains(courseId);
    }

    @Override
    public boolean hasEnrollments(int courseId) throws SQLException {
        maybeFail();
        return withEnrollments.contains(courseId);
    }
}
