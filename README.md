# NeoMind-AIGrow-Wrokspace-BE

# AI-Grow Backend

Backend API cho dự án AI-Grow, được xây dựng bằng **Spring Boot** và **MySQL**.  
Hỗ trợ quản lý người dùng (CRUD), đăng nhập, và các chức năng mở rộng sau này.

## 🚀 Tech Stack
- Java 17
- Spring Boot 3
- Spring Data JPA (Hibernate)
- MySQL
- Lombok
- Spring Validation
- Spring Security Crypto (BCrypt)
- Maven

## ⚙️ Yêu cầu hệ thống
- JDK 21 
- Maven 3.9+
- MySQL 8.0.4
- IDE: IntelliJ IDEA / VS Code

## 🛠️ Cài đặt & chạy dự án

git clone https://github.com/codegym-software/NeoMind-AIGrow-Wrokspace-BE

## Tạo database MySQL
CREATE DATABASE ai-grow;

## Cấu hình application.properties

spring.application.name=backend
spring.datasource.url=jdbc:mysql://localhost:3306/ai-grow?useSSL=false&serverTimezone=UTC
spring.datasource.username=root
spring.datasource.password=25112005

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true

server.port=8081

## Build and run

mvn spring-boot:run

## 👨‍💻 Nhóm phát triển: NeoMind