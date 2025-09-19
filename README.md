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
- **Maven**:  Tải xuống phiên bản mới nhất từ trang chủ Maven. Giải nén file và cấu hình biến môi trường MAVEN_HOME, sau đó thêm %MAVEN_HOME%\bin vào biến Path. Mở Terminal/Command Prompt mới và kiểm tra phiên bản bằng lệnh:
  ```bash
  mvn -v

- **MySQL Server và MySQL Workbench**: Tải xuống MySQL Installer từ MySQL Community Downloads. Trong quá trình cài đặt, hãy chọn tùy chọn "Full" để cài đặt cả MySQL Server và MySQL Workbench. Ghi nhớ mật khẩu root mà bạn đã thiết lập.

### 2️⃣ Tải dự án và cấu hình  

- **Clone project**:  
  ```bash
  git clone https://github.com/codegym-software/NeoMind-AIGrow-Wrokspace-BE
  cd NeoMind-AIGrow-Wrokspace-BE

- **Tạo database trong MySQL**:
  ```bash
  CREATE DATABASE `ai-grow`;


- **Cấu hình file src/main/resources/application.properties**:
  ```bash
   spring.application.name=backend
   spring.datasource.url=jdbc:mysql://localhost:3306/ai-grow?useSSL=false&serverTimezone=UTC
   spring.datasource.username=YOUR_USERNAME
   spring.datasource.password=YOUR_PASSWORD

   spring.jpa.hibernate.ddl-auto=update
   spring.jpa.show-sql=true
   spring.jpa.properties.hibernate.format_sql=true

   server.port=8081

## 3️⃣ Build & Run dự án
- Chạy lệnh:
  ```bash
   mvn spring-boot:run

Mặc định ứng dụng sẽ chạy tại:
👉 http://localhost:8081



## 👨‍💻 Nhóm phát triển
_**NeoMind Team**_ – AIGrow Workspace Backend