# 🚀 **HƯỚNG DẪN TEST POSTMAN - NEO MIND BACKEND**

## 🔧 **KHẮC PHỤC LỖI "SOMETHING WENT WRONG"**

### **Bước 1: Import Collection Mới**
1. **Xóa collection cũ** (nếu có lỗi)
2. **Import file mới**: `neomind-backend-api-fixed.postman_collection.json`
3. **Đảm bảo JSON format đúng**

### **Bước 2: Cấu Hình Variables**
1. **Mở Collection Settings** → Variables
2. **Cập nhật các biến**:
   ```
   baseUrl = http://localhost:8088
   jwtToken = (để trống, sẽ tự động lưu)
   companyId = (để trống, sẽ tự động lưu)
   workspaceId = (để trống, sẽ tự động lưu)
   ```

### **Bước 3: Khởi Động Backend**
```bash
# Đảm bảo backend đang chạy
cd C:\Users\khanh\OneDrive\Desktop\NeoMind-AIGrow-Wrokspace-BE
.\mvnw.cmd spring-boot:run
```

---

## 🧪 **WORKFLOW TEST CƠ BẢN**

### **Step 1: Test Health Check**
```
GET /health
```
- ✅ **Không cần authentication**
- ✅ **Kiểm tra backend có chạy không**

### **Step 2: Test Session (Cần JWT Token)**
```
POST /api/session
Headers: Authorization: Bearer YOUR_JWT_TOKEN
```
- ⚠️ **Cần JWT token từ Cognito**
- ✅ **Kiểm tra authentication**

### **Step 3: Test Company APIs**
```
POST /api/companies
Headers: 
  - Content-Type: application/json
  - Authorization: Bearer YOUR_JWT_TOKEN
Body:
{
  "name": "Test Company",
  "logoUrl": "https://example.com/logo.png"
}
```

### **Step 4: Test Workspace APIs**
```
POST /api/companies/{companyId}/workspaces
Headers:
  - Content-Type: application/json
  - Authorization: Bearer YOUR_JWT_TOKEN
Body:
{
  "name": "Test Workspace",
  "description": "This is a test workspace"
}
```

---

## 🔐 **AUTHENTICATION SETUP**

### **Option 1: Sử dụng JWT Token có sẵn**
1. **Lấy JWT token** từ frontend hoặc Cognito
2. **Paste vào variable** `jwtToken`
3. **Test các API cần authentication**

### **Option 2: Tạo JWT Token Test**
```bash
# Tạo JWT token test (tạm thời)
# Sử dụng tool online: https://jwt.io
# Hoặc lấy từ browser DevTools
```

---

## 🚨 **CÁC LỖI THƯỜNG GẶP**

### **1. "Something Went Wrong"**
- ✅ **Import lại collection mới**
- ✅ **Kiểm tra JSON format**
- ✅ **Restart Postman**

### **2. "401 Unauthorized"**
- ✅ **Kiểm tra JWT token**
- ✅ **Đảm bảo token chưa expire**
- ✅ **Format: Bearer YOUR_TOKEN**

### **3. "500 Internal Server Error"**
- ✅ **Kiểm tra backend có chạy không**
- ✅ **Kiểm tra database connection**
- ✅ **Xem logs trong console**

### **4. "404 Not Found"**
- ✅ **Kiểm tra URL endpoint**
- ✅ **Đảm bảo backend đang chạy trên port 8088**
- ✅ **Kiểm tra baseUrl variable**

---

## 📋 **CHECKLIST TEST**

### **✅ Pre-Test**
- [ ] Backend đang chạy (port 8088)
- [ ] Database connected
- [ ] Collection imported thành công
- [ ] Variables configured

### **✅ Basic Tests**
- [ ] GET /health → 200 OK
- [ ] POST /api/session → 200 OK (với JWT)
- [ ] POST /api/companies → 201 Created
- [ ] GET /api/companies → 200 OK
- [ ] POST /api/companies/{id}/workspaces → 201 Created

### **✅ Advanced Tests**
- [ ] Workspace member management
- [ ] Project management
- [ ] Role & permission management
- [ ] File upload

---

## 🎯 **QUICK START**

### **1. Import Collection**
```
File → Import → neomind-backend-api-fixed.postman_collection.json
```

### **2. Set Variables**
```
baseUrl = http://localhost:8088
jwtToken = YOUR_JWT_TOKEN_HERE
```

### **3. Test Health**
```
GET /health → Should return 200 OK
```

### **4. Test Authentication**
```
POST /api/session → Should return user info
```

### **5. Test CRUD Operations**
```
POST /api/companies → Create company
GET /api/companies → List companies
POST /api/companies/{id}/workspaces → Create workspace
```

---

## 💡 **TIPS**

### **🔍 Debug Tips**
- **Sử dụng Console** để xem request/response
- **Kiểm tra Headers** trong request
- **Xem Response Body** để hiểu lỗi
- **Check Network tab** trong browser

### **⚡ Performance Tips**
- **Sử dụng Environment** cho different stages
- **Save responses** để test offline
- **Use Collection Runner** cho bulk tests
- **Monitor response times**

---

## 🆘 **TROUBLESHOOTING**

### **Nếu vẫn gặp lỗi:**
1. **Restart Postman**
2. **Clear cache**
3. **Update Postman**
4. **Check internet connection**
5. **Verify backend logs**

### **Contact Support:**
- **Backend logs**: Check console output
- **Postman logs**: View → Show Postman Console
- **Network issues**: Check firewall/proxy

---

**🎉 Collection mới này đã được fix và sẵn sàng test!**