import { useState } from "react";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { Loader2, Plus, UserX, UserCheck } from "lucide-react";
import { usersApi, adminApi } from "../../services/apiEndpoints";
import { cn } from "../../lib/utils";
import type { User, Region } from "../../types";

export default function TeamTab() {
  const queryClient = useQueryClient();
  const [showAddForm, setShowAddForm] = useState(false);
  const [newEmail, setNewEmail] = useState("");
  const [newName, setNewName] = useState("");
  const [newRole, setNewRole] = useState<"AGENT" | "MANAGER">("AGENT");
  const [newRegions, setNewRegions] = useState<number[]>([]);

  const { data: users, isLoading } = useQuery({
    queryKey: ["users"],
    queryFn: () => usersApi.list(),
  });

  const { data: regions } = useQuery({
    queryKey: ["admin-regions"],
    queryFn: () => adminApi.listRegions(),
  });

  const toggleStatusMutation = useMutation({
    mutationFn: (user: User) =>
      adminApi.updateUser(user.id, {
        status: user.status === "ACTIVE" ? "DEACTIVATED" : "ACTIVE",
      }),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ["users"] }),
  });

  const addUserMutation = useMutation({
    mutationFn: () =>
      adminApi.addUser({
        email: newEmail,
        name: newName,
        role: newRole,
        regionIds: newRegions,
      }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["users"] });
      setShowAddForm(false);
      setNewEmail("");
      setNewName("");
      setNewRole("AGENT");
      setNewRegions([]);
    },
  });

  function toggleRegion(id: number) {
    setNewRegions((prev) =>
      prev.includes(id) ? prev.filter((r) => r !== id) : [...prev, id]
    );
  }

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
        <h3 className="text-sm font-semibold text-text-primary">Team Members</h3>
        <button
          type="button"
          onClick={() => setShowAddForm(!showAddForm)}
          className="inline-flex items-center gap-1.5 px-3 py-1.5 text-xs font-medium rounded-lg bg-primary text-white hover:bg-primary-hover transition-colors"
        >
          <Plus className="h-3.5 w-3.5" />
          Add Agent
        </button>
      </div>

      {showAddForm && (
        <div className="bg-white rounded-xl border border-border p-4 space-y-3">
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
            <div>
              <label className="block text-[11px] font-medium uppercase tracking-wider text-text-muted mb-1">
                Email
              </label>
              <input
                type="email"
                value={newEmail}
                onChange={(e) => setNewEmail(e.target.value)}
                className="w-full rounded-lg border border-border px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-primary/40 focus:border-primary"
                placeholder="agent@turno.com"
              />
            </div>
            <div>
              <label className="block text-[11px] font-medium uppercase tracking-wider text-text-muted mb-1">
                Name
              </label>
              <input
                type="text"
                value={newName}
                onChange={(e) => setNewName(e.target.value)}
                className="w-full rounded-lg border border-border px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-primary/40 focus:border-primary"
                placeholder="Full name"
              />
            </div>
            <div>
              <label className="block text-[11px] font-medium uppercase tracking-wider text-text-muted mb-1">
                Role
              </label>
              <select
                value={newRole}
                onChange={(e) => setNewRole(e.target.value as "AGENT" | "MANAGER")}
                className="w-full rounded-lg border border-border px-3 py-2 text-sm bg-white focus:outline-none focus:ring-2 focus:ring-primary/40 focus:border-primary"
              >
                <option value="AGENT">Agent</option>
                <option value="MANAGER">Manager</option>
              </select>
            </div>
            <div>
              <label className="block text-[11px] font-medium uppercase tracking-wider text-text-muted mb-1">
                Regions
              </label>
              <div className="flex flex-wrap gap-1.5">
                {(regions ?? []).map((r: Region) => (
                  <button
                    key={r.id}
                    type="button"
                    onClick={() => toggleRegion(r.id)}
                    className={cn(
                      "px-2 py-1 rounded-full text-xs font-medium border transition-colors",
                      newRegions.includes(r.id)
                        ? "bg-primary text-white border-primary"
                        : "bg-white text-text-secondary border-border hover:border-primary"
                    )}
                  >
                    {r.name}
                  </button>
                ))}
              </div>
            </div>
          </div>
          <div className="flex items-center gap-2 pt-2">
            <button
              type="button"
              onClick={() => addUserMutation.mutate()}
              disabled={!newEmail || !newName || addUserMutation.isPending}
              className="px-3 py-1.5 text-xs font-medium rounded-lg bg-primary text-white hover:bg-primary-hover disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
            >
              {addUserMutation.isPending ? "Adding..." : "Add User"}
            </button>
            <button
              type="button"
              onClick={() => setShowAddForm(false)}
              className="px-3 py-1.5 text-xs rounded-lg text-text-secondary hover:bg-slate-100 transition-colors"
            >
              Cancel
            </button>
          </div>
        </div>
      )}

      <div className="space-y-2">
        {(users ?? []).map((user: User) => {
          const initials = user.name
            .split(" ")
            .map((n) => n[0])
            .join("")
            .slice(0, 2);
          return (
            <div
              key={user.id}
              className="bg-white rounded-xl border border-border p-4 flex items-center gap-4"
            >
              <div className="w-10 h-10 rounded-full bg-primary flex items-center justify-center text-white text-sm font-semibold shrink-0">
                {initials}
              </div>
              <div className="flex-1 min-w-0">
                <p className="text-sm font-medium text-text-primary truncate">
                  {user.name}
                </p>
                <p className="text-xs text-text-muted truncate">{user.email}</p>
                {user.regions.length > 0 && (
                  <div className="flex flex-wrap gap-1 mt-1">
                    {user.regions.map((r) => (
                      <span
                        key={r.id}
                        className="px-1.5 py-0.5 rounded-full text-[10px] font-medium bg-slate-100 text-slate-600"
                      >
                        {r.name}
                      </span>
                    ))}
                  </div>
                )}
              </div>
              <span
                className={cn(
                  "px-2 py-0.5 rounded-full text-xs font-medium shrink-0",
                  user.status === "ACTIVE"
                    ? "bg-teal-50 text-teal-700"
                    : "bg-slate-100 text-slate-500"
                )}
              >
                {user.status === "ACTIVE" ? "Active" : "Deactivated"}
              </span>
              <button
                type="button"
                onClick={() => toggleStatusMutation.mutate(user)}
                disabled={toggleStatusMutation.isPending}
                className={cn(
                  "inline-flex items-center gap-1 text-xs font-medium hover:underline shrink-0",
                  user.status === "ACTIVE" ? "text-danger" : "text-primary"
                )}
              >
                {user.status === "ACTIVE" ? (
                  <>
                    <UserX className="h-3.5 w-3.5" /> Deactivate
                  </>
                ) : (
                  <>
                    <UserCheck className="h-3.5 w-3.5" /> Reactivate
                  </>
                )}
              </button>
            </div>
          );
        })}
      </div>
    </div>
  );
}
