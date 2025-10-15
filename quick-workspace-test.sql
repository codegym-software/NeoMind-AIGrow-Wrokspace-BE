-- ===============================================
-- QUICK WORKSPACE CREATION TEST
-- Script để test nhanh việc tạo workspace và member
-- ===============================================

-- 1. TẠO WORKSPACE ADMIN ROLE (nếu chưa có)
INSERT INTO roles (scope, target_id, code, name, description) 
VALUES ('workspace', NULL, 'admin', 'Workspace Admin', 'Administrator role for workspace management')
ON DUPLICATE KEY UPDATE name = VALUES(name);

-- 2. TẠO PERMISSION WORKSPACE:CREATE (nếu chưa có)
INSERT INTO permissions (scope, code, name, description) 
VALUES ('company', 'workspace:create', 'Create Workspace in Company', 'Allow creating new workspace in company')
ON DUPLICATE KEY UPDATE name = VALUES(name);

-- 3. GÁN PERMISSION CHO COMPANY ROLES
INSERT INTO role_permission (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.scope = 'company' AND p.code = 'workspace:create'
ON DUPLICATE KEY UPDATE role_id = VALUES(role_id);

-- 4. KIỂM TRA DỮ LIỆU CẦN THIẾT
SELECT '=== REQUIRED DATA CHECK ===' as section;

-- Workspace admin role
SELECT 
    'Workspace Admin Role' as item,
    CASE 
        WHEN COUNT(*) > 0 THEN 'EXISTS'
        ELSE 'MISSING'
    END as status,
    GROUP_CONCAT(CONCAT(id, ':', name)) as details
FROM roles 
WHERE scope = 'workspace' AND code = 'admin';

-- Workspace create permission
SELECT 
    'Workspace Create Permission' as item,
    CASE 
        WHEN COUNT(*) > 0 THEN 'EXISTS'
        ELSE 'MISSING'
    END as status,
    GROUP_CONCAT(CONCAT(id, ':', name)) as details
FROM permissions 
WHERE code = 'workspace:create';

-- Company roles with workspace create permission
SELECT 
    'Company Roles with Permission' as item,
    COUNT(*) as count,
    GROUP_CONCAT(CONCAT(r.id, ':', r.name)) as role_details
FROM roles r
JOIN role_permission rp ON r.id = rp.role_id
JOIN permissions p ON rp.permission_id = p.id
WHERE r.scope = 'company' AND p.code = 'workspace:create';

-- 5. LẤY THÔNG TIN ĐỂ TEST
SET @test_company_id = (SELECT id FROM companies ORDER BY created_at DESC LIMIT 1);
SET @test_user_id = (SELECT id FROM users ORDER BY created_at DESC LIMIT 1);
SET @workspace_admin_role_id = (SELECT id FROM roles WHERE scope = 'workspace' AND code = 'admin' LIMIT 1);

SELECT '=== TEST DATA ===' as section;
SELECT 
    @test_company_id as company_id,
    @test_user_id as user_id,
    @workspace_admin_role_id as workspace_admin_role_id;

-- 6. KIỂM TRA USER CÓ PHẢI MEMBER CỦA COMPANY KHÔNG
SELECT '=== USER COMPANY MEMBERSHIP ===' as section;
SELECT 
    CASE 
        WHEN EXISTS(
            SELECT 1 FROM company_members 
            WHERE company_id = @test_company_id 
            AND user_id = @test_user_id
        ) THEN 'IS MEMBER'
        ELSE 'NOT MEMBER'
    END as membership_status;

-- Nếu user không phải member, tạo company member
INSERT INTO company_members (company_id, user_id, role_id, is_owner)
SELECT 
    @test_company_id,
    @test_user_id,
    r.id,
    false
FROM roles r
WHERE r.scope = 'company' AND r.code = 'admin'
LIMIT 1
ON DUPLICATE KEY UPDATE role_id = VALUES(role_id);

-- 7. TEST TẠO WORKSPACE MEMBER THỦ CÔNG
-- Tạo workspace test
INSERT INTO workspaces (id, company_id, created_by, name, description)
VALUES (
    UUID(),
    @test_company_id,
    @test_user_id,
    'Test Workspace ' || UNIX_TIMESTAMP(),
    'Test workspace created by script'
);

-- Lấy workspace vừa tạo
SET @test_workspace_id = (SELECT id FROM workspaces ORDER BY created_at DESC LIMIT 1);

-- Tạo workspace member
INSERT INTO workspace_members (id, workspace_id, user_id, role_id, is_owner)
VALUES (
    UUID(),
    @test_workspace_id,
    @test_user_id,
    @workspace_admin_role_id,
    true
);

-- 8. KIỂM TRA KẾT QUẢ
SELECT '=== CREATION RESULT ===' as section;
SELECT 
    w.id as workspace_id,
    w.name as workspace_name,
    w.company_id,
    w.created_by,
    wm.id as member_id,
    wm.user_id,
    wm.role_id,
    wm.is_owner,
    u.email as creator_email,
    r.name as role_name
FROM workspaces w
LEFT JOIN workspace_members wm ON w.id = wm.workspace_id
LEFT JOIN users u ON w.created_by = u.id
LEFT JOIN roles r ON wm.role_id = r.id
WHERE w.id = @test_workspace_id;

-- 9. CLEANUP (OPTIONAL)
-- Xóa dữ liệu test nếu muốn
-- DELETE FROM workspace_members WHERE workspace_id = @test_workspace_id;
-- DELETE FROM workspaces WHERE id = @test_workspace_id;


