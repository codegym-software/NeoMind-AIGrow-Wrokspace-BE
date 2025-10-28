-- Fix workspace_members table to allow NULL role_id
-- This is needed because workspace owners don't need a role

ALTER TABLE workspace_members 
MODIFY COLUMN role_id INT NULL;

-- Verify the change
DESCRIBE workspace_members;
