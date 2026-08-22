# Bài tập 1: Đóng gói 4 dịch vụ Spring Boot (QuickBite Microservices)

## 1. Mục tiêu
- Biên soạn cấu trúc `Dockerfile` tối ưu hóa kích thước và hiệu năng cho từng microservice.
- Đóng gói đồng thời 4 dịch vụ Spring Boot độc lập thành các Docker Images chuẩn sản xuất.

---

## 2. Cấu trúc thư mục

```text
session_05/exercise_01/
├── user-service/
│   ├── build/libs/
│   │   └── user-service-0.0.1-SNAPSHOT.jar
│   └── Dockerfile
├── restaurant-service/
│   ├── build/libs/
│   │   └── restaurant-service-0.0.1-SNAPSHOT.jar
│   └── Dockerfile
├── order-service/
│   ├── build/libs/
│   │   └── order-service-0.0.1-SNAPSHOT.jar
│   └── Dockerfile
├── notification-service/
│   ├── build/libs/
│   │   └── notification-service-0.0.1-SNAPSHOT.jar
│   └── Dockerfile
├── app.sh
├── build_all.sh
└── README.md
```

---

## 3. Cấu trúc `Dockerfile` chuẩn cho các Microservice

Nội dung file `Dockerfile` tại mỗi thư mục dịch vụ (`user-service`, `restaurant-service`, `order-service`, `notification-service`):

```dockerfile
# 1. Sử dụng base image JRE 17 gọn nhẹ trên nền Alpine Linux
FROM eclipse-temurin:17-jre-alpine

# 2. Đặt thư mục làm việc mặc định trong container là /app
WORKDIR /app

# 3. Sao chép file JAR đã biên dịch vào container dưới tên app.jar
COPY build/libs/*.jar app.jar

# 4. Khai báo lệnh khởi chạy bằng Exec Form
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### Phân tích tối ưu hóa Dockerfile:
1. **Base Image `eclipse-temurin:17-jre-alpine`:**
   - Sử dụng **JRE** (Java Runtime Environment) thay vì full JDK giúp loại bỏ các công cụ biên dịch không cần thiết (`javac`, `javadoc`...), giảm dung lượng image từ ~500MB xuống còn ~150MB.
   - Nền tảng **Alpine Linux** siêu nhẹ (~5MB), giúp tăng tốc độ tải (pull/push) và triển khai container.
2. **Thư mục làm việc `WORKDIR /app`:**
   - Đảm bảo các câu lệnh tiếp theo và quá trình thực thi ứng dụng diễn ra trong một thư mục cô lập, an toàn và dễ quản lý.
3. **Sao chép `COPY build/libs/*.jar app.jar`:**
   - Đổi tên file JAR thành `app.jar` giúp chuẩn hóa điểm thực thi (entrypoint) cho mọi microservice mà không phụ thuộc vào version hoặc tên artifact cụ thể.
4. **Exec Form `ENTRYPOINT ["java", "-jar", "app.jar"]`:**
   - Dùng dạng JSON array (Exec Form) để process Java chạy trực tiếp dưới dạng **PID 1**, cho phép container tiếp nhận trực tiếp các tín hiệu Unix (`SIGTERM`, `SIGINT`) từ Docker để thực hiện **Graceful Shutdown**.

---

## 4. Hướng dẫn thực hiện và kiểm thử

### Cách 1: Tự động hóa toàn bộ bằng Script
Tại thư mục `session_05/exercise_01/`:
```bash
# Cấp quyền thực thi và chạy script build
chmod +x app.sh
./app.sh
```

### Cách 2: Thực hiện thủ công cho từng dịch vụ

#### 1. Dịch vụ `user-service`:
```bash
cd user-service
# Biên dịch file JAR (nếu có mã nguồn gradle):
./gradlew bootJar
# Đóng gói Docker Image:
docker build -t quickbite-user-service:latest .
cd ..
```

#### 2. Dịch vụ `restaurant-service`:
```bash
cd restaurant-service
./gradlew bootJar
docker build -t quickbite-restaurant-service:latest .
cd ..
```

#### 3. Dịch vụ `order-service`:
```bash
cd order-service
./gradlew bootJar
docker build -t quickbite-order-service:latest .
cd ..
```

#### 4. Dịch vụ `notification-service`:
```bash
cd notification-service
./gradlew bootJar
docker build -t quickbite-notification-service:latest .
cd ..
```

---

## 5. Kiểm tra kết quả đóng gói Images

Chạy lệnh lọc danh sách các image thuộc dự án QuickBite:
```bash
docker images | grep quickbite
```

**Kết quả mong đợi:**
```text
REPOSITORY                          TAG       IMAGE ID       CREATED          SIZE
quickbite-notification-service      latest    a1b2c3d4e5f6   10 seconds ago   185MB
quickbite-order-service             latest    b2c3d4e5f6a1   20 seconds ago   185MB
quickbite-restaurant-service        latest    c3d4e5f6a1b2   30 seconds ago   185MB
quickbite-user-service              latest    d4e5f6a1b2c3   40 seconds ago   185MB
```
