package com.qlsv.model;

/**
 * Trạng thái sinh viên (BR-20). Tên hằng khớp với CHECK của cột students.status.
 */
public enum StudentStatus {
    STUDYING("Đang học"),
    RESERVED("Bảo lưu"),
    DROPPED("Thôi học"),
    GRADUATED("Tốt nghiệp");

    private final String label;

    StudentStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    @Override
    public String toString() {
        return label;
    }
}
