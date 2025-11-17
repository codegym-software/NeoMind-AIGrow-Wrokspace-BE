-- Add attachments column to tasks table
ALTER TABLE tasks 
ADD COLUMN attachments JSON NULL COMMENT 'Danh sách file đính kèm dạng JSON';
