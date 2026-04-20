import { Component, type ReactNode } from "react";
import { AlertTriangle } from "lucide-react";

interface Props {
  children: ReactNode;
  fallback?: ReactNode;
}

interface State {
  hasError: boolean;
  error: Error | null;
}

export default class ErrorBoundary extends Component<Props, State> {
  state: State = { hasError: false, error: null };

  static getDerivedStateFromError(error: Error): State {
    return { hasError: true, error };
  }

  render() {
    if (this.state.hasError) {
      if (this.props.fallback) return this.props.fallback;
      return (
        <div className="flex items-center justify-center py-20">
          <div className="text-center max-w-md">
            <AlertTriangle className="h-10 w-10 mx-auto text-amber-500 mb-3" />
            <h2 className="text-lg font-semibold text-text-primary mb-1">Something went wrong</h2>
            <p className="text-sm text-text-muted mb-4">
              {this.state.error?.message || "An unexpected error occurred."}
            </p>
            <button
              type="button"
              onClick={() => {
                this.setState({ hasError: false, error: null });
                window.location.reload();
              }}
              className="px-4 py-2 text-sm font-medium rounded-lg bg-primary text-white hover:bg-primary-hover transition-colors"
            >
              Reload Page
            </button>
          </div>
        </div>
      );
    }
    return this.props.children;
  }
}
