-- Drop the `status` column from `epics`
-- Guarded for MySQL 8+: IF EXISTS prevents failure if column missing
ALTER TABLE epics
  DROP COLUMN IF EXISTS status;