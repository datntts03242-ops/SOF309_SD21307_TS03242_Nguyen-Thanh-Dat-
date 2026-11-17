# Kết quả JUnit Tuần 2 · SuMon

Chạy thực tế trên Linux ngày 15/11/2026, Temurin JDK 21, Maven 3.9.9.

Lệnh chạy: `mvn clean test -Dtest=SuMonControllerTest`.

| Lớp test | Số lượt | Failures | Errors | Skipped |
|---|---:|---:|---:|---:|
| controller.SuMonControllerTest | 20 | 0 | 0 | 0 |
| **Tổng** | **20** | **0** | **0** | **0** |

**BUILD SUCCESS.** JUnit dùng DAO/service giả lập. 28 ca chạy tay với web/SQL chưa được thực thi.

Báo cáo trong ZIP ở minh-chung-junit/linux-2026-11-15. CMD đăng GitHub chạy lại bộ test và đưa báo cáo của máy sinh viên vào minh-chung-junit/windows-latest.

Đã bổ sung các trường userId, userName, difficulty, score và createdAt cùng phương thức truy cập trong QuizResult để QuizResultDAO và RankingDAO biên dịch. Kiểm tra này chưa chạy ghi điểm hoặc bảng xếp hạng trên SQL Server.
