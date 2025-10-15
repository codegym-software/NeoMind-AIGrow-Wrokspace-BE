-- Script để test việc tạo workspace với owner
-- Chạy script này để kiểm tra workspace member được tạo với is_owner = true

-- 1. Kiểm tra workspace admin role có tồn tại không
SELECT '=== WORKSPACE ADMIN ROLE ===' as section;
SELECT * FROM roles WHERE scope = 'workspace' AND code = 'admin';

-- 2. Kiểm tra permission workspace:create có tồn tại không
SELECT '=== WORKSPACE CREATE PERMISSION ===' as section;
SELECT * FROM permissions WHERE code = 'workspace:create';

-- 3. Kiểm tra company có tồn tại không (thay company_id thực tế)
SELECT '=== COMPANY ===' as section;
SELECT * FROM companies LIMIT 1;

-- 4. Kiểm tra user có tồn tại không (thay email thực tế)
SELECT '=== USER ===' as section;
SELECT id, email, display_name FROM users LIMIT 1;

-- 5. Kiểm tra company members
SELECT '=== COMPANY MEMBERS ===' as section;
SELECT 
    cm.*,
    u.email,
    u.display_name,
    r.name as role_name
FROM company_members cm
LEFT JOIN users u ON cm.user_id = u.id
LEFT JOIN roles r ON cm.role_id = r.id
LIMIT 5;

-- 6. Kiểm tra workspace members hiện có
SELECT '=== WORKSPACE MEMBERS (BEFORE) ===' as section;
SELECT 
    wm.*,
    w.name as workspace_name,
    u.email,
    r.name as role_name,
    wm.is_owner
FROM workspace_members wm
LEFT JOIN workspaces w ON wm.workspace_id = w.id
LEFT JOIN users u ON wm.user_id = u.id
LEFT JOIN roles r ON wm.role_id = r.id
ORDER BY wm.created_at DESC
LIMIT 10;

-- 7. Test tạo workspace (sử dụng API call)
-- Sau khi gọi API tạo workspace, chạy query dưới đây để kiểm tra

-- 8. Kiểm tra workspace members sau khi tạo
SELECT '=== WORKSPACE MEMBERS (AFTER) ===' as section;
SELECT 
    wm.*,
    w.name as workspace_name,
    w.company_id,
    u.email,
    r.name as role_name,
    wm.is_owner,
    CASE 
        WHEN wm.is_owner = true THEN 'OWNER'
        ELSE 'MEMBER'
    END as member_type
FROM workspace_members wm
LEFT JOIN workspaces w ON wm.workspace_id = w.id
LEFT JOIN users u ON wm.user_id = u.id
LEFT JOIN roles r ON wm.role_id = r.id
ORDER BY w.created_at DESC
LIMIT 10;

-- 9. Kiểm tra workspace mới nhất được tạo
SELECT '=== LATEST WORKSPACE ===' as section;
SELECT 
    w.*,
    u.email as creator_email,
    u.display_name as creator_name
FROM workspaces w
LEFT JOIN users u ON w.created_by = u.id
ORDER BY w.created_at DESC
LIMIT 1;

-- 10. Kiểm tra workspace member của workspace mới nhất
SELECT '=== LATEST WORKSPACE MEMBERS ===' as section;
SELECT 
    wm.*,
    u.email,
    u.display_name,
    r.name as role_name,
    wm.is_owner,
    CASE 
        WHEN wm.is_owner = true THEN 'OWNER'
        ELSE 'MEMBER'
    END as member_type
FROM workspace_members wm
JOIN workspaces w ON wm.workspace_id = w.id
LEFT JOIN users u ON wm.user_id = u.id
LEFT JOIN roles r ON wm.role_id = r.id
WHERE w.id = (
    SELECT id FROM workspaces ORDER BY created_at DESC LIMIT 1
);

-- 11. Kiểm tra tổng số workspace members với owner privileges
SELECT '=== OWNER STATISTICS ===' as section;
SELECT 
    COUNT(*) as total_workspace_members,
    SUM(CASE WHEN is_owner = true THEN 1 ELSE 0 END) as total_owners,
    SUM(CASE WHEN is_owner = false THEN 1 ELSE 0 END) as total_members
FROM workspace_members;


