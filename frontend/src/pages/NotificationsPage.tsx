import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { Loader2, X, Check, BellOff } from "lucide-react";
import { notificationsApi } from "../services/apiEndpoints";
import { cn } from "../lib/utils";
import { timeAgo } from "../lib/formatters";
import type { Notification } from "../types";

const PRIORITY_DOT: Record<string, string> = {
  HIGH: "bg-red-500",
  MEDIUM: "bg-amber-500",
  LOW: "bg-slate-400",
};

export default function NotificationsPage() {
  const [showCleared, setShowCleared] = useState(false);
  const [page, setPage] = useState(0);
  const navigate = useNavigate();
  const queryClient = useQueryClient();

  const { data: notifPage, isLoading } = useQuery({
    queryKey: ["notifications-page", showCleared, page],
    queryFn: () =>
      notificationsApi.list({
        includeCleared: showCleared,
        page,
        size: 20,
      }),
    refetchInterval: 30000,
  });

  const notifications: Notification[] = notifPage?.content ?? [];
  const totalPages = notifPage?.totalPages ?? 0;

  const clearMutation = useMutation({
    mutationFn: (id: number) => notificationsApi.clear(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["notifications-page"] });
      queryClient.invalidateQueries({ queryKey: ["notification-count"] });
    },
  });

  const clearAllMutation = useMutation({
    mutationFn: () => notificationsApi.clearAll(),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["notifications-page"] });
      queryClient.invalidateQueries({ queryKey: ["notification-count"] });
    },
  });

  const handleNotificationClick = (notif: Notification) => {
    if (notif.dealId) {
      navigate(`/board/${notif.dealId}`);
    }
  };

  return (
    <div className="space-y-4">
      {/* Header */}
      <div className="flex items-center justify-between">
        <h1 className="text-xl font-semibold text-text-primary">
          Notifications
        </h1>
        <div className="flex items-center gap-3">
          <label className="flex items-center gap-2 text-xs text-text-muted cursor-pointer">
            <input
              type="checkbox"
              checked={showCleared}
              onChange={(e) => {
                setShowCleared(e.target.checked);
                setPage(0);
              }}
              className="rounded border-border text-primary focus:ring-primary/40"
            />
            Show Cleared
          </label>
          <button
            type="button"
            onClick={() => clearAllMutation.mutate()}
            disabled={clearAllMutation.isPending}
            className="px-3 py-1.5 text-xs font-medium rounded-lg bg-white border border-border text-text-secondary hover:bg-slate-50 transition-colors disabled:opacity-50"
          >
            Clear All
          </button>
        </div>
      </div>

      {/* Loading */}
      {isLoading && (
        <div className="flex items-center justify-center py-16">
          <Loader2 className="h-8 w-8 animate-spin text-primary" />
        </div>
      )}

      {/* Empty state */}
      {!isLoading && notifications.length === 0 && (
        <div className="flex flex-col items-center justify-center py-16">
          <BellOff className="h-10 w-10 text-text-muted mb-3" />
          <p className="text-sm font-medium text-text-primary">
            No notifications
          </p>
          <p className="text-xs text-text-muted mt-1">
            You are all caught up.
          </p>
        </div>
      )}

      {/* Notification cards */}
      <div className="space-y-2">
        {notifications.map((notif) => (
          <div
            key={notif.id}
            onClick={() => handleNotificationClick(notif)}
            className={cn(
              "bg-white rounded-xl border border-border p-4 shadow-[0_1px_2px_rgba(0,0,0,0.05)] flex items-start gap-3 cursor-pointer hover:shadow-md transition-shadow",
              notif.cleared && "opacity-50"
            )}
          >
            {/* Priority dot */}
            <div
              className={cn(
                "w-2.5 h-2.5 rounded-full shrink-0 mt-1",
                PRIORITY_DOT[notif.priority] ?? "bg-slate-400"
              )}
            />

            {/* Content */}
            <div className="flex-1 min-w-0">
              <p className="text-sm font-semibold text-text-primary">
                {notif.title}
              </p>
              <p className="text-xs text-text-muted mt-0.5">
                {notif.content}
              </p>
              <p className="text-[10px] text-text-muted mt-1">
                {timeAgo(notif.createdAt)}
              </p>
            </div>

            {/* Clear button */}
            {!notif.cleared ? (
              <button
                type="button"
                onClick={(e) => {
                  e.stopPropagation();
                  clearMutation.mutate(notif.id);
                }}
                className="shrink-0 p-1.5 rounded-lg hover:bg-slate-100 text-text-muted transition-colors"
              >
                <X className="h-4 w-4" />
              </button>
            ) : (
              <Check className="h-4 w-4 text-text-muted shrink-0 mt-1" />
            )}
          </div>
        ))}
      </div>

      {/* Pagination */}
      {totalPages > 1 && (
        <div className="flex items-center justify-center gap-2 pt-4">
          <button
            type="button"
            disabled={page === 0}
            onClick={() => setPage((p) => p - 1)}
            className="px-3 py-1.5 text-xs rounded-lg border border-border text-text-secondary hover:bg-slate-50 disabled:opacity-50 disabled:cursor-not-allowed"
          >
            Previous
          </button>
          <span className="text-xs text-text-muted">
            Page {page + 1} of {totalPages}
          </span>
          <button
            type="button"
            disabled={page >= totalPages - 1}
            onClick={() => setPage((p) => p + 1)}
            className="px-3 py-1.5 text-xs rounded-lg border border-border text-text-secondary hover:bg-slate-50 disabled:opacity-50 disabled:cursor-not-allowed"
          >
            Next
          </button>
        </div>
      )}
    </div>
  );
}
