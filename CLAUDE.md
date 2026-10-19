# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Dự án

Phần mềm desktop Quản lý Sinh viên & Điểm số: Java 17, Swing, Maven, JDBC thuần (`mysql-connector-j`), MySQL/MariaDB (XAMPP), JUnit 5. Đồ án nhóm làm theo **Kế hoạch 20 ngày** trong `README.md`, mỗi ngày một GitHub issue (#1–#20). Mỗi issue có sub-issue chia cho các thành viên A/B/C.

Trạng thái hiện tại: mới có tài liệu (`docs/`) và SQL (`database/`). Project Maven và `src/` sẽ được tạo ở #3, nên các lệnh Maven bên dưới chỉ dùng được sau khi xong #3.

## Ngôn ngữ và quy ước

- Commit message, mô tả PR, comment trong code, thông báo lỗi và câu trả lời đều viết **tiếng Việt**. Tên class, biến, method, bảng, cột đặt bằng tiếng Anh.
- Commit: `<loại>(#N): <mô tả tiếng Việt>`, `loại` ∈ `feat`, `fix`, `test`, `docs`, `refactor`, `chore`. Branch: `feature/<N>-<slug-không-dấu>`.
- Làm một issue trọn vẹn bằng skill `/task <số issue>` (`.claude/skills/task/SKILL.md`). Skill này lập kế hoạch và chờ duyệt rồi mới code, mở PR nhưng không tự merge.

## Lệnh

```bash
# Tạo lại database từ đầu (schema.sql chạy DROP DATABASE qlsv trước)
mysql -u root --default-character-set=utf8mb4 < database/schema.sql
mysql -u root --default-character-set=utf8mb4 < database/seed.sql

# Sau khi có project Maven (#3)
mvn clean package                      # build ra target/qlsv.jar
mvn test                               # chạy toàn bộ test
mvn test -Dtest=<TênClassTest>                # một class
mvn test -Dtest=<TênClassTest>#<tênMethod>    # một method
java -jar target/qlsv.jar
```

Môi trường máy dev:
- `mvn` chưa cài (`brew install maven`).
- MySQL 9.3 của Homebrew đang chiếm cổng 3306, Docker đang chiếm cổng 80. Phải tắt chúng thì Apache/MariaDB của XAMPP mới chạy được (phpMyAdmin: `http://localhost/phpmyadmin`).
- Script SQL phải chạy được trên cả MySQL 8.0.16+ và MariaDB 10.3 (bản đi kèm XAMPP), nên chỉ dùng cú pháp mà cả hai cùng hỗ trợ.

## Nguồn chuẩn về nghiệp vụ

`docs/yeu-cau.md` là nguồn chuẩn. Mỗi quy tắc có mã **BR-01…BR-26**; code, thông báo lỗi và test phải ghi mã BR tương ứng. Nếu README hoặc issue lệch với file này thì làm theo file này và báo người dùng. Muốn đổi quy tắc thì sửa file này trước.

Những điểm hay sai:
- Sinh viên đăng ký theo **lớp học phần** (`class_sections`, mã `IT101-01`), không đăng ký trực tiếp học phần. Một học phần có thể mở nhiều lớp trong cùng học kỳ.
- Trùng lịch (BR-22): cùng thứ và `startA ≤ endB && startB ≤ endA`. Buổi kề nhau (1–3 và 4–6) **không** tính là trùng. `day_of_week`: 2 = Thứ 2 … 8 = Chủ nhật; tiết từ 1 đến 12.
- Tối đa 12 tín chỉ/học kỳ: đúng 12 là hợp lệ. Không trùng môn trong học kỳ, kể cả khác lớp.
- Điểm tổng kết = CC×0.1 + GK×0.3 + CK×0.6, làm tròn 1 chữ số theo **nửa lên** (8.45 → 8.5, 3.95 → 4.0). Trong Java dùng `BigDecimal` với `RoundingMode.HALF_UP`, không dùng `double`. GPA làm tròn 2 chữ số. Học lại thì lấy lần điểm cao nhất.
- Lỗi nhập liệu ném `ValidationException`, vi phạm nghiệp vụ ném `BusinessException`.

## Kiến trúc

MVC + DAO, package gốc `com.qlsv`: `model` → `dao` (JDBC, `PreparedStatement`, try-with-resources) → `service` (quy tắc nghiệp vụ, `GpaCalculator`) → `controller` → `view` (Swing). Ngoài ra có `exception` và `util` (`DBConnection`, `Validator`). Cấu hình kết nối CSDL đặt ở `src/main/resources/db.properties`.

### CSDL (`database/`, mô tả trong `docs/erd.md`)

Gồm 6 bảng: `students`, `courses`, `semesters`, `class_sections`, `schedules`, `enrollments`. Quy tắc nghiệp vụ được chia giữa CSDL và Service:

- **CSDL chặn:** định dạng mã (CHECK REGEXP, phân biệt hoa thường, nên Java phải `trim().toUpperCase()` trước khi lưu), tín chỉ 1–6, sĩ số > 0, thứ/tiết, điểm 0–10, và chỉ một học kỳ mở (cột sinh ra `open_flag` + UNIQUE).
- **CHECK cho điểm:** `enrollments.total/letter/gpa4` do Java tính rồi lưu lại, nhưng CHECK bắt chúng khớp đúng công thức BR-09 và bảng quy đổi BR-10. Nếu Java tính lệch thì INSERT/UPDATE bị lỗi. Phải ghi ba cột này cùng lúc, và cùng NULL khi chưa đủ 3 điểm thành phần.
- **FK:** RESTRICT khi xóa học phần, học kỳ, lớp đang được dùng (BR-16). CASCADE cho `schedules` khi xóa lớp và cho `enrollments` khi xóa sinh viên (Service phải kiểm tra BR-14 trước khi xóa).
- **Service phải tự kiểm tra:** BR-04, 05, 07, 14, 15, 17 (email, tuổi), 21 (các buổi trong một lớp không trùng nhau), 22, 23, 24 (tiền tố mã lớp), 25, 26 (để báo lỗi tiếng Việt thay vì lỗi `Duplicate entry`).
- `students.status` và `gender` là ENUM mã tiếng Anh (`STUDYING/RESERVED/DROPPED/GRADUATED`, `MALE/FEMALE/OTHER`); giao diện hiển thị nhãn tiếng Việt.
- Đổi cấu trúc bảng thì sửa `schema.sql` rồi chạy lại. Không sửa trực tiếp bằng phpMyAdmin.
- `seed.sql` dùng id cố định. Đầu file liệt kê sẵn các ca dữ liệu để thử từng quy tắc (trùng/kề lịch, 12 và 10 tín chỉ, lớp đầy, học lại, làm tròn), nên dùng lại khi viết test.
