# Bài tập 2: Quản lý dữ liệu (Volumes) và Dependency giữa các container

## 1. Giải thích các cơ chế kỹ thuật đã sử dụng

### 1.1. Cơ chế `healthcheck` và `depends_on` với `condition: service_healthy`
- **Vấn đề của cấu hình cũ:** Cấu hình `depends_on` mặc định dạng danh sách (`- postgres`, `- redis`) chỉ đợi container được tạo và tiến trình ban đầu khởi chạy (`container started`). Lúc này, cơ sở dữ liệu PostgreSQL và cache Redis còn đang trong quá trình nạp cấu hình, mở socket và chưa sẵn sàng tiếp nhận kết nối. Kết quả là `backend` khởi động ngay lập tức sẽ bị lỗi kết nối và crash.
- **Giải pháp `healthcheck`:**
  - **PostgreSQL**: Sử dụng lệnh kiểm tra nội bộ `pg_isready -U admin -d storex_db`. Khi PostgreSQL thực sự sẵn sàng nhận kết nối, lệnh này trả về mã 0 và Docker đánh dấu container là `healthy`.
  - **Redis**: Sử dụng lệnh `redis-cli ping`. Khi Redis sẵn sàng, nó trả về `PONG` (mã 0) và được đánh dấu là `healthy`.
- **Cơ chế `condition: service_healthy`:**
  - Cấu hình ở service `backend` yêu cầu Docker Compose phải chờ cho đến khi cả `postgres` và `redis` chuyển sang trạng thái `healthy` thì mới tiến hành khởi động `backend`. Nhờ đó loại bỏ hoàn toàn tình trạng race-condition (khởi động trước khi DB/Cache sẵn sàng).

### 1.2. Cơ chế lưu trữ bền vững với Docker Named Volumes
- **Vấn đề của cấu hình cũ:** Dữ liệu trong container nằm ở lớp Read/Write tạm thời (Writable Layer). Khi thực hiện `docker compose down`, các container bị hủy và toàn bộ dữ liệu trong thư mục lưu trữ của PostgreSQL `/var/lib/postgresql/data` bị xóa vĩnh viễn.
- **Giải pháp Docker Volumes:**
  - Khai báo Named Volume `postgres_data` và mount vào thư mục lưu trữ dữ liệu `/var/lib/postgresql/data` của PostgreSQL.
  - Vòng đời của Volume tách biệt hoàn toàn với container: dữ liệu được lưu an toàn trên ổ đĩa của Host và tự động mount lại khi container mới được tạo (`docker compose up`).

---

## 2. File cấu hình hoàn chỉnh: `docker-compose.yml`

```yaml
services:
  backend:
    image: storex-backend:v2
    ports:
      - "8080:8080"
    environment:
      - SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/storex_db
      - SPRING_DATASOURCE_USERNAME=admin
      - SPRING_DATASOURCE_PASSWORD=admin
      - SPRING_REDIS_HOST=redis
      - SPRING_REDIS_PORT=6379
    depends_on:
      postgres:
        condition: service_healthy
      redis:
        condition: service_healthy

  postgres:
    image: postgres:13
    ports:
      - "5432:5432"
    environment:
      - POSTGRES_DB=storex_db
      - POSTGRES_USER=admin
      - POSTGRES_PASSWORD=admin
    volumes:
      - postgres_data:/var/lib/postgresql/data
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U admin -d storex_db"]
      interval: 5s
      timeout: 5s
      retries: 5
      start_period: 10s

  redis:
    image: redis:alpine
    ports:
      - "6379:6379"
    healthcheck:
      test: ["CMD", "redis-cli", "ping"]
      interval: 5s
      timeout: 3s
      retries: 5
      start_period: 5s

volumes:
  postgres_data:
```

---

## 3. Các bước kiểm tra thực tế

### Bước 1: Khởi chạy môi trường
```bash
docker compose up -d
```
*Quan sát thứ tự:* `postgres` & `redis` khởi động trước -> chuyển trạng thái `healthy` -> `backend` mới bắt đầu khởi động.

### Bước 2: Kiểm tra trạng thái các container
```bash
docker compose ps
```
Cột `STATUS` sẽ hiển thị rõ ràng: `Up (healthy)`.

### Bước 3: Thêm dữ liệu mẫu vào PostgreSQL
```bash
docker compose exec postgres psql -U admin -d storex_db -c "CREATE TABLE products (id SERIAL PRIMARY KEY, name VARCHAR(100)); INSERT INTO products (name) VALUES ('Laptop Dell XPS');"
```

### Bước 4: Kiểm tra tính toàn vẹn dữ liệu qua Volume (Persistence)
Dừng và xóa toàn bộ container:
```bash
docker compose down
```
Khởi động lại cụm container:
```bash
docker compose up -d
```
Truy vấn lại bảng dữ liệu:
```bash
docker compose exec postgres psql -U admin -d storex_db -c "SELECT * FROM products;"
```
*Kết quả:* Dữ liệu `Laptop Dell XPS` vẫn tồn tại nguyên vẹn.

### Bước 5: Xem log kết nối thành công của backend
```bash
docker compose logs backend
```
