-- Seed permission for project role grant_permission to mirror workspace logic
INSERT INTO permissions (scope, code, name, description)
SELECT 'project', 'project:role:grant_permission', 'Grant permissions to project role', 'Cho phép gán permission cho role dự án'
WHERE NOT EXISTS (
    SELECT 1 FROM permissions WHERE scope = 'project' AND code = 'project:role:grant_permission'
);