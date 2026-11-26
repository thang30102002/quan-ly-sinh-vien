package com.qlsv.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.qlsv.testutil.RecordingDialogHandler;

import javax.swing.JOptionPane;

import org.junit.jupiter.api.Test;

/** Các hàm thông báo dùng chung của DialogHandler: đúng tiêu đề và kiểu hộp thoại. */
class DialogHandlerTest {

    private final RecordingDialogHandler dialog = new RecordingDialogHandler();

    @Test
    void info_warning_error_dungTieuDeVaKieu() {
        dialog.info(null, "a");
        assertEquals("Thông báo", dialog.lastTitle);
        assertEquals(JOptionPane.INFORMATION_MESSAGE, dialog.lastMessageType);

        dialog.warning(null, "b");
        assertEquals("Cảnh báo", dialog.lastTitle);
        assertEquals(JOptionPane.WARNING_MESSAGE, dialog.lastMessageType);

        dialog.error(null, "c");
        assertEquals("Lỗi", dialog.lastTitle);
        assertEquals(JOptionPane.ERROR_MESSAGE, dialog.lastMessageType);
        assertEquals("c", dialog.lastMessage);
    }

    @Test
    void withoutRuleCodes_boMaBRGiuNoiDung() {
        assertEquals("Số tín chỉ phải từ 1 đến 6",
                DialogHandler.withoutRuleCodes("Số tín chỉ phải từ 1 đến 6 (BR-18)"));
        assertEquals("Đã chuyển sang \"Thôi học\".",
                DialogHandler.withoutRuleCodes("Đã chuyển sang \"Thôi học\" (BR-14)."));
        assertEquals("Ngày bạn nhập không tồn tại. Vui lòng kiểm tra lại.",
                DialogHandler.withoutRuleCodes("Ngày bạn nhập không tồn tại. Vui lòng kiểm tra lại. (BR-17)"));
        assertEquals("Lỗi", DialogHandler.withoutRuleCodes("Lỗi (BR-01, BR-17)"));
        assertEquals("Không xóa được buổi học cuối cùng (Thứ 2, tiết 1–3): mỗi lớp phải có ít nhất 1 buổi",
                DialogHandler.withoutRuleCodes(
                        "Không xóa được buổi học cuối cùng (Thứ 2, tiết 1–3): mỗi lớp phải có ít nhất 1 buổi (BR-21)"));
        assertEquals("Sĩ số nhỏ hơn số đã đăng ký (3)",
                DialogHandler.withoutRuleCodes("Sĩ số nhỏ hơn số đã đăng ký (3) (BR-25)"));
        assertEquals("Không có mã", DialogHandler.withoutRuleCodes("Không có mã"));
    }

    @Test
    void info_warning_error_confirm_khongHienMaBR() {
        dialog.info(null, "a (BR-01)");
        dialog.warning(null, "b (BR-02)");
        dialog.error(null, "c (BR-03)");
        dialog.confirmDelete(null, "d (BR-16)?");
        assertEquals(java.util.List.of("a", "b", "c", "d?"), dialog.messages);
    }

    @Test
    void confirmDelete_chiTrueKhiChonCo() {
        assertTrue(dialog.confirmDelete(null, "Xóa?"));
        assertEquals("Xác nhận xóa", dialog.lastTitle);

        dialog.confirmResponse = JOptionPane.NO_OPTION;
        assertFalse(dialog.confirmDelete(null, "Xóa?"));

        dialog.confirmResponse = JOptionPane.CLOSED_OPTION;
        assertFalse(dialog.confirmDelete(null, "Xóa?"));
    }
}
