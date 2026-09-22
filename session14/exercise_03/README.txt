1. Hướng dẫn thao tác Import Dashboard bằng ID (4701)
Truy cập Grafana: Mở trình duyệt và truy cập vào Grafana (http://localhost:3000).

Mở giao diện Import:

Trên thanh menu bên trái, nhấp vào biểu tượng dấu + (hoặc mục Dashboards).

Chọn Import.

Nhập Dashboard ID:

Tại mục Find and import dashboards through grafana.com, nhập ID: 4701 (JVM Micrometer Dashboard).

Nhấp nút Load.

Cấu hình thông số Import:

Name: Có thể giữ nguyên tên mặc định hoặc đổi tên nếu muốn.

Folder: Chọn thư mục lưu trữ (ví dụ: General).

Prometheus: Tại trường chọn Data Source ở dưới cùng, chọn Data Source Prometheus mà bạn đã kết nối ở Bài 1.

Hoàn tất: Nhấp nút Import. Dashboard sẽ được tải và hiển thị ngay lập tức.

2. Mô tả & Giải thích ý nghĩa 3 biểu đồ quan trọng nhất đối với ứng dụng Java
1. JVM Heap Memory (Sử dụng bộ nhớ Heap):

Mô tả: Biểu đồ hiển thị mức độ sử dụng bộ nhớ Heap (Used) so với mức tối đa cho phép (Max/Committed).

Ý nghĩa: Giúp theo dõi lượng RAM thực tế mà ứng dụng Java đang dùng để lưu trữ các Object. Nếu bộ nhớ Heap liên tục tăng tiệm cận mức Max mà không giảm sau khi Garbage Collection (GC) chạy, đó là dấu hiệu của lỗi Memory Leak (rò rỉ bộ nhớ), có thể dẫn đến lỗi nghiêm trọng OutOfMemoryError (OOM).

2. GC Pauses / Garbage Collection Time (Thời gian tạm dừng do dọn rác):

Mô tả: Biểu đồ đo tần suất và khoảng thời gian tiến trình GC kích hoạt để dọn dẹp các đối tượng không còn sử dụng trong bộ nhớ.

Ý nghĩa: Khi GC chạy (đặc biệt là Full GC), ứng dụng có thể bị khựng lại (Stop-The-World). Nếu thời gian GC Pauses quá cao hoặc diễn ra liên tục, hiệu năng của ứng dụng sẽ giảm mạnh, gây ra hiện tượng giật lag hoặc request bị timeout.

3. JVM Threads (Số lượng Thread trong JVM):

Mô tả: Hiển thị số lượng Thread đang ở các trạng thái khác nhau (Live, Daemon, Timed_Waiting, Blocked,...).

Ý nghĩa: Giúp phát hiện hiện tượng Thread Leak hoặc hiện tượng Deadlock (các thread chờ nhau vô tận). Nếu số lượng thread tăng đột biến mà không hạ xuống, server sẽ cạn kệt tài nguyên hệ thống và không thể xử lý các request mới.

3. Giải thích & Khắc phục câu hỏi bẫy ("No data" do thiếu variable application)
Thực trạng bẫy: Sau khi import, Dashboard báo lỗi "No data" do biến $application trên đầu trang trống rỗng (không chọn được ứng dụng nào).

Nguyên nhân: Dashboard ID 4701 lọc metrics dựa trên tag application do Micrometer gửi sang Prometheus. Mặc định Spring Boot Actuator/Micrometer không tự động thêm tag application vào các metrics nếu chưa được khai báo trong file cấu hình.

Cách khắc phục:
Thêm cấu hình tên ứng dụng vào mục management.metrics.tags.application trong file application.yml (hoặc application.properties) của ứng dụng Spring Boot:

YAML
management:
  endpoints:
    web:
      exposure:
        include: prometheus, health, info
  metrics:
    tags:
      application: my-spring-boot-app # <--- Dòng bị thiếu
Kết quả: Sau khi thêm dòng cấu hình trên và khởi động lại ứng dụng Spring Boot, Micrometer sẽ gắn tag application="my-spring-boot-app" vào mọi metric export ra Prometheus. Lúc này biến $application trên Grafana sẽ tự động nhận diện tên ứng dụng và hiển thị đầy đủ dữ liệu trên Dashboard.