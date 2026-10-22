# Cài đặt môi trường

> Issue: [#3](https://github.com/thang30102002/quan-ly-sinh-vien/issues/3) · Chi tiết về Docker: [`docker.md`](docker.md)

Mỗi thành viên làm theo các bước dưới đây, đến khi `mvn test` pass trên máy mình.

## 1. Cài phần mềm

| Phần mềm | Bắt buộc | Windows | macOS |
|---|---|---|---|
| JDK 17 | Có | `winget install EclipseAdoptium.Temurin.17.JDK` | `brew install --cask temurin@17` |
| Docker Desktop | Có | [Tải tại đây](https://www.docker.com/products/docker-desktop/) | [Tải tại đây](https://www.docker.com/products/docker-desktop/) |
| Maven | Không (có thể chạy bằng Docker) | `winget install Apache.Maven` hoặc dùng Maven có sẵn trong NetBeans | `brew install maven` |
| Git | Có | `winget install Git.Git` | có sẵn |

Cài xong thì **mở terminal mới** để nhận PATH mới, rồi kiểm tra lại:

```bash
java -version      # phải là 17.x
docker --version
mvn -version       # nếu đã cài Maven
```

## 2. Bật database

Mở Docker Desktop trước, sau đó ở thư mục gốc của repo:

```bash
cp .env.example .env          # Windows PowerShell: copy .env.example .env
docker compose up -d
docker compose ps             # qlsv-db phải "healthy"
```

Lần đầu khởi động, Docker tạo database `qlsv` và nạp sẵn `database/schema.sql`, `database/seed.sql`.

## 3. Cấu hình kết nối

`src/main/resources/db.properties` đã trỏ sẵn tới database Docker:

```properties
db.url=jdbc:postgresql://localhost:5435/qlsv
db.user=qlsv
db.password=qlsv
```

`DBConnection` ưu tiên dùng biến môi trường `DB_URL`, `DB_USER`, `DB_PASSWORD` nếu có. Nếu không có thì mới đọc file này. Bình thường không cần đặt các biến đó.

## 4. Chạy test

```bash
mvn test                                  # máy đã cài Maven
docker compose run --rm maven mvn test    # máy chưa cài Maven
```

Kết quả đúng phải có dòng `Tests run: 1, Failures: 0, Errors: 0` và `BUILD SUCCESS` (test `DBConnectionTest`).

## 5. Lỗi hay gặp

| Lỗi | Cách xử lý |
|---|---|
| `Ports are not available ... address already in use` khi `docker compose up` | Cổng 5435 đã có chương trình khác dùng. Đổi `DB_PORT` trong `.env` (vd `5436`), chạy lại `docker compose up -d`, rồi sửa cổng trong `db.url` của `db.properties` cho khớp |
| `Connection refused` khi chạy test | Database chưa chạy, hoặc cổng trong `db.properties` không khớp `DB_PORT`. Kiểm tra bằng `docker compose ps` |
| `password authentication failed` | Nhầm sang PostgreSQL cài sẵn trên máy ở cổng 5432. Database Docker dùng cổng **5435** |
| `Tests run: 0` | `pom.xml` thiếu `maven-surefire-plugin` 3.x |
| `java`/`mvn`/`docker` không phải là lệnh hợp lệ | Terminal được mở trước khi cài, nên chưa nhận PATH mới. Mở terminal mới (hoặc khởi động lại VS Code / NetBeans) |
| `Cannot connect to the Docker daemon` | Docker Desktop chưa được mở |
| `release version 17 not supported` | Maven đang chạy bằng JDK cũ. Kiểm tra `mvn -version` và đặt `JAVA_HOME` trỏ tới JDK 17 |
