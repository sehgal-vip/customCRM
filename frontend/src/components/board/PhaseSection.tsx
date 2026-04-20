import { useState } from "react";
import { ChevronDown, ChevronRight } from "lucide-react";
import type { DealListItem } from "../../types";
import { STAGE_NAMES } from "../../lib/constants";
import { formatINR } from "../../lib/formatters";
import { cn } from "../../lib/utils";
import DealCard from "./DealCard";
import StageRow from "./StageRow";

interface PhaseSectionProps {
  phaseName: string;
  stageKeys: string[];
  deals: DealListItem[];
  defaultExpanded?: boolean;
}

export default function PhaseSection({
  phaseName,
  stageKeys,
  deals,
  defaultExpanded = true,
}: PhaseSectionProps) {
  const [expanded, setExpanded] = useState(defaultExpanded);

  const totalValue = deals.reduce(
    (sum, d) => sum + (d.estimatedMonthlyValue ?? 0),
    0
  );

  const dealsByStage = stageKeys.reduce<Record<string, DealListItem[]>>(
    (acc, key) => {
      acc[key] = deals.filter((d) => d.currentStage === key);
      return acc;
    },
    {}
  );

  return (
    <section className="bg-white rounded-xl border border-border shadow-[0_1px_2px_rgba(0,0,0,0.05)]">
      {/* Header */}
      <button
        type="button"
        onClick={() => setExpanded((v) => !v)}
        className={cn(
          "w-full flex items-center gap-3 px-5 py-4 text-left",
          "hover:bg-slate-50 transition-colors rounded-xl",
          expanded && "rounded-b-none border-b border-border"
        )}
      >
        {expanded ? (
          <ChevronDown className="h-5 w-5 text-text-muted shrink-0" />
        ) : (
          <ChevronRight className="h-5 w-5 text-text-muted shrink-0" />
        )}

        <h2 className="text-base font-semibold text-text-primary">
          {phaseName}
        </h2>

        {/* Deal count badge */}
        <span className="inline-flex items-center justify-center min-w-[22px] h-[22px] px-1.5 rounded-full text-xs font-semibold bg-primary text-white">
          {deals.length}
        </span>

        {/* Pipeline value */}
        <span className="ml-auto text-sm font-medium text-text-secondary truncate">
          {formatINR(totalValue)}
        </span>
      </button>

      {/* Collapsed: show all phase deals in horizontal scroll */}
      {!expanded && deals.length > 0 && (
        <div className="px-5 pb-4 pt-2">
          <div className={cn(
            "grid grid-flow-col gap-2 overflow-x-auto pb-2 auto-cols-[200px]",
            deals.length > 1 ? "grid-rows-2" : "grid-rows-1"
          )}>
            {deals.map((deal) => (
              <DealCard key={deal.id} deal={deal} compact />
            ))}
          </div>
        </div>
      )}

      {/* Expanded: show deals grouped by stage */}
      {expanded && (
        <div className="px-5 pb-4 pt-2 space-y-2">
          {stageKeys.map((key) => (
            <StageRow
              key={key}
              stageKey={key}
              stageName={STAGE_NAMES[key] || key}
              deals={dealsByStage[key] || []}
            />
          ))}
        </div>
      )}
    </section>
  );
}
