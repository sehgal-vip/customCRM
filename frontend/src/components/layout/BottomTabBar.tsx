import { useState } from "react";
import { NavLink, useNavigate } from "react-router-dom";
import { LayoutGrid, CheckSquare, Bell, MoreHorizontal, Building2, BarChart3, Settings, X } from "lucide-react";
import { cn } from "../../lib/utils";
import { useAuth } from "../../context/AuthContext";

const tabs = [
  { to: "/board", label: "Board", icon: LayoutGrid },
  { to: "/tasks", label: "Tasks", icon: CheckSquare },
  { to: "/notifications", label: "Alerts", icon: Bell },
];

export default function BottomTabBar() {
  const [moreOpen, setMoreOpen] = useState(false);
  const { isManager } = useAuth();
  const navigate = useNavigate();

  const moreItems = [
    { to: "/operators", label: "Operators", icon: Building2 },
    { to: "/dashboard", label: "Dashboard", icon: BarChart3 },
    ...(isManager ? [{ to: "/admin", label: "Admin Settings", icon: Settings }] : []),
  ];

  return (
    <nav className="md:hidden fixed bottom-0 inset-x-0 z-40 bg-surface border-t border-border">
      {moreOpen && (
        <>
          <div className="fixed inset-0 z-30" onClick={() => setMoreOpen(false)} />
          <div className="absolute bottom-full left-0 right-0 z-40 bg-surface border-t border-border rounded-t-xl shadow-[0_-4px_12px_rgba(0,0,0,0.1)] animate-[dropdown-enter_0.15s_ease-out]">
            <div className="px-4 py-3 flex items-center justify-between border-b border-border">
              <span className="text-sm font-semibold text-text-primary">More</span>
              <button type="button" onClick={() => setMoreOpen(false)} className="p-1 rounded-lg text-text-muted hover:bg-slate-100">
                <X className="w-4 h-4" />
              </button>
            </div>
            <div className="py-2">
              {moreItems.map((item) => (
                <button
                  key={item.to}
                  type="button"
                  onClick={() => { navigate(item.to); setMoreOpen(false); }}
                  className="w-full flex items-center gap-3 px-4 py-3 text-sm text-text-primary hover:bg-slate-50 transition-colors"
                >
                  <item.icon className="w-5 h-5 text-text-muted" />
                  {item.label}
                </button>
              ))}
            </div>
          </div>
        </>
      )}

      <div className="flex items-center justify-around h-14">
        {tabs.map((tab) => (
          <NavLink
            key={tab.to}
            to={tab.to}
            onClick={() => setMoreOpen(false)}
            className={({ isActive }) =>
              cn(
                "flex flex-col items-center gap-0.5 py-1 px-3 text-[10px] font-medium transition-colors",
                isActive ? "text-primary" : "text-text-muted"
              )
            }
          >
            <tab.icon className="w-5 h-5" />
            {tab.label}
          </NavLink>
        ))}
        <button
          type="button"
          onClick={() => setMoreOpen(!moreOpen)}
          className={cn(
            "flex flex-col items-center gap-0.5 py-1 px-3 text-[10px] font-medium transition-colors",
            moreOpen ? "text-primary" : "text-text-muted"
          )}
        >
          <MoreHorizontal className="w-5 h-5" />
          More
        </button>
      </div>
    </nav>
  );
}
