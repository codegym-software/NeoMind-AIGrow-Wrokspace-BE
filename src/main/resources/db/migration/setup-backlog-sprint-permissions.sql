-- Add Sprint permissions for Backlog & Sprint Management
-- Run this file to add all required permissions for Sprint management

-- ==================================================
-- SPRINT PERMISSIONS
-- ==================================================
INSERT INTO permissions (scope, code, name, description) VALUES
('project', 'project:sprint:create', 'Create Sprint', 'Permission to create sprints in a project'),
('project', 'project:sprint:read', 'Read Sprint', 'Permission to view sprints in a project'),
('project', 'project:sprint:update', 'Update Sprint', 'Permission to update sprints in a project'),
('project', 'project:sprint:delete', 'Delete Sprint', 'Permission to delete sprints in a project')
ON DUPLICATE KEY UPDATE 
    name = VALUES(name),
    description = VALUES(description);

-- ==================================================
-- GRANT PERMISSIONS TO PROJECT ADMIN ROLE
-- ==================================================
INSERT INTO role_permission (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
CROSS JOIN permissions p
WHERE r.scope = 'project' 
  AND r.code = 'admin'
  AND p.scope = 'project'
  AND p.code IN (
    'project:sprint:create', 
    'project:sprint:read', 
    'project:sprint:update', 
    'project:sprint:delete'
  )
ON DUPLICATE KEY UPDATE role_id = role_id;

-- ==================================================
-- GRANT PERMISSIONS TO PROJECT MEMBER ROLE (Optional)
-- ==================================================
INSERT INTO role_permission (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
CROSS JOIN permissions p
WHERE r.scope = 'project' 
  AND r.code = 'member'
  AND p.scope = 'project'
  AND p.code IN (
    'project:sprint:read',
    'project:sprint:create'
  )
ON DUPLICATE KEY UPDATE role_id = role_id;

-- ==================================================
-- VERIFICATION QUERIES
-- ==================================================

-- Check all Sprint permissions
SELECT scope, code, name, description 
FROM permissions 
WHERE code LIKE '%sprint%'
ORDER BY scope, code;

-- Check role assignments for Sprint permissions
SELECT 
    r.scope as role_scope,
    r.code as role_code,
    r.name as role_name,
    p.code as permission_code,
    p.name as permission_name
FROM roles r
JOIN role_permission rp ON r.id = rp.role_id
JOIN permissions p ON rp.permission_id = p.id
WHERE p.code LIKE '%sprint%'
ORDER BY r.scope, r.code, p.code;

-- Count Sprint permissions by role
SELECT 
    r.code as role_code,
    r.name as role_name,
    COUNT(*) as sprint_permissions
FROM roles r
JOIN role_permission rp ON r.id = rp.role_id
JOIN permissions p ON rp.permission_id = p.id
WHERE r.scope = 'project' AND p.code LIKE '%sprint%'
GROUP BY r.id, r.code, r.name;
