-- V1_20__create_regression_requests.sql
-- Regression requests: agent requests to move a deal backward one stage

CREATE TABLE regression_requests (
    id           BIGSERIAL PRIMARY KEY,
    deal_id      BIGINT NOT NULL REFERENCES deals(id),
    requested_by BIGINT NOT NULL REFERENCES users(id),
    from_stage   deal_stage NOT NULL,
    to_stage     deal_stage NOT NULL,
    reason       TEXT NOT NULL,
    status       approval_status NOT NULL DEFAULT 'PENDING',
    reviewed_by  BIGINT REFERENCES users(id),
    reviewed_at  TIMESTAMPTZ,
    created_at   TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_regression_deal ON regression_requests (deal_id);
CREATE INDEX idx_regression_status ON regression_requests (status);
