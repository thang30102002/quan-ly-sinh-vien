package com.qlsv.view;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
