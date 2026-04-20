import type { DealListItem } from "../../types";
import { cn } from "../../lib/utils";
import DealCard from "./DealCard";

interface StageRowProps {
  stageKey: string;
  stageName: string;
  deals: DealListItem[];
}

export default function StageRow({ stageKey, stageName, deals }: StageRowProps) {
  return (
    <div className="py-2">
      {/* Stage label */}
      <div className="flex items-center gap-2 mb-2 px-1">
        <h3 className="text-sm font-semibold text-text-secondary">
          {stageKey.replace("STAGE_", "")}. {stageName}
        </h3>
        <span className="text-xs text-text-muted">({deals.length})</span>
      </div>

      {/* Cards */}
      {deals.length === 0 ? (
        <p className="text-xs text-text-muted italic px-1 py-3">
          No deals at this stage
        </p>
      ) : (
        <div className={cn(
          "grid grid-flow-col gap-2 overflow-x-auto pb-2 px-1 auto-cols-[200px]",
          deals.length > 1 ? "grid-rows-2" : "grid-rows-1"
        )}>
          {deals.map((deal) => (
            <DealCard key={deal.id} deal={deal} compact />
          ))}
        </div>
      )}
    </div>
  );
}
