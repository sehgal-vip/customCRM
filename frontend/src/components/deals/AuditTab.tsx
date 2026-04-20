import { useState } from "react";
import { useQuery } from "@tanstack/react-query";
import { Loader2, ChevronDown } from "lucide-react";
import { auditApi } from "../../services/apiEndpoints";
import { timeAgo } from "../../lib/formatters";
import { cn } from "../../lib/utils";
import type { AuditEntry } from "../../types";

const ACTION_COLORS: Record<string, string> = {
  CREATED: "bg-emerald-500",
  STAGE_CHANGED: "bg-primary",
  UPDATED: "bg-blue-500",
  ARCHIVED: "bg-red-500",
  REACTIVATED: "bg-amber-500",
  PRICING_SUBMITTED: "bg-blue-500",
  PRICING_APPROVED: "bg-emerald-500",
  PRICING_REJECTED: "bg-red-500",
  REPORT_LOGGED: "bg-teal-500",
  REPORT_VOIDED: "bg-red-400",
  DOCUMENT_UPDATED: "bg-blue-400",
  BACKFILL_REQUESTED: "bg-amber-500",
  ASSIGNED: "bg-blue-500",
};

function formatAction(entry: AuditEntry): string {
  const details = entry.details ?? {};
  switch (entry.action) {
    case "CREATED":
      return "Deal Created";
    case "STAGE_CHANGED": {
      const from = details.fromStage as string | undefined;
      const to = details.toStage as string | undefined;
      if (from && to) {
        const fromNum = from.replace("STAGE_", "");
        const toNum = to.replace("STAGE_", "");
        return `Stage Changed (${fromNum} \u2192 ${toNum})`;
      }
      return "Stage Changed";
    }
    case "UPDATED":
      return "Deal Updated";
    case "ARCHIVED":
      return `Deal Archived${details.reason ? ` -- ${details.reason}` : ""}`;
    case "REACTIVATED":
      return "Deal Reactivated";
    case "PRICING_SUBMITTED":
      return "Pricing Submitted";
    case "PRICING_APPROVED":
      return "Pricing Approved";
    case "PRICING_REJECTED":
      return `Pricing Rejected${details.note ? ` -- ${details.note}` : ""}`;
    case "REPORT_LOGGED":
      return "Activity Report Logged";
    case "REPORT_VOIDED":
      return "Activity Report Voided";
    case "DOCUMENT_UPDATED":
      return `Document ${details.status ?? "Updated"}`;
    case "BACKFILL_REQUESTED":
      return "Backfill Placement Requested";
    case "ASSIGNED":
      return `Assigned to ${details.agentName ?? "agent"}`;
    default:
      return entry.action.replace(/_/g, " ");
  }
}

interface AuditTabProps {
  dealId: number;
}

export default function AuditTab({ dealId }: AuditTabProps) {
  const [page, setPage] = useState(0);
  const [allEntries, setAllEntries] = useState<AuditEntry[]>([]);

  const { data, isLoading } = useQuery({
    queryKey: ["audit", dealId, page],
    queryFn: async () => {
      const result = await auditApi.getDealAudit(dealId, { page, size: 20 });
      if (page === 0) {
        setAllEntries(result.content);
      } else {
        setAllEntries((prev) => {
          const ids = new Set(prev.map((e) => e.id));
          const newItems = result.content.filter((e: AuditEntry) => !ids.has(e.id));
          return [...prev, ...newItems];
        });
      }
      return result;
    },
  });

  const hasMore = data ? data.number < data.totalPages - 1 : false;

  if (isLoading && page === 0) {
    return (
      <div className="flex items-center justify-center py-12">
        <Loader2 className="h-6 w-6 animate-spin text-primary" />
      </div>
    );
  }

  if (allEntries.length === 0) {
    return (
      <div className="py-12 text-center text-sm text-text-muted">
        No audit entries found.
      </div>
    );
  }

  return (
    <div className="space-y-0">
      {/* Timeline */}
      <div className="relative pl-8">
        {/* Vertical line */}
        <div className="absolute left-3 top-2 bottom-2 w-px bg-border" />

        {allEntries.map((entry, idx) => {
          const dotColor =
            ACTION_COLORS[entry.action] ?? "bg-slate-400";
          const isLast = idx === allEntries.length - 1;

          return (
            <div
              key={entry.id}
              className={cn("relative pb-6", isLast && "pb-0")}
            >
              {/* Dot */}
              <div
                className={cn(
                  "absolute left-[-20px] top-1.5 w-2.5 h-2.5 rounded-full ring-2 ring-white",
                  dotColor
                )}
              />

              {/* Content */}
              <div>
                <div className="flex items-baseline gap-2 flex-wrap">
                  <span className="text-sm font-medium text-text-primary">
                    {formatAction(entry)}
                  </span>
                  {entry.actorName && (
                    <span className="text-xs text-text-secondary">
                      by {entry.actorName}
                    </span>
                  )}
                </div>
                <p className="text-[11px] text-text-muted mt-0.5">
                  {timeAgo(entry.createdAt)}
                </p>
                {Object.keys(entry.details).length > 0 && (
                  <div className="mt-1 text-xs text-text-muted">
                    {Object.entries(entry.details)
                      .filter(
                        ([k]) =>
                          !["fromStage", "toStage", "reason", "note", "agentName", "status"].includes(k)
                      )
                      .map(([k, v]) => (
                        <span key={k} className="mr-3">
                          {k}: {String(v)}
                        </span>
                      ))}
                  </div>
                )}
              </div>
            </div>
          );
        })}
      </div>

      {/* Load More */}
      {hasMore && (
        <div className="pt-4 text-center">
          <button
            type="button"
            onClick={() => setPage((p) => p + 1)}
            disabled={isLoading}
            className="inline-flex items-center gap-1.5 px-4 py-2 text-sm font-medium rounded-lg bg-white border border-border text-text-secondary hover:bg-slate-50 disabled:opacity-50 transition-colors"
          >
            {isLoading ? (
              <Loader2 className="h-4 w-4 animate-spin" />
            ) : (
              <ChevronDown className="h-4 w-4" />
            )}
            Load More
          </button>
        </div>
      )}
    </div>
  );
}
