-- Script to fix workspaces table structure
-- Run this script in your MySQL database

-- Step 1: Check current structure
DESCRIBE workspaces;
SHOW CREATE TABLE workspaces;

-- Step 2: Check foreign key constraints
SELECT 
    TABLE_NAME,
    COLUMN_NAME,
    CONSTRAINT_NAME,
    REFERENCED_TABLE_NAME,
    REFERENCED_COLUMN_NAME
FROM 
    INFORMATION_SCHEMA.KEY_COLUMN_USAGE 
WHERE 
    REFERENCED_TABLE_NAME = 'workspaces' 
    AND TABLE_SCHEMA = 'project_mgmt1';

-- Step 3: Drop foreign key constraint from projects table (if exists)
-- Replace 'projects_ibfk_1' with actual constraint name from step 2
ALTER TABLE projects DROP FOREIGN KEY projects_ibfk_1;

-- Step 4: Modify workspaces table structure
ALTER TABLE workspaces 
MODIFY COLUMN id VARCHAR(36) NOT NULL,
MODIFY COLUMN company_id VARCHAR(36) NOT NULL,
MODIFY COLUMN name VARCHAR(255) NOT NULL,
MODIFY COLUMN description TEXT,
ADD COLUMN created_at DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL,
ADD COLUMN updated_at DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL ON UPDATE CURRENT_TIMESTAMP,
ADD COLUMN archived_at DATETIME NULL;

-- Step 5: Recreate foreign key constraint (if needed)
-- ALTER TABLE projects ADD CONSTRAINT projects_ibfk_1 
-- FOREIGN KEY (workspace_id) REFERENCES workspaces(id);

-- Step 6: Verify the changes
DESCRIBE workspaces;