package com.qlsv.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.qlsv.model.Semester;
import com.qlsv.service.SemesterService;
import com.qlsv.testutil.FakeSemesterDAO;
import com.qlsv.testutil.RecordingDialogHandler;
import com.qlsv.view.SemesterPanel;

import java.time.LocalDate;

import javax.swing.JOptionPane;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test luồng giao diện học kỳ (#10): SemesterController với DAO giả, không cần CSDL.
 * Bảng sắp học kỳ mới nhất lên đầu: dòng 0 là 2026-2, dòng 1 là 2026-1.
 */
class SemesterControllerTest {

    private FakeSemesterDAO dao;
    private SemesterPanel panel;
    private SemesterController controller;
    private RecordingDialogHandler dialog;
    private Semester hk1;

    @BeforeEach
    void setUp() {
        dao = new FakeSemesterDAO();
        SemesterService service = new SemesterService(dao);
        hk1 = new Semester("2026-1", "HK1 2026-2027", LocalDate.of(2026, 9, 7),
                LocalDate.of(2027, 1, 15), true);
        service.add(hk1);
        service.add(new Semester("2026-2", "HK2 2026-2027", LocalDate.of(2027, 2, 15),
                LocalDate.of(2027, 6, 30), false));

        panel = new SemesterPanel();
        controller = new SemesterController(panel, service);
        dialog = new RecordingDialogHandler();
        controller.setDialogHandler(dialog);
        controller.loadSemesters();
    }

    private void selectRow(int row) {
        panel.getTblSemesters().setRowSelectionInterval(row, row);
    }

    @Test
    void chonDong_dienFormNgayDdMMyyyy() {
        selectRow(1);

        assertEquals(hk1.getId(), controller.getSelectedId());
        assertEquals("07/09/2026", panel.getTxtStartDate().getText());
        assertTrue(panel.getChkOpen().isSelected());
    }

    @Test
    void onAdd_ngaySaiDinhDang_baoLoi() {
        panel.getTxtSemesterCode().setText("2027-1");
        panel.getTxtName().setText("HK1 2027-2028");
        panel.getTxtStartDate().setText("2027-09-07");
        panel.getTxtEndDate().setText("15/01/2028");

        controller.onAdd();

        assertEquals(JOptionPane.ERROR_MESSAGE, dialog.lastMessageType);
        assertTrue(dialog.lastMessage.contains("dd/MM/yyyy"));
    }

    @Test
    void onAdd_ngayKetThucTruocNgayBatDau_baoLoiBR19() {
        panel.getTxtSemesterCode().setText("2027-1");
        panel.getTxtName().setText("HK1 2027-2028");
        panel.getTxtStartDate().setText("07/09/2027");
        panel.getTxtEndDate().setText("07/09/2027");

        controller.onAdd();

        assertTrue(dialog.lastMessage.contains("BR-19"));
    }

    @Test
    void onAdd_haiHocKyCungMo_baoLoiBR26() {
        panel.getTxtSemesterCode().setText("2027-1");
        panel.getTxtName().setText("HK1 2027-2028");
        panel.getTxtStartDate().setText("07/09/2027");
        panel.getTxtEndDate().setText("15/01/2028");
        panel.getChkOpen().setSelected(true);

        controller.onAdd();

        assertEquals(DialogHandler.TITLE_ERROR, dialog.lastTitle);
        assertTrue(dialog.lastMessage.contains("BR-26"));
        assertEquals(2, dao.byId.size());
    }

    /** Bug B2: sửa xong bảng tải lại làm mất dòng đang chọn, bấm Sửa lần hai bị báo "chưa chọn". */
    @Test
    void onUpdate_thanhCong_vanChonDongVuaSua_suaTiepDuoc() {
        selectRow(1);
        panel.getChkOpen().setSelected(false);

        controller.onUpdate();

        assertEquals("Cập nhật học kỳ thành công!", dialog.lastMessage);
        assertEquals(hk1.getId(), controller.getSelectedId());
        Semester selected = controller.getTableModel().getSelectedItem(panel.getTblSemesters());
        assertNotNull(selected);
        assertFalse(selected.isOpen());

        panel.getTxtName().setText("Học kỳ 1 năm 2026-2027");
        controller.onUpdate();

        assertEquals("Cập nhật học kỳ thành công!", dialog.lastMessage);
        assertEquals("Học kỳ 1 năm 2026-2027", dao.byId.get(hk1.getId()).getName());
    }

    @Test
    void onDelete_daCoSinhVienDangKy_baoLoiBR16() {
        dao.withEnrollments.add(hk1.getId());
        selectRow(1);

        controller.onDelete();

        assertTrue(dialog.lastMessage.contains("BR-16"));
        assertEquals(2, dao.byId.size());
    }
}
