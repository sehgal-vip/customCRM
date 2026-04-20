import { useState } from "react";
import { useForm } from "react-hook-form";
import { z } from "zod";
import { zodResolver } from "@hookform/resolvers/zod";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { X, Loader2, Trash2 } from "lucide-react";
import { toast } from "sonner";
import { tasksApi, usersApi, dealsApi } from "../../services/apiEndpoints";
import { cn, getErrorMessage } from "../../lib/utils";
import { formatDate } from "../../lib/formatters";
import { useAuth } from "../../context/AuthContext";
import { useDialog } from "../../hooks/useDialog";
import TaskNotes from "../tasks/TaskNotes";
import type { Task, User as UserType, DealListItem } from "../../types";

const schema = z.object({
  title: z.string().min(1, "Title is required").max(200, "Max 200 characters"),
  description: z.string().max(1000).optional(),
  assignedToId: z.coerce.number().min(1, "Select an agent"),
  dealId: z.coerce.number().optional(),
  dueDate: z.string().min(1, "Due date is required"),
  priority: z.enum(["HIGH", "MEDIUM", "LOW"]),
  status: z.enum(["OPEN", "IN_PROGRESS", "DONE"]),
});

type FormValues = z.infer<typeof schema>;

interface EditTaskModalProps {
  open: boolean;
  onClose: () => void;
  task: Task | null;
}

export default function EditTaskModal({ open, onClose, task }: EditTaskModalProps) {
  const dialogRef = useDialog(open);
  const queryClient = useQueryClient();
  const { user, isManager } = useAuth();
  const [showDeleteConfirm, setShowDeleteConfirm] = useState(false);

  const invalidateTaskQueries = () => {
    queryClient.invalidateQueries({ queryKey: ["tasks"] });
    queryClient.invalidateQueries({ queryKey: ["deal-tasks"] });
  };

  const {
    register,
    handleSubmit,
    reset,
    formState: { errors, isDirty },
  } = useForm<FormValues>({
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    resolver: zodResolver(schema) as any,
    mode: "onChange",
  });

  // Fetch users for agent dropdown
  const { data: users } = useQuery({
    queryKey: ["users"],
    queryFn: () => usersApi.list(),
    enabled: open,
  });

  // Fetch deals for deal dropdown
  const { data: dealsPage } = useQuery({
    queryKey: ["deals-list-for-task"],
    queryFn: () => dealsApi.list({ size: "200", status: "ACTIVE" }),
    enabled: open,
    staleTime: 5 * 60 * 1000,
  });

  const agents = (users ?? []).filter((u: UserType) => u.status === "ACTIVE");
  const deals = dealsPage?.content ?? [];

  const updateMutation = useMutation({
    mutationFn: (data: FormValues) => {
      const payload: Record<string, unknown> = {
        title: data.title,
        description: data.description || undefined,
        priority: data.priority,
        assignedToId: data.assignedToId,
        dueDate: data.dueDate,
      };
      if (data.dealId) payload.dealId = data.dealId;
      return tasksApi.update(task!.id, payload);
    },
    onSuccess: () => {
      invalidateTaskQueries();
      toast.success("Task updated");
      onClose();
    },
    onError: (err: unknown) => {
      toast.error(getErrorMessage(err, "Failed to update task"));
    },
  });

  const statusMutation = useMutation({
    mutationFn: (status: string) => tasksApi.updateStatus(task!.id, status),
    onSuccess: invalidateTaskQueries,
    onError: (err: unknown) => {
      toast.error(getErrorMessage(err, "Failed to update task status"));
      invalidateTaskQueries();
    },
  });

  const deleteMutation = useMutation({
    mutationFn: () => tasksApi.delete(task!.id),
    onSuccess: () => {
      invalidateTaskQueries();
      toast.success("Task deleted");
      onClose();
    },
    onError: (err: unknown) => {
      toast.error(getErrorMessage(err, "Failed to delete task"));
    },
  });

  // Render-time sync from task prop when modal opens
  const [prevTaskKey, setPrevTaskKey] = useState<string | null>(null);
  const taskKey = open && task ? `${task.id}-${task.createdAt}` : null;
  if (taskKey && taskKey !== prevTaskKey) {
    setPrevTaskKey(taskKey);
    reset({
      title: task!.title,
      description: task!.description || "",
      assignedToId: task!.assignedToId,
      dealId: task!.dealId || undefined,
      dueDate: task!.dueDate,
      priority: task!.priority,
      status: task!.status,
    });
    setShowDeleteConfirm(false);
  }
  if (!open && prevTaskKey) {
    setPrevTaskKey(null);
  }

  const onSubmit = (data: FormValues) => {
    // If status changed, update status separately
    if (task && data.status !== task.status) {
      statusMutation.mutate(data.status);
    }
    // If other fields changed, update the task
    if (isDirty) {
      updateMutation.mutate(data);
    } else if (task && data.status !== task.status) {
      // Only status changed, close after status update
      onClose();
    }
  };

  if (!open || !task) return null;

  return (
    <dialog
      ref={dialogRef}
      onCancel={onClose}
      className="backdrop:bg-black/60 backdrop:backdrop-blur-sm bg-transparent p-0 m-auto rounded-2xl max-w-2xl w-full max-md:mx-3"
    >
      <div className="bg-white rounded-2xl shadow-[0_25px_60px_rgba(0,0,0,0.25),0_10px_20px_rgba(0,0,0,0.1)] max-h-[90vh] flex flex-col">
        {/* Header */}
        <div className="flex items-center justify-between px-5 py-3 border-b border-border">
          <h2 className="text-lg font-semibold text-text-primary">Task Details</h2>
          <button
            type="button"
            aria-label="Close"
            onClick={onClose}
            className="p-1 rounded-lg hover:bg-slate-100 text-text-muted transition-colors"
          >
            <X className="h-5 w-5" />
          </button>
        </div>

        {/* Body */}
        <div className="flex-1 overflow-y-auto px-5 py-4 space-y-4">
          <form id="edit-task-form" onSubmit={handleSubmit(onSubmit)} className="space-y-3">
            {/* Title */}
            <div>
              <label className="block text-[11px] font-medium uppercase tracking-wider text-text-muted mb-1">
                Title *
              </label>
              <input
                {...register("title")}
                autoFocus
                maxLength={200}
                className={cn(
                  "w-full rounded-lg border px-3 py-2 text-sm text-text-primary",
                  "focus:outline-none focus:ring-2 focus:ring-primary/40 focus:border-primary",
                  errors.title ? "border-danger" : "border-border"
                )}
              />
              {errors.title && (
                <span className="text-xs text-danger mt-1 block">{errors.title.message}</span>
              )}
            </div>

            {/* Description */}
            <div>
              <label className="block text-[11px] font-medium uppercase tracking-wider text-text-muted mb-1">
                Description
              </label>
              <textarea
                {...register("description")}
                rows={3}
                maxLength={1000}
                className="w-full rounded-lg border border-border px-3 py-2 text-sm text-text-primary focus:outline-none focus:ring-2 focus:ring-primary/40 focus:border-primary resize-none"
              />
            </div>

            {/* Two-column row: Agent + Deal */}
            <div className="grid grid-cols-2 gap-3">
              {/* Assigned Agent */}
              <div>
                <label className="block text-[11px] font-medium uppercase tracking-wider text-text-muted mb-1">
                  Assigned Agent *
                </label>
                <select
                  {...register("assignedToId")}
                  disabled={!isManager}
                  className={cn(
                    "w-full rounded-lg border px-3 py-2 text-sm text-text-primary bg-white",
                    "focus:outline-none focus:ring-2 focus:ring-primary/40 focus:border-primary",
                    errors.assignedToId ? "border-danger" : "border-border",
                    !isManager && "opacity-60"
                  )}
                >
                  {isManager ? (
                    <>
                      <option value="">Select agent...</option>
                      {agents.map((u: UserType) => (
                        <option key={u.id} value={u.id}>
                          {u.name} {u.role === "MANAGER" ? "[Manager]" : ""}
                        </option>
                      ))}
                    </>
                  ) : (
                    <option value={user?.id}>{user?.name}</option>
                  )}
                </select>
              </div>

              {/* Deal */}
              <div>
                <label className="block text-[11px] font-medium uppercase tracking-wider text-text-muted mb-1">
                  Linked Deal
                </label>
                <select
                  {...register("dealId")}
                  className="w-full rounded-lg border border-border px-3 py-2 text-sm text-text-primary bg-white focus:outline-none focus:ring-2 focus:ring-primary/40 focus:border-primary"
                >
                  <option value="">None</option>
                  {deals.map((d: DealListItem) => (
                    <option key={d.id} value={d.id}>
                      {d.name} -- {d.operatorName}
                    </option>
                  ))}
                </select>
              </div>
            </div>

            {/* Two-column row: Due Date + Status */}
            <div className="grid grid-cols-2 gap-3">
              {/* Due Date */}
              <div>
                <label className="block text-[11px] font-medium uppercase tracking-wider text-text-muted mb-1">
                  Due Date *
                </label>
                <input
                  {...register("dueDate")}
                  type="date"
                  className={cn(
                    "w-full rounded-lg border px-3 py-2 text-sm text-text-primary",
                    "focus:outline-none focus:ring-2 focus:ring-primary/40 focus:border-primary",
                    errors.dueDate ? "border-danger" : "border-border"
                  )}
                />
              </div>

              {/* Status */}
              <div>
                <label className="block text-[11px] font-medium uppercase tracking-wider text-text-muted mb-1">
                  Status
                </label>
                <select
                  {...register("status")}
                  className="w-full rounded-lg border border-border px-3 py-2 text-sm text-text-primary bg-white focus:outline-none focus:ring-2 focus:ring-primary/40 focus:border-primary"
                >
                  <option value="OPEN">Open</option>
                  <option value="IN_PROGRESS">In Progress</option>
                  <option value="DONE">Done</option>
                </select>
              </div>
            </div>

            {/* Priority */}
            <div>
              <label className="block text-[11px] font-medium uppercase tracking-wider text-text-muted mb-1">
                Priority
              </label>
              <div className="flex gap-3">
                {(["HIGH", "MEDIUM", "LOW"] as const).map((p) => (
                  <label key={p} className="flex items-center gap-1.5 cursor-pointer">
                    <input
                      {...register("priority")}
                      type="radio"
                      value={p}
                      className="accent-primary"
                    />
                    <span
                      className={cn(
                        "text-sm font-medium",
                        p === "HIGH" ? "text-red-600" :
                        p === "MEDIUM" ? "text-amber-600" : "text-slate-500"
                      )}
                    >
                      {p === "HIGH" ? "High" : p === "MEDIUM" ? "Medium" : "Low"}
                    </span>
                  </label>
                ))}
              </div>
            </div>
          </form>

          {/* Read-only metadata */}
          <div className="rounded-lg bg-slate-50 border border-border px-4 py-3">
            <div className="grid grid-cols-2 gap-2 text-xs">
              <div>
                <span className="text-text-muted">Created by:</span>{" "}
                <span className="text-text-primary font-medium">{task.createdByName}</span>
              </div>
              <div>
                <span className="text-text-muted">Created:</span>{" "}
                <span className="text-text-primary font-medium">{formatDate(task.createdAt)}</span>
              </div>
              {task.completedAt && (
                <div>
                  <span className="text-text-muted">Completed:</span>{" "}
                  <span className="text-text-primary font-medium">{formatDate(task.completedAt)}</span>
                </div>
              )}
              {task.activityReportId && (
                <div>
                  <span className="text-text-muted">Activity Report:</span>{" "}
                  <span className="text-primary font-medium">Linked</span>
                </div>
              )}
            </div>
          </div>

          {/* Notes */}
          <div className="border-t border-border pt-4">
            <h3 className="text-sm font-semibold text-text-primary mb-3">Notes</h3>
            <TaskNotes taskId={task.id} />
          </div>

          {/* Error messages */}
          {updateMutation.isError && (
            <div className="rounded-lg bg-red-50 border border-red-200 px-4 py-3 text-sm text-danger">
              Failed to update task. Please try again.
            </div>
          )}
        </div>

        {/* Footer */}
        <div className="flex items-center justify-between px-5 py-3 border-t border-border">
          <div>
            {isManager && !showDeleteConfirm && (
              <button
                type="button"
                onClick={() => setShowDeleteConfirm(true)}
                className="inline-flex items-center gap-1.5 px-3 py-1.5 text-sm text-danger hover:bg-red-50 rounded-lg transition-colors"
              >
                <Trash2 className="h-3.5 w-3.5" />
                Delete
              </button>
            )}
            {isManager && showDeleteConfirm && (
              <div className="flex items-center gap-2">
                <span className="text-xs text-danger font-medium">Delete this task?</span>
                <button
                  type="button"
                  onClick={() => deleteMutation.mutate()}
                  disabled={deleteMutation.isPending}
                  className="px-2 py-1 text-xs font-medium text-white bg-danger rounded-md hover:bg-red-700 disabled:opacity-50"
                >
                  {deleteMutation.isPending ? "Deleting..." : "Yes, delete"}
                </button>
                <button
                  type="button"
                  onClick={() => setShowDeleteConfirm(false)}
                  className="px-2 py-1 text-xs text-text-secondary hover:bg-slate-100 rounded-md"
                >
                  Cancel
                </button>
              </div>
            )}
          </div>

          <div className="flex items-center gap-3">
            <button
              type="button"
              onClick={onClose}
              className="px-4 py-2 text-sm rounded-lg text-text-secondary hover:bg-slate-100 transition-colors"
            >
              Cancel
            </button>
            <button
              type="submit"
              form="edit-task-form"
              disabled={updateMutation.isPending}
              className={cn(
                "px-4 py-2 text-sm font-medium rounded-lg text-white shadow-sm transition-colors",
                "bg-gradient-to-r from-teal-600 to-teal-500 hover:from-teal-700 hover:to-teal-600",
                "disabled:opacity-50 disabled:cursor-not-allowed"
              )}
            >
              {updateMutation.isPending ? (
                <span className="inline-flex items-center gap-2">
                  <Loader2 className="h-4 w-4 animate-spin" />
                  Saving...
                </span>
              ) : (
                "Save Changes"
              )}
            </button>
          </div>
        </div>
      </div>
    </dialog>
  );
}
