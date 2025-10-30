-- =========================================
-- MIGRATION: Chuẩn hóa Task assignee field
-- =========================================
-- Vấn đề: Có cả assignee_id lẫn assigned_to (trùng lặp)
-- Giải pháp: Chỉ giữ assignee_id (FK → users.id)

-- =========================================
-- KIỂM TRA CẤU TRÚC HIỆN TẠI
-- =========================================
-- Chạy lệnh này để xem cột nào đang tồn tại:
-- DESCRIBE tasks;

-- =========================================
-- TRƯỜNG HỢP 1: Nếu bảng có assigned_to, đổi tên thành assignee_id
-- =========================================

-- Kiểm tra xem cột assigned_to có tồn tại không
SELECT COUNT(*) 
FROM information_schema.COLUMNS 
WHERE TABLE_SCHEMA = DATABASE() 
  AND TABLE_NAME = 'tasks' 
  AND COLUMN_NAME = 'assigned_to';

-- Nếu kết quả = 1, chạy lệnh đổi tên:
ALTER TABLE tasks 
CHANGE COLUMN assigned_to assignee_id CHAR(36);

-- =========================================
-- TRƯỜNG HỢP 2: Nếu có CẢ HAI cột (assigned_to VÀ assignee_id)
-- =========================================

-- Kiểm tra xem có cả 2 cột không
SELECT COLUMN_NAME 
FROM information_schema.COLUMNS 
WHERE TABLE_SCHEMA = DATABASE() 
  AND TABLE_NAME = 'tasks' 
  AND COLUMN_NAME IN ('assigned_to', 'assignee_id');

-- Nếu có cả 2, thực hiện:

-- 1. Copy dữ liệu từ assigned_to sang assignee_id (nếu assignee_id trống)
UPDATE tasks 
SET assignee_id = assigned_to 
WHERE assignee_id IS NULL AND assigned_to IS NOT NULL;

-- 2. Xóa cột assigned_to (thừa)
ALTER TABLE tasks 
DROP COLUMN assigned_to;

-- =========================================
-- TRƯỜNG HỢP 3: Nếu chỉ có assignee_id (đã đúng)
-- =========================================
-- Không cần làm gì cả!

-- =========================================
-- THÊM INDEX CHO ASSIGNEE_ID (Performance optimization)
-- =========================================

-- Tạo index để tăng tốc query: "Lấy tất cả tasks của user X"
CREATE INDEX idx_tasks_assignee_id ON tasks(assignee_id);

-- =========================================
-- THÊM FOREIGN KEY CONSTRAINT (Optional - Nếu chưa có)
-- =========================================

-- Kiểm tra xem FK đã tồn tại chưa
SELECT CONSTRAINT_NAME 
FROM information_schema.KEY_COLUMN_USAGE 
WHERE TABLE_SCHEMA = DATABASE() 
  AND TABLE_NAME = 'tasks' 
  AND COLUMN_NAME = 'assignee_id' 
  AND REFERENCED_TABLE_NAME = 'users';

-- Nếu chưa có, thêm FK:
ALTER TABLE tasks 
ADD CONSTRAINT fk_tasks_assignee 
FOREIGN KEY (assignee_id) REFERENCES users(id) 
ON DELETE SET NULL;

-- =========================================
-- VERIFY CHANGES
-- =========================================

-- 1. Kiểm tra cấu trúc cột
DESCRIBE tasks;

-- 2. Kiểm tra indexes
SHOW INDEX FROM tasks WHERE Column_name = 'assignee_id';

-- 3. Kiểm tra foreign keys
SELECT 
    CONSTRAINT_NAME,
    COLUMN_NAME,
    REFERENCED_TABLE_NAME,
    REFERENCED_COLUMN_NAME
FROM information_schema.KEY_COLUMN_USAGE
WHERE TABLE_SCHEMA = DATABASE()
  AND TABLE_NAME = 'tasks'
  AND COLUMN_NAME = 'assignee_id';

-- 4. Test query
SELECT 
    t.id,
    t.title,
    t.assignee_id,
    u.display_name AS assignee_name
FROM tasks t
LEFT JOIN users u ON t.assignee_id = u.id
LIMIT 5;

-- =========================================
-- ROLLBACK SCRIPT (Nếu cần revert)
-- =========================================
-- CẢNH BÁO: Chỉ dùng khi cần rollback!

-- ALTER TABLE tasks DROP FOREIGN KEY fk_tasks_assignee;
-- ALTER TABLE tasks DROP INDEX idx_tasks_assignee_id;
-- ALTER TABLE tasks CHANGE COLUMN assignee_id assigned_to CHAR(36);
