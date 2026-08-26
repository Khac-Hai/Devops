# Bài tập 2: Hoàn thiện cấu hình CI cho User Service (Điền khuyết & Upload Artifact)

## 1. Mục tiêu bài tập
- **Đọc hiểu & Hoàn thiện Workflow:** Nắm vững cấu trúc tổng quan của một workflow CI tiêu chuẩn trên GitHub Actions.
- **Tra cứu và lựa chọn Actions chính thống:** Lựa chọn đúng runner, action checkout mã nguồn và cơ chế upload artifact với phiên bản mới nhất từ GitHub Marketplace.
- **Cơ chế Artifact & Output của Gradle:** Hiểu rõ cấu trúc thư mục đầu ra của Gradle (`build/libs/`) và cách lưu trữ file `.jar` sau khi build để tải về hoặc tái sử dụng cho các giai đoạn triển khai (CD) tiếp theo.

---

## 2. Bối cảnh và Đề bài
Đội ngũ DevOps của **QuickBite** chuẩn hóa quy trình CI cho service `user-service`.
Dưới đây là file cấu hình ban đầu còn khuyết các vị trí `[___]`:

```yaml
name: User Service CI
on:
  push:
    branches:
      - main
jobs:
  build_job:
    runs-on: [___]                    # Vị trí 1: Runner OS
    steps:
      - name: Checkout code
        uses: [___]                   # Vị trí 2: Action tải mã nguồn

      - name: Setup Java JDK 17
        uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'

      - name: Build Jar
        run: |
          chmod +x ./gradlew
          ./gradlew bootJar

      - name: Upload Artifact
        uses: actions/upload-artifact@v4
        with:
          name: user-service-artifact
          path: [___]                 # Vị trí 3: Đường dẫn tới file JAR
          retention-days: 3
```

---

## 3. Phân tích & Đáp án điền khuyết chi tiết

| Vị trí | Trường thiết lập | Giá trị điền | Phân tích & Giải thích kỹ thuật |
| :--- | :--- | :--- | :--- |
| **Vị trí 1** | `runs-on` | `ubuntu-latest` | **Runner hệ điều hành:** `ubuntu-latest` là môi trường máy ảo Linux (Ubuntu LTS) được GitHub Actions cung cấp miễn phí cho public repo, tốc độ khởi tạo nhanh nhất, nhẹ và tiêu tốn ít tài nguyên nhất. |
| **Vị trí 2** | `uses` (Checkout) | `actions/checkout@v4` | **Action checkout mã nguồn:** Action chính thức do GitHub duy trì (`actions/checkout`). Phiên bản `v4` là phiên bản mới nhất và ổn định nhất, hỗ trợ Node 20 runtime. |
| **Vị trí 3** | `path` (Upload Artifact) | `build/libs/*.jar` *(hoặc `session_07/exercise_02/build/libs/*.jar`)* | **Đường dẫn artifact đầu ra của Gradle:** Khi chạy lệnh `./gradlew bootJar`, Gradle biên dịch và đóng gói ứng dụng Spring Boot thành file executable JAR tại thư mục mặc định `build/libs/` (cụ thể: `user-service-0.0.1-SNAPSHOT.jar`). |

---

## 4. Cơ chế hoạt động của `actions/upload-artifact@v4`

Action `upload-artifact` cho phép lưu trữ tạm thời các file kết quả (build artifacts, test reports, logs, binaries) sau khi job chạy xong:

- `name: user-service-artifact`: Đặt tên cho gói artifact được hiển thị trên giao diện GitHub.
- `path: build/libs/*.jar`: Chỉ định chính xác các file `.jar` được nén và tải lên lưu trữ.
- `retention-days: 3`: Thiết lập thời gian tự động dọn dẹp artifact sau 3 ngày nhằm tối ưu hóa dung lượng lưu trữ trên GitHub.

---

## 5. File cấu hình hoàn chỉnh

### A. Cấu hình cho Repository độc lập (`session_07/exercise_02/ci.yml`)

```yaml
name: User Service CI

on:
  push:
    branches:
      - main

jobs:
  build_job:
    runs-on: ubuntu-latest
    steps:
      - name: Checkout code
        uses: actions/checkout@v4

      - name: Setup Java JDK 17
        uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'
          cache: 'gradle'

      - name: Build Jar
        run: |
          chmod +x ./gradlew
          ./gradlew bootJar

      - name: Upload Artifact
        uses: actions/upload-artifact@v4
        with:
          name: user-service-artifact
          path: build/libs/*.jar
          retention-days: 3
```

### B. Cấu hình chạy trên Monorepo (`.github/workflows/user-service-ci.yml`)

```yaml
name: User Service CI

on:
  push:
    branches:
      - main
  pull_request:
    branches:
      - main
  workflow_dispatch:

jobs:
  build_job:
    name: Build & Package User Service
    runs-on: ubuntu-latest

    defaults:
      run:
        working-directory: session_07/exercise_02

    steps:
      - name: Checkout code
        uses: actions/checkout@v4

      - name: Setup Java JDK 17
        uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'
          cache: 'gradle'

      - name: Build Jar
        run: |
          chmod +x ./gradlew
          ./gradlew bootJar --no-daemon

      - name: Upload Artifact
        uses: actions/upload-artifact@v4
        with:
          name: user-service-artifact
          path: session_07/exercise_02/build/libs/*.jar
          retention-days: 3
```

---

## 6. Hướng dẫn kiểm tra và nộp bài

### Bước 1: Kiểm tra build và tạo JAR trên máy local
```bash
cd session_07/exercise_02
./gradlew bootJar
```
*(Trên Windows PowerShell: `.\gradlew.bat bootJar`)*

Kiểm tra thư mục `build/libs` xem file `user-service-0.0.1-SNAPSHOT.jar` đã được sinh ra:
```bash
ls build/libs/
```

### Bước 2: Commit và Push lên GitHub
```bash
git add .
git commit -m "feat(ci): complete user-service CI workflow with artifact upload"
git push origin main
```

### Bước 3: Kiểm tra kết quả trên GitHub Actions
1. Truy cập vào GitHub repository của bạn: `https://github.com/Khac-Hai/Devops`.
2. Vào tab **Actions** -> Chọn workflow **User Service CI**.
3. Xem kết quả thực thi:
   - ✅ `Checkout code`: Thành công.
   - ✅ `Setup Java JDK 17`: Thành công.
   - ✅ `Build Jar`: Thành công biên dịch và tạo JAR.
   - ✅ `Upload Artifact`: Đã upload thành công `user-service-artifact`.
4. Cuộn xuống phần **Artifacts** ở cuối trang Summary của lần chạy:
   - Nhấp vào **`user-service-artifact`** để tải về file nén `.zip`.
   - Mở file `.zip` kiểm tra xem bên trong có đúng file `user-service-0.0.1-SNAPSHOT.jar`.

### Bước 4: Lấy link nộp bài
Copy đường link của lần chạy workflow thành công (hoặc link repository) và dán vào bài nộp trên Portal.
