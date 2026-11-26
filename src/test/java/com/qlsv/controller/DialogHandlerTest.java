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
    void confirmDelete_chiTrueKhiChonCo() {
        assertTrue(dialog.confirmDelete(null, "Xóa?"));
        assertEquals("Xác nhận xóa", dialog.lastTitle);

        dialog.confirmResponse = JOptionPane.NO_OPTION;
        assertFalse(dialog.confirmDelete(null, "Xóa?"));

        dialog.confirmResponse = JOptionPane.CLOSED_OPTION;
        assertFalse(dialog.confirmDelete(null, "Xóa?"));
    }
}
