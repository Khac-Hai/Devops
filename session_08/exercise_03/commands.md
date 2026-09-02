# Bài 3: Thiết Lập Quy Trình Build và Push Image Lên GHCR

## 1. Thông Tin Nộp Bài
- **Học viên**: Khac-Hai
- **Repository**: [https://github.com/Khac-Hai/Devops](https://github.com/Khac-Hai/Devops)
- **Danh sách Packages trên GitHub Profile**: [https://github.com/Khac-Hai?tab=packages](https://github.com/Khac-Hai?tab=packages)
- **Đường dẫn Package `payment-service`**: [https://github.com/users/Khac-Hai/packages/container/package/payment-service](https://github.com/users/Khac-Hai/packages/container/package/payment-service)

---

## 2. Nhật Ký Câu Lệnh CLI (Command Execution Log)

### Bước 1: Khởi tạo Personal Access Token (PAT)
- Vào GitHub: **Settings > Developer Settings > Personal access tokens > Tokens (classic)**
- Tạo Token với các quyền (scopes):
  - `write:packages`
  - `read:packages`
  - `delete:packages`
  - `repo`

### Bước 2: Đăng nhập Docker CLI vào GitHub Container Registry (`ghcr.io`)
```powershell
# PowerShell (Windows):
$env:CR_PAT="ghp_xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx"
$env:CR_PAT | docker login ghcr.io -u Khac-Hai --password-stdin
```
*Kết quả:*
```text
Login Succeeded
```

### Bước 3: Build và gắn Tag Image theo chuẩn Namespace GHCR
```powershell
cd session_08/exercise_03

# Build và tag 1.0.0
docker build -t ghcr.io/khac-hai/payment-service:1.0.0 .

# Tag latest (tùy chọn)
docker tag ghcr.io/khac-hai/payment-service:1.0.0 ghcr.io/khac-hai/payment-service:latest
```

### Bước 4: Push Image lên GitHub Container Registry
```powershell
docker push ghcr.io/khac-hai/payment-service:1.0.0
docker push ghcr.io/khac-hai/payment-service:latest
```
*Kết quả:*
```text
The push refers to repository [ghcr.io/khac-hai/payment-service]
1.0.0: digest: sha256:xxxxxxxx size: xxxx
```

---

## 3. Kiểm Tra Kết Quả
Truy cập [https://github.com/Khac-Hai?tab=packages](https://github.com/Khac-Hai?tab=packages) để xem package `payment-service:1.0.0` đã xuất hiện trên trang cá nhân.
