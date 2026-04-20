import { useState, useRef, useEffect } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { useQuery, useQueryClient, useMutation } from "@tanstack/react-query";
import {
  ArrowLeft,
  Loader2,
  MapPin,
  Video,
  Pencil,
  ArrowRight,
  Archive,
  Truck,
  Calendar,
  Clock,
  Lock,
  ClipboardList,
  CheckCircle,
  History,
  MoreVertical,
  ListChecks,
  Plus,
} from "lucide-react";
import { toast } from "sonner";
import { dealsApi, reportsApi, operatorsApi, tasksApi } from "../services/apiEndpoints";
import {
  STAGE_NAMES,
  PHASE_STAGES,
  PHASE_NAMES,
  STALE_THRESHOLDS,
  SUB_STATUS_LABELS,
  SUB_STATUS_COLORS,
  TEMPLATE_LABELS,
  DURATION_LABELS,
  STAGE_COLORS,
} from "../lib/constants";
import { formatINR, formatDate, formatEnum } from "../lib/formatters";
import { cn } from "../lib/utils";
import { useAuth } from "../context/AuthContext";
import StageProgressBar from "../components/shared/StageProgressBar";
import StageBadge from "../components/shared/StageBadge";
import LogActivityModal from "../components/modals/LogActivityModal";
import ChangeStageModal from "../components/modals/ChangeStageModal";
import ArchiveDealModal from "../components/modals/ArchiveDealModal";
import ApprovePricingModal from "../components/modals/ApprovePricingModal";
import PricingHistoryPanel from "../components/deals/PricingHistoryPanel";
import DocumentsTab from "../components/deals/DocumentsTab";
import OperatorTab from "../components/deals/OperatorTab";
import AuditTab from "../components/deals/AuditTab";
import BackfillRequestModal from "../components/modals/BackfillRequestModal";
import EditDealModal from "../components/modals/EditDealModal";
import CreateTaskModal from "../components/modals/CreateTaskModal";
import EditTaskModal from "../components/modals/EditTaskModal";
import ActivityReportModal from "../components/modals/ActivityReportModal";
import type { Deal, ActivityReport, Task } from "../types";

function getPhaseForStage(stage: string): string {
  for (const [phase, stages] of Object.entries(PHASE_STAGES)) {
    if ((stages as readonly string[]).includes(stage)) return phase;
  }
  return "EARLY";
}

function isOverdue(deal: Deal): boolean {
  if (!deal.nextActionEta) return false;
  return new Date(deal.nextActionEta) < new Date();
}

function isStale(deal: Deal): boolean {
  const phase = getPhaseForStage(deal.currentStage);
  const threshold = STALE_THRESHOLDS[phase] ?? 14;
  return deal.daysInStage > threshold;
}

export default function DealDetailPage() {
  const { dealId } = useParams<{ dealId: string }>();
  const navigate = useNavigate();
  const [activeTab, setActiveTab] = useState<"activity" | "documents" | "operator" | "audit">("activity");
  const [logModalOpen, setLogModalOpen] = useState(false);
  const [stageModalOpen, setStageModalOpen] = useState(false);
  const [archiveModalOpen, setArchiveModalOpen] = useState(false);
  const [pricingModalOpen, setPricingModalOpen] = useState(false);
  const [pricingHistoryOpen, setPricingHistoryOpen] = useState(false);
  const [backfillModalOpen, setBackfillModalOpen] = useState(false);
  const [editModalOpen, setEditModalOpen] = useState(false);
  const [createTaskModalOpen, setCreateTaskModalOpen] = useState(false);
  const [editingTask, setEditingTask] = useState<Task | null>(null);
  const { isManager } = useAuth();
  const queryClient = useQueryClient();

  const id = Number(dealId);

  const { data: deal, isLoading, isError } = useQuery({
    queryKey: ["deal", id],
    queryFn: () => dealsApi.get(id),
    enabled: !!id,
  });

  const { data: reportsPage } = useQuery({
    queryKey: ["reports", id],
    queryFn: () => reportsApi.list(id, { size: "50" }),
    enabled: !!id,
  });

  const { data: operator } = useQuery({
    queryKey: ["operator", deal?.operator?.id],
    queryFn: () => operatorsApi.get(deal!.operator.id),
    enabled: !!deal?.operator?.id,
  });

  const { data: dealTasks } = useQuery({
    queryKey: ["deal-tasks", id],
    queryFn: () => tasksApi.forDeal(id),
    enabled: !!id,
  });

  const taskStatusMutation = useMutation({
    mutationFn: ({ taskId, status }: { taskId: number; status: string }) =>
      tasksApi.updateStatus(taskId, status),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["deal-tasks", id] });
      queryClient.invalidateQueries({ queryKey: ["tasks"] });
    },
    onError: () => {
      toast.error("Failed to update task status");
      queryClient.invalidateQueries({ queryKey: ["deal-tasks", id] });
    },
  });

  const reports: ActivityReport[] = reportsPage?.content ?? [];
  const contacts = operator?.contacts ?? [];
  const activeTasks: Task[] = dealTasks ?? [];

  // Determine if first in phase
  const isFirstInPhase = reports.filter((r) => !r.voided).length === 0;

  if (isLoading) {
    return (
      <div className="flex items-center justify-center py-20">
        <Loader2 className="h-8 w-8 animate-spin text-primary" />
      </div>
    );
  }

  if (isError || !deal) {
    return (
      <div className="flex items-center justify-center py-20 text-center">
        <div>
          <p className="text-sm text-danger font-medium">Failed to load deal</p>
          <button
            type="button"
            onClick={() => navigate("/board")}
            className="mt-2 text-sm text-primary hover:underline"
          >
            Back to Pipeline
          </button>
        </div>
      </div>
    );
  }

  const isCompleted = deal.status === "COMPLETED";
  const isArchived = deal.status === "ARCHIVED";
  const isInactive = isCompleted || isArchived;
  const phase = getPhaseForStage(deal.currentStage);
  const overdue = !isInactive && isOverdue(deal);
  const stale = !isInactive && !overdue && isStale(deal);
  const daysThreshold = STALE_THRESHOLDS[phase] ?? 14;

  return (
    <div className="space-y-4">
      {/* Header */}
      <div>
        <button
          type="button"
          onClick={() => navigate("/board")}
          className="inline-flex items-center gap-1 text-sm text-text-muted hover:text-text-primary transition-colors mb-2"
        >
          <ArrowLeft className="h-4 w-4" />
          Back to Pipeline
        </button>

        <h1 className="text-xl font-semibold text-text-primary">{deal.name}</h1>

        <div className="flex items-center gap-2 mt-1 text-sm text-text-secondary flex-wrap">
          <span>{deal.operator.companyName}</span>
          <span className="text-text-muted">|</span>
          <span>{deal.assignedAgent.name}</span>
          <span className="text-text-muted">|</span>
          <span className="text-text-muted">{formatEnum(deal.leadSource)}</span>
        </div>

        {/* Status banner for inactive deals */}
        {isCompleted && (
          <div className="mt-2 rounded-lg bg-emerald-50 border border-emerald-200 px-4 py-2.5">
            <p className="text-sm font-medium text-emerald-800">This deal has been completed.</p>
          </div>
        )}
        {isArchived && (
          <div className="mt-2 rounded-lg bg-red-50 border border-red-200 px-4 py-2.5">
            <p className="text-sm font-medium text-red-800">
              This deal has been archived.{deal.archivedReason && ` Reason: ${deal.archivedReasonText || formatEnum(deal.archivedReason)}`}
            </p>
          </div>
        )}

        {/* Status badges */}
        <div className="flex flex-wrap items-center gap-1.5 mt-2">
          {isInactive ? (
            <span className={cn(
              "inline-flex items-center px-2 py-0.5 rounded-full text-xs font-medium",
              isCompleted ? "bg-emerald-100 text-emerald-700" : "bg-red-100 text-red-700"
            )}>
              {isCompleted ? "Completed" : "Archived"}
            </span>
          ) : (
            <span className="inline-flex items-center px-2 py-0.5 rounded-full text-xs font-medium bg-teal-50 text-teal-700">
              {PHASE_NAMES[phase]}
            </span>
          )}
          <StageBadge stage={deal.currentStage} />
          {deal.subStatus && (
            <span
              className={cn(
                "inline-flex items-center px-2 py-0.5 rounded-full text-xs font-medium",
                SUB_STATUS_COLORS[deal.subStatus]?.bg ?? "bg-slate-100",
                SUB_STATUS_COLORS[deal.subStatus]?.text ?? "text-slate-600"
              )}
            >
              {SUB_STATUS_LABELS[deal.subStatus] ?? deal.subStatus}
            </span>
          )}
          {stale && (
            <span className="inline-flex items-center px-2 py-0.5 rounded-full text-xs font-medium bg-amber-50 text-amber-700">
              Stale
            </span>
          )}
          {overdue && (
            <span className="inline-flex items-center px-2 py-0.5 rounded-full text-xs font-medium bg-red-50 text-red-700">
              Overdue
            </span>
          )}
        </div>
      </div>

      {/* Stage Progress Bar */}
      <StageProgressBar currentStage={deal.currentStage} />

      {/* Metadata Cards */}
      <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-5 gap-2">
        <MetadataCard
          label={deal.pricingApproved ? "Monthly Billing" : "Estimated Monthly Value"}
          value={formatINR(deal.estimatedMonthlyValue)}
          icon={deal.subStatus === "NEGOTIATING" ? <Lock className="h-3.5 w-3.5 text-text-muted" /> : undefined}
        />
        <MetadataCard
          label="Fleet Size"
          value={deal.fleetSize ? `${deal.fleetSize} buses` : "--"}
          icon={<Truck className="h-3.5 w-3.5 text-text-muted" />}
        />
        <MetadataCard
          label="Days in Stage"
          value={`${deal.daysInStage}`}
          valueClass={deal.daysInStage > daysThreshold ? "text-danger" : undefined}
          icon={<Clock className="h-3.5 w-3.5 text-text-muted" />}
        />
        <MetadataCard
          label="Next Action"
          value={deal.nextAction ?? "No action set"}
          valueClass={!deal.nextAction ? "text-text-muted italic" : undefined}
        />
        <MetadataCard
          label="Due Date"
          value={formatDate(deal.nextActionEta)}
          valueClass={overdue ? "text-danger font-medium" : undefined}
          icon={<Calendar className="h-3.5 w-3.5 text-text-muted" />}
        />
      </div>

      {/* Action Buttons */}
      <div className="flex flex-wrap gap-2">
        {!isInactive && (
          <>
            <button
              type="button"
              onClick={() => setLogModalOpen(true)}
              className="inline-flex items-center gap-1.5 px-4 py-2 text-sm font-medium rounded-lg bg-primary text-white shadow-sm hover:bg-primary-hover transition-colors"
            >
              <ClipboardList className="h-4 w-4" />
              Log Activity
            </button>
            <button
              type="button"
              onClick={() => setStageModalOpen(true)}
              className="inline-flex items-center gap-1.5 px-4 py-2 text-sm font-medium rounded-lg bg-white border border-border text-text-secondary hover:bg-slate-50 transition-colors"
            >
              <ArrowRight className="h-4 w-4" />
              Change Stage
            </button>
            <button
              type="button"
              onClick={() => setEditModalOpen(true)}
              className="inline-flex items-center gap-1.5 px-4 py-2 text-sm font-medium rounded-lg bg-white border border-border text-text-secondary hover:bg-slate-50 transition-colors"
            >
              <Pencil className="h-4 w-4" />
              Edit Deal
            </button>
            {isManager && deal.subStatus === "AWAITING_APPROVAL" && (
              <button
                type="button"
                onClick={() => setPricingModalOpen(true)}
                className="inline-flex items-center gap-1.5 px-4 py-2 text-sm font-medium rounded-lg bg-amber-50 border border-amber-300 text-amber-700 hover:bg-amber-100 transition-colors"
              >
                <CheckCircle className="h-4 w-4" />
                Approve Pricing
              </button>
            )}
          </>
        )}
        <button
          type="button"
          onClick={() => setPricingHistoryOpen(true)}
          className="inline-flex items-center gap-1.5 px-4 py-2 text-sm font-medium rounded-lg bg-white border border-border text-text-secondary hover:bg-slate-50 transition-colors"
        >
          <History className="h-4 w-4" />
          Pricing History
        </button>
        {!isInactive && deal.currentStage === "STAGE_1" && (
          <button
            type="button"
            onClick={() => setBackfillModalOpen(true)}
            className="inline-flex items-center gap-1.5 px-4 py-2 text-sm font-medium rounded-lg bg-white border border-border text-text-secondary hover:bg-slate-50 transition-colors"
          >
            <History className="h-4 w-4" />
            Backfill Request
          </button>
        )}
        {!isInactive && (
          <button
            type="button"
            onClick={() => setArchiveModalOpen(true)}
            className="inline-flex items-center gap-1.5 px-4 py-2 text-sm font-medium rounded-lg bg-red-50 border border-red-200 text-danger hover:bg-red-100 transition-colors"
          >
            <Archive className="h-4 w-4" />
            Archive
          </button>
        )}
        {isManager && isArchived && (
          <button
            type="button"
            onClick={async () => {
              await dealsApi.reactivate(deal.id);
              queryClient.invalidateQueries({ queryKey: ["deal", id] });
            }}
            className="inline-flex items-center gap-1.5 px-4 py-2 text-sm font-medium rounded-lg bg-emerald-50 border border-emerald-200 text-emerald-700 hover:bg-emerald-100 transition-colors"
          >
            <ArrowRight className="h-4 w-4" />
            Reactivate
          </button>
        )}
        {isManager && isCompleted && (
          <button
            type="button"
            onClick={async () => {
              await dealsApi.reopen(deal.id);
              queryClient.invalidateQueries({ queryKey: ["deal", id] });
            }}
            className="inline-flex items-center gap-1.5 px-4 py-2 text-sm font-medium rounded-lg bg-emerald-50 border border-emerald-200 text-emerald-700 hover:bg-emerald-100 transition-colors"
          >
            <ArrowRight className="h-4 w-4" />
            Reopen Deal
          </button>
        )}
      </div>

      {/* Deal Tasks Section */}
      <div className="bg-white rounded-xl border border-border p-3 shadow-[0_1px_2px_rgba(0,0,0,0.05)]">
        <div className="flex items-center justify-between mb-2">
          <div className="flex items-center gap-2">
            <ListChecks className="h-4 w-4 text-text-muted" />
            <h3 className="text-sm font-semibold text-text-primary">
              Tasks ({activeTasks.length})
            </h3>
          </div>
          <div className="flex items-center gap-2">
            <button
              type="button"
              onClick={() => setCreateTaskModalOpen(true)}
              className="inline-flex items-center gap-1 px-2 py-1 text-xs font-medium rounded-lg bg-primary text-white hover:bg-primary-hover transition-colors"
            >
              <Plus className="h-3 w-3" />
              Add
            </button>
            <button
              type="button"
              onClick={() => navigate(`/tasks?dealId=${deal.id}`)}
              className="text-xs text-primary hover:underline"
            >
              View all
            </button>
          </div>
        </div>
        {activeTasks.length === 0 ? (
          <p className="text-xs text-text-muted py-2">No tasks linked to this deal.</p>
        ) : (
          <div className="space-y-1.5">
            {activeTasks.slice(0, 5).map((task) => (
              <div
                key={task.id}
                onClick={() => setEditingTask(task)}
                className={cn(
                  "flex items-center gap-3 p-2 rounded-lg border border-border border-l-4 cursor-pointer hover:shadow-md transition-shadow",
                  task.priority === "HIGH" ? "border-l-red-500" :
                  task.priority === "MEDIUM" ? "border-l-amber-400" : "border-l-slate-300",
                  task.overdue && task.status !== "DONE" && "bg-red-50/30"
                )}
              >
                <div className="flex-1 min-w-0">
                  <p className={cn(
                    "text-xs font-medium text-text-primary truncate",
                    task.status === "DONE" && "line-through opacity-60"
                  )}>
                    {task.title}
                  </p>
                  <p className="text-[10px] text-text-muted">
                    {task.assignedToName} &middot; due {formatDate(task.dueDate)}
                    {task.overdue && task.status !== "DONE" && (
                      <span className="text-red-600 font-medium ml-1">OVERDUE</span>
                    )}
                  </p>
                </div>
                <select
                  value={task.status}
                  onClick={(e) => e.stopPropagation()}
                  onChange={(e) => { e.stopPropagation(); taskStatusMutation.mutate({ taskId: task.id, status: e.target.value }); }}
                  className={cn(
                    "rounded border px-1.5 py-0.5 text-[10px] font-medium cursor-pointer shrink-0",
                    task.status === "OPEN"
                      ? "border-teal-300 bg-teal-50 text-teal-700"
                      : task.status === "IN_PROGRESS"
                      ? "border-blue-300 bg-blue-50 text-blue-700"
                      : "border-slate-300 bg-slate-50 text-slate-600"
                  )}
                >
                  <option value="OPEN">Open</option>
                  <option value="IN_PROGRESS">In Progress</option>
                  <option value="DONE">Done</option>
                </select>
              </div>
            ))}
            {activeTasks.length > 5 && (
              <button
                type="button"
                onClick={() => navigate(`/tasks?dealId=${deal.id}`)}
                className="text-xs text-primary hover:underline"
              >
                +{activeTasks.length - 5} more tasks
              </button>
            )}
          </div>
        )}
      </div>

      {/* Tabs */}
      <div className="border-b border-border">
        <div className="flex gap-0 -mb-px">
          {(["activity", "documents", "operator", "audit"] as const).map((tab) => (
            <button
              key={tab}
              type="button"
              onClick={() => setActiveTab(tab)}
              className={cn(
                "px-4 py-2.5 text-sm font-medium border-b-2 transition-colors capitalize",
                activeTab === tab
                  ? "border-primary text-primary"
                  : "border-transparent text-text-muted hover:text-text-secondary"
              )}
            >
              {tab}
            </button>
          ))}
        </div>
      </div>

      {/* Tab Content */}
      {activeTab === "activity" && (
        <ActivityTab reports={reports} dealStage={deal.currentStage} dealId={deal.id} />
      )}
      {activeTab === "documents" && (
        <DocumentsTab dealId={deal.id} currentStage={deal.currentStage} />
      )}
      {activeTab === "operator" && (
        <OperatorTab operator={operator} isLoading={!operator && !!deal?.operator?.id} />
      )}
      {activeTab === "audit" && (
        <AuditTab dealId={deal.id} />
      )}

      {/* Modals */}
      <LogActivityModal
        open={logModalOpen}
        onClose={() => setLogModalOpen(false)}
        deal={deal}
        contacts={contacts}
        isFirstInPhase={isFirstInPhase}
      />
      <ChangeStageModal
        open={stageModalOpen}
        onClose={() => setStageModalOpen(false)}
        deal={deal}
      />
      <ArchiveDealModal
        open={archiveModalOpen}
        onClose={() => setArchiveModalOpen(false)}
        dealId={deal.id}
        dealName={deal.name}
      />
      <ApprovePricingModal
        open={pricingModalOpen}
        onClose={() => setPricingModalOpen(false)}
        deal={deal}
      />
      <PricingHistoryPanel
        open={pricingHistoryOpen}
        onClose={() => setPricingHistoryOpen(false)}
        dealId={deal.id}
        fleetSize={deal.fleetSize ?? 1}
      />
      <BackfillRequestModal
        open={backfillModalOpen}
        onClose={() => setBackfillModalOpen(false)}
        dealId={deal.id}
        dealName={deal.name}
      />
      <EditDealModal
        open={editModalOpen}
        onClose={() => setEditModalOpen(false)}
        deal={deal}
      />
      <CreateTaskModal
        open={createTaskModalOpen}
        onClose={() => setCreateTaskModalOpen(false)}
        defaultDealId={deal.id}
      />
      <EditTaskModal
        open={!!editingTask}
        onClose={() => setEditingTask(null)}
        task={editingTask}
      />
    </div>
  );
}

/* --- Sub-components --- */

function MetadataCard({
  label,
  value,
  icon,
  valueClass,
}: {
  label: string;
  value: string;
  icon?: React.ReactNode;
  valueClass?: string;
}) {
  return (
    <div className="bg-white rounded-xl border border-border p-3 shadow-[0_1px_2px_rgba(0,0,0,0.05)]">
      <p className="text-[10px] font-medium uppercase tracking-wider text-text-muted mb-1">
        {label}
      </p>
      <div className="flex items-center gap-1.5">
        {icon}
        <p className={cn("text-sm font-semibold text-text-primary truncate", valueClass)}>
          {value}
        </p>
      </div>
    </div>
  );
}

function ActivityTab({
  reports,
  dealId,
}: {
  reports: ActivityReport[];
  dealStage: string;
  dealId: number;
}) {
  const [stageFilter, setStageFilter] = useState<string>("ALL");
  const [selectedReport, setSelectedReport] = useState<ActivityReport | null>(null);

  if (reports.length === 0) {
    return (
      <div className="py-8 text-center">
        <ClipboardList className="h-10 w-10 mx-auto text-text-muted mb-3" />
        <p className="text-sm font-medium text-text-primary">
          No activity reports yet
        </p>
        <p className="text-xs text-text-muted mt-1">
          Log your first activity to start tracking this deal.
        </p>
      </div>
    );
  }

  const filteredReports = stageFilter === "ALL"
    ? reports
    : reports.filter((r: ActivityReport) => r.loggedAtStage === stageFilter);

  return (
    <div className="space-y-3">
      {/* Stage filter */}
      <div className="flex items-center gap-2 mb-3">
        <label className="text-xs font-medium text-text-muted uppercase tracking-wider">Filter by Stage:</label>
        <select
          value={stageFilter}
          onChange={(e) => setStageFilter(e.target.value)}
          className="rounded-lg border border-border px-2 py-1 text-xs text-text-secondary bg-white focus:outline-none focus:ring-2 focus:ring-primary/40"
        >
          <option value="ALL">All Stages</option>
          {Object.entries(STAGE_NAMES)
            .filter(([key]) => reports.some((r: ActivityReport) => r.loggedAtStage === key))
            .map(([key, name]) => {
              const count = reports.filter((r: ActivityReport) => r.loggedAtStage === key).length;
              return <option key={key} value={key}>{name} ({count})</option>;
            })}
        </select>
      </div>

      {filteredReports.map((report) => (
        <ReportCard key={report.id} report={report} dealId={dealId} onClick={() => setSelectedReport(report)} />
      ))}

      <ActivityReportModal
        open={!!selectedReport}
        onClose={() => setSelectedReport(null)}
        report={selectedReport}
        dealId={dealId}
      />
    </div>
  );
}

function ReportCard({ report, dealId, onClick }: { report: ActivityReport; dealId: number; onClick: () => void }) {
  const [expanded, setExpanded] = useState(false);
  const [menuOpen, setMenuOpen] = useState(false);
  const [voidConfirm, setVoidConfirm] = useState(false);
  const [voidReason, setVoidReason] = useState("");
  const menuRef = useRef<HTMLDivElement>(null);
  const { isManager } = useAuth();
  const queryClient = useQueryClient();

  useEffect(() => {
    const handleClick = (e: MouseEvent) => {
      if (menuRef.current && !menuRef.current.contains(e.target as Node)) {
        setMenuOpen(false);
      }
    };
    document.addEventListener("mousedown", handleClick);
    return () => document.removeEventListener("mousedown", handleClick);
  }, []);

  const isField = report.activityType === "FIELD_VISIT";
  const templateLabel = TEMPLATE_LABELS[report.templateType] ?? report.templateType;
  const contactName = report.contact?.name || report.contactName || "Unknown";

  return (
    <div
      onClick={onClick}
      className={cn(
        "bg-white rounded-xl border border-border p-4 shadow-[0_1px_3px_rgba(0,0,0,0.1),0_1px_2px_rgba(0,0,0,0.06)] cursor-pointer hover:shadow-[0_10px_15px_-3px_rgba(0,0,0,0.1),0_4px_6px_-4px_rgba(0,0,0,0.05)] transition-all duration-200",
        "border-l-4",
        STAGE_COLORS[report.loggedAtStage]?.borderL || "border-l-slate-300",
        report.voided && "opacity-50"
      )}
    >
      <div className="flex items-start gap-3">
        {/* Type icon */}
        <div
          className={cn(
            "shrink-0 w-8 h-8 rounded-full flex items-center justify-center",
            isField ? "bg-teal-100" : "bg-blue-100"
          )}
        >
          {isField ? (
            <MapPin className="h-4 w-4 text-teal-600" />
          ) : (
            <Video className="h-4 w-4 text-blue-600" />
          )}
        </div>

        <div className="flex-1 min-w-0">
          {/* Header row */}
          <div className="flex items-start justify-between gap-2">
            <div>
              <p className={cn("text-sm font-medium text-text-primary", report.voided && "line-through")}>
                {contactName}
                <span className="text-text-muted font-normal"> -- {formatEnum(report.contactRole)}</span>
              </p>
              <p className="text-xs text-text-muted mt-0.5">
                {formatDate(report.interactionDatetime)}
              </p>
            </div>
            <div className="flex items-center gap-1.5 shrink-0">
              <span className="inline-flex items-center px-1.5 py-0.5 rounded text-[10px] font-medium bg-slate-100 text-slate-600">
                {DURATION_LABELS[report.duration] || formatEnum(report.duration)}
              </span>
              {report.voided && (
                <span className="inline-flex items-center px-1.5 py-0.5 rounded text-[10px] font-medium bg-red-100 text-red-700">
                  VOIDED
                </span>
              )}
              {isManager && (
                <div className="relative" ref={menuRef}>
                  <button
                    type="button"
                    aria-label="Report actions"
                    onClick={(e) => { e.stopPropagation(); setMenuOpen(!menuOpen); }}
                    className="p-1 rounded hover:bg-slate-100 text-text-muted"
                  >
                    <MoreVertical className="h-4 w-4" />
                  </button>
                  {menuOpen && (
                    <div className="absolute right-0 top-full mt-1 z-20 bg-white border border-border rounded-lg shadow-lg py-1 min-w-[140px]">
                      {!report.voided ? (
                        <button
                          type="button"
                          onClick={(e) => { e.stopPropagation(); setMenuOpen(false); setVoidConfirm(true); }}
                          className="w-full text-left px-3 py-1.5 text-xs text-danger hover:bg-red-50"
                        >
                          Void Report
                        </button>
                      ) : (
                        <button
                          type="button"
                          onClick={async (e) => {
                            e.stopPropagation();
                            setMenuOpen(false);
                            await reportsApi.unvoid(dealId, report.id);
                            queryClient.invalidateQueries({ queryKey: ["reports", dealId] });
                            queryClient.invalidateQueries({ queryKey: ["deal", dealId] });
                          }}
                          className="w-full text-left px-3 py-1.5 text-xs text-primary hover:bg-primary-light"
                        >
                          Un-void Report
                        </button>
                      )}
                    </div>
                  )}
                </div>
              )}
            </div>
          </div>

          {/* Template label */}
          <div className="flex items-center gap-2 mt-1">
            <p className="text-[10px] text-text-muted">
              {report.templateType} -- {templateLabel}
            </p>
            {report.loggedAtStage && (
              <span className="inline-flex items-center px-2 py-0.5 rounded-full text-[10px] font-medium bg-slate-100 text-slate-600">
                {STAGE_NAMES[report.loggedAtStage] || report.loggedAtStage}
              </span>
            )}
          </div>

          {/* Notes */}
          {report.notes && (
            <div className="mt-2">
              <p
                className={cn(
                  "text-xs text-text-secondary",
                  !expanded && "line-clamp-2",
                  report.voided && "line-through"
                )}
              >
                {report.notes}
              </p>
              {report.notes.length > 120 && (
                <button
                  type="button"
                  onClick={(e) => { e.stopPropagation(); setExpanded(!expanded); }}
                  className="text-[11px] text-primary hover:underline mt-0.5"
                >
                  {expanded ? "Show less" : "Show more"}
                </button>
              )}
            </div>
          )}

          {/* Chips */}
          {(report.objections.length > 0 || report.buyingSignals.length > 0) && (
            <div className="flex flex-wrap gap-1 mt-2">
              {report.objections.map((obj) => (
                <span
                  key={obj}
                  className="px-1.5 py-0.5 rounded-full text-[10px] font-medium bg-red-50 text-red-700"
                >
                  {obj}
                </span>
              ))}
              {report.buyingSignals.map((sig) => (
                <span
                  key={sig}
                  className="px-1.5 py-0.5 rounded-full text-[10px] font-medium bg-teal-50 text-teal-700"
                >
                  {sig}
                </span>
              ))}
            </div>
          )}

          {/* Next action footer */}
          {report.nextAction && !report.voided && (
            <div className="mt-2 pt-2 border-t border-border flex items-center gap-2 text-xs">
              <span className="text-text-muted">Next:</span>
              <span className="text-text-primary font-medium">{report.nextAction}</span>
              {report.nextActionEta && (
                <span
                  className={cn(
                    "text-text-muted",
                    new Date(report.nextActionEta) < new Date() && "text-danger"
                  )}
                >
                  due {formatDate(report.nextActionEta)}
                </span>
              )}
              {report.nextActionOwner && (
                <span className="px-1.5 py-0.5 rounded bg-slate-100 text-[10px] text-text-muted">
                  {formatEnum(report.nextActionOwner)}
                </span>
              )}
            </div>
          )}

          {/* Void reason */}
          {report.voided && report.voidedReason && (
            <div className="mt-2 text-xs text-danger">
              Void reason: {report.voidedReason}
            </div>
          )}

          {/* Void confirmation */}
          {voidConfirm && (
            <div className="mt-3 p-3 rounded-lg bg-red-50 border border-red-200 space-y-2" onClick={(e) => e.stopPropagation()}>
              <p className="text-xs text-danger font-medium">Void this report?</p>
              <textarea
                value={voidReason}
                onChange={(e) => setVoidReason(e.target.value)}
                placeholder="Reason for voiding..."
                rows={2}
                className="w-full rounded border border-red-200 px-2 py-1 text-xs resize-none"
              />
              <div className="flex gap-2">
                <button
                  type="button"
                  disabled={!voidReason.trim()}
                  onClick={async () => {
                    await reportsApi.void_(dealId, report.id, { reason: voidReason });
                    queryClient.invalidateQueries({ queryKey: ["reports", dealId] });
                    queryClient.invalidateQueries({ queryKey: ["deal", dealId] });
                    setVoidConfirm(false);
                    setVoidReason("");
                  }}
                  className="px-2 py-1 text-[10px] font-medium rounded bg-danger text-white disabled:opacity-50"
                >
                  Confirm Void
                </button>
                <button type="button" onClick={() => { setVoidConfirm(false); setVoidReason(""); }} className="px-2 py-1 text-[10px] rounded text-text-muted hover:bg-slate-100">
                  Cancel
                </button>
              </div>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
