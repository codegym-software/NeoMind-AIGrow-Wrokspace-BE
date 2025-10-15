# Debug Workspace Creation - Test Cases

## Các nguyên nhân có thể gây lỗi 400 khi tạo workspace:

### 1. Validation Errors (Jakarta Validation)
- **companyId**: `@NotBlank` - không được để trống
- **name**: `@NotBlank` và `@Size(max = 255)` - không được để trống và tối đa 255 ký tự
- **description**: `@Size(max = 1000)` - tối đa 1000 ký tự (optional)

### 2. Permission Errors (GateManager)
- User không có quyền `workspace:create` trong company
- User không có quyền `company:update` trong company  
- User không phải là owner của company

### 3. Business Logic Errors (WorkspaceService)
- Company không tồn tại
- Workspace name đã tồn tại trong company
- User không tồn tại trong database
- Role ID 200 (Workspace Admin) không tồn tại

### 4. Authentication Errors (UserIdResolver)
- JWT token không có email
- User không tồn tại trong database với email từ token

## Test Cases để Debug:

### Test Case 1: Valid Request
```json
POST /api/workspaces
{
  "companyId": "valid-company-id",
  "name": "Test Workspace",
  "description": "Test description"
}
```

### Test Case 2: Missing companyId
```json
POST /api/workspaces
{
  "name": "Test Workspace",
  "description": "Test description"
}
```

### Test Case 3: Missing name
```json
POST /api/workspaces
{
  "companyId": "valid-company-id",
  "description": "Test description"
}
```

### Test Case 4: Empty name
```json
POST /api/workspaces
{
  "companyId": "valid-company-id",
  "name": "",
  "description": "Test description"
}
```

### Test Case 5: Name too long (>255 chars)
```json
POST /api/workspaces
{
  "companyId": "valid-company-id",
  "name": "Very long name...",
  "description": "Test description"
}
```

### Test Case 6: Description too long (>1000 chars)
```json
POST /api/workspaces
{
  "companyId": "valid-company-id",
  "name": "Test Workspace",
  "description": "Very long description..."
}
```

## Cách Debug:

1. **Khởi động ứng dụng** với debug logging đã thêm
2. **Gửi request** với các test cases trên
3. **Kiểm tra console logs** để xem:
   - Request data được nhận
   - User ID được resolve
   - Permission check results
   - Service validation results
   - Error details

## Expected Debug Output:

```
=== WORKSPACE CONTROLLER DEBUG ===
Request received:
  - companyId: valid-company-id
  - name: Test Workspace
  - description: Test description

=== USER ID RESOLVER DEBUG ===
Extracted email: user@example.com
Resolved userId: user-uuid-123
=============================

=== GATE MANAGER DEBUG ===
Checking workspace:create permission
  - userId: user-uuid-123
  - companyId: valid-company-id
  - hasWorkspaceCreate: true/false
  - hasCompanyUpdate: true/false
  - isOwner: true/false
  - Final result: true/false
==========================

=== SERVICE DEBUG ===
Request companyId: valid-company-id
Request name: Test Workspace
Creator userId: user-uuid-123
...
```

## Common Issues:

1. **Permission Denied**: User không có quyền tạo workspace
2. **Company Not Found**: Company ID không tồn tại
3. **Duplicate Name**: Tên workspace đã tồn tại
4. **User Not Found**: Email trong JWT không có trong database
5. **Role Not Found**: Role ID 200 không tồn tại