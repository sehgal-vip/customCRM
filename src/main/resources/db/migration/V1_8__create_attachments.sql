-- V1_8__create_attachments.sql
-- Attachments: files uploaded with activity reports

CREATE TABLE attachments (
    id                 BIGSERIAL PRIMARY KEY,
    activity_report_id BIGINT NOT NULL REFERENCES activity_reports(id) ON DELETE CASCADE,
    file_name          VARCHAR(500) NOT NULL,
    file_key           VARCHAR(1000) NOT NULL,
    file_size          BIGINT NOT NULL,
    category_tag       VARCHAR(100) NOT NULL,
    uploaded_at        TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    -- 50MB max file size
    CONSTRAINT chk_file_size CHECK (file_size > 0 AND file_size <= 52428800)
);

CREATE INDEX idx_attachments_report ON attachments (activity_report_id);
