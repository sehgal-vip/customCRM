-- V1_11__create_pricing_submissions.sql
-- Pricing submissions: agent submits pricing for manager approval

CREATE TABLE pricing_submissions (
    id                          BIGSERIAL PRIMARY KEY,
    deal_id                     BIGINT NOT NULL REFERENCES deals(id),
    submitted_by                BIGINT NOT NULL REFERENCES users(id),

    -- Agent's submission (FRD Appendix B.1)
    services_selected           TEXT[] NOT NULL,
    monthly_km_commitment       NUMERIC(12, 2) NOT NULL,
    price_per_km                NUMERIC(10, 2) NOT NULL,

    -- Auto-calculated (Appendix B.2)
    monthly_value_per_vehicle   NUMERIC(14, 2) GENERATED ALWAYS AS (monthly_km_commitment * price_per_km) STORED,

    -- Manager's edits (FRD Section 6)
    manager_services_selected   TEXT[],
    manager_monthly_km          NUMERIC(12, 2),
    manager_price_per_km        NUMERIC(10, 2),

    -- Approval status
    status                      pricing_status NOT NULL DEFAULT 'SUBMITTED',
    reviewed_by                 BIGINT REFERENCES users(id),
    reviewed_at                 TIMESTAMPTZ,
    rejection_note              TEXT,

    created_at                  TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    -- Bus lease must be in services_selected
    CONSTRAINT chk_bus_lease CHECK ('BUS_LEASE' = ANY(services_selected))
);

CREATE INDEX idx_pricing_deal ON pricing_submissions (deal_id);
CREATE INDEX idx_pricing_deal_status ON pricing_submissions (deal_id, status);
CREATE INDEX idx_pricing_submitted_by ON pricing_submissions (submitted_by);
