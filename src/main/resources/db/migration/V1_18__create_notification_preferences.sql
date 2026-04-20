-- V1_18__create_notification_preferences.sql
-- Notification preferences: per-role toggles for notification event types

CREATE TABLE notification_preferences (
    id         BIGSERIAL PRIMARY KEY,
    role       user_role NOT NULL,
    event_type notification_event_type NOT NULL,
    enabled    BOOLEAN NOT NULL DEFAULT TRUE,
    updated_by BIGINT REFERENCES users(id),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT uq_role_event_type UNIQUE (role, event_type)
);
