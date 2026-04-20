import { useEffect, useRef, useState } from "react";
import { useMutation, useQueryClient } from "@tanstack/react-query";
import { X, AlertTriangle, Loader2 } from "lucide-react";
import { toast } from "sonner";
import { dealsApi } from "../../services/apiEndpoints";
import { LOST_REASONS } from "../../lib/constants";
import { cn, getErrorMessage } from "../../lib/utils";

interface ArchiveDealModalProps {
  open: boolean;
  onClose: () => void;
  dealId: number;
  dealName: string;
}

export default function ArchiveDealModal({
  open,
  onClose,
  dealId,
  dealName,
}: ArchiveDealModalProps) {
  const dialogRef = useRef<HTMLDialogElement>(null);
  const queryClient = useQueryClient();

  const [reason, setReason] = useState("");
  const [notes, setNotes] = useState("");

  const mutation = useMutation({
    mutationFn: () =>
      dealsApi.archive(dealId, { reason, notes: notes || undefined }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["deals"] });
      toast.success("Deal archived");
      onClose();
    },
    onError: (err: unknown) => {
      toast.error(getErrorMessage(err, "Failed to archive deal"));
    },
  });

  // Render-time reset when modal closes
  const [wasOpen, setWasOpen] = useState(false);
  if (open && !wasOpen) {
    setWasOpen(true);
  }
  if (!open && wasOpen) {
    setWasOpen(false);
    setReason("");
    setNotes("");
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

  if (!open) return null;

  return (
    <dialog
      ref={dialogRef}
      onCancel={onClose}
      className="backdrop:bg-black/50 backdrop:backdrop-blur-sm bg-transparent p-0 m-auto rounded-2xl max-w-md w-full"
    >
      <div className="bg-white rounded-2xl shadow-[0_25px_50px_rgba(0,0,0,0.15)] max-h-[90vh] flex flex-col">
        {/* Header */}
        <div className="flex items-center justify-between px-6 py-4 border-b border-border">
          <div className="flex items-center gap-2">
            <AlertTriangle className="h-5 w-5 text-danger" />
            <h2 className="text-lg font-semibold text-text-primary">
              Archive Deal
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
          {/* Warning banner */}
          <div className="rounded-lg bg-red-50 border border-red-200 px-4 py-3">
            <p className="text-sm text-danger font-medium">
              This will archive{" "}
              <span className="font-semibold">{dealName}</span> and remove it
              from the active pipeline.
            </p>
            <p className="text-xs text-red-500 mt-1">
              Deal history and reports will be preserved. It can be reactivated
              later.
            </p>
          </div>

          {/* Lost Reason */}
          <div>
            <label className="block text-[11px] font-medium uppercase tracking-wider text-text-muted mb-1">
              Lost Reason <span className="text-danger">*</span>
            </label>
            <select
              value={reason}
              onChange={(e) => setReason(e.target.value)}
              autoFocus
              className={cn(
                "w-full rounded-lg border px-3 py-2 text-sm text-text-primary bg-white",
                "focus:outline-none focus:ring-2 focus:ring-primary/40 focus:border-primary",
                !reason ? "border-border" : "border-border"
              )}
            >
              <option value="">Select a reason...</option>
              {LOST_REASONS.map((r) => (
                <option key={r} value={r}>
                  {r}
                </option>
              ))}
            </select>
          </div>

          {/* Notes */}
          <div>
            <label className="block text-[11px] font-medium uppercase tracking-wider text-text-muted mb-1">
              Notes (optional)
            </label>
            <textarea
              value={notes}
              onChange={(e) => setNotes(e.target.value)}
              rows={3}
              className="w-full rounded-lg border border-border px-3 py-2 text-sm text-text-primary focus:outline-none focus:ring-2 focus:ring-primary/40 focus:border-primary resize-none"
              placeholder="Additional context..."
            />
          </div>

          {/* Error */}
          {mutation.isError && (
            <div className="rounded-lg bg-red-50 border border-red-200 px-4 py-3 text-sm text-danger">
              Failed to archive deal. Please try again.
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
            disabled={!reason || mutation.isPending}
            onClick={() => mutation.mutate()}
            className={cn(
              "px-4 py-2 text-sm font-medium rounded-lg shadow-sm transition-colors",
              "bg-red-50 border border-red-200 text-danger",
              "hover:bg-red-100",
              "disabled:opacity-50 disabled:cursor-not-allowed"
            )}
          >
            {mutation.isPending ? (
              <span className="inline-flex items-center gap-2">
                <Loader2 className="h-4 w-4 animate-spin" />
                Archiving...
              </span>
            ) : (
              "Archive Deal"
            )}
          </button>
        </div>
      </div>
    </dialog>
  );
}
