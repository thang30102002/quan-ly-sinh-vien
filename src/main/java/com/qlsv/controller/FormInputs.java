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

    /**
     * Như {@link #readInt} nhưng cho ô chỉ cần số dương, có giới hạn trên chỉ để vừa cột CSDL
     * (vd sĩ số, tối đa 32767): gõ 0 hoặc số âm thì báo "phải lớn hơn 0" thay vì nêu cả khoảng
     * "từ 1 đến 32767" khó hiểu với người dùng.
     */
    static int readPositiveInt(JSpinner spinner, String fieldName, String rule) {
        try {
            return readInt(spinner, fieldName, rule);
        } catch (ValidationException e) {
            Long typed = typedNumber(spinner);
            if (typed != null && spinner.getModel() instanceof SpinnerNumberModel m) {
                if (typed < 1) {
                    throw new ValidationException(fieldName + " phải lớn hơn 0" + ruleSuffix(rule));
                }
                throw new ValidationException(fieldName + " không được vượt quá " + m.getMaximum()
                        + ruleSuffix(rule));
            }
            throw e;
        }
    }

    /** Thông báo khi ngày đúng dạng dd/MM/yyyy nhưng không có trên lịch, vd 31/02/2006. */
    static final String DATE_NOT_EXIST = "Ngày bạn nhập không tồn tại. Vui lòng kiểm tra lại.";

    /**
     * Đọc ngày dạng dd/MM/yyyy; để trống thì báo bắt buộc. Đúng dạng nhưng không có trên
     * lịch (31/02, 29/02 năm không nhuận, tháng 13) thì báo {@link #DATE_NOT_EXIST}.
     */
    static LocalDate readDate(String text, String fieldName, String rule) {
        String value = text == null ? "" : text.trim();
        if (value.isEmpty()) {
            throw new ValidationException(fieldName + " là bắt buộc" + ruleSuffix(rule));
        }
        try {
            return LocalDate.parse(value, UiStyles.DATE_FORMAT);
        } catch (DateTimeParseException e) {
            if (value.matches("\\d{2}/\\d{2}/\\d{4}")) {
                throw new ValidationException(DATE_NOT_EXIST + ruleSuffix(rule));
            }
            throw new ValidationException(fieldName + " không đúng định dạng " + UiStyles.DATE_PATTERN
                    + ", ví dụ 07/09/2026" + ruleSuffix(rule));
        }
    }

    /** Chữ người dùng gõ là số nguyên nhưng ngoài khoảng thì nêu khoảng, còn lại báo không phải số. */
    private static String invalidIntMessage(JSpinner spinner, String fieldName) {
        if (typedNumber(spinner) != null && spinner.getModel() instanceof SpinnerNumberModel m) {
            return fieldName + " phải từ " + m.getMinimum() + " đến " + m.getMaximum();
        }
        return fieldName + " phải là số nguyên";
    }

    /** @return số nguyên người dùng đang gõ trong ô (kể cả ngoài khoảng), hoặc null nếu không phải số */
    private static Long typedNumber(JSpinner spinner) {
        String typed = spinner.getEditor() instanceof JSpinner.DefaultEditor editor
                ? editor.getTextField().getText().trim() : "";
        return typed.matches("-?\\d{1,9}") ? Long.valueOf(typed) : null;
    }

    private static String ruleSuffix(String rule) {
        return rule == null ? "" : " (" + rule + ")";
    }
}
