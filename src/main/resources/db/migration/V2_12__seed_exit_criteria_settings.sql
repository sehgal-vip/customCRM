-- Seed exit criteria settings per stage
-- STAGE_1 defaults to false (no activity required to advance), all others true
INSERT INTO admin_settings (setting_type, setting_key, setting_value, updated_at) VALUES
  ('EXIT_CRITERIA', 'STAGE_1', '{"activityRequired": false}', NOW()),
  ('EXIT_CRITERIA', 'STAGE_2', '{"activityRequired": true}', NOW()),
  ('EXIT_CRITERIA', 'STAGE_3', '{"activityRequired": true}', NOW()),
  ('EXIT_CRITERIA', 'STAGE_4', '{"activityRequired": true}', NOW()),
  ('EXIT_CRITERIA', 'STAGE_5', '{"activityRequired": true}', NOW()),
  ('EXIT_CRITERIA', 'STAGE_6', '{"activityRequired": true}', NOW()),
  ('EXIT_CRITERIA', 'STAGE_7', '{"activityRequired": true}', NOW()),
  ('EXIT_CRITERIA', 'STAGE_8', '{"activityRequired": true}', NOW())
ON CONFLICT (setting_type, setting_key) DO NOTHING;
