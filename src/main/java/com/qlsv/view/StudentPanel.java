package com.qlsv.view;

import com.qlsv.model.Gender;
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
import java.time.format.DateTimeFormatter;

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

/**
 * Màn hình quản lý sinh viên: form bên trái, bảng danh sách bên phải, thanh nút bên dưới.
 * <p>
 * Panel chỉ dựng giao diện. Xử lý sự kiện và gọi Service thuộc về
 * {@link com.qlsv.controller.StudentController}; Panel không chứa SQL.
 */
public class StudentPanel extends JPanel {

    /** Định dạng ngày sinh người dùng nhập vào form. */
    public static final String DATE_PATTERN = "dd/MM/yyyy";
    public static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern(DATE_PATTERN);

    private static final int FIELD_COLUMNS = 12;

    /** Màu nền dòng chẵn/lẻ xen kẽ cho dễ dò theo hàng. */
    private static final Color ROW_EVEN = Color.WHITE;
    private static final Color ROW_ODD = new Color(0xF3, 0xF6, 0xFA);
    private static final Color GRID = new Color(0xE2, 0xE6, 0xEB);
    private static final Color DANGER = new Color(0xB4, 0x23, 0x18);

    /** Độ rộng ưu tiên của từng cột, theo thứ tự cột trong {@link StudentTableModel}. */
    private static final int[] COLUMN_WIDTHS = {85, 170, 95, 70, 90, 55, 200, 100, 90};

    // Form thông tin sinh viên
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

    // Bảng danh sách
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

    // Thanh nút
    private final JButton btnAdd = new JButton("Thêm");
    private final JButton btnUpdate = new JButton("Sửa");
    private final JButton btnDelete = new JButton("Xóa");
    private final JButton btnRefresh = new JButton("Làm mới");
    private final JLabel lblStatus = new JLabel(" ");

    public StudentPanel() {
        super(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        add(buildForm(), BorderLayout.WEST);
        add(buildTable(), BorderLayout.CENTER);
        add(buildButtonBar(), BorderLayout.SOUTH);
    }

    private JPanel buildForm() {
        // Hiện năm dạng 2026, không có dấu phân cách hàng nghìn
        spnIntakeYear.setEditor(new JSpinner.NumberEditor(spnIntakeYear, "#"));
        txtDob.setToolTipText("Nhập theo dạng " + DATE_PATTERN + ", ví dụ 01/05/2006");
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

        // Dòng trống cuối cùng chiếm phần cao còn lại để các ô dồn lên trên
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

    private JScrollPane buildTable() {
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
        scroll.setBorder(BorderFactory.createTitledBorder("Danh sách sinh viên"));
        return scroll;
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

    /**
     * Gắn model cho bảng rồi đặt độ rộng và cách căn lề từng cột. Phải gọi hàm này
     * thay vì {@code getTblStudents().setModel(...)}, vì đổi model thì JTable tạo lại cột.
     */
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
                setText(value instanceof LocalDate d ? DATE_FORMAT.format(d) : "");
            }
        };
        date.setHorizontalAlignment(SwingConstants.CENTER);
        columns.getColumn(StudentTableModel.COL_DOB).setCellRenderer(date);
    }

    /** Hiện một dòng thông báo ngắn ở góc phải thanh nút, ví dụ số sinh viên đã tải. */
    public void setStatusText(String text) {
        lblStatus.setText(text == null || text.isEmpty() ? " " : text);
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

    /**
     * Bảng có sắp xếp khi bấm tiêu đề cột, nên chỉ số dòng trên bảng có thể khác chỉ số
     * trong model: dùng {@code convertRowIndexToModel} trước khi lấy sinh viên ra.
     */
    public JTable getTblStudents() { return tblStudents; }

    public JButton getBtnAdd() { return btnAdd; }
    public JButton getBtnUpdate() { return btnUpdate; }
    public JButton getBtnDelete() { return btnDelete; }
    public JButton getBtnRefresh() { return btnRefresh; }
}
