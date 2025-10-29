-- Make start_date and end_date nullable in sprints table
-- This allows creating sprints for planning purposes without specific dates

ALTER TABLE sprints 
    MODIFY COLUMN start_date DATE NULL,
    MODIFY COLUMN end_date DATE NULL;

-- Add comment to document the change
ALTER TABLE sprints 
    COMMENT = 'Sprints table - start_date and end_date are optional for planning phase';
