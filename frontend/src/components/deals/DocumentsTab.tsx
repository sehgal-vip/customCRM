import { useRef } from "react";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { Loader2, Upload, AlertTriangle } from "lucide-react";
import { documentsApi } from "../../services/apiEndpoints";
import { cn } from "../../lib/utils";
import { STAGE_NAMES } from "../../lib/constants";
import type { DocumentChecklistItem } from "../../types";

interface DocumentsTabProps {
  dealId: number;
  currentStage: string;
}

const DOC_STATUS_STYLES: Record<string, { bg: string; text: string; label: string }> = {
  NOT_STARTED: { bg: "bg-slate-100", text: "text-slate-600", label: "Not Started" },
  REQUESTED: { bg: "bg-amber-50", text: "text-amber-700", label: "Requested" },
  RECEIVED: { bg: "bg-blue-50", text: "text-blue-700", label: "Received" },
  VERIFIED: { bg: "bg-emerald-50", text: "text-emerald-700", label: "Verified" },
};

export default function DocumentsTab({ dealId, currentStage }: DocumentsTabProps) {
  const queryClient = useQueryClient();

  const { data, isLoading } = useQuery({
    queryKey: ["documents", dealId],
    queryFn: () => documentsApi.list(dealId),
  });

  const isStage8OrLater =
    parseInt(currentStage.replace("STAGE_", ""), 10) >= 8;
  const hasUnmetMandatory =
    data && data.mandatoryComplete < data.mandatoryTotal;

  if (isLoading) {
    return (
      <div className="flex items-center justify-center py-12">
        <Loader2 className="h-6 w-6 animate-spin text-primary" />
      </div>
    );
  }

  if (!data) {
    return (
      <div className="py-12 text-center text-sm text-text-muted">
        Failed to load documents.
      </div>
    );
  }

  return (
    <div className="space-y-4">
      {/* Stage 8 Warning */}
      {isStage8OrLater && hasUnmetMandatory && (
        <div className="rounded-lg bg-red-50 border border-red-200 px-4 py-3 flex items-start gap-2">
          <AlertTriangle className="h-4 w-4 text-danger shrink-0 mt-0.5" />
          <p className="text-sm text-danger">
            All mandatory documents must be Received or Verified before
            advancing to Stage 8.
          </p>
        </div>
      )}

      {/* Completion bar */}
      <div className="bg-white rounded-xl border border-border p-4 shadow-[0_1px_2px_rgba(0,0,0,0.05)]">
        <div className="flex items-center justify-between mb-2">
          <p className="text-sm font-medium text-text-primary">
            Document Completion
          </p>
          <p className="text-xs text-text-muted">
            {data.mandatoryComplete} of {data.mandatoryTotal} mandatory
            documents complete
          </p>
        </div>
        <div className="w-full h-2 bg-slate-200 rounded-full overflow-hidden">
          <div
            className="h-full bg-primary rounded-full transition-all duration-300"
            style={{ width: `${data.percentage}%` }}
          />
        </div>
      </div>

      {/* Checklist Table */}
      {data.items.length === 0 ? (
        <div className="bg-white rounded-xl border border-border p-8 text-center shadow-[0_1px_2px_rgba(0,0,0,0.05)]">
          <Upload className="h-8 w-8 mx-auto text-text-muted mb-2" />
          <p className="text-sm font-medium text-text-primary">No documents required yet</p>
          <p className="text-xs text-text-muted mt-1">
            Document requirements will appear as the deal progresses through stages.
          </p>
        </div>
      ) : (
        <div className="bg-white rounded-xl border border-border shadow-[0_1px_2px_rgba(0,0,0,0.05)] overflow-hidden">
          {/* Desktop table */}
          <div className="hidden md:block overflow-x-auto">
            <table className="w-full text-sm">
              <thead>
                <tr className="border-b border-border">
                  <th className="px-4 py-3 text-left text-[11px] font-medium uppercase tracking-wider text-text-muted">
                    Document
                  </th>
                  <th className="px-4 py-3 text-left text-[11px] font-medium uppercase tracking-wider text-text-muted">
                    Requirement
                  </th>
                  <th className="px-4 py-3 text-left text-[11px] font-medium uppercase tracking-wider text-text-muted">
                    Required By
                  </th>
                  <th className="px-4 py-3 text-left text-[11px] font-medium uppercase tracking-wider text-text-muted">
                    Status
                  </th>
                  <th className="px-4 py-3 text-left text-[11px] font-medium uppercase tracking-wider text-text-muted">
                    Upload
                  </th>
                </tr>
              </thead>
              <tbody>
                {data.items.map((item) => (
                  <DocumentRow
                    key={item.id}
                    item={item}
                    dealId={dealId}
                    queryClient={queryClient}
                  />
                ))}
              </tbody>
            </table>
          </div>

          {/* Mobile cards — pb-16 ensures last card's upload button isn't obscured by fixed bottom tab bar */}
          <div className="md:hidden divide-y divide-border pb-16">
            {data.items.map((item) => (
              <DocumentCard
                key={item.id}
                item={item}
                dealId={dealId}
                queryClient={queryClient}
              />
            ))}
          </div>
        </div>
      )}
    </div>
  );
}

function DocumentRow({
  item,
  dealId,
  queryClient,
}: {
  item: DocumentChecklistItem;
  dealId: number;
  queryClient: ReturnType<typeof useQueryClient>;
}) {
  const fileInputRef = useRef<HTMLInputElement>(null);
  const statusStyle = DOC_STATUS_STYLES[item.status] ?? DOC_STATUS_STYLES.NOT_STARTED;

  const uploadMutation = useMutation({
    mutationFn: (file: File) => documentsApi.upload(dealId, item.id, file),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["documents", dealId] });
    },
  });

  const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (file) uploadMutation.mutate(file);
  };

  return (
    <tr className="border-b border-border last:border-0 hover:bg-slate-50 transition-colors">
      <td className="px-4 py-3 text-text-primary font-medium">
        {item.documentName}
      </td>
      <td className="px-4 py-3">
        <span
          className={cn(
            "inline-flex items-center px-2 py-0.5 rounded-full text-[10px] font-medium",
            item.requirement === "MANDATORY"
              ? "bg-red-50 text-red-700"
              : "bg-slate-100 text-slate-600"
          )}
        >
          {item.requirement === "MANDATORY" ? "Mandatory" : "Optional"}
        </span>
      </td>
      <td className="px-4 py-3 text-text-secondary text-xs">
        {STAGE_NAMES[item.requiredByStage] ?? item.requiredByStage}
      </td>
      <td className="px-4 py-3">
        <span
          className={cn(
            "inline-flex items-center px-2 py-0.5 rounded-full text-[10px] font-medium",
            statusStyle.bg,
            statusStyle.text
          )}
        >
          {statusStyle.label}
        </span>
      </td>
      <td className="px-4 py-3">
        <input
          ref={fileInputRef}
          type="file"
          onChange={handleFileChange}
          className="hidden"
        />
        <button
          type="button"
          onClick={() => fileInputRef.current?.click()}
          disabled={uploadMutation.isPending}
          className="inline-flex items-center gap-1 px-2 py-1 text-xs rounded-lg border border-border text-text-secondary hover:bg-slate-50 transition-colors disabled:opacity-50"
        >
          {uploadMutation.isPending ? (
            <Loader2 className="h-3 w-3 animate-spin" />
          ) : (
            <Upload className="h-3 w-3" />
          )}
          Upload
        </button>
      </td>
    </tr>
  );
}

function DocumentCard({
  item,
  dealId,
  queryClient,
}: {
  item: DocumentChecklistItem;
  dealId: number;
  queryClient: ReturnType<typeof useQueryClient>;
}) {
  const fileInputRef = useRef<HTMLInputElement>(null);
  const statusStyle = DOC_STATUS_STYLES[item.status] ?? DOC_STATUS_STYLES.NOT_STARTED;

  const uploadMutation = useMutation({
    mutationFn: (file: File) => documentsApi.upload(dealId, item.id, file),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["documents", dealId] });
    },
  });

  const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (file) uploadMutation.mutate(file);
  };

  return (
    <div className="p-4 space-y-2">
      <div className="flex items-start justify-between gap-2">
        <p className="text-sm font-medium text-text-primary">
          {item.documentName}
        </p>
        <span
          className={cn(
            "inline-flex items-center px-2 py-0.5 rounded-full text-[10px] font-medium shrink-0",
            statusStyle.bg,
            statusStyle.text
          )}
        >
          {statusStyle.label}
        </span>
      </div>
      <div className="flex items-center gap-2 text-xs text-text-muted">
        <span
          className={cn(
            "px-1.5 py-0.5 rounded text-[10px] font-medium",
            item.requirement === "MANDATORY"
              ? "bg-red-50 text-red-700"
              : "bg-slate-100 text-slate-600"
          )}
        >
          {item.requirement === "MANDATORY" ? "Mandatory" : "Optional"}
        </span>
        <span>
          Required by {STAGE_NAMES[item.requiredByStage] ?? item.requiredByStage}
        </span>
      </div>
      <input
        ref={fileInputRef}
        type="file"
        onChange={handleFileChange}
        className="hidden"
      />
      <button
        type="button"
        onClick={() => fileInputRef.current?.click()}
        disabled={uploadMutation.isPending}
        className="relative z-10 inline-flex items-center gap-1 px-2 py-1 text-xs rounded-lg border border-border text-text-secondary hover:bg-slate-50 transition-colors disabled:opacity-50"
      >
        {uploadMutation.isPending ? (
          <Loader2 className="h-3 w-3 animate-spin" />
        ) : (
          <Upload className="h-3 w-3" />
        )}
        Upload
      </button>
    </div>
  );
}
