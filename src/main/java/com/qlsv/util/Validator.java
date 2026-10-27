package com.qlsv.util;

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
}
