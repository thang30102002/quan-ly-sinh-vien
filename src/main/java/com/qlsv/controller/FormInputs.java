package com.qlsv.controller;

import com.qlsv.exception.ValidationException;
import com.qlsv.view.UiStyles;

import java.text.ParseException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

/**
 * Đọc giá trị từ ô nhập của form, lỗi thì ném {@link ValidationException} tiếng Việt.
 * Tham số {@code rule} là mã quy tắc nghiệp vụ (vd "BR-18") ghi vào cuối thông báo,
 * truyền null nếu không có.
 */
final class FormInputs {

    private FormInputs() {
    }

    /**
     * Lấy số nguyên từ JSpinner, chốt cả giá trị người dùng vừa gõ mà chưa nhấn Enter.
     * Gõ số nằm ngoài khoảng của spinner thì báo đúng khoảng cho phép, không báo
     * "phải là số nguyên".
     */
    static int readInt(JSpinner spinner, String fieldName, String rule) {
        try {
            spinner.commitEdit();
        } catch (ParseException e) {
            throw new ValidationException(invalidIntMessage(spinner, fieldName) + ruleSuffix(rule));
        }
        if (spinner.getValue() instanceof Number n) {
            return n.intValue();
        }
        throw new ValidationException(fieldName + " phải là số nguyên" + ruleSuffix(rule));
    }

    /** Đọc ngày dạng dd/MM/yyyy; để trống thì báo bắt buộc. */
    static LocalDate readDate(String text, String fieldName, String rule) {
        String value = text == null ? "" : text.trim();
        if (value.isEmpty()) {
            throw new ValidationException(fieldName + " là bắt buộc" + ruleSuffix(rule));
        }
        try {
            return LocalDate.parse(value, UiStyles.DATE_FORMAT);
        } catch (DateTimeParseException e) {
            throw new ValidationException(fieldName + " không đúng định dạng " + UiStyles.DATE_PATTERN
                    + ", ví dụ 07/09/2026" + ruleSuffix(rule));
        }
    }

    /** Chữ người dùng gõ là số nguyên nhưng ngoài khoảng thì nêu khoảng, còn lại báo không phải số. */
    private static String invalidIntMessage(JSpinner spinner, String fieldName) {
        String typed = spinner.getEditor() instanceof JSpinner.DefaultEditor editor
                ? editor.getTextField().getText().trim() : "";
        if (typed.matches("-?\\d{1,9}") && spinner.getModel() instanceof SpinnerNumberModel m) {
            return fieldName + " phải từ " + m.getMinimum() + " đến " + m.getMaximum();
        }
        return fieldName + " phải là số nguyên";
    }

    private static String ruleSuffix(String rule) {
        return rule == null ? "" : " (" + rule + ")";
    }
}
