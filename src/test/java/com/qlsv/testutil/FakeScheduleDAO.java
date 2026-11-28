package com.qlsv.testutil;

import com.qlsv.dao.ScheduleDAO;
import com.qlsv.model.ClassSection;
import com.qlsv.model.Schedule;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** DAO giả: giả lập UNIQUE (section_id, day_of_week, start_period). */
public class FakeScheduleDAO implements ScheduleDAO {
    public final Map<Integer, Schedule> byId = new LinkedHashMap<>();
    private int nextId = 1;

    private static Schedule copy(Schedule s) {
        ClassSection section = new ClassSection();
        section.setId(s.getSection().getId());
        return new Schedule(s.getId(), section, s.getDayOfWeek(), s.getStartPeriod(),
                s.getEndPeriod(), s.getRoom());
    }

    private void checkUnique(Schedule s) throws SQLException {
        for (Schedule other : byId.values()) {
            if (!other.getId().equals(s.getId())
                    && other.getSection().getId().equals(s.getSection().getId())
                    && other.getDayOfWeek() == s.getDayOfWeek()
                    && other.getStartPeriod() == s.getStartPeriod()) {
                throw new SQLException("duplicate key uq_schedules_slot", "23505");
            }
        }
    }

    @Override
    public void insert(Schedule schedule) throws SQLException {
        checkUnique(schedule);
        schedule.setId(nextId++);
        byId.put(schedule.getId(), copy(schedule));
    }

    @Override
    public boolean update(Schedule schedule) throws SQLException {
        if (!byId.containsKey(schedule.getId())) {
            return false;
        }
        checkUnique(schedule);
        byId.put(schedule.getId(), copy(schedule));
        return true;
    }

    @Override
    public boolean delete(int id) {
        return byId.remove(id) != null;
    }

    @Override
    public Schedule findById(int id) {
        Schedule s = byId.get(id);
        return s != null ? copy(s) : null;
    }

    @Override
    public List<Schedule> findBySection(int sectionId) {
        List<Schedule> list = new ArrayList<>();
        byId.values().stream().filter(s -> s.getSection().getId() == sectionId)
                .forEach(s -> list.add(copy(s)));
        list.sort(Comparator.comparingInt(Schedule::getDayOfWeek)
                .thenComparingInt(Schedule::getStartPeriod));
        return list;
    }
}
