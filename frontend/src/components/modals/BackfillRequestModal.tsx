import { useEffect, useRef, useState } from "react";
import { useMutation, useQueryClient } from "@tanstack/react-query";
import { X, Loader2, RotateCcw } from "lucide-react";
import { toast } from "sonner";
import { backfillApi } from "../../services/apiEndpoints";
import { STAGE_NAMES } from "../../lib/constants";
import { cn, getErrorMessage } from "../../lib/utils";

interface BackfillRequestModalProps {
  open: boolean;
  onClose: () => void;
  dealId: number;
  dealName: string;
}

const TARGET_STAGES = [
  "STAGE_2", "STAGE_3", "STAGE_4", "STAGE_5",
  "STAGE_6", "STAGE_7", "STAGE_8",
];

const LAUNCH_DATE = "2026-04-01";

export default function BackfillRequestModal({
  open,
  onClose,
  dealId,
  dealName,
}: BackfillRequestModalProps) {
  const dialogRef = useRef<HTMLDialogElement>(null);
  const queryClient = useQueryClient();

  const [targetStage, setTargetStage] = useState("");
  const [originalStartDate, setOriginalStartDate] = useState("");
  const [context, setContext] = useState("");
  const [dateError, setDateError] = useState("");

  const mutation = useMutation({
    mutationFn: () =>
      backfillApi.request(dealId, {
        targetStage,
        originalStartDate,
        context,
      }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["deal", dealId] });
      toast.success("Backfill request submitted");
      onClose();
    },
    onError: (err: unknown) => {
      toast.error(getErrorMessage(err, "Failed to submit backfill request"));
    },
  });

  // Render-time reset when modal closes
  const [wasOpen, setWasOpen] = useState(false);
  if (open && !wasOpen) {
    setWasOpen(true);
  }
  if (!open && wasOpen) {
    setWasOpen(false);
    setTargetStage("");
    setOriginalStartDate("");
    setContext("");
    setDateError("");
  }

  // Render-time date validation
  const computedDateError =
    originalStartDate && originalStartDate >= LAUNCH_DATE
      ? "Date must be before launch date (April 1, 2026)"
      : "";
  if (computedDateError !== dateError) {
    setDateError(computedDateError);
  }

  useEffect(() => {
    const dlg = dialogRef.current;
    if (!dlg) return;
    if (open && !dlg.open) {
      dlg.showModal();
    } else if (!open && dlg.open) {
      dlg.close();
    }
  }, [open]);

  const isValid =
    targetStage && originalStartDate && context.trim() && !dateError;

  if (!open) return null;

  return (
    <dialog
      ref={dialogRef}
      onCancel={onClose}
      className="backdrop:bg-black/50 backdrop:backdrop-blur-sm bg-transparent p-0 m-auto md:rounded-2xl rounded-t-2xl max-w-md w-full fixed md:relative bottom-0 md:bottom-auto"
    >
      <div className="bg-white rounded-2xl md:rounded-2xl rounded-b-none md:rounded-b-2xl shadow-[0_25px_50px_rgba(0,0,0,0.15)] max-h-[90vh] flex flex-col">
        {/* Drag handle (mobile) */}
        <div className="flex justify-center pt-2 md:hidden">
          <div className="w-10 h-1 rounded-full bg-slate-300" />
        </div>

        {/* Header */}
        <div className="flex items-center justify-between px-6 py-4 border-b border-border">
          <div className="flex items-center gap-2">
            <RotateCcw className="h-5 w-5 text-primary" />
            <h2 className="text-lg font-semibold text-text-primary">
              Request Backfill Placement
            </h2>
          </div>
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
        <div className="flex-1 overflow-y-auto px-6 py-5 space-y-4">
          <p className="text-sm text-text-secondary">
            Request to place <strong>{dealName}</strong> at a later stage based
            on prior progress.
          </p>

          {/* Target Stage */}
          <div>
            <label className="block text-[11px] font-medium uppercase tracking-wider text-text-muted mb-1">
              Target Stage <span className="text-danger">*</span>
            </label>
            <select
              value={targetStage}
              onChange={(e) => setTargetStage(e.target.value)}
              autoFocus
              className="w-full rounded-lg border border-border px-3 py-2 text-sm text-text-primary bg-white focus:outline-none focus:ring-2 focus:ring-primary/40 focus:border-primary"
            >
              <option value="">Select target stage...</option>
              {TARGET_STAGES.map((s) => (
                <option key={s} value={s}>
                  {STAGE_NAMES[s]}
                </option>
              ))}
            </select>
          </div>

          {/* Original Start Date */}
          <div>
            <label className="block text-[11px] font-medium uppercase tracking-wider text-text-muted mb-1">
              Original Deal Start Date <span className="text-danger">*</span>
            </label>
            <input
              type="date"
              value={originalStartDate}
              onChange={(e) => setOriginalStartDate(e.target.value)}
              max="2026-03-31"
              className={cn(
                "w-full rounded-lg border px-3 py-2 text-sm text-text-primary focus:outline-none focus:ring-2 focus:ring-primary/40",
                dateError ? "border-danger" : "border-border"
              )}
            />
            {dateError && (
              <p className="text-xs text-danger mt-1">{dateError}</p>
            )}
          </div>

          {/* Context */}
          <div>
            <label className="block text-[11px] font-medium uppercase tracking-wider text-text-muted mb-1">
              Context <span className="text-danger">*</span>
            </label>
            <textarea
              value={context}
              onChange={(e) => setContext(e.target.value)}
              rows={4}
              className="w-full rounded-lg border border-border px-3 py-2 text-sm text-text-primary focus:outline-none focus:ring-2 focus:ring-primary/40 focus:border-primary resize-none"
              placeholder="Explain why this deal should be placed at the target stage..."
            />
          </div>

          {/* Error */}
          {mutation.isError && (
            <div className="rounded-lg bg-red-50 border border-red-200 px-4 py-3 text-sm text-danger">
              Failed to submit backfill request. Please try again.
            </div>
          )}
        </div>

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
            type="button"
            disabled={!isValid || mutation.isPending}
            onClick={() => mutation.mutate()}
            className={cn(
              "px-4 py-2 text-sm font-medium rounded-lg shadow-sm transition-colors",
              "bg-primary text-white hover:bg-primary-hover",
              "disabled:opacity-50 disabled:cursor-not-allowed"
            )}
          >
            {mutation.isPending ? (
              <span className="inline-flex items-center gap-2">
                <Loader2 className="h-4 w-4 animate-spin" />
                Submitting...
              </span>
            ) : (
              "Request Manager Approval"
            )}
          </button>
        </div>
      </div>
    </dialog>
  );
}
