import { useEffect, useRef, useState } from "react";
import { useForm } from "react-hook-form";
import { z } from "zod";
import { zodResolver } from "@hookform/resolvers/zod";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { X, Loader2 } from "lucide-react";
import { toast } from "sonner";
import { dealsApi, operatorsApi, usersApi } from "../../services/apiEndpoints";
import { LEAD_SOURCES, LEAD_SOURCE_VALUES } from "../../lib/constants";
import { cn, getErrorMessage } from "../../lib/utils";

const schema = z.object({
  name: z.string().min(1, "Deal name is required").max(100, "Max 100 characters"),
  operatorId: z.coerce.number().min(1, "Select an operator"),
  assignedAgentId: z.coerce.number().min(1, "Select an agent"),
  fleetSize: z.coerce.number().int().positive().optional(),
  estimatedMonthlyValue: z.coerce.number().positive().optional(),
  leadSource: z.string().min(1, "Select a lead source"),
});

type FormValues = z.infer<typeof schema>;

interface CreateDealModalProps {
  open: boolean;
  onClose: () => void;
}

export default function CreateDealModal({ open, onClose }: CreateDealModalProps) {
  const dialogRef = useRef<HTMLDialogElement>(null);
  const queryClient = useQueryClient();

  const {
    register,
    handleSubmit,
    watch,
    reset,
    formState: { errors, isValid },
  } = useForm<FormValues>({
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    resolver: zodResolver(schema) as any,
    mode: "onChange",
  });

  const dealName = watch("name") || "";

  // Fetch operators & users
  const { data: operatorsPage } = useQuery({
    queryKey: ["operators"],
    queryFn: () => operatorsApi.list({ size: "200" }),
    enabled: open,
  });

  const { data: users } = useQuery({
    queryKey: ["users"],
    queryFn: () => usersApi.list(),
    enabled: open,
  });

  const operators = operatorsPage?.content ?? [];

  const mutation = useMutation({
    mutationFn: (data: FormValues) => dealsApi.create(data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["deals"] });
      toast.success("Deal created");
      reset();
      onClose();
    },
    onError: (err: unknown) => {
      toast.error(getErrorMessage(err, "Failed to create deal"));
    },
  });

  // Render-time reset when modal closes
  const [wasOpen, setWasOpen] = useState(false);
  if (open && !wasOpen) {
    setWasOpen(true);
  }
  if (!open && wasOpen) {
    setWasOpen(false);
    reset();
  }

  // Dialog open/close
  useEffect(() => {
    const dlg = dialogRef.current;
    if (!dlg) return;
    if (open && !dlg.open) {
      dlg.showModal();
    } else if (!open && dlg.open) {
      dlg.close();
    }
  }, [open]);

  const onSubmit = (data: FormValues) => {
    const payload = {
      ...data,
      leadSource: LEAD_SOURCE_VALUES[data.leadSource] || data.leadSource,
    };
    mutation.mutate(payload as unknown as FormValues);
  };

  if (!open) return null;

  return (
    <dialog
      ref={dialogRef}
      onCancel={onClose}
      className="backdrop:bg-black/50 backdrop:backdrop-blur-sm bg-transparent p-0 m-auto rounded-2xl max-w-lg w-full"
    >
      <div className="bg-white rounded-2xl shadow-[0_25px_50px_rgba(0,0,0,0.15)] max-h-[90vh] flex flex-col">
        {/* Header */}
        <div className="flex items-center justify-between px-6 py-4 border-b border-border">
          <h2 className="text-lg font-semibold text-text-primary">
            Create New Deal
          </h2>
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
        <form
          onSubmit={handleSubmit(onSubmit)}
          className="flex-1 overflow-y-auto px-6 py-5 space-y-4"
        >
          {/* Deal Name */}
          <div>
            <label className="block text-[11px] font-medium uppercase tracking-wider text-text-muted mb-1">
              Deal Name
            </label>
            <input
              {...register("name")}
              autoFocus
              maxLength={100}
              className={cn(
                "w-full rounded-lg border px-3 py-2 text-sm text-text-primary",
                "focus:outline-none focus:ring-2 focus:ring-primary/40 focus:border-primary",
                errors.name ? "border-danger" : "border-border"
              )}
              placeholder="e.g. Mumbai Fleet Expansion"
            />
            <div className="flex justify-between mt-1">
              {errors.name && (
                <span className="text-xs text-danger">{errors.name.message}</span>
              )}
              <span className="text-[11px] text-text-muted ml-auto">
                {dealName.length}/100
              </span>
            </div>
          </div>

          {/* Operator */}
          <div>
            <label className="block text-[11px] font-medium uppercase tracking-wider text-text-muted mb-1">
              Operator
            </label>
            <select
              {...register("operatorId")}
              className={cn(
                "w-full rounded-lg border px-3 py-2 text-sm text-text-primary bg-white",
                "focus:outline-none focus:ring-2 focus:ring-primary/40 focus:border-primary",
                errors.operatorId ? "border-danger" : "border-border"
              )}
            >
              <option value="">Select operator...</option>
              {operators.map((op) => (
                <option key={op.id} value={op.id}>
                  {op.companyName}
                </option>
              ))}
            </select>
            {errors.operatorId && (
              <span className="text-xs text-danger mt-1 block">
                {errors.operatorId.message}
              </span>
            )}
          </div>

          {/* Assigned Agent */}
          <div>
            <label className="block text-[11px] font-medium uppercase tracking-wider text-text-muted mb-1">
              Assigned Agent
            </label>
            <select
              {...register("assignedAgentId")}
              className={cn(
                "w-full rounded-lg border px-3 py-2 text-sm text-text-primary bg-white",
                "focus:outline-none focus:ring-2 focus:ring-primary/40 focus:border-primary",
                errors.assignedAgentId ? "border-danger" : "border-border"
              )}
            >
              <option value="">Select agent...</option>
              {(users ?? []).map((u) => (
                <option key={u.id} value={u.id}>
                  {u.name}
                  {u.role === "MANAGER" ? " [Manager]" : ""}
                </option>
              ))}
            </select>
            {errors.assignedAgentId && (
              <span className="text-xs text-danger mt-1 block">
                {errors.assignedAgentId.message}
              </span>
            )}
          </div>

          {/* Fleet Size */}
          <div>
            <label className="block text-[11px] font-medium uppercase tracking-wider text-text-muted mb-1">
              Fleet Size
            </label>
            <input
              {...register("fleetSize")}
              type="number"
              min={1}
              className="w-full rounded-lg border border-border px-3 py-2 text-sm text-text-primary focus:outline-none focus:ring-2 focus:ring-primary/40 focus:border-primary"
              placeholder="Number of buses"
            />
          </div>

          {/* Estimated Monthly Value */}
          <div>
            <label className="block text-[11px] font-medium uppercase tracking-wider text-text-muted mb-1">
              Estimated Monthly Value
            </label>
            <div className="relative">
              <span className="absolute left-3 top-1/2 -translate-y-1/2 text-sm text-text-muted">
                INR
              </span>
              <input
                {...register("estimatedMonthlyValue")}
                type="number"
                min={0}
                className="w-full rounded-lg border border-border pl-12 pr-3 py-2 text-sm text-text-primary focus:outline-none focus:ring-2 focus:ring-primary/40 focus:border-primary"
                placeholder="0"
              />
            </div>
          </div>

          {/* Lead Source */}
          <div>
            <label className="block text-[11px] font-medium uppercase tracking-wider text-text-muted mb-1">
              Lead Source
            </label>
            <select
              {...register("leadSource")}
              className={cn(
                "w-full rounded-lg border px-3 py-2 text-sm text-text-primary bg-white",
                "focus:outline-none focus:ring-2 focus:ring-primary/40 focus:border-primary",
                errors.leadSource ? "border-danger" : "border-border"
              )}
            >
              <option value="">Select source...</option>
              {LEAD_SOURCES.map((src) => (
                <option key={src} value={src}>
                  {src}
                </option>
              ))}
            </select>
            {errors.leadSource && (
              <span className="text-xs text-danger mt-1 block">
                {errors.leadSource.message}
              </span>
            )}
          </div>

          {/* Error message */}
          {mutation.isError && (
            <div className="rounded-lg bg-red-50 border border-red-200 px-4 py-3 text-sm text-danger">
              Failed to create deal. Please try again.
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
              "bg-primary hover:bg-primary-hover",
              "disabled:opacity-50 disabled:cursor-not-allowed"
            )}
          >
            {mutation.isPending ? (
              <span className="inline-flex items-center gap-2">
                <Loader2 className="h-4 w-4 animate-spin" />
                Creating...
              </span>
            ) : (
              "Create Deal"
            )}
          </button>
        </div>
      </div>
    </dialog>
  );
}
