-- Script để xóa role Workspace Admin với targetId là null
DELETE FROM roles WHERE name = 'Workspace Admin' AND target_id IS NULL;

-- Xóa các quyền liên quan đến role Workspace Admin (nếu có)
DELETE FROM role_permissions WHERE role_id IN (SELECT id FROM roles WHERE name = 'Workspace Admin' AND target_id IS NULL);

-- Xóa các liên kết giữa người dùng và role Workspace Admin (nếu có)
DELETE FROM workspace_member_role WHERE role_id IN (SELECT id FROM roles WHERE name = 'Workspace Admin' AND target_id IS NULL);