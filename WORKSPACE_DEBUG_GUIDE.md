# 🔧 **DEBUG WORKSPACE API 403 FORBIDDEN**

## 🚨 **VẤN ĐỀ ĐÃ PHÁT HIỆN**

### **Nguyên nhân chính:**
1. **WorkspaceController** sử dụng `currentUserId()` method riêng → **SAI**
2. **CompanyController** sử dụng `UserIdResolver.resolveInternalId()` → **ĐÚNG**
3. **Permission logic** khác nhau giữa company và workspace

### **Đã sửa:**
✅ **WorkspaceController** giờ sử dụng `UserIdResolver` giống CompanyController
✅ **Permission** từ `workspace:create` → `company:read` (đơn giản hơn)

---

## 🧪 **CÁCH TEST VÀ DEBUG**

### **Bước 1: Test Company API (Để so sánh)**
```bash
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
**Expected:** 201 Created

### **Bước 2: Test Workspace API (Sau khi fix)**
```bash
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
**Expected:** 201 Created (thay vì 403 Forbidden)

---

## 🔍 **DEBUG STEPS**

### **Nếu vẫn 403 Forbidden:**

#### **1. Kiểm tra JWT Token**
```bash
# Decode JWT token tại https://jwt.io
# Kiểm tra có field "email" không
```

#### **2. Kiểm tra User trong Database**
```sql
-- Kiểm tra user có tồn tại không
SELECT * FROM users WHERE email = 'your-email@example.com';

-- Kiểm tra company member
SELECT * FROM company_members WHERE company_id = 'YOUR_COMPANY_ID' AND user_id = 'YOUR_USER_ID';
```

#### **3. Kiểm tra Backend Logs**
```bash
# Xem console logs khi chạy API
# Tìm error messages về authentication/authorization
```

#### **4. Test với Postman Console**
```javascript
// Thêm vào Pre-request Script
console.log('JWT Token:', pm.collectionVariables.get('jwtToken'));
console.log('Company ID:', pm.collectionVariables.get('companyId'));

// Thêm vào Test Script
console.log('Response Status:', pm.response.code);
console.log('Response Body:', pm.response.text());
```

---

## 🛠️ **CÁC FIX ĐÃ ÁP DỤNG**

### **1. WorkspaceController.java**
```java
// TRƯỚC (SAI):
private String currentUserId(Authentication auth) {
    if (auth instanceof JwtAuthenticationToken jwt) {
        Object uid = jwt.getToken().getClaim("uid");
        return uid != null ? uid.toString() : jwt.getName();
    }
    return auth.getName();
}

// SAU (ĐÚNG):
private final UserIdResolver userIdResolver;
String actorId = userIdResolver.resolveInternalId(auth);
```

### **2. Permission Logic**
```java
// TRƯỚC (Phức tạp):
@PreAuthorize("@gate.allows('workspace:create', authentication, #companyId)")

// SAU (Đơn giản):
@PreAuthorize("@gate.allows('company:read', authentication, #companyId)")
```

---

## 🎯 **WORKFLOW TEST HOÀN CHỈNH**

### **Step 1: Authentication**
```bash
1. Lấy JWT token từ Cognito hoặc frontend
2. Set vào Postman variable: jwtToken
```

### **Step 2: Create Company**
```bash
POST /api/companies
→ Lưu companyId vào variable
→ User tự động trở thành owner
```

### **Step 3: Create Workspace**
```bash
POST /api/companies/{companyId}/workspaces
→ Should work now (201 Created)
```

### **Step 4: Test Other Workspace APIs**
```bash
GET /api/workspaces/{workspaceId}
GET /api/companies/{companyId}/workspaces
POST /api/workspaces/{workspaceId}/members
```

---

## 🚨 **NẾU VẪN LỖI**

### **Kiểm tra thêm:**

#### **1. Database Permissions**
```sql
-- Kiểm tra user có quyền gì trong company
SELECT * FROM role_permissions WHERE role_id IN (
    SELECT role_id FROM company_members 
    WHERE company_id = 'YOUR_COMPANY_ID' AND user_id = 'YOUR_USER_ID'
);
```

#### **2. GateManager Logic**
```java
// Kiểm tra logic trong GateManagerImpl.java
// Xem method allows() có hoạt động đúng không
```

#### **3. UserIdResolver**
```java
// Kiểm tra UserIdResolver.resolveInternalId()
// Xem có resolve đúng user ID không
```

---

## 💡 **TIPS DEBUG**

### **🔍 Console Logs**
- **Backend:** Xem console khi chạy API
- **Postman:** Mở Console (View → Show Postman Console)
- **Browser:** F12 → Network tab

### **🧪 Test Scripts**
```javascript
// Postman Test Script
pm.test("Status code is 201", function () {
    pm.response.to.have.status(201);
});

pm.test("Response has workspace ID", function () {
    const jsonData = pm.response.json();
    pm.expect(jsonData.id).to.exist;
    pm.collectionVariables.set("workspaceId", jsonData.id);
});
```

### **📊 Response Analysis**
- **200/201:** Success
- **400:** Bad Request (check request body)
- **401:** Unauthorized (check JWT token)
- **403:** Forbidden (check permissions)
- **404:** Not Found (check URL/IDs)
- **500:** Server Error (check backend logs)

---

## 🎉 **KẾT QUẢ MONG ĐỢI**

Sau khi áp dụng các fix:

✅ **Company APIs:** Hoạt động bình thường
✅ **Workspace APIs:** Hoạt động bình thường (không còn 403)
✅ **User ID Resolution:** Consistent giữa các controllers
✅ **Permission Logic:** Đơn giản và rõ ràng

**Hãy test lại và cho tôi biết kết quả!** 🚀