import { ChevronDown } from "lucide-react";
import { formatINR } from "../../lib/formatters";
import { STAGE_NAMES, STAGE_COLORS, PHASE_HEADER_COLORS } from "../../lib/constants";
import { cn } from "../../lib/utils";
import DealCard from "./DealCard";
import type { DealListItem } from "../../types";

interface ExpandedPhaseSectionProps {
  phaseName: string;
  phaseKey: string;
  stageKeys: string[];
  dealsByStage: Record<string, DealListItem[]>;
  onCollapse: () => void;
}

export default function ExpandedPhaseSection({
  phaseName,
  phaseKey,
  stageKeys,
  dealsByStage,
  onCollapse,
}: ExpandedPhaseSectionProps) {
  const totalDeals = stageKeys.reduce((sum, k) => sum + (dealsByStage[k]?.length ?? 0), 0);
  const totalValue = stageKeys.reduce((acc, k) => acc + (dealsByStage[k] ?? []).reduce((s, d) => s + (d.estimatedMonthlyValue ?? 0), 0), 0);
  const colors = PHASE_HEADER_COLORS[phaseKey] ?? PHASE_HEADER_COLORS.EARLY;

  return (
    <div className="flex flex-col min-h-0 h-full">
      <button
        type="button"
        onClick={onCollapse}
        className={cn(
          "flex items-center justify-between px-4 py-2 rounded-t-xl border transition-colors shadow-sm",
          colors.bg, colors.border,
          "hover:brightness-95"
        )}
      >
        <div className="flex items-center gap-2">
          <ChevronDown className={cn("h-4 w-4", colors.text)} />
          <h3 className={cn("text-sm font-bold", colors.text)}>{phaseName}</h3>
          <span className={cn("text-[10px] font-semibold px-2 py-0.5 rounded-full", colors.bg, colors.text, "border", colors.border)}>
            {totalDeals}
          </span>
        </div>
        <span className="text-xs text-text-muted font-medium truncate ml-1">{formatINR(totalValue)}</span>
      </button>

      <div
        className="flex-1 grid gap-0 border border-t-0 rounded-b-xl overflow-hidden"
        style={{ gridTemplateColumns: `repeat(${stageKeys.length}, minmax(0, 1fr))` }}
      >
        {stageKeys.map((stageKey, idx) => {
          const stageColors = STAGE_COLORS[stageKey];
          const stageDeals = dealsByStage[stageKey] ?? [];
          const stageNum = stageKey.replace("STAGE_", "");
          const isLast = idx === stageKeys.length - 1;

          return (
            <div
              key={stageKey}
              className={cn(
                "flex flex-col min-h-0",
                !isLast && "border-r border-border"
              )}
            >
              <div className={cn(
                "px-2.5 py-1.5 border-b border-border",
                stageColors?.bg || "bg-slate-100"
              )}>
                <div className="flex items-center gap-1.5">
                  <span className={cn(
                    "text-[9px] font-bold w-4 h-4 rounded-full flex items-center justify-center text-white",
                    stageColors?.bar || "bg-slate-400"
                  )}>
                    {stageNum}
                  </span>
                  <span className={cn("text-[11px] font-semibold truncate", stageColors?.text || "text-slate-700")}>
                    {STAGE_NAMES[stageKey] || stageKey}
                  </span>
                  <span className="text-[10px] text-text-muted ml-auto">{stageDeals.length}</span>
                </div>
              </div>

              <div className="flex-1 overflow-y-auto p-1.5 space-y-1.5 bg-white/50">
                {stageDeals.length === 0 ? (
                  <p className="text-[10px] text-text-muted text-center py-4 italic">No deals</p>
                ) : (
                  stageDeals.map((deal) => (
                    <DealCard key={deal.id} deal={deal} />
                  ))
                )}
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
}
