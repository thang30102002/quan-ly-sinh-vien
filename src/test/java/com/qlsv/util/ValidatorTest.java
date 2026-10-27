package com.qlsv.util;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertFalse;
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
}
