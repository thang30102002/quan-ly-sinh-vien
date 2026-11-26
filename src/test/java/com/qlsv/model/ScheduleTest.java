package com.qlsv.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Test {@link Schedule#overlaps(Schedule)} theo BR-22 và các ca biên ở mục 5.4 docs/yeu-cau.md.
 */
class ScheduleTest {

    private static Schedule s(int day, int start, int end) {
        return new Schedule(null, day, start, end, null);
    }

    @Test
    void overlaps_cungThuGiaoMotTiet_trung() {
        // Thứ 2 tiết 1–3 và Thứ 2 tiết 3–5: trùng ở tiết 3
        assertTrue(s(2, 1, 3).overlaps(s(2, 3, 5)));
        assertTrue(s(2, 3, 5).overlaps(s(2, 1, 3)));
    }

    @Test
    void overlaps_keNhau_khongTrung() {
        // Thứ 2 tiết 1–3 và Thứ 2 tiết 4–6: chỉ kề nhau
        assertFalse(s(2, 1, 3).overlaps(s(2, 4, 6)));
        assertFalse(s(2, 4, 6).overlaps(s(2, 1, 3)));
    }

    @Test
    void overlaps_khacThu_khongTrung() {
        assertFalse(s(2, 1, 3).overlaps(s(3, 1, 3)));
    }

    @Test
    void overlaps_bocTrongNhau_trung() {
        assertTrue(s(4, 1, 6).overlaps(s(4, 2, 3)));
        assertTrue(s(4, 2, 3).overlaps(s(4, 1, 6)));
    }

    @Test
    void overlaps_giongHetNhau_trung() {
        assertTrue(s(8, 7, 9).overlaps(s(8, 7, 9)));
    }

    @Test
    void overlaps_buoiMotTiet() {
        assertTrue(s(5, 4, 4).overlaps(s(5, 1, 4)));
        assertFalse(s(5, 5, 5).overlaps(s(5, 1, 4)));
    }

    @Test
    void overlaps_null_khongTrung() {
        assertFalse(s(2, 1, 3).overlaps(null));
    }

    @Test
    void dayLabel_chuNhatVaThuThuong() {
        assertEquals("Thứ 2", Schedule.dayLabel(2));
        assertEquals("Thứ 7", Schedule.dayLabel(7));
        assertEquals("Chủ nhật", Schedule.dayLabel(8));
    }

    @Test
    void toString_inThuTietPhong() {
        assertEquals("Thứ 2, tiết 1–3, phòng A101", new Schedule(null, 2, 1, 3, "A101").toString());
        assertEquals("Chủ nhật, tiết 7", new Schedule(null, 8, 7, 7, null).toString());
    }
}
