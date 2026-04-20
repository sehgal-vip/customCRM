import {
  createContext,
  useContext,
  useState,
  useCallback,
  useEffect,
  type ReactNode,
} from "react";
import type { User } from "../types";
import { authApi } from "../services/apiEndpoints";

interface AuthContextType {
  user: User | null;
  token: string | null;
  isAuthenticated: boolean;
  isManager: boolean;
  login: (userId: number) => Promise<void>;
  logout: () => void;
}

const AuthContext = createContext<AuthContextType | null>(null);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<User | null>(() => {
    const stored = localStorage.getItem("user");
    return stored ? JSON.parse(stored) : null;
  });
  const [token, setToken] = useState<string | null>(() =>
    localStorage.getItem("token")
  );

  const isAuthenticated = !!token && !!user;
  const isManager = user?.role === "MANAGER";

  // Verify session on mount only (not on every token change)
  useEffect(() => {
    const storedToken = localStorage.getItem("token");
    if (storedToken) {
      authApi.me().then(setUser).catch(() => {
        localStorage.removeItem("token");
        localStorage.removeItem("user");
        setToken(null);
        setUser(null);
      });
    }
  }, []);

  const login = useCallback(async (userId: number) => {
    const response = await authApi.devLogin(userId);
    localStorage.setItem("token", response.token);
    localStorage.setItem("user", JSON.stringify(response.user));
    setToken(response.token);
    setUser(response.user);
  }, []);

  const logout = useCallback(() => {
    localStorage.removeItem("token");
    localStorage.removeItem("user");
    setToken(null);
    setUser(null);
  }, []);

  return (
    <AuthContext.Provider
      value={{ user, token, isAuthenticated, isManager, login, logout }}
    >
      {children}
    </AuthContext.Provider>
  );
}

// eslint-disable-next-line react-refresh/only-export-components
export function useAuth(): AuthContextType {
  const context = useContext(AuthContext);
  if (!context) throw new Error("useAuth must be used within AuthProvider");
  return context;
}
