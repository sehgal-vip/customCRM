-- V1_12__create_notifications.sql
-- Notifications: in-app notification system

CREATE TABLE notifications (
    id         BIGSERIAL PRIMARY KEY,
    user_id    BIGINT NOT NULL REFERENCES users(id),
    event_type notification_event_type NOT NULL,
    priority   notification_priority NOT NULL,
    title      VARCHAR(500) NOT NULL,
    content    TEXT NOT NULL,
    deal_id    BIGINT REFERENCES deals(id),
    cleared    BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    cleared_at TIMESTAMPTZ
);

CREATE INDEX idx_notifications_user_cleared ON notifications (user_id, cleared);
CREATE INDEX idx_notifications_user_created ON notifications (user_id, created_at DESC);
CREATE INDEX idx_notifications_deal ON notifications (deal_id);
