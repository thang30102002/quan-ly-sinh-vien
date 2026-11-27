package com.qlsv.controller;

import com.qlsv.exception.ValidationException;
import com.qlsv.view.UiStyles;

import java.math.BigInteger;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.regex.Pattern;

import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

/**
 * Đọc giá trị từ ô nhập của form, lỗi thì ném {@link ValidationException} tiếng Việt.
 * Tham số {@code rule} là mã quy tắc nghiệp vụ (vd "BR-18") ghi vào cuối thông báo,
 * truyền null nếu không có.
 */
final class FormInputs {

    /** Thông báo khi còn ô bắt buộc (nhãn có dấu *) để trống. */
    static final String REQUIRED_FIELDS_MISSING =
            "Vui lòng điền đầy đủ các thông tin bắt buộc * trước khi tiếp tục";

    private FormInputs() {
    }

    /**
     * Báo {@link #REQUIRED_FIELDS_MISSING} nếu có ô bắt buộc để trống (chỉ có dấu cách
     * cũng tính là trống). Gọi trước khi đọc, kiểm tra định dạng từng ô.
     *
     * @param texts chữ trong các ô bắt buộc; ô số thì truyền {@link #text(JSpinner)}
     */
    static void requireFilled(String... texts) {
        for (String t : texts) {
            if (t == null || t.isBlank()) {
                throw new ValidationException(REQUIRED_FIELDS_MISSING);
            }
        }
    }

    /** @return chữ đang có trong ô số (kể cả chưa nhấn Enter) */
    static String text(JSpinner spinner) {
        return typedText(spinner);
    }

    /** Chữ người dùng gõ phải là số nguyên viết liền, không dấu chấm, dấu phẩy. */
    private static final Pattern INTEGER_TEXT = Pattern.compile("-?\\d+");

    /**
     * Lấy số nguyên từ JSpinner, chốt cả giá trị người dùng vừa gõ mà chưa nhấn Enter.
     * <ul>
     * <li>Không phải số nguyên ("3.5", "3,5", "abc", để trống): báo "… không hợp lệ".
     *     Kiểm tra chữ trước khi để Swing đọc vì Swing đọc theo ngôn ngữ máy: máy tiếng Anh
     *     đọc "3.5", máy tiếng Việt đọc "3,5" thành 3 rồi lặng lẽ bỏ phần lẻ (#10).</li>
     * <li>Số nguyên ngoài khoảng của spinner: báo đúng khoảng cho phép.</li>
     * </ul>
     */
    static int readInt(JSpinner spinner, String fieldName, String rule) {
        String typed = typedText(spinner);
        if (!INTEGER_TEXT.matcher(typed).matches()) {
            throw new ValidationException(fieldName + " không hợp lệ" + ruleSuffix(rule));
        }
        // Swing không đọc được " 5 " (dấu cách thừa) nên ghi lại chữ đã bỏ khoảng trắng
        if (spinner.getEditor() instanceof JSpinner.DefaultEditor editor
                && !editor.getTextField().getText().equals(typed)) {
            editor.getTextField().setText(typed);
        }
        try {
            spinner.commitEdit();
        } catch (ParseException e) {
            throw new ValidationException(outOfRangeMessage(spinner, fieldName) + ruleSuffix(rule));
        }
        if (spinner.getValue() instanceof Number n) {
            return n.intValue();
        }
        throw new ValidationException(fieldName + " không hợp lệ" + ruleSuffix(rule));
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
            String typed = typedText(spinner);
            if (INTEGER_TEXT.matcher(typed).matches() && spinner.getModel() instanceof SpinnerNumberModel m) {
                if (new BigInteger(typed).signum() <= 0) {
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

    /** Năm hợp lý cho mọi ô ngày của ứng dụng (ngày sinh, ngày học kỳ). */
    static final int MIN_YEAR = 1900;
    static final int MAX_YEAR = 2100;

    /** Đúng dạng dd/MM/yyyy: 2 chữ số ngày, 2 chữ số tháng, 4 chữ số năm. */
    private static final Pattern DATE_TEXT = Pattern.compile("\\d{2}/\\d{2}/\\d{4}");

    /**
     * Đọc ngày dạng dd/MM/yyyy.
     * <ul>
     * <li>Để trống: báo bắt buộc.</li>
     * <li>Sai dạng (vd 2006-05-01, 1/5/2006, năm 5 chữ số): báo định dạng.</li>
     * <li>Đúng dạng nhưng không có trên lịch (31/02, 29/02 năm không nhuận, tháng 13):
     *     báo {@link #DATE_NOT_EXIST}.</li>
     * <li>Năm ngoài {@value #MIN_YEAR}–{@value #MAX_YEAR} (vd 01/01/0000): báo khoảng năm.</li>
     * </ul>
     */
    static LocalDate readDate(String text, String fieldName, String rule) {
        String value = text == null ? "" : text.trim();
        if (value.isEmpty()) {
            throw new ValidationException(fieldName + " là bắt buộc" + ruleSuffix(rule));
        }
        if (!DATE_TEXT.matcher(value).matches()) {
            throw new ValidationException(fieldName + " không đúng định dạng " + UiStyles.DATE_PATTERN
                    + ", ví dụ 07/09/2026" + ruleSuffix(rule));
        }
        LocalDate date;
        try {
            date = LocalDate.parse(value, UiStyles.DATE_FORMAT);
        } catch (DateTimeParseException e) {
            throw new ValidationException(DATE_NOT_EXIST + ruleSuffix(rule));
        }
        if (date.getYear() < MIN_YEAR || date.getYear() > MAX_YEAR) {
            throw new ValidationException(fieldName + " phải có năm từ " + MIN_YEAR + " đến " + MAX_YEAR
                    + ruleSuffix(rule));
        }
        return date;
    }

    /** Số nguyên người dùng gõ nằm ngoài khoảng của spinner: nêu khoảng cho phép. */
    private static String outOfRangeMessage(JSpinner spinner, String fieldName) {
        if (spinner.getModel() instanceof SpinnerNumberModel m) {
            return fieldName + " phải từ " + m.getMinimum() + " đến " + m.getMaximum();
        }
        return fieldName + " không hợp lệ";
    }

    /** @return chữ đang có trong ô (kể cả chưa nhấn Enter), đã bỏ khoảng trắng hai đầu */
    private static String typedText(JSpinner spinner) {
        return spinner.getEditor() instanceof JSpinner.DefaultEditor editor
                ? editor.getTextField().getText().trim() : "";
    }

    private static String ruleSuffix(String rule) {
        return rule == null ? "" : " (" + rule + ")";
    }
}
