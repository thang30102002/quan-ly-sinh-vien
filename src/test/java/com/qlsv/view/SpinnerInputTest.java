package com.qlsv.view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.text.ParseException;
import java.util.List;
import java.util.Locale;

import javax.swing.JFormattedTextField;
import javax.swing.JSpinner;

import org.junit.jupiter.api.Test;

/**
 * Ô số trên mọi form giữ chữ người dùng gõ khi mất focus (không tự trả về giá trị cũ),
 * và đặt lại form thì hiện đúng giá trị (#10).
 */
class SpinnerInputTest {

    private static JFormattedTextField field(JSpinner spinner) {
        return ((JSpinner.DefaultEditor) spinner.getEditor()).getTextField();
    }

    @Test
    void moiOSo_giuChuKhiMatFocus() {
        CoursePanel course = new CoursePanel();
        ClassSectionPanel section = new ClassSectionPanel();
        StudentPanel student = new StudentPanel();
        List<JSpinner> spinners = List.of(course.getSpnCredits(), section.getSpnMaxStudents(),
                section.getSpnStartPeriod(), section.getSpnEndPeriod(), student.getSpnIntakeYear());

        for (JSpinner s : spinners) {
            assertEquals(JFormattedTextField.COMMIT, field(s).getFocusLostBehavior());
        }
    }

    /**
     * Bộ đọc của ô là thứ Swing dùng khi ô mất focus (bấm chuột sang nút Thêm). Nó phải từ
     * chối số lẻ, chữ lẫn số, số ngoài khoảng trên mọi ngôn ngữ máy, để chữ được giữ nguyên
     * và controller báo lỗi; trước đây "3,5"/"3.5" bị chốt thành 3 và lưu luôn (#10).
     */
    @Test
    void boDocCuaO_chiNhanSoNguyenTrongKhoang_moiNgonNgu() throws Exception {
        Locale original = Locale.getDefault();
        try {
            for (Locale locale : List.of(Locale.US, Locale.forLanguageTag("vi-VN"))) {
                Locale.setDefault(locale);
                JFormattedTextField credits = field(new CoursePanel().getSpnCredits());
                JFormattedTextField.AbstractFormatter f = credits.getFormatter();

                assertEquals(3, f.stringToValue("3"), locale.toString());
                assertEquals(6, f.stringToValue(" 6 "), locale.toString());
                for (String bad : List.of("3,5", "3.5", "3a", "abc", "", "0", "7", "-1", "1.000", "99999999999")) {
                    assertThrows(ParseException.class, () -> f.stringToValue(bad), locale + " \"" + bad + "\"");
                }

                JSpinner maxStudents = new ClassSectionPanel().getSpnMaxStudents();
                UiStyles.setSpinnerValue(maxStudents, 1500);
                assertEquals("1500", field(maxStudents).getText(), locale.toString());
                assertEquals(1500, field(maxStudents).getFormatter().stringToValue("1500"));
            }
        } finally {
            Locale.setDefault(original);
        }
    }

    @Test
    void muiTenTangGiam_vanHoatDong() {
        JSpinner credits = new CoursePanel().getSpnCredits(); // 3
        credits.setValue(credits.getNextValue());
        assertEquals(4, credits.getValue());
        assertEquals("4", field(credits).getText());
        credits.setValue(6);
        assertEquals(null, credits.getNextValue()); // không vượt quá 6
    }

    @Test
    void setSpinnerValue_cungGiaTri_vanXoaChuSaiConSot() {
        JSpinner credits = new CoursePanel().getSpnCredits(); // giá trị 3
        field(credits).setText("7");                          // chữ sai còn sót sau lần bấm Thêm lỗi

        UiStyles.setSpinnerValue(credits, 3);

        assertEquals("3", field(credits).getText());
        assertEquals(3, credits.getValue());
    }

    @Test
    void clearForm_xoaChuSaiConSot_oMoiForm() {
        CoursePanel course = new CoursePanel();
        field(course.getSpnCredits()).setText("7");
        course.clearForm();
        assertEquals("3", field(course.getSpnCredits()).getText());

        ClassSectionPanel section = new ClassSectionPanel();
        field(section.getSpnMaxStudents()).setText("0");
        field(section.getSpnStartPeriod()).setText("13");
        section.clearSectionForm();
        section.clearScheduleForm();
        assertEquals("40", field(section.getSpnMaxStudents()).getText());
        assertEquals("1", field(section.getSpnStartPeriod()).getText());

        StudentPanel student = new StudentPanel();
        int year = (Integer) student.getSpnIntakeYear().getValue();
        field(student.getSpnIntakeYear()).setText("1800");
        student.clearForm();
        assertEquals(String.valueOf(year), field(student.getSpnIntakeYear()).getText());
    }
}
