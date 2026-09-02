# Session 09 - Exercise 02: Container Hóa Ứng Dụng Spring Boot Microservices (Payment Service)

## 1. Yêu Cầu Bài Tập

Ứng dụng `Payment Service` sau khi chạy lệnh `./gradlew build` sẽ sinh ra file thực thi `payment-service-1.0.0.jar` tại thư mục `build/libs/`.
Nhiệm vụ: Điền các chỉ thị (keywords) chuẩn xác của Docker vào các vị trí còn trống trong template Dockerfile.

---

## 2. Dockerfile Hoàn Chỉnh Sau Khi Điền Khuyết

```dockerfile
# 1. Chọn base image chứa JRE 17 gọn nhẹ
FROM eclipse-temurin:17-jre-alpine

# 2. Tạo thư mục làm việc trong container
WORKDIR /app

# 3. Copy file jar từ thư mục build của Gradle vào container
COPY build/libs/payment-service-1.0.0.jar app.jar

# 4. Mở port 8080 để giao tiếp với các microservices khác
EXPOSE 8080

# 5. Lệnh khởi chạy ứng dụng Spring Boot
ENTRYPOINT ["java", "-jar", "app.jar"]
```

---

## 3. Ý Nghĩa & Vai Trò Của Từng Chỉ Thị (Keywords)

| Chỉ thị (Keyword) | Giá trị sử dụng | Ý nghĩa & Vai trò |
| :--- | :--- | :--- |
| **`FROM`** | `eclipse-temurin:17-jre-alpine` | Khai báo Base Image nền tảng chứa môi trường JRE 17 trên Linux Alpine siêu nhẹ, tối ưu dung lượng container. |
| **`WORKDIR`** | `/app` | Thiết lập thư mục làm việc mặc định trong container. Các lệnh `COPY`, `RUN`, `ENTRYPOINT` tiếp theo sẽ chạy tại đây. |
| **`COPY`** | `build/libs/payment-service-1.0.0.jar app.jar` | Sao chép file `.jar` đã build từ máy host vào thư mục làm việc `/app` trong container và đổi tên thành `app.jar`. |
| **`EXPOSE`** | `8080` | Khai báo cổng mạng (port) mà ứng dụng Spring Boot sẽ lắng nghe khi container chạy, giúp giao tiếp với các microservices khác. |
| **`ENTRYPOINT`** | `["java", "-jar", "app.jar"]` | Định nghĩa câu lệnh thực thi chính (ở dạng Exec form) sẽ luôn được chạy khi container khởi động. |

---

## 4. Hướng Dẫn Build & Chạy Thử Container

### Bước 1: Build file JAR bằng Gradle
Tại thư mục `session_09/exercise_02`:
```bash
./gradlew bootJar
```
*(File `payment-service-1.0.0.jar` sẽ được tạo tại `build/libs/`).*

### Bước 2: Build Docker Image
```bash
docker build -t quickbite/payment-service:1.0.0 .
```

### Bước 3: Chạy và kiểm tra Container
```bash
docker run --rm -d -p 8080:8080 --name payment-service quickbite/payment-service:1.0.0
```
Kiểm tra log:
```bash
docker logs payment-service
```
*(Hiển thị log Spring Boot khởi động thành công trên cổng 8080).*
