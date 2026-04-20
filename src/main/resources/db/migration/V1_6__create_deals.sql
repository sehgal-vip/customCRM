-- V1_6__create_deals.sql
-- Deals table: leasing transactions progressing through the 9-stage pipeline

CREATE TABLE deals (
    id                      BIGSERIAL PRIMARY KEY,
    name                    VARCHAR(100) NOT NULL UNIQUE,
    operator_id             BIGINT NOT NULL REFERENCES operators(id),
    assigned_agent_id       BIGINT NOT NULL REFERENCES users(id),
    fleet_size              INTEGER,
    estimated_monthly_value NUMERIC(14, 2),
    lead_source             lead_source NOT NULL,
    current_stage           deal_stage NOT NULL DEFAULT 'STAGE_1',
    sub_status              stage5_sub_status,
    status                  deal_status NOT NULL DEFAULT 'ACTIVE',
    archived_reason         VARCHAR(100),
    archived_reason_text    TEXT,
    reopened                BOOLEAN NOT NULL DEFAULT FALSE,
    backfilled              BOOLEAN NOT NULL DEFAULT FALSE,
    backfill_approved_by    BIGINT REFERENCES users(id),
    original_start_date     DATE,
    source_event_id         VARCHAR(255) UNIQUE,
    created_at              TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    -- Sub-status only valid for Stage 5 (FRD Section 4.1)
    CONSTRAINT chk_sub_status_stage5 CHECK (
        sub_status IS NULL OR current_stage = 'STAGE_5'
    )
);

CREATE INDEX idx_deals_assigned_agent ON deals (assigned_agent_id);
CREATE INDEX idx_deals_operator ON deals (operator_id);
CREATE INDEX idx_deals_current_stage ON deals (current_stage);
CREATE INDEX idx_deals_status ON deals (status);
CREATE INDEX idx_deals_stage_status ON deals (current_stage, status);
CREATE INDEX idx_deals_agent_status ON deals (assigned_agent_id, status);
CREATE INDEX idx_deals_reopened ON deals (reopened) WHERE reopened = TRUE;
CREATE INDEX idx_deals_source_event_id ON deals (source_event_id) WHERE source_event_id IS NOT NULL;
CREATE INDEX idx_deals_created_at ON deals (created_at);

-- Full-text search on deals
CREATE INDEX idx_deals_fts ON deals USING GIN (
    to_tsvector('english', coalesce(name, ''))
);
