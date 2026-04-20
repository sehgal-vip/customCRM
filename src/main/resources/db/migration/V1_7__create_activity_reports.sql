-- V1_7__create_activity_reports.sql
-- Activity reports: visit and interaction logs for deals

CREATE TABLE activity_reports (
    id                      BIGSERIAL PRIMARY KEY,
    deal_id                 BIGINT NOT NULL REFERENCES deals(id),
    agent_id                BIGINT NOT NULL REFERENCES users(id),
    template_type           template_type NOT NULL,
    activity_type           activity_type NOT NULL,
    interaction_datetime    TIMESTAMPTZ NOT NULL,
    submission_datetime     TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    contact_id              BIGINT NOT NULL REFERENCES contacts(id),
    contact_role            contact_role NOT NULL,
    duration                report_duration NOT NULL,

    -- Phase-specific data stored as JSONB (varies by template T1-T12)
    phase_specific_data     JSONB NOT NULL DEFAULT '{}',

    -- Footer fields
    buying_signals          TEXT[],
    objections              TEXT[],
    notes                   TEXT,
    next_action             TEXT NOT NULL,
    next_action_eta         DATE,
    next_action_owner       next_action_owner NOT NULL,

    -- Voiding (FRD Section 5.4)
    voided                  BOOLEAN NOT NULL DEFAULT FALSE,
    voided_by               BIGINT REFERENCES users(id),
    voided_reason           TEXT,
    voided_at               TIMESTAMPTZ,
    unvoided_by             BIGINT REFERENCES users(id),
    unvoided_at             TIMESTAMPTZ,

    -- Derived field for template selection optimization
    is_first_in_phase       BOOLEAN NOT NULL DEFAULT FALSE,

    created_at              TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_reports_deal ON activity_reports (deal_id);
CREATE INDEX idx_reports_agent ON activity_reports (agent_id);
CREATE INDEX idx_reports_deal_voided ON activity_reports (deal_id, voided);
CREATE INDEX idx_reports_submission_date ON activity_reports (submission_datetime);
CREATE INDEX idx_reports_agent_submission ON activity_reports (agent_id, submission_datetime);
CREATE INDEX idx_reports_deal_template ON activity_reports (deal_id, template_type, voided);
CREATE INDEX idx_reports_deal_activity_type ON activity_reports (deal_id, activity_type, voided);
CREATE INDEX idx_reports_next_action_eta ON activity_reports (next_action_eta) WHERE voided = FALSE;
