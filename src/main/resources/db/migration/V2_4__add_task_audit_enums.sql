-- Add TASK to audit_entity_type and STATUS_CHANGE to audit_action
ALTER TYPE audit_entity_type ADD VALUE IF NOT EXISTS 'TASK';
ALTER TYPE audit_action ADD VALUE IF NOT EXISTS 'STATUS_CHANGE';
