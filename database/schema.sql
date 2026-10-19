-- =====================================================================
-- Quản lý Sinh viên & Điểm số: tạo cơ sở dữ liệu (issue #2)
--
-- Cách chạy: phpMyAdmin > tab SQL > dán toàn bộ file này > Thực hiện,
-- sau đó chạy database/seed.sql.
-- File này XÓA database qlsv cũ (nếu có) rồi tạo lại từ đầu.
--
-- Tương thích: MySQL 8.0.16+ và MariaDB 10.3+ (XAMPP).
-- Quy tắc nghiệp vụ (BR-xx) xem docs/yeu-cau.md, sơ đồ xem docs/erd.md.
--
-- Các quy tắc KHÔNG biểu diễn được bằng ràng buộc CSDL, kiểm tra ở Service:
--   BR-04 tối đa 12 tín chỉ/học kỳ, BR-05 không trùng môn trong học kỳ,
--   BR-07 chỉ SV "Đang học" được đăng ký, BR-14/15 không xóa/hủy khi có điểm,
--   BR-17 email, tuổi >= 16, BR-21 các buổi của một lớp không trùng nhau,
--   BR-22/23 trùng lịch học, BR-24 tiền tố mã lớp = mã học phần,
--   BR-25 lớp đủ sĩ số.
-- =====================================================================

DROP DATABASE IF EXISTS qlsv;
CREATE DATABASE qlsv
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;
USE qlsv;

SET NAMES utf8mb4;

-- ---------------------------------------------------------------------
-- Sinh viên
-- ---------------------------------------------------------------------
CREATE TABLE students (
    id            INT          NOT NULL AUTO_INCREMENT,
    student_code  VARCHAR(9)   NOT NULL COMMENT 'Mã SV, vd SV2026001 (BR-01)',
    full_name     VARCHAR(100) NOT NULL COMMENT 'Họ tên (BR-17)',
    dob           DATE         NOT NULL COMMENT 'Ngày sinh',
    gender        ENUM('MALE', 'FEMALE', 'OTHER') NOT NULL COMMENT 'Giới tính',
    email         VARCHAR(100)     NULL,
    phone         VARCHAR(10)      NULL COMMENT '10 chữ số, bắt đầu bằng 0 (BR-17)',
    class_name    VARCHAR(20)  NOT NULL COMMENT 'Lớp sinh hoạt, vd CNTT-K24A',
    intake_year   SMALLINT     NOT NULL COMMENT 'Khóa (năm nhập học)',
    status        ENUM('STUDYING', 'RESERVED', 'DROPPED', 'GRADUATED')
                               NOT NULL DEFAULT 'STUDYING'
                               COMMENT 'Đang học / Bảo lưu / Thôi học / Tốt nghiệp (BR-20)',
    PRIMARY KEY (id),
    CONSTRAINT uq_students_code UNIQUE (student_code),
    -- BR-01: COLLATE utf8mb4_bin để REGEXP phân biệt hoa thường
    CONSTRAINT chk_students_code
        CHECK (student_code COLLATE utf8mb4_bin REGEXP '^SV[0-9]{7}$'),
    -- BR-01: 4 số sau "SV" là năm nhập học
    CONSTRAINT chk_students_intake_year
        CHECK (intake_year BETWEEN 2000 AND 2100
               AND CAST(SUBSTRING(student_code, 3, 4) AS UNSIGNED) = intake_year),
    CONSTRAINT chk_students_full_name CHECK (CHAR_LENGTH(TRIM(full_name)) > 0),
    CONSTRAINT chk_students_phone CHECK (phone REGEXP '^0[0-9]{9}$')
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  COMMENT = 'Sinh viên';

-- ---------------------------------------------------------------------
-- Học phần
-- ---------------------------------------------------------------------
CREATE TABLE courses (
    id           INT          NOT NULL AUTO_INCREMENT,
    course_code  VARCHAR(7)   NOT NULL COMMENT 'Mã học phần, vd IT101 (BR-02)',
    name         VARCHAR(150) NOT NULL COMMENT 'Tên học phần',
    credits      TINYINT      NOT NULL COMMENT 'Số tín chỉ 1–6 (BR-18)',
    description  TEXT             NULL COMMENT 'Mô tả',
    PRIMARY KEY (id),
    CONSTRAINT uq_courses_code UNIQUE (course_code),
    CONSTRAINT chk_courses_code
        CHECK (course_code COLLATE utf8mb4_bin REGEXP '^[A-Z]{2,4}[0-9]{3}$'),
    CONSTRAINT chk_courses_name CHECK (CHAR_LENGTH(TRIM(name)) > 0),
    CONSTRAINT chk_courses_credits CHECK (credits BETWEEN 1 AND 6)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  COMMENT = 'Học phần';

-- ---------------------------------------------------------------------
-- Học kỳ
-- ---------------------------------------------------------------------
CREATE TABLE semesters (
    id             INT          NOT NULL AUTO_INCREMENT,
    semester_code  VARCHAR(6)   NOT NULL COMMENT 'Mã học kỳ, vd 2026-1 (BR-03)',
    name           VARCHAR(100) NOT NULL COMMENT 'Tên học kỳ',
    start_date     DATE         NOT NULL,
    end_date       DATE         NOT NULL,
    is_open        BOOLEAN      NOT NULL DEFAULT FALSE COMMENT 'Đang mở đăng ký (BR-26)',
    -- BR-26: chỉ một học kỳ mở đăng ký. Học kỳ mở có open_flag = 1, các học kỳ
    -- khác là NULL; UNIQUE cho phép nhiều NULL nhưng chỉ một giá trị 1.
    open_flag      TINYINT GENERATED ALWAYS AS (IF(is_open, 1, NULL)) STORED,
    PRIMARY KEY (id),
    CONSTRAINT uq_semesters_code UNIQUE (semester_code),
    CONSTRAINT uq_semesters_open_flag UNIQUE (open_flag),
    CONSTRAINT chk_semesters_code
        CHECK (semester_code REGEXP '^[0-9]{4}-[123]$'),
    CONSTRAINT chk_semesters_name CHECK (CHAR_LENGTH(TRIM(name)) > 0),
    -- BR-19
    CONSTRAINT chk_semesters_dates CHECK (end_date > start_date)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  COMMENT = 'Học kỳ';

-- ---------------------------------------------------------------------
-- Lớp học phần: một học phần mở trong một học kỳ, có thể nhiều lớp
-- ---------------------------------------------------------------------
CREATE TABLE class_sections (
    id             INT          NOT NULL AUTO_INCREMENT,
    section_code   VARCHAR(10)  NOT NULL COMMENT 'Mã lớp, vd IT101-01 (BR-24)',
    course_id      INT          NOT NULL,
    semester_id    INT          NOT NULL,
    lecturer_name  VARCHAR(100)     NULL COMMENT 'Giảng viên phụ trách',
    max_students   SMALLINT     NOT NULL COMMENT 'Sĩ số tối đa (BR-25)',
    PRIMARY KEY (id),
    -- BR-24: mã lớp duy nhất trong một học kỳ
    CONSTRAINT uq_class_sections_code UNIQUE (semester_id, section_code),
    -- BR-16: không xóa học phần / học kỳ đã mở lớp
    CONSTRAINT fk_class_sections_course
        FOREIGN KEY (course_id) REFERENCES courses (id)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_class_sections_semester
        FOREIGN KEY (semester_id) REFERENCES semesters (id)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT chk_class_sections_code
        CHECK (section_code COLLATE utf8mb4_bin REGEXP '^[A-Z]{2,4}[0-9]{3}-[0-9]{2}$'),
    CONSTRAINT chk_class_sections_max CHECK (max_students > 0)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  COMMENT = 'Lớp học phần';

-- ---------------------------------------------------------------------
-- Thời khóa biểu: các buổi học lặp lại hằng tuần của lớp học phần
-- ---------------------------------------------------------------------
CREATE TABLE schedules (
    id            INT         NOT NULL AUTO_INCREMENT,
    section_id    INT         NOT NULL,
    day_of_week   TINYINT     NOT NULL COMMENT '2 = Thứ 2 … 7 = Thứ 7, 8 = Chủ nhật',
    start_period  TINYINT     NOT NULL COMMENT 'Tiết bắt đầu 1–12',
    end_period    TINYINT     NOT NULL COMMENT 'Tiết kết thúc 1–12',
    room          VARCHAR(20)     NULL COMMENT 'Phòng học',
    PRIMARY KEY (id),
    -- Chặn nhập trùng y hệt; trùng khoảng tiết (BR-21) kiểm tra ở Service
    CONSTRAINT uq_schedules_slot UNIQUE (section_id, day_of_week, start_period),
    -- Xóa lớp học phần thì xóa luôn các buổi học
    CONSTRAINT fk_schedules_section
        FOREIGN KEY (section_id) REFERENCES class_sections (id)
        ON UPDATE CASCADE ON DELETE CASCADE,
    -- BR-21
    CONSTRAINT chk_schedules_day CHECK (day_of_week BETWEEN 2 AND 8),
    CONSTRAINT chk_schedules_periods
        CHECK (start_period BETWEEN 1 AND 12
               AND end_period BETWEEN 1 AND 12
               AND start_period <= end_period)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  COMMENT = 'Thời khóa biểu lớp học phần';

-- ---------------------------------------------------------------------
-- Đăng ký học phần và điểm
-- total / letter / gpa4 do phần mềm tính (GpaCalculator, #13) rồi lưu lại;
-- các CHECK bên dưới bảo đảm giá trị lưu khớp BR-09, BR-10.
-- ---------------------------------------------------------------------
CREATE TABLE enrollments (
    id             INT          NOT NULL AUTO_INCREMENT,
    student_id     INT          NOT NULL,
    section_id     INT          NOT NULL,
    registered_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    attendance     DECIMAL(3,1)     NULL COMMENT 'Chuyên cần 0–10 (BR-08)',
    midterm        DECIMAL(3,1)     NULL COMMENT 'Giữa kỳ 0–10 (BR-08)',
    final_exam     DECIMAL(3,1)     NULL COMMENT 'Cuối kỳ 0–10 (BR-08)',
    total          DECIMAL(3,1)     NULL COMMENT 'Điểm tổng kết (BR-09)',
    letter         VARCHAR(2)       NULL COMMENT 'Điểm chữ (BR-10)',
    gpa4           DECIMAL(2,1)     NULL COMMENT 'Điểm hệ 4 (BR-10)',
    PRIMARY KEY (id),
    CONSTRAINT uq_enrollments UNIQUE (student_id, section_id),
    -- BR-14: xóa sinh viên (chưa có điểm) thì xóa luôn đăng ký
    CONSTRAINT fk_enrollments_student
        FOREIGN KEY (student_id) REFERENCES students (id)
        ON UPDATE CASCADE ON DELETE CASCADE,
    -- BR-16: không xóa lớp học phần đã có sinh viên đăng ký
    CONSTRAINT fk_enrollments_section
        FOREIGN KEY (section_id) REFERENCES class_sections (id)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    -- BR-08
    CONSTRAINT chk_enrollments_attendance CHECK (attendance BETWEEN 0 AND 10),
    CONSTRAINT chk_enrollments_midterm    CHECK (midterm    BETWEEN 0 AND 10),
    CONSTRAINT chk_enrollments_final      CHECK (final_exam BETWEEN 0 AND 10),
    -- BR-09: chỉ có điểm tổng kết khi đủ 3 điểm thành phần, và phải đúng công thức
    CONSTRAINT chk_enrollments_total
        CHECK (total IS NULL
               OR (attendance IS NOT NULL AND midterm IS NOT NULL AND final_exam IS NOT NULL
                   AND total = ROUND(attendance * 0.1 + midterm * 0.3 + final_exam * 0.6, 1))),
    -- BR-10: điểm chữ và hệ 4 đi cùng điểm tổng kết, đúng bảng quy đổi.
    -- Dùng <=> vì CHECK chỉ chặn khi kết quả FALSE, so sánh với NULL bằng "="
    -- cho UNKNOWN và sẽ lọt.
    CONSTRAINT chk_enrollments_letter
        CHECK ((total IS NULL AND letter IS NULL)
               OR total IS NOT NULL AND letter <=> CASE
                               WHEN total >= 8.5 THEN 'A'
                               WHEN total >= 8.0 THEN 'B+'
                               WHEN total >= 7.0 THEN 'B'
                               WHEN total >= 6.5 THEN 'C+'
                               WHEN total >= 5.5 THEN 'C'
                               WHEN total >= 5.0 THEN 'D+'
                               WHEN total >= 4.0 THEN 'D'
                               ELSE 'F'
                           END),
    CONSTRAINT chk_enrollments_gpa4
        CHECK ((total IS NULL AND gpa4 IS NULL)
               OR total IS NOT NULL AND gpa4 <=> CASE
                             WHEN total >= 8.5 THEN 4.0
                             WHEN total >= 8.0 THEN 3.5
                             WHEN total >= 7.0 THEN 3.0
                             WHEN total >= 6.5 THEN 2.5
                             WHEN total >= 5.5 THEN 2.0
                             WHEN total >= 5.0 THEN 1.5
                             WHEN total >= 4.0 THEN 1.0
                             ELSE 0.0
                         END)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci
  COMMENT = 'Đăng ký lớp học phần và điểm';
