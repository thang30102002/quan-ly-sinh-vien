# Quản lý Sinh viên & Điểm số

Phần mềm desktop viết bằng Java để quản lý sinh viên, học phần, đăng ký học phần, nhập điểm và tính GPA.

## Chức năng

- Thêm, sửa, xóa, tìm kiếm sinh viên
- Quản lý học phần và học kỳ
- Đăng ký / hủy học phần (tối đa 24 tín chỉ mỗi học kỳ)
- Nhập điểm theo lớp học phần
- Tính GPA học kỳ, GPA tích lũy và xếp loại học lực
- Thống kê, xuất bảng điểm

## Công nghệ

| Thành phần | Công nghệ |
|---|---|
| Ngôn ngữ | Java 17 |
| Build, thư viện | Maven |
| Giao diện | Java Swing |
| Cơ sở dữ liệu | MySQL / MariaDB (XAMPP), quản lý bằng phpMyAdmin |
| Kết nối CSDL | JDBC (`mysql-connector-j`) |
| Kiểm thử | JUnit 5 |
| Kiến trúc | MVC + DAO |

## Cấu trúc thư mục (dự kiến)

```
database/                schema.sql, seed.sql
docs/                    yêu cầu, sơ đồ, test case
src/main/java/com/qlsv/
├── model/               Student, Course, Semester, Enrollment
├── dao/                 truy cập CSDL bằng JDBC
├── service/             quy tắc nghiệp vụ, GpaCalculator
├── controller/          xử lý sự kiện giao diện
├── view/                các màn hình Swing
├── exception/           ValidationException, BusinessException
└── util/                DBConnection, Validator
src/main/resources/      db.properties
src/test/java/           JUnit test
```

## Quy tắc tính điểm

- Điểm tổng kết = Chuyên cần × 10% + Giữa kỳ × 30% + Cuối kỳ × 60%, làm tròn 1 chữ số
- GPA = Σ(điểm hệ 4 × số tín chỉ) / Σ số tín chỉ, làm tròn 2 chữ số
- Học lại một môn thì lấy lần điểm cao nhất

| Thang 10 | 8.5–10 | 8.0–8.4 | 7.0–7.9 | 6.5–6.9 | 5.5–6.4 | 5.0–5.4 | 4.0–4.9 | < 4.0 |
|---|---|---|---|---|---|---|---|---|
| Điểm chữ | A | B+ | B | C+ | C | D+ | D | F |
| Thang 4 | 4.0 | 3.5 | 3.0 | 2.5 | 2.0 | 1.5 | 1.0 | 0 |

## Kế hoạch 20 ngày

Theo dõi tiến độ tại [Milestones](https://github.com/thang30102002/quan-ly-sinh-vien/milestones) và [Issues](https://github.com/thang30102002/quan-ly-sinh-vien/issues).

| Tuần | Ngày dự kiến | Công việc |
|---|---|---|
| 1 | T2 19/10 | [#1 Phân tích yêu cầu và quy tắc nghiệp vụ](https://github.com/thang30102002/quan-ly-sinh-vien/issues/1) |
| 1 | T3 20/10 | [#2 Thiết kế cơ sở dữ liệu](https://github.com/thang30102002/quan-ly-sinh-vien/issues/2) |
| 1 | T4 21/10 | [#3 Khởi tạo project Maven và kết nối CSDL](https://github.com/thang30102002/quan-ly-sinh-vien/issues/3) |
| 1 | T5 22/10 | [#4 Lớp Model và Validator](https://github.com/thang30102002/quan-ly-sinh-vien/issues/4) |
| 1 | T6 23/10 | [#5 StudentDAO](https://github.com/thang30102002/quan-ly-sinh-vien/issues/5) |
| 2 | T2 26/10 | [#6 StudentService](https://github.com/thang30102002/quan-ly-sinh-vien/issues/6) |
| 2 | T3 27/10 | [#7 Khung giao diện chính](https://github.com/thang30102002/quan-ly-sinh-vien/issues/7) |
| 2 | T4 28/10 | [#8 Giao diện quản lý sinh viên](https://github.com/thang30102002/quan-ly-sinh-vien/issues/8) |
| 2 | T5 29/10 | [#9 Quản lý học phần và học kỳ](https://github.com/thang30102002/quan-ly-sinh-vien/issues/9) |
| 2 | T6 30/10 | [#10 Kiểm thử và refactor tuần 2](https://github.com/thang30102002/quan-ly-sinh-vien/issues/10) |
| 3 | T2 02/11 | [#11 Logic đăng ký học phần](https://github.com/thang30102002/quan-ly-sinh-vien/issues/11) |
| 3 | T3 03/11 | [#12 Giao diện đăng ký học phần](https://github.com/thang30102002/quan-ly-sinh-vien/issues/12) |
| 3 | T4 04/11 | [#13 GpaCalculator: tính điểm và quy đổi](https://github.com/thang30102002/quan-ly-sinh-vien/issues/13) |
| 3 | T5 05/11 | [#14 Nhập điểm theo lớp học phần](https://github.com/thang30102002/quan-ly-sinh-vien/issues/14) |
| 3 | T6 06/11 | [#15 GPA tích lũy và bảng điểm cá nhân](https://github.com/thang30102002/quan-ly-sinh-vien/issues/15) |
| 4 | T2 09/11 | [#16 Thống kê và báo cáo](https://github.com/thang30102002/quan-ly-sinh-vien/issues/16) |
| 4 | T3 10/11 | [#17 Xuất và nhập file](https://github.com/thang30102002/quan-ly-sinh-vien/issues/17) |
| 4 | T4 11/11 | [#18 Đăng nhập và phân quyền (tùy chọn)](https://github.com/thang30102002/quan-ly-sinh-vien/issues/18) |
| 4 | T5 12/11 | [#19 Kiểm thử tổng thể và đóng gói .jar](https://github.com/thang30102002/quan-ly-sinh-vien/issues/19) |
| 4 | T6 13/11 | [#20 Tài liệu và chuẩn bị demo](https://github.com/thang30102002/quan-ly-sinh-vien/issues/20) |

## Cài đặt và chạy

> Phần này sẽ được hoàn thiện ở [#20](https://github.com/thang30102002/quan-ly-sinh-vien/issues/20).

1. Cài JDK 17 và XAMPP, bật MySQL trong XAMPP Control Panel
2. Trong phpMyAdmin, chạy `database/schema.sql` rồi `database/seed.sql`
3. Sửa `src/main/resources/db.properties` nếu tài khoản database khác mặc định
4. Build: `mvn clean package`
5. Chạy: `java -jar target/qlsv.jar`
