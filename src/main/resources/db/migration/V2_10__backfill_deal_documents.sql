-- Backfill deal_documents for existing deals that are missing checklist entries.
-- This happens when checklist items are added after deals were created.
INSERT INTO deal_documents (deal_id, checklist_item_id, status, updated_at)
SELECT d.id, ci.id, 'NOT_STARTED', NOW()
FROM deals d
CROSS JOIN document_checklist_items ci
WHERE ci.active = true
  AND NOT EXISTS (
    SELECT 1 FROM deal_documents dd
    WHERE dd.deal_id = d.id AND dd.checklist_item_id = ci.id
  );
