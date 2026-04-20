-- V1_1__create_regions.sql
-- Regions table for geographic territory management

CREATE TABLE regions (
    id         BIGSERIAL PRIMARY KEY,
    name       VARCHAR(100) NOT NULL UNIQUE,
    active     BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_regions_active ON regions (active);
