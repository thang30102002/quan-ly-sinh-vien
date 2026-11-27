package com.qlsv.view;

import com.qlsv.model.Course;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

/**
 * Giao diện quản lý học phần: form bên trái, bảng danh sách ở giữa, nút thao tác ở dưới.
 * Chỉ dựng giao diện; xử lý sự kiện nằm ở {@code CourseController}.
 */
public class CoursePanel extends JPanel {

    private static final int FIELD_COLUMNS = 14;

    private final JTextField txtCourseCode = new JTextField(FIELD_COLUMNS);
    private final JTextField txtName = new JTextField(FIELD_COLUMNS);
    private final JSpinner spnCredits = new JSpinner(new SpinnerNumberModel(3, 1, 6, 1));
    private final JTextArea txtDescription = new JTextArea(4, FIELD_COLUMNS);

    private final JTable tblCourses = UiStyles.createTable();
    private final JTextField txtSearch = new JTextField(14);
    private final JButton btnSearch = new JButton("Tìm kiếm");

    private final JButton btnAdd = new JButton("Thêm");
    private final JButton btnUpdate = new JButton("Sửa");
    private final JButton btnDelete = new JButton("Xóa");
    private final JButton btnRefresh = new JButton("Làm mới");
    private final JLabel lblStatus = new JLabel(" ");

    public CoursePanel() {
        super(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        add(buildForm(), BorderLayout.WEST);
        add(buildTable(), BorderLayout.CENTER);
        add(buildButtonBar(), BorderLayout.SOUTH);
    }

    private JPanel buildForm() {
        txtCourseCode.setToolTipText("2–4 chữ in hoa + 3 chữ số, ví dụ IT101 (BR-02)");
        spnCredits.setToolTipText("Số nguyên từ 1 đến 6 (BR-18)");
        UiStyles.setupNumberSpinner(spnCredits);
        txtDescription.setLineWrap(true);
        txtDescription.setWrapStyleWord(true);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createTitledBorder("Thông tin học phần"));
        int row = 0;
        UiStyles.addFormRow(form, row++, "Mã học phần *", txtCourseCode);
        UiStyles.addFormRow(form, row++, "Tên học phần *", txtName);
        UiStyles.addFormRow(form, row++, "Số tín chỉ *", spnCredits);
        UiStyles.addFormRow(form, row++, "Mô tả", new JScrollPane(txtDescription));
        UiStyles.addFormFiller(form, row);
        return form;
    }

    private JPanel buildTable() {
        txtSearch.setToolTipText("Tìm theo mã hoặc tên học phần");
        JPanel searchBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        searchBar.add(new JLabel("Tìm kiếm:"));
        searchBar.add(txtSearch);
        searchBar.add(btnSearch);

        JPanel tablePanel = new JPanel(new BorderLayout(0, 4));
        tablePanel.setBorder(BorderFactory.createTitledBorder("Danh sách học phần"));
        tablePanel.add(searchBar, BorderLayout.NORTH);
        tablePanel.add(new JScrollPane(tblCourses), BorderLayout.CENTER);
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

    public void setTableModel(CourseTableModel model) {
        tblCourses.setModel(model);
        UiStyles.setColumnWidths(tblCourses, 80, 240, 60, 320);
        UiStyles.centerColumns(tblCourses, CourseTableModel.COL_CREDITS);
    }

    public void setStatusText(String text) {
        lblStatus.setText(text == null || text.isEmpty() ? " " : text);
    }

    public void clearForm() {
        txtCourseCode.setText("");
        txtName.setText("");
        UiStyles.setSpinnerValue(spnCredits, 3);
        txtDescription.setText("");
        tblCourses.clearSelection();
    }

    public void setCourseToForm(Course c) {
        if (c == null) {
            clearForm();
            return;
        }
        txtCourseCode.setText(c.getCourseCode());
        txtName.setText(c.getName());
        UiStyles.setSpinnerValue(spnCredits, c.getCredits());
        txtDescription.setText(c.getDescription() != null ? c.getDescription() : "");
    }

    public JTextField getTxtCourseCode() { return txtCourseCode; }
    public JTextField getTxtName() { return txtName; }
    public JSpinner getSpnCredits() { return spnCredits; }
    public JTextArea getTxtDescription() { return txtDescription; }

    public JTable getTblCourses() { return tblCourses; }
    public JTextField getTxtSearch() { return txtSearch; }
    public JButton getBtnSearch() { return btnSearch; }

    public JButton getBtnAdd() { return btnAdd; }
    public JButton getBtnUpdate() { return btnUpdate; }
    public JButton getBtnDelete() { return btnDelete; }
    public JButton getBtnRefresh() { return btnRefresh; }
}
