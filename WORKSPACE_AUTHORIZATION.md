# Workspace Authorization Rules

## Tạo Workspace (`POST /api/companies/{companyId}/workspaces`)

### ✅ **Được phép tạo workspace khi:**

1. **Company Owner** - Người sở hữu công ty
   ```java
   companyMemberRepo.isOwner(companyId, userId) == true
   ```

2. **Có quyền `workspace:create`** - Người có permission trong company scope
   ```java
   abilityService.hasPermission(userId, Scope.company, companyId, "workspace:create")
   ```

### ❌ **Không được phép tạo workspace khi:**

1. **Không phải owner** và **không có permission**
2. **Không thuộc company** 
3. **Token không hợp lệ**

## Logic Authorization trong GateManagerImpl

```java
dynamicGates.put("workspace:create", (ctx, target) -> {
    String companyId = (String) target;
    // Company owner can always create workspace
    if (companyMemberRepo.isOwner(companyId, ctx.userId())) return true;
    // Or user has workspace:create permission in company scope
    return abilityService.hasPermission(ctx.userId(), Scope.company, companyId, "workspace:create");
});
```

## Test Cases

### Test Case 1: Company Owner
```bash
# User là owner của company
curl -X POST "http://localhost:8080/api/companies/{companyId}/workspaces" \
  -H "Authorization: Bearer OWNER_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"name": "Test Workspace", "description": "Test"}'
# Expected: 200 OK
```

### Test Case 2: User có permission workspace:create
```bash
# User có permission workspace:create trong company scope
curl -X POST "http://localhost:8080/api/companies/{companyId}/workspaces" \
  -H "Authorization: Bearer PERMITTED_USER_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"name": "Test Workspace", "description": "Test"}'
# Expected: 200 OK
```

### Test Case 3: User không có quyền
```bash
# User không phải owner và không có permission
curl -X POST "http://localhost:8080/api/companies/{companyId}/workspaces" \
  -H "Authorization: Bearer REGULAR_USER_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"name": "Test Workspace", "description": "Test"}'
# Expected: 403 Forbidden
```

## Các Endpoint khác

### Xem danh sách workspace (`GET /api/companies/{companyId}/workspaces`)
- **Permission:** `company:read` (owner hoặc có quyền đọc company)

### Xem workspace chi tiết (`GET /api/workspaces/{id}`)
- **Permission:** `workspace:read` (owner workspace hoặc có quyền đọc)

### Cập nhật workspace (`PUT /api/workspaces/{id}`)
- **Permission:** `workspace:update` (owner workspace hoặc có quyền cập nhật)

### Xóa workspace (`DELETE /api/workspaces/{id}`)
- **Permission:** `workspace:delete` (owner workspace hoặc có quyền xóa)

## Database Schema

### CompanyMember Table
```sql
CREATE TABLE company_members (
    id INT PRIMARY KEY AUTO_INCREMENT,
    company_id VARCHAR(36) NOT NULL,
    user_id VARCHAR(36) NOT NULL,
    is_owner BOOLEAN DEFAULT FALSE,
    -- other fields
);
```

### Workspace Table
```sql
CREATE TABLE workspaces (
    id VARCHAR(36) PRIMARY KEY,
    company_id VARCHAR(36) NOT NULL,
    created_by VARCHAR(36) NOT NULL,  -- User tạo workspace
    name VARCHAR(255) NOT NULL,
    description TEXT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    archived_at TIMESTAMP
);
```

### WorkspaceMember Table
```sql
CREATE TABLE workspace_members (
    id INT PRIMARY KEY AUTO_INCREMENT,
    workspace_id VARCHAR(36) NOT NULL,
    user_id VARCHAR(36) NOT NULL,
    role_id INT NOT NULL,
    is_owner BOOLEAN DEFAULT FALSE,
    invited_by VARCHAR(36),
    joined_at TIMESTAMP
);
```