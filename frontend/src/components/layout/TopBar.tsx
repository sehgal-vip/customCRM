import { Plus } from "lucide-react";
import GlobalSearch from "./GlobalSearch";
import NotificationPanel from "../notifications/NotificationPanel";

interface TopBarProps {
  title: string;
  onNewDeal?: () => void;
}

export default function TopBar({ title, onNewDeal }: TopBarProps) {
  return (
    <header className="sticky top-0 z-30 bg-surface border-b border-border px-4 md:px-6 h-14 flex items-center justify-between shrink-0">
      <h1 className="text-lg font-semibold text-text-primary">{title}</h1>

      <div className="flex items-center gap-2">
        {/* Global Search — desktop */}
        <div className="hidden md:block">
          <GlobalSearch />
        </div>

        {/* Notification bell with panel */}
        <NotificationPanel />

        {/* New Deal button — desktop only */}
        {onNewDeal && (
          <button
            onClick={onNewDeal}
            className="hidden md:inline-flex items-center gap-1.5 px-4 py-2 bg-primary hover:bg-primary-hover text-white text-sm font-medium rounded-lg shadow-sm transition-colors"
          >
            <Plus className="w-4 h-4" />
            New Deal
          </button>
        )}
      </div>
    </header>
  );
}
