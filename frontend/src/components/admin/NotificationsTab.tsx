import { useState, useMemo } from "react";
import { useQuery, useMutation } from "@tanstack/react-query";
import { Loader2, Save } from "lucide-react";
import { adminApi } from "../../services/apiEndpoints";
import { cn } from "../../lib/utils";
import type { NotificationPref } from "../../types";

const EVENT_TYPES: { type: string; description: string }[] = [
  { type: "DEAL_CREATED", description: "New deal is created" },
  { type: "DEAL_ARCHIVED", description: "Deal is archived/lost" },
  { type: "DEAL_REACTIVATED", description: "Archived deal is reactivated" },
  { type: "STAGE_ADVANCED", description: "Deal moves forward a stage" },
  { type: "STAGE_REGRESSED", description: "Deal moves backward a stage" },
  { type: "ACTIVITY_LOGGED", description: "Activity report is submitted" },
  { type: "PRICING_SUBMITTED", description: "Pricing submission needs review" },
  { type: "PRICING_APPROVED", description: "Pricing is approved by manager" },
  { type: "PRICING_REJECTED", description: "Pricing is rejected by manager" },
  { type: "FOLLOW_UP_OVERDUE", description: "Follow-up action is overdue" },
  { type: "DEAL_STALE", description: "Deal exceeds stale threshold" },
  { type: "BACKFILL_REQUESTED", description: "Backfill placement is requested" },
];

function buildPrefsMap(data: NotificationPref[] | undefined): Record<string, Record<string, boolean>> {
  const map: Record<string, Record<string, boolean>> = {};
  if (data) {
    data.forEach((p) => {
      if (!map[p.eventType]) map[p.eventType] = {};
      map[p.eventType][p.role] = p.enabled;
    });
  }
  EVENT_TYPES.forEach((e) => {
    if (!map[e.type]) map[e.type] = {};
    if (map[e.type]["AGENT"] === undefined) map[e.type]["AGENT"] = true;
    if (map[e.type]["MANAGER"] === undefined) map[e.type]["MANAGER"] = true;
  });
  return map;
}

export default function NotificationsTab() {
  const [overrides, setOverrides] = useState<Record<string, Record<string, boolean>> | null>(null);
  const [saved, setSaved] = useState(false);

  const { data, isLoading } = useQuery({
    queryKey: ["notification-prefs"],
    queryFn: () => adminApi.getNotificationPrefs(),
  });

  const basePrefs = useMemo(() => buildPrefsMap(data as NotificationPref[] | undefined), [data]);
  const prefs = overrides ?? basePrefs;

  const setPrefs = (updater: (prev: Record<string, Record<string, boolean>>) => Record<string, Record<string, boolean>>) => {
    setOverrides(updater(prefs));
  };

  const mutation = useMutation({
    mutationFn: () => {
      const payload: NotificationPref[] = [];
      for (const [eventType, roles] of Object.entries(prefs)) {
        for (const [role, enabled] of Object.entries(roles)) {
          payload.push({ eventType, role, enabled });
        }
      }
      return adminApi.updateNotificationPrefs(payload);
    },
    onSuccess: () => {
      setSaved(true);
      setTimeout(() => setSaved(false), 2000);
    },
  });

  function toggle(eventType: string, role: string) {
    setPrefs((prev) => ({
      ...prev,
      [eventType]: {
        ...prev[eventType],
        [role]: !prev[eventType]?.[role],
      },
    }));
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
      <h3 className="text-sm font-semibold text-text-primary">
        Notification Preferences
      </h3>

      <div className="bg-white rounded-xl border border-border overflow-hidden">
        <div className="overflow-x-auto">
          <table className="w-full text-sm">
            <thead>
              <tr className="border-b border-border">
                <th className="text-left px-4 py-3 text-[11px] font-medium uppercase tracking-wider text-text-muted">
                  Event Type
                </th>
                <th className="text-left px-4 py-3 text-[11px] font-medium uppercase tracking-wider text-text-muted">
                  Description
                </th>
                <th className="text-center px-4 py-3 text-[11px] font-medium uppercase tracking-wider text-text-muted">
                  Agent
                </th>
                <th className="text-center px-4 py-3 text-[11px] font-medium uppercase tracking-wider text-text-muted">
                  Manager
                </th>
              </tr>
            </thead>
            <tbody>
              {EVENT_TYPES.map((e) => (
                <tr
                  key={e.type}
                  className="border-b border-border last:border-b-0 hover:bg-slate-50 transition-colors"
                >
                  <td className="px-4 py-3 font-medium text-text-primary whitespace-nowrap">
                    {e.type.replace(/_/g, " ")}
                  </td>
                  <td className="px-4 py-3 text-text-secondary">{e.description}</td>
                  <td className="px-4 py-3 text-center">
                    <button
                      type="button"
                      onClick={() => toggle(e.type, "AGENT")}
                      className={cn(
                        "w-10 h-5 rounded-full relative transition-colors",
                        prefs[e.type]?.["AGENT"]
                          ? "bg-primary"
                          : "bg-slate-200"
                      )}
                    >
                      <span
                        className={cn(
                          "absolute top-0.5 w-4 h-4 rounded-full bg-white shadow transition-transform",
                          prefs[e.type]?.["AGENT"]
                            ? "translate-x-5"
                            : "translate-x-0.5"
                        )}
                      />
                    </button>
                  </td>
                  <td className="px-4 py-3 text-center">
                    <button
                      type="button"
                      onClick={() => toggle(e.type, "MANAGER")}
                      className={cn(
                        "w-10 h-5 rounded-full relative transition-colors",
                        prefs[e.type]?.["MANAGER"]
                          ? "bg-primary"
                          : "bg-slate-200"
                      )}
                    >
                      <span
                        className={cn(
                          "absolute top-0.5 w-4 h-4 rounded-full bg-white shadow transition-transform",
                          prefs[e.type]?.["MANAGER"]
                            ? "translate-x-5"
                            : "translate-x-0.5"
                        )}
                      />
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>

      <div className="flex items-center gap-3">
        <button
          type="button"
          onClick={() => mutation.mutate()}
          disabled={mutation.isPending}
          className="inline-flex items-center gap-1.5 px-4 py-2 text-sm font-medium rounded-lg bg-primary text-white hover:bg-primary-hover disabled:opacity-50 transition-colors"
        >
          {mutation.isPending ? (
            <Loader2 className="h-4 w-4 animate-spin" />
          ) : (
            <Save className="h-4 w-4" />
          )}
          Save Preferences
        </button>
        {saved && (
          <span className="text-sm text-emerald-600 font-medium">Saved!</span>
        )}
      </div>
    </div>
  );
}
