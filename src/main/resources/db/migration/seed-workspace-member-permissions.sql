-- SQL script to add Workspace Member permissions to database
-- These permissions are for managing workspace members (scope = 'workspace')
-- Note: Excluding permission ids 1-4 as specified

-- Workspace Member Management Permissions
INSERT INTO permissions (scope, code, name) VALUES 
('workspace', 'workspace:member:read', 'Đọc danh sách thành viên workspace'),
('workspace', 'workspace:member:invite', 'Mời/thêm thành viên vào workspace'),
('workspace', 'workspace:member:set_user_permissions', 'Set quyền cho thành viên workspace'),
('workspace', 'workspace:member:delete', 'Xóa thành viên khỏi workspace');

-- Query to verify the permissions were added
SELECT id, scope, code, name 
FROM permissions 
WHERE scope = 'workspace' 
ORDER BY id;


