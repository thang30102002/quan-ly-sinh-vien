package com.qlsv.controller;

import java.awt.Component;
import java.util.regex.Pattern;

import javax.swing.JOptionPane;

/**
 * Hiển thị hộp thoại cho Controller. Tách thành interface để test thay bằng bản ghi lại
 * thông báo, không phải bật cửa sổ thật.
 * <p>
 * Các hàm {@link #info}, {@link #warning}, {@link #error}, {@link #confirmDelete} dùng chung cho
 * mọi Controller để tiêu đề và kiểu hộp thoại thống nhất; bản cài đặt chỉ cần hai hàm gốc.
 * <p>
 * Thông báo trong code và exception vẫn ghi mã quy tắc (vd "(BR-18)") để truy vết, nhưng
 * người dùng không cần thấy mã này nên các hàm trên bỏ đi trước khi hiển thị
 * ({@link #withoutRuleCodes}).
 */
public interface DialogHandler {

    String TITLE_INFO = "Thông báo";
    String TITLE_WARNING = "Cảnh báo";
    String TITLE_ERROR = "Lỗi";
    String TITLE_CONFIRM_DELETE = "Xác nhận xóa";

    /** Cặp ngoặc chứa mã quy tắc, vd " (BR-18)", " (BR-01, BR-17)", kèm khoảng trắng phía trước. */
    Pattern RULE_CODES = Pattern.compile("\\s*\\(BR-\\d+[^()]*\\)");

    void showMessage(Component parent, Object message, String title, int messageType);

    int showConfirm(Component parent, Object message, String title, int optionType);

    /** Thông báo thao tác thành công. */
    default void info(Component parent, String message) {
        showMessage(parent, withoutRuleCodes(message), TITLE_INFO, JOptionPane.INFORMATION_MESSAGE);
    }

    /** Nhắc người dùng làm đúng thao tác, ví dụ chưa chọn dòng trong bảng. */
    default void warning(Component parent, String message) {
        showMessage(parent, withoutRuleCodes(message), TITLE_WARNING, JOptionPane.WARNING_MESSAGE);
    }

    /** Báo lỗi nhập liệu, lỗi nghiệp vụ hoặc lỗi CSDL. */
    default void error(Component parent, String message) {
        showMessage(parent, withoutRuleCodes(message), TITLE_ERROR, JOptionPane.ERROR_MESSAGE);
    }

    /** Hỏi xác nhận xóa (Có/Không). @return true nếu người dùng chọn Có */
    default boolean confirmDelete(Component parent, String message) {
        return showConfirm(parent, withoutRuleCodes(message), TITLE_CONFIRM_DELETE, JOptionPane.YES_NO_OPTION)
                == JOptionPane.YES_OPTION;
    }

    /**
     * Bỏ mã quy tắc khỏi thông báo trước khi hiển thị:
     * "Số tín chỉ phải từ 1 đến 6 (BR-18)" → "Số tín chỉ phải từ 1 đến 6".
     * Ngoặc không bắt đầu bằng "BR-" như "(3)" được giữ nguyên.
     */
    static String withoutRuleCodes(String message) {
        return message == null ? null : RULE_CODES.matcher(message).replaceAll("");
    }

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
