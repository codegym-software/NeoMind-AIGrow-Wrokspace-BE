# Hướng dẫn cấu hình biến môi trường

## 1. Cấu hình AWS Cognito

### Bước 1: Tạo AWS Cognito User Pool
1. Đăng nhập vào [AWS Console](https://aws.amazon.com/console/)
2. Tìm kiếm "Cognito" và chọn "Cognito User Pools"
3. Click "Create user pool"
4. Chọn "Step through settings"
5. Điền thông tin:
   - **Pool name**: `neomind-user-pool`
   - **How do you want your end users to sign in?**: Email
6. Click "Next" và tiếp tục với các cài đặt mặc định
7. Tạo User Pool

### Bước 2: Lấy thông tin từ User Pool
1. Sau khi tạo xong, click vào User Pool vừa tạo
2. Trong tab "General settings", copy **Pool ID**
3. **Issuer URI** sẽ có dạng: `https://cognito-idp.{region}.amazonaws.com/{pool-id}`

### Bước 3: Tạo App Client
1. Trong User Pool, click tab "App clients"
2. Click "Create app client"
3. Điền thông tin:
   - **App client name**: `neomind-backend-client`
   - **Client type**: Public client (không có client secret)
4. Click "Create app client"
5. Copy **Client ID**

## 2. Cấu hình biến môi trường

### Cách 1: Sử dụng file .env (Khuyến nghị)

1. Copy file `env.example` thành `.env`:
   ```bash
   cp env.example .env
   ```

2. Mở file `.env` và điền thông tin thực tế:
   ```env
   # AWS Cognito Configuration
   ISSUER_URI=https://cognito-idp.ap-southeast-2.amazonaws.com/ap-southeast-2_1234567890
   CLIENT_ID=1234567890abcdef1234567890
   AWS_REGION=ap-southeast-2
   
   # Application Configuration
   PORT=8088
   FRONTEND_ORIGIN=http://localhost:3000
   MAX_UPLOAD_SIZE=10MB
   PUBLIC_FILES_BASE_URL=http://localhost:8088/files
   FILE_STORAGE_BASE=./uploads
   ```

### Cách 2: Sử dụng biến môi trường hệ thống

#### Windows (PowerShell):
```powershell
$env:ISSUER_URI="https://cognito-idp.ap-southeast-2.amazonaws.com/ap-southeast-2_1234567890"
$env:CLIENT_ID="1234567890abcdef1234567890"
$env:AWS_REGION="ap-southeast-2"
$env:PORT="8088"
$env:FRONTEND_ORIGIN="http://localhost:3000"
```

#### Windows (Command Prompt):
```cmd
set ISSUER_URI=https://cognito-idp.ap-southeast-2.amazonaws.com/ap-southeast-2_1234567890
set CLIENT_ID=1234567890abcdef1234567890
set AWS_REGION=ap-southeast-2
set PORT=8088
set FRONTEND_ORIGIN=http://localhost:3000
```

#### Linux/Mac:
```bash
export ISSUER_URI="https://cognito-idp.ap-southeast-2.amazonaws.com/ap-southeast-2_1234567890"
export CLIENT_ID="1234567890abcdef1234567890"
export AWS_REGION="ap-southeast-2"
export PORT="8088"
export FRONTEND_ORIGIN="http://localhost:3000"
```

### Cách 3: Sử dụng Docker Compose

1. Tạo file `.env` trong thư mục gốc:
   ```env
   ISSUER_URI=https://cognito-idp.ap-southeast-2.amazonaws.com/ap-southeast-2_1234567890
   CLIENT_ID=1234567890abcdef1234567890
   AWS_REGION=ap-southeast-2
   PORT=8088
   FRONTEND_ORIGIN=http://localhost:3000
   ```

2. Chạy với Docker Compose:
   ```bash
   docker-compose up
   ```

## 3. Kiểm tra cấu hình

### Kiểm tra biến môi trường:
```bash
# Kiểm tra endpoint cấu hình
curl http://localhost:8088/api/test/config-info
```

### Kiểm tra authentication:
1. Lấy JWT token từ AWS Cognito
2. Test với endpoint protected:
   ```bash
   curl -H "Authorization: Bearer YOUR_JWT_TOKEN" \
        http://localhost:8088/api/test/protected
   ```

## 4. Troubleshooting

### Lỗi thường gặp:

1. **401 Unauthorized**:
   - Kiểm tra `ISSUER_URI` có đúng không
   - Kiểm tra JWT token có hợp lệ không
   - Kiểm tra token có expired không

2. **CORS Error**:
   - Kiểm tra `FRONTEND_ORIGIN` có đúng không
   - Đảm bảo frontend chạy trên port đúng

3. **Database Connection Error**:
   - Kiểm tra MySQL có chạy không
   - Kiểm tra thông tin database trong `application.properties`

### Logs để debug:
```bash
# Xem logs của ứng dụng
tail -f logs/application.log

# Hoặc nếu chạy với Maven
mvn spring-boot:run
```

## 5. Cấu hình Frontend

Frontend cần được cấu hình để:
1. Authenticate với AWS Cognito
2. Lấy JWT token từ Cognito
3. Gửi token trong header `Authorization: Bearer <token>`

### Ví dụ cấu hình frontend:
```javascript
// Cấu hình Cognito
const cognitoConfig = {
  region: 'ap-southeast-2',
  userPoolId: 'ap-southeast-2_1234567890',
  userPoolWebClientId: '1234567890abcdef1234567890'
};

// Authenticate và lấy token
const token = await cognitoUser.getSession().getIdToken().getJwtToken();

// Gửi request với token
fetch('/api/companies', {
  headers: {
    'Authorization': `Bearer ${token}`,
    'Content-Type': 'application/json'
  }
});
```
