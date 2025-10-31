# Workspace Member API Setup

## Tổng quan
Đã thêm đầy đủ API để quản lý thành viên workspace với đầy đủ chức năng: thêm, xóa, cập nhật role, và chuyển quyền sở hữu.

## Cài đặt

### 1. Chạy SQL để thêm permissions vào database

Chạy file `seed-workspace-member-permissions.sql` trong MySQL:

```bash
mysql -u YOUR_USERNAME -p ai-grow < seed-workspace-member-permissions.sql
```

Hoặc copy-paste SQL vào MySQL Workbench:
```sql
-- Workspace Member Management Permissions
INSERT INTO permissions (scope, code, name) VALUES 
('workspace', 'workspace:member:read', 'Đọc danh sách thành viên workspace'),
('workspace', 'workspace:member:create', 'Thêm thành viên vào workspace'),
('workspace', 'workspace:member:update', 'Cập nhật thông tin thành viên workspace'),
('workspace', 'workspace:member:delete', 'Xóa thành viên khỏi workspace'),
('workspace', 'workspace:member:transfer_ownership', 'Chuyển quyền sở hữu workspace');
```

### 2. Các file đã tạo/thay đổi

#### DTOs (dto/workspace/)
- `AddWorkspaceMemberReq.java` - Request để thêm member
- `AssignWorkspaceRoleReq.java` - Request để cập nhật role
- `TransferWorkspaceOwnershipReq.java` - Request để chuyển ownership

#### Service
- `WorkspaceMemberService.java` - Interface
- `WorkspaceMemberServiceImpl.java` - Implementation với logic:
  - Chỉ thêm user đã là member của company
  - Validate role thuộc scope workspace
  - Prevent xóa owner duy nhất
  - Transfer ownership với downgrade role

#### Controller
- `WorkspaceMemberController.java` - REST endpoints

#### Repository
- Đã cập nhật `WorkspaceMemberRepository.java` - Thêm method `countOwners()`

#### Security
- Đã cập nhật `GateManagerImpl.java` - Thêm 5 dynamic gates:
  - `workspace:member:create`
  - `workspace:member:read`
  - `workspace:member:update`
  - `workspace:member:delete`
  - `workspace:member:transfer_ownership`

## API Endpoints

### 1. Liệt kê members
```
GET /api/workspaces/{workspaceId}/members
```
**Quyền**: `workspace:member:read`

### 2. Thêm member vào workspace
```
POST /api/workspaces/{workspaceId}/members
```
**Quyền**: `workspace:member:create` (chỉ Owner)
**Body**:
```json
{
  "userId": "user-id-here",
  "roleId": 123
}
```
**Lưu ý**: Chỉ thêm được user đã là member của company

### 3. Cập nhật role
```
PUT /api/workspaces/{workspaceId}/members/{targetUserId}/role
```
**Quyền**: `workspace:member:update` (chỉ Owner)
**Body**:
```json
{
  "roleId": 123
}
```

### 4. Xóa member
```
DELETE /api/workspaces/{workspaceId}/members/{targetUserId}
```
**Quyền**: `workspace:member:delete` (chỉ Owner)
**Lưu ý**: Không thể xóa owner duy nhất

### 5. Chuyển quyền sở hữu
```
POST /api/workspaces/{workspaceId}/members/transfer-ownership
```
**Quyền**: `workspace:member:transfer_ownership` (chỉ Owner)
**Body**:
```json
{
  "toUserId": "new-owner-id",
  "downgradeRoleId": 123
}
```

## Phân quyền & Logic

### Owner
- Có full quyền trên tất cả member operations
- Có thể transfer ownership
- Không thể xóa chính mình nếu là owner duy nhất

### Members (có permission trong DB)
- Có thể được Owner assign/revoke các permissions
- Quyền được quản lý qua RolePermissions trong DB

### Logic validation
1. **Thêm member**: Phải đã là member của company
2. **Cập nhật role**: Role phải thuộc scope workspace
3. **Xóa member**: Không thể xóa owner duy nhất
4. **Transfer ownership**: 
   - Chỉ Owner mới được
   - Owner cũ được assign downgrade role
   - Owner mới được set `isOwner=true` và `roleId=null`

## Cách sử dụng với frontend

Ví dụ với React/TypeScript:
```typescript
// Fetch members
const members = await fetch(`/api/workspaces/${workspaceId}/members`, {
  headers: { 'Authorization': `Bearer ${token}` }
});

// Add member
await fetch(`/api/workspaces/${workspaceId}/members`, {
  method: 'POST',
  headers: { 
    'Authorization': `Bearer ${token}`,
    'Content-Type': 'application/json' 
  },
  body: JSON.stringify({ userId: 'xxx', roleId: 123 })
});
```

## Kiểm tra

Sau khi chạy SQL, kiểm tra permissions đã được thêm:
```sql
SELECT * FROM permissions WHERE scope = 'workspace' ORDER BY id;
```

Bạn nên thấy 5 permissions mới được thêm vào.


