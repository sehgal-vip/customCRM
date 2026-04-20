-- V1_19__create_backfill_requests.sql
-- Backfill requests: agent requests to backdate a deal's start stage

CREATE TABLE backfill_requests (
    id                  BIGSERIAL PRIMARY KEY,
    deal_id             BIGINT NOT NULL REFERENCES deals(id),
    requested_by        BIGINT NOT NULL REFERENCES users(id),
    target_stage        deal_stage NOT NULL,
    context             TEXT NOT NULL,
    original_start_date DATE NOT NULL,
    status              approval_status NOT NULL DEFAULT 'PENDING',
    reviewed_by         BIGINT REFERENCES users(id),
    reviewed_at         TIMESTAMPTZ,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_backfill_deal ON backfill_requests (deal_id);
CREATE INDEX idx_backfill_status ON backfill_requests (status);
