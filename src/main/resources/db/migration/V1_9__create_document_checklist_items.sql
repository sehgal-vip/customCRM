-- V1_9__create_document_checklist_items.sql
-- Document checklist items: configurable list of required documents per stage

CREATE TABLE document_checklist_items (
    id                BIGSERIAL PRIMARY KEY,
    document_name     VARCHAR(255) NOT NULL,
    requirement       doc_requirement NOT NULL,
    required_by_stage deal_stage NOT NULL,
    active            BOOLEAN NOT NULL DEFAULT TRUE,
    created_at        TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_checklist_items_active ON document_checklist_items (active);
