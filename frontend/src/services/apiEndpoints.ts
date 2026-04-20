import api from "./api";
import type {
  User,
  LoginResponse,
  Operator,
  Deal,
  DealListItem,
  PagedResponse,
  Contact,
  ActivityReport,
  Task,
  SearchResults,
  PricingSubmission,
  DocumentCompletion,
  Notification,
  PipelineOverview,
  AgentPerformance,
  WinLoss,
  TaxonomyItem,
  StaleThreshold,
  ExitCriteria,
  ChecklistConfig,
  Region,
  WebhookKey,
  NotificationPref,
  AuditEntry,
} from "../types";

export const authApi = {
  listUsers: () =>
    api.get<User[]>("/auth/users").then((r) => r.data),
  devLogin: (userId: number) =>
    api.post<LoginResponse>("/auth/dev-login", { userId }).then((r) => r.data),
  me: () => api.get<User>("/auth/me").then((r) => r.data),
};

export const operatorsApi = {
  list: (params?: Record<string, unknown>) =>
    api.get<PagedResponse<Operator>>("/operators", { params }).then((r) => r.data),
  get: (id: number) =>
    api.get<Operator>(`/operators/${id}`).then((r) => r.data),
  create: (data: Record<string, unknown>) =>
    api.post<Operator>("/operators", data).then((r) => r.data),
  update: (id: number, data: Record<string, unknown>) =>
    api.put<Operator>(`/operators/${id}`, data).then((r) => r.data),
  addContact: (operatorId: number, data: Record<string, unknown>) =>
    api.post<Contact>(`/operators/${operatorId}/contacts`, data).then((r) => r.data),
  updateContact: (operatorId: number, contactId: number, data: { name: string; role: string; mobile: string | null; email: string | null }) =>
    api.put<Contact>(`/operators/${operatorId}/contacts/${contactId}`, data).then((r) => r.data),
};

export const dealsApi = {
  list: (params?: Record<string, unknown>) =>
    api.get<PagedResponse<DealListItem>>("/deals", { params }).then((r) => r.data),
  get: (id: number) =>
    api.get<Deal>(`/deals/${id}`).then((r) => r.data),
  create: (data: Record<string, unknown>) =>
    api.post<Deal>("/deals", data).then((r) => r.data),
  update: (id: number, data: Record<string, unknown>) =>
    api.put<Deal>(`/deals/${id}`, data).then((r) => r.data),
  archive: (id: number, data: Record<string, unknown>) =>
    api.post(`/deals/${id}/archive`, data).then((r) => r.data),
  reactivate: (id: number) =>
    api.post(`/deals/${id}/reactivate`).then((r) => r.data),
  reopen: (id: number) =>
    api.post(`/deals/${id}/reopen`).then((r) => r.data),
};

export const usersApi = {
  list: (params?: Record<string, unknown>) =>
    api.get<User[]>("/users", { params }).then((r) => r.data),
};

export const taxonomyApi = {
  list: (type: string) =>
    api.get(`/admin/taxonomy/${type}`).then((r) => r.data),
};

export const stageApi = {
  moveForward: (dealId: number, data?: { overrideReason?: string }) =>
    api.post(`/deals/${dealId}/stage/forward`, data || {}).then((r) => r.data),
  moveBackward: (dealId: number, data: { reason: string }) =>
    api.post(`/deals/${dealId}/stage/backward`, data).then((r) => r.data),
  approveRegression: (requestId: number) =>
    api.post(`/deals/regression-requests/${requestId}/approve`).then((r) => r.data),
  rejectRegression: (requestId: number) =>
    api.post(`/deals/regression-requests/${requestId}/reject`).then((r) => r.data),
};

export const reportsApi = {
  list: (dealId: number, params?: Record<string, unknown>) =>
    api.get<PagedResponse<ActivityReport>>(`/deals/${dealId}/reports`, { params }).then((r) => r.data),
  get: (dealId: number, reportId: number) =>
    api.get<ActivityReport>(`/deals/${dealId}/reports/${reportId}`).then((r) => r.data),
  submit: (dealId: number, data: Record<string, unknown>) =>
    api.post<ActivityReport>(`/deals/${dealId}/reports`, data).then((r) => r.data),
  void_: (dealId: number, reportId: number, data: { reason: string }) =>
    api.post(`/deals/${dealId}/reports/${reportId}/void`, data).then((r) => r.data),
  unvoid: (dealId: number, reportId: number) =>
    api.post(`/deals/${dealId}/reports/${reportId}/unvoid`).then((r) => r.data),
  update: (dealId: number, reportId: number, data: Record<string, unknown>) =>
    api.put<ActivityReport>(`/deals/${dealId}/reports/${reportId}`, data).then((r) => r.data),
  getNotes: (dealId: number, reportId: number) =>
    api.get<import("../types").ActivityReportNote[]>(`/deals/${dealId}/reports/${reportId}/notes`).then((r) => r.data),
  addNote: (dealId: number, reportId: number, data: { content: string }) =>
    api.post<import("../types").ActivityReportNote>(`/deals/${dealId}/reports/${reportId}/notes`, data).then((r) => r.data),
};

export const tasksApi = {
  list: (params?: Record<string, unknown>) =>
    api.get<Task[]>("/tasks", { params }).then((r) => r.data),
  create: (data: Record<string, unknown>) =>
    api.post<Task>("/tasks", data).then((r) => r.data),
  get: (id: number) =>
    api.get<Task>(`/tasks/${id}`).then((r) => r.data),
  update: (id: number, data: Record<string, unknown>) =>
    api.put<Task>(`/tasks/${id}`, data).then((r) => r.data),
  updateStatus: (id: number, status: string) =>
    api.put<Task>(`/tasks/${id}/status`, { status }).then((r) => r.data),
  delete: (id: number) =>
    api.delete(`/tasks/${id}`).then((r) => r.data),
  forDeal: (dealId: number) =>
    api.get<Task[]>(`/tasks/deal/${dealId}`).then((r) => r.data),
  getNotes: (taskId: number) =>
    api.get<import("../types").TaskNote[]>(`/tasks/${taskId}/notes`).then((r) => r.data),
  addNote: (taskId: number, data: { content: string; noteType: string }) =>
    api.post<import("../types").TaskNote>(`/tasks/${taskId}/notes`, data).then((r) => r.data),
};

export const searchApi = {
  search: (q: string) =>
    api.get<SearchResults>("/search", { params: { q } }).then((r) => r.data),
};

export const pricingApi = {
  submit: (dealId: number, data: Record<string, unknown>) =>
    api.post(`/deals/${dealId}/pricing/submit`, data).then((r) => r.data),
  approve: (dealId: number, submissionId: number, data?: Record<string, unknown>) =>
    api.post(`/deals/${dealId}/pricing/${submissionId}/approve`, data || {}).then((r) => r.data),
  reject: (dealId: number, submissionId: number, data: Record<string, unknown>) =>
    api.post(`/deals/${dealId}/pricing/${submissionId}/reject`, data).then((r) => r.data),
  history: (dealId: number) =>
    api.get<PricingSubmission[]>(`/deals/${dealId}/pricing/history`).then((r) => r.data),
};

export const documentsApi = {
  list: (dealId: number) =>
    api.get<DocumentCompletion>(`/deals/${dealId}/documents`).then((r) => r.data),
  updateStatus: (dealId: number, docId: number, status: string) =>
    api.put(`/deals/${dealId}/documents/${docId}/status`, { status }).then((r) => r.data),
  upload: (dealId: number, docId: number, file: File) => {
    const formData = new FormData();
    formData.append("file", file);
    return api.post(`/deals/${dealId}/documents/${docId}/upload`, formData).then((r) => r.data);
  },
};

export const notificationsApi = {
  list: (params?: Record<string, unknown>) =>
    api.get<PagedResponse<Notification>>("/notifications", { params }).then((r) => r.data),
  count: () =>
    api.get<{ count: number }>("/notifications/count").then((r) => r.data),
  clear: (id: number) =>
    api.post(`/notifications/${id}/clear`).then((r) => r.data),
  clearAll: () =>
    api.post("/notifications/clear-all").then((r) => r.data),
};

export const adminApi = {
  // Taxonomy
  listTaxonomy: (type: string) =>
    api.get<TaxonomyItem[]>(`/admin/taxonomy/${type}`).then((r) => r.data),
  addTaxonomy: (type: string, value: string) =>
    api.post(`/admin/taxonomy/${type}`, { value }).then((r) => r.data),
  updateTaxonomy: (type: string, id: number, data: Record<string, unknown>) =>
    api.put(`/admin/taxonomy/${type}/${id}`, data).then((r) => r.data),
  // Stale thresholds
  getStaleThresholds: () =>
    api.get<StaleThreshold[]>("/admin/stale-thresholds").then((r) => r.data),
  updateStaleThresholds: (data: StaleThreshold[]) =>
    api.put("/admin/stale-thresholds", data).then((r) => r.data),
  // Exit criteria
  getExitCriteria: () =>
    api.get<ExitCriteria[]>("/admin/exit-criteria").then((r) => r.data),
  updateExitCriteria: (criteria: Record<string, boolean>) =>
    api.put("/admin/exit-criteria", { criteria }).then((r) => r.data),
  // Checklist
  listChecklist: () =>
    api.get<ChecklistConfig[]>("/admin/checklist").then((r) => r.data),
  addChecklistItem: (data: Record<string, unknown>) =>
    api.post("/admin/checklist", data).then((r) => r.data),
  updateChecklistItem: (id: number, data: Record<string, unknown>) =>
    api.put(`/admin/checklist/${id}`, data).then((r) => r.data),
  // Regions
  listRegions: () =>
    api.get<Region[]>("/admin/regions").then((r) => r.data),
  addRegion: (name: string) =>
    api.post("/admin/regions", { name }).then((r) => r.data),
  updateRegion: (id: number, data: Record<string, unknown>) =>
    api.put(`/admin/regions/${id}`, data).then((r) => r.data),
  // Webhook keys
  listWebhookKeys: () =>
    api.get<WebhookKey[]>("/admin/api-keys").then((r) => r.data),
  generateKey: (description: string) =>
    api.post("/admin/api-keys", { description }).then((r) => r.data),
  revokeKey: (id: number) =>
    api.post(`/admin/api-keys/${id}/revoke`).then((r) => r.data),
  // Notification prefs
  getNotificationPrefs: () =>
    api.get<NotificationPref[]>("/admin/notification-preferences").then((r) => r.data),
  updateNotificationPrefs: (prefs: NotificationPref[]) =>
    api.put("/admin/notification-preferences", prefs).then((r) => r.data),
  // Users management
  addUser: (data: Record<string, unknown>) =>
    api.post<User>("/admin/users", data).then((r) => r.data),
  updateUser: (id: number, data: Record<string, unknown>) =>
    api.put(`/admin/users/${id}`, data).then((r) => r.data),
};

export const auditApi = {
  getDealAudit: (dealId: number, params?: Record<string, unknown>) =>
    api.get<PagedResponse<AuditEntry>>(`/deals/${dealId}/audit`, { params }).then((r) => r.data),
};

export const backfillApi = {
  request: (dealId: number, data: Record<string, unknown>) =>
    api.post(`/deals/${dealId}/backfill`, data).then((r) => r.data),
};

export const attachmentsApi = {
  upload: (file: File, subPath: string) => {
    const formData = new FormData();
    formData.append("file", file);
    formData.append("subPath", subPath);
    return api.post<{ fileKey: string; originalFilename: string; fileSize: number }>(
      "/attachments/upload", formData
    ).then(r => r.data);
  },
  download: (id: number) =>
    api.get(`/attachments/${id}/download`, { responseType: "blob" }).then((r) => ({
      blob: r.data as Blob,
      contentType: r.headers["content-type"] || "application/octet-stream",
    })),
};

export const dashboardApi = {
  pipelineOverview: (params?: Record<string, unknown>) =>
    api.get<PipelineOverview>("/dashboard/pipeline-overview", { params }).then((r) => r.data),
  agentPerformance: (params?: Record<string, unknown>) =>
    api.get<AgentPerformance>("/dashboard/agent-performance", { params }).then((r) => r.data),
  winLoss: (params?: Record<string, unknown>) =>
    api.get<WinLoss>("/dashboard/win-loss", { params }).then((r) => r.data),
  objectionHeatmap: (params?: Record<string, unknown>) =>
    api.get("/dashboard/objection-heatmap", { params }).then((r) => r.data),
  alerts: () =>
    api.get("/dashboard/alerts").then((r) => r.data),
};
