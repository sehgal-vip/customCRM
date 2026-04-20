import { STAGE_NAMES, STAGE_COLORS } from "../../lib/constants";
import { cn } from "../../lib/utils";

const ALL_STAGES = [
  "STAGE_1", "STAGE_2", "STAGE_3", "STAGE_4",
  "STAGE_5", "STAGE_6", "STAGE_7", "STAGE_8",
] as const;

interface StageProgressBarProps {
  currentStage: string;
}

export default function StageProgressBar({ currentStage }: StageProgressBarProps) {
  const currentIdx = ALL_STAGES.indexOf(currentStage as typeof ALL_STAGES[number]);

  return (
    <div className="w-full">
      {/* Segments */}
      <div className="flex gap-1">
        {ALL_STAGES.map((stage, idx) => {
          const isCompleted = idx < currentIdx;
          const isCurrent = idx === currentIdx;
          const colors = STAGE_COLORS[stage];
          return (
            <div
              key={stage}
              className={cn(
                "h-2 flex-1 rounded-full transition-colors",
                (isCompleted || isCurrent) && colors?.bar,
                isCurrent && "animate-pulse",
                !isCompleted && !isCurrent && "bg-slate-200"
              )}
            />
          );
        })}
      </div>

      {/* Labels */}
      <div className="flex gap-1 mt-1.5">
        {ALL_STAGES.map((stage, idx) => {
          const isCurrent = idx === currentIdx;
          const colors = STAGE_COLORS[stage];
          const shortName = STAGE_NAMES[stage]?.split(" ").slice(0, 2).join(" ") ?? stage;
          return (
            <div key={stage} className="flex-1 text-center min-w-0">
              <span
                className={cn(
                  "text-[10px] leading-tight block",
                  isCurrent ? cn("font-semibold", colors?.text || "text-teal-700") : "text-text-muted",
                  !isCurrent && idx !== 0 && idx !== 7 && "hidden md:block"
                )}
              >
                {idx + 1}
              </span>
              <span
                className={cn(
                  "text-[9px] leading-tight block truncate",
                  isCurrent ? cn("font-medium", colors?.text || "text-teal-700") : "text-text-muted",
                  !isCurrent && idx !== 0 && idx !== 7 && "hidden md:block"
                )}
              >
                {shortName}
              </span>
            </div>
          );
        })}
      </div>
    </div>
  );
}
