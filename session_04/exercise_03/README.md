# Bài tập 3: Tối ưu hóa biến môi trường và Custom Network

## 1. Phân tích các cải tiến bảo mật và tối ưu hóa

### 1.1. Tách cấu hình nhạy cảm ra file `.env`
- **Vấn đề trước đây:** Việc hardcode thông tin nhạy cảm như `DB_USER` và `DB_PASS` trực tiếp trong `docker-compose.yml` tiềm ẩn rủi ro lộ mật khẩu khi mã nguồn được đẩy lên các kho lưu trữ công khai (GitHub, GitLab, v.v.).
- **Giải pháp:**
  - Tạo file `.env` để lưu trữ các giá trị thực tế của cấu hình môi trường.
  - Sử dụng cú pháp nội suy `${VARIABLE_NAME}` trong `docker-compose.yml` để nạp tự động các biến từ file `.env`.
  - Tạo file `.env.example` chứa các biến mẫu (không chứa mật khẩu thật) để các thành viên khác trong team biết cần cấu hình những biến nào.
  - Thêm `.env` vào file `.gitignore` để ngăn chặn việc vô tình commit thông tin nhạy cảm lên Git.

### 1.2. Đóng cổng Database ra máy Host
- **Vấn đề trước đây:** Cấu hình `ports: - "5432:5432"` mở cổng cơ sở dữ liệu trực tiếp ra ngoài Internet/máy Host. Kẻ tấn công hoặc các tiến trình khác trên máy có thể quét và dò mật khẩu Database.
- **Giải pháp:** Xóa bỏ phần `ports` ở service `postgres`. Nhờ đó:
  - Database không bị lộ ra bên ngoài máy Host.
  - Chỉ các service nằm chung mạng nội bộ Docker (như `backend`) mới có thể kết nối đến cổng 5432 của `postgres`.

### 1.3. Cô lập hệ thống mạng bằng Custom Network (`storex-net`)
- **Vấn đề trước đây:** Sử dụng mạng mặc định (default network) khiến tất cả các container không liên quan nếu cùng kết nối vào mạng mặc định đều có thể dò quét và truy cập Database.
- **Giải pháp:**
  - Khai báo một Custom Bridge Network có tên `storex-net`.
  - Gán cả 2 service `backend` và `postgres` vào mạng này.
  - Tăng cường tính cô lập (isolation), kiểm soát lưu lượng và tăng tính an toàn cho hệ thống.

---

## 2. File cấu hình hoàn chỉnh: `docker-compose.yml`

```yaml
services:
  backend:
    image: storex-backend:v3
    ports:
      - "8080:8080"
    environment:
      - DB_HOST=postgres
      - DB_USER=${DB_USER}
      - DB_PASS=${DB_PASS}
    networks:
      - storex-net
    depends_on:
      - postgres

  postgres:
    image: postgres:13
    # Đã đóng cổng 5432 ra host để đảm bảo an toàn
    environment:
      - POSTGRES_USER=${DB_USER}
      - POSTGRES_PASSWORD=${DB_PASS}
    networks:
      - storex-net

networks:
  storex-net:
    driver: bridge
```

---

## 3. Nội dung file `.env` và `.env.example`

- **File `.env` (chạy trên môi trường local, không commit lên Git):**
  ```env
  DB_USER=root
  DB_PASS=supersecret_dont_share
  ```

- **File `.env.example` (file mẫu được commit lên Git repo):**
  ```env
  DB_USER=your_db_username
  DB_PASS=your_db_password
  ```

- **File `.gitignore`:**
  ```gitignore
  .env
  ```

---

## 4. Hướng dẫn chạy và kiểm tra kết quả

### Bước 1: Khởi tạo file `.env` từ file mẫu (nếu mới clone repo)
```bash
cp .env.example .env
# Chỉnh sửa thông tin mật khẩu trong .env nếu cần
```

### Bước 2: Khởi chạy các container
```bash
docker compose up -d
```

### Bước 3: Kiểm tra trạng thái và port của các service
```bash
docker compose ps
```
*Kết quả:* Service `backend` mở cổng `8080->8080/tcp`, còn `postgres` chỉ mở cổng nội bộ `5432/tcp` (không map ra host).

### Bước 4: Kiểm tra log kết nối của backend
```bash
docker compose logs backend
```
*Kết quả:* Backend nhận đúng thông tin biến môi trường từ `.env` và kết nối thành công tới `postgres:5432` trong mạng `storex-net`.

### Bước 5: Dừng dịch vụ khi xong việc
```bash
docker compose down
```
