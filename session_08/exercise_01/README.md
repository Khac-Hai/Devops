# Session 08 - Exercise 01: Sửa Lỗi Cấu Hình Chia Sẻ Docker Socket (DooD)

## 1. Phân Tích Hiện Tượng & Nguyên Nhân Lỗi

### Thông báo lỗi gặp phải:
```text
Cannot connect to the Docker daemon at unix:///var/run/docker.sock. Is the docker daemon running?
```

### Bản chất kỹ thuật & Nguyên nhân:
1. **Kiến trúc Client - Server của Docker**:
   - Docker bao gồm 2 thành phần chính: **Docker CLI** (Client) và **Docker Daemon - `dockerd`** (Server/Engine).
   - Khi chạy trên Linux, Docker CLI gửi các câu lệnh (REST API calls) tới Docker Daemon thông qua một **Unix Domain Socket** mặc định nằm tại đường dẫn `/var/run/docker.sock`.
2. **Container Isolation (Tính cô lập của container)**:
   - Self-Hosted Runner được triển khai dưới dạng một Docker container độc lập.
   - Bên trong container Runner chỉ có sẵn công cụ Docker CLI nhưng **không có Docker Daemon riêng**.
   - Mặc định, container không thể truy cập socket của máy Host nếu không được cấu hình ánh xạ (volume mount).
   - Khi luồng CI gọi các lệnh như `docker build` hay `docker info`, Docker CLI tìm kiếm socket `/var/run/docker.sock` bên trong container nhưng không thấy (hoặc không kết nối được tới Daemon), dẫn đến lỗi trên.

---

## 2. Giải Pháp: Mô Hình Docker-outside-of-Docker (DooD)

### Khái niệm DooD:
- **Docker-outside-of-Docker (DooD)** là giải pháp cho phép container Runner sử dụng trực tiếp Docker Daemon đang chạy trên máy chủ vật lý (Host Machine) bằng cách mount file Unix socket `/var/run/docker.sock` từ Host vào Container.
- Bất kỳ container nào được `docker run` hay `docker build` từ bên trong Runner sẽ thực chất được sinh ra và quản lý bởi Host Docker Engine (nằm ngang hàng với Runner container).

### So sánh DooD vs DinD (Docker-in-Docker):
| Tiêu chí | DooD (Docker-outside-of-Docker) | DinD (Docker-in-Docker) |
| :--- | :--- | :--- |
| **Cách thức** | Mount socket `/var/run/docker.sock` từ Host | Chạy một Docker Daemon hoàn toàn độc lập bên trong container |
| **Yêu cầu quyền** | Không cần `--privileged` (bảo mật hơn) | Yêu cầu quyền Root/`--privileged` trên container |
| **Tận dụng Cache** | Dùng chung Docker image cache của Host (build nhanh) | Cache riêng biệt, tốn dung lượng và tài nguyên |
| **Độ phức tạp** | Đơn giản, nhẹ nhàng, phổ biến trong CI/CD | Phức tạp, dễ gặp xung đột Storage Driver |

---

## 3. Chi Tiết Cấu Hình Đã Chỉnh Sửa

Trong file `docker-compose.yml`:
```yaml
version: '3.8'

services:
  quickbite-dood-runner:
    image: myoung34/github-runner:latest
    container_name: quickbite-dood-runner
    restart: unless-stopped
    environment:
      - REPO_URL=${REPO_URL:-https://github.com/Khac-Hai/Devops}
      - RUNNER_TOKEN=${RUNNER_TOKEN}
      - RUNNER_NAME=${RUNNER_NAME:-quickbite-dood-runner}
      - RUNNER_LABELS=${RUNNER_LABELS:-quickbite-runner,dood-runner,linux,x64}
      - RUNNER_WORKDIR=/tmp/runner/work
      - DISABLE_AUTO_UPDATE=true
      - DOCKER_PRUNE=false
    volumes:
      # ÁNH XẠ DOCKER SOCKET ĐỂ SỬ DỤNG DOCKER DAEMON CỦA MÁY HOST (DooD)
      - /var/run/docker.sock:/var/run/docker.sock
      # Ánh xạ workdir để lưu cache và artifacts
      - runner-work:/tmp/runner/work

volumes:
  runner-work:
    name: quickbite-dood-runner-work
```

---

## 4. Hướng Dẫn Thực Hành Từng Bước

### Bước 1: Lấy Runner Registration Token từ GitHub
1. Truy cập Repository trên GitHub: `https://github.com/Khac-Hai/Devops`
2. Vào **Settings** > **Actions** > **Runners**.
3. Nhấn **New runner**, chọn **Linux** / **x64**.
4. Sao chép chuỗi mã token ở bước cài đặt (ví dụ: `AQXXXXXXXXXXXXX`).

### Bước 2: Thiết lập biến môi trường `.env`
1. Tạo file `.env` từ file mẫu:
   ```bash
   cp session_08/exercise_01/.env.example session_08/exercise_01/.env
   ```
2. Mở file `.env` và dán token vừa lấy vào:
   ```env
   REPO_URL=https://github.com/Khac-Hai/Devops
   RUNNER_TOKEN=AQXXXXXXXXXXXXXX
   RUNNER_NAME=quickbite-dood-runner
   RUNNER_LABELS=quickbite-runner,dood-runner,linux,x64
   ```

### Bước 3: Khởi chạy Self-Hosted Runner
Di chuyển vào thư mục `session_08/exercise_01` và khởi động container:
```bash
docker compose up -d
```
Kiểm tra log để đảm bảo Runner đã kết nối thành công:
```bash
docker compose logs -f
```
*(Khi thấy thông báo `Listening for Jobs` là Runner đã sẵn sàng nhận việc).*

### Bước 4: Kiểm chứng luồng CI trên GitHub Actions
1. Đẩy code lên nhánh `main` của repository GitHub.
2. Vào tab **Actions** trên GitHub, chọn workflow **Session 08 Exercise 01 - Docker Socket DooD CI**.
3. Xem chi tiết log thực thi của Job:
   - Bước **Check Docker Version & Client**: Hiển thị phiên bản Docker CLI.
   - Bước **Verify Docker Daemon Connectivity (DooD)**: Lệnh `docker info` chạy thành công mà không còn gặp lỗi `Cannot connect to the Docker daemon`.
   - Bước **Build Docker Image inside CI Runner**: Lệnh `docker build` build thành công ứng dụng Java `quickbite/exercise01-app:latest`.
   - Bước **Run Built Container**: Container chạy in ra dòng chữ kiểm thử Java thành công.

---

## 5. Hướng Dẫn Nộp Bài Lên Portal
- **File cần nộp**: File `docker-compose.yml` (trong `session_08/exercise_01/docker-compose.yml`).
- **Hình ảnh minh chứng**: Chụp ảnh màn hình log GitHub Actions với job chạy thành công (đặc biệt là log của các step `docker info` và `docker build`).
