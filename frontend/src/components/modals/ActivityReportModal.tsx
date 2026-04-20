import { useState } from "react";
import { useMutation, useQueryClient } from "@tanstack/react-query";
import { X, MapPin, Video, Paperclip } from "lucide-react";
import { reportsApi } from "../../services/apiEndpoints";
import {
  TEMPLATE_LABELS,
  DURATION_LABELS,
  STAGE_NAMES,
} from "../../lib/constants";
import { formatDate, formatEnum, renderFieldValue } from "../../lib/formatters";
import { cn } from "../../lib/utils";
import { useAuth } from "../../context/AuthContext";
import { useDialog } from "../../hooks/useDialog";
import { TEMPLATE_FIELDS } from "../../lib/templateFields";
import ReportNotes from "../activity/ReportNotes";
import AttachmentPreviewModal from "../shared/AttachmentPreviewModal";
import type { ActivityReport, Attachment } from "../../types";

interface ActivityReportModalProps {
  open: boolean;
  onClose: () => void;
  report: ActivityReport | null;
  dealId: number;
}

export default function ActivityReportModal({ open, onClose, report, dealId }: ActivityReportModalProps) {
  const dialogRef = useDialog(open);
  const queryClient = useQueryClient();
  const { isManager } = useAuth();
  const defaultEta = open && report ? (report.nextActionEta ?? "") : "";
  const [editedEta, setEditedEta] = useState(defaultEta);
  const [etaDirty, setEtaDirty] = useState(false);
  const [previewAttachment, setPreviewAttachment] = useState<Attachment | null>(null);

  // Reset when modal opens with a new report
  const [prevReportId, setPrevReportId] = useState<number | null>(null);
  if (open && report && report.id !== prevReportId) {
    setPrevReportId(report.id);
    setEditedEta(report.nextActionEta ?? "");
    setEtaDirty(false);
  }

  const updateMutation = useMutation({
    mutationFn: (eta: string) => reportsApi.update(dealId, report!.id, { nextActionEta: eta }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["reports", dealId] });
      setEtaDirty(false);
    },
  });

  if (!open || !report) return null;

  const isField = report.activityType === "FIELD_VISIT";
  const templateLabel = TEMPLATE_LABELS[report.templateType] ?? report.templateType;
  const contactName = report.contact?.name || report.contactName || "Unknown";
  const fields = TEMPLATE_FIELDS[report.templateType] ?? [];

  return (
    <>
    <dialog
      ref={dialogRef}
      onCancel={onClose}
      className="backdrop:bg-black/60 backdrop:backdrop-blur-sm bg-transparent p-0 m-auto rounded-2xl max-w-2xl w-full max-md:mx-3"
    >
      <div className="bg-white rounded-2xl shadow-[0_25px_60px_rgba(0,0,0,0.25),0_10px_20px_rgba(0,0,0,0.1)] max-h-[90vh] flex flex-col">
        {/* Header */}
        <div className={cn(
          "flex items-center justify-between px-5 py-3 border-b border-border",
          report.voided && "bg-red-50"
        )}>
          <div className="flex items-center gap-3">
            <div className={cn(
              "shrink-0 w-8 h-8 rounded-full flex items-center justify-center",
              isField ? "bg-teal-100" : "bg-blue-100"
            )}>
              {isField ? <MapPin className="h-4 w-4 text-teal-600" /> : <Video className="h-4 w-4 text-blue-600" />}
            </div>
            <div>
              <h2 className={cn("text-lg font-semibold text-text-primary", report.voided && "line-through")}>
                {contactName}
                <span className="text-text-muted font-normal text-sm ml-1">-- {formatEnum(report.contactRole)}</span>
              </h2>
              <p className="text-xs text-text-muted">{isField ? "Field Visit" : "Virtual"}</p>
            </div>
          </div>
          <div className="flex items-center gap-2">
            {report.voided && (
              <span className="px-2 py-0.5 rounded text-[10px] font-medium bg-red-100 text-red-700">VOIDED</span>
            )}
            <button type="button" onClick={onClose} className="p-1 rounded-lg hover:bg-slate-100 text-text-muted">
              <X className="h-5 w-5" />
            </button>
          </div>
        </div>

        {/* Body */}
        <div className="flex-1 overflow-y-auto px-5 py-4 space-y-4">
          {/* Metadata */}
          <div className="grid grid-cols-2 md:grid-cols-3 gap-2">
            <MetaItem label="Template" value={`${report.templateType} - ${templateLabel}`} />
            <MetaItem label="Stage" value={STAGE_NAMES[report.loggedAtStage] || report.loggedAtStage} />
            <MetaItem label="Duration" value={DURATION_LABELS[report.duration] || formatEnum(report.duration)} />
            <MetaItem label="Interaction Date" value={formatDate(report.interactionDatetime)} />
            <MetaItem label="Submitted" value={formatDate(report.submissionDatetime)} />
            <MetaItem label="Agent" value={report.agentName || report.agent?.name || "-"} />
          </div>

          {/* Phase-specific data */}
          {fields.length > 0 && (
            <div>
              <h3 className="text-xs font-semibold uppercase tracking-wider text-text-muted mb-2">
                Template Data
              </h3>
              <div className="rounded-lg border border-border bg-slate-50 p-3">
                <div className="grid grid-cols-2 gap-x-4 gap-y-2">
                  {fields.map((field) => (
                    <div key={field.name}>
                      <span className="text-[10px] text-text-muted uppercase tracking-wider">{field.label}</span>
                      <p className="text-sm text-text-primary font-medium">
                        {renderFieldValue(report.phaseSpecificData?.[field.name])}
                      </p>
                    </div>
                  ))}
                </div>
              </div>
            </div>
          )}

          {/* Notes (report.notes field) */}
          {report.notes && (
            <div>
              <h3 className="text-xs font-semibold uppercase tracking-wider text-text-muted mb-1">
                Activity Notes
              </h3>
              <p className="text-sm text-text-secondary whitespace-pre-wrap bg-slate-50 rounded-lg border border-border p-3">
                {report.notes}
              </p>
            </div>
          )}

          {/* Objections & Buying Signals */}
          {(report.objections?.length > 0 || report.buyingSignals?.length > 0) && (
            <div className="flex flex-wrap gap-2">
              {report.objections?.length > 0 && (
                <div>
                  <h3 className="text-[10px] font-semibold uppercase tracking-wider text-text-muted mb-1">Objections</h3>
                  <div className="flex flex-wrap gap-1">
                    {report.objections.map((obj) => (
                      <span key={obj} className="px-2 py-0.5 rounded-full text-[10px] font-medium bg-red-50 text-red-700">
                        {obj}
                      </span>
                    ))}
                  </div>
                </div>
              )}
              {report.buyingSignals?.length > 0 && (
                <div>
                  <h3 className="text-[10px] font-semibold uppercase tracking-wider text-text-muted mb-1">Buying Signals</h3>
                  <div className="flex flex-wrap gap-1">
                    {report.buyingSignals.map((sig) => (
                      <span key={sig} className="px-2 py-0.5 rounded-full text-[10px] font-medium bg-teal-50 text-teal-700">
                        {sig}
                      </span>
                    ))}
                  </div>
                </div>
              )}
            </div>
          )}

          {/* Attachments */}
          {report.attachments?.length > 0 && (
            <div>
              <h3 className="text-xs font-semibold uppercase tracking-wider text-text-muted mb-1">Attachments</h3>
              <div className="space-y-1">
                {report.attachments.map((att) => (
                  <button
                    key={att.id}
                    type="button"
                    onClick={() => setPreviewAttachment(att)}
                    className="flex items-center gap-2 text-xs text-primary hover:underline w-full text-left py-0.5"
                  >
                    <Paperclip className="h-3 w-3 shrink-0" />
                    <span className="truncate">{att.fileName}</span>
                    <span className="text-text-muted shrink-0">({(att.fileSize / 1024).toFixed(0)} KB)</span>
                    {att.categoryTag && (
                      <span className="px-1.5 py-0.5 rounded bg-slate-100 text-[10px] text-text-muted shrink-0">
                        {att.categoryTag}
                      </span>
                    )}
                  </button>
                ))}
              </div>
            </div>
          )}

          {/* Next Action */}
          {report.nextAction && !report.voided && (
            <div className="rounded-lg border border-border bg-slate-50 p-3">
              <h3 className="text-xs font-semibold uppercase tracking-wider text-text-muted mb-2">Next Action</h3>
              <p className="text-sm text-text-primary font-medium">{report.nextAction}</p>
              <div className="grid grid-cols-2 gap-3 mt-2">
                <div>
                  <span className="text-[10px] text-text-muted uppercase tracking-wider">Due Date</span>
                  {isManager ? (
                    <div className="flex items-center gap-2 mt-0.5">
                      <input
                        type="date"
                        value={editedEta}
                        onChange={(e) => { setEditedEta(e.target.value); setEtaDirty(true); }}
                        className="rounded-lg border border-border px-2 py-1 text-sm text-text-primary focus:outline-none focus:ring-2 focus:ring-primary/40"
                      />
                      {etaDirty && (
                        <button
                          type="button"
                          onClick={() => updateMutation.mutate(editedEta)}
                          disabled={updateMutation.isPending}
                          className="px-2 py-1 text-xs font-medium rounded-md bg-primary text-white hover:bg-primary-hover disabled:opacity-50"
                        >
                          {updateMutation.isPending ? "Saving..." : "Save"}
                        </button>
                      )}
                    </div>
                  ) : (
                    <p className={cn(
                      "text-sm font-medium mt-0.5",
                      report.nextActionEta && new Date(report.nextActionEta) < new Date() ? "text-danger" : "text-text-primary"
                    )}>
                      {report.nextActionEta ? formatDate(report.nextActionEta) : "-"}
                    </p>
                  )}
                </div>
                <div>
                  <span className="text-[10px] text-text-muted uppercase tracking-wider">Owner</span>
                  <p className="text-sm text-text-primary font-medium mt-0.5">
                    {report.nextActionOwner ? formatEnum(report.nextActionOwner) : "-"}
                  </p>
                </div>
              </div>
            </div>
          )}

          {/* Void info */}
          {report.voided && (
            <div className="rounded-lg bg-red-50 border border-red-200 p-3">
              <h3 className="text-xs font-semibold uppercase tracking-wider text-red-700 mb-1">Voided</h3>
              {report.voidedReason && <p className="text-sm text-danger">{report.voidedReason}</p>}
              {report.voidedAt && (
                <p className="text-[10px] text-text-muted mt-1">Voided on {formatDate(report.voidedAt)}</p>
              )}
            </div>
          )}

          {/* Notes section */}
          <div className="border-t border-border pt-4">
            <h3 className="text-sm font-semibold text-text-primary mb-3">Notes</h3>
            <ReportNotes dealId={dealId} reportId={report.id} />
          </div>
        </div>

        {/* Footer */}
        <div className="flex items-center justify-end px-5 py-3 border-t border-border">
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

    <AttachmentPreviewModal
      open={!!previewAttachment}
      onClose={() => setPreviewAttachment(null)}
      attachmentId={previewAttachment?.id ?? null}
      fileName={previewAttachment?.fileName ?? ""}
    />
    </>
  );
}

function MetaItem({ label, value }: { label: string; value: string }) {
  return (
    <div>
      <span className="text-[10px] text-text-muted uppercase tracking-wider">{label}</span>
      <p className="text-sm text-text-primary font-medium">{value}</p>
    </div>
  );
}
