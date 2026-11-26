package com.qlsv.controller;

import com.qlsv.exception.ValidationException;
import com.qlsv.view.UiStyles;

import java.text.ParseException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

import javax.swing.JSpinner;

/**
 * Đọc giá trị từ ô nhập của form, lỗi thì ném {@link ValidationException} tiếng Việt.
 */
final class FormInputs {

    private FormInputs() {
    }

    /** Lấy số nguyên từ JSpinner, chốt cả giá trị người dùng vừa gõ mà chưa nhấn Enter. */
    static int readInt(JSpinner spinner, String fieldName) {
        try {
            spinner.commitEdit();
        } catch (ParseException e) {
            throw new ValidationException(fieldName + " phải là số nguyên");
        }
        if (spinner.getValue() instanceof Number n) {
            return n.intValue();
        }
        throw new ValidationException(fieldName + " phải là số nguyên");
    }

    /** Đọc ngày dạng dd/MM/yyyy; để trống thì báo bắt buộc. */
    static LocalDate readDate(String text, String fieldName) {
        String value = text == null ? "" : text.trim();
        if (value.isEmpty()) {
            throw new ValidationException(fieldName + " là bắt buộc");
        }
        try {
            return LocalDate.parse(value, UiStyles.DATE_FORMAT);
        } catch (DateTimeParseException e) {
            throw new ValidationException(fieldName + " không đúng định dạng " + UiStyles.DATE_PATTERN
                    + ", ví dụ 07/09/2026");
        }
    }
}
