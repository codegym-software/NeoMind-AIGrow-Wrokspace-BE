# 🚀 CHẠY NEOM-69 TRONG INTELLIJ

## CÁCH 1: RUN CONFIGURATION VỚI H2 DATABASE

### BƯỚC 1: TẠO RUN CONFIGURATION
1. **Run** → **Edit Configurations**
2. **Add New** → **Application**
3. **Name**: `NEOM-69 Test`
4. **Main class**: `com.springboot.demo.NeoMindBeApplication`
5. **VM options**: `-Dspring.profiles.active=test`
6. **Apply** → **OK**

### BƯỚC 2: CHẠY APPLICATION
1. **Chọn configuration**: `NEOM-69 Test`
2. **Click Run** (▶️)
3. **Đợi start**: Tìm dòng "Started NeoMindBeApplication"

### BƯỚC 3: TEST SWAGGER UI
1. **Mở**: http://localhost:8080/swagger-ui.html
2. **Tìm**: "Project Controller"
3. **Test APIs**

## CÁCH 2: NẾU VẪN LỖI - DÙNG MAVEN WRAPPER

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=test
```

## CÁCH 3: BUILD JAR VÀ CHẠY

```bash
./mvnw clean package -DskipTests
java -jar target/NeoMind_BE-0.0.1-SNAPSHOT.jar --spring.profiles.active=test
```

## ✅ EXPECTED RESULTS:
- Application starts on port 8080
- H2 database auto-creates tables
- Swagger UI available
- Project Management APIs ready to test
