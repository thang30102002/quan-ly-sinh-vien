package com.qlsv.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.qlsv.model.Course;
import com.qlsv.service.CourseService;
import com.qlsv.testutil.FakeCourseDAO;
import com.qlsv.testutil.RecordingDialogHandler;
import com.qlsv.view.CoursePanel;

import javax.swing.JOptionPane;
import javax.swing.JSpinner;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test luồng giao diện học phần (#10): CourseController với DAO giả, không cần CSDL.
 */
class CourseControllerTest {

    private FakeCourseDAO dao;
    private CoursePanel panel;
    private CourseController controller;
    private RecordingDialogHandler dialog;
    private Course it102;

    @BeforeEach
    void setUp() {
        dao = new FakeCourseDAO();
        CourseService service = new CourseService(dao);
        service.add(new Course("IT101", "Nhập môn lập trình", 3, null));
        it102 = new Course("IT102", "Cấu trúc dữ liệu", 3, null);
        service.add(it102);

        panel = new CoursePanel();
        controller = new CourseController(panel, service);
        dialog = new RecordingDialogHandler();
        controller.setDialogHandler(dialog);
        controller.loadCourses();
    }

    @AfterEach
    void khongHienMaBR() {
        dialog.assertNoRuleCodes();
    }

    private void selectRow(int row) {
        panel.getTblCourses().setRowSelectionInterval(row, row);
    }

    @Test
    void chonDong_dienForm() {
        selectRow(1);

        assertEquals(it102.getId(), controller.getSelectedId());
        assertEquals("IT102", panel.getTxtCourseCode().getText());
        assertEquals("Cấu trúc dữ liệu", panel.getTxtName().getText());
    }

    @Test
    void onAdd_thanhCong_xoaFormVaTaiLaiBang() {
        panel.getTxtCourseCode().setText(" it201 ");
        panel.getTxtName().setText("Cơ sở dữ liệu");
        panel.getSpnCredits().setValue(4);

        controller.onAdd();

        assertEquals("Thêm học phần thành công!", dialog.lastMessage);
        assertEquals(DialogHandler.TITLE_INFO, dialog.lastTitle);
        assertEquals(3, controller.getTableModel().getRowCount());
        assertTrue(dao.byId.values().stream().anyMatch(c -> c.getCourseCode().equals("IT201")));
        assertEquals("", panel.getTxtCourseCode().getText());
    }

    /**
     * Bug B3: gõ 7 vào ô tín chỉ (khoảng 1–6) bị báo "phải là số nguyên" thay vì báo khoảng
     * cho phép theo BR-18. Chữ "7" còn trong ô vì ô giữ chữ khi mất focus
     * (UiStyles.keepTypedTextOnFocusLost).
     */
    @Test
    void onAdd_goTinChiNgoaiKhoang_baoDungKhoangBR18() {
        panel.getTxtCourseCode().setText("IT201");
        panel.getTxtName().setText("Cơ sở dữ liệu");
        ((JSpinner.DefaultEditor) panel.getSpnCredits().getEditor()).getTextField().setText("7");

        controller.onAdd();

        assertEquals(JOptionPane.ERROR_MESSAGE, dialog.lastMessageType);
        assertEquals("Số tín chỉ phải từ 1 đến 6", dialog.lastMessage);
        assertEquals(2, dao.byId.size());
    }

    /** Bug B2: sửa xong bảng tải lại làm mất dòng đang chọn, bấm Sửa lần hai bị báo "chưa chọn". */
    @Test
    void onUpdate_thanhCong_vanChonDongVuaSua_suaTiepDuoc() {
        selectRow(1);
        panel.getTxtName().setText("Cấu trúc dữ liệu và giải thuật");

        controller.onUpdate();

        assertEquals("Cập nhật học phần thành công!", dialog.lastMessage);
        assertEquals(it102.getId(), controller.getSelectedId());
        Course selected = controller.getTableModel().getSelectedItem(panel.getTblCourses());
        assertNotNull(selected);
        assertEquals("Cấu trúc dữ liệu và giải thuật", selected.getName());

        panel.getSpnCredits().setValue(4);
        controller.onUpdate();

        assertEquals("Cập nhật học phần thành công!", dialog.lastMessage);
        assertEquals(4, dao.byId.get(it102.getId()).getCredits());
    }

    @Test
    void onUpdate_chuaChon_canhBao() {
        controller.onUpdate();

        assertEquals(JOptionPane.WARNING_MESSAGE, dialog.lastMessageType);
        assertEquals(DialogHandler.TITLE_WARNING, dialog.lastTitle);
    }

    @Test
    void onDelete_tuChoiXacNhan_khongXoa() {
        selectRow(0);
        dialog.confirmResponse = JOptionPane.NO_OPTION;

        controller.onDelete();

        assertEquals(DialogHandler.TITLE_CONFIRM_DELETE, dialog.lastTitle);
        assertEquals(2, dao.byId.size());
    }

    @Test
    void onDelete_daMoLop_baoLoiBR16() {
        dao.withSections.add(it102.getId());
        selectRow(1);

        controller.onDelete();

        assertEquals(JOptionPane.ERROR_MESSAGE, dialog.lastMessageType);
        assertEquals("Không xóa được học phần IT102 vì đã mở lớp học phần,"
                + " hãy xóa các lớp của học phần này trước", dialog.lastMessage);
        assertNotNull(dao.byId.get(it102.getId()));
    }

    @Test
    void onDelete_xacNhan_xoaVaBoChon() {
        selectRow(1);

        controller.onDelete();

        assertEquals("Đã xóa học phần IT102.", dialog.lastMessage);
        assertNull(dao.byId.get(it102.getId()));
        assertNull(controller.getSelectedId());
        assertEquals(1, controller.getTableModel().getRowCount());
    }

    @Test
    void onSearch_kyTuDacBiet_khongLoi() {
        panel.getTxtSearch().setText("cấu trúc");
        controller.onSearch();
        assertEquals(1, controller.getTableModel().getRowCount());

        panel.getTxtSearch().setText("%_");
        controller.onSearch();
        assertEquals(0, controller.getTableModel().getRowCount());
    }
}
