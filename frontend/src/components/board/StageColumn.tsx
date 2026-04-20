import { STAGE_NAMES, STAGE_COLORS } from "../../lib/constants";
import { cn } from "../../lib/utils";
import DealCard from "./DealCard";
import type { DealListItem } from "../../types";

interface StageColumnProps {
  stageKey: string;
  deals: DealListItem[];
}

export default function StageColumn({ stageKey, deals }: StageColumnProps) {
  const colors = STAGE_COLORS[stageKey];
  const stageNum = stageKey.replace("STAGE_", "");
  const stageName = STAGE_NAMES[stageKey] || stageKey;

  return (
    <div className="flex flex-col min-h-0">
      <div className={cn(
        "px-3 py-2.5 rounded-t-xl border",
        colors?.bg || "bg-slate-100",
        "border-border"
      )}>
        <div className="flex items-center gap-2">
          <span className={cn(
            "text-[10px] font-bold w-5 h-5 rounded-full flex items-center justify-center",
            colors?.bar || "bg-slate-400",
            "text-white"
          )}>
            {stageNum}
          </span>
          <h4 className={cn("text-xs font-semibold truncate", colors?.text || "text-slate-700")}>
            {stageName}
          </h4>
          <span className="text-[10px] text-text-muted ml-auto shrink-0">
            {deals.length}
          </span>
        </div>
      </div>

      <div className="flex-1 overflow-y-auto border border-t-0 rounded-b-xl p-1.5 space-y-1.5 bg-white/50">
        {deals.length === 0 ? (
          <p className="text-[10px] text-text-muted text-center py-3 italic">No deals</p>
        ) : (
          deals.map((deal) => (
            <DealCard key={deal.id} deal={deal} />
          ))
        )}
      </div>
    </div>
  );
}
