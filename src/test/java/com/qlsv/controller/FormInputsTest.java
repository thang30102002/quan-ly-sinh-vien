package com.qlsv.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.qlsv.exception.ValidationException;
import com.qlsv.view.UiStyles;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Các ca đọc số chạy dưới cả tiếng Anh (như container Maven) và tiếng Việt (như máy dev):
 * Swing đọc số theo ngôn ngữ máy, nên một ca chỉ chạy dưới một ngôn ngữ đã làm lọt lỗi
 * "3,5" được lưu thành 3 (#10).
 */
class FormInputsTest {

    private static final List<Locale> LOCALES = List.of(Locale.US, Locale.forLanguageTag("vi-VN"));
    private static final Locale ORIGINAL = Locale.getDefault();

    @AfterEach
    void restoreLocale() {
        Locale.setDefault(ORIGINAL);
    }

    /** Ô số giống trên form thật (UiStyles.setupNumberSpinner), đã gõ sẵn {@code typed}. */
    private static JSpinner spinner(int value, int min, int max, String typed) {
        JSpinner s = new JSpinner(new SpinnerNumberModel(value, min, max, 1));
        UiStyles.setupNumberSpinner(s);
        ((JSpinner.DefaultEditor) s.getEditor()).getTextField().setText(typed);
        return s;
    }

    private static JSpinner credits(String typed) {
        return spinner(3, 1, 6, typed);
    }

    private static JSpinner maxStudents(String typed) {
        return spinner(40, 1, Short.MAX_VALUE, typed);
    }

    private static String readIntError(JSpinner s, String field, String rule) {
        return assertThrows(ValidationException.class, () -> FormInputs.readInt(s, field, rule)).getMessage();
    }

    private static String readPositiveIntError(JSpinner s, String field, String rule) {
        return assertThrows(ValidationException.class,
                () -> FormInputs.readPositiveInt(s, field, rule)).getMessage();
    }

    @Test
    void readInt_layGiaTriVuaGoChuaNhanEnter() {
        for (Locale locale : LOCALES) {
            Locale.setDefault(locale);
            assertEquals(5, FormInputs.readInt(credits("5"), "Số tín chỉ", "BR-18"), locale.toString());
            assertEquals(5, FormInputs.readInt(credits(" 5 "), "Số tín chỉ", "BR-18"), locale.toString());
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "7", "-1", "100", "99999999999"})
    void readInt_soNguyenNgoaiKhoang_baoKhoangChoPhep(String typed) {
        for (Locale locale : LOCALES) {
            Locale.setDefault(locale);
            assertEquals("Số tín chỉ phải từ 1 đến 6 (BR-18)",
                    readIntError(credits(typed), "Số tín chỉ", "BR-18"), locale.toString());
        }
    }

    /** Lỗi tìm được khi chạy test trong Docker: "3.5" (máy tiếng Anh), "3,5" (máy tiếng Việt) lưu thành 3. */
    @ParameterizedTest
    @ValueSource(strings = {"3.5", "3,5", "2.0", "1.000", "1,000", "abc", "", "  ", "3a", "+3", "1e1"})
    void readInt_khongPhaiSoNguyen_baoKhongHopLe(String typed) {
        for (Locale locale : LOCALES) {
            Locale.setDefault(locale);
            assertEquals("Số tín chỉ không hợp lệ",
                    readIntError(credits(typed), "Số tín chỉ", null), locale + " gõ \"" + typed + "\"");
        }
    }

    @Test
    void oSo_hienSoLienKhongDauPhanCach_vaDocLaiDuoc() {
        for (Locale locale : LOCALES) {
            Locale.setDefault(locale);
            JSpinner s = maxStudents("");
            UiStyles.setSpinnerValue(s, 1500);
            String shown = ((JSpinner.DefaultEditor) s.getEditor()).getTextField().getText();
            assertEquals("1500", shown, locale.toString());
            assertEquals(1500, FormInputs.readPositiveInt(s, "Sĩ số tối đa", "BR-25"), locale.toString());
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "-5", "-99999999999"})
    void readPositiveInt_khongDuong_baoLonHon0(String typed) {
        for (Locale locale : LOCALES) {
            Locale.setDefault(locale);
            assertEquals("Sĩ số tối đa phải lớn hơn 0 (BR-25)",
                    readPositiveIntError(maxStudents(typed), "Sĩ số tối đa", "BR-25"), locale.toString());
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"40000", "99999999999"})
    void readPositiveInt_vuotGioiHan_baoGioiHan(String typed) {
        for (Locale locale : LOCALES) {
            Locale.setDefault(locale);
            assertEquals("Sĩ số tối đa không được vượt quá 32767",
                    readPositiveIntError(maxStudents(typed), "Sĩ số tối đa", null), locale.toString());
        }
    }

    @Test
    void readPositiveInt_hopLe_vaKhongPhaiSoNguyen() {
        for (Locale locale : LOCALES) {
            Locale.setDefault(locale);
            assertEquals(55, FormInputs.readPositiveInt(maxStudents("55"), "Sĩ số tối đa", null));
            assertEquals("Sĩ số tối đa không hợp lệ",
                    readPositiveIntError(maxStudents("40,5"), "Sĩ số tối đa", null), locale.toString());
            assertEquals("Sĩ số tối đa không hợp lệ",
                    readPositiveIntError(maxStudents("40.5"), "Sĩ số tối đa", null), locale.toString());
        }
    }

    @Test
    void readDate_dungDinhDang() {
        assertEquals(LocalDate.of(2006, 5, 1), FormInputs.readDate(" 01/05/2006 ", "Ngày sinh", "BR-17"));
    }

    @Test
    void readDate_ngayNhuanHopLe() {
        assertEquals(LocalDate.of(2024, 2, 29), FormInputs.readDate("29/02/2024", "Ngày sinh", "BR-17"));
    }

    @Test
    void readDate_trong_baoBatBuoc() {
        ValidationException ex = assertThrows(ValidationException.class,
                () -> FormInputs.readDate("  ", "Ngày sinh", "BR-17"));
        assertEquals("Ngày sinh là bắt buộc (BR-17)", ex.getMessage());
    }

    /**
     * Bug B4: 31/02/2006, 29/02/2025 từng được chấp nhận và tự đổi thành ngày cuối tháng.
     * Đúng dạng dd/MM/yyyy nhưng không có trên lịch thì báo ngày không tồn tại.
     */
    @ParameterizedTest
    @ValueSource(strings = {"31/02/2006", "29/02/2025", "31/04/2026", "00/05/2006", "15/13/2006", " 30/02/2024 "})
    void readDate_ngayKhongTonTai_baoKhongTonTai(String text) {
        ValidationException ex = assertThrows(ValidationException.class,
                () -> FormInputs.readDate(text, "Ngày sinh", "BR-17"));
        assertEquals("Ngày bạn nhập không tồn tại. Vui lòng kiểm tra lại.",
                DialogHandler.withoutRuleCodes(ex.getMessage()));
    }

    @ParameterizedTest
    @ValueSource(strings = {"2006-05-01", "1/5/2006", "01/05/06", "abc", "01-05-2006"})
    void readDate_saiDinhDang_baoDinhDang(String text) {
        ValidationException ex = assertThrows(ValidationException.class,
                () -> FormInputs.readDate(text, "Ngày sinh", "BR-17"));
        assertEquals("Ngày sinh không đúng định dạng dd/MM/yyyy, ví dụ 07/09/2026 (BR-17)", ex.getMessage());
    }
}
