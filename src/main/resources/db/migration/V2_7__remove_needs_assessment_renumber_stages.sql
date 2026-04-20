-- Remove STAGE_3 (Needs Assessment) and renumber STAGE_4-9 to STAGE_3-8

-- Step 1: Drop constraints that reference the deal_stage enum
ALTER TABLE deals DROP CONSTRAINT IF EXISTS chk_sub_status_stage5;

-- Step 2: Drop default on deals.current_stage
ALTER TABLE deals ALTER COLUMN current_stage DROP DEFAULT;

-- Step 3: Convert ALL deal_stage columns to text
ALTER TABLE deals ALTER COLUMN current_stage TYPE text USING current_stage::text;
ALTER TABLE activity_reports ALTER COLUMN logged_at_stage TYPE text USING logged_at_stage::text;
ALTER TABLE stage_transitions ALTER COLUMN from_stage TYPE text USING from_stage::text;
ALTER TABLE stage_transitions ALTER COLUMN to_stage TYPE text USING to_stage::text;
ALTER TABLE backfill_requests ALTER COLUMN target_stage TYPE text USING target_stage::text;
ALTER TABLE regression_requests ALTER COLUMN from_stage TYPE text USING from_stage::text;
ALTER TABLE regression_requests ALTER COLUMN to_stage TYPE text USING to_stage::text;
ALTER TABLE document_checklist_items ALTER COLUMN required_by_stage TYPE text USING required_by_stage::text;

-- Step 4: Drop old enum
DROP TYPE deal_stage;

-- Step 5: Move STAGE_3 → STAGE_2
UPDATE deals SET current_stage = 'STAGE_2' WHERE current_stage = 'STAGE_3';
UPDATE activity_reports SET logged_at_stage = 'STAGE_2' WHERE logged_at_stage = 'STAGE_3';
UPDATE stage_transitions SET from_stage = 'STAGE_2' WHERE from_stage = 'STAGE_3';
UPDATE stage_transitions SET to_stage = 'STAGE_2' WHERE to_stage = 'STAGE_3';
UPDATE backfill_requests SET target_stage = 'STAGE_2' WHERE target_stage = 'STAGE_3';
UPDATE regression_requests SET from_stage = 'STAGE_2' WHERE from_stage = 'STAGE_3';
UPDATE regression_requests SET to_stage = 'STAGE_2' WHERE to_stage = 'STAGE_3';
UPDATE document_checklist_items SET required_by_stage = 'STAGE_2' WHERE required_by_stage = 'STAGE_3';

-- Step 6: Renumber using CASE (all text now, no enum conflicts)
UPDATE deals SET current_stage = CASE current_stage
  WHEN 'STAGE_4' THEN 'STAGE_3' WHEN 'STAGE_5' THEN 'STAGE_4' WHEN 'STAGE_6' THEN 'STAGE_5'
  WHEN 'STAGE_7' THEN 'STAGE_6' WHEN 'STAGE_8' THEN 'STAGE_7' WHEN 'STAGE_9' THEN 'STAGE_8'
  ELSE current_stage END
WHERE current_stage IN ('STAGE_4','STAGE_5','STAGE_6','STAGE_7','STAGE_8','STAGE_9');

UPDATE activity_reports SET logged_at_stage = CASE logged_at_stage
  WHEN 'STAGE_4' THEN 'STAGE_3' WHEN 'STAGE_5' THEN 'STAGE_4' WHEN 'STAGE_6' THEN 'STAGE_5'
  WHEN 'STAGE_7' THEN 'STAGE_6' WHEN 'STAGE_8' THEN 'STAGE_7' WHEN 'STAGE_9' THEN 'STAGE_8'
  ELSE logged_at_stage END
WHERE logged_at_stage IN ('STAGE_4','STAGE_5','STAGE_6','STAGE_7','STAGE_8','STAGE_9');

UPDATE stage_transitions SET
  from_stage = CASE from_stage
    WHEN 'STAGE_4' THEN 'STAGE_3' WHEN 'STAGE_5' THEN 'STAGE_4' WHEN 'STAGE_6' THEN 'STAGE_5'
    WHEN 'STAGE_7' THEN 'STAGE_6' WHEN 'STAGE_8' THEN 'STAGE_7' WHEN 'STAGE_9' THEN 'STAGE_8'
    ELSE from_stage END,
  to_stage = CASE to_stage
    WHEN 'STAGE_4' THEN 'STAGE_3' WHEN 'STAGE_5' THEN 'STAGE_4' WHEN 'STAGE_6' THEN 'STAGE_5'
    WHEN 'STAGE_7' THEN 'STAGE_6' WHEN 'STAGE_8' THEN 'STAGE_7' WHEN 'STAGE_9' THEN 'STAGE_8'
    ELSE to_stage END
WHERE from_stage IN ('STAGE_4','STAGE_5','STAGE_6','STAGE_7','STAGE_8','STAGE_9')
   OR to_stage IN ('STAGE_4','STAGE_5','STAGE_6','STAGE_7','STAGE_8','STAGE_9');

UPDATE backfill_requests SET target_stage = CASE target_stage
  WHEN 'STAGE_4' THEN 'STAGE_3' WHEN 'STAGE_5' THEN 'STAGE_4' WHEN 'STAGE_6' THEN 'STAGE_5'
  WHEN 'STAGE_7' THEN 'STAGE_6' WHEN 'STAGE_8' THEN 'STAGE_7' WHEN 'STAGE_9' THEN 'STAGE_8'
  ELSE target_stage END
WHERE target_stage IN ('STAGE_4','STAGE_5','STAGE_6','STAGE_7','STAGE_8','STAGE_9');

UPDATE regression_requests SET
  from_stage = CASE from_stage
    WHEN 'STAGE_4' THEN 'STAGE_3' WHEN 'STAGE_5' THEN 'STAGE_4' WHEN 'STAGE_6' THEN 'STAGE_5'
    WHEN 'STAGE_7' THEN 'STAGE_6' WHEN 'STAGE_8' THEN 'STAGE_7' WHEN 'STAGE_9' THEN 'STAGE_8'
    ELSE from_stage END,
  to_stage = CASE to_stage
    WHEN 'STAGE_4' THEN 'STAGE_3' WHEN 'STAGE_5' THEN 'STAGE_4' WHEN 'STAGE_6' THEN 'STAGE_5'
    WHEN 'STAGE_7' THEN 'STAGE_6' WHEN 'STAGE_8' THEN 'STAGE_7' WHEN 'STAGE_9' THEN 'STAGE_8'
    ELSE to_stage END
WHERE from_stage IN ('STAGE_4','STAGE_5','STAGE_6','STAGE_7','STAGE_8','STAGE_9')
   OR to_stage IN ('STAGE_4','STAGE_5','STAGE_6','STAGE_7','STAGE_8','STAGE_9');

UPDATE document_checklist_items SET required_by_stage = CASE required_by_stage
  WHEN 'STAGE_4' THEN 'STAGE_3' WHEN 'STAGE_5' THEN 'STAGE_4' WHEN 'STAGE_6' THEN 'STAGE_5'
  WHEN 'STAGE_7' THEN 'STAGE_6' WHEN 'STAGE_8' THEN 'STAGE_7' WHEN 'STAGE_9' THEN 'STAGE_8'
  ELSE required_by_stage END
WHERE required_by_stage IN ('STAGE_4','STAGE_5','STAGE_6','STAGE_7','STAGE_8','STAGE_9');

-- Step 7: Create new enum with 8 values
CREATE TYPE deal_stage AS ENUM ('STAGE_1', 'STAGE_2', 'STAGE_3', 'STAGE_4', 'STAGE_5', 'STAGE_6', 'STAGE_7', 'STAGE_8');

-- Step 8: Convert columns back to enum
ALTER TABLE deals ALTER COLUMN current_stage TYPE deal_stage USING current_stage::deal_stage;
ALTER TABLE deals ALTER COLUMN current_stage SET DEFAULT 'STAGE_1';
ALTER TABLE activity_reports ALTER COLUMN logged_at_stage TYPE deal_stage USING logged_at_stage::deal_stage;
ALTER TABLE stage_transitions ALTER COLUMN from_stage TYPE deal_stage USING from_stage::deal_stage;
ALTER TABLE stage_transitions ALTER COLUMN to_stage TYPE deal_stage USING to_stage::deal_stage;
ALTER TABLE backfill_requests ALTER COLUMN target_stage TYPE deal_stage USING target_stage::deal_stage;
ALTER TABLE regression_requests ALTER COLUMN from_stage TYPE deal_stage USING from_stage::deal_stage;
ALTER TABLE regression_requests ALTER COLUMN to_stage TYPE deal_stage USING to_stage::deal_stage;
ALTER TABLE document_checklist_items ALTER COLUMN required_by_stage TYPE deal_stage USING required_by_stage::deal_stage;

-- Step 9: Recreate constraint (STAGE_4 is now Closure of Commercials, was STAGE_5)
ALTER TABLE deals ADD CONSTRAINT chk_sub_status_stage4
  CHECK (sub_status IS NULL OR current_stage = 'STAGE_4'::deal_stage);
