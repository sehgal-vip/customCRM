-- V1_16__create_taxonomy_items.sql
-- Taxonomy items: admin-configurable dropdown values

CREATE TABLE taxonomy_items (
    id            BIGSERIAL PRIMARY KEY,
    taxonomy_type taxonomy_type NOT NULL,
    value         VARCHAR(255) NOT NULL,
    active        BOOLEAN NOT NULL DEFAULT TRUE,
    created_at    TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT uq_taxonomy_type_value UNIQUE (taxonomy_type, value)
);

CREATE INDEX idx_taxonomy_type_active ON taxonomy_items (taxonomy_type, active);
