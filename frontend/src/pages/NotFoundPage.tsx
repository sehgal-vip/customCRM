import { Link } from "react-router-dom";

export default function NotFoundPage() {
  return (
    <div className="flex items-center justify-center min-h-[60vh] text-center">
      <div>
        <p className="text-6xl font-bold text-text-muted mb-2">404</p>
        <p className="text-sm font-medium text-text-primary">Page not found</p>
        <p className="text-xs text-text-muted mt-1">The page you're looking for doesn't exist.</p>
        <Link
          to="/board"
          className="inline-flex items-center gap-1.5 mt-4 px-4 py-2 text-sm font-medium rounded-lg bg-gradient-to-r from-teal-600 to-teal-500 text-white hover:from-teal-700 hover:to-teal-600 transition-colors"
        >
          Back to Pipeline
        </Link>
      </div>
    </div>
  );
}
