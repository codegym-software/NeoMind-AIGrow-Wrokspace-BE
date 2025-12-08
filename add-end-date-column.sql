-- Add end_date column to tasks table for timeline feature
ALTER TABLE tasks ADD COLUMN end_date DATETIME NULL AFTER due_date;
