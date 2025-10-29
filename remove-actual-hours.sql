-- Migration: Remove actual_hours column from tasks table
-- Date: 2025-10-29
-- Description: Remove actualHours field as it's no longer needed

USE ai_grow;

-- Drop the actual_hours column
ALTER TABLE tasks DROP COLUMN actual_hours;

-- Verify the column is removed
DESCRIBE tasks;
