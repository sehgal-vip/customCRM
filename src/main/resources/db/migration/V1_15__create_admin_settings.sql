-- V1_15__create_admin_settings.sql
-- Admin settings: configurable system parameters

CREATE TABLE admin_settings (
    id            BIGSERIAL PRIMARY KEY,
    setting_type  admin_setting_type NOT NULL,
    setting_key   VARCHAR(100) NOT NULL,
    setting_value JSONB NOT NULL,
    updated_by    BIGINT REFERENCES users(id),
    updated_at    TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT uq_setting_type_key UNIQUE (setting_type, setting_key)
);
