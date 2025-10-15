# 🔧 **FIX COGNITO CONNECTION ERROR**

## 🚨 **VẤN ĐỀ:**
```
java.net.UnknownHostException: cognito-idp.ap-southeast-2.amazonaws.com
```

**Backend không thể kết nối đến AWS Cognito để verify JWT token.**

---

## 🔧 **GIẢI PHÁP:**

### **Option 1: Kiểm tra Network**
```bash
# Test kết nối
ping cognito-idp.ap-southeast-2.amazonaws.com
nslookup cognito-idp.ap-southeast-2.amazonaws.com
```

### **Option 2: Disable JWT Verification (Tạm thời)**

#### **A. Tạo SecurityConfig mới (không cần Cognito):**
```java
// File: src/main/java/com/springboot/demo/config/SecurityConfigTest.java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@Profile("test") // Chỉ active khi profile = test
public class SecurityConfigTest {
    
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/health").permitAll()
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .anyRequest().permitAll() // Cho phép tất cả requests
            );
        return http.build();
    }
    
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration cfg = new CorsConfiguration();
        cfg.setAllowedOriginPatterns(List.of("*"));
        cfg.setAllowedMethods(List.of("GET","POST","PUT","DELETE","OPTIONS","PATCH"));
        cfg.setAllowedHeaders(List.of("*"));
        cfg.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cfg);
        return source;
    }
}
```

#### **B. Chạy với profile test:**
```bash
# Chạy backend với profile test (không cần Cognito)
.\mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=test
```

### **Option 3: Mock JWT Token**

#### **A. Tạo Mock JWT Decoder:**
```java
// File: src/main/java/com/springboot/demo/config/MockJwtDecoder.java
@Component
@Profile("test")
public class MockJwtDecoder implements JwtDecoder {
    
    @Override
    public Jwt decode(String token) throws JwtException {
        // Tạo mock JWT token
        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", "test-user-id");
        claims.put("email", "test@example.com");
        claims.put("token_use", "access");
        
        return new Jwt(token, Instant.now(), Instant.now().plusSeconds(3600), 
                     Map.of("alg", "RS256"), claims);
    }
}
```

#### **B. Update SecurityConfig:**
```java
@Bean
@Profile("!test") // Chỉ active khi KHÔNG phải profile test
JwtDecoder jwtDecoder(@Value("${cognito.issuer-uri}") String issuer) {
    // Original JWT decoder code
}

@Bean
@Profile("test") // Chỉ active khi profile = test
JwtDecoder mockJwtDecoder() {
    return new MockJwtDecoder();
}
```

---

## 🚀 **QUICK FIX (Đơn giản nhất):**

### **1. Tạo application-test.yml:**
```yaml
# File: src/main/resources/application-test.yml
spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: "https://mock-issuer.com" # Fake issuer
```

### **2. Chạy với profile test:**
```bash
.\mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=test
```

### **3. Test API không cần JWT:**
```bash
# Test health check
GET http://localhost:8088/health

# Test company API (không cần auth)
POST http://localhost:8088/api/companies
Body: {"name": "Test Company"}
```

---

## 🎯 **RECOMMENDED APPROACH:**

### **Step 1: Kiểm tra Network**
```bash
# Test kết nối internet
ping google.com
ping cognito-idp.ap-southeast-2.amazonaws.com
```

### **Step 2: Nếu không kết nối được Cognito**
```bash
# Chạy với profile test
.\mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=test
```

### **Step 3: Test API**
```bash
# Test các API không cần authentication
GET /health
POST /api/companies (nếu đã disable auth)
```

---

## 🔍 **DEBUG STEPS:**

### **1. Kiểm tra Internet:**
```bash
ping cognito-idp.ap-southeast-2.amazonaws.com
curl -I https://cognito-idp.ap-southeast-2.amazonaws.com/ap-southeast-2_TespiMS7C/.well-known/jwks.json
```

### **2. Kiểm tra Proxy/Firewall:**
- Corporate network có block AWS không?
- Cần VPN không?
- Proxy settings có đúng không?

### **3. Test với Mock:**
- Sử dụng profile test
- Disable JWT verification
- Test API functionality

---

## 💡 **TIPS:**

### **🔧 Development Mode:**
- Sử dụng profile `test` để bypass Cognito
- Mock JWT tokens cho testing
- Disable authentication cho development

### **🌐 Production Mode:**
- Đảm bảo network connectivity
- Configure proper proxy/firewall
- Use real Cognito tokens

---

**🎯 Hãy thử chạy với profile test trước để bypass Cognito và test API functionality!**