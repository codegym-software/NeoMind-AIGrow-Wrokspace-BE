-- Cleanup script to remove transfer_ownership permissions from DB
-- Run this if you previously seeded these permissions and now rely on owner-only gates

DELETE FROM permissions 
WHERE scope = 'workspace' AND code = 'workspace:member:transfer_ownership';

DELETE FROM permissions 
WHERE scope = 'project' AND code = 'project:member:transfer_ownership';

-- Also remove any role_permission references to these permissions
DELETE rp FROM role_permission rp
JOIN permissions p ON p.id = rp.permission_id
WHERE (p.scope = 'workspace' AND p.code = 'workspace:member:transfer_ownership')
   OR (p.scope = 'project'   AND p.code = 'project:member:transfer_ownership');