-- =====================================================================
-- Quản lý Sinh viên & Điểm số: dữ liệu mẫu (issue #2)
--
-- Chạy SAU database/schema.sql (schema.sql xóa và tạo lại database, nên
-- muốn nạp lại dữ liệu mẫu thì chạy lại cả hai file theo thứ tự).
--
-- Tóm tắt:
--   20 sinh viên, 10 học phần, 2 học kỳ (2025-2 đã đóng, 2026-1 đang mở),
--   15 lớp học phần (IT102, IT201 mở 2 lớp ở HK 2026-1), 17 buổi học,
--   đăng ký có điểm ở HK 2025-2 và đăng ký chưa có điểm ở HK 2026-1.
--
-- Các ca dùng để thử quy tắc nghiệp vụ (xem docs/yeu-cau.md):
--   BR-22 trùng lịch : IT102-01 (Thứ 2, tiết 1–3) và IT201-01 (Thứ 2, tiết 3–5)
--                      (IT201-01 cũng trùng với IT202-01, Thứ 2, tiết 4–5)
--   BR-22 kề lịch    : IT102-01 (Thứ 2, tiết 1–3) và IT202-01 (Thứ 2, tiết 4–6)
--   BR-04 12 tín chỉ : SV2024001 đã đăng ký đúng 12 tín chỉ ở HK 2026-1;
--                      SV2024002 có 10 tín chỉ, thêm lớp 3 tín chỉ thì bị từ chối
--   BR-05 trùng môn  : SV2024001 đã học IT102-01, thử đăng ký thêm IT102-02
--   BR-25 lớp đầy    : IT301-01 sĩ số tối đa 3, đã có 3 sinh viên
--   BR-06/BR-12      : SV2024003 trượt IT101 (2.6, F) ở HK 2025-2, học lại ở HK 2026-1
--   BR-07            : SV2024008 đang bảo lưu, không được đăng ký
--   BR-09 làm tròn   : SV2024002 IT101 = 3.95 → 4.0 (D), MATH101 = 8.45 → 8.5 (A);
--                      SV2024005 IT101 = 7.25 → 7.3
--   BR-14            : SV2024009 (Thôi học) đã có điểm nên không xóa được;
--                      SV2025005–SV2025010 chưa đăng ký lớp nào, xóa được
--   BR-08            : SV2024007 lớp PE101-01 HK 2025-2 mới có điểm chuyên cần
-- =====================================================================

USE qlsv;
SET NAMES utf8mb4;

-- ---------------------------------------------------------------------
-- Sinh viên (id 1–20)
-- ---------------------------------------------------------------------
INSERT INTO students
    (id, student_code, full_name, dob, gender, email, phone, class_name, intake_year, status)
VALUES
    ( 1, 'SV2024001', 'Nguyễn Văn An',      '2006-03-12', 'MALE',   'sv2024001@st.qlsv.edu.vn', '0912345601', 'CNTT-K24A', 2024, 'STUDYING'),
    ( 2, 'SV2024002', 'Trần Thị Bích Ngọc', '2006-07-25', 'FEMALE', 'sv2024002@st.qlsv.edu.vn', '0912345602', 'CNTT-K24A', 2024, 'STUDYING'),
    ( 3, 'SV2024003', 'Lê Hoàng Long',      '2006-01-08', 'MALE',   'sv2024003@st.qlsv.edu.vn', '0912345603', 'CNTT-K24A', 2024, 'STUDYING'),
    ( 4, 'SV2024004', 'Phạm Thu Hương',     '2006-11-30', 'FEMALE', 'sv2024004@st.qlsv.edu.vn', NULL,         'CNTT-K24A', 2024, 'STUDYING'),
    ( 5, 'SV2024005', 'Hoàng Minh Tuấn',    '2006-05-17', 'MALE',   'sv2024005@st.qlsv.edu.vn', '0912345605', 'CNTT-K24B', 2024, 'STUDYING'),
    ( 6, 'SV2024006', 'Vũ Thị Lan Anh',     '2006-09-03', 'FEMALE', NULL,                       '0912345606', 'CNTT-K24B', 2024, 'STUDYING'),
    ( 7, 'SV2024007', 'Đặng Quốc Bảo',      '2006-02-14', 'MALE',   'sv2024007@st.qlsv.edu.vn', '0912345607', 'CNTT-K24B', 2024, 'STUDYING'),
    ( 8, 'SV2024008', 'Bùi Ngọc Ánh',       '2006-12-21', 'FEMALE', 'sv2024008@st.qlsv.edu.vn', '0912345608', 'CNTT-K24B', 2024, 'RESERVED'),
    ( 9, 'SV2024009', 'Đỗ Đức Thắng',       '2006-06-06', 'MALE',   'sv2024009@st.qlsv.edu.vn', NULL,         'CNTT-K24B', 2024, 'DROPPED'),
    (10, 'SV2022001', 'Ngô Thị Mai',        '2004-04-19', 'FEMALE', 'sv2022001@st.qlsv.edu.vn', '0987654310', 'CNTT-K22A', 2022, 'GRADUATED'),
    (11, 'SV2025001', 'Dương Văn Khánh',    '2007-08-09', 'MALE',   'sv2025001@st.qlsv.edu.vn', '0976543211', 'CNTT-K25A', 2025, 'STUDYING'),
    (12, 'SV2025002', 'Lý Thị Thảo',        '2007-10-15', 'FEMALE', 'sv2025002@st.qlsv.edu.vn', '0976543212', 'CNTT-K25A', 2025, 'STUDYING'),
    (13, 'SV2025003', 'Phan Thanh Sơn',     '2007-03-27', 'MALE',   'sv2025003@st.qlsv.edu.vn', '0976543213', 'CNTT-K25A', 2025, 'STUDYING'),
    (14, 'SV2025004', 'Trịnh Khánh Linh',   '2007-01-02', 'FEMALE', 'sv2025004@st.qlsv.edu.vn', NULL,         'CNTT-K25A', 2025, 'STUDYING'),
    (15, 'SV2025005', 'Huỳnh Gia Huy',      '2007-05-30', 'MALE',   'sv2025005@st.qlsv.edu.vn', '0976543215', 'CNTT-K25A', 2025, 'STUDYING'),
    (16, 'SV2025006', 'Mai Phương Thúy',    '2007-07-07', 'FEMALE', 'sv2025006@st.qlsv.edu.vn', '0976543216', 'CNTT-K25B', 2025, 'STUDYING'),
    (17, 'SV2025007', 'Tạ Quang Vinh',      '2007-09-18', 'MALE',   NULL,                       '0976543217', 'CNTT-K25B', 2025, 'STUDYING'),
    (18, 'SV2025008', 'Đinh Thị Yến',       '2007-11-11', 'FEMALE', 'sv2025008@st.qlsv.edu.vn', '0976543218', 'CNTT-K25B', 2025, 'STUDYING'),
    (19, 'SV2025009', 'Lương Nhật Minh',    '2007-02-28', 'MALE',   'sv2025009@st.qlsv.edu.vn', '0976543219', 'CNTT-K25B', 2025, 'STUDYING'),
    (20, 'SV2025010', 'Châu Ngọc Trâm',     '2007-12-01', 'FEMALE', 'sv2025010@st.qlsv.edu.vn', '0976543220', 'CNTT-K25B', 2025, 'STUDYING');

-- ---------------------------------------------------------------------
-- Học phần (id 1–10)
-- ---------------------------------------------------------------------
INSERT INTO courses (id, course_code, name, credits, description) VALUES
    ( 1, 'IT101',   'Nhập môn lập trình',             3, 'Tư duy lập trình, cấu trúc điều khiển, hàm, mảng'),
    ( 2, 'IT102',   'Cấu trúc dữ liệu và giải thuật', 3, 'Danh sách, ngăn xếp, hàng đợi, cây, sắp xếp, tìm kiếm'),
    ( 3, 'IT201',   'Cơ sở dữ liệu',                  3, 'Mô hình quan hệ, chuẩn hóa, SQL'),
    ( 4, 'IT202',   'Lập trình hướng đối tượng',      3, 'Lớp, đối tượng, kế thừa, đa hình với Java'),
    ( 5, 'IT301',   'Mạng máy tính',                  3, 'Mô hình OSI, TCP/IP, định tuyến'),
    ( 6, 'MATH101', 'Giải tích 1',                    4, 'Giới hạn, đạo hàm, tích phân hàm một biến'),
    ( 7, 'MATH102', 'Đại số tuyến tính',              3, 'Ma trận, định thức, không gian vectơ'),
    ( 8, 'PHY101',  'Vật lý đại cương',               3, 'Cơ học, nhiệt học, điện từ học'),
    ( 9, 'ENG101',  'Tiếng Anh 1',                    2, NULL),
    (10, 'PE101',   'Giáo dục thể chất 1',            1, NULL);

-- ---------------------------------------------------------------------
-- Học kỳ: chỉ HK 2026-1 đang mở đăng ký (BR-26)
-- ---------------------------------------------------------------------
INSERT INTO semesters (id, semester_code, name, start_date, end_date, is_open) VALUES
    (1, '2025-2', 'Học kỳ 2 năm học 2025–2026', '2026-01-12', '2026-05-31', FALSE),
    (2, '2026-1', 'Học kỳ 1 năm học 2026–2027', '2026-09-07', '2027-01-17', TRUE);

-- ---------------------------------------------------------------------
-- Lớp học phần (id 1–6: HK 2025-2, id 7–15: HK 2026-1)
-- ---------------------------------------------------------------------
INSERT INTO class_sections (id, section_code, course_id, semester_id, lecturer_name, max_students) VALUES
    ( 1, 'IT101-01',   1, 1, 'ThS. Nguyễn Hữu Phước', 40),
    ( 2, 'MATH101-01', 6, 1, 'TS. Trần Thị Bình',     50),
    ( 3, 'ENG101-01',  9, 1, 'ThS. Lê Thu Hà',        35),
    ( 4, 'PHY101-01',  8, 1, 'TS. Phạm Minh Đức',     45),
    ( 5, 'MATH102-01', 7, 1, 'TS. Trần Thị Bình',     50),
    ( 6, 'PE101-01',  10, 1, 'ThS. Võ Quốc Hùng',     60),
    ( 7, 'IT101-01',   1, 2, 'ThS. Nguyễn Hữu Phước', 40),
    ( 8, 'IT102-01',   2, 2, 'ThS. Nguyễn Hữu Phước', 40),
    ( 9, 'IT102-02',   2, 2, 'ThS. Đặng Thu Trang',   40),
    (10, 'IT201-01',   3, 2, 'TS. Hoàng Văn Nam',     45),
    (11, 'IT201-02',   3, 2, 'TS. Hoàng Văn Nam',     45),
    (12, 'IT202-01',   4, 2, 'ThS. Đặng Thu Trang',   40),
    (13, 'IT301-01',   5, 2, 'TS. Bùi Quang Huy',      3),
    (14, 'MATH102-01', 7, 2, 'TS. Trần Thị Bình',     50),
    (15, 'PE101-01',  10, 2, 'ThS. Võ Quốc Hùng',     60);

-- ---------------------------------------------------------------------
-- Thời khóa biểu (thứ: 2–7 = Thứ 2–Thứ 7, 8 = Chủ nhật)
-- ---------------------------------------------------------------------
INSERT INTO schedules (section_id, day_of_week, start_period, end_period, room) VALUES
    -- HK 2025-2
    ( 1, 2,  1,  3, 'A101'),          -- IT101-01
    ( 2, 3,  1,  3, 'B201'),          -- MATH101-01, buổi 1
    ( 2, 5,  1,  2, 'B201'),          -- MATH101-01, buổi 2
    ( 3, 4,  7,  8, 'C301'),          -- ENG101-01
    ( 4, 6,  4,  6, 'B202'),          -- PHY101-01
    ( 5, 2,  7,  9, 'B201'),          -- MATH102-01
    ( 6, 7,  1,  2, 'Nhà thi đấu'),   -- PE101-01
    -- HK 2026-1
    ( 7, 4,  7,  9, 'A101'),          -- IT101-01
    ( 8, 2,  1,  3, 'A201'),          -- IT102-01, buổi 1
    ( 8, 4,  1,  2, 'A201'),          -- IT102-01, buổi 2
    ( 9, 3,  7,  9, 'A202'),          -- IT102-02
    (10, 2,  3,  5, 'B101'),          -- IT201-01: TRÙNG tiết 3 với IT102-01
    (11, 5,  7,  9, 'B101'),          -- IT201-02
    (12, 2,  4,  6, 'B102'),          -- IT202-01: KỀ (không trùng) với IT102-01
    (13, 6,  1,  3, 'C101'),          -- IT301-01
    (14, 3,  1,  3, 'B201'),          -- MATH102-01
    (15, 8,  1,  2, 'Nhà thi đấu');   -- PE101-01, Chủ nhật

-- ---------------------------------------------------------------------
-- Đăng ký và điểm HK 2025-2 (đã kết thúc)
-- total = ROUND(CC×0.1 + GK×0.3 + CK×0.6, 1); letter, gpa4 theo bảng quy đổi.
-- Schema có CHECK nên nếu tính sai thì lệnh INSERT sẽ báo lỗi.
-- ---------------------------------------------------------------------
INSERT INTO enrollments
    (student_id, section_id, registered_at, attendance, midterm, final_exam, total, letter, gpa4)
VALUES
    -- SV2024001: ví dụ ở mục 5.1 docs/yeu-cau.md
    ( 1, 1, '2026-01-05 08:00:00',  9.0, 7.0, 6.5, 6.9, 'C+', 2.5),
    ( 1, 2, '2026-01-05 08:00:00',  8.0, 8.0, 9.0, 8.6, 'A',  4.0),
    ( 1, 3, '2026-01-05 08:00:00', 10.0, 8.0, 8.5, 8.5, 'A',  4.0),
    -- SV2024002: ca biên làm tròn 3.95 → 4.0 và 8.45 → 8.5
    ( 2, 1, '2026-01-05 08:10:00',  3.5, 4.0, 4.0, 4.0, 'D',  1.0),
    ( 2, 2, '2026-01-05 08:10:00',  8.0, 8.5, 8.5, 8.5, 'A',  4.0),
    -- SV2024003: trượt IT101, học lại ở HK 2026-1
    ( 3, 1, '2026-01-05 08:20:00',  5.0, 3.0, 2.0, 2.6, 'F',  0.0),
    ( 3, 5, '2026-01-05 08:20:00',  7.0, 6.0, 7.0, 6.7, 'C+', 2.5),
    ( 3, 6, '2026-01-05 08:20:00', 10.0, 9.0, 9.0, 9.1, 'A',  4.0),
    -- SV2024004
    ( 4, 1, '2026-01-05 08:30:00',  9.0, 8.0, 8.0, 8.1, 'B+', 3.5),
    ( 4, 4, '2026-01-05 08:30:00',  6.0, 5.0, 5.5, 5.4, 'D+', 1.5),
    ( 4, 3, '2026-01-05 08:30:00',  7.0, 7.0, 7.0, 7.0, 'B',  3.0),
    -- SV2024005: 7.25 → 7.3
    ( 5, 2, '2026-01-05 08:40:00',  4.0, 3.0, 3.5, 3.4, 'F',  0.0),
    ( 5, 1, '2026-01-05 08:40:00',  8.0, 7.5, 7.0, 7.3, 'B',  3.0),
    -- SV2024006
    ( 6, 1, '2026-01-05 08:50:00', 10.0, 9.0, 9.5, 9.4, 'A',  4.0),
    ( 6, 5, '2026-01-05 08:50:00',  6.0, 6.0, 6.0, 6.0, 'C',  2.0),
    -- SV2024007: PE101 mới có điểm chuyên cần, chưa tính tổng kết (BR-08)
    ( 7, 4, '2026-01-05 09:00:00',  7.0, 5.0, 4.0, 4.6, 'D',  1.0),
    ( 7, 6, '2026-01-05 09:00:00',  9.0, NULL, NULL, NULL, NULL, NULL),
    -- SV2024009 (Thôi học): đã có điểm nên không xóa được (BR-14)
    ( 9, 1, '2026-01-05 09:10:00',  2.0, 3.0, 1.0, 1.7, 'F',  0.0),
    -- SV2025001
    (11, 2, '2026-01-06 08:00:00',  7.0, 8.0, 7.5, 7.6, 'B',  3.0),
    (11, 3, '2026-01-06 08:00:00',  9.0, 8.0, 9.0, 8.7, 'A',  4.0),
    -- SV2025002
    (12, 2, '2026-01-06 08:10:00',  5.0, 5.0, 5.0, 5.0, 'D+', 1.5),
    (12, 6, '2026-01-06 08:10:00',  8.0, 8.0, 8.0, 8.0, 'B+', 3.5);

-- ---------------------------------------------------------------------
-- Đăng ký HK 2026-1 (đang mở, chưa có điểm)
-- ---------------------------------------------------------------------
INSERT INTO enrollments (student_id, section_id, registered_at) VALUES
    -- SV2024001: IT102-01 + IT202-01 + IT301-01 + MATH102-01 = 12 tín chỉ (BR-04)
    ( 1,  8, '2026-08-25 09:00:00'),
    ( 1, 12, '2026-08-25 09:00:00'),
    ( 1, 13, '2026-08-25 09:00:00'),
    ( 1, 14, '2026-08-25 09:00:00'),
    -- SV2024002: IT102-01 + IT301-01 + MATH102-01 + PE101-01 = 10 tín chỉ
    ( 2,  8, '2026-08-25 09:10:00'),
    ( 2, 13, '2026-08-25 09:10:00'),
    ( 2, 14, '2026-08-25 09:10:00'),
    ( 2, 15, '2026-08-25 09:10:00'),
    -- SV2024003: học lại IT101 (BR-06)
    ( 3,  7, '2026-08-25 09:20:00'),
    -- SV2024004: IT301-01 đủ 3/3 sinh viên sau đăng ký này (BR-25)
    ( 4, 13, '2026-08-25 09:30:00'),
    ( 4, 10, '2026-08-25 09:30:00'),
    ( 5,  9, '2026-08-25 09:40:00'),
    (11,  7, '2026-08-26 08:00:00'),
    (12,  7, '2026-08-26 08:10:00'),
    (13, 11, '2026-08-26 08:20:00'),
    (14, 15, '2026-08-26 08:30:00');
