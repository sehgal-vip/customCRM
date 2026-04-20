-- V1_3__create_user_regions.sql
-- Junction table linking users to regions

CREATE TABLE user_regions (
    user_id   BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    region_id BIGINT NOT NULL REFERENCES regions(id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, region_id)
);

CREATE INDEX idx_user_regions_region ON user_regions (region_id);
