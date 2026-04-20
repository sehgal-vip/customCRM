-- V1_4__create_operators.sql
-- Operators table: bus fleet companies/individuals

CREATE TABLE operators (
    id                      BIGSERIAL PRIMARY KEY,
    company_name            VARCHAR(255) NOT NULL,
    phone                   VARCHAR(20),
    email                   VARCHAR(255),
    region_id               BIGINT REFERENCES regions(id),
    operator_type           operator_type,
    referral_source         VARCHAR(255),
    referred_by_operator_id BIGINT REFERENCES operators(id),
    fleet_size              INTEGER,
    num_routes              INTEGER,
    primary_use_case        primary_use_case,
    created_by              BIGINT NOT NULL REFERENCES users(id),
    created_at              TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT uq_operator_name_phone UNIQUE (company_name, phone)
);

CREATE INDEX idx_operators_company_name ON operators (company_name);
CREATE INDEX idx_operators_phone ON operators (phone);
CREATE INDEX idx_operators_region ON operators (region_id);
CREATE INDEX idx_operators_referred_by ON operators (referred_by_operator_id);
CREATE INDEX idx_operators_created_by ON operators (created_by);

-- Full-text search index on operators
CREATE INDEX idx_operators_fts ON operators USING GIN (
    to_tsvector('english', coalesce(company_name, '') || ' ' || coalesce(phone, '') || ' ' || coalesce(email, ''))
);
