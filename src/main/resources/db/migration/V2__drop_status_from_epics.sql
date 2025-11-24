-- Drop the `status` column from `epics`
-- MySQL 8+ syntax; ensure table exists before migration
ALTER TABLE epics
  DROP COLUMN status;