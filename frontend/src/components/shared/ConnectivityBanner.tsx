import { useState, useEffect } from "react";
import { WifiOff } from "lucide-react";
import { cn } from "../../lib/utils";

export default function ConnectivityBanner() {
  const [isOffline, setIsOffline] = useState(!navigator.onLine);
  const [showBanner, setShowBanner] = useState(!navigator.onLine);

  useEffect(() => {
    function handleOffline() {
      setIsOffline(true);
      setShowBanner(true);
    }

    function handleOnline() {
      setIsOffline(false);
      // Auto-hide after 2 seconds of reconnection
      setTimeout(() => setShowBanner(false), 2000);
    }

    window.addEventListener("offline", handleOffline);
    window.addEventListener("online", handleOnline);

    return () => {
      window.removeEventListener("offline", handleOffline);
      window.removeEventListener("online", handleOnline);
    };
  }, []);

  if (!showBanner) return null;

  return (
    <div
      className={cn(
        "w-full px-4 py-1.5 flex items-center justify-center gap-2 text-xs font-medium transition-colors",
        isOffline
          ? "bg-amber-100 text-amber-800"
          : "bg-emerald-100 text-emerald-800"
      )}
    >
      {isOffline ? (
        <>
          <WifiOff className="h-3.5 w-3.5" />
          You&apos;re offline -- changes will sync when connected
        </>
      ) : (
        "Back online"
      )}
    </div>
  );
}
