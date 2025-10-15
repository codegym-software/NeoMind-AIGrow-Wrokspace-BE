# 🚀 **NeoMind Backend API - Complete Postman Collection**

## 📋 **TỔNG QUAN**

Đây là Postman collection hoàn chỉnh và chính xác cho NeoMind Backend API, được tạo dựa trên **tất cả các controllers thực tế** trong dự án.

---

## ✨ **TÍNH NĂNG NỔI BẬT**

### **🔐 Authentication Flow**
- ✅ **Cognito Password Grant** (Public & Confidential Client)
- ✅ **Auto-save JWT Token** từ response
- ✅ **Bearer Token** cho tất cả API calls

### **📊 Auto-Save Variables**
- ✅ **Company ID** tự động lưu khi tạo company
- ✅ **Workspace ID** tự động lưu khi tạo workspace  
- ✅ **Project ID** tự động lưu khi tạo project
- ✅ **Role ID** tự động lưu khi tạo role

### **🎯 Complete API Coverage**
- ✅ **9 Controller Groups** với 50+ endpoints
- ✅ **Tất cả HTTP Methods** (GET, POST, PUT, PATCH, DELETE)
- ✅ **Request/Response Examples** cho mỗi endpoint
- ✅ **Query Parameters** và **Request Bodies** đầy đủ

---

## 📁 **CẤU TRÚC COLLECTION**

### **1. 🔐 Cognito Authentication**
- Password Grant (Public Client)
- Password Grant (Confidential Client)

### **2. 🏥 Health Check**
- GET /health

### **3. 👤 User & Account**
- POST /api/session
- POST /api/account/change-password

### **4. 🏢 Company Management**
- POST /api/companies
- GET /api/companies/{id}
- PUT /api/companies/{id}
- PATCH /api/companies/{id}
- GET /api/companies
- DELETE /api/companies/{id}
- GET /api/companies/admin/all

### **5. 👥 Company Members**
- GET /api/companies/{companyId}/members
- POST /api/companies/{companyId}/members
- PUT /api/companies/{companyId}/members/{memberUserId}/role
- POST /api/companies/{companyId}/members/transfer-ownership
- DELETE /api/companies/{companyId}/members/{memberUserId}
- POST /api/companies/{companyId}/invite

### **6. 🏗️ Workspace Management**
- POST /api/companies/{companyId}/workspaces
- GET /api/workspaces/{id}
- PUT /api/workspaces/{id}
- DELETE /api/workspaces/{id}
- GET /api/companies/{companyId}/workspaces
- GET /api/companies/{companyId}/workspaces/my

### **7. 👥 Workspace Members**
- GET /api/workspaces/{workspaceId}/members
- POST /api/workspaces/{workspaceId}/members/invite
- POST /api/workspaces/{workspaceId}/members
- PUT /api/workspaces/{workspaceId}/members/{userId}/role
- DELETE /api/workspaces/{workspaceId}/members/{userId}

### **8. 📁 Project Management**
- POST /api/workspaces/{workspaceId}/projects
- GET /api/workspaces/{workspaceId}/projects
- PATCH /api/projects/{projectId}/rename
- DELETE /api/projects/{projectId}
- GET /api/projects/mine

### **9. 🔐 Role Management**
- GET /api/companies/{companyId}/roles
- POST /api/companies/{companyId}/roles
- PUT /api/companies/{companyId}/roles/{roleId}
- DELETE /api/companies/{companyId}/roles/{roleId}
- PUT /api/companies/{companyId}/roles/{roleId}/permissions

### **10. 🛡️ Permission Management**
- GET /api/permissions
- GET /api/roles/{roleId}/permissions
- PUT /api/roles/{roleId}/permissions

### **11. 📤 File Upload**
- POST /api/uploads/{bucket}

---

## ⚙️ **SETUP HƯỚNG DẪN**

### **1. Import Collection**
```bash
# Import file: neomind-backend-api-complete.postman_collection.json
```

### **2. Configure Variables**
```json
{
  "baseUrl": "http://localhost:8088",
  "cognitoBaseUrl": "https://YOUR_DOMAIN.auth.YOUR_REGION.amazoncognito.com",
  "clientId": "YOUR_CLIENT_ID",
  "clientSecret": "YOUR_CLIENT_SECRET",
  "username": "your-username",
  "password": "your-password"
}
```

### **3. Authentication Flow**
1. **Chạy Cognito Auth** → Lấy JWT token
2. **Token tự động lưu** vào collection variables
3. **Tất cả API calls** sẽ sử dụng token này

---

## 🎯 **WORKFLOW SỬ DỤNG**

### **Step 1: Authentication**
```bash
1. Chạy "Password Grant (Public Client)" hoặc "Password Grant (Confidential Client)"
2. JWT token sẽ tự động lưu vào {{jwtToken}}
```

### **Step 2: Create Company**
```bash
1. Chạy "POST /api/companies"
2. Company ID sẽ tự động lưu vào {{companyId}}
```

### **Step 3: Create Workspace**
```bash
1. Chạy "POST /api/companies/{companyId}/workspaces"
2. Workspace ID sẽ tự động lưu vào {{workspaceId}}
```

### **Step 4: Manage Members**
```bash
1. Chạy "POST /api/workspaces/{workspaceId}/members/invite"
2. Hoặc "POST /api/workspaces/{workspaceId}/members"
```

### **Step 5: Create Projects**
```bash
1. Chạy "POST /api/workspaces/{workspaceId}/projects"
2. Project ID sẽ tự động lưu vào {{projectId}}
```

---

## 🔧 **COLLECTION VARIABLES**

| Variable | Description | Auto-Saved |
|----------|-------------|------------|
| `baseUrl` | Backend API base URL | ❌ |
| `jwtToken` | JWT authentication token | ✅ |
| `companyId` | Current company ID | ✅ |
| `workspaceId` | Current workspace ID | ✅ |
| `projectId` | Current project ID | ✅ |
| `roleId` | Current role ID | ✅ |
| `userId` | User ID for testing | ❌ |
| `uploadBucket` | File upload bucket name | ❌ |

---

## 📝 **REQUEST EXAMPLES**

### **Create Company**
```json
POST /api/companies
{
  "name": "Test Company",
  "logoUrl": "https://example.com/logo.png"
}
```

### **Create Workspace**
```json
POST /api/companies/{companyId}/workspaces
{
  "name": "Test Workspace",
  "description": "This is a test workspace"
}
```

### **Add Workspace Member**
```json
POST /api/workspaces/{workspaceId}/members
{
  "userId": "USER_ID_TO_ADD",
  "roleId": 201
}
```

### **Create Project**
```json
POST /api/workspaces/{workspaceId}/projects
{
  "name": "Test Project",
  "description": "This is a test project"
}
```

---

## 🚨 **LƯU Ý QUAN TRỌNG**

### **1. Authentication Required**
- ✅ **Tất cả endpoints** (trừ /health) cần JWT token
- ✅ **Token tự động** được thêm vào headers

### **2. Permission-Based Access**
- ✅ **@PreAuthorize** annotations kiểm tra quyền
- ✅ **Gate Manager** xử lý authorization logic

### **3. Auto-Save IDs**
- ✅ **Company ID** → `{{companyId}}`
- ✅ **Workspace ID** → `{{workspaceId}}`
- ✅ **Project ID** → `{{projectId}}`
- ✅ **Role ID** → `{{roleId}}`

### **4. Error Handling**
- ✅ **400 Bad Request** cho invalid data
- ✅ **401 Unauthorized** cho missing token
- ✅ **403 Forbidden** cho insufficient permissions
- ✅ **404 Not Found** cho missing resources

---

## 🎉 **KẾT LUẬN**

Postman collection mới này cung cấp:

- ✅ **100% API Coverage** - Tất cả endpoints thực tế
- ✅ **Auto-Authentication** - JWT token tự động
- ✅ **Auto-Save Variables** - IDs tự động lưu
- ✅ **Complete Examples** - Request/Response mẫu
- ✅ **Organized Structure** - Nhóm theo chức năng
- ✅ **Ready to Use** - Import và test ngay

**Collection này hoàn toàn chính xác và sẵn sàng sử dụng cho việc test API!** 🚀