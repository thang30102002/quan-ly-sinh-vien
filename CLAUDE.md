# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Dự án

Phần mềm desktop Quản lý Sinh viên & Điểm số: Java 17, Swing, Maven, JDBC thuần (`org.postgresql:postgresql`), PostgreSQL 16 chạy bằng Docker Compose, JUnit 5. Đồ án nhóm làm theo **Kế hoạch 20 ngày** trong `README.md`, mỗi ngày một GitHub issue (#1–#20). Mỗi issue có sub-issue chia cho các thành viên A/B/C.

Trạng thái hiện tại: có tài liệu (`docs/`), SQL (`database/`), môi trường Docker (`docker-compose.yml`) và project Maven (#3) với `util/DBConnection`, `db.properties`, `DBConnectionTest`. Các package khác mới chỉ có `package-info.java`. Hướng dẫn cài môi trường: `docs/cai-dat.md`.

## Ngôn ngữ và quy ước

- Commit message, mô tả PR, comment trong code, thông báo lỗi và câu trả lời đều viết **tiếng Việt**. Tên class, biến, method, bảng, cột đặt bằng tiếng Anh.
- Commit: `<loại>(#N): <mô tả tiếng Việt>`, `loại` ∈ `feat`, `fix`, `test`, `docs`, `refactor`, `chore`. Branch: `feature/<N>-<slug-không-dấu>`.
- Làm một issue trọn vẹn bằng skill `/task <số issue>` (`.claude/skills/task/SKILL.md`). Skill này lập kế hoạch và chờ duyệt rồi mới code, mở PR nhưng không tự merge.

## Lệnh

```bash
# Database (lần đầu: cp .env.example .env). Volume trống thì tự chạy schema.sql rồi seed.sql
docker compose up -d                               # bật PostgreSQL + pgAdmin (http://localhost:5050)
docker compose down -v && docker compose up -d     # tạo lại từ đầu sau khi sửa script
docker compose exec -T db psql -U qlsv -d qlsv -v ON_ERROR_STOP=1 < database/schema.sql   # chạy lại không xóa volume
docker compose exec db psql -U qlsv -d qlsv        # mở psql

# Maven (test cần database Docker đang chạy)
mvn clean package                      # build ra target/qlsv.jar
mvn test                               # chạy toàn bộ test
mvn test -Dtest=<TênClassTest>                # một class
mvn test -Dtest=<TênClassTest>#<tênMethod>    # một method
java -jar target/qlsv.jar
docker compose run --rm maven mvn test             # khi máy chưa cài Maven
```

Môi trường máy dev:
- `mvn` và JDK chưa cài (`brew install maven`), hoặc dùng service `maven` của Docker.
- Một PostgreSQL cài sẵn đang chiếm cổng 5432, nên cổng của DB Docker mặc định là **5435** (`DB_PORT` trong `.env`). JDBC URL từ máy thật: `jdbc:postgresql://localhost:5435/qlsv`, user/mật khẩu `qlsv`/`qlsv`. Từ container khác thì dùng `db:5432`. Chi tiết xem `docs/docker.md`.
- Script SQL viết cho PostgreSQL 16 và chạy bằng `psql` với `ON_ERROR_STOP`.

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

- **CSDL chặn:** định dạng mã (CHECK với toán tử `~`, phân biệt hoa thường, nên Java phải `trim().toUpperCase()` trước khi lưu), tín chỉ 1–6, sĩ số > 0, thứ/tiết, điểm 0–10, và chỉ một học kỳ mở (partial unique index `uq_semesters_open ... WHERE is_open`).
- **CHECK cho điểm:** `enrollments.total/letter/gpa4` do Java tính rồi lưu lại, nhưng CHECK bắt chúng khớp đúng công thức BR-09 và bảng quy đổi BR-10. Nếu Java tính lệch thì INSERT/UPDATE bị lỗi. Phải ghi ba cột này cùng lúc, và cùng NULL khi chưa đủ 3 điểm thành phần.
- **FK:** RESTRICT khi xóa học phần, học kỳ, lớp đang được dùng (BR-16). CASCADE cho `schedules` khi xóa lớp và cho `enrollments` khi xóa sinh viên (Service phải kiểm tra BR-14 trước khi xóa).
- **Service phải tự kiểm tra:** BR-04, 05, 07, 14, 15, 17 (email, tuổi), 21 (các buổi trong một lớp không trùng nhau), 22, 23, 24 (tiền tố mã lớp), 25, 26 (để báo lỗi tiếng Việt thay vì lỗi `duplicate key value`).
- `students.status` và `gender` là `VARCHAR` + CHECK chứa mã tiếng Anh (`STUDYING/RESERVED/DROPPED/GRADUATED`, `MALE/FEMALE/OTHER`), không dùng kiểu ENUM của PostgreSQL nên DAO gán bằng `setString`; giao diện hiển thị nhãn tiếng Việt.
- Đổi cấu trúc bảng thì sửa `schema.sql` rồi chạy lại. Không sửa trực tiếp bằng pgAdmin.
- `seed.sql` dùng id cố định, cuối file gọi `setval` để bộ đếm IDENTITY chạy tiếp sau id lớn nhất (thêm bảng có seed thì nhớ thêm dòng này). Đầu file liệt kê sẵn các ca dữ liệu để thử từng quy tắc (trùng/kề lịch, 12 và 10 tín chỉ, lớp đầy, học lại, làm tròn), nên dùng lại khi viết test.
