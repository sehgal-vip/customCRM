import { useState, useMemo } from "react";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { Loader2, Save } from "lucide-react";
import { toast } from "sonner";
import { adminApi } from "../../services/apiEndpoints";
import { STAGE_NAMES, PHASE_STAGES, PHASE_NAMES } from "../../lib/constants";
import { getErrorMessage } from "../../lib/utils";
import type { StaleThreshold, ExitCriteria } from "../../types";

const ALL_STAGES = [
  "STAGE_1", "STAGE_2", "STAGE_3", "STAGE_4",
  "STAGE_5", "STAGE_6", "STAGE_7", "STAGE_8",
];

function getPhaseForStage(stage: string): string {
  for (const [phase, stages] of Object.entries(PHASE_STAGES)) {
    if ((stages as readonly string[]).includes(stage)) return phase;
  }
  return "EARLY";
}

function buildThresholdsMap(data: StaleThreshold[] | undefined): Record<string, number> {
  const map: Record<string, number> = {};
  if (data) {
    data.forEach((t: StaleThreshold) => {
      map[t.stage] = t.thresholdDays;
    });
  }
  ALL_STAGES.forEach((s) => {
    if (!(s in map)) {
      const phase = getPhaseForStage(s);
      map[s] = phase === "CLOSURE" ? 21 : 14;
    }
  });
  return map;
}

function buildCriteriaMap(data: ExitCriteria[] | undefined): Record<string, boolean> {
  const map: Record<string, boolean> = {};
  ALL_STAGES.forEach((s) => { map[s] = true; }); // default: required
  if (data) {
    data.forEach((c: ExitCriteria) => {
      map[c.stage] = c.activityRequired;
    });
  }
  return map;
}

export default function StagesTab() {
  const queryClient = useQueryClient();
  const [overrides, setOverrides] = useState<Record<string, number> | null>(null);
  const [criteriaOverrides, setCriteriaOverrides] = useState<Record<string, boolean> | null>(null);
  const [saved, setSaved] = useState(false);

  const { data, isLoading } = useQuery({
    queryKey: ["stale-thresholds"],
    queryFn: () => adminApi.getStaleThresholds(),
  });

  const { data: criteriaData, isLoading: criteriaLoading } = useQuery({
    queryKey: ["exit-criteria"],
    queryFn: () => adminApi.getExitCriteria(),
  });

  const baseThresholds = useMemo(() => buildThresholdsMap(data), [data]);
  const thresholds = overrides ?? baseThresholds;

  const baseCriteria = useMemo(() => buildCriteriaMap(criteriaData), [criteriaData]);
  const criteria = criteriaOverrides ?? baseCriteria;

  const setThresholds = (updater: (prev: Record<string, number>) => Record<string, number>) => {
    setOverrides(updater(thresholds));
  };

  const thresholdMutation = useMutation({
    mutationFn: () => {
      const payload: StaleThreshold[] = Object.entries(thresholds).map(
        ([stage, thresholdDays]) => ({ stage, thresholdDays })
      );
      return adminApi.updateStaleThresholds(payload);
    },
    onSuccess: () => {
      setSaved(true);
      setTimeout(() => setSaved(false), 2000);
    },
    onError: (err: unknown) => {
      toast.error(getErrorMessage(err, "Failed to save thresholds"));
    },
  });

  const criteriaMutation = useMutation({
    mutationFn: () => adminApi.updateExitCriteria(criteria),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["exit-criteria"] });
      toast.success("Exit criteria saved");
    },
    onError: (err: unknown) => {
      toast.error(getErrorMessage(err, "Failed to save exit criteria"));
    },
  });

  const saveBoth = () => {
    thresholdMutation.mutate();
    criteriaMutation.mutate();
  };

  if (isLoading || criteriaLoading) {
    return (
      <div className="flex items-center justify-center py-12">
        <Loader2 className="h-6 w-6 animate-spin text-primary" />
      </div>
    );
  }

  const isSaving = thresholdMutation.isPending || criteriaMutation.isPending;

  return (
    <div className="space-y-4">
      <h3 className="text-sm font-semibold text-text-primary">
        Pipeline Stages &amp; Configuration
      </h3>

      <div className="space-y-2">
        {ALL_STAGES.map((stage, idx) => {
          const phase = getPhaseForStage(stage);
          return (
            <div
              key={stage}
              className="bg-white rounded-xl border border-border p-4 flex items-center gap-4 flex-wrap"
            >
              <span className="w-8 h-8 rounded-full bg-slate-100 flex items-center justify-center text-sm font-semibold text-text-primary shrink-0">
                {idx + 1}
              </span>
              <div className="flex-1 min-w-0">
                <p className="text-sm font-medium text-text-primary">
                  {STAGE_NAMES[stage]}
                </p>
                <span className="text-[10px] font-medium text-text-muted uppercase tracking-wider">
                  {PHASE_NAMES[phase]}
                </span>
              </div>
              <label className="flex items-center gap-2 shrink-0 cursor-pointer">
                <input
                  type="checkbox"
                  checked={criteria[stage] ?? true}
                  onChange={(e) =>
                    setCriteriaOverrides({ ...criteria, [stage]: e.target.checked })
                  }
                  className="h-4 w-4 rounded border-border text-primary focus:ring-primary/40"
                />
                <span className="text-xs text-text-secondary">Activity required</span>
              </label>
              <div className="flex items-center gap-2 shrink-0">
                <input
                  type="number"
                  min={1}
                  max={90}
                  value={thresholds[stage] ?? 14}
                  onChange={(e) =>
                    setThresholds((prev) => ({
                      ...prev,
                      [stage]: parseInt(e.target.value, 10) || 0,
                    }))
                  }
                  className="w-16 rounded-lg border border-border px-2 py-1.5 text-sm text-center focus:outline-none focus:ring-2 focus:ring-primary/40 focus:border-primary"
                />
                <span className="text-xs text-text-muted">days</span>
              </div>
            </div>
          );
        })}
      </div>

      <div className="flex items-center gap-3">
        <button
          type="button"
          onClick={saveBoth}
          disabled={isSaving}
          className="inline-flex items-center gap-1.5 px-4 py-2 text-sm font-medium rounded-lg bg-primary text-white hover:bg-primary-hover disabled:opacity-50 transition-colors"
        >
          {isSaving ? (
            <Loader2 className="h-4 w-4 animate-spin" />
          ) : (
            <Save className="h-4 w-4" />
          )}
          Save Settings
        </button>
        {saved && (
          <span className="text-sm text-emerald-600 font-medium">Saved!</span>
        )}
      </div>
    </div>
  );
}
