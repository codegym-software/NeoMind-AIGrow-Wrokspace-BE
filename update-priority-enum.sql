-- Migration script to remove URGENT priority
-- This script will:
-- 1. Update any tasks with URGENT priority to HIGH
-- 2. Alter the ENUM column to remove URGENT option

-- Step 1: Update existing tasks with URGENT priority to HIGH
UPDATE tasks 
SET priority = 'HIGH' 
WHERE priority = 'URGENT';

-- Step 2: Alter the column to use new ENUM without URGENT
ALTER TABLE tasks 
MODIFY COLUMN priority ENUM('LOW', 'MEDIUM', 'HIGH') NOT NULL DEFAULT 'MEDIUM';

-- Verify the change
SELECT COLUMN_TYPE 
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_NAME = 'tasks' 
AND COLUMN_NAME = 'priority';
