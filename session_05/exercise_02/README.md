# Bài tập 2: Thiết lập Docker Compose mạng ngoài (QuickBite Microservices)

## 1. Mục tiêu
- Biên soạn tệp `docker-compose.yml` quản lý cụm dịch vụ phân tán gồm 4 backend microservices.
- Kế thừa mạng ngoài (`external: true`) để kết nối backend tới container database đang chạy độc lập.
- Cấu hình ánh xạ cổng và truyền biến môi trường kết nối cơ sở dữ liệu.

---

## 2. Phân tích kiến trúc và cấu hình

### Cơ chế mạng ngoài (`external: true`)
- Trong kiến trúc Microservices phân tán, container Database (PostgreSQL/MySQL) có thể được triển khai độc lập hoặc chạy từ một stack khác để tránh bị xóa dữ liệu khi reset backend.
- Bằng cách cấu hình `networks.quickbite-net.external: true`, Docker Compose sẽ không tự tạo mạng mới mà tái sử dụng mạng `quickbite-net` đã có sẵn, giúp các container backend giao tiếp trực tiếp với database thông qua DNS nội bộ của Docker.

---

## 3. Nội dung file `docker-compose.yml`

```yaml
services:
  quickbite-user:
    image: quickbite-user-service:latest
    container_name: quickbite-user
    ports:
      - "8081:8081"
    environment:
      - SERVER_PORT=8081
      - SPRING_DATASOURCE_URL=jdbc:postgresql://quickbite-db:5432/user_db
      - SPRING_DATASOURCE_USERNAME=postgres
      - SPRING_DATASOURCE_PASSWORD=postgres
    networks:
      - quickbite-net

  quickbite-restaurant:
    image: quickbite-restaurant-service:latest
    container_name: quickbite-restaurant
    ports:
      - "8082:8082"
    environment:
      - SERVER_PORT=8082
      - SPRING_DATASOURCE_URL=jdbc:postgresql://quickbite-db:5432/restaurant_db
      - SPRING_DATASOURCE_USERNAME=postgres
      - SPRING_DATASOURCE_PASSWORD=postgres
    networks:
      - quickbite-net

  quickbite-order:
    image: quickbite-order-service:latest
    container_name: quickbite-order
    ports:
      - "8083:8083"
    environment:
      - SERVER_PORT=8083
      - SPRING_DATASOURCE_URL=jdbc:postgresql://quickbite-db:5432/order_db
      - SPRING_DATASOURCE_USERNAME=postgres
      - SPRING_DATASOURCE_PASSWORD=postgres
    networks:
      - quickbite-net

  quickbite-notification:
    image: quickbite-notification-service:latest
    container_name: quickbite-notification
    ports:
      - "8084:8084"
    environment:
      - SERVER_PORT=8084
      - SPRING_DATASOURCE_URL=jdbc:postgresql://quickbite-db:5432/notification_db
      - SPRING_DATASOURCE_USERNAME=postgres
      - SPRING_DATASOURCE_PASSWORD=postgres
    networks:
      - quickbite-net

networks:
  quickbite-net:
    external: true
```

---

## 4. Hướng dẫn chạy và kiểm thử

### Bước 1: Khởi tạo mạng ngoài `quickbite-net` (nếu chưa có)
```bash
docker network create quickbite-net
```

### Bước 2: Khởi chạy các dịch vụ với Docker Compose
```bash
docker compose up -d
```

### Bước 3: Kiểm tra trạng thái các container
```bash
docker compose ps
```

**Kết quả mong đợi:**
```text
NAME                     IMAGE                                  COMMAND                  SERVICE                  CREATED          STATUS          PORTS
quickbite-user           quickbite-user-service:latest          "java -jar app.jar"      quickbite-user           10 seconds ago   Up 9 seconds    0.0.0.0:8081->8081/tcp
quickbite-restaurant     quickbite-restaurant-service:latest    "java -jar app.jar"      quickbite-restaurant     10 seconds ago   Up 9 seconds    0.0.0.0:8082->8082/tcp
quickbite-order          quickbite-order-service:latest         "java -jar app.jar"      quickbite-order          10 seconds ago   Up 9 seconds    0.0.0.0:8083->8083/tcp
quickbite-notification   quickbite-notification-service:latest  "java -jar app.jar"      quickbite-notification   10 seconds ago   Up 9 seconds    0.0.0.0:8084->8084/tcp
```

### Bước 4: Dừng và dọn dẹp các dịch vụ khi hoàn tất
```bash
docker compose down
```
