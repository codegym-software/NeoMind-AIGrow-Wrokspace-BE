# 📋 ĐIỀU KIỆN DỮ LIỆU ĐỂ THÊM VÀO BẢNG WORKSPACE_MEMBERS

## 🗄️ **SCHEMA BẢNG WORKSPACE_MEMBERS**

```sql
CREATE TABLE workspace_members (
    id           CHAR(36)             NOT NULL PRIMARY KEY,
    workspace_id CHAR(36)             NOT NULL,
    user_id      CHAR(36)             NOT NULL,
    role_id      INT                  NULL,
    is_owner     TINYINT(1) DEFAULT 1 NULL,
    
    CONSTRAINT workspace_members_ibfk_1 
        FOREIGN KEY (workspace_id) REFERENCES workspaces (id),
    CONSTRAINT workspace_members_ibfk_2 
        FOREIGN KEY (user_id) REFERENCES users (id),
    
    INDEX idx_workspace_user (workspace_id, user_id),
    INDEX user_id (user_id)
);
```

---

## ✅ **ĐIỀU KIỆN BẮT BUỘC (REQUIRED)**

### **1. ID (Primary Key)**
- **Field**: `id`
- **Type**: `CHAR(36)` (UUID)
- **Required**: ✅ YES
- **Auto-generated**: ✅ YES (UUID.randomUUID())
- **Unique**: ✅ YES

### **2. Workspace ID**
- **Field**: `workspace_id`
- **Type**: `CHAR(36)` (UUID)
- **Required**: ✅ YES
- **Validation**: 
  - Must exist in `workspaces` table
  - Foreign key constraint
- **Error**: `"Workspace with ID {workspaceId} does not exist"`

### **3. User ID**
- **Field**: `user_id`
- **Type**: `CHAR(36)` (UUID)
- **Required**: ✅ YES
- **Validation**:
  - Must exist in `users` table
  - Foreign key constraint
- **Error**: `"User with ID {userId} does not exist"`

### **4. Is Owner**
- **Field**: `is_owner`
- **Type**: `TINYINT(1)` (Boolean)
- **Required**: ✅ YES
- **Default**: `false` (for new members)
- **Special Values**:
  - `true`: Workspace owner (role_id = null)
  - `false`: Regular member (role_id required)

---

## 🔄 **ĐIỀU KIỆN TÙY CHỌN (OPTIONAL)**

### **5. Role ID**
- **Field**: `role_id`
- **Type**: `INT`
- **Required**: ❌ NO (nullable = true)
- **Validation**:
  - If provided, must exist in `roles` table
  - If `is_owner = true`, then `role_id = null`
  - If `is_owner = false`, then `role_id` is required
- **Error**: `"Role with ID {roleId} does not exist"`

---

## 🚫 **ĐIỀU KIỆN RÀNG BUỘC (CONSTRAINTS)**

### **1. Unique Constraint**
- **Constraint**: `(workspace_id, user_id)`
- **Rule**: Một user chỉ có thể là member của một workspace một lần
- **Error**: `"User is already a member of this workspace"`

### **2. Owner Constraint**
- **Rule**: Mỗi workspace chỉ có thể có một owner
- **Logic**: 
  - Khi tạo workspace → creator becomes owner (is_owner=true, role_id=null)
  - Khi thêm member → is_owner=false, role_id required

### **3. Removal Constraint**
- **Rule**: Owner không thể bị xóa khỏi workspace
- **Error**: `"Cannot remove the workspace owner"`

---

## 📝 **CÁC TRƯỜNG HỢP THÊM DỮ LIỆU**

### **Case 1: Tạo Workspace (Auto-add Owner)**
```java
// Trong WorkspaceServiceImpl.create()
WorkspaceMember member = new WorkspaceMember(
    savedWorkspace.getId(),  // workspace_id
    actualUserId,            // user_id (creator)
    null,                    // role_id = null for owner
    true                     // is_owner = true
);
```

**Điều kiện**:
- ✅ Workspace phải tồn tại
- ✅ User (creator) phải tồn tại
- ✅ Không cần role_id (owner)
- ✅ is_owner = true

### **Case 2: Thêm Member (Manual Add)**
```java
// Trong WorkspaceMemberServiceImpl.addMember()
WorkspaceMember member = new WorkspaceMember();
member.setId(UUID.randomUUID().toString());
member.setWorkspaceId(workspaceId);
member.setUserId(request.userId());
member.setRoleId(request.roleId());  // Required for members
member.setOwner(false);              // Not owner
```

**Điều kiện**:
- ✅ Workspace phải tồn tại
- ✅ User phải tồn tại
- ✅ Role phải tồn tại
- ✅ User chưa là member của workspace
- ✅ is_owner = false

---

## 🔍 **VALIDATION FLOW**

### **Step 1: Pre-validation**
```java
// 1. Check workspace exists
if (!workspaceRepository.existsById(workspaceId)) {
    throw new IllegalArgumentException("Workspace with ID " + workspaceId + " does not exist");
}

// 2. Check user exists
User user = userRepository.findById(request.userId())
    .orElseThrow(() -> new IllegalArgumentException("User with ID " + request.userId() + " does not exist"));

// 3. Check role exists (if not owner)
Role role = roleRepository.findById(request.roleId())
    .orElseThrow(() -> new IllegalArgumentException("Role with ID " + request.roleId() + " does not exist"));

// 4. Check user not already member
if (workspaceMemberRepository.existsByWorkspaceIdAndUserId(workspaceId, request.userId())) {
    throw new IllegalArgumentException("User is already a member of this workspace");
}
```

### **Step 2: Data Creation**
```java
// 5. Create member record
WorkspaceMember member = new WorkspaceMember();
member.setId(UUID.randomUUID().toString());
member.setWorkspaceId(workspaceId);
member.setUserId(request.userId());
member.setRoleId(request.roleId());
member.setOwner(false);
```

### **Step 3: Database Save**
```java
// 6. Save to database
WorkspaceMember savedMember = workspaceMemberRepository.save(member);
```

---

## ⚠️ **CÁC LỖI CÓ THỂ XẢY RA**

### **Validation Errors (400 Bad Request)**
```json
{
  "timestamp": "2025-10-15T10:30:00.000Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Workspace with ID workspace-123 does not exist",
  "path": "/api/workspaces/workspace-123/members"
}
```

### **Business Logic Errors**
```json
{
  "timestamp": "2025-10-15T10:30:00.000Z",
  "status": 400,
  "error": "Bad Request", 
  "message": "User is already a member of this workspace",
  "path": "/api/workspaces/workspace-123/members"
}
```

### **Authorization Errors (403 Forbidden)**
```json
{
  "timestamp": "2025-10-15T10:30:00.000Z",
  "status": 403,
  "error": "Forbidden",
  "message": "Access Denied",
  "path": "/api/workspaces/workspace-123/members"
}
```

---

## 🎯 **API ENDPOINTS LIÊN QUAN**

### **Thêm Member**
```http
POST /api/workspaces/{workspaceId}/members
Authorization: Bearer {jwt_token}
Content-Type: application/json

{
  "userId": "user-uuid-here",
  "roleId": 201
}
```

### **Kiểm tra Member**
```http
GET /api/workspaces/{workspaceId}/members/{userId}/check
Authorization: Bearer {jwt_token}
```

### **Kiểm tra Owner**
```http
GET /api/workspaces/{workspaceId}/members/{userId}/owner
Authorization: Bearer {jwt_token}
```

---

## 📊 **TÓM TẮT ĐIỀU KIỆN**

| Field | Required | Type | Validation | Default |
|-------|----------|------|------------|---------|
| `id` | ✅ | CHAR(36) | Auto-generated UUID | - |
| `workspace_id` | ✅ | CHAR(36) | Must exist in workspaces | - |
| `user_id` | ✅ | CHAR(36) | Must exist in users | - |
| `role_id` | ❌ | INT | Must exist in roles (if not owner) | null |
| `is_owner` | ✅ | BOOLEAN | true/false | false |

**🔑 Key Points:**
- Owner: `is_owner=true`, `role_id=null`
- Member: `is_owner=false`, `role_id=required`
- Unique constraint: `(workspace_id, user_id)`
- Owner cannot be removed