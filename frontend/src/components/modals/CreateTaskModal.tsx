import { useEffect, useRef } from "react";
import { useForm } from "react-hook-form";
import { z } from "zod";
import { zodResolver } from "@hookform/resolvers/zod";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { X, Loader2 } from "lucide-react";
import { tasksApi, usersApi, dealsApi } from "../../services/apiEndpoints";
import { cn } from "../../lib/utils";
import { useAuth } from "../../context/AuthContext";
import type { User as UserType, DealListItem } from "../../types";

const schema = z.object({
  title: z.string().min(1, "Title is required").max(200, "Max 200 characters"),
  description: z.string().max(1000).optional(),
  assignedToId: z.coerce.number().min(1, "Select an agent"),
  dealId: z.coerce.number().optional(),
  dueDate: z.string().min(1, "Due date is required"),
  priority: z.enum(["HIGH", "MEDIUM", "LOW"]),
});

type FormValues = z.infer<typeof schema>;

interface CreateTaskModalProps {
  open: boolean;
  onClose: () => void;
  defaultDealId?: number;
}

export default function CreateTaskModal({ open, onClose, defaultDealId }: CreateTaskModalProps) {
  const dialogRef = useRef<HTMLDialogElement>(null);
  const queryClient = useQueryClient();
  const { user, isManager } = useAuth();

  const {
    register,
    handleSubmit,
    reset,
    formState: { errors, isValid },
  } = useForm<FormValues>({
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    resolver: zodResolver(schema) as any,
    mode: "onChange",
    defaultValues: {
      priority: "MEDIUM",
      assignedToId: isManager ? undefined : user?.id,
      dealId: defaultDealId,
    },
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
  });

  const agents = (users ?? []).filter((u: UserType) => u.status === "ACTIVE");
  const deals = dealsPage?.content ?? [];

  const mutation = useMutation({
    mutationFn: (data: FormValues) => {
      const payload: Record<string, unknown> = {
        title: data.title,
        description: data.description || undefined,
        priority: data.priority,
        assignedToId: data.assignedToId,
        dueDate: data.dueDate,
      };
      if (data.dealId) payload.dealId = data.dealId;
      return tasksApi.create(payload);
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["tasks"] });
      queryClient.invalidateQueries({ queryKey: ["deal-tasks"] });
      reset();
      onClose();
    },
  });

  useEffect(() => {
    const dlg = dialogRef.current;
    if (!dlg) return;
    if (open && !dlg.open) {
      dlg.showModal();
    } else if (!open && dlg.open) {
      dlg.close();
    }
  }, [open]);

  useEffect(() => {
    if (!open) {
      reset({
        priority: "MEDIUM",
        assignedToId: isManager ? undefined : user?.id,
        dealId: defaultDealId,
      });
    }
  }, [open, reset, isManager, user?.id, defaultDealId]);

  const onSubmit = (data: FormValues) => {
    mutation.mutate(data);
  };

  if (!open) return null;

  return (
    <dialog
      ref={dialogRef}
      onCancel={onClose}
      className="backdrop:bg-black/60 backdrop:backdrop-blur-sm bg-transparent p-0 m-auto rounded-2xl max-w-lg w-full max-md:mx-3"
    >
      <div className="bg-white rounded-2xl shadow-[0_25px_60px_rgba(0,0,0,0.25),0_10px_20px_rgba(0,0,0,0.1)] max-h-[90vh] flex flex-col">
        {/* Header */}
        <div className="flex items-center justify-between px-6 py-4 border-b border-border">
          <h2 className="text-lg font-semibold text-text-primary">New Task</h2>
          <button
            type="button"
            onClick={onClose}
            className="p-1 rounded-lg hover:bg-slate-100 text-text-muted transition-colors"
          >
            <X className="h-5 w-5" />
          </button>
        </div>

        {/* Body */}
        <form
          onSubmit={handleSubmit(onSubmit)}
          className="flex-1 overflow-y-auto px-6 py-5 space-y-4"
        >
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
              placeholder="e.g. Follow up on pricing proposal"
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
              placeholder="Optional details..."
            />
          </div>

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
            {errors.assignedToId && (
              <span className="text-xs text-danger mt-1 block">{errors.assignedToId.message}</span>
            )}
          </div>

          {/* Deal (optional) */}
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
            {errors.dueDate && (
              <span className="text-xs text-danger mt-1 block">{errors.dueDate.message}</span>
            )}
          </div>

          {/* Priority */}
          <div>
            <label className="block text-[11px] font-medium uppercase tracking-wider text-text-muted mb-1">
              Priority
            </label>
            <div className="flex gap-3">
              {(["HIGH", "MEDIUM", "LOW"] as const).map((p) => (
                <label
                  key={p}
                  className="flex items-center gap-1.5 cursor-pointer"
                >
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

          {/* Error message */}
          {mutation.isError && (
            <div className="rounded-lg bg-red-50 border border-red-200 px-4 py-3 text-sm text-danger">
              Failed to create task. Please try again.
            </div>
          )}
        </form>

        {/* Footer */}
        <div className="flex items-center justify-end gap-3 px-6 py-4 border-t border-border">
          <button
            type="button"
            onClick={onClose}
            className="px-4 py-2 text-sm rounded-lg text-text-secondary hover:bg-slate-100 transition-colors"
          >
            Cancel
          </button>
          <button
            type="submit"
            disabled={!isValid || mutation.isPending}
            onClick={handleSubmit(onSubmit)}
            className={cn(
              "px-4 py-2 text-sm font-medium rounded-lg text-white shadow-sm transition-colors",
              "bg-gradient-to-r from-teal-600 to-teal-500 hover:from-teal-700 hover:to-teal-600",
              "disabled:opacity-50 disabled:cursor-not-allowed"
            )}
          >
            {mutation.isPending ? (
              <span className="inline-flex items-center gap-2">
                <Loader2 className="h-4 w-4 animate-spin" />
                Creating...
              </span>
            ) : (
              "Create Task"
            )}
          </button>
        </div>
      </div>
    </dialog>
  );
}
