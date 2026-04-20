import { useEffect, useRef, useState, useMemo } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { X, Loader2, CheckCircle, XCircle } from "lucide-react";
import { toast } from "sonner";
import { pricingApi } from "../../services/apiEndpoints";
import { cn, getErrorMessage } from "../../lib/utils";
import { formatINR } from "../../lib/formatters";
import type { Deal, PricingSubmission } from "../../types";

interface ApprovePricingModalProps {
  open: boolean;
  onClose: () => void;
  deal: Deal;
}

const SERVICE_OPTIONS = ["Bus Lease", "Charging", "Maintenance"] as const;

export default function ApprovePricingModal({
  open,
  onClose,
  deal,
}: ApprovePricingModalProps) {
  const dialogRef = useRef<HTMLDialogElement>(null);
  const queryClient = useQueryClient();

  const [services, setServices] = useState<string[]>(["Bus Lease"]);
  const [monthlyKm, setMonthlyKm] = useState<number>(0);
  const [pricePerKm, setPricePerKm] = useState<number>(0);
  const [tokenAmount, setTokenAmount] = useState<number | undefined>(undefined);
  const [showReject, setShowReject] = useState(false);
  const [rejectionNote, setRejectionNote] = useState("");

  // Fetch pricing history to get latest submission
  const { data: pricingHistory } = useQuery({
    queryKey: ["pricing-history", deal.id],
    queryFn: () => pricingApi.history(deal.id),
    enabled: open,
  });

  const latestSubmission: PricingSubmission | undefined = useMemo(() => {
    if (!pricingHistory) return undefined;
    return pricingHistory.find((p) => p.status === "SUBMITTED");
  }, [pricingHistory]);

  // Pre-fill from latest submission (render-time sync instead of useEffect)
  const [syncedSubmissionId, setSyncedSubmissionId] = useState<number | null>(null);
  if (latestSubmission && latestSubmission.id !== syncedSubmissionId) {
    setSyncedSubmissionId(latestSubmission.id);
    setServices(latestSubmission.servicesSelected);
    setMonthlyKm(latestSubmission.monthlyKmCommitment);
    setPricePerKm(latestSubmission.pricePerKm);
    setTokenAmount(latestSubmission.tokenAmount ?? undefined);
  }

  const monthlyValuePerVehicle = monthlyKm * pricePerKm;
  const monthlyDealValue = monthlyValuePerVehicle * (deal.fleetSize ?? 1);

  const kmChanged = latestSubmission && monthlyKm !== latestSubmission.monthlyKmCommitment;
  const priceChanged = latestSubmission && pricePerKm !== latestSubmission.pricePerKm;
  const tokenChanged = latestSubmission && tokenAmount !== (latestSubmission.tokenAmount ?? undefined);

  const approveMutation = useMutation({
    mutationFn: () =>
      pricingApi.approve(deal.id, latestSubmission!.id, {
        servicesSelected: services,
        monthlyKmCommitment: monthlyKm,
        pricePerKm,
        ...(tokenAmount !== undefined ? { tokenAmount } : {}),
      }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["deal", deal.id] });
      queryClient.invalidateQueries({ queryKey: ["pricing-history", deal.id] });
      queryClient.invalidateQueries({ queryKey: ["deals"] });
      toast.success("Pricing approved");
      onClose();
    },
    onError: (err: unknown) => {
      toast.error(getErrorMessage(err, "Failed to approve pricing"));
    },
  });

  const rejectMutation = useMutation({
    mutationFn: () =>
      pricingApi.reject(deal.id, latestSubmission!.id, {
        rejectionNote,
      }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["deal", deal.id] });
      queryClient.invalidateQueries({ queryKey: ["pricing-history", deal.id] });
      queryClient.invalidateQueries({ queryKey: ["deals"] });
      toast.success("Pricing rejected");
      onClose();
    },
    onError: (err: unknown) => {
      toast.error(getErrorMessage(err, "Failed to reject pricing"));
    },
  });

  // Render-time reset when modal closes
  const [wasOpen, setWasOpen] = useState(false);
  if (open && !wasOpen) {
    setWasOpen(true);
  }
  if (!open && wasOpen) {
    setWasOpen(false);
    setShowReject(false);
    setRejectionNote("");
  }

  useEffect(() => {
    const dlg = dialogRef.current;
    if (!dlg) return;
    if (open && !dlg.open) dlg.showModal();
    else if (!open && dlg.open) dlg.close();
  }, [open]);

  const toggleService = (svc: string) => {
    if (svc === "Bus Lease") return;
    setServices((prev) =>
      prev.includes(svc) ? prev.filter((s) => s !== svc) : [...prev, svc]
    );
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
          <div>
            <h2 className="text-lg font-semibold text-text-primary">
              Review Pricing
            </h2>
            <p className="text-xs text-text-muted mt-0.5">
              {deal.name}
              {latestSubmission && (
                <> — submitted by {latestSubmission.submittedBy.name}</>
              )}
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
        <div className="flex-1 overflow-y-auto px-6 py-5 space-y-5">
          {!latestSubmission ? (
            <div className="py-8 text-center text-sm text-text-muted">
              No pending pricing submission found.
            </div>
          ) : (
            <>
              {/* Services */}
              <div>
                <label className="block text-[11px] font-medium uppercase tracking-wider text-text-muted mb-2">
                  Services Selected
                </label>
                <div className="flex flex-wrap gap-2">
                  {SERVICE_OPTIONS.map((svc) => (
                    <button
                      key={svc}
                      type="button"
                      onClick={() => toggleService(svc)}
                      disabled={svc === "Bus Lease"}
                      className={cn(
                        "px-3 py-1.5 text-sm rounded-lg border transition-colors",
                        services.includes(svc)
                          ? "bg-teal-50 border-teal-300 text-teal-700"
                          : "bg-white border-border text-text-secondary hover:bg-slate-50",
                        svc === "Bus Lease" && "opacity-70 cursor-not-allowed"
                      )}
                    >
                      {svc}
                    </button>
                  ))}
                </div>
              </div>

              {/* Monthly KM Commitment */}
              <div>
                <label className="block text-[11px] font-medium uppercase tracking-wider text-text-muted mb-1">
                  Monthly KM Commitment
                </label>
                <input
                  type="number"
                  value={monthlyKm}
                  onChange={(e) => setMonthlyKm(Number(e.target.value))}
                  className="w-full rounded-lg border border-border px-3 py-2 text-sm text-text-primary focus:outline-none focus:ring-2 focus:ring-primary/40 focus:border-primary"
                />
                {kmChanged && (
                  <p className="text-xs text-text-muted mt-1">
                    Agent submitted: {latestSubmission.monthlyKmCommitment.toLocaleString("en-IN")} km
                  </p>
                )}
              </div>

              {/* Price per KM */}
              <div>
                <label className="block text-[11px] font-medium uppercase tracking-wider text-text-muted mb-1">
                  Price per KM (INR)
                </label>
                <input
                  type="number"
                  value={pricePerKm}
                  onChange={(e) => setPricePerKm(Number(e.target.value))}
                  step="0.01"
                  className="w-full rounded-lg border border-border px-3 py-2 text-sm text-text-primary focus:outline-none focus:ring-2 focus:ring-primary/40 focus:border-primary"
                />
                {priceChanged && (
                  <p className="text-xs text-text-muted mt-1">
                    Agent submitted: {formatINR(latestSubmission.pricePerKm)}/km
                  </p>
                )}
              </div>

              {/* Token Amount */}
              {latestSubmission.tokenAmount != null && (
                <div>
                  <label className="block text-[11px] font-medium uppercase tracking-wider text-text-muted mb-1">
                    Token Amount (INR)
                  </label>
                  <input
                    type="number"
                    value={tokenAmount ?? ""}
                    onChange={(e) => setTokenAmount(e.target.value ? Number(e.target.value) : undefined)}
                    step="0.01"
                    className="w-full rounded-lg border border-border px-3 py-2 text-sm text-text-primary focus:outline-none focus:ring-2 focus:ring-primary/40 focus:border-primary"
                  />
                  {tokenChanged && (
                    <p className="text-xs text-text-muted mt-1">
                      Agent submitted: {formatINR(latestSubmission.tokenAmount)}
                    </p>
                  )}
                </div>
              )}

              {/* Auto-calculated */}
              <div className="grid grid-cols-2 gap-4">
                <div className="bg-slate-50 rounded-lg p-3">
                  <p className="text-[10px] font-medium uppercase tracking-wider text-text-muted mb-1">
                    Monthly Billing / Vehicle
                  </p>
                  <p className="text-sm font-semibold text-text-primary">
                    {formatINR(monthlyValuePerVehicle)}
                  </p>
                </div>
                <div className="bg-slate-50 rounded-lg p-3">
                  <p className="text-[10px] font-medium uppercase tracking-wider text-text-muted mb-1">
                    Monthly Billing (Deal)
                  </p>
                  <p className="text-sm font-semibold text-text-primary">
                    {formatINR(monthlyDealValue)}
                  </p>
                </div>
              </div>

              {/* Reject section */}
              {showReject && (
                <div className="space-y-3 p-4 bg-red-50 rounded-lg border border-red-200">
                  <label className="block text-[11px] font-medium uppercase tracking-wider text-danger mb-1">
                    Rejection Note <span className="text-danger">*</span>
                  </label>
                  <textarea
                    value={rejectionNote}
                    onChange={(e) => setRejectionNote(e.target.value)}
                    rows={3}
                    placeholder="Explain why the pricing is being rejected..."
                    className="w-full rounded-lg border border-red-200 px-3 py-2 text-sm text-text-primary focus:outline-none focus:ring-2 focus:ring-red-300 focus:border-red-300 resize-none bg-white"
                  />
                  <button
                    type="button"
                    disabled={!rejectionNote.trim() || rejectMutation.isPending}
                    onClick={() => rejectMutation.mutate()}
                    className={cn(
                      "w-full px-4 py-2 text-sm font-medium rounded-lg shadow-sm transition-colors",
                      "bg-red-600 text-white hover:bg-red-700",
                      "disabled:opacity-50 disabled:cursor-not-allowed"
                    )}
                  >
                    {rejectMutation.isPending ? (
                      <span className="inline-flex items-center justify-center gap-2">
                        <Loader2 className="h-4 w-4 animate-spin" />
                        Rejecting...
                      </span>
                    ) : (
                      "Confirm Rejection"
                    )}
                  </button>
                </div>
              )}

              {/* Error messages */}
              {(approveMutation.isError || rejectMutation.isError) && (
                <div className="rounded-lg bg-red-50 border border-red-200 px-4 py-3 text-sm text-danger">
                  Failed to process. Please try again.
                </div>
              )}
            </>
          )}
        </div>

        {/* Footer */}
        {latestSubmission && (
          <div className="flex items-center justify-between gap-3 px-6 py-4 border-t border-border">
            <button
              type="button"
              onClick={() => setShowReject(!showReject)}
              className={cn(
                "inline-flex items-center gap-1.5 px-4 py-2 text-sm font-medium rounded-lg transition-colors",
                showReject
                  ? "text-text-secondary hover:bg-slate-100"
                  : "bg-red-50 border border-red-200 text-danger hover:bg-red-100"
              )}
            >
              <XCircle className="h-4 w-4" />
              {showReject ? "Cancel Rejection" : "Reject"}
            </button>
            {!showReject && (
              <button
                type="button"
                disabled={approveMutation.isPending}
                onClick={() => approveMutation.mutate()}
                className={cn(
                  "inline-flex items-center gap-1.5 px-4 py-2 text-sm font-medium rounded-lg shadow-sm transition-colors",
                  "bg-emerald-600 text-white hover:bg-emerald-700",
                  "disabled:opacity-50 disabled:cursor-not-allowed"
                )}
              >
                {approveMutation.isPending ? (
                  <span className="inline-flex items-center gap-2">
                    <Loader2 className="h-4 w-4 animate-spin" />
                    Approving...
                  </span>
                ) : (
                  <>
                    <CheckCircle className="h-4 w-4" />
                    Approve
                  </>
                )}
              </button>
            )}
          </div>
        )}
      </div>
    </dialog>
  );
}
