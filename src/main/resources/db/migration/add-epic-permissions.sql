INSERT INTO permissions (scope, code, name, description) VALUES
('project', 'project:epic:create', 'Tạo epic', 'Tạo epic trong project')
ON DUPLICATE KEY UPDATE name = VALUES(name), description = VALUES(description);

INSERT INTO permissions (scope, code, name, description) VALUES
('project', 'project:epic:read', 'Xem epic', 'Đọc thông tin epic trong project')
ON DUPLICATE KEY UPDATE name = VALUES(name), description = VALUES(description);

INSERT INTO permissions (scope, code, name, description) VALUES
('project', 'project:epic:update', 'Cập nhật epic', 'Sửa thông tin epic trong project')
ON DUPLICATE KEY UPDATE name = VALUES(name), description = VALUES(description);

INSERT INTO permissions (scope, code, name, description) VALUES
('project', 'project:epic:delete', 'Xóa epic', 'Xóa epic trong project')
ON DUPLICATE KEY UPDATE name = VALUES(name), description = VALUES(description);


