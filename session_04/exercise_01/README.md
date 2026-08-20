# Bài tập 1: Khởi tạo môi trường phát triển cơ bản với Docker Compose

## 1. Nguyên nhân gây ra lỗi kết nối (`Connection refused`)
- **Cơ chế cô lập mạng (Network Isolation) của Docker:** Mỗi container trong Docker hoạt động như một máy chủ độc lập với network namespace và interface loopback (`localhost` / `127.0.0.1`) riêng biệt.
- **Lỗi khi gọi `localhost`:** Trong cấu hình ban đầu, `SPRING_DATASOURCE_URL` được cấu hình là `jdbc:postgresql://localhost:5432/storex`. Khi ứng dụng bên trong container `backend` kết nối tới `localhost`, nó đang tìm PostgreSQL ngay trong chính container `backend` (chứ không phải container `postgres` hay máy host). Do bên trong container `backend` không có database PostgreSQL nào chạy ở cổng `5432`, kết nối bị từ chối với lỗi **`Connection refused`**.
- **Cơ chế Docker Embedded DNS:** Khi khởi chạy bằng Docker Compose, một mạng chung (default bridge network) được tạo tự động cho tất cả các service. Docker Compose tích hợp DNS Server nội bộ, cho phép các container phân giải tự động **Service Name** (ví dụ: `postgres`) thành địa chỉ IP nội bộ tương ứng.

---

## 2. Giải pháp khắc phục
Thay đổi hostname trong biến môi trường `SPRING_DATASOURCE_URL` từ `localhost` thành tên service database là `postgres`:

- **Trước khi sửa:**
  ```yaml
  - SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/storex
  ```
- **Sau khi sửa:**
  ```yaml
  - SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/storex
  ```
- Thêm `depends_on: - postgres` để container `backend` chờ container `postgres` khởi động trước.

---

## 3. Nội dung file `docker-compose.yml` hoàn chỉnh

```yaml
services:
  backend:
    image: storex-backend:latest
    ports:
      - "8080:8080"
    environment:
      - SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/storex
      - SPRING_DATASOURCE_USERNAME=postgres
      - SPRING_DATASOURCE_PASSWORD=secret
    depends_on:
      - postgres

  postgres:
    image: postgres:13
    ports:
      - "5432:5432"
    environment:
      - POSTGRES_DB=storex
      - POSTGRES_USER=postgres
      - POSTGRES_PASSWORD=secret
```

---

## 4. Hướng dẫn chạy và kiểm tra kết quả

### Bước 1: Build image giả lập backend (nếu chưa có sẵn image trên máy)
```bash
docker build -t storex-backend:latest .
```

### Bước 2: Khởi chạy các container với Docker Compose
```bash
docker compose up -d
```

### Bước 3: Kiểm tra trạng thái các container
```bash
docker compose ps
```

### Bước 4: Kiểm tra log kết nối thành công của backend
```bash
docker compose logs -f backend
```

**Kết quả log mong đợi:**
```text
[StoreX Backend] Starting Spring Boot application...
[StoreX Backend] Connecting to database at jdbc:postgresql://postgres:5432/storex...
[StoreX Backend] Waiting for database at postgres:5432...
[StoreX Backend] Successfully connected to PostgreSQL (postgres:5432)!
[StoreX Backend] HikariPool-1 - Added connection org.postgresql.jdbc.PgConnection
[StoreX Backend] HikariPool-1 - Start completed.
[StoreX Backend] Tomcat started on port 8080 (http) with context path ''
[StoreX Backend] Started StoreXApplication
```

### Bước 5: Dừng dịch vụ sau khi hoàn tất
```bash
docker compose down
```
