# Bài tập 4: Xác minh Hibernate DDL-Auto (QuickBite Microservices)

## 1. Mục tiêu
- Hiểu rõ cơ chế tự động tạo và cập nhật schema cơ sở dữ liệu của **Hibernate DDL-Auto** (`spring.jpa.hibernate.ddl-auto`).
- Xác minh tính nhất quán dữ liệu khi các dịch vụ backend Spring Boot kết nối và tự động sinh bảng trong container PostgreSQL độc lập.

---

## 2. Tìm hiểu cơ chế Hibernate `ddl-auto`

Thuộc tính `spring.jpa.hibernate.ddl-auto` quyết định cách Hibernate quản lý schema DB khi ứng dụng khởi động:

| Giá trị | Hành vi | Môi trường khuyến nghị |
| :--- | :--- | :--- |
| `none` | Không thực hiện bất kỳ thay đổi nào với DB. | **Production** (kết hợp Liquibase/Flyway) |
| `validate` | Chỉ kiểm tra schema DB có khớp với Entity không, nếu sai sẽ báo lỗi và dừng app. | **Staging / Production** |
| `update` | Tự động tạo bảng mới hoặc thêm cột mới nếu chưa tồn tại (không xóa bảng/cột cũ). | **Development** |
| `create` | Xóa sạch toàn bộ schema và tạo lại từ đầu mỗi khi ứng dụng khởi động. | **Testing / Demo** |
| `create-drop` | Tạo mới khi khởi động và tự động xóa sạch khi ứng dụng tắt (`SessionFactory.close()`). | **Unit Tests** |

---

## 3. Cấu hình triển khai

Nội dung file `docker-compose.yml` tích hợp container Database `quickbite-db` và kích hoạt `SPRING_JPA_HIBERNATE_DDL_AUTO=update` cho cả 4 services:

```yaml
services:
  quickbite-db:
    image: postgres:15-alpine
    container_name: quickbite-db
    ports:
      - "5432:5432"
    environment:
      - POSTGRES_DB=quickbite_user_db
      - POSTGRES_USER=postgres
      - POSTGRES_PASSWORD=postgres
    volumes:
      - ./init-db.sql:/docker-entrypoint-initdb.d/init-db.sql
    networks:
      - quickbite-net

  quickbite-user:
    build: ../exercise_01/user-service
    image: quickbite-user-service:latest
    container_name: quickbite-user
    ports:
      - "8081:8081"
    environment:
      - SERVER_PORT=8081
      - SPRING_DATASOURCE_URL=jdbc:postgresql://quickbite-db:5432/quickbite_user_db
      - SPRING_DATASOURCE_USERNAME=postgres
      - SPRING_DATASOURCE_PASSWORD=postgres
      - SPRING_JPA_HIBERNATE_DDL_AUTO=update
    networks:
      - quickbite-net
    depends_on:
      - quickbite-db

  quickbite-restaurant:
    build: ../exercise_01/restaurant-service
    image: quickbite-restaurant-service:latest
    container_name: quickbite-restaurant
    ports:
      - "8082:8082"
    environment:
      - SERVER_PORT=8082
      - SPRING_DATASOURCE_URL=jdbc:postgresql://quickbite-db:5432/restaurant_db
      - SPRING_DATASOURCE_USERNAME=postgres
      - SPRING_DATASOURCE_PASSWORD=postgres
      - SPRING_JPA_HIBERNATE_DDL_AUTO=update
    networks:
      - quickbite-net
    depends_on:
      - quickbite-db

  quickbite-order:
    build: ../exercise_01/order-service
    image: quickbite-order-service:latest
    container_name: quickbite-order
    ports:
      - "8083:8083"
    environment:
      - SERVER_PORT=8083
      - SPRING_DATASOURCE_URL=jdbc:postgresql://quickbite-db:5432/order_db
      - SPRING_DATASOURCE_USERNAME=postgres
      - SPRING_DATASOURCE_PASSWORD=postgres
      - SPRING_JPA_HIBERNATE_DDL_AUTO=update
    networks:
      - quickbite-net
    depends_on:
      - quickbite-db

  quickbite-notification:
    build: ../exercise_01/notification-service
    image: quickbite-notification-service:latest
    container_name: quickbite-notification
    ports:
      - "8084:8084"
    environment:
      - SERVER_PORT=8084
      - SPRING_DATASOURCE_URL=jdbc:postgresql://quickbite-db:5432/notification_db
      - SPRING_DATASOURCE_USERNAME=postgres
      - SPRING_DATASOURCE_PASSWORD=postgres
      - SPRING_JPA_HIBERNATE_DDL_AUTO=update
    networks:
      - quickbite-net
    depends_on:
      - quickbite-db

networks:
  quickbite-net:
    external: true
```

---

## 4. Hướng dẫn thực hiện và kiểm thử

### Bước 1: Di chuyển vào thư mục `exercise_04`
```bash
cd session_05/exercise_04
```

### Bước 2: Khởi chạy cụm container với tùy chọn `--build`
```bash
docker compose up -d --build
```

### Bước 3: Truy cập vào Postgres container để kiểm tra danh sách bảng
```bash
docker exec -it quickbite-db psql -U postgres -d quickbite_user_db -c "\dt"
```

**Kết quả mong đợi:**
```text
               List of relations
 Schema |      Name      | Type  |  Owner   
--------+----------------+-------+----------
 public | user_addresses | table | postgres
 public | user_wallets   | table | postgres
 public | users          | table | postgres
(3 rows)
```

> 📸 **Nộp bài:** Chụp lại màn hình terminal bảng danh sách 3 quan hệ trên và lưu tại: `session_05/exercise_04/table_generation.png`.
