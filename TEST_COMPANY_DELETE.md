# Hướng dẫn Test Chức năng Xóa Company

## Cách Test

### 1. Test qua API (Khuyến nghị)

Sử dụng file `test-company-delete.http` hoặc các công cụ như Postman, curl, hoặc REST Client.

#### Bước 1: Lấy JWT Token
- Đăng nhập qua API `/api/session` hoặc `/api/mock-auth/login`
- Copy JWT token từ response

#### Bước 2: Lấy danh sách companies
```http
GET http://localhost:8081/api/companies
Authorization: Bearer YOUR_TOKEN
```

#### Bước 3: Chọn một company để test
- Copy `id` của một company từ danh sách
- Đảm bảo bạn có quyền `company:delete` cho company đó

#### Bước 4: Xóa company
```http
DELETE http://localhost:8081/api/companies/{companyId}
Authorization: Bearer YOUR_TOKEN
```

**Expected Response:**
- Status: `204 No Content` (thành công)
- Hoặc `404 Not Found` nếu company không tồn tại
- Hoặc `403 Forbidden` nếu không có quyền

#### Bước 5: Verify deletion
```http
GET http://localhost:8081/api/companies/{companyId}
Authorization: Bearer YOUR_TOKEN
```

**Expected Response:**
- Status: `404 Not Found` hoặc `500` với message "Company not found"

### 2. Test qua Database

Sau khi xóa company, kiểm tra database để đảm bảo tất cả entities liên quan đã được xóa:

```sql
-- Kiểm tra company đã bị xóa
SELECT * FROM companies WHERE id = 'YOUR_COMPANY_ID';

-- Kiểm tra company members đã bị xóa
SELECT * FROM company_members WHERE company_id = 'YOUR_COMPANY_ID';

-- Kiểm tra workspaces đã bị xóa
SELECT * FROM workspaces WHERE company_id = 'YOUR_COMPANY_ID';

-- Kiểm tra projects đã bị xóa (thông qua workspaces)
SELECT p.* FROM projects p
JOIN workspaces w ON w.id = p.workspace_id
WHERE w.company_id = 'YOUR_COMPANY_ID';

-- Kiểm tra roles đã bị xóa
SELECT * FROM roles WHERE scope = 'company' AND target_id = 'YOUR_COMPANY_ID';
SELECT * FROM roles WHERE scope = 'workspace' AND target_id IN (
    SELECT id FROM workspaces WHERE company_id = 'YOUR_COMPANY_ID'
);
SELECT * FROM roles WHERE scope = 'project' AND target_id IN (
    SELECT p.id FROM projects p
    JOIN workspaces w ON w.id = p.workspace_id
    WHERE w.company_id = 'YOUR_COMPANY_ID'
);

-- Kiểm tra role_permissions đã bị xóa
SELECT rp.* FROM role_permission rp
JOIN roles r ON r.id = rp.role_id
WHERE (r.scope = 'company' AND r.target_id = 'YOUR_COMPANY_ID')
   OR (r.scope = 'workspace' AND r.target_id IN (SELECT id FROM workspaces WHERE company_id = 'YOUR_COMPANY_ID'))
   OR (r.scope = 'project' AND r.target_id IN (
       SELECT p.id FROM projects p
       JOIN workspaces w ON w.id = p.workspace_id
       WHERE w.company_id = 'YOUR_COMPANY_ID'
   ));
```

Tất cả các query trên phải trả về **0 rows**.

### 3. Test Unit (Nếu có database test)

Chạy test unit:
```bash
mvn test -Dtest=CompanyServiceDeleteTest
```

## Checklist Test

- [ ] Company được xóa thành công
- [ ] Company members được xóa
- [ ] Workspaces được xóa
- [ ] Workspace members được xóa
- [ ] Projects được xóa
- [ ] Project members được xóa
- [ ] Tasks được xóa
- [ ] Comments được xóa
- [ ] Comment mentions được xóa
- [ ] Tags được xóa
- [ ] Task tags được xóa
- [ ] Boards được xóa
- [ ] Board columns được xóa
- [ ] Epics được xóa
- [ ] Sprints được xóa
- [ ] Company roles được xóa
- [ ] Workspace roles được xóa
- [ ] Project roles được xóa
- [ ] Role permissions được xóa

## Lưu ý

1. **Backup database** trước khi test trên môi trường production
2. Đảm bảo bạn có quyền `company:delete` trước khi test
3. Test trên một company test, không test trên dữ liệu thật
4. Kiểm tra log để đảm bảo không có lỗi SQL constraint violation


