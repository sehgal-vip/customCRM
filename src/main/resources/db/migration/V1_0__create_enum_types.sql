-- V1_0__create_enum_types.sql
-- All PostgreSQL enum types for TurnoCRM

-- User role (FRD Section 2)
CREATE TYPE user_role AS ENUM ('AGENT', 'MANAGER');

-- User status (FRD Section 8)
CREATE TYPE user_status AS ENUM ('ACTIVE', 'DEACTIVATED');

-- Operator type (FRD Section 3.1)
CREATE TYPE operator_type AS ENUM ('PRIVATE_FLEET', 'GOVT_CONTRACT', 'SCHOOL_CORPORATE', 'MIXED');

-- Primary use case (FRD Section 3.1)
CREATE TYPE primary_use_case AS ENUM ('CITY_TRANSPORT', 'INTERCITY', 'SCHOOL_SHUTTLE', 'CORPORATE_SHUTTLE', 'LAST_MILE', 'MIXED');

-- Contact role (Activity Report Spec Section 3.1)
CREATE TYPE contact_role AS ENUM ('OWNER', 'FLEET_MANAGER', 'FINANCE_HEAD', 'DRIVER_SUPERVISOR', 'OTHER');

-- Lead source (FRD Section 5.2)
CREATE TYPE lead_source AS ENUM ('AGENT_FIELD', 'REFERRALS', 'INBOUND', 'COMPANY_LISTS');

-- Pipeline stage 1-9 (FRD Section 4)
CREATE TYPE deal_stage AS ENUM ('STAGE_1', 'STAGE_2', 'STAGE_3', 'STAGE_4', 'STAGE_5', 'STAGE_6', 'STAGE_7', 'STAGE_8', 'STAGE_9');

-- Stage 5 sub-status (FRD Section 4.1)
CREATE TYPE stage5_sub_status AS ENUM ('PROPOSAL_SENT', 'AWAITING_APPROVAL', 'NEGOTIATING');

-- Deal status (FRD Section 4, 4.4)
CREATE TYPE deal_status AS ENUM ('ACTIVE', 'ARCHIVED', 'COMPLETED');

-- Activity type (Activity Report Spec Section 2.1)
CREATE TYPE activity_type AS ENUM ('FIELD_VISIT', 'VIRTUAL');

-- Template type (Activity Report Spec Section 2.4)
CREATE TYPE template_type AS ENUM ('T1', 'T2', 'T3', 'T4', 'T5', 'T6', 'T7', 'T8', 'T9', 'T10', 'T11', 'T12');

-- Duration (Activity Report Spec Section 3.1)
CREATE TYPE report_duration AS ENUM ('UNDER_15_MIN', 'MIN_15_TO_30', 'MIN_30_TO_60', 'HRS_1_TO_2', 'OVER_2_HRS');

-- Document status (FRD Section 5.6)
CREATE TYPE doc_status AS ENUM ('NOT_STARTED', 'REQUESTED', 'RECEIVED', 'VERIFIED');

-- Document requirement level (FRD Section 5.6)
CREATE TYPE doc_requirement AS ENUM ('MANDATORY', 'OPTIONAL');

-- Pricing submission status (FRD Section 6, Appendix B.3)
CREATE TYPE pricing_status AS ENUM ('SUBMITTED', 'APPROVED', 'REJECTED', 'SUPERSEDED');

-- Notification event type (FRD Section 7.1)
CREATE TYPE notification_event_type AS ENUM (
    'PRICING_APPROVAL_REQUESTED',
    'PRICING_DECISION',
    'FOLLOW_UP_DUE_TODAY',
    'FOLLOW_UP_OVERDUE',
    'REGRESSION_REQUESTED',
    'REGRESSION_DECISION',
    'STALE_DEAL_ALERT',
    'NO_NEXT_ACTION_SET',
    'DEAL_ARCHIVED',
    'DEAL_REACTIVATED',
    'DEAL_ASSIGNED_NEW_OWNER',
    'DEAL_REASSIGNED_PREVIOUS_OWNER'
);

-- Notification priority (FRD Section 7.2)
CREATE TYPE notification_priority AS ENUM ('HIGH', 'MEDIUM', 'LOW');

-- Audit entity type
CREATE TYPE audit_entity_type AS ENUM (
    'DEAL', 'OPERATOR', 'CONTACT', 'ACTIVITY_REPORT', 'PRICING_SUBMISSION',
    'USER', 'ADMIN_SETTING', 'TAXONOMY_ITEM', 'DOCUMENT_CHECKLIST_ITEM', 'DEAL_DOCUMENT'
);

-- Audit action
CREATE TYPE audit_action AS ENUM (
    'CREATE', 'UPDATE', 'STAGE_FORWARD', 'STAGE_BACKWARD',
    'ARCHIVE', 'REACTIVATE', 'COMPLETE', 'REOPEN', 'VOID', 'UNVOID',
    'SUBMIT', 'APPROVE', 'REJECT',
    'BACKFILL_REQUEST', 'BACKFILL_APPROVE', 'BACKFILL_REJECT',
    'REGRESSION_REQUEST', 'REGRESSION_APPROVE', 'REGRESSION_REJECT',
    'DEACTIVATE'
);

-- Stage transition type (FRD Section 3.2)
CREATE TYPE transition_type AS ENUM ('FORWARD', 'BACKWARD', 'ARCHIVE', 'REACTIVATE', 'COMPLETE', 'REOPEN', 'BACKFILL');

-- Taxonomy type
CREATE TYPE taxonomy_type AS ENUM ('OBJECTION', 'BUYING_SIGNAL', 'ATTACHMENT_TAG', 'LOST_REASON');

-- Approval status for backfill/regression requests
CREATE TYPE approval_status AS ENUM ('PENDING', 'APPROVED', 'REJECTED');

-- Next action owner (Activity Report Spec Section 3.2)
CREATE TYPE next_action_owner AS ENUM ('SELF', 'MANAGER', 'OPERATOR', 'OPS_TEAM');

-- Admin setting type
CREATE TYPE admin_setting_type AS ENUM ('STALE_THRESHOLD', 'NOTIFICATION_TIMING', 'GENERAL');
