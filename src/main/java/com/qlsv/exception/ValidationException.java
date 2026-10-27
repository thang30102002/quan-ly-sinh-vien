package com.qlsv.exception;

/**
 * Lỗi dữ liệu nhập sai định dạng hoặc bị thiếu, ví dụ mã SV sai (BR-01),
 * email sai, chưa đủ 16 tuổi (BR-17). Thông báo ghi mã BR tương ứng.
 */
public class ValidationException extends RuntimeException {

    public ValidationException(String message) {
        super(message);
    }

    public ValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
