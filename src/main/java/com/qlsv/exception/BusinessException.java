package com.qlsv.exception;

/**
 * Lỗi vi phạm quy tắc nghiệp vụ, ví dụ vượt 12 tín chỉ (BR-04),
 * trùng lịch (BR-22), lớp đã đủ sĩ số (BR-25). Thông báo ghi mã BR tương ứng.
 */
public class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }

    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }
}
