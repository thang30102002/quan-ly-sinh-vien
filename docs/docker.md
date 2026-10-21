# Môi trường dev bằng Docker

> Issue: [#83](https://github.com/thang30102002/quan-ly-sinh-vien/issues/83) · Cấu hình: [`docker-compose.yml`](../docker-compose.yml), [`.env.example`](../.env.example)

Docker chạy database PostgreSQL 16 có sẵn dữ liệu mẫu, kèm pgAdmin để xem database trên web, nên cả nhóm dùng cùng một môi trường mà không phải cài PostgreSQL lên máy. Ứng dụng Swing vẫn chạy trên máy thật (`java -jar target/qlsv.jar`) và kết nối tới database trong Docker qua `localhost`.

| Service | Bật bằng | Dùng để |
|---|---|---|
| `db` | `docker compose up -d` | PostgreSQL 16, database `qlsv` |
| `pgadmin` | `docker compose up -d` (bật cùng `db`) | Giao diện web để xem, truy vấn dữ liệu và vẽ ERD. App không cần nó |
| `maven` | `docker compose run --rm maven mvn ...` | Build/test khi máy chưa cài Maven (tùy chọn) |

## 1. Cài đặt lần đầu

1. Cài [Docker Desktop](https://www.docker.com/products/docker-desktop/) và mở nó lên.
2. Ở thư mục gốc của repo:

   ```bash
   cp .env.example .env
   docker compose up -d
   docker compose ps        # qlsv-db phải ở trạng thái "healthy", qlsv-pgadmin "running"
   ```

Lần đầu khởi động, Docker tạo database `qlsv` rồi chạy `database/schema.sql`, sau đó `database/seed.sql`. Container chỉ chuyển sang `healthy` khi đã nạp xong dữ liệu mẫu.

## 2. Thông tin kết nối

Giá trị mặc định lấy từ `.env.example`:

| | Từ máy thật (app, test JUnit) | Từ container khác (`maven`, `pgadmin`) |
|---|---|---|
| Host | `localhost` | `db` |
| Cổng | `5435` (`DB_PORT`) | `5432` |
| Database | `qlsv` | `qlsv` |
| User / mật khẩu | `qlsv` / `qlsv` (`DB_USER`, `DB_PASSWORD`) | như bên trái |
| JDBC URL | `jdbc:postgresql://localhost:5435/qlsv` | `jdbc:postgresql://db:5432/qlsv` |

Cổng mặc định là 5435, không phải 5432, để không đụng PostgreSQL cài sẵn trên máy.

`src/main/resources/db.properties` (tạo ở [#3](https://github.com/thang30102002/quan-ly-sinh-vien/issues/3)):

```properties
db.url=jdbc:postgresql://localhost:5435/qlsv
db.user=qlsv
db.password=qlsv
```

Service `maven` truyền sẵn biến môi trường `DB_URL`, `DB_USER`, `DB_PASSWORD` trỏ tới `db:5432`. Vì vậy `DBConnection` nên ưu tiên đọc các biến này khi có, nếu không có thì mới dùng `db.properties`. Có như vậy test mới chạy được cả trên máy thật lẫn trong container.

## 3. Lệnh hay dùng

```bash
docker compose up -d                  # khởi động (dữ liệu giữ nguyên giữa các lần)
docker compose down                   # dừng, giữ dữ liệu
docker compose logs -f db             # xem log database

# Xóa sạch rồi tạo lại từ schema.sql + seed.sql (dùng sau khi sửa script)
docker compose down -v && docker compose up -d

# Chạy lại script mà không xóa volume
docker compose exec -T db psql -U qlsv -d qlsv -v ON_ERROR_STOP=1 < database/schema.sql
docker compose exec -T db psql -U qlsv -d qlsv -v ON_ERROR_STOP=1 < database/seed.sql

# Mở psql để gõ truy vấn
docker compose exec db psql -U qlsv -d qlsv

# Build/test khi máy chưa cài Maven (sau khi có project Maven ở #3)
docker compose run --rm maven mvn test
docker compose run --rm maven mvn clean package
```

## 4. pgAdmin

pgAdmin tự bật cùng database khi chạy `docker compose up -d`. Muốn tắt riêng nó cho nhẹ máy thì chạy `docker compose stop pgadmin`.

1. Mở <http://localhost:5050> (cổng đặt bằng `PGADMIN_PORT`). Không cần đăng nhập pgAdmin.
2. Server **qlsv (Docker)** đã được khai báo sẵn. Lần đầu mở, nhập mật khẩu `qlsv` và tick *Save password*.
3. Vẽ ERD (#26): chuột phải database `qlsv` → **ERD For Database**, rồi xuất ảnh ra `docs/erd.png`.

Đừng sửa cấu trúc bảng bằng pgAdmin. Hãy sửa `database/schema.sql` rồi chạy lại.

## 5. Lỗi hay gặp

| Lỗi | Cách xử lý |
|---|---|
| `Ports are not available ... address already in use` | Cổng đã có chương trình khác dùng. Đổi `DB_PORT` hoặc `PGADMIN_PORT` trong `.env`, rồi sửa `db.properties` cho khớp |
| Sửa `schema.sql`/`seed.sql` nhưng database không đổi | Script chỉ tự chạy khi volume còn trống: `docker compose down -v && docker compose up -d` |
| Đổi `DB_USER`/`DB_PASSWORD` trong `.env` mà không đăng nhập được | Tài khoản chỉ được tạo khi volume còn trống. Chạy `docker compose down -v` rồi `up` lại |
| `qlsv-db` mãi không `healthy` | Xem `docker compose logs db`. Thường là script SQL bị lỗi, và log sẽ chỉ ra dòng lỗi |
| `Cannot connect to the Docker daemon` | Docker Desktop chưa chạy |
| App báo `Connection refused` | Kiểm tra `docker compose ps` và cổng trong `db.properties` có khớp `DB_PORT` không |
