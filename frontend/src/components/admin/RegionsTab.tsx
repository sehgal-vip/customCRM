import { useState } from "react";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { Loader2, Plus, Power } from "lucide-react";
import { adminApi } from "../../services/apiEndpoints";
import { cn } from "../../lib/utils";
import type { Region } from "../../types";

export default function RegionsTab() {
  const queryClient = useQueryClient();
  const [newName, setNewName] = useState("");

  const { data: regions, isLoading } = useQuery({
    queryKey: ["admin-regions"],
    queryFn: () => adminApi.listRegions(),
  });

  const addMutation = useMutation({
    mutationFn: () => adminApi.addRegion(newName),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["admin-regions"] });
      setNewName("");
    },
  });

  const updateMutation = useMutation({
    mutationFn: ({ id, data }: { id: number; data: Record<string, unknown> }) =>
      adminApi.updateRegion(id, data),
    onSuccess: () =>
      queryClient.invalidateQueries({ queryKey: ["admin-regions"] }),
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
      <h3 className="text-sm font-semibold text-text-primary">Regions</h3>

      <div className="flex items-center gap-2">
        <input
          type="text"
          value={newName}
          onChange={(e) => setNewName(e.target.value)}
          placeholder="New region name..."
          className="flex-1 max-w-xs rounded-lg border border-border px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-primary/40 focus:border-primary"
          onKeyDown={(e) => {
            if (e.key === "Enter" && newName.trim()) addMutation.mutate();
          }}
        />
        <button
          type="button"
          onClick={() => addMutation.mutate()}
          disabled={!newName.trim() || addMutation.isPending}
          className="inline-flex items-center gap-1.5 px-3 py-2 text-sm font-medium rounded-lg bg-primary text-white hover:bg-primary-hover disabled:opacity-50 transition-colors"
        >
          <Plus className="h-4 w-4" />
          Add Region
        </button>
      </div>

      <div className="space-y-2">
        {((regions as Region[] | undefined) ?? []).map((region) => (
          <div
            key={region.id}
            className={cn(
              "bg-white rounded-xl border border-border p-4 flex items-center justify-between",
              region.active === false && "opacity-50"
            )}
          >
            <div>
              <p className="text-sm font-medium text-text-primary">
                {region.name}
              </p>
              <span
                className={cn(
                  "text-xs font-medium",
                  region.active !== false ? "text-emerald-600" : "text-text-muted"
                )}
              >
                {region.active !== false ? "Active" : "Archived"}
              </span>
            </div>
            <button
              type="button"
              onClick={() =>
                updateMutation.mutate({
                  id: region.id,
                  data: { active: region.active === false },
                })
              }
              className={cn(
                "inline-flex items-center gap-1 text-xs font-medium",
                region.active !== false
                  ? "text-amber-600 hover:text-amber-700"
                  : "text-emerald-600 hover:text-emerald-700"
              )}
            >
              <Power className="h-3.5 w-3.5" />
              {region.active !== false ? "Archive" : "Restore"}
            </button>
          </div>
        ))}
      </div>
    </div>
  );
}
