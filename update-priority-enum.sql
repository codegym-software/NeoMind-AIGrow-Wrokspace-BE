-- Migration script to fix priority enum values
-- This script will:
-- 1. Update any tasks with URGENT priority to high
-- 2. Convert all uppercase priority values to lowercase
-- 3. Alter the ENUM column to use lowercase values

-- Step 1: Update existing tasks with URGENT priority to high
UPDATE tasks 
SET priority = 'high' 
WHERE priority = 'URGENT';



-- Step 3: Alter the column to use lowercase ENUM values
ALTER TABLE tasks 
MODIFY COLUMN priority ENUM('low', 'medium', 'high') NOT NULL DEFAULT 'medium';

-- Verify the change
SELECT COLUMN_TYPE 
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_NAME = 'tasks' 
AND COLUMN_NAME = 'priority';
