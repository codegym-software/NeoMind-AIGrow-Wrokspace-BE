# 🚀 **WORKSPACE CONTROLLER - ACTOR ID INTEGRATION**

## 📋 **TÓM TẮT CÁC THAY ĐỔI**

Đã cập nhật WorkspaceController để sử dụng pattern `actorId` giống như CompanyController, cho phép lấy ID của người dùng từ JWT token và sử dụng trong các operations.

---

## 🔧 **CÁC FILE ĐÃ ĐƯỢC CẬP NHẬT**

### **1. WorkspaceController.java**
- ✅ Thêm `Authentication auth` parameter vào tất cả endpoints
- ✅ Sử dụng `userIdResolver.resolveInternalId(auth)` để lấy `actorId`
- ✅ Thêm `@PreAuthorize` annotations cho security
- ✅ Thêm endpoint `/my` để lấy workspaces của current user

### **2. WorkspaceService.java**
- ✅ Thêm method `searchByCurrentUser()` với pagination support
- ✅ Import `Page` và `Pageable` từ Spring Data

### **3. WorkspaceServiceImpl.java**
- ✅ Implement method `searchByCurrentUser()`
- ✅ Thêm imports cho pagination
- ✅ Logic tìm workspaces theo user membership

### **4. WorkspaceRepository.java**
- ✅ Thêm method `findByUserId()` với pagination
- ✅ Thêm method `findByUserIdAndNameContainingIgnoreCase()` với search
- ✅ Sử dụng native queries để join với workspace_members table

---

## 🎯 **CÁC ENDPOINTS ĐÃ ĐƯỢC CẬP NHẬT**

### **Core Workspace Endpoints**
```http
POST /api/workspaces
GET /api/workspaces/{id}
PUT /api/workspaces/{id}
DELETE /api/workspaces/{id}
PATCH /api/workspaces/{id}/restore
```

### **Company-specific Endpoints**
```http
GET /api/workspaces/company/{companyId}
GET /api/workspaces/company/{companyId}/all
GET /api/workspaces/company/{companyId}/count
POST /api/workspaces/companies/{companyId}
GET /api/workspaces/companies/{companyId}
PUT /api/workspaces/companies/{companyId}/{id}
DELETE /api/workspaces/companies/{companyId}/{id}
```

### **User-specific Endpoints**
```http
GET /api/workspaces/my?q=search&page=0&size=10
GET /api/workspaces/user/{userId}
```

### **Member Management Endpoints**
```http
POST /api/workspaces/{workspaceId}/members
GET /api/workspaces/{workspaceId}/members
GET /api/workspaces/{workspaceId}/members/{userId}
PUT /api/workspaces/{workspaceId}/members/{userId}/role
DELETE /api/workspaces/{workspaceId}/members/{userId}
GET /api/workspaces/{workspaceId}/members/{userId}/check
GET /api/workspaces/{workspaceId}/members/{userId}/owner
```

---

## 🔐 **SECURITY PATTERN**

### **Authentication Flow**
```java
// Pattern được sử dụng trong tất cả endpoints
@PreAuthorize("@gate.allows('permission', authentication, #targetId)")
public ResponseType methodName(@PathVariable String targetId, Authentication auth) {
    String actorId = userIdResolver.resolveInternalId(auth);
    // Use actorId for business logic
    return service.method(targetId, actorId);
}
```

### **Permission Checks**
- **Workspace Operations**: `@gate.allows('workspace:create|read|update|delete', authentication, #workspaceId)`
- **Company Operations**: `@gate.allows('company:read', authentication, #companyId)`
- **Member Management**: `@gate.allows('workspace:manage_members', authentication, #workspaceId)`
- **General Access**: `@PreAuthorize("isAuthenticated()")`

---

## 📊 **PAGINATION SUPPORT**

### **New Endpoint: GET /api/workspaces/my**
```http
GET /api/workspaces/my?q=search&page=0&size=10
Authorization: Bearer {jwt_token}
```

**Response:**
```json
{
  "content": [
    {
      "id": "workspace-uuid",
      "companyId": "company-uuid",
      "name": "My Workspace",
      "description": "Description",
      "createdAt": "2025-10-15T10:00:00Z",
      "updatedAt": "2025-10-15T10:00:00Z",
      "archivedAt": null
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 10
  },
  "totalElements": 25,
  "totalPages": 3,
  "first": true,
  "last": false
}
```

---

## 🔍 **DATABASE QUERIES**

### **User Workspaces Query**
```sql
SELECT w.*
FROM workspaces w
JOIN workspace_members wm ON wm.workspace_id = w.id
WHERE wm.user_id = :userId
  AND w.archived_at IS NULL
ORDER BY w.created_at DESC
```

### **Search User Workspaces Query**
```sql
SELECT w.*
FROM workspaces w
JOIN workspace_members wm ON wm.workspace_id = w.id
WHERE wm.user_id = :userId
  AND LOWER(w.name) LIKE LOWER(CONCAT('%', :query, '%'))
  AND w.archived_at IS NULL
ORDER BY w.created_at DESC
```

---

## 🎉 **BENEFITS**

### **1. Consistent Pattern**
- Tất cả endpoints đều sử dụng `actorId` pattern
- Giống với CompanyController để maintain consistency

### **2. Security Enhancement**
- Mọi operation đều có authentication check
- Permission-based access control với `@gate.allows`

### **3. User Context**
- Có thể track user actions
- Audit trail với actorId
- Personal workspace management

### **4. Pagination Support**
- Efficient data loading
- Better performance với large datasets
- Standard Spring Data pagination

### **5. Flexible Search**
- Search workspaces by name
- Filter by user membership
- Support for complex queries

---

## 🚀 **USAGE EXAMPLES**

### **Create Workspace**
```javascript
const createWorkspace = async (companyId, name, description) => {
  const response = await fetch('/api/workspaces', {
    method: 'POST',
    headers: {
      'Authorization': `Bearer ${jwtToken}`,
      'Content-Type': 'application/json'
    },
    body: JSON.stringify({
      companyId,
      name,
      description
    })
  });
  
  return response.json();
};
```

### **Get My Workspaces**
```javascript
const getMyWorkspaces = async (query = '', page = 0, size = 10) => {
  const response = await fetch(`/api/workspaces/my?q=${query}&page=${page}&size=${size}`, {
    headers: {
      'Authorization': `Bearer ${jwtToken}`
    }
  });
  
  return response.json();
};
```

### **Add Member to Workspace**
```javascript
const addMember = async (workspaceId, userId, roleId) => {
  const response = await fetch(`/api/workspaces/${workspaceId}/members`, {
    method: 'POST',
    headers: {
      'Authorization': `Bearer ${jwtToken}`,
      'Content-Type': 'application/json'
    },
    body: JSON.stringify({
      userId,
      roleId
    })
  });
  
  return response.json();
};
```

---

## ✅ **TESTING CHECKLIST**

- [ ] Compilation successful ✅
- [ ] All endpoints have Authentication parameter
- [ ] All endpoints use actorId pattern
- [ ] Security annotations properly configured
- [ ] Pagination works correctly
- [ ] Search functionality works
- [ ] Owner logic preserved
- [ ] Member management works
- [ ] Permission checks enforced

---

## 🎯 **NEXT STEPS**

1. **Test API Endpoints**: Verify all endpoints work with JWT tokens
2. **Frontend Integration**: Update frontend to use new endpoints
3. **Permission Testing**: Test different user roles and permissions
4. **Performance Testing**: Test pagination with large datasets
5. **Documentation**: Update API documentation with new endpoints

**Tất cả thay đổi đã được implement thành công và sẵn sàng để test!** 🚀