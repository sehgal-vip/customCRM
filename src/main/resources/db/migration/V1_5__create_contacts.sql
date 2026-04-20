-- V1_5__create_contacts.sql
-- Contacts table: people associated with operators

CREATE TABLE contacts (
    id          BIGSERIAL PRIMARY KEY,
    operator_id BIGINT NOT NULL REFERENCES operators(id) ON DELETE CASCADE,
    name        VARCHAR(255) NOT NULL,
    role        contact_role NOT NULL,
    mobile      VARCHAR(20),
    email       VARCHAR(255),
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    -- At least one of mobile or email required (FRD Section 3.1)
    CONSTRAINT chk_contact_phone_or_email CHECK (mobile IS NOT NULL OR email IS NOT NULL)
);

CREATE INDEX idx_contacts_operator ON contacts (operator_id);
CREATE INDEX idx_contacts_name ON contacts (name);
CREATE INDEX idx_contacts_mobile ON contacts (mobile);

-- Full-text search on contacts
CREATE INDEX idx_contacts_fts ON contacts USING GIN (
    to_tsvector('english', coalesce(name, '') || ' ' || coalesce(mobile, '') || ' ' || coalesce(email, ''))
);
