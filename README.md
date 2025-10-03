# 🌱 AI-Grow Backend  

Backend API cho dự án **AI-Grow**, được xây dựng bằng **Spring Boot** và **MySQL**.  
Hệ thống hỗ trợ **CRUD người dùng, đăng nhập**, và các tính năng mở rộng trong tương lai.  

---

## 🚀 Tech Stack
- **☕ Java 21**
- **🌱 Spring Boot 3**
- **🗄 Spring Data JPA (Hibernate)**
- **🐬 MySQL 8**
- **🧩 Lombok**
- **✅ Spring Validation**
- **🔐 Spring Security (BCrypt)**
- **📦 Maven**

---

## ⚙️ Yêu cầu hệ thống
- **JDK 21** → [Tải tại đây](https://jdk.java.net/)  
- **Maven 3.9+** → [Tải tại đây](https://maven.apache.org/download.cgi)  
- **MySQL 8.0.4** → [Tải tại đây](https://dev.mysql.com/downloads/installer/)  
- IDE khuyến nghị: **IntelliJ IDEA** / **VS Code**  

---

## 🛠️ Hướng dẫn cài đặt & chạy dự án  

### 1️⃣ Cài đặt môi trường  
- **Java Development Kit (JDK) 21**:  Tải xuống và cài đặt từ trang web chính thức của Oracle hoặc OpenJDK. Sau khi cài đặt, mở Terminal/Command Prompt và kiểm tra phiên bản bằng lệnh:
```bash
java -version
```
- **Maven**:  Tải xuống phiên bản mới nhất từ trang chủ Maven. Giải nén file và cấu hình biến môi trường MAVEN_HOME, sau đó thêm %MAVEN_HOME%\bin vào biến Path. Mở Terminal/Command Prompt mới và kiểm tra phiên bản bằng lệnh:
```bash
mvn -v
```
- **MySQL Server và MySQL Workbench**: Tải xuống MySQL Installer từ MySQL Community Downloads. Trong quá trình cài đặt, hãy chọn tùy chọn "Full" để cài đặt cả MySQL Server và MySQL Workbench. Ghi nhớ mật khẩu root mà bạn đã thiết lập.

### 2️⃣ Tải dự án và cấu hình  
- **Clone project**:  
```bash
git clone https://github.com/codegym-software/NeoMind-AIGrow-Wrokspace-BE
cd NeoMind-AIGrow-Wrokspace-BE
```
- **Tạo database trong MySQL**:
```sql
CREATE DATABASE `ai-grow`;
```
- **Cấu hình file `src/main/resources/application.properties`**:
```properties
spring.application.name=backend
spring.datasource.url=jdbc:mysql://localhost:3306/ai-grow?useSSL=false&serverTimezone=UTC
spring.datasource.username=YOUR_USERNAME
spring.datasource.password=YOUR_PASSWORD

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true

server.port=8081
```

## 3️⃣ Build & Run dự án (local)
- Chạy lệnh:
```bash
mvn spring-boot:run
```
Mặc định ứng dụng sẽ chạy tại: http://localhost:8080

---

## 🐳 Triển khai bằng Docker

### Tùy chọn A: Chạy container app độc lập
1. Build image:
```bash
docker build -t aigrow-backend:latest .
```
2. Chạy container (cần có MySQL đang chạy và `application.properties` trỏ đúng DB):
```bash
docker run --name aigrow-backend \
  -e SPRING_PROFILES_ACTIVE=dev \
  -e SERVER_PORT=8088 \
  -e SPRING_DATASOURCE_URL="jdbc:mysql://host.docker.internal:3306/ai-grow?useSSL=false&serverTimezone=UTC" \
  -e SPRING_DATASOURCE_USERNAME=YOUR_USERNAME \
  -e SPRING_DATASOURCE_PASSWORD=YOUR_PASSWORD \
  -p 8088:8088 \
  aigrow-backend:latest
```

### Tùy chọn B: Dùng Docker Compose (app + MySQL)
- File `docker-compose.yml` đã cấu hình sẵn MySQL và app. Chạy:
```bash
docker compose up -d --build
```
- Mặc định:
  - App: http://localhost:8088
  - MySQL: localhost:3306 (user: app / pass: app / db: ai-grow)

#### Tùy biến nhanh qua biến môi trường
- PORT app trong container: `SERVER_PORT` (mặc định 8088 qua compose)
- Profile Spring: `SPRING_PROFILES_ACTIVE` (mặc định `dev`)
- Database:
  - `SPRING_DATASOURCE_URL` (ví dụ: `jdbc:mysql://db:3306/ai-grow?useSSL=false&serverTimezone=UTC`)
  - `SPRING_DATASOURCE_USERNAME`
  - `SPRING_DATASOURCE_PASSWORD`

> Lưu ý: Ứng dụng đọc một số cấu hình qua `application.yml` bằng placeholder env (ISSUER_URI, CLIENT_ID, FRONTEND_ORIGIN). Bạn có thể override qua env trong Compose nếu cần.

---
## 📧 Cấu hình Email SMTP & lấy App Password
Dự án sử dụng Gmail để gửi email (ví dụ: thông báo, invite user). Gmail không cho phép sử dụng mật khẩu chính trực tiếp, bạn cần tạo App Password.  

### Bật 2-Step Verification cho Gmail
-Truy cập Google Account Security


-Trong mục “Signing in to Google”, bật 2-Step Verification.

-Hoàn tất các bước xác minh (qua điện thoại, Authenticator App, v.v.).

### Tạo App Password
1. Sau khi bật 2-Step Verification, vào App Passwords:
https://myaccount.google.com/apppasswords

2. Chọn:
- App: Mail
- Device: Other (tên tuỳ ý, ví dụ AI-Grow Backend)

3. Nhấn Generate.

4. Copy app password và paste vào file `application.properties`:
```properties
spring.mail.username=your_email@gmail.com
spring.mail.password=YOUR_APP_PASSWORD
```

## 👨‍💻 Nhóm phát triển
_**NeoMind Team**_ – AIGrow Workspace Backend
