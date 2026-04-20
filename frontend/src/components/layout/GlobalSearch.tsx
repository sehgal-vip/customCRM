import { useState, useRef, useEffect, useCallback } from "react";
import { useNavigate } from "react-router-dom";
import { Search, X, Loader2, FileText, Building2, CheckSquare, Users, UserCircle } from "lucide-react";
import { searchApi } from "../../services/apiEndpoints";
import type { SearchResults } from "../../types";

interface SectionConfig {
  key: keyof SearchResults;
  title: string;
  icon: typeof FileText;
  getUrl: (item: { id: number }) => string | null;
  getLabel: (item: Record<string, unknown>) => string;
  getSublabel: (item: Record<string, unknown>) => string;
  getBadge?: (item: Record<string, unknown>) => { text: string; className: string } | null;
}

const SECTIONS: SectionConfig[] = [
  {
    key: "deals",
    title: "Deals",
    icon: FileText,
    getUrl: (item) => `/board/${item.id}`,
    getLabel: (item) => item.name as string,
    getSublabel: (item) => item.operatorName as string || "",
  },
  {
    key: "tasks",
    title: "Tasks",
    icon: CheckSquare,
    getUrl: () => `/tasks`,
    getLabel: (item) => item.title as string,
    getSublabel: (item) => [item.assignedToName, item.dealName].filter(Boolean).join(" · "),
    getBadge: (item) => {
      const status = item.status as string;
      if (status === "DONE") return { text: "Done", className: "bg-slate-100 text-slate-600" };
      if (status === "IN_PROGRESS") return { text: "In Progress", className: "bg-blue-50 text-blue-700" };
      return { text: "Open", className: "bg-teal-50 text-teal-700" };
    },
  },
  {
    key: "operators",
    title: "Operators",
    icon: Building2,
    getUrl: (item) => `/operators/${item.id}`,
    getLabel: (item) => item.companyName as string,
    getSublabel: (item) => [item.phone, item.email].filter(Boolean).join(" · "),
  },
  {
    key: "contacts",
    title: "Contacts",
    icon: UserCircle,
    getUrl: (item) => `/operators/${(item as Record<string, unknown>).operatorId}`,
    getLabel: (item) => item.name as string,
    getSublabel: (item) => [item.operatorName, item.role].filter(Boolean).join(" · "),
  },
  {
    key: "users",
    title: "People",
    icon: Users,
    getUrl: () => null,
    getLabel: (item) => item.name as string,
    getSublabel: (item) => item.email as string || "",
    getBadge: (item) => ({
      text: item.role as string,
      className: item.role === "MANAGER" ? "bg-primary-light text-primary" : "bg-slate-100 text-slate-600",
    }),
  },
];

export default function GlobalSearch() {
  const navigate = useNavigate();
  const [query, setQuery] = useState("");
  const [isOpen, setIsOpen] = useState(false);
  const [isLoading, setIsLoading] = useState(false);
  const [results, setResults] = useState<SearchResults | null>(null);
  const containerRef = useRef<HTMLDivElement>(null);
  const timerRef = useRef<ReturnType<typeof setTimeout>>(null);

  const doSearch = useCallback(async (q: string) => {
    if (q.length < 2) {
      setResults(null);
      setIsLoading(false);
      return;
    }
    setIsLoading(true);
    try {
      const data = await searchApi.search(q);
      setResults(data);
    } catch {
      setResults(null);
    } finally {
      setIsLoading(false);
    }
  }, []);

  const handleChange = (value: string) => {
    setQuery(value);
    setIsOpen(true);
    if (timerRef.current) clearTimeout(timerRef.current);
    timerRef.current = setTimeout(() => doSearch(value), 300);
  };

  const handleClear = () => {
    setQuery("");
    setResults(null);
    setIsOpen(false);
  };

  useEffect(() => {
    const handler = (e: MouseEvent) => {
      if (containerRef.current && !containerRef.current.contains(e.target as Node)) {
        setIsOpen(false);
      }
    };
    document.addEventListener("mousedown", handler);
    return () => document.removeEventListener("mousedown", handler);
  }, []);

  const totalResults = results
    ? SECTIONS.reduce((sum, s) => sum + ((results[s.key] as unknown[]) ?? []).length, 0)
    : 0;

  return (
    <div ref={containerRef} className="relative">
      <div className="relative">
        <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-text-muted" />
        <input
          type="text"
          value={query}
          onChange={(e) => handleChange(e.target.value)}
          onFocus={() => query.length >= 2 && setIsOpen(true)}
          placeholder="Search deals, tasks, operators, people..."
          className="w-full md:w-72 pl-9 pr-8 py-1.5 rounded-lg border border-border text-sm text-text-primary placeholder:text-text-muted bg-white focus:outline-none focus:ring-2 focus:ring-primary/40 focus:border-primary transition-colors"
        />
        {query && (
          <button
            type="button"
            onClick={handleClear}
            className="absolute right-2 top-1/2 -translate-y-1/2 p-0.5 rounded hover:bg-slate-100 text-text-muted"
          >
            <X className="h-3.5 w-3.5" />
          </button>
        )}
      </div>

      {isOpen && query.length >= 2 && (
        <div className="absolute top-full left-0 right-0 mt-1 bg-white rounded-xl border border-border shadow-[0_10px_15px_rgba(0,0,0,0.1)] z-50 max-h-96 overflow-y-auto animate-[dropdown-enter_0.15s_ease-out]">
          {isLoading && (
            <div className="flex items-center justify-center py-6">
              <Loader2 className="h-5 w-5 animate-spin text-primary" />
            </div>
          )}

          {!isLoading && results && totalResults === 0 && (
            <div className="py-6 text-center text-sm text-text-muted">
              No results found
            </div>
          )}

          {!isLoading && results && totalResults > 0 && (
            <>
              {SECTIONS.map((section) => {
                const items = ((results[section.key] ?? []) as unknown as Record<string, unknown>[]);
                if (items.length === 0) return null;

                return (
                  <div key={section.key}>
                    <div className="px-3 py-1.5 text-[10px] font-semibold uppercase tracking-wider text-text-muted bg-slate-50">
                      {section.title}
                    </div>
                    {items.map((item) => {
                      const url = section.getUrl(item as { id: number });
                      const badge = section.getBadge?.(item);

                      return (
                        <button
                          key={`${section.key}-${(item as { id: number }).id}`}
                          type="button"
                          onClick={() => {
                            if (url) navigate(url);
                            handleClear();
                          }}
                          className="w-full px-3 py-2 flex items-center gap-2 hover:bg-slate-50 text-left transition-colors"
                        >
                          <section.icon className="h-4 w-4 text-text-muted shrink-0" />
                          <div className="min-w-0 flex-1">
                            <p className="text-sm font-medium text-text-primary truncate">
                              {section.getLabel(item)}
                            </p>
                            {section.getSublabel(item) && (
                              <p className="text-xs text-text-muted truncate">
                                {section.getSublabel(item)}
                              </p>
                            )}
                          </div>
                          {badge && (
                            <span className={`text-[10px] font-medium px-1.5 py-0.5 rounded-full shrink-0 ${badge.className}`}>
                              {badge.text}
                            </span>
                          )}
                        </button>
                      );
                    })}
                  </div>
                );
              })}
            </>
          )}
        </div>
      )}
    </div>
  );
}
