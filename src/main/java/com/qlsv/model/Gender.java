package com.qlsv.model;

/**
 * Giới tính sinh viên. Tên hằng khớp với CHECK của cột students.gender.
 */
public enum Gender {
    MALE("Nam"),
    FEMALE("Nữ"),
    OTHER("Khác");

    private final String label;

    Gender(String label) {
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
