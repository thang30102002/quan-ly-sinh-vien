package com.qlsv.view;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

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
