import { STAGE_NAMES, STAGE_COLORS } from "../../lib/constants";
import { cn } from "../../lib/utils";

interface StageBadgeProps {
  stage: string;
}

export default function StageBadge({ stage }: StageBadgeProps) {
  const colors = STAGE_COLORS[stage];
  return (
    <span className={cn(
      "inline-flex items-center px-2 py-0.5 rounded-full text-xs font-medium shadow-sm",
      colors ? cn(colors.bg, colors.text) : "bg-slate-100 text-slate-600"
    )}>
      {STAGE_NAMES[stage] || stage}
    </span>
  );
}
