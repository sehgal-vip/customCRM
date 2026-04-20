-- V1_10__create_deal_documents.sql
-- Deal documents: tracks document collection status per deal

CREATE TABLE deal_documents (
    id                BIGSERIAL PRIMARY KEY,
    deal_id           BIGINT NOT NULL REFERENCES deals(id) ON DELETE CASCADE,
    checklist_item_id BIGINT NOT NULL REFERENCES document_checklist_items(id),
    status            doc_status NOT NULL DEFAULT 'NOT_STARTED',
    file_key          VARCHAR(1000),
    uploaded_at       TIMESTAMPTZ,
    updated_at        TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT uq_deal_checklist_item UNIQUE (deal_id, checklist_item_id)
);

CREATE INDEX idx_deal_documents_deal ON deal_documents (deal_id);
CREATE INDEX idx_deal_documents_status ON deal_documents (deal_id, status);
