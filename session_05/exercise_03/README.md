# Bài tập 3: Cấu hình biến môi trường qua tệp .env (QuickBite Microservices)

## 1. Mục tiêu
- Tổ chức cấu hình khoa học bằng cách phân tách biến môi trường ra tệp `.env` dùng chung.
- Sử dụng tiền tố (prefix) riêng biệt cho từng dịch vụ (`USER_`, `RESTAURANT_`, `ORDER_`, `NOTIFICATION_`) để tránh xung đột key.
- Bảo mật thông tin nhạy cảm (mật khẩu database) bằng cách đưa `.env` vào `.gitignore` và cung cấp `.env.example` làm template.

---

## 2. Bảng tham chiếu biến môi trường

| Nhóm Dịch Vụ | Tên Biến | Giá Trị Mặc Định | Ý Nghĩa |
| :--- | :--- | :--- | :--- |
| **Dùng chung** | `DB_HOST` | `quickbite-db` | Hostname của database trong mạng Docker |
| | `DB_PORT` | `5432` | Cổng kết nối database PostgreSQL |
| **User** | `USER_DB_NAME` | `user_db` | Tên CSDL cho User Service |
| | `USER_DB_USERNAME` | `user_admin` | Tài khoản kết nối DB |
| | `USER_DB_PASSWORD` | `user_secret_password` | Mật khẩu kết nối DB |
| | `USER_SERVER_PORT` | `8081` | Cổng chạy User Service |
| **Restaurant** | `RESTAURANT_DB_NAME` | `restaurant_db` | Tên CSDL cho Restaurant Service |
| | `RESTAURANT_DB_USERNAME` | `restaurant_admin` | Tài khoản kết nối DB |
| | `RESTAURANT_DB_PASSWORD` | `restaurant_secret_password` | Mật khẩu kết nối DB |
| | `RESTAURANT_SERVER_PORT` | `8082` | Cổng chạy Restaurant Service |
| **Order** | `ORDER_DB_NAME` | `order_db` | Tên CSDL cho Order Service |
| | `ORDER_DB_USERNAME` | `order_admin` | Tài khoản kết nối DB |
| | `ORDER_DB_PASSWORD` | `order_secret_password` | Mật khẩu kết nối DB |
| | `ORDER_SERVER_PORT` | `8083` | Cổng chạy Order Service |
| **Notification**| `NOTIFICATION_DB_NAME` | `notification_db` | Tên CSDL cho Notification Service |
| | `NOTIFICATION_DB_USERNAME` | `notification_admin` | Tài khoản kết nối DB |
| | `NOTIFICATION_DB_PASSWORD` | `notification_secret_password`| Mật khẩu kết nối DB |
| | `NOTIFICATION_SERVER_PORT` | `8084` | Cổng chạy Notification Service |

---

## 3. Nội dung cấu hình các tệp tin

### 📄 `.env`
```ini
DB_HOST=quickbite-db
DB_PORT=5432

USER_DB_NAME=user_db
USER_DB_USERNAME=user_admin
USER_DB_PASSWORD=user_secret_password
USER_SERVER_PORT=8081

RESTAURANT_DB_NAME=restaurant_db
RESTAURANT_DB_USERNAME=restaurant_admin
RESTAURANT_DB_PASSWORD=restaurant_secret_password
RESTAURANT_SERVER_PORT=8082

ORDER_DB_NAME=order_db
ORDER_DB_USERNAME=order_admin
ORDER_DB_PASSWORD=order_secret_password
ORDER_SERVER_PORT=8083

NOTIFICATION_DB_NAME=notification_db
NOTIFICATION_DB_USERNAME=notification_admin
NOTIFICATION_DB_PASSWORD=notification_secret_password
NOTIFICATION_SERVER_PORT=8084
```

### 📄 `docker-compose.yml` (Sử dụng biến nội suy `${...}`)
```yaml
services:
  quickbite-user:
    image: quickbite-user-service:latest
    container_name: quickbite-user
    ports:
      - "${USER_SERVER_PORT}:${USER_SERVER_PORT}"
    environment:
      - SERVER_PORT=${USER_SERVER_PORT}
      - SPRING_DATASOURCE_URL=jdbc:postgresql://${DB_HOST}:${DB_PORT}/${USER_DB_NAME}
      - SPRING_DATASOURCE_USERNAME=${USER_DB_USERNAME}
      - SPRING_DATASOURCE_PASSWORD=${USER_DB_PASSWORD}
    networks:
      - quickbite-net

  quickbite-restaurant:
    image: quickbite-restaurant-service:latest
    container_name: quickbite-restaurant
    ports:
      - "${RESTAURANT_SERVER_PORT}:${RESTAURANT_SERVER_PORT}"
    environment:
      - SERVER_PORT=${RESTAURANT_SERVER_PORT}
      - SPRING_DATASOURCE_URL=jdbc:postgresql://${DB_HOST}:${DB_PORT}/${RESTAURANT_DB_NAME}
      - SPRING_DATASOURCE_USERNAME=${RESTAURANT_DB_USERNAME}
      - SPRING_DATASOURCE_PASSWORD=${RESTAURANT_DB_PASSWORD}
    networks:
      - quickbite-net

  quickbite-order:
    image: quickbite-order-service:latest
    container_name: quickbite-order
    ports:
      - "${ORDER_SERVER_PORT}:${ORDER_SERVER_PORT}"
    environment:
      - SERVER_PORT=${ORDER_SERVER_PORT}
      - SPRING_DATASOURCE_URL=jdbc:postgresql://${DB_HOST}:${DB_PORT}/${ORDER_DB_NAME}
      - SPRING_DATASOURCE_USERNAME=${ORDER_DB_USERNAME}
      - SPRING_DATASOURCE_PASSWORD=${ORDER_DB_PASSWORD}
    networks:
      - quickbite-net

  quickbite-notification:
    image: quickbite-notification-service:latest
    container_name: quickbite-notification
    ports:
      - "${NOTIFICATION_SERVER_PORT}:${NOTIFICATION_SERVER_PORT}"
    environment:
      - SERVER_PORT=${NOTIFICATION_SERVER_PORT}
      - SPRING_DATASOURCE_URL=jdbc:postgresql://${DB_HOST}:${DB_PORT}/${NOTIFICATION_DB_NAME}
      - SPRING_DATASOURCE_USERNAME=${NOTIFICATION_DB_USERNAME}
      - SPRING_DATASOURCE_PASSWORD=${NOTIFICATION_DB_PASSWORD}
    networks:
      - quickbite-net

networks:
  quickbite-net:
    external: true
```

---

## 4. Hướng dẫn kiểm thử và nộp bài

### Bước 1: Di chuyển vào thư mục `exercise_03`
```bash
cd session_05/exercise_03
```

### Bước 2: Kiểm tra cấu hình nội suy (Interpolation Validation)
```bash
docker compose config
```

> 📸 **Nộp bài:** Chụp lại màn hình terminal kết quả của lệnh `docker compose config` (in ra đầy đủ các biến đã được phân giải sang giá trị cụ thể) và lưu tại: `session_05/exercise_03/env_config.png`.
