-- V1_21__create_system_reminders.sql
-- System reminders: auto-generated task reminders for deals

CREATE TABLE system_reminders (
    id            BIGSERIAL PRIMARY KEY,
    deal_id       BIGINT NOT NULL REFERENCES deals(id),
    reminder_type VARCHAR(50) NOT NULL DEFAULT 'NO_NEXT_ACTION',
    created_at    TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    dismissed     BOOLEAN NOT NULL DEFAULT FALSE,
    dismissed_at  TIMESTAMPTZ
);

CREATE INDEX idx_system_reminders_deal ON system_reminders (deal_id);
CREATE INDEX idx_system_reminders_active ON system_reminders (deal_id, dismissed) WHERE dismissed = FALSE;
