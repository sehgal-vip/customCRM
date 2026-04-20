-- Task notes: agent and manager notes on tasks
CREATE TYPE note_type AS ENUM ('AGENT', 'MANAGER');

CREATE TABLE task_notes (
    id          BIGSERIAL       PRIMARY KEY,
    task_id     BIGINT          NOT NULL REFERENCES tasks(id) ON DELETE CASCADE,
    content     TEXT            NOT NULL,
    note_type   note_type       NOT NULL,
    created_by  BIGINT          NOT NULL REFERENCES users(id),
    created_at  TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_task_notes_task_id ON task_notes(task_id);

ALTER TYPE audit_entity_type ADD VALUE IF NOT EXISTS 'TASK_NOTE';
