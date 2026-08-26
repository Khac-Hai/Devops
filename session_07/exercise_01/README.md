# Bài tập 1: Sửa lỗi cấu hình GitHub Actions cơ bản (QuickBite - Cart Service)

## 1. Mục tiêu bài tập
- **Phân tích log lỗi:** Tự đọc hiểu và xác định chính xác nguyên nhân gốc rễ (*Root Cause*) khi GitHub Actions workflow bị thất bại.
- **Hiểu cơ chế Gradle Wrapper:** Nắm vững cách thức hoạt động của `gradlew` trên môi trường Linux runner và giải quyết triệt để lỗi phân quyền file (`Permission denied`).
- **Cấu hình chuẩn CI/CD Pipeline:** Nắm vững cấu trúc file workflow YAML, cách cấu hình `working-directory`, caching dependency và tối ưu hóa thời gian build.

---

## 2. Bối cảnh và Đề bài
Trong hệ thống Microservices **QuickBite**, service `cart-service` được xây dựng bằng Java Spring Boot và quản lý build bằng Gradle.
File cấu hình CI ban đầu `.github/workflows/ci.yml` được cung cấp như sau:

```yaml
name: Cart Service CI
on: [push]
jobs:
  build:
    runs-on: ubuntu-latest
    steps:
    - uses: actions/checkout@v4
    - name: Setup Java
      uses: actions/setup-java@v4
      with:
        java-version: 17
        distribution: temurin
    - name: Build with Gradle
      run: ./gradlew bootJar
```

Khi push file cấu hình này lên GitHub, pipeline liên tục thất bại ở bước **Build with Gradle**.

---

## 3. Phân tích nguyên nhân lỗi (Root Cause Analysis)

Khi quan sát log chi tiết trong tab **Actions** trên GitHub, pipeline báo lỗi do các nguyên nhân chính sau:

### 🔴 Nguyên nhân 1: Lỗi thiếu quyền thực thi file `gradlew` trên Linux (`Permission denied`)
- **Hiện tượng log:**
  ```text
  Run ./gradlew bootJar
  /home/runner/work/_temp/...sh: line 1: ./gradlew: Permission denied
  Error: Process completed with exit code 126.
  ```
- **Bản chất kỹ thuật:**
  - File `gradlew` là một Shell script (`#!/bin/sh`) chịu trách nhiệm tải và kích hoạt Gradle Wrapper.
  - Trên hệ điều hành Linux (runner `ubuntu-latest`), một file muốn được thực thi trực tiếp bằng lệnh `./gradlew` bắt buộc phải có thuộc tính thực thi (`executable bit` / `+x`).
  - Khi mã nguồn được commit từ môi trường Windows hoặc chưa được cấp quyền thực thi trong Git metadata (`filemode 100644` thay vì `100755`), file `gradlew` khi clone về máy ảo Linux sẽ không thể tự thực thi và bị hệ điều hành chặn lại với lỗi `Permission denied` (Exit code `126`).

### 🔴 Nguyên nhân 2: Lỗi đường dẫn làm việc trong cấu trúc thư mục Monorepo (`No such file or directory`)
- **Hiện tượng log:**
  ```text
  Run ./gradlew bootJar
  /home/runner/work/_temp/...sh: line 1: ./gradlew: No such file or directory
  Error: Process completed with exit code 127.
  ```
- **Bản chất kỹ thuật:**
  - GitHub Actions mặc định chạy tất cả các lệnh tại thư mục gốc (*Root*) của Repository.
  - Khi project `cart-service` được đặt trong thư mục con (ví dụ: `session_07/exercise_01/`), lệnh `./gradlew` ở thư mục gốc sẽ không tìm thấy file nếu không cấu hình `working-directory`.

---

## 4. Giải pháp khắc phục

1. **Cấp quyền thực thi cho Gradle Wrapper:**
   Thêm bước chạy lệnh `chmod +x gradlew` ngay trước bước `Build with Gradle`.
2. **Cấu hình thư mục làm việc (*Working Directory*):**
   Thiết lập `working-directory` trỏ chính xác đến thư mục chứa mã nguồn của `cart-service`.
3. **Tối ưu hóa Pipeline với Dependency Caching:**
   Kích hoạt thuộc tính `cache: 'gradle'` trong action `actions/setup-java@v4` để GitHub Actions tự động lưu cache các thư viện Gradle tải từ Maven Central, giúp rút ngắn thời gian build các lần sau.

---

## 5. File cấu hình hoàn chỉnh

### Cấu hình Workflow chạy cho Monorepo (`.github/workflows/ci.yml`)

```yaml
name: Cart Service CI

on:
  push:
    branches: [ "main", "master" ]
  pull_request:
    branches: [ "main", "master" ]
  workflow_dispatch:

jobs:
  build:
    name: Build & Package Cart Service
    runs-on: ubuntu-latest

    defaults:
      run:
        working-directory: session_07/exercise_01

    steps:
      - name: Checkout Source Code
        uses: actions/checkout@v4

      - name: Set up JDK 17
        uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'
          cache: 'gradle'

      - name: Grant execute permission for gradlew
        run: chmod +x gradlew

      - name: Build with Gradle
        run: ./gradlew bootJar --no-daemon
```

### Cấu hình Workflow cho Repository riêng lẻ (`ci.yml`)

Nếu tách `cart-service` thành một repository riêng biệt trên GitHub:

```yaml
name: Cart Service CI

on: [push]

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - name: Checkout repository
        uses: actions/checkout@v4

      - name: Setup Java
        uses: actions/setup-java@v4
        with:
          java-version: 17
          distribution: temurin
          cache: gradle

      - name: Grant execute permission for gradlew
        run: chmod +x gradlew

      - name: Build with Gradle
        run: ./gradlew bootJar
```

---

## 6. Hướng dẫn kiểm tra và nộp bài

1. **Kiểm tra build local:**
   ```bash
   cd session_07/exercise_01
   ./gradlew bootJar
   ```
   *(Trên Windows có thể dùng: `gradlew.bat bootJar`)*

2. **Commit và Push lên GitHub:**
   ```bash
   git add .
   git commit -m "feat(ci): fix github actions permissions and setup cart-service build"
   git push origin main
   ```

3. **Xác nhận kết quả trên GitHub:**
   - Truy cập vào repository trên GitHub.
   - Nhấp vào tab **Actions**.
   - Xem workflow run mới nhất mang tên `Cart Service CI`.
   - Tất cả các bước: `Checkout Source Code` -> `Set up JDK 17` -> `Grant execute permission for gradlew` -> `Build with Gradle` đều có dấu tích xanh (✅ **Success**).
4. **Nộp bài:** Copy link repository hoặc link chạy thành công của GitHub Actions và dán vào Portal nộp bài.
