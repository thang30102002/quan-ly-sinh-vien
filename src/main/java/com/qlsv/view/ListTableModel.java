package com.qlsv.view;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

/**
 * Bảng chỉ để xem, mỗi dòng là một đối tượng {@code T}; sửa dữ liệu qua form.
 * Lớp con khai báo tên cột, kiểu cột và cách lấy giá trị từng ô.
 */
public abstract class ListTableModel<T> extends AbstractTableModel {

    private final String[] columnNames;
    private final Class<?>[] columnClasses;
    private final List<T> items = new ArrayList<>();

    protected ListTableModel(String[] columnNames, Class<?>[] columnClasses) {
        if (columnNames.length != columnClasses.length) {
            throw new IllegalArgumentException("Số tên cột và số kiểu cột phải bằng nhau");
        }
        this.columnNames = columnNames.clone();
        this.columnClasses = columnClasses.clone();
    }

    /** Giá trị của ô ở cột {@code column} cho dòng {@code item}. */
    protected abstract Object valueAt(T item, int column);

    /** Thay toàn bộ dữ liệu của bảng. Danh sách truyền vào được sao chép lại. */
    public void setItems(List<? extends T> list) {
        items.clear();
        if (list != null) {
            items.addAll(list);
        }
        fireTableDataChanged();
    }

    /**
     * @param modelRow chỉ số dòng trong model; nếu lấy từ JTable thì đổi bằng
     *                 {@code table.convertRowIndexToModel(viewRow)} trước
     */
    public T getItemAt(int modelRow) {
        return items.get(modelRow);
    }

    public List<T> getItems() {
        return Collections.unmodifiableList(items);
    }

    /**
     * @param table bảng đang hiển thị model này (có thể đã sắp xếp)
     * @return đối tượng ở dòng đang chọn, hoặc null nếu chưa chọn dòng nào
     */
    public T getSelectedItem(JTable table) {
        int viewRow = table.getSelectedRow();
        return viewRow >= 0 ? items.get(table.convertRowIndexToModel(viewRow)) : null;
    }

    /**
     * Chọn dòng đầu tiên thỏa {@code match} và cuộn tới dòng đó, dùng để chọn lại dòng vừa
     * thêm/sửa sau khi tải lại bảng (tải lại làm JTable bỏ chọn).
     *
     * @return true nếu tìm thấy và đã chọn
     */
    public boolean selectFirst(JTable table, Predicate<? super T> match) {
        for (int row = 0; row < items.size(); row++) {
            if (match.test(items.get(row))) {
                int viewRow = table.convertRowIndexToView(row);
                if (viewRow < 0) {
                    return false;
                }
                table.setRowSelectionInterval(viewRow, viewRow);
                table.scrollRectToVisible(table.getCellRect(viewRow, 0, true));
                return true;
            }
        }
        return false;
    }

    @Override
    public int getRowCount() {
        return items.size();
    }

    @Override
    public int getColumnCount() {
        return columnNames.length;
    }

    @Override
    public String getColumnName(int column) {
        return columnNames[column];
    }

    @Override
    public Class<?> getColumnClass(int column) {
        return columnClasses[column];
    }

    @Override
    public boolean isCellEditable(int row, int column) {
        return false;
    }

    @Override
    public Object getValueAt(int row, int column) {
        if (column < 0 || column >= columnNames.length) {
            throw new IndexOutOfBoundsException("Không có cột " + column);
        }
        return valueAt(items.get(row), column);
    }
}
