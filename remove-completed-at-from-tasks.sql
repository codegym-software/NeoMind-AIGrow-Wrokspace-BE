-- ================================================================
-- Script: Xóa cột completed_at khỏi bảng tasks
-- Ngày: 2025-10-30
-- Lý do: Sử dụng statusColumn (column_id → "Done") để xác định task hoàn thành
--        Không cần completed_at nữa vì đã có trạng thái động
-- ================================================================

USE ai_grow;

-- Xóa cột completed_at
ALTER TABLE tasks
DROP COLUMN completed_at;

-- Kiểm tra kết quả
DESCRIBE tasks;

-- ================================================================
-- LOGIC MỚI:
-- - Task.isCompleted() = kiểm tra statusColumn.name == "Done"
-- - Không cần timestamp riêng vì có thể dùng updated_at
-- - Khi task chuyển sang cột "Done" → updated_at = thời điểm completed
-- ================================================================
