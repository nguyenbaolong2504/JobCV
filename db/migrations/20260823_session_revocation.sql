-- RECRUITFLOW_SAFE_ADDITIVE_MIGRATION
-- Add a credential/session version to the currently selected JobCV database.
-- This migration is idempotent and never deletes, resets, or seeds user data.
-- MySQL 8.0.16+; run it after choosing the intended schema (for example `jobcvdb`).

SET @recruitflow_schema := DATABASE();
SET @recruitflow_statement := (
    SELECT IF(
        EXISTS (
            SELECT 1
            FROM information_schema.columns
            WHERE table_schema = @recruitflow_schema
              AND table_name = 'users'
              AND column_name = 'session_version'
        ),
        'SELECT ''session_version already exists'' AS migration_status',
        'ALTER TABLE users ADD COLUMN session_version INT UNSIGNED NOT NULL DEFAULT 0 AFTER status'
    )
);
PREPARE recruitflow_statement FROM @recruitflow_statement;
EXECUTE recruitflow_statement;
DEALLOCATE PREPARE recruitflow_statement;
