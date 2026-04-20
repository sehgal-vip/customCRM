import { useEffect, useRef, useState } from "react";
import { useMutation, useQueryClient } from "@tanstack/react-query";
import { X, Loader2, ArrowRight, ArrowLeft, AlertTriangle } from "lucide-react";
import { toast } from "sonner";
import { stageApi } from "../../services/apiEndpoints";
import { STAGE_NAMES } from "../../lib/constants";
import { cn, getErrorMessage } from "../../lib/utils";
import { useAuth } from "../../context/AuthContext";
import type { Deal, CriteriaResult } from "../../types";

const STAGE_ORDER = [
  "STAGE_1", "STAGE_2", "STAGE_3", "STAGE_4",
  "STAGE_5", "STAGE_6", "STAGE_7", "STAGE_8",
] as const;

interface ChangeStageModalProps {
  open: boolean;
  onClose: () => void;
  deal: Deal;
}

export default function ChangeStageModal({
  open,
  onClose,
  deal,
}: ChangeStageModalProps) {
  const dialogRef = useRef<HTMLDialogElement>(null);
  const queryClient = useQueryClient();
  const { isManager } = useAuth();

  const [backReason, setBackReason] = useState("");
  const [overrideReason, setOverrideReason] = useState("");
  const [criteriaWarnings, setCriteriaWarnings] = useState<CriteriaResult[]>([]);
  const [hardBlock, setHardBlock] = useState<string | null>(null);
  const [showOverride, setShowOverride] = useState(false);

  const currentIdx = STAGE_ORDER.indexOf(deal.currentStage as typeof STAGE_ORDER[number]);
  const canGoForward = currentIdx < STAGE_ORDER.length - 1;
  const canGoBack = currentIdx > 0;
  const nextStage = canGoForward ? STAGE_ORDER[currentIdx + 1] : null;
  const prevStage = canGoBack ? STAGE_ORDER[currentIdx - 1] : null;

  // Stage 4 (Closure of Commercials) hard block: cannot advance unless sub_status is NEGOTIATING
  const stage5Block =
    deal.currentStage === "STAGE_4" && deal.subStatus !== "NEGOTIATING";

  const forwardMutation = useMutation({
    mutationFn: (data?: { overrideReason?: string }) =>
      stageApi.moveForward(deal.id, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["deal", deal.id] });
      queryClient.invalidateQueries({ queryKey: ["deals"] });
      toast.success("Stage advanced");
      onClose();
    },
    onError: (error: unknown) => {
      const err = error as { response?: { status?: number; data?: { criteria?: CriteriaResult[]; message?: string } } };
      if (err.response?.status === 422) {
        const data = err.response.data;
        if (data?.criteria) {
          const soft = data.criteria.filter((c: CriteriaResult) => c.softBlock && !c.met);
          const hard = data.criteria.filter((c: CriteriaResult) => !c.softBlock && !c.met);
          if (hard.length > 0) {
            setHardBlock(hard.map((c: CriteriaResult) => c.message).join(". "));
          } else if (soft.length > 0) {
            setCriteriaWarnings(soft);
            setShowOverride(true);
          }
        } else if (data?.message) {
          setHardBlock(data.message);
        }
      }
    },
  });

  const backwardMutation = useMutation({
    mutationFn: (reason: string) => stageApi.moveBackward(deal.id, { reason: reason || "Manager direct move" }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["deal", deal.id] });
      queryClient.invalidateQueries({ queryKey: ["deals"] });
      toast.success("Stage moved back");
      onClose();
    },
    onError: (err: unknown) => {
      toast.error(getErrorMessage(err, "Failed to move stage backward"));
    },
  });

  // Render-time reset when modal closes
  const [wasOpen, setWasOpen] = useState(false);
  if (open && !wasOpen) {
    setWasOpen(true);
  }
  if (!open && wasOpen) {
    setWasOpen(false);
    setBackReason("");
    setOverrideReason("");
    setCriteriaWarnings([]);
    setHardBlock(null);
    setShowOverride(false);
  }

  useEffect(() => {
    const dlg = dialogRef.current;
    if (!dlg) return;
    if (open && !dlg.open) dlg.showModal();
    else if (!open && dlg.open) dlg.close();
  }, [open]);

  const handleAdvance = () => {
    if (showOverride) {
      forwardMutation.mutate({ overrideReason });
    } else {
      forwardMutation.mutate(undefined);
    }
  };

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
          <div>
            <h2 className="text-lg font-semibold text-text-primary">
              Change Stage
            </h2>
            <p className="text-xs text-text-muted mt-0.5">
              Currently: {STAGE_NAMES[deal.currentStage]}
            </p>
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
        <div className="flex-1 overflow-y-auto px-6 py-5 space-y-6">
          {/* Move Forward */}
          {canGoForward && nextStage && (
            <div className="space-y-3">
              <h3 className="text-sm font-semibold text-text-primary flex items-center gap-2">
                <ArrowRight className="h-4 w-4 text-primary" />
                Move Forward
              </h3>
              <p className="text-sm text-text-secondary">
                Advance to{" "}
                <span className="font-medium">
                  Stage {currentIdx + 2}: {STAGE_NAMES[nextStage]}
                </span>
              </p>

              {/* Stage 5 hard block */}
              {stage5Block && (
                <div className="rounded-lg bg-red-50 border border-red-200 px-4 py-3 flex items-start gap-2">
                  <AlertTriangle className="h-4 w-4 text-danger shrink-0 mt-0.5" />
                  <p className="text-sm text-danger">
                    Cannot advance. Pricing must be approved first (sub-status
                    must be Negotiating).
                  </p>
                </div>
              )}

              {/* Hard block from API */}
              {hardBlock && (
                <div className="rounded-lg bg-red-50 border border-red-200 px-4 py-3 flex items-start gap-2">
                  <AlertTriangle className="h-4 w-4 text-danger shrink-0 mt-0.5" />
                  <p className="text-sm text-danger">{hardBlock}</p>
                </div>
              )}

              {/* Soft block warnings */}
              {criteriaWarnings.length > 0 && (
                <div className="rounded-lg bg-amber-50 border border-amber-200 px-4 py-3 space-y-2">
                  <p className="text-sm font-medium text-amber-800">
                    Exit criteria not fully met:
                  </p>
                  <ul className="text-sm text-amber-700 space-y-1">
                    {criteriaWarnings.map((c) => (
                      <li key={c.rule} className="flex items-start gap-1.5">
                        <span className="text-amber-500 shrink-0">--</span>
                        {c.message}
                      </li>
                    ))}
                  </ul>
                </div>
              )}

              {/* Override reason */}
              {showOverride && (
                <div>
                  <label className="block text-[11px] font-medium uppercase tracking-wider text-text-muted mb-1">
                    Override Reason
                  </label>
                  <textarea
                    value={overrideReason}
                    onChange={(e) => setOverrideReason(e.target.value)}
                    rows={2}
                    placeholder="Explain why you are overriding the criteria..."
                    className="w-full rounded-lg border border-border px-3 py-2 text-sm text-text-primary focus:outline-none focus:ring-2 focus:ring-primary/40 focus:border-primary resize-none"
                  />
                </div>
              )}

              <button
                type="button"
                disabled={
                  stage5Block ||
                  !!hardBlock ||
                  (showOverride && !overrideReason.trim()) ||
                  forwardMutation.isPending
                }
                onClick={handleAdvance}
                className={cn(
                  "w-full px-4 py-2.5 text-sm font-medium rounded-lg text-white shadow-sm transition-colors",
                  "bg-primary hover:bg-primary-hover",
                  "disabled:opacity-50 disabled:cursor-not-allowed"
                )}
              >
                {forwardMutation.isPending ? (
                  <span className="inline-flex items-center justify-center gap-2">
                    <Loader2 className="h-4 w-4 animate-spin" />
                    Advancing...
                  </span>
                ) : showOverride ? (
                  `Advance with Override`
                ) : (
                  `Advance to Stage ${currentIdx + 2}`
                )}
              </button>
            </div>
          )}

          {/* Divider */}
          {canGoForward && canGoBack && (
            <div className="border-t border-border" />
          )}

          {/* Move Backward */}
          {canGoBack && prevStage && (
            <div className="space-y-3">
              <h3 className="text-sm font-semibold text-text-primary flex items-center gap-2">
                <ArrowLeft className="h-4 w-4 text-text-muted" />
                Move Backward
              </h3>
              <p className="text-sm text-text-secondary">
                Return to{" "}
                <span className="font-medium">
                  Stage {currentIdx}: {STAGE_NAMES[prevStage]}
                </span>
              </p>

              <div>
                <label className="block text-[11px] font-medium uppercase tracking-wider text-text-muted mb-1">
                  Reason {!isManager && <span className="text-danger">*</span>}
                </label>
                <textarea
                  value={backReason}
                  onChange={(e) => setBackReason(e.target.value)}
                  rows={2}
                  placeholder={isManager ? "Reason for moving back (optional)..." : "Explain why this deal needs to move back..."}
                  className="w-full rounded-lg border border-border px-3 py-2 text-sm text-text-primary focus:outline-none focus:ring-2 focus:ring-primary/40 focus:border-primary resize-none"
                />
              </div>

              <button
                type="button"
                disabled={
                  (!isManager && !backReason.trim()) ||
                  backwardMutation.isPending
                }
                onClick={() => backwardMutation.mutate(backReason)}
                className={cn(
                  "w-full px-4 py-2.5 text-sm font-medium rounded-lg shadow-sm transition-colors",
                  "bg-white border border-border text-text-secondary hover:bg-slate-50",
                  "disabled:opacity-50 disabled:cursor-not-allowed"
                )}
              >
                {backwardMutation.isPending ? (
                  <span className="inline-flex items-center justify-center gap-2">
                    <Loader2 className="h-4 w-4 animate-spin" />
                    Processing...
                  </span>
                ) : isManager ? (
                  "Move Back"
                ) : (
                  "Request Regression"
                )}
              </button>

              {backwardMutation.isError && (
                <div className="rounded-lg bg-red-50 border border-red-200 px-4 py-3 text-sm text-danger">
                  Failed to process request. Please try again.
                </div>
              )}
            </div>
          )}

          {/* Forward mutation error (non-422) */}
          {forwardMutation.isError &&
            !hardBlock &&
            criteriaWarnings.length === 0 && (
              <div className="rounded-lg bg-red-50 border border-red-200 px-4 py-3 text-sm text-danger">
                Failed to advance stage. Please try again.
              </div>
            )}
        </div>

        {/* Footer */}
        <div className="flex items-center justify-end px-6 py-4 border-t border-border">
          <button
            type="button"
            onClick={onClose}
            className="px-4 py-2 text-sm rounded-lg text-text-secondary hover:bg-slate-100 transition-colors"
          >
            Close
          </button>
        </div>
      </div>
    </dialog>
  );
}
