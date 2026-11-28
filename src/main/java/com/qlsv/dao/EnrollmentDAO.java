package com.qlsv.dao;

import com.qlsv.model.Enrollment;
import com.qlsv.model.Schedule;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * Truy cập bảng enrollments và lịch học đã đăng ký của sinh viên.
 */
public interface EnrollmentDAO {

    void register(Enrollment enrollment) throws SQLException;

    void register(Connection conn, Enrollment enrollment) throws SQLException;

    boolean cancel(int enrollmentId) throws SQLException;

    boolean cancel(Connection conn, int enrollmentId) throws SQLException;

    Enrollment findById(int id) throws SQLException;

    Enrollment findById(Connection conn, int id) throws SQLException;

    List<Enrollment> findBySemester(int semesterId) throws SQLException;

    List<Enrollment> findBySemester(Connection conn, int semesterId) throws SQLException;

    List<Enrollment> findByStudentAndSemester(int studentId, int semesterId) throws SQLException;

    List<Enrollment> findByStudentAndSemester(Connection conn, int studentId, int semesterId) throws SQLException;

    List<Enrollment> findBySection(int sectionId) throws SQLException;

    List<Enrollment> findBySection(Connection conn, int sectionId) throws SQLException;

    int countBySection(int sectionId) throws SQLException;

    int countBySection(Connection conn, int sectionId) throws SQLException;

    List<Schedule> findSchedulesByStudentAndSemester(int studentId, int semesterId) throws SQLException;

    List<Schedule> findSchedulesByStudentAndSemester(Connection conn, int studentId, int semesterId) throws SQLException;
}
