package com.qlsv.view;

import com.qlsv.model.Semester;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;

/**
 * Giao diện quản lý học kỳ. Chỉ dựng giao diện; xử lý sự kiện nằm ở {@code SemesterController}.
 */
public class SemesterPanel extends JPanel {

    private static final int FIELD_COLUMNS = 14;

    private final JTextField txtSemesterCode = new JTextField(FIELD_COLUMNS);
    private final JTextField txtName = new JTextField(FIELD_COLUMNS);
    private final JTextField txtStartDate = new JTextField(FIELD_COLUMNS);
    private final JTextField txtEndDate = new JTextField(FIELD_COLUMNS);
    private final JCheckBox chkOpen = new JCheckBox("Đang mở đăng ký");

    private final JTable tblSemesters = UiStyles.createTable();

    private final JButton btnAdd = new JButton("Thêm");
    private final JButton btnUpdate = new JButton("Sửa");
    private final JButton btnDelete = new JButton("Xóa");
    private final JButton btnRefresh = new JButton("Làm mới");
    private final JLabel lblStatus = new JLabel(" ");

    public SemesterPanel() {
        super(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        add(buildForm(), BorderLayout.WEST);
        add(buildTable(), BorderLayout.CENTER);
        add(buildButtonBar(), BorderLayout.SOUTH);
    }

    private JPanel buildForm() {
        txtSemesterCode.setToolTipText("<năm>-<1|2|3>, ví dụ 2026-1; 3 là học kỳ hè (BR-03)");
        String dateTip = "Nhập theo dạng " + UiStyles.DATE_PATTERN + ", ví dụ 07/09/2026";
        txtStartDate.setToolTipText(dateTip);
        txtEndDate.setToolTipText(dateTip + "; phải sau ngày bắt đầu (BR-19)");
        chkOpen.setToolTipText("Mỗi lúc chỉ một học kỳ được mở đăng ký (BR-26)");

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createTitledBorder("Thông tin học kỳ"));
        int row = 0;
        UiStyles.addFormRow(form, row++, "Mã học kỳ *", txtSemesterCode);
        UiStyles.addFormRow(form, row++, "Tên học kỳ *", txtName);
        UiStyles.addFormRow(form, row++, "Ngày bắt đầu *", txtStartDate);
        UiStyles.addFormRow(form, row++, "Ngày kết thúc *", txtEndDate);
        UiStyles.addFormRow(form, row++, "Trạng thái", chkOpen);
        UiStyles.addFormFiller(form, row);
        return form;
    }

    private JPanel buildTable() {
        JPanel tablePanel = new JPanel(new BorderLayout(0, 4));
        tablePanel.setBorder(BorderFactory.createTitledBorder("Danh sách học kỳ"));
        tablePanel.add(new JScrollPane(tblSemesters), BorderLayout.CENTER);
        return tablePanel;
    }

    private JPanel buildButtonBar() {
        UiStyles.stylePrimary(btnAdd);
        UiStyles.styleDanger(btnDelete);
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        buttons.add(btnAdd);
        buttons.add(btnUpdate);
        buttons.add(btnDelete);
        buttons.add(btnRefresh);

        JPanel bar = new JPanel(new BorderLayout());
        bar.add(buttons, BorderLayout.WEST);
        bar.add(lblStatus, BorderLayout.EAST);
        return bar;
    }

    public void setTableModel(SemesterTableModel model) {
        tblSemesters.setModel(model);
        UiStyles.setColumnWidths(tblSemesters, 80, 240, 110, 110, 130);
        UiStyles.dateColumns(tblSemesters, SemesterTableModel.COL_START, SemesterTableModel.COL_END);
        UiStyles.centerColumns(tblSemesters, SemesterTableModel.COL_STATUS);
    }

    public void setStatusText(String text) {
        lblStatus.setText(text == null || text.isEmpty() ? " " : text);
    }

    public void clearForm() {
        txtSemesterCode.setText("");
        txtName.setText("");
        txtStartDate.setText("");
        txtEndDate.setText("");
        chkOpen.setSelected(false);
        tblSemesters.clearSelection();
    }

    public void setSemesterToForm(Semester s) {
        if (s == null) {
            clearForm();
            return;
        }
        txtSemesterCode.setText(s.getSemesterCode());
        txtName.setText(s.getName());
        txtStartDate.setText(s.getStartDate() != null ? UiStyles.DATE_FORMAT.format(s.getStartDate()) : "");
        txtEndDate.setText(s.getEndDate() != null ? UiStyles.DATE_FORMAT.format(s.getEndDate()) : "");
        chkOpen.setSelected(s.isOpen());
    }

    public JTextField getTxtSemesterCode() { return txtSemesterCode; }
    public JTextField getTxtName() { return txtName; }
    public JTextField getTxtStartDate() { return txtStartDate; }
    public JTextField getTxtEndDate() { return txtEndDate; }
    public JCheckBox getChkOpen() { return chkOpen; }

    public JTable getTblSemesters() { return tblSemesters; }

    public JButton getBtnAdd() { return btnAdd; }
    public JButton getBtnUpdate() { return btnUpdate; }
    public JButton getBtnDelete() { return btnDelete; }
    public JButton getBtnRefresh() { return btnRefresh; }
}
