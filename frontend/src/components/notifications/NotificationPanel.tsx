import { useState, useRef, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { Bell, X, Loader2, Check } from "lucide-react";
import { notificationsApi } from "../../services/apiEndpoints";
import { cn } from "../../lib/utils";
import { timeAgo } from "../../lib/formatters";
import type { Notification } from "../../types";

const PRIORITY_DOT: Record<string, string> = {
  HIGH: "bg-red-500",
  MEDIUM: "bg-amber-500",
  LOW: "bg-slate-400",
};

export default function NotificationPanel() {
  const [isOpen, setIsOpen] = useState(false);
  const [showCleared, setShowCleared] = useState(false);
  const panelRef = useRef<HTMLDivElement>(null);
  const navigate = useNavigate();
  const queryClient = useQueryClient();

  // Unread count — polls every 30s
  const { data: countData } = useQuery({
    queryKey: ["notification-count"],
    queryFn: () => notificationsApi.count(),
    refetchInterval: 30000,
  });
  const unreadCount = countData?.count ?? 0;

  // Notifications list
  const { data: notifPage, isLoading } = useQuery({
    queryKey: ["notifications", showCleared],
    queryFn: () =>
      notificationsApi.list({
        includeCleared: showCleared,
        size: 20,
      }),
    enabled: isOpen,
    refetchInterval: 30000,
  });
  const notifications: Notification[] = notifPage?.content ?? [];

  // Clear single
  const clearMutation = useMutation({
    mutationFn: (id: number) => notificationsApi.clear(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["notifications"] });
      queryClient.invalidateQueries({ queryKey: ["notification-count"] });
    },
  });

  // Clear all
  const clearAllMutation = useMutation({
    mutationFn: () => notificationsApi.clearAll(),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["notifications"] });
      queryClient.invalidateQueries({ queryKey: ["notification-count"] });
    },
  });

  // Close on click outside
  useEffect(() => {
    const handleClick = (e: MouseEvent) => {
      if (panelRef.current && !panelRef.current.contains(e.target as Node)) {
        setIsOpen(false);
      }
    };
    if (isOpen) {
      document.addEventListener("mousedown", handleClick);
    }
    return () => document.removeEventListener("mousedown", handleClick);
  }, [isOpen]);

  const handleNotificationClick = (notif: Notification) => {
    if (notif.dealId) {
      navigate(`/board/${notif.dealId}`);
      setIsOpen(false);
    }
  };

  return (
    <div ref={panelRef} className="relative">
      {/* Bell button */}
      <button
        type="button"
        onClick={() => setIsOpen(!isOpen)}
        className="relative p-2 rounded-lg text-text-secondary hover:bg-background hover:text-text-primary transition-colors"
      >
        <Bell className="w-5 h-5" />
        {unreadCount > 0 && (
          <span className="absolute -top-0.5 -right-0.5 min-w-[18px] h-[18px] flex items-center justify-center rounded-full bg-primary text-white text-[10px] font-bold px-1">
            {unreadCount > 99 ? "99+" : unreadCount}
          </span>
        )}
      </button>

      {/* Dropdown panel */}
      {isOpen && (
        <div className="absolute right-0 top-full mt-2 w-[400px] max-h-[500px] bg-white rounded-xl border border-border shadow-[0_10px_15px_rgba(0,0,0,0.1)] flex flex-col z-50">
          {/* Panel header */}
          <div className="flex items-center justify-between px-4 py-3 border-b border-border">
            <h3 className="text-sm font-semibold text-text-primary">
              Notifications
            </h3>
            <button
              type="button"
              onClick={() => clearAllMutation.mutate()}
              disabled={clearAllMutation.isPending}
              className="text-xs text-primary hover:underline disabled:opacity-50"
            >
              {clearAllMutation.isPending ? "Clearing..." : "Clear All"}
            </button>
          </div>

          {/* Notification list */}
          <div className="flex-1 overflow-y-auto">
            {isLoading && (
              <div className="flex items-center justify-center py-8">
                <Loader2 className="h-5 w-5 animate-spin text-primary" />
              </div>
            )}

            {!isLoading && notifications.length === 0 && (
              <div className="py-8 text-center text-sm text-text-muted">
                No notifications
              </div>
            )}

            {notifications.map((notif) => (
              <div
                key={notif.id}
                className={cn(
                  "flex items-start gap-3 px-4 py-3 border-b border-border last:border-0 hover:bg-slate-50 transition-colors cursor-pointer",
                  notif.cleared && "opacity-50"
                )}
                onClick={() => handleNotificationClick(notif)}
              >
                {/* Priority dot */}
                <div
                  className={cn(
                    "w-2 h-2 rounded-full shrink-0 mt-1.5",
                    PRIORITY_DOT[notif.priority] ?? "bg-slate-400"
                  )}
                />

                {/* Content */}
                <div className="flex-1 min-w-0">
                  <p className="text-sm font-semibold text-text-primary truncate">
                    {notif.title}
                  </p>
                  <p className="text-xs text-text-muted truncate mt-0.5">
                    {notif.content}
                  </p>
                  <p className="text-[10px] text-text-muted mt-1">
                    {timeAgo(notif.createdAt)}
                  </p>
                </div>

                {/* Clear button */}
                {!notif.cleared && (
                  <button
                    type="button"
                    onClick={(e) => {
                      e.stopPropagation();
                      clearMutation.mutate(notif.id);
                    }}
                    className="shrink-0 p-1 rounded hover:bg-slate-200 text-text-muted transition-colors"
                  >
                    <X className="h-3.5 w-3.5" />
                  </button>
                )}
                {notif.cleared && (
                  <Check className="h-3.5 w-3.5 text-text-muted shrink-0 mt-1" />
                )}
              </div>
            ))}
          </div>

          {/* Footer */}
          <div className="flex items-center justify-between px-4 py-2 border-t border-border">
            <label className="flex items-center gap-2 text-xs text-text-muted cursor-pointer">
              <input
                type="checkbox"
                checked={showCleared}
                onChange={(e) => setShowCleared(e.target.checked)}
                className="rounded border-border text-primary focus:ring-primary/40"
              />
              Show Cleared
            </label>
            <button
              type="button"
              onClick={() => {
                navigate("/notifications");
                setIsOpen(false);
              }}
              className="text-xs text-primary hover:underline"
            >
              View All
            </button>
          </div>
        </div>
      )}
    </div>
  );
}
