import { Navigate, Outlet, useLocation } from "react-router-dom";
import { useAuth } from "../../context/AuthContext";
import Sidebar from "./Sidebar";
import TopBar from "./TopBar";
import BottomTabBar from "./BottomTabBar";
import ConnectivityBanner from "../shared/ConnectivityBanner";

const pageTitles: Record<string, string> = {
  "/board": "Pipeline Board",
  "/tasks": "Tasks",
  "/operators": "Operators",
  "/dashboard": "Dashboard",
  "/admin": "Admin Settings",
};

function getPageTitle(pathname: string): string {
  if (pageTitles[pathname]) return pageTitles[pathname];
  if (pathname.startsWith("/board/")) return "Deal Detail";
  if (pathname.startsWith("/operators/")) return "Operator Detail";
  return "TurnoCRM";
}

export default function AuthenticatedLayout() {
  const { isAuthenticated } = useAuth();
  const location = useLocation();

  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }

  const title = getPageTitle(location.pathname);

  return (
    <div className="flex min-h-screen bg-background">
      <Sidebar />
      <div className="flex-1 flex flex-col min-w-0">
        <TopBar title={title} />
        <ConnectivityBanner />
        <main className="flex-1 p-4 md:p-6 pb-20 md:pb-6">
          <Outlet />
        </main>
      </div>
      <BottomTabBar />
    </div>
  );
}
