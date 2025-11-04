-- Cleanup script: Xóa tất cả board và column cũ để test auto-create lại từ đầu
-- Backend sẽ tự động tạo 3 columns mặc định: To Do, In Progress, Done
-- User có thể tự thêm các columns khác từ UI (Status động)
-- Run this in MySQL Workbench or command line

USE neomind_db;

-- 1. Xóa tất cả tasks có column_id (set NULL trước khi xóa columns)
UPDATE tasks SET status_column_id = NULL WHERE status_column_id IS NOT NULL;

-- 2. Xóa tất cả board columns
DELETE FROM board_columns;

-- 3. Xóa tất cả boards
DELETE FROM boards;

-- 4. Reset auto_increment
ALTER TABLE boards AUTO_INCREMENT = 1;
ALTER TABLE board_columns AUTO_INCREMENT = 1;

-- Verify deletion
SELECT COUNT(*) as total_boards FROM boards;
SELECT COUNT(*) as total_columns FROM board_columns;
SELECT COUNT(*) as tasks_with_column FROM tasks WHERE status_column_id IS NOT NULL;

-- Expected results: all counts should be 0
-- Sau khi cleanup, frontend sẽ tự động tạo board với 3 columns mặc định từ backend
