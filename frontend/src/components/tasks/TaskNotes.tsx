import { useState } from "react";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { Send, Loader2 } from "lucide-react";
import { tasksApi } from "../../services/apiEndpoints";
import { formatDateTime } from "../../lib/formatters";
import { useAuth } from "../../context/AuthContext";
import { cn } from "../../lib/utils";
import type { TaskNote } from "../../types";

interface TaskNotesProps {
  taskId: number;
}

export default function TaskNotes({ taskId }: TaskNotesProps) {
  const { isManager } = useAuth();
  const queryClient = useQueryClient();
  const [noteText, setNoteText] = useState("");

  const { data: notes = [], isLoading } = useQuery({
    queryKey: ["task-notes", taskId],
    queryFn: () => tasksApi.getNotes(taskId),
  });

  const addNoteMutation = useMutation({
    mutationFn: (data: { content: string; noteType: "AGENT" | "MANAGER" }) =>
      tasksApi.addNote(taskId, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["task-notes", taskId] });
      setNoteText("");
    },
  });

  const handleSubmit = () => {
    const content = noteText.trim();
    if (!content) return;
    addNoteMutation.mutate({
      content,
      noteType: isManager ? "MANAGER" : "AGENT",
    });
  };

  if (isLoading) {
    return (
      <div className="flex items-center justify-center py-4">
        <Loader2 className="h-4 w-4 animate-spin text-text-muted" />
      </div>
    );
  }

  return (
    <div className="space-y-3">
      <div className="max-h-[280px] overflow-y-auto space-y-2">
        {notes.length === 0 && (
          <p className="text-xs text-text-muted italic py-2 text-center">No notes yet</p>
        )}
        {notes.map((note: TaskNote) => {
          const isAgent = note.noteType === "AGENT";
          return (
            <div
              key={note.id}
              className={cn(
                "rounded-lg px-3 py-2 max-w-[85%]",
                isAgent
                  ? "border-r-4 border-r-teal-400 bg-teal-50/60 ml-auto"
                  : "border-l-4 border-l-blue-400 bg-blue-50/60 mr-auto"
              )}
            >
              <p className="text-sm text-text-primary whitespace-pre-wrap">{note.content}</p>
              <div className="flex items-center justify-end gap-1.5 mt-1.5 text-[10px] text-text-muted">
                <span className="font-medium">{note.createdByName}</span>
                <span>&middot;</span>
                <span>{formatDateTime(note.createdAt)}</span>
              </div>
            </div>
          );
        })}
      </div>

      <div className="flex items-end gap-2">
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
