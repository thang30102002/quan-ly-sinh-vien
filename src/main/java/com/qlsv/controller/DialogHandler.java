package com.qlsv.controller;

import java.awt.Component;

import javax.swing.JOptionPane;

/**
 * Hiển thị hộp thoại cho Controller. Tách thành interface để test thay bằng bản ghi lại
 * thông báo, không phải bật cửa sổ thật.
 */
public interface DialogHandler {

    void showMessage(Component parent, Object message, String title, int messageType);

    int showConfirm(Component parent, Object message, String title, int optionType);

    /** Bản mặc định dùng {@link JOptionPane}. */
    static DialogHandler swing() {
        return new DialogHandler() {
            @Override
            public void showMessage(Component parent, Object message, String title, int messageType) {
                JOptionPane.showMessageDialog(parent, message, title, messageType);
            }

            @Override
            public int showConfirm(Component parent, Object message, String title, int optionType) {
                return JOptionPane.showConfirmDialog(parent, message, title, optionType,
                        JOptionPane.QUESTION_MESSAGE);
            }
        };
    }
}
