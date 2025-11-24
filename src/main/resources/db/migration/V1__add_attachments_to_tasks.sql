-- Thêm cột attachments dạng JSON cho bảng tasks
ALTER TABLE tasks
    ADD COLUMN attachments TEXT NULL COMMENT 'Danh sách file đính kèm dạng JSON';