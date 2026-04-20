import { useEffect, useRef } from "react";
import { useQuery } from "@tanstack/react-query";
import { X, Loader2 } from "lucide-react";
import { pricingApi } from "../../services/apiEndpoints";
import { cn } from "../../lib/utils";
import { formatINR, formatDate } from "../../lib/formatters";
import type { PricingSubmission } from "../../types";

interface PricingHistoryPanelProps {
  open: boolean;
  onClose: () => void;
  dealId: number;
  fleetSize: number;
}

const STATUS_STYLES: Record<string, { bg: string; text: string; label: string }> = {
  SUBMITTED: { bg: "bg-blue-50", text: "text-blue-700", label: "Submitted" },
  APPROVED: { bg: "bg-emerald-50", text: "text-emerald-700", label: "Approved" },
  REJECTED: { bg: "bg-red-50", text: "text-red-700", label: "Rejected" },
  SUPERSEDED: { bg: "bg-slate-100", text: "text-slate-500", label: "Superseded" },
};

export default function PricingHistoryPanel({
  open,
  onClose,
  dealId,
  fleetSize,
}: PricingHistoryPanelProps) {
  const dialogRef = useRef<HTMLDialogElement>(null);

  const { data: history, isLoading } = useQuery({
    queryKey: ["pricing-history", dealId],
    queryFn: () => pricingApi.history(dealId),
    enabled: open,
  });

  useEffect(() => {
    const dlg = dialogRef.current;
    if (!dlg) return;
    if (open && !dlg.open) dlg.showModal();
    else if (!open && dlg.open) dlg.close();
  }, [open]);

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
            Pricing History
          </h2>
          <button
            type="button"
            onClick={onClose}
            className="p-1 rounded-lg hover:bg-slate-100 text-text-muted transition-colors"
          >
            <X className="h-5 w-5" />
          </button>
        </div>

        {/* Body */}
        <div className="flex-1 overflow-y-auto px-6 py-5 space-y-4">
          {isLoading && (
            <div className="flex items-center justify-center py-12">
              <Loader2 className="h-6 w-6 animate-spin text-primary" />
            </div>
          )}

          {!isLoading && (!history || history.length === 0) && (
            <div className="py-12 text-center text-sm text-text-muted">
              No pricing submissions yet.
            </div>
          )}

          {history?.map((entry) => (
            <PricingEntryCard
              key={entry.id}
              entry={entry}
              fleetSize={fleetSize}
            />
          ))}
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

function PricingEntryCard({
  entry,
  fleetSize,
}: {
  entry: PricingSubmission;
  fleetSize: number;
}) {
  const style = STATUS_STYLES[entry.status] ?? STATUS_STYLES.SUBMITTED;
  const managerEdited =
    entry.status === "APPROVED" &&
    (entry.managerMonthlyKm !== undefined || entry.managerPricePerKm !== undefined);

  const effectiveKm = managerEdited && entry.managerMonthlyKm != null
    ? entry.managerMonthlyKm
    : entry.monthlyKmCommitment;
  const effectivePrice = managerEdited && entry.managerPricePerKm != null
    ? entry.managerPricePerKm
    : entry.pricePerKm;
  const monthlyPerVehicle = effectiveKm * effectivePrice;
  const monthlyDealValue = monthlyPerVehicle * fleetSize;

  return (
    <div
      className={cn(
        "rounded-xl border border-border p-4 shadow-[0_1px_2px_rgba(0,0,0,0.05)]",
        entry.status === "SUPERSEDED" && "opacity-60"
      )}
    >
      {/* Header */}
      <div className="flex items-start justify-between gap-2 mb-3">
        <div>
          <p className="text-xs text-text-muted">
            {formatDate(entry.createdAt)} — {entry.submittedBy.name}
          </p>
          {entry.reviewedBy && (
            <p className="text-xs text-text-muted mt-0.5">
              Reviewed by {entry.reviewedBy.name}
              {entry.reviewedAt && <> on {formatDate(entry.reviewedAt)}</>}
            </p>
          )}
        </div>
        <span
          className={cn(
            "inline-flex items-center px-2 py-0.5 rounded-full text-[10px] font-medium",
            style.bg,
            style.text
          )}
        >
          {style.label}
        </span>
      </div>

      {/* Services chips */}
      <div className="flex flex-wrap gap-1 mb-3">
        {(managerEdited && entry.managerServicesSelected
          ? entry.managerServicesSelected
          : entry.servicesSelected
        ).map((svc) => (
          <span
            key={svc}
            className="px-2 py-0.5 rounded-full text-[10px] font-medium bg-teal-50 text-teal-700"
          >
            {svc}
          </span>
        ))}
      </div>

      {/* Pricing grid */}
      <div className="grid grid-cols-2 gap-x-4 gap-y-2 text-xs">
        <div>
          <span className="text-text-muted">Monthly KM</span>
          <div>
            {managerEdited && entry.managerMonthlyKm != null && entry.managerMonthlyKm !== entry.monthlyKmCommitment ? (
              <>
                <span className="line-through text-text-muted mr-1">
                  {entry.monthlyKmCommitment.toLocaleString("en-IN")}
                </span>
                <span className="font-bold text-text-primary">
                  {entry.managerMonthlyKm.toLocaleString("en-IN")}
                </span>
              </>
            ) : (
              <span className="font-medium text-text-primary">
                {entry.monthlyKmCommitment.toLocaleString("en-IN")}
              </span>
            )}
          </div>
        </div>
        <div>
          <span className="text-text-muted">Price/KM</span>
          <div>
            {managerEdited && entry.managerPricePerKm != null && entry.managerPricePerKm !== entry.pricePerKm ? (
              <>
                <span className="line-through text-text-muted mr-1">
                  {formatINR(entry.pricePerKm)}
                </span>
                <span className="font-bold text-text-primary">
                  {formatINR(entry.managerPricePerKm)}
                </span>
              </>
            ) : (
              <span className="font-medium text-text-primary">
                {formatINR(entry.pricePerKm)}
              </span>
            )}
          </div>
        </div>
        <div>
          <span className="text-text-muted">Value/Vehicle</span>
          <span className="font-medium text-text-primary block">
            {formatINR(monthlyPerVehicle)}
          </span>
        </div>
        <div>
          <span className="text-text-muted">Deal Value</span>
          <span className="font-medium text-text-primary block">
            {formatINR(monthlyDealValue)}
          </span>
        </div>
        {entry.tokenAmount != null && (
          <div className="col-span-2">
            <span className="text-text-muted">Token Amount</span>
            <div>
              {managerEdited && entry.managerTokenAmount != null && entry.managerTokenAmount !== entry.tokenAmount ? (
                <>
                  <span className="line-through text-text-muted mr-1">
                    {formatINR(entry.tokenAmount)}
                  </span>
                  <span className="font-bold text-text-primary">
                    {formatINR(entry.managerTokenAmount)}
                  </span>
                </>
              ) : (
                <span className="font-medium text-text-primary">
                  {formatINR(entry.tokenAmount)}
                </span>
              )}
            </div>
          </div>
        )}
      </div>

      {/* Rejection note */}
      {entry.status === "REJECTED" && entry.rejectionNote && (
        <div className="mt-3 p-2 rounded-lg bg-red-50 border border-red-200">
          <p className="text-xs text-danger">
            <span className="font-medium">Rejection note:</span>{" "}
            {entry.rejectionNote}
          </p>
        </div>
      )}
    </div>
  );
}
