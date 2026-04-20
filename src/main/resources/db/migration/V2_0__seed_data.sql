-- V2_0__seed_data.sql
-- Seed data for TurnoCRM: regions, users, documents, settings, taxonomies, notification preferences

-- 1. Regions
INSERT INTO regions (name) VALUES
    ('Delhi NCR'),
    ('Mumbai'),
    ('Bangalore'),
    ('Hyderabad'),
    ('Chennai'),
    ('Pune');

-- 2. Users
INSERT INTO users (email, name, role, status) VALUES
    ('manager@turno.com', 'Vipul Sehgal', 'MANAGER', 'ACTIVE'),
    ('agent1@turno.com', 'Rahul Sharma', 'AGENT', 'ACTIVE'),
    ('agent2@turno.com', 'Priya Patel', 'AGENT', 'ACTIVE'),
    ('agent3@turno.com', 'Amit Kumar', 'AGENT', 'ACTIVE'),
    ('agent4@turno.com', 'Sneha Reddy', 'AGENT', 'ACTIVE'),
    ('agent5@turno.com', 'Vikram Singh', 'AGENT', 'ACTIVE');

-- 3. User-region mappings
-- Manager gets all regions
INSERT INTO user_regions (user_id, region_id)
SELECT u.id, r.id
FROM users u, regions r
WHERE u.email = 'manager@turno.com';

-- Agent 1: Delhi NCR, Mumbai
INSERT INTO user_regions (user_id, region_id)
SELECT u.id, r.id
FROM users u, regions r
WHERE u.email = 'agent1@turno.com' AND r.name IN ('Delhi NCR', 'Mumbai');

-- Agent 2: Mumbai, Pune
INSERT INTO user_regions (user_id, region_id)
SELECT u.id, r.id
FROM users u, regions r
WHERE u.email = 'agent2@turno.com' AND r.name IN ('Mumbai', 'Pune');

-- Agent 3: Bangalore, Hyderabad
INSERT INTO user_regions (user_id, region_id)
SELECT u.id, r.id
FROM users u, regions r
WHERE u.email = 'agent3@turno.com' AND r.name IN ('Bangalore', 'Hyderabad');

-- Agent 4: Hyderabad, Chennai
INSERT INTO user_regions (user_id, region_id)
SELECT u.id, r.id
FROM users u, regions r
WHERE u.email = 'agent4@turno.com' AND r.name IN ('Hyderabad', 'Chennai');

-- Agent 5: Delhi NCR, Bangalore
INSERT INTO user_regions (user_id, region_id)
SELECT u.id, r.id
FROM users u, regions r
WHERE u.email = 'agent5@turno.com' AND r.name IN ('Delhi NCR', 'Bangalore');

-- 4. Document checklist items (10 defaults from FRD)
INSERT INTO document_checklist_items (document_name, requirement, required_by_stage) VALUES
    ('PAN Card', 'MANDATORY', 'STAGE_6'),
    ('GST Certificate', 'MANDATORY', 'STAGE_6'),
    ('Certificate of Incorporation', 'MANDATORY', 'STAGE_6'),
    ('Bank Statement', 'MANDATORY', 'STAGE_7'),
    ('Audited Financials', 'MANDATORY', 'STAGE_7'),
    ('Route Permit', 'MANDATORY', 'STAGE_8'),
    ('Vehicle Insurance', 'MANDATORY', 'STAGE_8'),
    ('Board Resolution', 'OPTIONAL', 'STAGE_7'),
    ('Address Proof', 'MANDATORY', 'STAGE_6'),
    ('Signed LOI', 'MANDATORY', 'STAGE_8');

-- 5. Stale thresholds (admin_settings): 14 days for Stages 1-5, 21 days for Stages 6-9
INSERT INTO admin_settings (setting_type, setting_key, setting_value) VALUES
    ('STALE_THRESHOLD', 'STAGE_1', '{"days": 14}'),
    ('STALE_THRESHOLD', 'STAGE_2', '{"days": 14}'),
    ('STALE_THRESHOLD', 'STAGE_3', '{"days": 14}'),
    ('STALE_THRESHOLD', 'STAGE_4', '{"days": 14}'),
    ('STALE_THRESHOLD', 'STAGE_5', '{"days": 14}'),
    ('STALE_THRESHOLD', 'STAGE_6', '{"days": 21}'),
    ('STALE_THRESHOLD', 'STAGE_7', '{"days": 21}'),
    ('STALE_THRESHOLD', 'STAGE_8', '{"days": 21}'),
    ('STALE_THRESHOLD', 'STAGE_9', '{"days": 21}');

-- 6. Default taxonomy items

-- 6a. Objections
INSERT INTO taxonomy_items (taxonomy_type, value) VALUES
    ('OBJECTION', 'Price too high'),
    ('OBJECTION', 'Lease terms unfavorable'),
    ('OBJECTION', 'Range anxiety'),
    ('OBJECTION', 'Charging infrastructure concerns'),
    ('OBJECTION', 'Prefers diesel/CNG'),
    ('OBJECTION', 'Competitor offering better deal'),
    ('OBJECTION', 'Wants to buy not lease'),
    ('OBJECTION', 'Timing not right'),
    ('OBJECTION', 'Decision maker unavailable'),
    ('OBJECTION', 'Maintenance concerns'),
    ('OBJECTION', 'Regulatory uncertainty'),
    ('OBJECTION', 'Resale/residual value concerns');

-- 6b. Buying signals
INSERT INTO taxonomy_items (taxonomy_type, value) VALUES
    ('BUYING_SIGNAL', 'Asking about delivery timelines'),
    ('BUYING_SIGNAL', 'Requesting references/case studies'),
    ('BUYING_SIGNAL', 'Discussing fleet replacement schedule'),
    ('BUYING_SIGNAL', 'Asking about financing details'),
    ('BUYING_SIGNAL', 'Involving finance/procurement team'),
    ('BUYING_SIGNAL', 'Requesting site visit/demo'),
    ('BUYING_SIGNAL', 'Comparing specific bus models'),
    ('BUYING_SIGNAL', 'Asking about government subsidies'),
    ('BUYING_SIGNAL', 'Sharing competitor quotes'),
    ('BUYING_SIGNAL', 'Introducing us to other operators');

-- 6c. Attachment tags
INSERT INTO taxonomy_items (taxonomy_type, value) VALUES
    ('ATTACHMENT_TAG', 'Site Photos'),
    ('ATTACHMENT_TAG', 'Route Maps'),
    ('ATTACHMENT_TAG', 'Competitor Materials'),
    ('ATTACHMENT_TAG', 'Fleet Documents'),
    ('ATTACHMENT_TAG', 'Depot Photos'),
    ('ATTACHMENT_TAG', 'Charging Infra'),
    ('ATTACHMENT_TAG', 'Meeting Notes'),
    ('ATTACHMENT_TAG', 'Proposal/Quote'),
    ('ATTACHMENT_TAG', 'Contract Draft'),
    ('ATTACHMENT_TAG', 'Other');

-- 6d. Lost reasons
INSERT INTO taxonomy_items (taxonomy_type, value) VALUES
    ('LOST_REASON', 'Chose competitor'),
    ('LOST_REASON', 'Price/terms unacceptable'),
    ('LOST_REASON', 'Budget unavailable'),
    ('LOST_REASON', 'Timing not right'),
    ('LOST_REASON', 'Went with purchase'),
    ('LOST_REASON', 'Regulatory/permit issue'),
    ('LOST_REASON', 'No response/went dark'),
    ('LOST_REASON', 'Internal decision'),
    ('LOST_REASON', 'Other');

-- 7. Notification preferences: all event types enabled for both AGENT and MANAGER roles
INSERT INTO notification_preferences (role, event_type, enabled)
SELECT r.role, e.event_type, TRUE
FROM (VALUES ('AGENT'::user_role), ('MANAGER'::user_role)) AS r(role)
CROSS JOIN (VALUES
    ('PRICING_APPROVAL_REQUESTED'::notification_event_type),
    ('PRICING_DECISION'::notification_event_type),
    ('FOLLOW_UP_DUE_TODAY'::notification_event_type),
    ('FOLLOW_UP_OVERDUE'::notification_event_type),
    ('REGRESSION_REQUESTED'::notification_event_type),
    ('REGRESSION_DECISION'::notification_event_type),
    ('STALE_DEAL_ALERT'::notification_event_type),
    ('NO_NEXT_ACTION_SET'::notification_event_type),
    ('DEAL_ARCHIVED'::notification_event_type),
    ('DEAL_REACTIVATED'::notification_event_type),
    ('DEAL_ASSIGNED_NEW_OWNER'::notification_event_type),
    ('DEAL_REASSIGNED_PREVIOUS_OWNER'::notification_event_type)
) AS e(event_type);
