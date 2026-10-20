---
name: task
description: Hoàn thành trọn một GitHub issue của dự án Quản lý Sinh viên (đọc issue và sub-issue → lập kế hoạch → chờ duyệt → code → test → commit → mở PR). Dùng khi người dùng gọi /task <số issue>, hoặc nói "làm issue #N", "làm task ngày N", "làm task hôm nay".
argument-hint: "<số issue | 'hôm nay'>"
---

# /task — Hoàn thành một issue

Đầu vào: `$ARGUMENTS` là số issue (vd `5`, `#5`). Nếu trống hoặc là "hôm nay", tra bảng **Kế hoạch 20 ngày** trong `README.md` theo ngày hiện tại để chọn issue; nếu hôm nay không có issue thì lấy issue OPEN có số nhỏ nhất. Báo cho người dùng biết đã chọn issue nào.

Mọi commit message, mô tả PR, comment trong code và câu trả lời đều viết **tiếng Việt**. Tên class/biến/method vẫn đặt bằng tiếng Anh theo cấu trúc trong README.

## Bước 1 — Thu thập ngữ cảnh

1. `gh issue view <N>` — lấy tiêu đề, nhãn, milestone, mục **Mục tiêu / Công việc / Hoàn thành khi**, danh sách sub-issue.
2. Với mỗi sub-issue: `gh issue view <số>` để đọc nội dung.
3. Nếu issue ở trạng thái CLOSED → báo người dùng và dừng.
4. Kiểm tra issue phụ thuộc: đọc các issue số nhỏ hơn có liên quan (vd #5 StudentDAO cần #3, #4). Nếu chưa xong, cảnh báo nhưng vẫn cho người dùng quyết định.
5. Đọc `docs/yeu-cau.md` (quy tắc nghiệp vụ BR-xx), `README.md` (cấu trúc thư mục, công nghệ) và các file liên quan đã có trong repo (`docs/`, `database/`, `src/`).
6. `git status` phải sạch; `git checkout main && git pull`.

## Bước 2 — Lập kế hoạch và **dừng chờ duyệt**

Trình bày cho người dùng:
- Tóm tắt mục tiêu issue.
- Danh sách file sẽ tạo/sửa (đường dẫn cụ thể).
- Ánh xạ từng mục checklist `- [ ]` (cả issue cha lẫn sub-issue) → cách làm. Đánh dấu rõ mục nào **phải làm thủ công** (vd cài Docker Desktop, vẽ draw.io, clone repo, thao tác pgAdmin) — skill không làm thay được.
- Các điểm còn mơ hồ cần người dùng chốt (vd định dạng mã SV, trọng số điểm). Hỏi bằng AskUserQuestion nếu có.
- Cách kiểm thử.

**Không viết code trước khi người dùng duyệt kế hoạch.** Nếu người dùng chỉnh sửa, cập nhật kế hoạch rồi mới làm.

## Bước 3 — Thực hiện

1. Tạo branch: `feature/<N>-<slug-tiếng-việt-không-dấu>` (vd `feature/5-student-dao`).
2. Code theo kiến trúc MVC + DAO trong README:
   - Package gốc `com.qlsv`; model / dao / service / controller / view / exception / util.
   - Java 17, JDBC thuần (`PreparedStatement`, try-with-resources), cấu hình DB đọc từ `src/main/resources/db.properties`.
   - Lỗi nhập liệu ném `ValidationException`, vi phạm nghiệp vụ ném `BusinessException`.
   - Tuân thủ quy tắc nghiệp vụ trong `docs/yeu-cau.md` (nguồn chuẩn, mã BR-xx; khi lệch với README hay issue thì theo file này và báo người dùng). Các quy tắc chính: tối đa 12 tín chỉ/học kỳ; một học phần mở nhiều lớp học phần mỗi học kỳ (mã lớp `IT101-01`, có sĩ số tối đa); sinh viên đăng ký theo lớp học phần; không trùng môn trong học kỳ (kể cả khác lớp); không đăng ký lớp trùng lịch học (cùng thứ, khoảng tiết 1–12 giao nhau, kề nhau không tính); chỉ một học kỳ mở đăng ký; điểm 0–10; điểm tổng kết = CC×10% + GK×30% + CK×60% làm tròn 1 số; GPA làm tròn 2 số; học lại lấy điểm cao nhất; bảng quy đổi chữ/hệ 4 trong README. Trong code và thông báo lỗi, ghi mã BR-xx tương ứng.
3. Tài liệu đặt trong `docs/`, SQL đặt trong `database/`.
4. Commit nhỏ theo từng phần việc, message dạng:
   ```
   <loại>(#N): <mô tả ngắn tiếng Việt>
   ```
   `loại` ∈ `feat`, `fix`, `test`, `docs`, `refactor`, `chore`.

## Bước 4 — Kiểm thử

- Có code Java: viết JUnit 5 cho service/validator/GpaCalculator (ưu tiên các ca biên ở mục 5.4 của `docs/yeu-cau.md`: 3.95→4.0, 8.45→8.5, đúng 12 tín chỉ và 10+3, trùng lịch tiết 1–3 với 3–5, kề lịch 1–3 với 4–6, điểm âm/ >10…). Chạy `mvn test`.
- Nếu `mvn` chưa cài (`which mvn` rỗng): báo người dùng và gợi ý `brew install maven`; không tự cài.
- Phần cần CSDL: bật bằng `docker compose up -d` (xem docs/docker.md); nếu không kết nối được thì ghi rõ "chưa kiểm thử với CSDL thật" chứ không tuyên bố là đã chạy được.
- Đối chiếu lại từng mục **Hoàn thành khi** — mục nào chưa đạt phải nói rõ.

## Bước 5 — PR và cập nhật issue

1. `git push -u origin <branch>`.
2. `gh pr create --base main --title "#N <tiêu đề issue>" --body ...` với nội dung:
   ```
   ## Tóm tắt
   - ...

   ## Checklist
   - [x] mục đã làm
   - [ ] mục còn lại (lý do / cần làm thủ công)

   ## Kiểm thử
   - ...

   Closes #N
   Closes #<sub-issue> (chỉ những sub-issue đã hoàn thành đủ)
   ```
3. Không tự merge PR. Không sửa nội dung issue gốc.

## Bước 6 — Báo cáo

Kết thúc bằng tóm tắt ngắn: link PR, những gì đã xong, mục còn phải làm thủ công, kết quả test, và issue tiếp theo trong kế hoạch.
