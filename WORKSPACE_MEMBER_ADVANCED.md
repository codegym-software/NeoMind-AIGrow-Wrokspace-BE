# Workspace Member Management - Advanced Features

## Tổng quan
Tài liệu này mô tả các chức năng nâng cao cho quản lý thành viên workspace, bao gồm:
- Mời thành viên bằng email
- Transfer ownership
- Phân quyền chi tiết
- Xử lý fallback khi email không có trong JWT

## API Endpoints

### 1. Mời thành viên bằng email
```
POST /api/workspaces/{workspaceId}/members/invite-by-email
```
**Parameters:**
- `inviterEmail`: Email của người mời
- `invitedEmail`: Email của người được mời
- `roleId`: Role ID (200=Admin, 202=Editor, 203=Viewer)

**Authorization:** `workspace:manage_members`

### 2. Transfer ownership
```
POST /api/workspaces/{workspaceId}/transfer-ownership
```
**Body:**
```json
{
    "toUserId": "user-id-to-transfer-to",
    "downgradeRoleId": 202
}
```

**Authorization:** `workspace:transfer_ownership`

**Logic:**
- Owner cũ: `is_owner = false`, `roleId = downgradeRoleId` (role từ database)
- Owner mới: `is_owner = true`, `roleId = null` (không cần role vì đã có toàn quyền)

**Note:** `downgradeRoleId` phải là ID của một workspace role có sẵn trong database, không phải hardcode.

### 2.1. Lấy danh sách workspace roles
```
GET /api/workspaces/roles
```
**Response:**
```json
[
    {
        "id": 200,
        "scope": "workspace",
        "code": "admin",
        "name": "Workspace Admin",
        "description": "Full access to workspace"
    },
    {
        "id": 201,
        "scope": "workspace", 
        "code": "member",
        "name": "Workspace Member",
        "description": "Basic workspace access"
    }
]
```

### 3. Cập nhật role với authorization
```
PUT /api/workspaces/{workspaceId}/members/{userId}/role
```
**Body:** `roleId` (number)

**Authorization:** `workspace:set_user_permissions`

### 4. Đọc danh sách thành viên
```
GET /api/workspaces/{workspaceId}/members
```
**Authorization:** `workspace:member:read`

## Authorization Rules

### workspace:member:read
- ✅ Owner workspace
- ✅ Thành viên workspace
- ✅ User có permission `workspace:member:read`

### workspace:manage_members
- ✅ Owner workspace
- ✅ User có permission `workspace:manage_members`

### workspace:set_user_permissions
- ✅ Owner workspace
- ✅ User có permission `workspace:set_user_permissions`

### workspace:transfer_ownership
- ✅ Owner workspace
- ✅ User có permission `workspace:transfer_ownership`

## Business Logic

### Mời thành viên bằng email
1. Kiểm tra workspace tồn tại
2. Tìm user bằng email (với fallback từ Cognito)
3. Kiểm tra user là thành viên công ty
4. Kiểm tra user chưa có trong workspace
5. Tạo WorkspaceMember

### Transfer ownership
1. Kiểm tra actor là owner hiện tại
2. Kiểm tra target user là thành viên workspace
3. Kiểm tra target user là thành viên công ty
4. Cập nhật current owner thành member với role mới
5. Cập nhật target member thành owner với role Admin

### Cập nhật role
1. Kiểm tra actor có quyền (owner hoặc có permission)
2. Không cho phép thay đổi role của owner
3. Cập nhật role của member

## Error Handling

### Email Fallback
Khi email không có trong JWT:
1. Thử fetch từ Cognito UserInfo
2. Nếu vẫn không có, throw `IllegalStateException`

### Validation Errors
- `EntityNotFoundException`: Workspace/user không tồn tại
- `IllegalArgumentException`: Logic business không hợp lệ
- `AccessDeniedException`: Không có quyền truy cập

## Test Cases

### 1. Mời thành viên bằng email
```bash
# Success case
curl -X POST "/api/workspaces/ws-123/members/invite-by-email" \
  -d "inviterEmail=admin@company.com" \
  -d "invitedEmail=user@company.com" \
  -d "roleId=203"

# Error: User không phải thành viên công ty
# Error: User đã có trong workspace
# Error: Email không tồn tại
```

### 2. Transfer ownership
```bash
# Success case
curl -X POST "/api/workspaces/ws-123/transfer-ownership" \
  -H "Content-Type: application/json" \
  -d '{"toUserId": "user-456", "downgradeRoleId": 202}'

# Error: Không phải owner
# Error: Target user không phải thành viên workspace
```

### 3. Cập nhật role
```bash
# Success case
curl -X PUT "/api/workspaces/ws-123/members/user-456/role" \
  -H "Content-Type: application/json" \
  -d "202"

# Error: Không có quyền
# Error: Thay đổi role của owner
```

## Security Considerations

1. **Email Validation**: Luôn validate email format
2. **Company Membership**: Chỉ thành viên công ty mới được mời
3. **Ownership Transfer**: Chỉ owner mới có thể transfer
4. **Role Hierarchy**: Admin > Editor > Viewer
5. **JWT Fallback**: Secure fallback mechanism cho email resolution