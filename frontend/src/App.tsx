import { lazy, Suspense } from "react";
import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { Toaster } from "sonner";
import { AuthProvider } from "./context/AuthContext";
import ErrorBoundary from "./components/shared/ErrorBoundary";
import LoginPage from "./pages/LoginPage";
import AuthenticatedLayout from "./components/layout/AuthenticatedLayout";
import { Loader2 } from "lucide-react";

const PipelineBoardPage = lazy(() => import("./pages/PipelineBoardPage"));
const DealDetailPage = lazy(() => import("./pages/DealDetailPage"));
const TasksPage = lazy(() => import("./pages/TasksPage"));
const OperatorsPage = lazy(() => import("./pages/OperatorsPage"));
const OperatorDetailPage = lazy(() => import("./pages/OperatorDetailPage"));
const DashboardPage = lazy(() => import("./pages/DashboardPage"));
const NotificationsPage = lazy(() => import("./pages/NotificationsPage"));
const AdminSettingsPage = lazy(() => import("./pages/AdminSettingsPage"));
const NotFoundPage = lazy(() => import("./pages/NotFoundPage"));

const queryClient = new QueryClient({
  defaultOptions: { queries: { staleTime: 60000, retry: 1 } },
});

function PageLoader() {
  return (
    <div className="flex items-center justify-center py-20">
      <Loader2 className="h-8 w-8 animate-spin text-primary" />
    </div>
  );
}

export default function App() {
  return (
    <ErrorBoundary>
      <QueryClientProvider client={queryClient}>
        <AuthProvider>
          <BrowserRouter future={{ v7_startTransition: true, v7_relativeSplatPath: true }}>
            <Suspense fallback={<PageLoader />}>
              <Routes>
                <Route path="/login" element={<LoginPage />} />
                <Route element={<AuthenticatedLayout />}>
                  <Route path="/board" element={<PipelineBoardPage />} />
                  <Route path="/board/:dealId" element={<DealDetailPage />} />
                  <Route path="/tasks" element={<TasksPage />} />
                  <Route path="/operators" element={<OperatorsPage />} />
                  <Route path="/operators/:operatorId" element={<OperatorDetailPage />} />
                  <Route path="/dashboard" element={<DashboardPage />} />
                  <Route path="/notifications" element={<NotificationsPage />} />
                  <Route path="/admin" element={<AdminSettingsPage />} />
                  <Route path="/" element={<Navigate to="/board" replace />} />
                  <Route path="*" element={<NotFoundPage />} />
                </Route>
              </Routes>
            </Suspense>
          </BrowserRouter>
          <Toaster position="top-right" richColors closeButton />
        </AuthProvider>
      </QueryClientProvider>
    </ErrorBoundary>
  );
}
