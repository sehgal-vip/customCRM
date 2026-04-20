import { useState, useMemo, useCallback } from "react";
import { useNavigate, useSearchParams } from "react-router-dom";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import {
  Loader2,
  AlertCircle,
  ChevronLeft,
  ChevronRight,
  ChevronDown,
  ChevronUp,
  List,
  CalendarDays,
  CalendarRange,
  Columns3,
  Plus,
  X,
} from "lucide-react";
import { tasksApi, usersApi } from "../services/apiEndpoints";
import { formatDate } from "../lib/formatters";
import { cn } from "../lib/utils";
import { useAuth } from "../context/AuthContext";
import CreateTaskModal from "../components/modals/CreateTaskModal";
import EditTaskModal from "../components/modals/EditTaskModal";
import MultiSelectDropdown from "../components/shared/MultiSelectDropdown";
import KanbanBoard from "../components/tasks/KanbanBoard";
import type { Task, User as UserType } from "../types";

type ViewMode = "list" | "month" | "week" | "board";

const PRIORITY_COLORS: Record<string, { border: string; bg: string; text: string }> = {
  HIGH: { border: "border-l-red-500", bg: "bg-red-50", text: "text-red-700" },
  MEDIUM: { border: "border-l-amber-400", bg: "bg-amber-50", text: "text-amber-700" },
  LOW: { border: "border-l-slate-300", bg: "bg-slate-50", text: "text-slate-600" },
};

export default function TasksPage() {
  const navigate = useNavigate();
  const [searchParams, setSearchParams] = useSearchParams();
  const { isManager } = useAuth();
  const queryClient = useQueryClient();

  const [viewMode, setViewMode] = useState<ViewMode>("list");
  const [calendarDate, setCalendarDate] = useState(() => new Date());
  const [createModalOpen, setCreateModalOpen] = useState(false);
  const [doneCollapsed, setDoneCollapsed] = useState(true);
  const [editingTask, setEditingTask] = useState<Task | null>(null);

  // Filters from URL params (multi-select: comma-separated)
  const statusesFilter = (searchParams.get("statuses") || searchParams.get("status") || "").split(",").filter(Boolean);
  const prioritiesFilter = (searchParams.get("priorities") || searchParams.get("priority") || "").split(",").filter(Boolean);
  const agentIdsFilter = (searchParams.get("agentIds") || searchParams.get("agentId") || "").split(",").filter(Boolean);
  const dealIdFilter = searchParams.get("dealId") || "";
  const overdueFilter = searchParams.get("overdue") === "true";

  const setMultiFilter = (key: string, values: string[]) => {
    const params = new URLSearchParams(searchParams);
    // Clean up old singular param names
    if (key === "statuses") params.delete("status");
    if (key === "priorities") params.delete("priority");
    if (key === "agentIds") params.delete("agentId");
    if (values.length === 0) {
      params.delete(key);
    } else {
      params.set(key, values.join(","));
    }
    setSearchParams(params, { replace: true });
  };

  const clearDealFilter = () => {
    const params = new URLSearchParams(searchParams);
    params.delete("dealId");
    setSearchParams(params, { replace: true });
  };

  const toggleOverdueFilter = () => {
    const params = new URLSearchParams(searchParams);
    if (overdueFilter) {
      params.delete("overdue");
    } else {
      params.set("overdue", "true");
    }
    setSearchParams(params, { replace: true });
  };

  // Fetch users for agent filter (manager only)
  const { data: users } = useQuery({
    queryKey: ["users"],
    queryFn: () => usersApi.list(),
    enabled: isManager,
  });

  const agents = useMemo(
    () => (users ?? []).filter((u: UserType) => u.status === "ACTIVE"),
    [users]
  );

  // Build query params for API
  const apiParams = useMemo(() => {
    const params: Record<string, unknown> = {};
    if (statusesFilter.length > 0) params.statuses = statusesFilter.join(",");
    if (prioritiesFilter.length > 0) params.priorities = prioritiesFilter.join(",");
    if (agentIdsFilter.length > 0) params.agentIds = agentIdsFilter.join(",");
    if (dealIdFilter) params.dealId = dealIdFilter;
    return params;
  }, [statusesFilter, prioritiesFilter, agentIdsFilter, dealIdFilter]);

  const { data: tasks, isLoading, isError } = useQuery({
    queryKey: ["tasks", apiParams],
    queryFn: () => tasksApi.list(apiParams),
  });

  // Status change mutation
  const statusMutation = useMutation({
    mutationFn: ({ id, status }: { id: number; status: string }) =>
      tasksApi.updateStatus(id, status),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["tasks"] });
      queryClient.invalidateQueries({ queryKey: ["deal-tasks"] });
    },
  });

  const handleStatusChange = (id: number, status: string) =>
    statusMutation.mutate({ id, status });

  const allTasks = useMemo(() => tasks ?? [], [tasks]);

  const overdueCount = useMemo(
    () => allTasks.filter((t) => t.overdue && t.status !== "DONE").length,
    [allTasks]
  );

  const filteredTasks = useMemo(() => {
    if (!overdueFilter) return allTasks;
    return allTasks.filter((t) => t.overdue && t.status !== "DONE");
  }, [allTasks, overdueFilter]);

  // Group tasks for list view
  const groupedTasks = useMemo(() => {
    if (!filteredTasks.length) return { overdue: [], open: [], inProgress: [], done: [] };
    const overdue: Task[] = [];
    const open: Task[] = [];
    const inProgress: Task[] = [];
    const done: Task[] = [];

    for (const t of filteredTasks) {
      if (t.status === "DONE") {
        done.push(t);
      } else if (t.overdue) {
        overdue.push(t);
      } else if (t.status === "OPEN") {
        open.push(t);
      } else if (t.status === "IN_PROGRESS") {
        inProgress.push(t);
      }
    }
    return { overdue, open, inProgress, done };
  }, [filteredTasks]);

  const dealFilterLabel = dealIdFilter
    ? allTasks.find((t) => t.dealId === Number(dealIdFilter))?.dealName || `Deal #${dealIdFilter}`
    : "";

  return (
    <div className="space-y-3">
      {/* Header */}
      <div className="flex items-center justify-between">
        <h1 className="text-lg font-semibold text-text-primary hidden md:block">Tasks</h1>
        <div className="flex items-center gap-3">
          <button
            type="button"
            onClick={() => setCreateModalOpen(true)}
            className="inline-flex items-center gap-1.5 px-3 py-1.5 text-sm font-medium rounded-lg bg-gradient-to-r from-teal-600 to-teal-500 text-white shadow-sm hover:from-teal-700 hover:to-teal-600 transition-colors"
          >
            <Plus className="h-4 w-4" />
            New Task
          </button>
        </div>
      </div>

      {/* Filters + View Toggle */}
      <div className="flex items-center justify-between gap-2 md:gap-3 flex-wrap relative z-40">
        <div className="flex items-center gap-2 flex-wrap">
          {/* Status filter */}
          <MultiSelectDropdown
            label="Status"
            options={[
              { value: "OPEN", label: "Open" },
              { value: "IN_PROGRESS", label: "In Progress" },
              { value: "DONE", label: "Done" },
            ]}
            selected={statusesFilter}
            onChange={(values) => setMultiFilter("statuses", values)}
          />

          {/* Agent filter (manager only) */}
          {isManager && (
            <MultiSelectDropdown
              label="Agent"
              options={agents.map((a: UserType) => ({
                value: String(a.id),
                label: a.name,
              }))}
              selected={agentIdsFilter}
              onChange={(values) => setMultiFilter("agentIds", values)}
            />
          )}

          {/* Priority filter */}
          <MultiSelectDropdown
            label="Priority"
            options={[
              { value: "HIGH", label: "High" },
              { value: "MEDIUM", label: "Medium" },
              { value: "LOW", label: "Low" },
            ]}
            selected={prioritiesFilter}
            onChange={(values) => setMultiFilter("priorities", values)}
          />

          {/* Overdue toggle */}
          <button
            type="button"
            onClick={toggleOverdueFilter}
            className={cn(
              "inline-flex items-center gap-1 px-2.5 py-1.5 text-xs font-medium rounded-lg border transition-colors",
              overdueFilter
                ? "bg-red-50 text-red-700 border-red-300"
                : "bg-white text-text-muted border-border hover:border-slate-300"
            )}
          >
            Overdue
            {overdueCount > 0 && (
              <span className={cn(
                "text-[10px] px-1.5 py-0.5 rounded-full font-semibold",
                overdueFilter ? "bg-red-200 text-red-800" : "bg-slate-100 text-text-muted"
              )}>
                {overdueCount}
              </span>
            )}
          </button>

          {/* Clear deal filter if present */}
          {dealIdFilter && (
            <button
              type="button"
              onClick={clearDealFilter}
              className="inline-flex items-center gap-1 px-2 py-1 text-xs font-medium rounded-full bg-teal-100 text-teal-700 border border-teal-300"
            >
              {dealFilterLabel}
              <X className="h-3 w-3" />
            </button>
          )}
        </div>

        {/* View mode toggle */}
        <div className="inline-flex rounded-lg bg-slate-100 p-0.5">
          <ViewButton
            active={viewMode === "list"}
            onClick={() => setViewMode("list")}
            icon={<List className="h-4 w-4" />}
            label="List"
          />
          <ViewButton
            active={viewMode === "month"}
            onClick={() => setViewMode("month")}
            icon={<CalendarDays className="h-4 w-4" />}
            label="Month"
          />
          <ViewButton
            active={viewMode === "week"}
            onClick={() => setViewMode("week")}
            icon={<CalendarRange className="h-4 w-4" />}
            label="Week"
          />
          <ViewButton
            active={viewMode === "board"}
            onClick={() => setViewMode("board")}
            icon={<Columns3 className="h-4 w-4" />}
            label="Board"
          />
        </div>
      </div>

      {/* Loading / Error */}
      {isLoading && (
        <div className="flex items-center justify-center py-12">
          <Loader2 className="h-8 w-8 animate-spin text-primary" />
        </div>
      )}

      {isError && (
        <div className="flex items-center justify-center py-12 text-center">
          <div>
            <AlertCircle className="h-8 w-8 text-danger mx-auto mb-2" />
            <p className="text-sm text-danger font-medium">Failed to load tasks</p>
          </div>
        </div>
      )}

      {/* Empty state for non-list views */}
      {!isLoading && !isError && viewMode !== "list" && filteredTasks.length === 0 && (
        <div className="py-12 text-center text-sm text-text-muted">
          No tasks match the current filters. Try adjusting your filters or click "+ New Task" to create one.
        </div>
      )}

      {/* List View */}
      {!isLoading && !isError && viewMode === "list" && (
        <ListView
          groups={groupedTasks}
          doneCollapsed={doneCollapsed}
          setDoneCollapsed={setDoneCollapsed}
          onStatusChange={handleStatusChange}
          onTaskClick={setEditingTask}
          navigate={navigate}
        />
      )}

      {/* Month View */}
      {!isLoading && !isError && viewMode === "month" && filteredTasks.length > 0 && (
        <MonthView
          tasks={filteredTasks}
          calendarDate={calendarDate}
          onChangeDate={setCalendarDate}
          onTaskClick={setEditingTask}
        />
      )}

      {/* Board View */}
      {!isLoading && !isError && viewMode === "board" && filteredTasks.length > 0 && (
        <KanbanBoard
          tasks={filteredTasks}
          onStatusChange={handleStatusChange}
          onTaskClick={setEditingTask}
        />
      )}

      {/* Week View */}
      {!isLoading && !isError && viewMode === "week" && filteredTasks.length > 0 && (
        <WeekView
          tasks={filteredTasks}
          calendarDate={calendarDate}
          onChangeDate={setCalendarDate}
          onTaskClick={setEditingTask}
        />
      )}

      {/* Create Task Modal */}
      <CreateTaskModal
        open={createModalOpen}
        onClose={() => setCreateModalOpen(false)}
        defaultDealId={dealIdFilter ? Number(dealIdFilter) : undefined}
      />

      {/* Edit Task Modal */}
      <EditTaskModal
        open={!!editingTask}
        onClose={() => setEditingTask(null)}
        task={editingTask}
      />
    </div>
  );
}

/* --- View Toggle Button --- */

function ViewButton({
  active,
  onClick,
  icon,
  label,
}: {
  active: boolean;
  onClick: () => void;
  icon: React.ReactNode;
  label: string;
}) {
  return (
    <button
      type="button"
      onClick={onClick}
      className={cn(
        "inline-flex items-center gap-1 px-2.5 py-1.5 text-xs font-medium rounded-md transition-all",
        active
          ? "bg-white shadow-sm text-text-primary"
          : "text-text-muted hover:text-text-secondary"
      )}
    >
      {icon}
      <span className="hidden md:inline">{label}</span>
    </button>
  );
}

/* --- List View --- */

function ListView({
  groups,
  doneCollapsed,
  setDoneCollapsed,
  onStatusChange,
  onTaskClick,
  navigate,
}: {
  groups: { overdue: Task[]; open: Task[]; inProgress: Task[]; done: Task[] };
  doneCollapsed: boolean;
  setDoneCollapsed: (v: boolean) => void;
  onStatusChange: (id: number, status: string) => void;
  onTaskClick: (task: Task) => void;
  navigate: (path: string) => void;
}) {
  const isEmpty =
    groups.overdue.length === 0 &&
    groups.open.length === 0 &&
    groups.inProgress.length === 0 &&
    groups.done.length === 0;

  if (isEmpty) {
    return (
      <div className="py-12 text-center text-sm text-text-muted">
        No tasks found. Click "+ New Task" to create one.
      </div>
    );
  }

  return (
    <div className="space-y-4">
      {groups.overdue.length > 0 && (
        <TaskSection
          title="Overdue"
          count={groups.overdue.length}
          headerColor="text-red-600"
          dotColor="bg-red-500"
          tasks={groups.overdue}
          onStatusChange={onStatusChange}
          onTaskClick={onTaskClick}
          navigate={navigate}
        />
      )}
      {groups.open.length > 0 && (
        <TaskSection
          title="Open"
          count={groups.open.length}
          headerColor="text-teal-700"
          dotColor="bg-teal-500"
          tasks={groups.open}
          onStatusChange={onStatusChange}
          onTaskClick={onTaskClick}
          navigate={navigate}
        />
      )}
      {groups.inProgress.length > 0 && (
        <TaskSection
          title="In Progress"
          count={groups.inProgress.length}
          headerColor="text-blue-700"
          dotColor="bg-blue-500"
          tasks={groups.inProgress}
          onStatusChange={onStatusChange}
          onTaskClick={onTaskClick}
          navigate={navigate}
        />
      )}
      {groups.done.length > 0 && (
        <div>
          <button
            type="button"
            onClick={() => setDoneCollapsed(!doneCollapsed)}
            className="flex items-center gap-2 mb-2"
          >
            <span className="w-2 h-2 rounded-full bg-slate-400" />
            <h3 className="text-sm font-semibold text-slate-500">Done</h3>
            <span className="text-xs text-text-muted">({groups.done.length})</span>
            {doneCollapsed ? (
              <ChevronDown className="h-3.5 w-3.5 text-text-muted" />
            ) : (
              <ChevronUp className="h-3.5 w-3.5 text-text-muted" />
            )}
          </button>
          {!doneCollapsed && (
            <div className="space-y-2">
              {groups.done.map((task) => (
                <TaskCard
                  key={task.id}
                  task={task}
                  onStatusChange={onStatusChange}
                  onTaskClick={onTaskClick}
                  navigate={navigate}
                />
              ))}
            </div>
          )}
        </div>
      )}
    </div>
  );
}

function TaskSection({
  title,
  count,
  headerColor,
  dotColor,
  tasks,
  onStatusChange,
  onTaskClick,
  navigate,
}: {
  title: string;
  count: number;
  headerColor: string;
  dotColor: string;
  tasks: Task[];
  onStatusChange: (id: number, status: string) => void;
  onTaskClick: (task: Task) => void;
  navigate: (path: string) => void;
}) {
  return (
    <div>
      <div className="flex items-center gap-2 mb-2">
        <span className={cn("w-2 h-2 rounded-full", dotColor)} />
        <h3 className={cn("text-sm font-semibold", headerColor)}>{title}</h3>
        <span className="text-xs text-text-muted">({count})</span>
      </div>
      <div className="space-y-2">
        {tasks.map((task) => (
          <TaskCard
            key={task.id}
            task={task}
            onStatusChange={onStatusChange}
            onTaskClick={onTaskClick}
            navigate={navigate}
          />
        ))}
      </div>
    </div>
  );
}

function TaskCard({
  task,
  onStatusChange,
  onTaskClick,
  navigate,
}: {
  task: Task;
  onStatusChange: (id: number, status: string) => void;
  onTaskClick: (task: Task) => void;
  navigate: (path: string) => void;
}) {
  const priorityColor = PRIORITY_COLORS[task.priority] ?? PRIORITY_COLORS.LOW;

  return (
    <div
      onClick={() => onTaskClick(task)}
      className={cn(
        "bg-white rounded-xl border border-border p-3 cursor-pointer hover:shadow-[0_10px_15px_-3px_rgba(0,0,0,0.1),0_4px_6px_-4px_rgba(0,0,0,0.05)] hover:scale-[1.01] transition-all duration-200",
        "border-l-4",
        priorityColor.border,
        task.overdue && task.status !== "DONE" && "bg-red-50"
      )}
    >
      <div className="min-w-0">
        <div className="flex items-center gap-2">
          <p className={cn(
            "text-sm font-semibold text-text-primary md:truncate max-md:line-clamp-2",
            task.status === "DONE" && "line-through opacity-60"
          )}>
            {task.title}
          </p>
          {task.overdue && task.status !== "DONE" && (
            <span className="text-[10px] font-medium px-1.5 py-0.5 rounded-full bg-red-100 text-red-700 shrink-0">
              OVERDUE
            </span>
          )}
        </div>
        {task.description && (
          <p className="text-xs text-text-muted mt-0.5 line-clamp-1">{task.description}</p>
        )}
        <div className="flex items-center gap-2 mt-1 text-xs text-text-muted flex-wrap">
          {task.dealName && <span>Deal: {task.dealName}</span>}
          <span>Agent: {task.assignedToName}</span>
          {task.activityReportId && (
            <span className="text-primary">From report</span>
          )}
        </div>
      </div>
      <div className="flex items-center gap-2 mt-2 flex-wrap">
        {/* Status dropdown */}
        <select
          value={task.status}
          onClick={(e) => e.stopPropagation()}
          onChange={(e) => { e.stopPropagation(); onStatusChange(task.id, e.target.value); }}
          className={cn(
            "rounded border px-1.5 py-0.5 text-[10px] font-medium cursor-pointer",
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
        {/* Due date */}
        <span
          className={cn(
            "text-xs font-medium whitespace-nowrap",
            task.overdue && task.status !== "DONE" ? "text-danger" : "text-text-muted"
          )}
        >
          {formatDate(task.dueDate)}
        </span>
        {/* Navigate to deal */}
        {task.dealId && (
          <button
            type="button"
            onClick={(e) => { e.stopPropagation(); navigate(`/board/${task.dealId}`); }}
            className="text-xs text-primary hover:underline whitespace-nowrap"
          >
            &rarr; {task.dealName || 'Deal'}
          </button>
        )}
      </div>
    </div>
  );
}

/* --- Month View --- */

function MonthView({
  tasks,
  calendarDate,
  onChangeDate,
  onTaskClick,
}: {
  tasks: Task[];
  calendarDate: Date;
  onChangeDate: (d: Date) => void;
  onTaskClick: (task: Task) => void;
}) {
  const year = calendarDate.getFullYear();
  const month = calendarDate.getMonth();

  const monthName = calendarDate.toLocaleDateString("en-IN", {
    month: "long",
    year: "numeric",
  });

  const prevMonth = () => onChangeDate(new Date(year, month - 1, 1));
  const nextMonth = () => onChangeDate(new Date(year, month + 1, 1));

  // Build calendar grid
  const firstDay = new Date(year, month, 1);
  const startDow = (firstDay.getDay() + 6) % 7; // Mon=0
  const daysInMonth = new Date(year, month + 1, 0).getDate();

  const todayStr = new Date().toISOString().slice(0, 10);

  // Group tasks by date
  const tasksByDate = useMemo(() => {
    const map: Record<string, Task[]> = {};
    for (const t of tasks) {
      if (!t.dueDate) continue;
      const d = t.dueDate.slice(0, 10);
      if (!map[d]) map[d] = [];
      map[d].push(t);
    }
    return map;
  }, [tasks]);

  const cells: (number | null)[] = [];
  for (let i = 0; i < startDow; i++) cells.push(null);
  for (let d = 1; d <= daysInMonth; d++) cells.push(d);
  while (cells.length % 7 !== 0) cells.push(null);

  const [hoveredTask, setHoveredTask] = useState<{ task: Task; rect: DOMRect } | null>(null);

  const handleMouseEnter = useCallback(
    (e: React.MouseEvent<HTMLDivElement>, task: Task) => {
      const rect = e.currentTarget.getBoundingClientRect();
      setHoveredTask({ task, rect });
    },
    []
  );

  const handleMouseLeave = useCallback(() => {
    setHoveredTask(null);
  }, []);

  const popoverStyle = useMemo(() => {
    if (!hoveredTask) return {};
    const { rect } = hoveredTask;
    const popoverWidth = 256;
    const popoverHeight = 200;
    let top = rect.bottom + 4;
    let left = rect.left;
    if (left + popoverWidth > window.innerWidth - 8) {
      left = window.innerWidth - popoverWidth - 8;
    }
    if (left < 8) left = 8;
    if (top + popoverHeight > window.innerHeight - 8) {
      top = rect.top - popoverHeight - 4;
    }
    return { top, left };
  }, [hoveredTask]);

  return (
    <div>
      {/* Month nav */}
      <div className="flex items-center justify-between mb-4">
        <button
          type="button"
          onClick={prevMonth}
          className="p-1 rounded hover:bg-slate-100 text-text-muted"
        >
          <ChevronLeft className="h-5 w-5" />
        </button>
        <span className="text-sm font-semibold text-text-primary">{monthName}</span>
        <button
          type="button"
          onClick={nextMonth}
          className="p-1 rounded hover:bg-slate-100 text-text-muted"
        >
          <ChevronRight className="h-5 w-5" />
        </button>
      </div>

      {/* Day headers */}
      <div className="grid grid-cols-7 gap-px mb-1">
        {["Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"].map((d) => (
          <div
            key={d}
            className="text-center text-[10px] font-medium uppercase text-text-muted py-1"
          >
            {d}
          </div>
        ))}
      </div>

      {/* Grid */}
      <div className="grid grid-cols-7 gap-px bg-border rounded-lg overflow-hidden">
        {cells.map((day, idx) => {
          if (day === null) {
            return (
              <div key={`empty-${idx}`} className="bg-white p-1 min-h-[60px] md:min-h-[80px]" />
            );
          }
          const dateStr = `${year}-${String(month + 1).padStart(2, "0")}-${String(day).padStart(2, "0")}`;
          const isToday = dateStr === todayStr;
          const dayTasks = tasksByDate[dateStr] ?? [];

          return (
            <div
              key={dateStr}
              className={cn("bg-white p-1 min-h-[60px] md:min-h-[80px]", isToday && "bg-teal-50")}
            >
              <span
                className={cn(
                  "text-[11px] font-medium",
                  isToday ? "text-primary font-semibold" : "text-text-secondary"
                )}
              >
                {day}
              </span>
              <div className="mt-0.5">
                {dayTasks.slice(0, 2).map((t) => {
                  const pColor = PRIORITY_COLORS[t.priority] ?? PRIORITY_COLORS.LOW;
                  return (
                    <div
                      key={t.id}
                      className={cn(
                        "border-l-2 pl-1.5 py-0.5 mb-0.5 cursor-pointer rounded-sm",
                        pColor.border,
                        t.overdue && t.status !== "DONE" && "bg-red-50"
                      )}
                      onClick={() => onTaskClick(t)}
                      onMouseEnter={(e) => handleMouseEnter(e, t)}
                      onMouseLeave={handleMouseLeave}
                    >
                      <p className={cn(
                        "text-[10px] font-medium text-text-primary truncate",
                        t.status === "DONE" && "line-through opacity-50"
                      )}>
                        {t.title}
                      </p>
                      <p className="text-[9px] text-text-muted truncate">
                        {t.assignedToName?.split(" ")[0]}
                      </p>
                    </div>
                  );
                })}
                {dayTasks.length > 2 && (
                  <span className="text-[9px] text-text-muted">
                    +{dayTasks.length - 2} more
                  </span>
                )}
              </div>
            </div>
          );
        })}
      </div>

      {/* Hover popover */}
      {hoveredTask && (
        <div
          className="fixed z-50 bg-white rounded-lg border border-border shadow-lg p-3 w-64 pointer-events-none"
          style={popoverStyle}
        >
          <p className="text-sm font-semibold text-text-primary">{hoveredTask.task.title}</p>
          <div className="mt-1.5 space-y-1 text-xs text-text-secondary">
            {hoveredTask.task.description && (
              <p className="line-clamp-2">{hoveredTask.task.description}</p>
            )}
            <p>
              <span className="text-text-muted">Agent:</span> {hoveredTask.task.assignedToName}
            </p>
            <p>
              <span className="text-text-muted">Priority:</span> {hoveredTask.task.priority}
            </p>
            <p>
              <span className="text-text-muted">Status:</span> {hoveredTask.task.status.replace("_", " ")}
            </p>
            <p>
              <span className="text-text-muted">Due:</span> {formatDate(hoveredTask.task.dueDate)}
            </p>
            {hoveredTask.task.dealName && (
              <p>
                <span className="text-text-muted">Deal:</span> {hoveredTask.task.dealName}
              </p>
            )}
          </div>
        </div>
      )}
    </div>
  );
}

/* --- Week View --- */

function WeekView({
  tasks,
  calendarDate,
  onChangeDate,
  onTaskClick,
}: {
  tasks: Task[];
  calendarDate: Date;
  onChangeDate: (d: Date) => void;
  onTaskClick: (task: Task) => void;
}) {

  // Get Monday of current week
  const dayOfWeek = (calendarDate.getDay() + 6) % 7;
  const monday = new Date(calendarDate);
  monday.setDate(calendarDate.getDate() - dayOfWeek);

  const weekDates = Array.from({ length: 7 }, (_, i) => {
    const d = new Date(monday);
    d.setDate(monday.getDate() + i);
    return d;
  });

  const todayStr = new Date().toISOString().slice(0, 10);

  const prevWeek = () => {
    const d = new Date(calendarDate);
    d.setDate(d.getDate() - 7);
    onChangeDate(d);
  };
  const nextWeek = () => {
    const d = new Date(calendarDate);
    d.setDate(d.getDate() + 7);
    onChangeDate(d);
  };

  const tasksByDate = useMemo(() => {
    const map: Record<string, Task[]> = {};
    for (const t of tasks) {
      if (!t.dueDate) continue;
      const d = t.dueDate.slice(0, 10);
      if (!map[d]) map[d] = [];
      map[d].push(t);
    }
    return map;
  }, [tasks]);

  const weekLabel = `${weekDates[0].toLocaleDateString("en-IN", { day: "numeric", month: "short" })} -- ${weekDates[6].toLocaleDateString("en-IN", { day: "numeric", month: "short", year: "numeric" })}`;

  return (
    <div>
      {/* Week nav */}
      <div className="flex items-center justify-between mb-4">
        <button
          type="button"
          onClick={prevWeek}
          className="p-1 rounded hover:bg-slate-100 text-text-muted"
        >
          <ChevronLeft className="h-5 w-5" />
        </button>
        <span className="text-sm font-semibold text-text-primary">{weekLabel}</span>
        <button
          type="button"
          onClick={nextWeek}
          className="p-1 rounded hover:bg-slate-100 text-text-muted"
        >
          <ChevronRight className="h-5 w-5" />
        </button>
      </div>

      {/* Columns */}
      <div className="grid grid-cols-7 gap-1.5">
        {weekDates.map((date) => {
          const dateStr = date.toISOString().slice(0, 10);
          const isToday = dateStr === todayStr;
          const dayTasks = tasksByDate[dateStr] ?? [];
          const dayLabel = date.toLocaleDateString("en-IN", {
            weekday: "short",
            day: "numeric",
          });

          return (
            <div key={dateStr} className="min-h-[200px]">
              <div
                className={cn(
                  "text-center text-[11px] font-medium py-1 rounded-t-lg",
                  isToday ? "bg-teal-100 text-teal-700" : "bg-slate-100 text-text-muted"
                )}
              >
                {dayLabel}
              </div>
              <div className="space-y-1 mt-1">
                {dayTasks.map((t) => {
                  const pColor = PRIORITY_COLORS[t.priority] ?? PRIORITY_COLORS.LOW;
                  return (
                    <button
                      key={t.id}
                      type="button"
                      onClick={() => onTaskClick(t)}
                      className={cn(
                        "w-full text-left p-1.5 rounded border border-l-2 text-[10px]",
                        "hover:shadow-sm transition-colors",
                        t.overdue && t.status !== "DONE" ? "bg-red-50" : pColor.bg,
                        pColor.border,
                        t.overdue && t.status !== "DONE"
                          ? "border-t-red-200 border-r-red-200 border-b-red-200"
                          : "border-t-border border-r-border border-b-border",
                        "cursor-pointer"
                      )}
                    >
                      <p
                        className={cn(
                          "font-medium text-text-primary truncate",
                          t.status === "DONE" && "line-through opacity-50"
                        )}
                      >
                        {t.title}
                      </p>
                      <p className="text-text-muted truncate">
                        {t.assignedToName?.split(" ")[0]}
                      </p>
                      {t.overdue && t.status !== "DONE" && (
                        <p className="text-red-600 font-medium mt-0.5">overdue</p>
                      )}
                    </button>
                  );
                })}
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
}
