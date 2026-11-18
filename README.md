# Quản lý lịch hẹn

Ứng dụng Android cho bài thực hành quản lý lịch hẹn, viết bằng Java và XML, lưu dữ liệu bằng SQLiteOpenHelper.

## Chạy ứng dụng

Mở thư mục dự án bằng Android Studio, chọn Android SDK 35 và chạy module `app` trên thiết bị Android 6.0 trở lên. Cấu hình đường dẫn Android SDK trong `local.properties` theo máy sử dụng. Tài khoản mẫu: `admin` / `123456`.

Ứng dụng có bốn màn hình: đăng nhập, danh sách lịch hẹn, thêm lịch hẹn và chi tiết lịch hẹn. Danh sách được sắp xếp tăng dần theo ngày giờ. Trong chi tiết có thể đổi trạng thái nhắc nhở hoặc xóa lịch sau khi xác nhận.

## Dữ liệu

Cơ sở dữ liệu `appointments.db`, phiên bản 1, có bảng `users` và `appointments`. Tài khoản và ba lịch mẫu được tạo một lần trong `onCreate()`. Ngày lưu dạng `yyyy-MM-dd`, giờ dạng `HH:mm`, nhắc nhở là 0 hoặc 1. Danh sách và thống kê được tải lại trong `MainActivity.onResume()`.

Giữ nguyên ba lịch mẫu ngày 25–27/10/2026 theo đề, kể cả khi các ngày này đã qua. Khi thêm lịch mới, tên và địa điểm phải có nội dung, ngày giờ phải sau hiện tại. Nhắc nhở chỉ lưu trạng thái, không phát thông báo hệ thống.

## Build

Dùng JDK 21 và Android SDK 35:

```powershell
./gradlew.bat :app:assembleDebug :app:lintDebug
```

APK nằm tại `app/build/outputs/apk/debug/app-debug.apk`.

## Kiểm thử trên máy ảo

Bộ kiểm thử chạy trực tiếp trên Android, không cần thư viện kiểm thử bên ngoài. Bộ kiểm thử xóa và tạo lại dữ liệu của bản ứng dụng đang kiểm thử; chỉ chạy trên máy ảo hoặc bản cài dùng để thử nghiệm.

```powershell
./gradlew.bat :app:assembleDebug :app:assembleDebugAndroidTest
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb install -r app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk
adb shell am instrument -w com.example.quanlylichhen.test/com.example.quanlylichhen.AppointmentTestRunner
adb shell am force-stop com.example.quanlylichhen
adb shell am instrument -w -e phase verify com.example.quanlylichhen.test/com.example.quanlylichhen.AppointmentTestRunner
```

Mỗi lượt thành công in `ALL CHECKS PASSED`. Lượt đầu kiểm tra SQLite và các thao tác giao diện, sau đó để lại dữ liệu kiểm tra. Lượt `verify` xác nhận dữ liệu sau khi dừng tiến trình, rồi khôi phục ba lịch mẫu. Ảnh giao diện được lưu trong thư mục external files `test-screenshots` của ứng dụng trên thiết bị.

## Các lớp chính

| Lớp | Vai trò |
|---|---|
| `Appointment` | Chứa dữ liệu một lịch hẹn và định dạng ngày giờ hiển thị |
| `DatabaseHelper` | Tạo SQLite, dữ liệu mẫu, truy vấn đăng nhập, thêm, đọc, cập nhật nhắc nhở, xóa và đếm |
| `LoginActivity` | Kiểm tra dữ liệu nhập và đăng nhập qua SQLite |
| `MainActivity` | Hiển thị danh sách, thống kê và mở các màn hình |
| `AddAppointmentActivity` | Chọn ngày giờ, kiểm tra hợp lệ và lưu lịch mới |
| `AppointmentDetailActivity` | Truy vấn theo id, đổi nhắc nhở và xóa lịch |
| `AppointmentAdapter` | Hiển thị kết quả truy vấn trong ListView |
| `BaseActivity` | Căn nội dung tránh thanh hệ thống trên Android 15 trở lên |
