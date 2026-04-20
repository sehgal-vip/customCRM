export interface User {
  id: number;
  email: string;
  name: string;
  role: "AGENT" | "MANAGER";
  status: "ACTIVE" | "DEACTIVATED";
  regions: Region[];
  activeDeals?: number;
}

export interface Region {
  id: number;
  name: string;
  active?: boolean;
}

export interface LoginResponse {
  token: string;
  user: User;
}

export interface Operator {
  id: number;
  companyName: string;
  phone?: string;
  email?: string;
  region?: Region;
  operatorType?: string;
  referralSource?: string;
  fleetSize?: number;
  numRoutes?: number;
  primaryUseCase?: string;
  contacts: Contact[];
  dealCount: number;
  createdAt: string;
}

export interface Contact {
  id: number;
  name: string;
  role: string;
  mobile?: string;
  email?: string;
  incomplete?: boolean;
}

export interface Deal {
  id: number;
  name: string;
  operator: { id: number; companyName: string };
  assignedAgent: { id: number; name: string };
  fleetSize?: number;
  estimatedMonthlyValue?: number;
  leadSource: string;
  currentStage: string;
  subStatus?: string;
  status: "ACTIVE" | "ARCHIVED" | "COMPLETED";
  archivedReason?: string;
  archivedReasonText?: string;
  reopened: boolean;
  backfilled: boolean;
  daysInStage: number;
  docCompletionPct: number;
  nextAction?: string;
  nextActionEta?: string;
  createdAt: string;
  updatedAt: string;
  pricingApproved?: boolean;
}

export interface DealListItem {
  id: number;
  name: string;
  operatorName: string;
  agentName: string;
  fleetSize?: number;
  estimatedMonthlyValue?: number;
  currentStage: string;
  subStatus?: string;
  status: string;
  daysInStage: number;
  nextAction?: string;
  nextActionEta?: string;
  pricingApproved?: boolean;
}

export interface PagedResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}

export interface ActivityReport {
  id: number;
  dealId: number;
  agentId: number;
  agentName?: string;
  agent?: { id: number; name: string; email?: string };
  templateType: string;
  activityType: "FIELD_VISIT" | "VIRTUAL";
  interactionDatetime: string;
  submissionDatetime: string;
  contact?: { id: number; name?: string; mobile?: string; email?: string };
  contactName?: string;
  contactRole: string;
  duration: string;
  phaseSpecificData: Record<string, unknown>;
  buyingSignals: string[];
  objections: string[];
  notes?: string;
  nextAction?: string;
  nextActionEta?: string;
  nextActionOwner?: string;
  loggedAtStage: string; // "STAGE_1" through "STAGE_8"
  voided: boolean;
  voidedReason?: string;
  voidedAt?: string;
  isFirstInPhase: boolean;
  attachments: Attachment[];
}

export interface Attachment {
  id: number;
  fileName: string;
  fileKey: string;
  fileSize: number;
  categoryTag: string;
}

export interface TaskItem {
  dealId: number;
  dealName: string;
  operatorName: string;
  nextAction: string;
  nextActionEta: string;
  nextActionOwner: string;
  currentStage: string;
  agentName: string;
  agentId: number;
  overdue: boolean;
  type: string;
}

export interface Task {
  id: number;
  title: string;
  description?: string;
  status: "OPEN" | "IN_PROGRESS" | "DONE";
  priority: "HIGH" | "MEDIUM" | "LOW";
  assignedToId: number;
  assignedToName: string;
  createdById: number;
  createdByName: string;
  dealId?: number;
  dealName?: string;
  activityReportId?: number;
  dueDate: string;
  completedAt?: string;
  createdAt: string;
  overdue: boolean;
}

export interface ActivityReportNote {
  id: number;
  activityReportId: number;
  content: string;
  createdById: number;
  createdByName: string;
  createdAt: string;
}

export interface TaskNote {
  id: number;
  taskId: number;
  content: string;
  noteType: "AGENT" | "MANAGER";
  createdById: number;
  createdByName: string;
  createdAt: string;
}

export interface TaskSearchResult {
  id: number;
  title: string;
  assignedToName: string;
  dealName?: string;
  status: string;
  dueDate?: string;
}

export interface UserSearchResult {
  id: number;
  name: string;
  email: string;
  role: string;
}

export interface ContactSearchResult {
  id: number;
  name: string;
  role?: string;
  mobile?: string;
  email?: string;
  operatorId: number;
  operatorName: string;
}

export interface SearchResults {
  deals: DealListItem[];
  operators: Operator[];
  tasks: TaskSearchResult[];
  users: UserSearchResult[];
  contacts: ContactSearchResult[];
}

export interface StageTransitionResponse {
  deal: Deal;
  fromStage: string;
  toStage: string;
  transitionType: string;
}

export interface CriteriaResult {
  rule: string;
  met: boolean;
  softBlock: boolean;
  message: string;
}

export interface PricingSubmission {
  id: number;
  dealId: number;
  submittedBy: { id: number; name: string };
  servicesSelected: string[];
  monthlyKmCommitment: number;
  pricePerKm: number;
  monthlyValuePerVehicle: number;
  managerServicesSelected?: string[];
  managerMonthlyKm?: number;
  managerPricePerKm?: number;
  tokenAmount?: number;
  managerTokenAmount?: number;
  status: "SUBMITTED" | "APPROVED" | "REJECTED" | "SUPERSEDED";
  reviewedBy?: { id: number; name: string };
  reviewedAt?: string;
  rejectionNote?: string;
  createdAt: string;
}

export interface DocumentChecklistItem {
  id: number;
  documentName: string;
  requirement: "MANDATORY" | "OPTIONAL";
  requiredByStage: string;
  status: "NOT_STARTED" | "REQUESTED" | "RECEIVED" | "VERIFIED";
  fileKey?: string;
  hasFile: boolean;
}

export interface DocumentCompletion {
  mandatoryComplete: number;
  mandatoryTotal: number;
  percentage: number;
  items: DocumentChecklistItem[];
}

export interface Notification {
  id: number;
  eventType: string;
  priority: "HIGH" | "MEDIUM" | "LOW";
  title: string;
  content: string;
  dealId?: number;
  cleared: boolean;
  createdAt: string;
}

export interface PipelineOverview {
  stageMetrics: { stage: string; dealCount: number; totalValue: number }[];
  pendingPricing: number;
  pendingRegression: number;
}

export interface AgentPerformance {
  agents: { agentId: number; agentName: string; activeDeals: number; pipelineValue: number; overdueFollowUps: number }[];
}

export interface WinLoss {
  winRate: number;
  lostReasons: { reason: string; count: number }[];
}

export interface TaxonomyItem {
  id: number;
  taxonomyType: string;
  value: string;
  active: boolean;
}

export interface StaleThreshold {
  stage: string;
  thresholdDays: number;
}

export interface ExitCriteria {
  stage: string;
  activityRequired: boolean;
}

export interface ChecklistConfig {
  id: number;
  documentName: string;
  requirement: string;
  requiredByStage: string;
  active: boolean;
}

export interface WebhookKey {
  id: number;
  keyPrefix: string;
  description: string;
  active: boolean;
  createdAt: string;
  revokedAt?: string;
}

export interface NotificationPref {
  role: string;
  eventType: string;
  enabled: boolean;
}

export interface AuditEntry {
  id: number;
  entityType: string;
  entityId: number;
  action: string;
  actorName?: string;
  details: Record<string, unknown>;
  createdAt: string;
}
