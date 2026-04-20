import { useState } from "react";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { Loader2, Plus, Power } from "lucide-react";
import { adminApi } from "../../services/apiEndpoints";
import { STAGE_NAMES } from "../../lib/constants";
import { cn } from "../../lib/utils";
import type { ChecklistConfig } from "../../types";

const ALL_STAGES = [
  "STAGE_1", "STAGE_2", "STAGE_3", "STAGE_4",
  "STAGE_5", "STAGE_6", "STAGE_7", "STAGE_8",
];

export default function ChecklistTab() {
  const queryClient = useQueryClient();
  const [showAdd, setShowAdd] = useState(false);
  const [newDocName, setNewDocName] = useState("");
  const [newRequirement, setNewRequirement] = useState<"MANDATORY" | "OPTIONAL">("MANDATORY");
  const [newStage, setNewStage] = useState("STAGE_7");

  const { data: items, isLoading } = useQuery({
    queryKey: ["admin-checklist"],
    queryFn: () => adminApi.listChecklist(),
  });

  const addMutation = useMutation({
    mutationFn: () =>
      adminApi.addChecklistItem({
        documentName: newDocName,
        requirement: newRequirement,
        requiredByStage: newStage,
      }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["admin-checklist"] });
      setShowAdd(false);
      setNewDocName("");
      setNewRequirement("MANDATORY");
      setNewStage("STAGE_7");
    },
  });

  const updateMutation = useMutation({
    mutationFn: ({ id, data }: { id: number; data: Record<string, unknown> }) =>
      adminApi.updateChecklistItem(id, data),
    onSuccess: () =>
      queryClient.invalidateQueries({ queryKey: ["admin-checklist"] }),
  });

  if (isLoading) {
    return (
      <div className="flex items-center justify-center py-12">
        <Loader2 className="h-6 w-6 animate-spin text-primary" />
      </div>
    );
  }

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <h3 className="text-sm font-semibold text-text-primary">Document Checklist</h3>
        <button
          type="button"
          onClick={() => setShowAdd(!showAdd)}
          className="inline-flex items-center gap-1.5 px-3 py-1.5 text-xs font-medium rounded-lg bg-primary text-white hover:bg-primary-hover transition-colors"
        >
          <Plus className="h-3.5 w-3.5" />
          Add Document
        </button>
      </div>

      {showAdd && (
        <div className="bg-white rounded-xl border border-border p-4 space-y-3">
          <div className="grid grid-cols-1 sm:grid-cols-3 gap-3">
            <div>
              <label className="block text-[11px] font-medium uppercase tracking-wider text-text-muted mb-1">
                Document Name
              </label>
              <input
                type="text"
                value={newDocName}
                onChange={(e) => setNewDocName(e.target.value)}
                className="w-full rounded-lg border border-border px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-primary/40"
                placeholder="e.g., Fleet Registration"
              />
            </div>
            <div>
              <label className="block text-[11px] font-medium uppercase tracking-wider text-text-muted mb-1">
                Requirement
              </label>
              <select
                value={newRequirement}
                onChange={(e) => setNewRequirement(e.target.value as "MANDATORY" | "OPTIONAL")}
                className="w-full rounded-lg border border-border px-3 py-2 text-sm bg-white focus:outline-none focus:ring-2 focus:ring-primary/40"
              >
                <option value="MANDATORY">Mandatory</option>
                <option value="OPTIONAL">Optional</option>
              </select>
            </div>
            <div>
              <label className="block text-[11px] font-medium uppercase tracking-wider text-text-muted mb-1">
                Required By Stage
              </label>
              <select
                value={newStage}
                onChange={(e) => setNewStage(e.target.value)}
                className="w-full rounded-lg border border-border px-3 py-2 text-sm bg-white focus:outline-none focus:ring-2 focus:ring-primary/40"
              >
                {ALL_STAGES.map((s) => (
                  <option key={s} value={s}>
                    {STAGE_NAMES[s]}
                  </option>
                ))}
              </select>
            </div>
          </div>
          <div className="flex items-center gap-2">
            <button
              type="button"
              onClick={() => addMutation.mutate()}
              disabled={!newDocName.trim() || addMutation.isPending}
              className="px-3 py-1.5 text-xs font-medium rounded-lg bg-primary text-white hover:bg-primary-hover disabled:opacity-50 transition-colors"
            >
              {addMutation.isPending ? "Adding..." : "Add"}
            </button>
            <button
              type="button"
              onClick={() => setShowAdd(false)}
              className="px-3 py-1.5 text-xs rounded-lg text-text-secondary hover:bg-slate-100 transition-colors"
            >
              Cancel
            </button>
          </div>
        </div>
      )}

      {/* Table */}
      <div className="bg-white rounded-xl border border-border overflow-hidden">
        <div className="overflow-x-auto">
          <table className="w-full text-sm">
            <thead>
              <tr className="border-b border-border">
                <th className="text-left px-4 py-3 text-[11px] font-medium uppercase tracking-wider text-text-muted">
                  Document Name
                </th>
                <th className="text-left px-4 py-3 text-[11px] font-medium uppercase tracking-wider text-text-muted">
                  Requirement
                </th>
                <th className="text-left px-4 py-3 text-[11px] font-medium uppercase tracking-wider text-text-muted">
                  Required By Stage
                </th>
                <th className="text-right px-4 py-3 text-[11px] font-medium uppercase tracking-wider text-text-muted">
                  Actions
                </th>
              </tr>
            </thead>
            <tbody>
              {((items as ChecklistConfig[] | undefined) ?? []).map((item) => (
                <tr
                  key={item.id}
                  className={cn(
                    "border-b border-border last:border-b-0 hover:bg-slate-50 transition-colors",
                    !item.active && "opacity-50"
                  )}
                >
                  <td className="px-4 py-3 font-medium text-text-primary">
                    {item.documentName}
                  </td>
                  <td className="px-4 py-3">
                    <button
                      type="button"
                      onClick={() =>
                        updateMutation.mutate({
                          id: item.id,
                          data: {
                            requirement:
                              item.requirement === "MANDATORY"
                                ? "OPTIONAL"
                                : "MANDATORY",
                          },
                        })
                      }
                      className={cn(
                        "px-2 py-0.5 rounded-full text-xs font-medium",
                        item.requirement === "MANDATORY"
                          ? "bg-red-50 text-red-700"
                          : "bg-slate-100 text-slate-600"
                      )}
                    >
                      {item.requirement === "MANDATORY" ? "Mandatory" : "Optional"}
                    </button>
                  </td>
                  <td className="px-4 py-3">
                    <select
                      value={item.requiredByStage}
                      onChange={(e) =>
                        updateMutation.mutate({
                          id: item.id,
                          data: { requiredByStage: e.target.value },
                        })
                      }
                      className="rounded border border-border px-2 py-1 text-xs bg-white focus:outline-none focus:ring-2 focus:ring-primary/40"
                    >
                      {ALL_STAGES.map((s) => (
                        <option key={s} value={s}>
                          {STAGE_NAMES[s]}
                        </option>
                      ))}
                    </select>
                  </td>
                  <td className="px-4 py-3 text-right">
                    <button
                      type="button"
                      onClick={() =>
                        updateMutation.mutate({
                          id: item.id,
                          data: { active: !item.active },
                        })
                      }
                      className={cn(
                        "inline-flex items-center gap-1 text-xs font-medium",
                        item.active
                          ? "text-amber-600 hover:text-amber-700"
                          : "text-emerald-600 hover:text-emerald-700"
                      )}
                    >
                      <Power className="h-3.5 w-3.5" />
                      {item.active ? "Deactivate" : "Activate"}
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}
