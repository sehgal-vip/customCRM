import { SUB_STATUS_COLORS, SUB_STATUS_LABELS } from "../../lib/constants";
import { cn } from "../../lib/utils";

interface SubStatusBadgeProps {
  subStatus: string;
}

export default function SubStatusBadge({ subStatus }: SubStatusBadgeProps) {
  const colors = SUB_STATUS_COLORS[subStatus];
  const label = SUB_STATUS_LABELS[subStatus] || subStatus;

  if (!colors) return null;

  return (
    <span
      className={cn(
        "inline-flex items-center px-2 py-0.5 rounded-full text-xs font-medium",
        colors.bg,
        colors.text
      )}
    >
      {label}
    </span>
  );
}
