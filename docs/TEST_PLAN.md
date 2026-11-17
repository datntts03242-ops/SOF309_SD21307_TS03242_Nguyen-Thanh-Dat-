# Kế hoạch kiểm thử SuMon

Nguyễn Thành Đạt · TS03242 · SD21307. Lịch đề xuất cập nhật Tuần 1 và 2 vào 15/11/2026.

| Nhân lực | Thời hạn | Ngày bắt đầu | Ngày bàn giao | Phương thức | Phạm vi và nhiệm vụ |
|---|---|---|---|---|---|
| Nguyễn Thành Đạt | Buổi cập nhật | 15/11/2026 | 15/11/2026 | Đọc mã và tài liệu | Đối chiếu đăng ký, đăng nhập và CRUD triều đại với đặc tả. |
| Nguyễn Thành Đạt | Buổi cập nhật | 15/11/2026 | 15/11/2026 | JUnit 5 và Mockito | Thực thi 20 ca UT_001–UT_020. Biên 5/6/7 ký tự tại form và hành vi server khi POST trực tiếp. Kiểm tra năm trước/bằng/sau, ID và năm sai định dạng. |
| Nguyễn Thành Đạt | Khi có SQL Server thử | 15/11/2026 | Cập nhật sau khi chạy | Eclipse/Tomcat, trình duyệt, HTTP | Chạy 28 ca TC001–TC028. Kiểm tra phản hồi, quyền và bản ghi DB. |
| Nguyễn Thành Đạt | Sau khi kiểm tra | 15/11/2026 | 15/11/2026 | Maven và GitHub | Lưu report Surefire, cập nhật trạng thái thực tế và nộp link commit cá nhân. |

| Điều kiện | Tiêu chí |
|---|---|
| Bắt đầu JUnit | JDK 21, Maven, JUnit 5 và Mockito. Chuẩn bị được dependencies. |
| Bắt đầu ca web | Tomcat tương thích Servlet, SQL Server thử, dữ liệu nền và tài khoản đúng vai trò. Xem SPECIFICATIONS.md. |
| Tạm dừng | Ghi lỗi môi trường hoặc dữ liệu thiếu. Khi xử lý xong, chạy lại ca bị ảnh hưởng. |
| Hoàn thành JUnit | 20 ca, 0 failures, 0 errors, 0 skipped. Giữ báo cáo Maven. |
| Hoàn thành web | Ghi Actual Result cho 28 ca. Với sai khác, ghi ID, dữ liệu, bước tái hiện và bằng chứng. |

JUnit xác nhận phản hồi HTTP, session và lời gọi DAO/service tại Controller. Các ca ngoại lệ ghi hành vi hiện tại để theo dõi. Kiểm tra ghi dữ liệu, JSP, filter và upload thực hiện với Tomcat và SQL Server thử.
