-- Add EXIT_CRITERIA to admin_setting_type enum (must be in own transaction)
ALTER TYPE admin_setting_type ADD VALUE IF NOT EXISTS 'EXIT_CRITERIA';
