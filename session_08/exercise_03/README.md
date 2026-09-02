# Session 08 - Exercise 03: Thiết Lập Quy Trình Build và Push Image lên GHCR

## 1. Mục Tiêu & Bối Cảnh Tình Huống

Hệ thống yêu cầu đóng gói và đẩy image của `payment-service` lên GitHub Container Registry (`ghcr.io`) thuộc tài khoản cá nhân.
Khi gõ lệnh `docker push`, hệ thống gặp lỗi:
```text
denied: requested access to the resource is denied
```

### Nguyên nhân lỗi:
1. **Chưa đăng nhập hoặc dùng mật khẩu thông thường**: Docker CLI cần xác thực với `ghcr.io` bằng **Personal Access Token (PAT)** có quyền `write:packages`, không thể dùng mật khẩu đăng nhập GitHub.
2. **Sai quy cách đặt Tag**: Image tag phải tuân theo chuẩn namespace:
   `ghcr.io/<github-username-lowercase>/<image-name>:<tag>`
   (Ví dụ: `ghcr.io/khac-hai/payment-service:1.0.0`).

---

## 2. Hướng Dẫn Thực Hiện Chi Tiết

### Bước 1: Tạo GitHub Personal Access Token (PAT)
1. Đăng nhập GitHub -> **Settings** (ở avatar góc phải trên).
2. Cuộn xuống chọn **Developer settings** -> **Personal access tokens** -> **Tokens (classic)**.
3. Nhấn **Generate new token (classic)**.
4. Đặt tên (Note): `ghcr-docker-token`.
5. Tích chọn các quyền (Scopes):
   - `write:packages` (Tải image lên GitHub Packages)
   - `read:packages` (Kéo image từ GitHub Packages)
   - `delete:packages` (Xóa image khi cần)
   - `repo` (Cần thiết nếu repository là Private)
6. Nhấn **Generate token** và sao chép mã token (bắt đầu bằng `ghp_...`).

---

### Bước 2: Đăng Nhập Docker CLI vào GHCR

Mở terminal (PowerShell trên Windows) và chạy:
```powershell
$env:CR_PAT = "DÁN_TOKEN_PAT_VÀO_ĐÂY"
$env:CR_PAT | docker login ghcr.io -u Khac-Hai --password-stdin
```
Khi hiển thị `Login Succeeded` tức là đã đăng nhập thành công.

---

### Bước 3: Build & Gắn Tag Image

Di chuyển vào thư mục bài tập:
```powershell
cd session_08/exercise_03
```

Build image với tag `1.0.0`:
```powershell
docker build -t ghcr.io/khac-hai/payment-service:1.0.0 .
```

Gắn thêm tag `latest`:
```powershell
docker tag ghcr.io/khac-hai/payment-service:1.0.0 ghcr.io/khac-hai/payment-service:latest
```

---

### Bước 4: Push Image lên GHCR

```powershell
docker push ghcr.io/khac-hai/payment-service:1.0.0
docker push ghcr.io/khac-hai/payment-service:latest
```

---

### Bước 5: Kiểm Tra Kết Quả
1. Vào profile GitHub: `https://github.com/Khac-Hai?tab=packages`
2. Bạn sẽ thấy package `payment-service` xuất hiện cùng phiên bản `1.0.0`.
3. (Tùy chọn) Chuyển package sang chế độ Public trong **Package settings** nếu cần chia sẻ công khai.

---

## 3. Hướng Dẫn Nộp Bài Lên Portal
- Nộp file `commands.txt` hoặc `commands.md` lưu lại các lệnh CLI đã chạy.
- Gửi kèm đường dẫn (URL) tới package:
  `https://github.com/users/Khac-Hai/packages/container/package/payment-service`
  hoặc đính kèm ảnh chụp màn hình trang Packages trên GitHub.
