import { useNavigate } from "react-router-dom";
import { Truck } from "lucide-react";
import type { DealListItem } from "../../types";
import { STAGE_COLORS } from "../../lib/constants";
import { formatINR } from "../../lib/formatters";
import { cn, isOverdue, isStale } from "../../lib/utils";
import StageBadge from "../shared/StageBadge";
import SubStatusBadge from "../shared/SubStatusBadge";

interface DealCardProps {
  deal: DealListItem;
  onClick?: () => void;
  compact?: boolean;
}

export default function DealCard({ deal, onClick, compact }: DealCardProps) {
  const navigate = useNavigate();

  const isCompleted = deal.status === "COMPLETED";
  const isArchived = deal.status === "ARCHIVED";
  const isInactive = isCompleted || isArchived;

  const overdue = !isInactive && isOverdue(deal.nextActionEta);
  const stale = !isInactive && !overdue && isStale(deal.currentStage, deal.daysInStage);

  const borderColor = isCompleted
    ? "border-l-emerald-500"
    : isArchived
      ? "border-l-slate-400"
      : overdue
        ? "border-l-red-500"
        : stale
          ? "border-l-amber-500"
          : STAGE_COLORS[deal.currentStage]?.borderL || "border-l-slate-300";

  const handleClick = () => {
    if (onClick) {
      onClick();
    } else {
      navigate(`/board/${deal.id}`);
    }
  };

  return (
    <button
      type="button"
      onClick={handleClick}
      className={cn(
        "text-left bg-white rounded-xl border border-border",
        "border-l-4 p-2.5 shadow-[0_1px_3px_rgba(0,0,0,0.1),0_1px_2px_rgba(0,0,0,0.06)]",
        "hover:shadow-[0_10px_15px_-3px_rgba(0,0,0,0.1),0_4px_6px_-4px_rgba(0,0,0,0.05)] hover:-translate-y-0.5 hover:scale-[1.01]",
        "transition-all duration-200 cursor-pointer",
        borderColor,
        isInactive && "opacity-60",
        compact ? "w-[200px] min-w-[200px] shrink-0" : "w-full"
      )}
    >
      {isInactive && (
        <span
          className={cn(
            "inline-block px-1.5 py-0.5 text-[10px] font-semibold rounded mb-1",
            isCompleted ? "bg-emerald-100 text-emerald-700" : "bg-slate-100 text-slate-600"
          )}
        >
          {isCompleted ? "Won" : "Lost"}
        </span>
      )}
      <p className="font-semibold text-sm text-text-primary truncate">
        {deal.operatorName}
      </p>

      <p className="text-xs text-text-muted truncate mt-0.5">{deal.name}</p>

      <div className="flex items-center gap-2.5 mt-1.5 text-xs text-text-secondary">
        <span className="inline-flex items-center gap-1">
          <Truck className="h-3.5 w-3.5 text-text-muted" />
          {deal.fleetSize ?? "—"}
        </span>
        <span className="font-medium">
          {formatINR(deal.estimatedMonthlyValue)}
        </span>
      </div>

      <div className="flex flex-wrap items-center gap-1 mt-1.5">
        <StageBadge stage={deal.currentStage} />
        {deal.subStatus && <SubStatusBadge subStatus={deal.subStatus} />}
      </div>

      <div className="flex items-center justify-between mt-1.5">
        <span
          className={cn(
            "text-[11px]",
            overdue ? "text-danger font-medium" : stale ? "text-warning font-medium" : "text-text-muted"
          )}
        >
          {deal.daysInStage}d in stage
        </span>
        <span className="text-[11px] text-text-muted truncate max-w-[100px]">
          {deal.agentName}
        </span>
      </div>
    </button>
  );
}
