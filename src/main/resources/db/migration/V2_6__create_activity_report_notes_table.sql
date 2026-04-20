-- Activity report notes: shared notes by agents and managers
CREATE TABLE activity_report_notes (
    id                    BIGSERIAL       PRIMARY KEY,
    activity_report_id    BIGINT          NOT NULL REFERENCES activity_reports(id) ON DELETE CASCADE,
    content               TEXT            NOT NULL,
    created_by            BIGINT          NOT NULL REFERENCES users(id),
    created_at            TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_activity_report_notes_report_id ON activity_report_notes(activity_report_id);

ALTER TYPE audit_entity_type ADD VALUE IF NOT EXISTS 'ACTIVITY_REPORT_NOTE';
