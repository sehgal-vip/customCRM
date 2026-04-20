-- V1_14__create_stage_transitions.sql
-- Stage transitions: records every deal stage change

CREATE TABLE stage_transitions (
    id              BIGSERIAL PRIMARY KEY,
    deal_id         BIGINT NOT NULL REFERENCES deals(id),
    from_stage      deal_stage,
    to_stage        deal_stage NOT NULL,
    transition_type transition_type NOT NULL,
    actor_id        BIGINT NOT NULL REFERENCES users(id),
    reason          TEXT,
    approved_by     BIGINT REFERENCES users(id),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_stage_transitions_deal ON stage_transitions (deal_id);
CREATE INDEX idx_stage_transitions_deal_created ON stage_transitions (deal_id, created_at DESC);
