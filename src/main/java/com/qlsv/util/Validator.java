package com.qlsv.util;

import com.qlsv.exception.ValidationException;

import java.time.LocalDate;
import java.time.Period;
import java.util.regex.Pattern;

public final class Validator {

    private static final Pattern STUDENT_CODE_PATTERN = Pattern.compile("^SV\\d{7}$");
    private static final Pattern COURSE_CODE_PATTERN = Pattern.compile("^[A-Z]{2,4}\\d{3}$");
    private static final Pattern CLASS_SECTION_CODE_PATTERN = Pattern.compile("^[A-Z]{2,4}\\d{3}-\\d{2}$");
    private static final Pattern SEMESTER_CODE_PATTERN = Pattern.compile("^\\d{4}-[123]$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^0\\d{9}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final int FULL_NAME_MAX_LENGTH = 100;

    private Validator() {
    }

    public static boolean isValidStudentCode(String code) {
        if (code == null) {
            return false;
        }
        return STUDENT_CODE_PATTERN.matcher(code.trim()).matches();
    }

    public static boolean isValidCourseCode(String code) {
        if (code == null) {
            return false;
        }
        return COURSE_CODE_PATTERN.matcher(code.trim()).matches();
    }

    public static boolean isValidClassSectionCode(String code) {
        if (code == null) {
            return false;
        }
        return CLASS_SECTION_CODE_PATTERN.matcher(code.trim()).matches();
    }

    public static boolean isValidSemesterCode(String code) {
        if (code == null) {
            return false;
        }
        return SEMESTER_CODE_PATTERN.matcher(code.trim()).matches();
    }

    public static boolean isValidPhone(String phone) {
        if (phone == null) {
            return false;
        }
        return PHONE_PATTERN.matcher(phone.trim()).matches();
    }

    public static boolean isValidEmail(String email) {
        if (email == null) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    public static boolean isValidDateOfBirth(LocalDate dob) {
        if (dob == null) {
            return false;
        }
        LocalDate today = LocalDate.now();
        if (dob.isAfter(today)) {
            return false;
        }
        return Period.between(dob, today).getYears() >= 16;
    }

    /** BR-17: họ tên bắt buộc, tối đa 100 ký tự (tính sau khi bỏ khoảng trắng hai đầu). */
    public static boolean isValidFullName(String fullName) {
        if (fullName == null) {
            return false;
        }
        String trimmed = fullName.trim();
        return !trimmed.isEmpty() && trimmed.length() <= FULL_NAME_MAX_LENGTH;
    }

    // Các hàm require* ném ValidationException kèm thông báo tiếng Việt và mã BR,
    // để Service gọi trực tiếp thay vì tự kiểm tra kết quả boolean.

    public static void requireStudentCode(String code) {
        if (!isValidStudentCode(code)) {
            throw new ValidationException("Mã sinh viên không hợp lệ: phải có dạng SV + 7 chữ số, ví dụ SV2026001 (BR-01)");
        }
    }

    public static void requireCourseCode(String code) {
        if (!isValidCourseCode(code)) {
            throw new ValidationException("Mã học phần không hợp lệ: phải gồm 2–4 chữ in hoa + 3 chữ số, ví dụ IT101 (BR-02)");
        }
    }

    public static void requireSemesterCode(String code) {
        if (!isValidSemesterCode(code)) {
            throw new ValidationException("Mã học kỳ không hợp lệ: phải có dạng <năm>-<1|2|3>, ví dụ 2026-1 (BR-03)");
        }
    }

    public static void requireClassSectionCode(String code) {
        if (!isValidClassSectionCode(code)) {
            throw new ValidationException("Mã lớp học phần không hợp lệ: phải có dạng <mã học phần>-<2 chữ số>, ví dụ IT101-01 (BR-24)");
        }
    }

    public static void requireFullName(String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) {
            throw new ValidationException("Họ tên là bắt buộc (BR-17)");
        }
        if (!isValidFullName(fullName)) {
            throw new ValidationException("Họ tên tối đa " + FULL_NAME_MAX_LENGTH + " ký tự (BR-17)");
        }
    }

    /** BR-17: email không bắt buộc, chỉ kiểm tra khi có nhập. */
    public static void requireEmail(String email) {
        if (isBlank(email)) {
            return;
        }
        if (!isValidEmail(email)) {
            throw new ValidationException("Email không đúng định dạng: " + email.trim() + " (BR-17)");
        }
    }

    /** BR-17: số điện thoại không bắt buộc, chỉ kiểm tra khi có nhập. */
    public static void requirePhone(String phone) {
        if (isBlank(phone)) {
            return;
        }
        if (!isValidPhone(phone)) {
            throw new ValidationException("Số điện thoại không hợp lệ: phải có 10 chữ số và bắt đầu bằng 0 (BR-17)");
        }
    }

    public static void requireDateOfBirth(LocalDate dob) {
        if (dob == null) {
            throw new ValidationException("Ngày sinh là bắt buộc (BR-17)");
        }
        if (dob.isAfter(LocalDate.now())) {
            throw new ValidationException("Ngày sinh không được ở tương lai (BR-17)");
        }
        if (!isValidDateOfBirth(dob)) {
            throw new ValidationException("Sinh viên phải đủ 16 tuổi trở lên (BR-17)");
        }
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
