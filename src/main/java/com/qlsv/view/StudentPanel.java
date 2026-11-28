package com.qlsv.view;

import com.qlsv.model.Gender;
import com.qlsv.model.Student;
import com.qlsv.model.StudentStatus;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.time.LocalDate;
import java.time.Year;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;

public class StudentPanel extends JPanel {

    /** Mục đầu của bộ lọc lớp, nghĩa là không lọc theo lớp. */
    public static final String ALL_CLASSES = "Tất cả các lớp";

    private static final int FIELD_COLUMNS = 12;

    private static final Color ROW_EVEN = Color.WHITE;
    private static final Color ROW_ODD = new Color(0xF3, 0xF6, 0xFA);
    private static final Color GRID = new Color(0xE2, 0xE6, 0xEB);
    private static final Color DANGER = new Color(0xB4, 0x23, 0x18);

    private static final int[] COLUMN_WIDTHS = {85, 170, 95, 70, 90, 55, 200, 100, 90};

    private final JTextField txtStudentCode = new JTextField(FIELD_COLUMNS);
    private final JTextField txtFullName = new JTextField(FIELD_COLUMNS);
    private final JTextField txtDob = new JTextField(FIELD_COLUMNS);
    private final JComboBox<Gender> cboGender = new JComboBox<>(Gender.values());
    private final JTextField txtEmail = new JTextField(FIELD_COLUMNS);
    private final JTextField txtPhone = new JTextField(FIELD_COLUMNS);
    private final JTextField txtClassName = new JTextField(FIELD_COLUMNS);
    private final JSpinner spnIntakeYear = new JSpinner(
            new SpinnerNumberModel(Year.now().getValue(), 1900, 2100, 1));
    private final JComboBox<StudentStatus> cboStatus = new JComboBox<>(StudentStatus.values());

    private final JTable tblStudents = new JTable() {
        @Override
        public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
            Component c = super.prepareRenderer(renderer, row, column);
            if (!isRowSelected(row)) {
                c.setBackground(row % 2 == 0 ? ROW_EVEN : ROW_ODD);
            }
            return c;
        }
    };

    private final JButton btnAdd = new JButton("Thêm");
    private final JButton btnUpdate = new JButton("Sửa");
    private final JButton btnDelete = new JButton("Xóa");
    private final JButton btnRefresh = new JButton("Làm mới");
    private final JLabel lblStatus = new JLabel(" ");

    private final JTextField txtSearch = new JTextField(12);
    private final JComboBox<String> cboClassFilter = new JComboBox<>();
    private final JButton btnSearch = new JButton("Tìm kiếm");

    public StudentPanel() {
        super(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        cboClassFilter.addItem(ALL_CLASSES);

        add(buildForm(), BorderLayout.WEST);
        add(buildTable(), BorderLayout.CENTER);
        add(buildButtonBar(), BorderLayout.SOUTH);
    }

    private JPanel buildForm() {
        UiStyles.setupNumberSpinner(spnIntakeYear);
        txtDob.setToolTipText("Nhập theo dạng " + UiStyles.DATE_PATTERN + ", ví dụ 01/05/2006");
        txtStudentCode.setToolTipText("Ví dụ SV2026001 (BR-01)");
        txtPhone.setToolTipText("10 chữ số, bắt đầu bằng 0 (BR-17)");
        spnIntakeYear.setToolTipText("Năm nhập học, trùng với 4 số sau SV trong mã (BR-01)");

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createTitledBorder("Thông tin sinh viên"));

        int row = 0;
        addRow(form, row++, "Mã SV *", txtStudentCode);
        addRow(form, row++, "Họ tên *", txtFullName);
        addRow(form, row++, "Ngày sinh *", txtDob);
        addRow(form, row++, "Giới tính *", cboGender);
        addRow(form, row++, "Email", txtEmail);
        addRow(form, row++, "Số điện thoại", txtPhone);
        addRow(form, row++, "Lớp sinh hoạt *", txtClassName);
        addRow(form, row++, "Khóa *", spnIntakeYear);
        addRow(form, row++, "Trạng thái *", cboStatus);

        GridBagConstraints filler = new GridBagConstraints();
        filler.gridy = row;
        filler.weighty = 1;
        form.add(new JLabel(), filler);
        return form;
    }

    private static void addRow(JPanel form, int row, String label, JComponent field) {
        GridBagConstraints c = new GridBagConstraints();
        c.gridy = row;
        c.insets = new Insets(4, 6, 4, 6);
        c.anchor = GridBagConstraints.LINE_START;

        c.gridx = 0;
        form.add(new JLabel(label), c);

        c.gridx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        form.add(field, c);
    }

    private JPanel buildTable() {
        tblStudents.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblStudents.setAutoCreateRowSorter(true);
        tblStudents.setFillsViewportHeight(true);
        tblStudents.setRowHeight(26);
        tblStudents.setShowGrid(false);
        tblStudents.setShowHorizontalLines(true);
        tblStudents.setGridColor(GRID);
        tblStudents.setIntercellSpacing(new Dimension(0, 1));

        JTableHeader header = tblStudents.getTableHeader();
        header.setReorderingAllowed(false);
        header.setFont(header.getFont().deriveFont(Font.BOLD));

        JScrollPane scroll = new JScrollPane(tblStudents);

        txtSearch.setToolTipText("Tìm theo mã hoặc họ tên");
        JPanel searchBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        searchBar.add(new JLabel("Tìm kiếm:"));
        searchBar.add(txtSearch);
        searchBar.add(new JLabel("Lớp:"));
        searchBar.add(cboClassFilter);
        searchBar.add(btnSearch);

        JPanel tablePanel = new JPanel(new BorderLayout(0, 4));
        tablePanel.setBorder(BorderFactory.createTitledBorder("Danh sách sinh viên"));
        tablePanel.add(searchBar, BorderLayout.NORTH);
        tablePanel.add(scroll, BorderLayout.CENTER);
        return tablePanel;
    }

    private JPanel buildButtonBar() {
        btnAdd.setFont(btnAdd.getFont().deriveFont(Font.BOLD));
        btnDelete.setForeground(DANGER);

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

    public void setTableModel(StudentTableModel model) {
        tblStudents.setModel(model);

        TableColumnModel columns = tblStudents.getColumnModel();
        for (int i = 0; i < COLUMN_WIDTHS.length && i < columns.getColumnCount(); i++) {
            columns.getColumn(i).setPreferredWidth(COLUMN_WIDTHS[i]);
            columns.getColumn(i).setMinWidth(Math.min(COLUMN_WIDTHS[i], 80));
        }

        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(SwingConstants.CENTER);
        columns.getColumn(StudentTableModel.COL_GENDER).setCellRenderer(center);
        columns.getColumn(StudentTableModel.COL_INTAKE_YEAR).setCellRenderer(center);

        DefaultTableCellRenderer date = new DefaultTableCellRenderer() {
            @Override
            protected void setValue(Object value) {
                setText(value instanceof LocalDate d ? UiStyles.DATE_FORMAT.format(d) : "");
            }
        };
        date.setHorizontalAlignment(SwingConstants.CENTER);
        columns.getColumn(StudentTableModel.COL_DOB).setCellRenderer(date);
    }

    public void setStatusText(String text) {
        lblStatus.setText(text == null || text.isEmpty() ? " " : text);
    }

    public void clearForm() {
        txtStudentCode.setText("");
        txtStudentCode.setEditable(true);
        txtFullName.setText("");
        txtDob.setText("");
        cboGender.setSelectedItem(Gender.MALE);
        txtEmail.setText("");
        txtPhone.setText("");
        txtClassName.setText("");
        UiStyles.setSpinnerValue(spnIntakeYear, Year.now().getValue());
        cboStatus.setSelectedItem(StudentStatus.STUDYING);
        tblStudents.clearSelection();
    }

    public void setStudentToForm(Student s) {
        if (s == null) {
            clearForm();
            return;
        }
        txtStudentCode.setText(s.getStudentCode());
        txtStudentCode.setEditable(false);
        txtFullName.setText(s.getFullName());
        txtDob.setText(s.getDob() != null ? UiStyles.DATE_FORMAT.format(s.getDob()) : "");
        cboGender.setSelectedItem(s.getGender());
        txtEmail.setText(s.getEmail() != null ? s.getEmail() : "");
        txtPhone.setText(s.getPhone() != null ? s.getPhone() : "");
        txtClassName.setText(s.getClassName() != null ? s.getClassName() : "");
        UiStyles.setSpinnerValue(spnIntakeYear, s.getIntakeYear());
        cboStatus.setSelectedItem(s.getStatus());
    }

    public void updateClassFilter(List<String> classes) {
        String currentSelection = (String) cboClassFilter.getSelectedItem();
        cboClassFilter.removeAllItems();
        cboClassFilter.addItem(ALL_CLASSES);
        if (classes != null) {
            for (String c : classes) {
                if (c != null && !c.isBlank()) {
                    cboClassFilter.addItem(c);
                }
            }
        }
        if (currentSelection != null) {
            cboClassFilter.setSelectedItem(currentSelection);
        }
        if (cboClassFilter.getSelectedIndex() < 0) {
            cboClassFilter.setSelectedIndex(0);
        }
    }

    public JTextField getTxtStudentCode() { return txtStudentCode; }
    public JTextField getTxtFullName() { return txtFullName; }
    public JTextField getTxtDob() { return txtDob; }
    public JComboBox<Gender> getCboGender() { return cboGender; }
    public JTextField getTxtEmail() { return txtEmail; }
    public JTextField getTxtPhone() { return txtPhone; }
    public JTextField getTxtClassName() { return txtClassName; }
    public JSpinner getSpnIntakeYear() { return spnIntakeYear; }
    public JComboBox<StudentStatus> getCboStatus() { return cboStatus; }

    public JTable getTblStudents() { return tblStudents; }

    public JButton getBtnAdd() { return btnAdd; }
    public JButton getBtnUpdate() { return btnUpdate; }
    public JButton getBtnDelete() { return btnDelete; }
    public JButton getBtnRefresh() { return btnRefresh; }

    public JTextField getTxtSearch() { return txtSearch; }
    public JComboBox<String> getCboClassFilter() { return cboClassFilter; }
    public JButton getBtnSearch() { return btnSearch; }
}
