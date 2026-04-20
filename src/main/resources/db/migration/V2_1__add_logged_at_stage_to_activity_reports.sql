-- Add logged_at_stage column to track which stage a report was logged at
ALTER TABLE activity_reports ADD COLUMN logged_at_stage deal_stage;

-- Backfill existing reports: infer stage from stage_transitions timestamps
UPDATE activity_reports ar
SET logged_at_stage = COALESCE(
    (SELECT st.to_stage FROM stage_transitions st
     WHERE st.deal_id = ar.deal_id
       AND st.created_at <= ar.submission_datetime
     ORDER BY st.created_at DESC LIMIT 1),
    (SELECT d.current_stage FROM deals d WHERE d.id = ar.deal_id)
);

-- Default any remaining NULLs to STAGE_1
UPDATE activity_reports SET logged_at_stage = 'STAGE_1' WHERE logged_at_stage IS NULL;

-- Make NOT NULL after backfill
ALTER TABLE activity_reports ALTER COLUMN logged_at_stage SET NOT NULL;

-- Add index for stage-specific queries
CREATE INDEX idx_activity_reports_deal_stage_voided ON activity_reports(deal_id, logged_at_stage, voided);
