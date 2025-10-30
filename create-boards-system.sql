-- =========================================
-- JIRA-STYLE BOARD SYSTEM MIGRATION
-- =========================================
-- Cấu trúc 4 cấp: Project → Board → BoardColumn → Task
-- Thực thi theo thứ tự để tránh lỗi foreign key

-- =========================================
-- 1. TẠO BẢNG BOARDS (Bảng - Ngăn kéo trong tủ)
-- =========================================
CREATE TABLE IF NOT EXISTS boards (
    id INT AUTO_INCREMENT PRIMARY KEY,
    project_id CHAR(36) NOT NULL,
    name VARCHAR(255) NOT NULL DEFAULT 'Kanban Board',
    description TEXT,
    is_default BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    -- Foreign key to projects table
    CONSTRAINT fk_boards_project FOREIGN KEY (project_id) 
        REFERENCES projects(id) ON DELETE CASCADE,
    
    -- Indexes for performance
    INDEX idx_boards_project_id (project_id),
    INDEX idx_boards_is_default (is_default)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =========================================
-- 2. CẬP NHẬT BẢNG BOARD_COLUMNS (Thêm constraints)
-- =========================================
-- Kiểm tra xem bảng board_columns đã tồn tại chưa
-- Nếu chưa có thì tạo mới, nếu có rồi thì cập nhật

-- Drop và tạo lại bảng board_columns với đầy đủ ràng buộc
DROP TABLE IF EXISTS board_columns;

CREATE TABLE board_columns (
    id INT AUTO_INCREMENT PRIMARY KEY,
    board_id INT NOT NULL,
    name VARCHAR(100) NOT NULL,
    position INT NOT NULL DEFAULT 0,
    color VARCHAR(7) DEFAULT '#6B7280',  -- Hex color code (e.g., #3B82F6)
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    -- Foreign key to boards table
    CONSTRAINT fk_board_columns_board FOREIGN KEY (board_id) 
        REFERENCES boards(id) ON DELETE CASCADE,
    
    -- Unique constraint: Không cho trùng tên trong cùng một board
    CONSTRAINT uk_board_columns_board_name UNIQUE (board_id, name),
    
    -- Indexes for performance
    INDEX idx_board_columns_board_id (board_id),
    INDEX idx_board_columns_position (position)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =========================================
-- 3. CẬP NHẬT BẢNG TASKS (Thêm column_id và order_index)
-- =========================================

-- Thêm cột column_id nếu chưa có
ALTER TABLE tasks 
    ADD COLUMN IF NOT EXISTS column_id INT DEFAULT NULL 
    AFTER sprint_id;

-- Thêm foreign key constraint
ALTER TABLE tasks 
    ADD CONSTRAINT fk_tasks_column 
    FOREIGN KEY (column_id) REFERENCES board_columns(id) 
    ON DELETE SET NULL;

-- Đảm bảo order_index đã có (nếu chưa có thì thêm)
ALTER TABLE tasks 
    MODIFY COLUMN order_index INT NOT NULL DEFAULT 0;

-- Thêm index cho performance
ALTER TABLE tasks 
    ADD INDEX IF NOT EXISTS idx_tasks_column_id (column_id);

ALTER TABLE tasks 
    ADD INDEX IF NOT EXISTS idx_tasks_order_index (order_index);

-- =========================================
-- 4. MIGRATION DATA: Tạo Board mặc định cho mỗi Project
-- =========================================

-- Tạo board mặc định cho tất cả các project hiện có
INSERT INTO boards (project_id, name, is_default, description)
SELECT 
    id AS project_id,
    CONCAT(name, ' - Board') AS name,
    TRUE AS is_default,
    'Default Kanban board' AS description
FROM projects
WHERE NOT EXISTS (
    SELECT 1 FROM boards WHERE boards.project_id = projects.id
);

-- =========================================
-- 5. MIGRATION DATA: Tạo 3 cột mặc định cho mỗi Board
-- =========================================

-- Tạo 3 cột mặc định: TO DO, IN PROGRESS, DONE
INSERT INTO board_columns (board_id, name, position, color)
SELECT 
    b.id AS board_id,
    'TO DO' AS name,
    0 AS position,
    '#6B7280' AS color
FROM boards b
WHERE NOT EXISTS (
    SELECT 1 FROM board_columns bc 
    WHERE bc.board_id = b.id AND bc.name = 'TO DO'
);

INSERT INTO board_columns (board_id, name, position, color)
SELECT 
    b.id AS board_id,
    'IN PROGRESS' AS name,
    1 AS position,
    '#3B82F6' AS color
FROM boards b
WHERE NOT EXISTS (
    SELECT 1 FROM board_columns bc 
    WHERE bc.board_id = b.id AND bc.name = 'IN PROGRESS'
);

INSERT INTO board_columns (board_id, name, position, color)
SELECT 
    b.id AS board_id,
    'DONE' AS name,
    2 AS position,
    '#10B981' AS color
FROM boards b
WHERE NOT EXISTS (
    SELECT 1 FROM board_columns bc 
    WHERE bc.board_id = b.id AND bc.name = 'DONE'
);

-- =========================================
-- 6. MIGRATION DATA: Migrate Tasks sang column_id
-- =========================================

-- Map các tasks hiện có sang cột TO DO (default)
-- Giả sử tasks chưa có column_id (NULL) thì gán vào cột TO DO
UPDATE tasks t
INNER JOIN projects p ON t.project_id = p.id
INNER JOIN boards b ON b.project_id = p.id AND b.is_default = TRUE
INNER JOIN board_columns bc ON bc.board_id = b.id AND bc.name = 'TO DO'
SET t.column_id = bc.id
WHERE t.column_id IS NULL;

-- =========================================
-- 7. VERIFY DATA (Optional - Chạy sau khi migration)
-- =========================================

-- Kiểm tra số lượng boards đã tạo
SELECT 'Total Boards Created:' AS info, COUNT(*) AS count FROM boards;

-- Kiểm tra số lượng columns đã tạo
SELECT 'Total Columns Created:' AS info, COUNT(*) AS count FROM board_columns;

-- Kiểm tra tasks đã được gán column_id
SELECT 
    'Tasks with column_id:' AS info,
    COUNT(*) AS count 
FROM tasks 
WHERE column_id IS NOT NULL;

-- Xem cấu trúc board của 1 project mẫu
SELECT 
    p.name AS project_name,
    b.name AS board_name,
    bc.name AS column_name,
    bc.position,
    COUNT(t.id) AS task_count
FROM projects p
LEFT JOIN boards b ON b.project_id = p.id
LEFT JOIN board_columns bc ON bc.board_id = b.id
LEFT JOIN tasks t ON t.column_id = bc.id AND t.archived_at IS NULL
GROUP BY p.id, b.id, bc.id
ORDER BY p.name, bc.position
LIMIT 10;

-- =========================================
-- 8. ROLLBACK SCRIPT (Chỉ dùng khi cần revert)
-- =========================================
-- CẢNH BÁO: Script này sẽ XÓA dữ liệu!
-- Uncomment các dòng dưới nếu cần rollback:

-- ALTER TABLE tasks DROP FOREIGN KEY fk_tasks_column;
-- ALTER TABLE tasks DROP COLUMN column_id;
-- DROP TABLE IF EXISTS board_columns;
-- DROP TABLE IF EXISTS boards;
