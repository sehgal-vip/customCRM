-- New enum types
CREATE TYPE task_status AS ENUM ('OPEN', 'IN_PROGRESS', 'DONE');
CREATE TYPE task_priority AS ENUM ('HIGH', 'MEDIUM', 'LOW');

-- Tasks table
CREATE TABLE tasks (
    id                  BIGSERIAL PRIMARY KEY,
    title               VARCHAR(500) NOT NULL,
    description         TEXT,
    status              task_status NOT NULL DEFAULT 'OPEN',
    priority            task_priority NOT NULL DEFAULT 'MEDIUM',
    assigned_to         BIGINT NOT NULL REFERENCES users(id),
    created_by          BIGINT NOT NULL REFERENCES users(id),
    deal_id             BIGINT REFERENCES deals(id),
    activity_report_id  BIGINT REFERENCES activity_reports(id),
    due_date            DATE NOT NULL,
    completed_at        TIMESTAMPTZ,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_tasks_assigned_status ON tasks(assigned_to, status);
CREATE INDEX idx_tasks_deal ON tasks(deal_id);
CREATE INDEX idx_tasks_due_date ON tasks(due_date);
CREATE INDEX idx_tasks_created_by ON tasks(created_by);

-- Backfill from existing activity reports
INSERT INTO tasks (title, status, priority, assigned_to, created_by, deal_id, activity_report_id, due_date, created_at)
SELECT
    ar.next_action,
    'OPEN'::task_status,
    'MEDIUM'::task_priority,
    d.assigned_agent_id,
    ar.agent_id,
    ar.deal_id,
    ar.id,
    COALESCE(ar.next_action_eta, CURRENT_DATE),
    ar.submission_datetime
FROM activity_reports ar
JOIN deals d ON ar.deal_id = d.id
WHERE ar.voided = false
  AND ar.next_action IS NOT NULL
  AND ar.next_action != ''
  AND ar.next_action != 'No further action'
  AND ar.id IN (
    SELECT DISTINCT ON (deal_id) id
    FROM activity_reports
    WHERE voided = false AND next_action IS NOT NULL AND next_action != ''
    ORDER BY deal_id, submission_datetime DESC
  );
