-- Add Sprint permissions to the permissions table
-- Scope: project
-- These permissions are required for Sprint management in Backlog & Sprint page

-- Insert Sprint permissions
INSERT INTO permissions (scope, code, name, description) VALUES
('project', 'project:sprint:create', 'Create Sprint', 'Permission to create sprints in a project'),
('project', 'project:sprint:read', 'Read Sprint', 'Permission to view sprints in a project'),
('project', 'project:sprint:update', 'Update Sprint', 'Permission to update sprints in a project'),
('project', 'project:sprint:delete', 'Delete Sprint', 'Permission to delete sprints in a project')
ON DUPLICATE KEY UPDATE 
    name = VALUES(name),
    description = VALUES(description);

-- Grant Sprint permissions to project admin role (assuming role_id = 1 for admin)
-- You may need to adjust the role_id based on your actual database

-- First, get the permission IDs
SET @sprint_create_perm = (SELECT id FROM permissions WHERE code = 'project:sprint:create' LIMIT 1);
SET @sprint_read_perm = (SELECT id FROM permissions WHERE code = 'project:sprint:read' LIMIT 1);
SET @sprint_update_perm = (SELECT id FROM permissions WHERE code = 'project:sprint:update' LIMIT 1);
SET @sprint_delete_perm = (SELECT id FROM permissions WHERE code = 'project:sprint:delete' LIMIT 1);

-- Grant all sprint permissions to project admin role (role with scope='project' and code='admin')
-- This assumes you have a role with code='admin' in scope='project'
INSERT INTO role_permission (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
CROSS JOIN permissions p
WHERE r.scope = 'project' 
  AND r.code = 'admin'
  AND p.scope = 'project'
  AND p.code IN ('project:sprint:create', 'project:sprint:read', 'project:sprint:update', 'project:sprint:delete')
ON DUPLICATE KEY UPDATE role_id = role_id;

-- Grant read permission to project member role (if exists)
INSERT INTO role_permission (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
CROSS JOIN permissions p
WHERE r.scope = 'project' 
  AND r.code = 'member'
  AND p.scope = 'project'
  AND p.code IN ('project:sprint:read', 'project:sprint:create')
ON DUPLICATE KEY UPDATE role_id = role_id;

-- Verify the inserted permissions
SELECT * FROM permissions WHERE code LIKE '%sprint%';

-- Verify role permissions
SELECT r.name as role_name, r.code as role_code, p.code as permission_code, p.name as permission_name
FROM roles r
JOIN role_permission rp ON r.id = rp.role_id
JOIN permissions p ON rp.permission_id = p.id
WHERE p.code LIKE '%sprint%'
ORDER BY r.name, p.code;
