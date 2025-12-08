-- Migration: Add Timeline columns to tasks table
-- Date: 2025-12-04
-- Description: Thêm start_date và completed_at cho tính năng Timeline/Gantt Chart

-- 1. Thêm cột start_date (ngày bắt đầu thực tế của task)
ALTER TABLE tasks 
ADD COLUMN start_date DATE NULL 
COMMENT 'Timeline: Ngày bắt đầu thực tế của task. Null = inherit từ sprint.start_date';

-- 2. Thêm cột end_date (thời điểm hoàn thành thực tế)
ALTER TABLE tasks 
ADD COLUMN end_date DATETIME NULL 
COMMENT 'Timeline: Thời điểm hoàn thành thực tế. So sánh với due_date để biết on-time hay delay';

-- 3. Tạo index cho performance
CREATE INDEX idx_tasks_start_date ON tasks(start_date);
CREATE INDEX idx_tasks_end_date ON tasks(end_date);

-- 4. Optional: Update end_date cho các task đã Done (nếu muốn migrate data cũ)
-- UPDATE tasks t
-- INNER JOIN board_columns bc ON t.column_id = bc.id
-- SET t.end_date = t.updated_at
-- WHERE bc.name = 'Done' 
--   AND t.end_date IS NULL;

-- Note: 
-- - due_date giữ nguyên (đã có sẵn) - đại diện cho KẾ HOẠCH
-- - end_date - đại diện cho THỰC TẾ
-- - So sánh due_date vs end_date để biết on-time/delay
