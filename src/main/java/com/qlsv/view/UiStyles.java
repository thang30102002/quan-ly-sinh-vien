package com.qlsv.view;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.math.BigInteger;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;
import javax.swing.text.DefaultFormatter;
import javax.swing.text.DefaultFormatterFactory;

/**
 * Kiểu dáng và định dạng dùng chung cho các Panel.
 */
public final class UiStyles {

    /** Định dạng ngày hiển thị cho người dùng trong hướng dẫn và thông báo lỗi. */
    public static final String DATE_PATTERN = "dd/MM/yyyy";

    /**
     * Định dạng ngày nhập và hiển thị trên mọi form, bảng. STRICT để ngày không có thật
     * như 31/02/2006 bị báo lỗi thay vì tự đổi thành 28/02/2006; STRICT cần "uuuu"
     * (năm) thay cho "yyyy" (năm theo kỷ nguyên).
     */
    public static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.STRICT);

    private static final Color ROW_EVEN = Color.WHITE;
    private static final Color ROW_ODD = new Color(0xF3, 0xF6, 0xFA);
    private static final Color GRID = new Color(0xE2, 0xE6, 0xEB);
    private static final Color DANGER = new Color(0xB4, 0x23, 0x18);

    private UiStyles() {
    }

    /**
     * Cài đặt ô nhập số nguyên có {@link SpinnerNumberModel} (#10):
     * <ul>
     * <li>Chỉ nhận chữ là số nguyên viết liền trong khoảng của model, giống nhau trên mọi
     *     ngôn ngữ máy. Bộ đọc mặc định của Swing đọc theo ngôn ngữ máy và cắt phần lẻ:
     *     "3,5" (máy tiếng Việt) hay "3.5" (máy tiếng Anh) thành 3, "3a" thành 3.</li>
     * <li>Hiện số liền, không dấu phân cách hàng nghìn (máy tiếng Việt hiện 1000 thành
     *     "1.000").</li>
     * <li>Khi ô mất focus mà chữ không hợp lệ thì giữ nguyên chữ đó (COMMIT) thay vì lặng lẽ
     *     trả về giá trị cũ (mặc định COMMIT_OR_REVERT làm gõ 7 tín chỉ rồi bấm Thêm thì lưu
     *     luôn giá trị cũ). Controller đọc ô qua FormInputs.readInt sẽ báo lỗi.</li>
     * </ul>
     */
    public static void setupNumberSpinner(JSpinner... spinners) {
        for (JSpinner spinner : spinners) {
            spinner.setEditor(new JSpinner.NumberEditor(spinner, "#"));
            if (spinner.getEditor() instanceof JSpinner.DefaultEditor editor
                    && spinner.getModel() instanceof SpinnerNumberModel model) {
                JFormattedTextField field = editor.getTextField();
                field.setFormatterFactory(new DefaultFormatterFactory(new IntegerTextFormatter(model)));
                field.setFocusLostBehavior(JFormattedTextField.COMMIT);
                field.setValue(spinner.getValue());
            }
        }
    }

    /** Đọc/hiện số nguyên dạng "-?chữ số", kiểm tra khoảng theo model; không theo ngôn ngữ máy. */
    private static final class IntegerTextFormatter extends DefaultFormatter {

        private final SpinnerNumberModel model;

        IntegerTextFormatter(SpinnerNumberModel model) {
            this.model = model;
            setValueClass(Integer.class);
            setCommitsOnValidEdit(false);
            setOverwriteMode(false);
        }

        @Override
        public Object stringToValue(String text) throws ParseException {
            String t = text == null ? "" : text.trim();
            if (!t.matches("-?\\d+")) {
                throw new ParseException(text, 0);
            }
            BigInteger n = new BigInteger(t);
            if (n.compareTo(bound(model.getMinimum(), Integer.MIN_VALUE)) < 0
                    || n.compareTo(bound(model.getMaximum(), Integer.MAX_VALUE)) > 0) {
                throw new ParseException(text, 0);
            }
            return n.intValue();
        }

        @Override
        public String valueToString(Object value) {
            return value == null ? "" : value.toString();
        }

        private static BigInteger bound(Comparable<?> limit, int fallback) {
            return BigInteger.valueOf(limit instanceof Number n ? n.longValue() : fallback);
        }
    }

    /**
     * Gán giá trị cho ô số và hiển thị lại đúng giá trị đó. Cần thiết vì sau
     * {@link #setupNumberSpinner} ô có thể còn chữ sai (vd "7") trong khi giá trị
     * vẫn là 3; {@code setValue(3)} khi đó không đổi gì nên chữ "7" không mất.
     */
    public static void setSpinnerValue(JSpinner spinner, int value) {
        spinner.setValue(value);
        if (spinner.getEditor() instanceof JSpinner.DefaultEditor editor) {
            editor.getTextField().setValue(spinner.getValue());
        }
    }

    /** Bảng chọn một dòng, có sắp xếp, dòng xen kẽ màu. */
    public static JTable createTable() {
        JTable table = new JTable() {
            @Override
            public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
                Component c = super.prepareRenderer(renderer, row, column);
                if (!isRowSelected(row)) {
                    c.setBackground(row % 2 == 0 ? ROW_EVEN : ROW_ODD);
                }
                return c;
            }
        };
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setAutoCreateRowSorter(true);
        table.setFillsViewportHeight(true);
        table.setRowHeight(26);
        table.setShowGrid(false);
        table.setShowHorizontalLines(true);
        table.setGridColor(GRID);
        table.setIntercellSpacing(new Dimension(0, 1));
        JTableHeader header = table.getTableHeader();
        header.setReorderingAllowed(false);
        header.setFont(header.getFont().deriveFont(Font.BOLD));
        return table;
    }

    /** Đặt độ rộng ưu tiên cho các cột, theo thứ tự. */
    public static void setColumnWidths(JTable table, int... widths) {
        TableColumnModel columns = table.getColumnModel();
        for (int i = 0; i < widths.length && i < columns.getColumnCount(); i++) {
            columns.getColumn(i).setPreferredWidth(widths[i]);
        }
    }

    public static void centerColumns(JTable table, int... columnIndexes) {
        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(SwingConstants.CENTER);
        for (int i : columnIndexes) {
            table.getColumnModel().getColumn(i).setCellRenderer(center);
        }
    }

    /** Hiển thị cột {@link LocalDate} theo dạng dd/MM/yyyy, căn giữa. */
    public static void dateColumns(JTable table, int... columnIndexes) {
        DefaultTableCellRenderer date = new DefaultTableCellRenderer() {
            @Override
            protected void setValue(Object value) {
                setText(value instanceof LocalDate d ? DATE_FORMAT.format(d) : "");
            }
        };
        date.setHorizontalAlignment(SwingConstants.CENTER);
        for (int i : columnIndexes) {
            table.getColumnModel().getColumn(i).setCellRenderer(date);
        }
    }

    /** Thêm một dòng "nhãn: ô nhập" vào form dùng GridBagLayout. */
    public static void addFormRow(JPanel form, int row, String label, JComponent field) {
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

    /** Ô trống co giãn ở cuối form để các dòng dồn lên trên. */
    public static void addFormFiller(JPanel form, int row) {
        GridBagConstraints filler = new GridBagConstraints();
        filler.gridy = row;
        filler.weighty = 1;
        form.add(new JLabel(), filler);
    }

    public static void stylePrimary(JButton button) {
        button.setFont(button.getFont().deriveFont(Font.BOLD));
    }

    public static void styleDanger(JButton button) {
        button.setForeground(DANGER);
    }
}
