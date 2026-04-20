import { useMemo } from "react";
import { ChevronRight } from "lucide-react";
import { formatINR } from "../../lib/formatters";
import { PHASE_HEADER_COLORS } from "../../lib/constants";
import { cn } from "../../lib/utils";
import DealCard from "./DealCard";
import type { DealListItem } from "../../types";

interface PhaseColumnProps {
  phaseName: string;
  phaseKey: string;
  deals: DealListItem[];
  onExpand: () => void;
}

export default function PhaseColumn({ phaseName, phaseKey, deals, onExpand }: PhaseColumnProps) {
  const totalValue = useMemo(() => deals.reduce((sum, d) => sum + (d.estimatedMonthlyValue ?? 0), 0), [deals]);
  const colors = PHASE_HEADER_COLORS[phaseKey] ?? PHASE_HEADER_COLORS.EARLY;

  return (
    <div className="flex flex-col min-h-0 h-full">
      <button
        type="button"
        onClick={onExpand}
        className={cn(
          "flex items-center justify-between px-4 py-2 rounded-t-xl border transition-colors shadow-sm",
          colors.bg, colors.border,
          "hover:brightness-95"
        )}
      >
        <div className="flex items-center gap-2">
          <ChevronRight className={cn("h-4 w-4", colors.text)} />
          <h3 className={cn("text-sm font-bold", colors.text)}>{phaseName}</h3>
          <span className={cn("text-[10px] font-semibold px-2 py-0.5 rounded-full", colors.bg, colors.text, "border", colors.border)}>
            {deals.length}
          </span>
        </div>
        <span className="text-xs text-text-muted font-medium truncate ml-1">{formatINR(totalValue)}</span>
      </button>

      <div className={cn(
        "flex-1 overflow-y-auto border border-t-0 rounded-b-xl p-2 space-y-1.5 bg-slate-50/50",
        colors.border
      )}>
        {deals.length === 0 ? (
          <p className="text-xs text-text-muted text-center py-4 italic">No deals</p>
        ) : (
          deals.map((deal) => (
            <DealCard key={deal.id} deal={deal} />
          ))
        )}
      </div>
    </div>
  );
}
