# SmartTravel - Ứng dụng Gợi ý Kế hoạch Du lịch Thông minh

## 📱 Mô tả

SmartTravel là một ứng dụng Android được xây dựng để hỗ trợ người dùng tạo và quản lý kế hoạch du lịch một cách thông minh. Ứng dụng sử dụng trí tuệ nhân tạo (AI) - cụ thể là Google Gemini - để tự động sinh ra lịch trình du lịch chi tiết dựa trên sở thích, ngân sách và yêu cầu của người dùng.

## ✨ Tính năng chính

- **🔐 Xác thực người dùng**: Đăng ký, đăng nhập, quên mật khẩu với Firebase Authentication
- **🔍 Khám phá điểm đến**: Tìm kiếm và xem chi tiết các điểm đến du lịch nổi bật
- **🤖 Tạo kế hoạch bằng AI**: Sử dụng AI để tự động tạo lịch trình du lịch chi tiết dựa trên:
  - Điểm đến
  - Thời gian (ngày bắt đầu và kết thúc)
  - Ngân sách
  - Mục đích chuyến đi
  - Người đồng hành
- **📅 Quản lý kế hoạch**: Xem kế hoạch theo dạng danh sách và lịch, chỉnh sửa và theo dõi
- **💡 Gợi ý thông minh**: Hệ thống đề xuất điểm đến phù hợp dựa trên sở thích và lịch sử
- **🗺️ Bản đồ**: Hiển thị bản đồ với OpenStreetMap
- **📸 Quản lý hình ảnh**: Lưu và quản lý hình ảnh địa điểm

## 🛠️ Công nghệ sử dụng

- **Ngôn ngữ**: Kotlin
- **UI Framework**: Jetpack Compose
- **Kiến trúc**: MVVM (Model-View-ViewModel)
- **Dependency Injection**: Hilt
- **Backend**: Firebase
  - Firebase Authentication
  - Cloud Firestore
  - Realtime Database
  - Storage
  - Analytics
- **AI Service**: Google Gemini API
- **Bản đồ**: OpenStreetMap (Osmdroid)
- **Navigation**: Navigation Compose
- **Image Loading**: Coil
- **Lottie Animations**: Lottie Compose

## 📋 Yêu cầu hệ thống

- **Android Studio**: Arctic Fox trở lên (khuyến nghị Hedgehog hoặc mới hơn)
- **JDK**: 11 hoặc cao hơn
- **Android SDK**:
  - Minimum SDK: 26 (Android 8.0)
  - Target SDK: 34 (Android 14)
  - Compile SDK: 36
- **Gradle**: 8.13.0
- **Kotlin**: 2.0.21
- **Kết nối Internet**: Cần thiết cho Firebase và AI API

## 🚀 Cách cài đặt

### Bước 1: Clone repository

```bash
git clone <repository-url>
cd smarttravel-app
```

### Bước 2: Cấu hình Firebase

1. Tạo một dự án Firebase mới tại [Firebase Console](https://console.firebase.google.com/)
2. Thêm ứng dụng Android vào dự án Firebase
3. Tải file `google-services.json` từ Firebase Console
4. Đặt file `google-services.json` vào thư mục `app/` (thay thế file hiện có nếu có)

### Bước 3: Cấu hình Google Gemini API

1. Tạo API key cho Google Gemini tại [Google AI Studio](https://makersuite.google.com/app/apikey)
2. Thêm API key vào file `local.properties`:
   ```properties
   GEMINI_API_KEY=your_api_key_here
   ```
   
   Hoặc cấu hình trong code tại file `AiServiceImpl.kt` hoặc thông qua biến môi trường.

### Bước 4: Cấu hình local.properties (nếu cần)

Tạo file `local.properties` trong thư mục gốc nếu chưa có:
```properties
sdk.dir=C\:\\Users\\YourUsername\\AppData\\Local\\Android\\Sdk
GEMINI_API_KEY=your_gemini_api_key_here
```

### Bước 5: Đồng bộ dự án

1. Mở Android Studio
2. Chọn **File > Open** và chọn thư mục `smarttravel-app`
3. Android Studio sẽ tự động đồng bộ Gradle và tải các dependencies

### Bước 6: Build và chạy ứng dụng

1. Kết nối thiết bị Android hoặc khởi động emulator
2. Chọn thiết bị từ danh sách thiết bị
3. Nhấn **Run** (▶️) hoặc sử dụng phím tắt `Shift + F10`

### Bước 7: Cấp quyền (nếu cần)

Ứng dụng sẽ tự động yêu cầu các quyền sau khi cần:
- **Vị trí**: Để hiển thị bản đồ và tìm kiếm địa điểm
- **Lưu trữ**: Để lưu và tải hình ảnh
- **Internet**: Để kết nối với Firebase và AI API

## 📁 Cấu trúc dự án

```
smarttravel-app/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/example/smarttravel/
│   │   │   │   ├── ui/              # Giao diện người dùng (Compose)
│   │   │   │   ├── data/            # Models, Repositories, Data sources
│   │   │   │   ├── navigation/      # Navigation setup
│   │   │   │   └── ...
│   │   │   ├── res/                 # Resources (images, strings, etc.)
│   │   │   └── AndroidManifest.xml
│   │   └── test/                    # Unit tests
│   ├── build.gradle.kts
│   └── google-services.json         # Firebase config
├── build.gradle.kts                 # Project-level build config
├── gradle/
│   └── libs.versions.toml          # Version catalog
└── settings.gradle.kts
```

## 🔧 Troubleshooting

### Lỗi Firebase
- Đảm bảo file `google-services.json` đã được đặt đúng vị trí trong thư mục `app/`
- Kiểm tra package name trong `AndroidManifest.xml` khớp với package name trong Firebase Console

### Lỗi API Key
- Kiểm tra API key Gemini đã được cấu hình đúng
- Đảm bảo API key có quyền truy cập Gemini API

### Lỗi Build
- Xóa cache: **File > Invalidate Caches / Restart**
- Clean project: **Build > Clean Project**
- Rebuild: **Build > Rebuild Project**

### Lỗi Dependencies
- Đồng bộ lại Gradle: **File > Sync Project with Gradle Files**
- Xóa thư mục `.gradle` và build lại

## 📝 Ghi chú

- Ứng dụng yêu cầu kết nối Internet để hoạt động
- API key Gemini có thể có giới hạn sử dụng tùy theo gói dịch vụ
- Một số tính năng có thể yêu cầu tài khoản Firebase đã được xác thực

## 👥 Thành viên nhóm

- Trần Việt Hiếu (MSSV: 064205000599)
- Trần Tấn Thuận (MSSV: 080205001708)
- Nguyễn Giang (MSSV: 066205016616)

## 📄 License

Dự án này được phát triển cho mục đích học tập.

---

**Lưu ý**: Đây là phiên bản phát triển. Một số tính năng có thể đang trong quá trình hoàn thiện.

