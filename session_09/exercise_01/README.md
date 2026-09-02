# Session 09 - Exercise 01: Khắc Phục Sự Cố Pipeline CI/CD Cơ Bản (.gitlab-ci.yml)

## 1. Phân Tích Lý Do Lỗi Của Đoạn Mã Ban Đầu

### Đoạn mã `.gitlab-ci.yml` ban đầu bị lỗi:
```yaml
stages:
  build_app

build_job:
  stage: build_app
  script:
    - ./gradlew clean build -x test
```

---

### Các điểm lỗi & thiếu sót chính:

#### 1. Lỗi Cú pháp YAML tại từ khóa `stages` (Syntax Error):
- **Hiện tượng**: `stages` trong GitLab CI bắt buộc phải là một danh sách các stage (YAML sequence / list).
- **Nguyên nhân**: Cấu hình ban đầu thiếu dấu gạch ngang `-` trước `build_app`. Khai báo `build_app` dưới dạng giá trị đơn lẻ khiến trình phân tích YAML của GitLab báo lỗi cú pháp pipeline ngay từ khâu validation (`stages config should be an array of strings`).
- **Cách sửa**: Sửa thành danh sách với dấu gạch ngang:
  ```yaml
  stages:
    - build_app
  ```

#### 2. Lỗi thiếu khai báo môi trường `image` (Runtime Environment - Lỗi "command not found"):
- **Hiện tượng**: Khi pipeline khởi chạy lệnh `./gradlew clean build -x test`, hệ thống văng ra lỗi: `command not found: java` (hoặc `command not found`).
- **Nguyên nhân**: Môi trường GitLab Runner mặc định sử dụng Docker image chung (thường là `ruby:latest` hoặc image cơ bản không cài sẵn Java JDK). Khi chạy `./gradlew`, Gradle wrapper cần gọi trình thực thi `java` nhưng không có trong container.
- **Cách sửa**: Bổ sung từ khóa `image` chỉ định môi trường chứa JDK 17:
  ```yaml
  image: eclipse-temurin:17-jdk-alpine
  ```

#### 3. Thiếu quyền thực thi cho file `gradlew` (Permission Denied):
- **Hiện tượng**: Trên môi trường Linux container của GitLab Runner, file wrapper `./gradlew` có thể không có quyền thực thi (`+x`), dẫn đến lỗi `bash: ./gradlew: Permission denied`.
- **Cách sửa**: Bổ sung `before_script` để cấp quyền trước khi build:
  ```yaml
  before_script:
    - chmod +x ./gradlew
  ```

#### 4. Thiếu cấu hình `artifacts` và `cache` (Tối ưu hóa):
- **Thiếu sót**: Sau khi build thành công, file `.jar` không được lưu lại để tải về hoặc chuyển tiếp cho các stage sau. Thiếu cache khiến mỗi lần chạy pipeline phải tải lại Gradle distribution và toàn bộ dependencies.

---

## 2. Nội Dung File `.gitlab-ci.yml` Hoàn Chỉnh Chuẩn Xác

```yaml
# 1. Định nghĩa Docker Image chứa môi trường Java JDK 17 cho GitLab Runner
image: eclipse-temurin:17-jdk-alpine

# 2. Khai báo danh sách các Stage trong Pipeline (Cú pháp đúng của YAML sequence)
stages:
  - build_app

# 3. Cấp quyền thực thi cho file gradlew trước khi tiến hành build
before_script:
  - chmod +x ./gradlew

# 4. Job thực thi build ứng dụng Spring Boot bằng Gradle
build_job:
  stage: build_app
  script:
    - ./gradlew clean build -x test
  # Lưu trữ file JAR đầu ra để phục vụ triển khai hoặc demo
  artifacts:
    name: "user-service-$CI_COMMIT_SHORT_SHA"
    paths:
      - build/libs/*.jar
    expire_in: 1 week
  # Caching dependencies để tối ưu tốc độ build ở các lần chạy tiếp theo
  cache:
    key: "$CI_COMMIT_REF_SLUG-gradle"
    paths:
      - .gradle/wrapper/
      - .gradle/caches/
```

---

## 3. Vai Trò Của Các Keywords Trong GitLab CI

| Keyword | Vai trò & Ý nghĩa |
| :--- | :--- |
| **`image`** | Chỉ định Docker image làm môi trường thực thi chứa sẵn các công cụ (ví dụ: JDK 17, Gradle) cho các jobs. |
| **`stages`** | Định nghĩa thứ tự thực thi của các giai đoạn trong toàn bộ pipeline (ví dụ: `build_app`, `test`, `deploy`). |
| **`before_script`** | Tập hợp các câu lệnh shell được thực thi trước khi các lệnh chính trong `script` chạy (thường dùng để cài đặt môi trường, cấp quyền). |
| **`script`** | Các câu lệnh shell chính mà Runner sẽ thực hiện cho job đó (ở đây là lệnh `./gradlew clean build -x test`). |
| **`artifacts`** | Khai báo các file hoặc thư mục sinh ra trong quá trình build cần được lưu trữ lại trên GitLab Server. |
| **`cache`** | Lưu trữ tạm thời các thư mục thư viện (dependencies) để tái sử dụng ở các lần chạy sau, giúp giảm thời gian build. |
