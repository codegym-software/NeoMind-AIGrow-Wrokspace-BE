# Hướng dẫn xử lý lỗi CORS với AWS Cognito

## Vấn đề hiện tại

**Lỗi CORS**: `No 'Access-Control-Allow-Origin' header is present`

**Nguyên nhân**: AWS Cognito chưa được cấu hình để cho phép requests từ `http://localhost:3000`

## Giải pháp

### 1. Cấu hình AWS Cognito (Khuyến nghị)

#### Bước 1: Mở AWS Console
1. Truy cập [AWS Console](https://console.aws.amazon.com/)
2. Chọn **Cognito** service
3. Chọn User Pool: `ap-southeast-2_TespiMS7C`

#### Bước 2: Cấu hình App Integration
1. Vào **App integration** tab
2. Tìm App client: `50j41pblepregei8fsrjok3lmd`
3. Click **Edit** button

#### Bước 3: Thêm Allowed Origins
1. Trong **Hosted UI**, tìm **Allowed callback URLs**
2. Thêm: `http://localhost:3000/callback`
3. Trong **Sign-out URLs**
4. Thêm: `http://localhost:3000/logout`

#### Bước 4: Thêm CORS Origins
1. Tìm **OAuth 2.0 grant types**
2. Đảm bảo có **Authorization code grant** và **Implicit grant**
3. Lưu thay đổi

### 2. Cấu hình Backend (Alternative)

Backend đã được cấu hình để hỗ trợ CORS:

```java
// SecurityConfig.java
@Bean
public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration cfg = new CorsConfiguration();
    cfg.setAllowedOriginPatterns(List.of("http://localhost:*", "http://127.0.0.1:*"));
    cfg.setAllowedMethods(List.of("GET","POST","PUT","DELETE","OPTIONS","PATCH"));
    cfg.setAllowedHeaders(List.of("Authorization","Content-Type","X-Requested-With","Accept","Origin"));
    cfg.setExposedHeaders(List.of("*"));
    cfg.setAllowCredentials(true);
    cfg.setMaxAge(3600L);
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", cfg);
    return source;
}
```

### 3. Nếu không có quyền truy cập AWS Console

#### Option A: Chạy Chrome với CORS disabled
```bash
chrome.exe --user-data-dir=C:\temp\chrome_dev_session --disable-web-security --disable-features=OutOfBlinkCors
```

#### Option B: Sử dụng Extension
- CORS Unblock
- ModHeader

## Frontend đang chạy

- **URL**: `http://localhost:3000`
- **Backend**: `http://localhost:8081`
- **Proxy**: Đã cấu hình trong `setupProxy.js`

## Kiểm tra cấu hình

### 1. Kiểm tra Backend
```bash
# Trong NeoMind-BE
mvn spring-boot:run
```

### 2. Kiểm tra Frontend
```bash
# Trong NeoMind-FE
npm start
```

### 3. Test API
```bash
curl -X GET http://localhost:8081/health
```

## Troubleshooting

### Lỗi: "redirect_uri_mismatch"
**Giải pháp**: Đảm bảo callback URL trong Cognito khớp với URL frontend

### Lỗi: "CORS policy"
**Giải pháp**: 
1. Kiểm tra Cognito configuration
2. Kiểm tra backend CORS config
3. Sử dụng proxy trong frontend

## Tài liệu tham khảo

- [AWS Cognito CORS](https://docs.aws.amazon.com/cognito/latest/developerguide/cognito-user-pools-app-client-settings.html)
- [React CORS](https://create-react-app.dev/docs/proxying-api-requests-in-development/)
- [Spring Boot CORS](https://spring.io/guides/gs/rest-service-cors/)






