import { NavLink, useNavigate } from "react-router-dom";
import {
  LayoutGrid,
  CheckSquare,
  Building2,
  BarChart3,
  Settings,
  LogOut,
} from "lucide-react";
import { useAuth } from "../../context/AuthContext";
import { cn } from "../../lib/utils";

const navItems = [
  { to: "/board", label: "Board", icon: LayoutGrid },
  { to: "/tasks", label: "Tasks", icon: CheckSquare },
  { to: "/operators", label: "Operators", icon: Building2 },
  { to: "/dashboard", label: "Dashboard", icon: BarChart3 },
];

const adminItems = [
  { to: "/admin", label: "Admin Settings", icon: Settings },
];

export default function Sidebar() {
  const { user, isManager, logout } = useAuth();
  const navigate = useNavigate();

  const initials = user?.name
    .split(" ")
    .map((n) => n[0])
    .join("")
    .slice(0, 2);

  return (
    <aside className="hidden md:flex flex-col w-60 bg-surface border-r border-border h-screen sticky top-0 shrink-0">
      {/* Logo */}
      <button
        type="button"
        onClick={() => navigate("/board")}
        className="w-full text-left px-5 py-5 border-b border-border cursor-pointer hover:bg-background transition-all duration-200"
      >
        <h1 className="text-lg font-bold text-text-primary tracking-tight">
          TurnoCRM
        </h1>
        <p className="text-[10px] text-text-muted uppercase tracking-[0.15em] font-medium">
          Electric Bus Leasing
        </p>
      </button>

      {/* Navigation */}
      <nav className="flex-1 py-4 px-3 space-y-1 overflow-y-auto">
        <p className="px-2 text-[10px] font-semibold text-text-muted uppercase tracking-wider mb-2">
          Pipeline
        </p>
        {navItems.map((item) => (
          <NavLink
            key={item.to}
            to={item.to}
            className={({ isActive }) =>
              cn(
                "flex items-center gap-3 px-3 py-2 rounded-lg text-sm font-medium transition-all duration-200",
                isActive
                  ? "bg-primary-light text-primary-hover border-l-[3px] border-primary shadow-sm"
                  : "text-text-secondary hover:bg-background hover:text-text-primary"
              )
            }
          >
            <item.icon className="w-[18px] h-[18px]" />
            {item.label}
          </NavLink>
        ))}

        {isManager && (
          <>
            <p className="px-2 text-[10px] font-semibold text-text-muted uppercase tracking-wider mt-6 mb-2">
              Admin
            </p>
            {adminItems.map((item) => (
              <NavLink
                key={item.to}
                to={item.to}
                className={({ isActive }) =>
                  cn(
                    "flex items-center gap-3 px-3 py-2 rounded-lg text-sm font-medium transition-all duration-200",
                    isActive
                      ? "bg-primary-light text-primary-hover border-l-[3px] border-primary shadow-sm"
                      : "text-text-secondary hover:bg-background hover:text-text-primary"
                  )
                }
              >
                <item.icon className="w-[18px] h-[18px]" />
                {item.label}
              </NavLink>
            ))}
          </>
        )}
      </nav>

      {/* User section */}
      <div className="px-4 py-4 border-t border-border">
        <div className="flex items-center gap-3">
          <div className="w-8 h-8 rounded-full bg-primary flex items-center justify-center text-white text-xs font-semibold shrink-0">
            {initials}
          </div>
          <div className="min-w-0 flex-1">
            <p className="text-sm font-medium text-text-primary truncate">
              {user?.name}
            </p>
            <span
              className={cn(
                "text-[10px] font-medium px-1.5 py-0.5 rounded-full",
                isManager
                  ? "bg-primary-light text-primary"
                  : "bg-background text-text-secondary"
              )}
            >
              {user?.role}
            </span>
          </div>
          <button
            onClick={logout}
            className="p-1.5 rounded-lg text-text-muted hover:text-danger hover:bg-danger/10 transition-colors"
            title="Logout"
          >
            <LogOut className="w-4 h-4" />
          </button>
        </div>
      </div>
    </aside>
  );
}
