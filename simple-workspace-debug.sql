-- ===============================================
-- SIMPLE WORKSPACE DEBUG SCRIPT
-- Script đơn giản để debug workspace creation
-- ===============================================

-- 1. KIỂM TRA WORKSPACE ADMIN ROLE
SELECT '1. Workspace Admin Role:' as debug_step;
SELECT id, scope, code, name FROM roles WHERE scope = 'workspace' AND code = 'admin';

-- Nếu không có, tạo role
INSERT IGNORE INTO roles (scope, target_id, code, name, description) 
VALUES ('workspace', NULL, 'admin', 'Workspace Admin', 'Admin role for workspace');

-- 2. KIỂM TRA PERMISSION
SELECT '2. Workspace Create Permission:' as debug_step;
SELECT id, scope, code, name FROM permissions WHERE code = 'workspace:create';

-- Nếu không có, tạo permission
INSERT IGNORE INTO permissions (scope, code, name, description) 
VALUES ('company', 'workspace:create', 'Create Workspace', 'Allow creating workspace');

-- 3. GÁN PERMISSION CHO ROLES
INSERT IGNORE INTO role_permission (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.scope = 'company' AND p.code = 'workspace:create';

-- 4. KIỂM TRA COMPANY VÀ USER
SELECT '3. Latest Company:' as debug_step;
SELECT id, name FROM companies ORDER BY created_at DESC LIMIT 1;

SELECT '4. Latest User:' as debug_step;
SELECT id, email FROM users ORDER BY created_at DESC LIMIT 1;

-- 5. KIỂM TRA COMPANY MEMBERS
SELECT '5. Company Members:' as debug_step;
SELECT 
    cm.company_id,
    cm.user_id,
    cm.role_id,
    u.email,
    r.name as role_name
FROM company_members cm
JOIN users u ON cm.user_id = u.id
JOIN roles r ON cm.role_id = r.id
ORDER BY cm.id DESC
LIMIT 3;

-- 6. KIỂM TRA WORKSPACES HIỆN TẠI
SELECT '6. Current Workspaces:' as debug_step;
SELECT 
    w.id,
    w.name,
    w.company_id,
    w.created_by,
    w.created_at,
    u.email as creator_email
FROM workspaces w
LEFT JOIN users u ON w.created_by = u.id
ORDER BY w.created_at DESC
LIMIT 3;

-- 7. KIỂM TRA WORKSPACE MEMBERS
SELECT '7. Current Workspace Members:' as debug_step;
SELECT 
    wm.id,
    wm.workspace_id,
    wm.user_id,
    wm.role_id,
    wm.is_owner,
    w.name as workspace_name,
    u.email,
    r.name as role_name
FROM workspace_members wm
JOIN workspaces w ON wm.workspace_id = w.id
LEFT JOIN users u ON wm.user_id = u.id
LEFT JOIN roles r ON wm.role_id = r.id
ORDER BY wm.id DESC
LIMIT 5;

-- 8. TEST TẠO WORKSPACE MEMBER THỦ CÔNG
SELECT '8. Testing Manual Workspace Member Creation:' as debug_step;

-- Lấy workspace mới nhất
SET @ws_id = (SELECT id FROM workspaces ORDER BY created_at DESC LIMIT 1);
SET @user_id = (SELECT id FROM users ORDER BY created_at DESC LIMIT 1);
SET @role_id = (SELECT id FROM roles WHERE scope = 'workspace' AND code = 'admin' LIMIT 1);

-- Tạo workspace member
INSERT INTO workspace_members (id, workspace_id, user_id, role_id, is_owner)
VALUES (UUID(), @ws_id, @user_id, @role_id, true);

-- Kiểm tra kết quả
SELECT '9. Workspace Member Created:' as debug_step;
SELECT 
    wm.id,
    wm.workspace_id,
    wm.user_id,
    wm.role_id,
    wm.is_owner,
    w.name as workspace_name,
    u.email,
    r.name as role_name
FROM workspace_members wm
JOIN workspaces w ON wm.workspace_id = w.id
LEFT JOIN users u ON wm.user_id = u.id
LEFT JOIN roles r ON wm.role_id = r.id
WHERE wm.workspace_id = @ws_id;

-- 10. SUMMARY
SELECT '10. Summary:' as debug_step;
SELECT 
    (SELECT COUNT(*) FROM roles WHERE scope = 'workspace' AND code = 'admin') as workspace_admin_roles,
    (SELECT COUNT(*) FROM permissions WHERE code = 'workspace:create') as workspace_create_permissions,
    (SELECT COUNT(*) FROM workspaces) as total_workspaces,
    (SELECT COUNT(*) FROM workspace_members) as total_workspace_members,
    (SELECT COUNT(*) FROM workspace_members WHERE is_owner = true) as total_workspace_owners;


