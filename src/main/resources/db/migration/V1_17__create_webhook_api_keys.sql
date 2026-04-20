-- V1_17__create_webhook_api_keys.sql
-- Webhook API keys: authentication for external lead ingestion

CREATE TABLE webhook_api_keys (
    id          BIGSERIAL PRIMARY KEY,
    key_hash    VARCHAR(128) NOT NULL UNIQUE,
    description VARCHAR(255),
    created_by  BIGINT REFERENCES users(id),
    active      BOOLEAN NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    revoked_at  TIMESTAMPTZ
);

CREATE INDEX idx_webhook_keys_hash ON webhook_api_keys (key_hash) WHERE active = TRUE;
