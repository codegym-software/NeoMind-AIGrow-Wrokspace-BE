# Hướng dẫn sử dụng Mock Authentication (Không cần AWS Cognito)

## ✅ Giải pháp không cần đăng nhập AWS Cognito!

### 1. Backend đã được cấu hình

#### Mock JWT Decoder
- Backend sử dụng **Mock JWT Decoder** thay vì validate với Cognito
- Chấp nhận bất kỳ JWT token nào có format hợp lệ
- Không cần kết nối đến AWS Cognito

#### Mock Auth Controller
- Endpoint: `POST /auth/mock/login`
- Tạo JWT token giả để test
- Không cần AWS credentials

### 2. Cách sử dụng

#### Bước 1: Khởi động Backend
```bash
cd NeoMind-BE
mvn spring-boot:run
```

#### Bước 2: Test Mock Authentication

**Login và lấy token:**
```bash
curl -X POST http://localhost:8081/auth/mock/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "name": "Test User"
  }'
```

**Response:**
```json
{
  "access_token": "eyJhbGc...",
  "token_type": "Bearer",
  "expires_in": 3600,
  "user": {
    "email": "test@example.com",
    "name": "Test User",
    "sub": "test@example.com"
  }
}
```

#### Bước 3: Sử dụng token

**Get current user:**
```bash
curl -X GET http://localhost:8081/auth/mock/me \
  -H "Authorization: Bearer YOUR_TOKEN"
```

**Call protected API:**
```bash
curl -X GET http://localhost:8081/api/companies \
  -H "Authorization: Bearer YOUR_TOKEN"
```

### 3. Frontend Integration

#### Option A: Sử dụng Mock Authentication (Khuyến nghị)

1. Thay đổi login flow trong frontend:
```javascript
// Login với mock auth
const login = async (email, name) => {
  const response = await fetch('http://localhost:8081/auth/mock/login', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ email, name })
  });
  
  const data = await response.json();
  localStorage.setItem('token', data.access_token);
  return data;
};
```

2. Sử dụng token trong API calls:
```javascript
const apiCall = async (url) => {
  const token = localStorage.getItem('token');
  return fetch(url, {
    headers: {
      'Authorization': `Bearer ${token}`
    }
  });
};
```

#### Option B: Bypass Authentication hoàn toàn (Development only)

Thêm vào `SecurityConfig.java`:
```java
.requestMatchers("/**").permitAll() // Bypass tất cả security
```

**⚠️ Cảnh báo:** Chỉ dùng cho development!

### 4. Ưu điểm

✅ **Không cần AWS Cognito**: Test local hoàn toàn
✅ **Không cần internet**: Không cần kết nối AWS
✅ **Nhanh**: Không cần validate với Cognito
✅ **Đơn giản**: Dễ test và debug

### 5. Lưu ý

⚠️ **Mock Authentication chỉ dùng cho development!**
- Không dùng cho production
- Token không được validate với Cognito
- Bảo mật thấp

⚠️ **Khi deploy production:**
- Revert lại JwtDecoder về dùng Cognito
- Xóa MockAuthController
- Cấu hình Cognito đúng cách

### 6. Revert về Cognito Authentication

Nếu muốn dùng lại Cognito:

```java
@Bean
JwtDecoder jwtDecoder() {
    String issuer = "https://cognito-idp.us-east-1.amazonaws.com/us-east-1_OpgQZqXXW";
    NimbusJwtDecoder decoder = (NimbusJwtDecoder) JwtDecoders.fromIssuerLocation(issuer);
    
    var withIssuer = JwtValidators.createDefaultWithIssuer(issuer);
    
    OAuth2TokenValidator<Jwt> tokenUseIsAccess = jwt -> {
        String tokenUse = jwt.getClaimAsString("token_use");
        return "access".equals(tokenUse)
                ? OAuth2TokenValidatorResult.success()
                : OAuth2TokenValidatorResult.failure(
                new OAuth2Error("invalid_token", "token_use must be 'access'", null));
    };
    
    decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(withIssuer, tokenUseIsAccess));
    return decoder;
}
```

### 7. Testing

Test mock authentication:

```bash
# 1. Login
curl -X POST http://localhost:8081/auth/mock/login \
  -H "Content-Type: application/json" \
  -d '{"email": "user@test.com", "name": "User Test"}'

# 2. Copy token từ response
# 3. Test API
curl -X GET http://localhost:8081/api/companies \
  -H "Authorization: Bearer YOUR_TOKEN"
```

## 🎉 Hoàn thành!

Bây giờ bạn có thể:
- ✅ Test backend mà không cần AWS Cognito
- ✅ Development local hoàn toàn offline
- ✅ Không cần cấu hình CORS trên AWS
- ✅ Frontend có thể kết nối backend ngay lập tức






