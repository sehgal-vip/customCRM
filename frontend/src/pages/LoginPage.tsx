import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import { authApi } from "../services/apiEndpoints";
import type { User } from "../types";

export default function LoginPage() {
  const { login, isAuthenticated } = useAuth();
  const navigate = useNavigate();
  const [users, setUsers] = useState<User[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  useEffect(() => {
    if (isAuthenticated) {
      navigate("/board", { replace: true });
    }
  }, [isAuthenticated, navigate]);

  useEffect(() => {
    authApi.listUsers().then((res) => setUsers(Array.isArray(res) ? res : [])).catch(() => setError("Failed to load users"));
  }, []);

  const handleLogin = async (userId: number) => {
    setLoading(true);
    setError("");
    try {
      await login(userId);
      navigate("/board", { replace: true });
    } catch {
      setError("Login failed. Is the backend running?");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-primary to-primary-hover p-4">
      <div className="bg-surface rounded-2xl shadow-2xl p-8 w-full max-w-md">
        {/* Header */}
        <div className="text-center mb-8">
          <h1 className="text-3xl font-bold text-text-primary tracking-tight">
            TurnoCRM
          </h1>
          <p className="text-sm text-text-muted mt-1 uppercase tracking-widest">
            Electric Bus Leasing CRM
          </p>
        </div>

        {/* Dev mode badge */}
        <div className="flex justify-center mb-6">
          <span className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full bg-warning/10 text-warning text-xs font-medium border border-warning/20">
            <span className="w-1.5 h-1.5 rounded-full bg-warning" />
            Dev Mode
          </span>
        </div>

        {/* User selection */}
        <div>
          <label className="block text-xs font-medium text-text-muted uppercase tracking-wider mb-2">
            Select User
          </label>
          <div className="space-y-2">
            {users.map((u) => (
              <button
                key={u.id}
                onClick={() => handleLogin(u.id)}
                disabled={loading}
                className="w-full flex items-center gap-3 p-3 rounded-lg border border-border hover:border-primary hover:bg-primary-light/50 transition-colors text-left disabled:opacity-50 disabled:cursor-not-allowed"
              >
                <div className="w-9 h-9 rounded-full bg-primary flex items-center justify-center text-white text-sm font-semibold shrink-0">
                  {u.name
                    .split(" ")
                    .map((n) => n[0])
                    .join("")
                    .slice(0, 2)}
                </div>
                <div className="min-w-0 flex-1">
                  <div className="text-sm font-semibold text-text-primary truncate">
                    {u.name}
                  </div>
                </div>
                <span
                  className={`text-xs font-medium px-2 py-0.5 rounded-full ${
                    u.role === "MANAGER"
                      ? "bg-primary-light text-primary"
                      : "bg-background text-text-secondary"
                  }`}
                >
                  {u.role}
                </span>
              </button>
            ))}
          </div>

          {users.length === 0 && !error && (
            <p className="text-center text-sm text-text-muted py-4">
              Loading users...
            </p>
          )}

          {error && (
            <p className="text-center text-sm text-danger mt-4">{error}</p>
          )}
        </div>

        {/* Footer */}
        <p className="text-center text-xs text-text-muted mt-6">
          Restricted to Turno employees
        </p>
      </div>
    </div>
  );
}
