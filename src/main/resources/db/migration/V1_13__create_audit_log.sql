-- V1_13__create_audit_log.sql
-- Audit log: immutable record of all entity changes

CREATE TABLE audit_log (
    id          BIGSERIAL PRIMARY KEY,
    entity_type audit_entity_type NOT NULL,
    entity_id   BIGINT NOT NULL,
    action      audit_action NOT NULL,
    actor_id    BIGINT NOT NULL REFERENCES users(id),
    details     JSONB NOT NULL DEFAULT '{}',
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_audit_entity ON audit_log (entity_type, entity_id);
CREATE INDEX idx_audit_actor ON audit_log (actor_id);
CREATE INDEX idx_audit_created ON audit_log (created_at);
CREATE INDEX idx_audit_entity_created ON audit_log (entity_type, entity_id, created_at DESC);
