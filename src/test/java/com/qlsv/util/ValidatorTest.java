package com.qlsv.util;

import com.qlsv.exception.ValidationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ValidatorTest {

    @Test
    void testStudentCode() {
        assertTrue(Validator.isValidStudentCode("SV2026001"));
        assertTrue(Validator.isValidStudentCode("SV2024123"));

        assertFalse(Validator.isValidStudentCode(null));
        assertFalse(Validator.isValidStudentCode(""));
        assertFalse(Validator.isValidStudentCode("SV123"));
        assertFalse(Validator.isValidStudentCode("2026001"));
        assertFalse(Validator.isValidStudentCode("sv2026001"));
        assertFalse(Validator.isValidStudentCode("SV20260001"));
    }

    @Test
    void testCourseCode() {
        assertTrue(Validator.isValidCourseCode("IT101"));
        assertTrue(Validator.isValidCourseCode("MATH201"));
        assertTrue(Validator.isValidCourseCode("ENG102"));

        assertFalse(Validator.isValidCourseCode(null));
        assertFalse(Validator.isValidCourseCode(""));
        assertFalse(Validator.isValidCourseCode("it101"));
        assertFalse(Validator.isValidCourseCode("I101"));
        assertFalse(Validator.isValidCourseCode("ABCDE101"));
        assertFalse(Validator.isValidCourseCode("IT10"));
        assertFalse(Validator.isValidCourseCode("IT1001"));
    }

    @Test
    void testClassSectionCode() {
        assertTrue(Validator.isValidClassSectionCode("IT101-01"));
        assertTrue(Validator.isValidClassSectionCode("MATH201-02"));

        assertFalse(Validator.isValidClassSectionCode(null));
        assertFalse(Validator.isValidClassSectionCode(""));
        assertFalse(Validator.isValidClassSectionCode("IT101-1"));
        assertFalse(Validator.isValidClassSectionCode("IT101_01"));
        assertFalse(Validator.isValidClassSectionCode("it101-01"));
        assertFalse(Validator.isValidClassSectionCode("IT101-001"));
    }

    @Test
    void testSemesterCode() {
        assertTrue(Validator.isValidSemesterCode("2026-1"));
        assertTrue(Validator.isValidSemesterCode("2026-2"));
        assertTrue(Validator.isValidSemesterCode("2026-3"));

        assertFalse(Validator.isValidSemesterCode(null));
        assertFalse(Validator.isValidSemesterCode(""));
        assertFalse(Validator.isValidSemesterCode("2026-4"));
        assertFalse(Validator.isValidSemesterCode("2026-0"));
        assertFalse(Validator.isValidSemesterCode("26-1"));
        assertFalse(Validator.isValidSemesterCode("2026/1"));
    }

    @Test
    void testPhone() {
        assertTrue(Validator.isValidPhone("0912345678"));
        assertTrue(Validator.isValidPhone("0389998888"));

        assertFalse(Validator.isValidPhone(null));
        assertFalse(Validator.isValidPhone(""));
        assertFalse(Validator.isValidPhone("091234567"));
        assertFalse(Validator.isValidPhone("09123456789"));
        assertFalse(Validator.isValidPhone("1912345678"));
        assertFalse(Validator.isValidPhone("091234567a"));
    }

    @Test
    void testEmail() {
        assertTrue(Validator.isValidEmail("sinhvien@gmail.com"));
        assertTrue(Validator.isValidEmail("nguyen.van.a@edu.vn"));
        assertTrue(Validator.isValidEmail("sv_2026@school.org"));

        assertFalse(Validator.isValidEmail(null));
        assertFalse(Validator.isValidEmail(""));
        assertFalse(Validator.isValidEmail("sinhviengmail.com"));
        assertFalse(Validator.isValidEmail("sinhvien@"));
        assertFalse(Validator.isValidEmail("sinhvien@gmail"));
        assertFalse(Validator.isValidEmail("@gmail.com"));
    }

    @Test
    void testDateOfBirth() {
        LocalDate today = LocalDate.now();

        assertTrue(Validator.isValidDateOfBirth(today.minusYears(18)));
        assertTrue(Validator.isValidDateOfBirth(today.minusYears(16)));
        assertTrue(Validator.isValidDateOfBirth(today.minusYears(25)));

        assertFalse(Validator.isValidDateOfBirth(null));
        assertFalse(Validator.isValidDateOfBirth(today.plusDays(1)));
        assertFalse(Validator.isValidDateOfBirth(today.minusYears(15)));
        assertFalse(Validator.isValidDateOfBirth(today.minusYears(16).plusDays(1)));
    }

    @Test
    void testFullName() {
        assertTrue(Validator.isValidFullName("Nguyễn Văn A"));
        assertTrue(Validator.isValidFullName("A".repeat(100)));
        assertTrue(Validator.isValidFullName("  " + "A".repeat(100) + "  "));

        assertFalse(Validator.isValidFullName(null));
        assertFalse(Validator.isValidFullName(""));
        assertFalse(Validator.isValidFullName("   "));
        assertFalse(Validator.isValidFullName("A".repeat(101)));
    }

    @Test
    void testRequireCodesThrowValidationException() {
        assertDoesNotThrow(() -> Validator.requireStudentCode("SV2026001"));
        assertDoesNotThrow(() -> Validator.requireCourseCode("IT101"));
        assertDoesNotThrow(() -> Validator.requireSemesterCode("2026-1"));
        assertDoesNotThrow(() -> Validator.requireClassSectionCode("IT101-01"));

        assertMessageContains("BR-01", () -> Validator.requireStudentCode("SV123"));
        assertMessageContains("BR-02", () -> Validator.requireCourseCode("it101"));
        assertMessageContains("BR-03", () -> Validator.requireSemesterCode("2026-4"));
        assertMessageContains("BR-24", () -> Validator.requireClassSectionCode("IT101-1"));
    }

    @Test
    void testRequireFullName() {
        assertDoesNotThrow(() -> Validator.requireFullName("Nguyễn Văn A"));

        ValidationException missing = assertThrows(ValidationException.class,
                () -> Validator.requireFullName("  "));
        assertTrue(missing.getMessage().contains("bắt buộc"));
        assertTrue(missing.getMessage().contains("BR-17"));

        ValidationException tooLong = assertThrows(ValidationException.class,
                () -> Validator.requireFullName("A".repeat(101)));
        assertTrue(tooLong.getMessage().contains("100"));
        assertTrue(tooLong.getMessage().contains("BR-17"));
    }

    @Test
    void testRequireEmailAndPhoneAreOptional() {
        // BR-17: email và SĐT chỉ kiểm tra khi có nhập
        assertDoesNotThrow(() -> Validator.requireEmail(null));
        assertDoesNotThrow(() -> Validator.requireEmail(" "));
        assertDoesNotThrow(() -> Validator.requireEmail("sinhvien@gmail.com"));
        assertDoesNotThrow(() -> Validator.requirePhone(null));
        assertDoesNotThrow(() -> Validator.requirePhone(""));
        assertDoesNotThrow(() -> Validator.requirePhone("0912345678"));

        assertMessageContains("BR-17", () -> Validator.requireEmail("sinhvien@gmail"));
        assertMessageContains("BR-17", () -> Validator.requirePhone("1912345678"));
    }

    @Test
    void testRequireDateOfBirth() {
        LocalDate today = LocalDate.now();

        assertDoesNotThrow(() -> Validator.requireDateOfBirth(today.minusYears(16)));

        assertMessageContains("bắt buộc", () -> Validator.requireDateOfBirth(null));
        assertMessageContains("tương lai", () -> Validator.requireDateOfBirth(today.plusDays(1)));
        assertMessageContains("16 tuổi", () -> Validator.requireDateOfBirth(today.minusYears(15)));
    }

    private static void assertMessageContains(String expected, Executable executable) {
        ValidationException ex = assertThrows(ValidationException.class, executable);
        assertTrue(ex.getMessage().contains(expected),
                () -> "Thông báo lỗi \"" + ex.getMessage() + "\" không chứa \"" + expected + "\"");
    }
}
