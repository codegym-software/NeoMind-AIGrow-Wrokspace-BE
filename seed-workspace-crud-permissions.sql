-- SQL seed: CRUD permissions for Workspace
-- Note: Excludes referencing specific IDs 1-4; relies on (scope, code) uniqueness

-- Company-scope permission to create workspace under a company
INSERT INTO permissions (scope, code, name) VALUES
('company', 'workspace:create', 'Tạo workspace trong công ty')
ON DUPLICATE KEY UPDATE name = VALUES(name);

-- Workspace-scope CRUD permissions
INSERT INTO permissions (scope, code, name) VALUES
('workspace', 'workspace:read', 'Xem workspace')
ON DUPLICATE KEY UPDATE name = VALUES(name);

INSERT INTO permissions (scope, code, name) VALUES
('workspace', 'workspace:update', 'Cập nhật workspace')
ON DUPLICATE KEY UPDATE name = VALUES(name);

INSERT INTO permissions (scope, code, name) VALUES
('workspace', 'workspace:delete', 'Xóa workspace')
ON DUPLICATE KEY UPDATE name = VALUES(name);

-- Optional aggregate permission
INSERT INTO permissions (scope, code, name) VALUES
('workspace', 'workspace:crud', 'Toàn quyền CRUD workspace')
ON DUPLICATE KEY UPDATE name = VALUES(name);

-- Verify
SELECT id, scope, code, name FROM permissions WHERE scope IN ('company','workspace') AND code LIKE 'workspace:%' ORDER BY id;

