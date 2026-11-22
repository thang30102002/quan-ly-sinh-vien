package com.qlsv.service;

import com.qlsv.exception.BusinessException;

import java.sql.SQLException;

/**
 * Hàm dùng chung cho các Service học phần, học kỳ, lớp học phần, buổi học.
 */
final class ServiceSupport {

    /** Mã lỗi PostgreSQL khi vi phạm ràng buộc UNIQUE. */
    static final String SQLSTATE_UNIQUE_VIOLATION = "23505";

    /** Mã lỗi PostgreSQL khi vi phạm khóa ngoại (vd xóa dòng đang được tham chiếu). */
    static final String SQLSTATE_FOREIGN_KEY_VIOLATION = "23503";

    private ServiceSupport() {
    }

    static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    static boolean isUniqueViolation(SQLException e) {
        return SQLSTATE_UNIQUE_VIOLATION.equals(e.getSQLState());
    }

    static boolean isForeignKeyViolation(SQLException e) {
        return SQLSTATE_FOREIGN_KEY_VIOLATION.equals(e.getSQLState());
    }

    /** Thông báo lỗi của PostgreSQL có tên ràng buộc, dùng để biết UNIQUE nào bị vi phạm. */
    static boolean mentions(SQLException e, String constraintName) {
        return e.getMessage() != null && e.getMessage().contains(constraintName);
    }

    static BusinessException databaseError(String action, SQLException e) {
        return new BusinessException("Lỗi CSDL khi " + action + ": " + e.getMessage(), e);
    }
}
