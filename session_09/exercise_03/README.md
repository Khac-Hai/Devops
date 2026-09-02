# Session 09 - Exercise 03: Xây Dựng Pipeline CI/CD Kết Nối PostgreSQL (GitLab CI)

## 1. Yêu Cầu & Bối Cảnh Bài Tập

Dự án "Quản lý nhân sự" (HR Service) sử dụng kiến trúc Microservices và cơ sở dữ liệu PostgreSQL.
Nhiệm vụ: Thiết lập một CI Pipeline tự động gồm 2 stage tuần tự:
1. **Stage `test`**: Dựng tạm một Database PostgreSQL thông qua từ khóa `services` trong GitLab CI, thực thi Integration Test kiểm tra logic truy vấn database bằng `./gradlew test`.
2. **Stage `build`**: Nếu stage `test` vượt qua thành công, tiến hành đóng gói ra file thực thi `.jar` bằng `./gradlew build -x test` và lưu trữ qua `artifacts` với thời gian hết hạn là 1 ngày.

---

## 2. Cấu Hình File [`.gitlab-ci.yml`](file:///c:/Users/ADMIN/Devops/session_09/exercise_03/.gitlab-ci.yml) Hoàn Chỉnh

```yaml
# 1. Định nghĩa thứ tự các stages tuần tự trong Pipeline
stages:
  - test
  - build

# 2. Cấu hình biến môi trường toàn cục cho PostgreSQL và Spring Boot
variables:
  POSTGRES_DB: hr_db
  POSTGRES_USER: postgres
  POSTGRES_PASSWORD: password123
  POSTGRES_HOST_AUTH_METHOD: trust
  # Cấu hình Spring Datasource trỏ tới hostname 'postgres' (alias của service)
  SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/hr_db
  SPRING_DATASOURCE_USERNAME: postgres
  SPRING_DATASOURCE_PASSWORD: password123

# 3. Cấp quyền thực thi cho Gradle wrapper
before_script:
  - chmod +x ./gradlew

# =========================================================================
# STAGE 1: TEST - CHẠY INTEGRATION TEST KẾT NỐI POSTGRESQL SERVICE
# =========================================================================
test_job:
  stage: test
  image: gradle:8.5-jdk17-alpine
  services:
    # Khởi chạy PostgreSQL container chạy song song với container test
    - name: postgres:14-alpine
      alias: postgres
  variables:
    POSTGRES_DB: hr_db
    POSTGRES_USER: postgres
    POSTGRES_PASSWORD: password123
  script:
    - ./gradlew test
  cache:
    key: "$CI_COMMIT_REF_SLUG-gradle"
    paths:
      - .gradle/wrapper/
      - .gradle/caches/

# =========================================================================
# STAGE 2: BUILD - ĐÓNG GÓI ỨNG DỤNG VÀ LƯU ARTIFACTS
# =========================================================================
build_job:
  stage: build
  image: eclipse-temurin:17-jdk-alpine
  script:
    - ./gradlew build -x test
  # Khai báo block artifacts để lưu trữ file JAR thành phẩm
  artifacts:
    name: "hr-service-$CI_COMMIT_SHORT_SHA"
    paths:
      - build/libs/*.jar
    expire_in: 1 day
  cache:
    key: "$CI_COMMIT_REF_SLUG-gradle"
    paths:
      - .gradle/wrapper/
      - .gradle/caches/
```

---

## 3. Phân Tích Kỹ Thuật & Các Thành Phần Quan Trọng

### 1. Cơ chế `services` trong GitLab CI
- Từ khóa `services` cho phép khởi chạy các container phụ trợ (như PostgreSQL, Redis, MySQL) chạy song song trong cùng một Docker Network với job container chính.
- Container chính có thể kết nối trực tiếp tới container phụ trợ thông qua **hostname** chính là tên image hoặc `alias` được định nghĩa (ở đây hostname là `postgres` trên cổng `5432`).

### 2. Cấu hình biến môi trường (`variables`)
- `POSTGRES_DB`, `POSTGRES_USER`, `POSTGRES_PASSWORD`: Các biến môi trường tiêu chuẩn của PostgreSQL image để tự động khởi tạo database khi container khởi động.
- `SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/hr_db`: Thiết lập URL kết nối cho Spring Boot trỏ tới service `postgres`.

### 3. Cấu hình lưu trữ sản phẩm build (`artifacts`)
- `paths: - build/libs/*.jar`: Chỉ định chính xác đường dẫn file JAR được tạo ra sau lệnh `./gradlew build`.
- `expire_in: 1 day`: Thiết lập thời gian lưu trữ file trên GitLab Server là 1 ngày theo đúng yêu cầu đề bài.

---

## 4. Hướng Dẫn Kiểm Thử Tại Máy Cục Bộ

Tại thư mục `session_09/exercise_03`:
```bash
# 1. Chạy Unit/Integration Tests
./gradlew test

# 2. Đóng gói ứng dụng thành file JAR
./gradlew build -x test
```
File `build/libs/hr-service-1.0.0.jar` được tạo thành công và sẵn sàng triển khai.
