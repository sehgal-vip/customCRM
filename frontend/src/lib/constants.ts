export const STAGE_NAMES: Record<string, string> = {
  STAGE_1: "Lead Captured",
  STAGE_2: "First Contact",
  STAGE_3: "Qualified Lead",
  STAGE_4: "Closure of Commercials",
  STAGE_5: "Token Received",
  STAGE_6: "Documentation",
  STAGE_7: "Lease Signing",
  STAGE_8: "Vehicle Delivery",
};

export const PHASE_STAGES = {
  EARLY: ["STAGE_1", "STAGE_2"],
  COMMERCIAL: ["STAGE_3", "STAGE_4"],
  CLOSURE: ["STAGE_5", "STAGE_6", "STAGE_7", "STAGE_8"],
};

export const PHASE_NAMES: Record<string, string> = {
  EARLY: "Early",
  COMMERCIAL: "Commercial",
  CLOSURE: "Closure",
};

export const PHASE_ORDER = ["EARLY", "COMMERCIAL", "CLOSURE"] as const;

export const LOST_REASONS = [
  "Chose competitor",
  "Price/terms unacceptable",
  "Budget unavailable",
  "Timing not right",
  "Went with purchase",
  "Regulatory/permit issue",
  "No response/went dark",
  "Internal decision",
  "Other",
] as const;

export const LEAD_SOURCES = [
  "Agent Field",
  "Referrals",
  "Inbound",
  "Company Lists",
] as const;

export const SUB_STATUS_COLORS: Record<string, { bg: string; text: string }> = {
  PROPOSAL_SENT: { bg: "bg-blue-50", text: "text-blue-700" },
  AWAITING_APPROVAL: { bg: "bg-amber-50", text: "text-amber-700" },
  NEGOTIATING: { bg: "bg-green-50", text: "text-green-700" },
};

export const SUB_STATUS_LABELS: Record<string, string> = {
  PROPOSAL_SENT: "Proposal Sent",
  AWAITING_APPROVAL: "Awaiting Approval",
  NEGOTIATING: "Negotiating",
};

/** Stale thresholds in days by phase */
export const STALE_THRESHOLDS: Record<string, number> = {
  EARLY: 14,
  COMMERCIAL: 14,
  CLOSURE: 21,
};

export const TEMPLATE_LABELS: Record<string, string> = {
  T1: "Early Field Visit -- First",
  T2: "Early Field Visit -- Follow-up",
  T3: "Early Virtual -- First",
  T4: "Early Virtual -- Follow-up",
  T5: "Commercial Field Visit -- First",
  T6: "Commercial Field Visit -- Follow-up",
  T7: "Commercial Virtual -- First",
  T8: "Commercial Virtual -- Follow-up",
  T9: "Closure Field Visit -- First",
  T10: "Closure Field Visit -- Follow-up",
  T11: "Closure Virtual -- First",
  T12: "Closure Virtual -- Follow-up",
};

export const DURATION_OPTIONS = [
  "< 15 min",
  "15-30 min",
  "30-60 min",
  "1-2 hrs",
  "2+ hrs",
] as const;

export const DURATION_VALUES: Record<string, string> = {
  "< 15 min": "UNDER_15_MIN",
  "15-30 min": "MIN_15_TO_30",
  "30-60 min": "MIN_30_TO_60",
  "1-2 hrs": "HRS_1_TO_2",
  "2+ hrs": "OVER_2_HRS",
};

/** Reverse mapping: enum value → display label */
export const DURATION_LABELS: Record<string, string> = {
  UNDER_15_MIN: "< 15 min",
  MIN_15_TO_30: "15-30 min",
  MIN_30_TO_60: "30-60 min",
  HRS_1_TO_2: "1-2 hrs",
  OVER_2_HRS: "2+ hrs",
};

export const NEXT_ACTION_OWNERS = [
  "Self",
  "Manager",
  "Operator",
  "Ops Team",
] as const;

export const NEXT_ACTION_OWNER_VALUES: Record<string, string> = {
  Self: "SELF",
  Manager: "MANAGER",
  Operator: "OPERATOR",
  "Ops Team": "OPS_TEAM",
};

export const CONTACT_ROLES = [
  "Owner",
  "Fleet Manager",
  "Finance Head",
  "Driver Supervisor",
  "Other",
] as const;

export const CONTACT_ROLE_VALUES: Record<string, string> = {
  Owner: "OWNER",
  "Fleet Manager": "FLEET_MANAGER",
  "Finance Head": "FINANCE_HEAD",
  "Driver Supervisor": "DRIVER_SUPERVISOR",
  Other: "OTHER",
};

export const LEAD_SOURCE_VALUES: Record<string, string> = {
  "Agent Field": "AGENT_FIELD",
  Referrals: "REFERRALS",
  Inbound: "INBOUND",
  "Company Lists": "COMPANY_LISTS",
};

export const DEFAULT_OBJECTIONS = [
  "Price too high",
  "Lease terms unfavorable",
  "Range anxiety",
  "Charging infrastructure concerns",
  "Prefers diesel / CNG",
  "Competitor offering better deal",
  "Wants to buy, not lease",
  "Timing not right",
  "Decision maker unavailable",
  "Maintenance concerns",
  "Regulatory uncertainty",
  "Resale / residual value concerns",
  "Other",
] as const;

export const DEFAULT_BUYING_SIGNALS = [
  "Asking about delivery timelines",
  "Requesting references / case studies",
  "Discussing fleet replacement schedule",
  "Asking about financing details",
  "Involving finance / procurement team",
  "Requesting a site visit or demo",
  "Comparing specific bus models",
  "Asking about government subsidies",
  "Sharing competitor quotes",
  "Introducing us to other operators",
  "Other",
] as const;

export const STAGE_COLORS: Record<string, { borderL: string; bg: string; text: string; bar: string }> = {
  STAGE_1: { borderL: "border-l-slate-400", bg: "bg-slate-100", text: "text-slate-700", bar: "bg-slate-400" },
  STAGE_2: { borderL: "border-l-sky-400", bg: "bg-sky-100", text: "text-sky-700", bar: "bg-sky-400" },
  STAGE_3: { borderL: "border-l-blue-500", bg: "bg-blue-100", text: "text-blue-700", bar: "bg-blue-500" },
  STAGE_4: { borderL: "border-l-indigo-500", bg: "bg-indigo-100", text: "text-indigo-700", bar: "bg-indigo-500" },
  STAGE_5: { borderL: "border-l-violet-500", bg: "bg-violet-100", text: "text-violet-700", bar: "bg-violet-500" },
  STAGE_6: { borderL: "border-l-amber-500", bg: "bg-amber-100", text: "text-amber-700", bar: "bg-amber-500" },
  STAGE_7: { borderL: "border-l-orange-500", bg: "bg-orange-100", text: "text-orange-700", bar: "bg-orange-500" },
  STAGE_8: { borderL: "border-l-emerald-500", bg: "bg-emerald-100", text: "text-emerald-700", bar: "bg-emerald-500" },
};

export const PHASE_HEADER_COLORS: Record<string, { bg: string; text: string; border: string }> = {
  EARLY: { bg: "bg-sky-50", text: "text-sky-800", border: "border-sky-200" },
  COMMERCIAL: { bg: "bg-violet-50", text: "text-violet-800", border: "border-violet-200" },
  CLOSURE: { bg: "bg-emerald-50", text: "text-emerald-800", border: "border-emerald-200" },
};

export const PRIORITY_COLORS: Record<string, { borderL: string; bg: string; text: string }> = {
  HIGH: { borderL: "border-l-red-500", bg: "bg-red-50", text: "text-red-700" },
  MEDIUM: { borderL: "border-l-amber-400", bg: "bg-amber-50", text: "text-amber-700" },
  LOW: { borderL: "border-l-slate-300", bg: "bg-slate-50", text: "text-slate-600" },
};
