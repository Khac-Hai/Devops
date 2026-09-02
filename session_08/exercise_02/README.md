# Session 08 - Exercise 02: Hoàn Thiện Dockerfile Multi-stage Build

## 1. Yêu Cầu & Bối Cảnh Bài Tập

Đội ngũ kiến trúc phần mềm tại QuickBite đang tiến hành chiến dịch tối ưu kích thước image của `cart-service` để tăng tốc độ khởi tạo container trên môi trường Production.
Nhiệm vụ: Điền các câu lệnh thích hợp vào chỗ `[___]` để hoàn thiện cấu trúc Dockerfile Multi-stage build.

---

## 2. Đoạn Mã Dockerfile Hoàn Chỉnh

```dockerfile
# Stage 1: Build mã nguồn
FROM eclipse-temurin:17-jdk-alpine AS builder
WORKDIR /app
COPY . .
RUN chmod +x gradlew && ./gradlew bootJar --no-daemon

# Stage 2: Runtime
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=builder /app/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

---

## 3. Giải Thích Chi Tiết Các Phần Điền Khuyết

### Vị trí 1: `FROM eclipse-temurin:17-jre-alpine`
- **Tại sao chọn JRE Alpine?**:
  - Giai đoạn Runtime (Stage 2) chỉ cần môi trường Java Runtime Environment (JRE) để thực thi file `.jar`, không cần bộ công cụ biên dịch (JDK), Gradle wrapper hay source code.
  - Image nền tảng Alpine Linux siêu nhẹ giúp giảm thiểu tối đa dung lượng base image (chỉ ~140MB thay vì >450MB của JDK).

### Vị trí 2: `COPY --from=builder /app/build/libs/*.jar app.jar`
- **Cú pháp `COPY --from=<stage_name>`**:
  - Cho phép sao chép có chọn lọc artifact (file thực thi `.jar`) được tạo ra từ `Stage 1` (được đặt tên là `builder` qua `AS builder`).
  - Toàn bộ source code, thư mục `build/`, `gradle/` và các file tạm thời ở Stage 1 sẽ bị loại bỏ hoàn toàn khỏi image cuối cùng.

---

## 4. Bảng So Sánh Hiệu Quả Đóng Gói (Single-stage vs Multi-stage)

| Tiêu chí so sánh | Single-stage Build (Dùng JDK đầy đủ) | Multi-stage Build (Tách JDK & JRE Alpine) |
| :--- | :--- | :--- |
| **Base Image** | `eclipse-temurin:17-jdk` | Stage 1: `jdk-alpine` -> Stage 2: `jre-alpine` |
| **Kích thước Image** | **~480MB - 600MB** | **~140MB - 160MB** (Giảm tới ~70%) |
| **Bảo mật mã nguồn** | Chứa toàn bộ source code thô, build files | **Chỉ chứa file `.jar` thành phẩm**, mã nguồn được bảo vệ an toàn |
| **Tốc độ Pull/Deploy** | Chậm hơn, tốn băng thông Registry | **Cực nhanh**, tối ưu cho CI/CD và Auto-scaling |
| **Bề mặt tấn công (Attack Surface)** | Lớn (chứa trình biên dịch, shell tools) | **Rất nhỏ**, giảm nguy cơ lỗ hổng bảo mật (CVE) |

---

## 5. Hướng Dẫn Kiểm Thử Tại Máy Cục Bộ

### Bước 1: Build Docker Image
Di chuyển vào thư mục `session_08/exercise_02`:
```bash
cd session_08/exercise_02
docker build -t quickbite/cart-service:multistage .
```

### Bước 2: Kiểm tra kích thước Image
```bash
docker images quickbite/cart-service:multistage
```
*(Kết quả kích thước image thực tế sẽ đạt dưới 160MB).*

### Bước 3: Chạy thử nghiệm Container
```bash
docker run --rm -p 8080:8080 --name cart-service-test quickbite/cart-service:multistage
```
Truy cập `http://localhost:8080` hoặc kiểm tra log khởi động Spring Boot thành công.
