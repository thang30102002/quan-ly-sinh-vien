package com.qlsv.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.qlsv.exception.ValidationException;

import java.time.LocalDate;

import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class FormInputsTest {

    private JSpinner credits;

    @BeforeEach
    void setUp() {
        credits = new JSpinner(new SpinnerNumberModel(3, 1, 6, 1));
    }

    private void type(String text) {
        ((JSpinner.DefaultEditor) credits.getEditor()).getTextField().setText(text);
    }

    @Test
    void readInt_layGiaTriVuaGoChuaNhanEnter() {
        type("5");
        assertEquals(5, FormInputs.readInt(credits, "Số tín chỉ", "BR-18"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "7", "-1", "100"})
    void readInt_soNgoaiKhoang_baoKhoangChoPhep(String typed) {
        type(typed);
        ValidationException ex = assertThrows(ValidationException.class,
                () -> FormInputs.readInt(credits, "Số tín chỉ", "BR-18"));
        assertEquals("Số tín chỉ phải từ 1 đến 6 (BR-18)", ex.getMessage());
    }

    @ParameterizedTest
    @ValueSource(strings = {"abc", "3.5", "", "99999999999"})
    void readInt_khongPhaiSoNguyen_baoPhaiLaSoNguyen(String typed) {
        type(typed);
        ValidationException ex = assertThrows(ValidationException.class,
                () -> FormInputs.readInt(credits, "Số tín chỉ", null));
        assertEquals("Số tín chỉ phải là số nguyên", ex.getMessage());
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
