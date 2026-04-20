import { useState } from "react";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { Send, Loader2 } from "lucide-react";
import { reportsApi } from "../../services/apiEndpoints";
import { cn } from "../../lib/utils";
import { formatDateTime } from "../../lib/formatters";
import type { ActivityReportNote } from "../../types";

interface ReportNotesProps {
  dealId: number;
  reportId: number;
}

export default function ReportNotes({ dealId, reportId }: ReportNotesProps) {
  const queryClient = useQueryClient();
  const [noteText, setNoteText] = useState("");

  const { data: notes = [], isLoading } = useQuery({
    queryKey: ["report-notes", dealId, reportId],
    queryFn: () => reportsApi.getNotes(dealId, reportId),
  });

  const addNoteMutation = useMutation({
    mutationFn: (content: string) => reportsApi.addNote(dealId, reportId, { content }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["report-notes", dealId, reportId] });
      setNoteText("");
    },
  });

  const handleSubmit = () => {
    const content = noteText.trim();
    if (!content) return;
    addNoteMutation.mutate(content);
  };

  if (isLoading) {
    return (
      <div className="flex items-center justify-center py-4">
        <Loader2 className="h-4 w-4 animate-spin text-text-muted" />
      </div>
    );
  }

  return (
    <div>
      {/* Notes list */}
      <div className="max-h-[200px] overflow-y-auto space-y-1.5">
        {notes.length === 0 && (
          <p className="text-xs text-text-muted italic py-2">No notes yet</p>
        )}
        {notes.map((note: ActivityReportNote) => (
          <div
            key={note.id}
            className="rounded-lg border border-border bg-slate-50 px-2.5 py-1.5"
          >
            <p className="text-sm text-text-primary whitespace-pre-wrap">{note.content}</p>
            <div className="flex items-center gap-2 mt-1.5 text-[10px] text-text-muted">
              <span className="font-medium">{note.createdByName}</span>
              <span>{formatDateTime(note.createdAt)}</span>
            </div>
          </div>
        ))}
      </div>

      {/* Add note */}
      <div className="flex items-end gap-2 mt-2">
        <textarea
          value={noteText}
          onChange={(e) => setNoteText(e.target.value)}
          placeholder="Add a note..."
          rows={2}
          className="flex-1 rounded-lg border border-border px-3 py-2 text-sm text-text-primary resize-none focus:outline-none focus:ring-2 focus:ring-primary/40 focus:border-primary"
        />
        <button
          type="button"
          onClick={handleSubmit}
          disabled={!noteText.trim() || addNoteMutation.isPending}
          className={cn(
            "p-2 rounded-lg text-white transition-colors",
            "bg-primary hover:bg-primary-hover",
            "disabled:opacity-50 disabled:cursor-not-allowed"
          )}
        >
          {addNoteMutation.isPending ? (
            <Loader2 className="h-4 w-4 animate-spin" />
          ) : (
            <Send className="h-4 w-4" />
          )}
        </button>
      </div>
    </div>
  );
}
