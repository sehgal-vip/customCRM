import { useState } from "react";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { Loader2, Pencil, Check, X, Power, Plus } from "lucide-react";
import { adminApi } from "../../services/apiEndpoints";
import { cn } from "../../lib/utils";
import type { TaxonomyItem } from "../../types";

const TAXONOMY_TYPES = [
  { key: "OBJECTION", label: "Objections" },
  { key: "BUYING_SIGNAL", label: "Buying Signals" },
  { key: "LOST_REASON", label: "Lost Reasons" },
  { key: "ATTACHMENT_TAG", label: "Attachment Tags" },
];

function TaxonomyCard({ type, label }: { type: string; label: string }) {
  const queryClient = useQueryClient();
  const [newValue, setNewValue] = useState("");
  const [editingId, setEditingId] = useState<number | null>(null);
  const [editValue, setEditValue] = useState("");

  const { data: items, isLoading } = useQuery({
    queryKey: ["taxonomy", type],
    queryFn: () => adminApi.listTaxonomy(type),
  });

  const addMutation = useMutation({
    mutationFn: () => adminApi.addTaxonomy(type, newValue),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["taxonomy", type] });
      setNewValue("");
    },
  });

  const updateMutation = useMutation({
    mutationFn: ({ id, data }: { id: number; data: Record<string, unknown> }) =>
      adminApi.updateTaxonomy(type, id, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["taxonomy", type] });
      setEditingId(null);
    },
  });

  function startEdit(item: TaxonomyItem) {
    setEditingId(item.id);
    setEditValue(item.value);
  }

  function saveEdit(id: number) {
    updateMutation.mutate({ id, data: { value: editValue } });
  }

  function toggleActive(item: TaxonomyItem) {
    updateMutation.mutate({ id: item.id, data: { active: !item.active } });
  }

  return (
    <div className="bg-white rounded-xl border border-border p-4">
      <h4 className="text-sm font-semibold text-text-primary mb-3">{label}</h4>
      {isLoading ? (
        <div className="flex justify-center py-4">
          <Loader2 className="h-5 w-5 animate-spin text-primary" />
        </div>
      ) : (
        <div className="space-y-1.5">
          {((items as TaxonomyItem[] | undefined) ?? []).map((item) => (
            <div
              key={item.id}
              className={cn(
                "flex items-center gap-2 py-1.5 px-2 rounded-lg group",
                !item.active && "opacity-50"
              )}
            >
              {editingId === item.id ? (
                <>
                  <input
                    type="text"
                    value={editValue}
                    onChange={(e) => setEditValue(e.target.value)}
                    className="flex-1 rounded border border-border px-2 py-1 text-sm focus:outline-none focus:ring-2 focus:ring-primary/40"
                    autoFocus
                  />
                  <button
                    type="button"
                    onClick={() => saveEdit(item.id)}
                    className="p-1 text-emerald-600 hover:bg-emerald-50 rounded"
                  >
                    <Check className="h-3.5 w-3.5" />
                  </button>
                  <button
                    type="button"
                    onClick={() => setEditingId(null)}
                    className="p-1 text-text-muted hover:bg-slate-100 rounded"
                  >
                    <X className="h-3.5 w-3.5" />
                  </button>
                </>
              ) : (
                <>
                  <span className="flex-1 text-sm text-text-primary truncate">
                    {item.value}
                  </span>
                  <button
                    type="button"
                    onClick={() => startEdit(item)}
                    className="p-1 text-text-muted hover:text-primary opacity-0 group-hover:opacity-100 transition-opacity"
                    title="Rename"
                  >
                    <Pencil className="h-3.5 w-3.5" />
                  </button>
                  <button
                    type="button"
                    onClick={() => toggleActive(item)}
                    className={cn(
                      "p-1 rounded transition-opacity opacity-0 group-hover:opacity-100",
                      item.active
                        ? "text-amber-600 hover:bg-amber-50"
                        : "text-emerald-600 hover:bg-emerald-50"
                    )}
                    title={item.active ? "Archive" : "Restore"}
                  >
                    <Power className="h-3.5 w-3.5" />
                  </button>
                </>
              )}
            </div>
          ))}
        </div>
      )}

      <div className="flex items-center gap-2 mt-3 pt-3 border-t border-border">
        <input
          type="text"
          value={newValue}
          onChange={(e) => setNewValue(e.target.value)}
          placeholder="New item..."
          className="flex-1 rounded-lg border border-border px-3 py-1.5 text-sm focus:outline-none focus:ring-2 focus:ring-primary/40 focus:border-primary"
          onKeyDown={(e) => {
            if (e.key === "Enter" && newValue.trim()) addMutation.mutate();
          }}
        />
        <button
          type="button"
          onClick={() => addMutation.mutate()}
          disabled={!newValue.trim() || addMutation.isPending}
          className="inline-flex items-center gap-1 px-3 py-1.5 text-xs font-medium rounded-lg bg-primary text-white hover:bg-primary-hover disabled:opacity-50 transition-colors"
        >
          <Plus className="h-3.5 w-3.5" />
          Add
        </button>
      </div>
    </div>
  );
}

export default function TaxonomiesTab() {
  return (
    <div className="space-y-4">
      <h3 className="text-sm font-semibold text-text-primary">Taxonomies</h3>
      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
        {TAXONOMY_TYPES.map((t) => (
          <TaxonomyCard key={t.key} type={t.key} label={t.label} />
        ))}
      </div>
    </div>
  );
}
