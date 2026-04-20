import { useState, useMemo } from "react";
import { useNavigate } from "react-router-dom";
import { useQuery } from "@tanstack/react-query";
import {
  Loader2,
  ArrowUpRight,
  TrendingUp,
  AlertTriangle,
  BarChart3,
} from "lucide-react";
import { dashboardApi } from "../services/apiEndpoints";
import { STAGE_NAMES, STAGE_COLORS } from "../lib/constants";
import { formatINR } from "../lib/formatters";
import { cn } from "../lib/utils";
import type { AgentPerformance, WinLoss } from "../types";

type DatePreset = "THIS_WEEK" | "THIS_MONTH" | "LAST_30" | "LAST_90" | "CUSTOM";

const DATE_PRESETS: { value: DatePreset; label: string }[] = [
  { value: "THIS_WEEK", label: "This Week" },
  { value: "THIS_MONTH", label: "This Month" },
  { value: "LAST_30", label: "Last 30 Days" },
  { value: "LAST_90", label: "Last 90 Days" },
  { value: "CUSTOM", label: "Custom" },
];

function getDateRange(preset: DatePreset, customFrom?: string, customTo?: string) {
  const now = new Date();
  let from: string;
  let to: string = now.toISOString().slice(0, 10);

  switch (preset) {
    case "THIS_WEEK": {
      const d = new Date(now);
      d.setDate(d.getDate() - d.getDay());
      from = d.toISOString().slice(0, 10);
      break;
    }
    case "THIS_MONTH":
      from = new Date(now.getFullYear(), now.getMonth(), 1).toISOString().slice(0, 10);
      break;
    case "LAST_30": {
      const d = new Date(now);
      d.setDate(d.getDate() - 30);
      from = d.toISOString().slice(0, 10);
      break;
    }
    case "LAST_90": {
      const d = new Date(now);
      d.setDate(d.getDate() - 90);
      from = d.toISOString().slice(0, 10);
      break;
    }
    case "CUSTOM":
      from = customFrom || now.toISOString().slice(0, 10);
      to = customTo || now.toISOString().slice(0, 10);
      break;
  }
  return { from, to };
}

export default function DashboardPage() {
  const navigate = useNavigate();
  const [datePreset, setDatePreset] = useState<DatePreset>("LAST_30");
  const [customFrom, setCustomFrom] = useState("");
  const [customTo, setCustomTo] = useState("");

  const dateRange = useMemo(
    () => getDateRange(datePreset, customFrom, customTo),
    [datePreset, customFrom, customTo]
  );

  const params = { from: dateRange.from, to: dateRange.to };

  const { data: pipeline, isLoading: pipelineLoading } = useQuery({
    queryKey: ["dashboard-pipeline", params],
    queryFn: () => dashboardApi.pipelineOverview(params),
  });

  const { data: agentPerf, isLoading: agentLoading } = useQuery({
    queryKey: ["dashboard-agents", params],
    queryFn: () => dashboardApi.agentPerformance(params),
  });

  const { data: winLoss, isLoading: winLossLoading } = useQuery({
    queryKey: ["dashboard-winloss", params],
    queryFn: () => dashboardApi.winLoss(params),
  });

  const { data: heatmapData } = useQuery({
    queryKey: ["dashboard-heatmap", params],
    queryFn: () => dashboardApi.objectionHeatmap(params),
  });

  // Compute stats
  const totalDeals = pipeline?.stageMetrics.reduce((s, m) => s + m.dealCount, 0) ?? 0;
  const totalValue = pipeline?.stageMetrics.reduce((s, m) => s + m.totalValue, 0) ?? 0;
  const overdueCount = agentPerf?.agents.reduce((s, a) => s + a.overdueFollowUps, 0) ?? 0;
  const maxDealCount = pipeline
    ? Math.max(...pipeline.stageMetrics.map((m) => m.dealCount), 1)
    : 1;

  const anyLoading = pipelineLoading || agentLoading || winLossLoading;

  // Heatmap items
  const heatmapItems: { objection: string; count: number }[] = Array.isArray(heatmapData)
    ? heatmapData
    : [];
  const maxHeatmapCount = heatmapItems.length > 0
    ? Math.max(...heatmapItems.map((h) => h.count), 1)
    : 1;

  return (
    <div className="space-y-6">
      {/* Date range selector */}
      <div className="flex flex-wrap items-center justify-end gap-2">
        {DATE_PRESETS.map((p) => (
          <button
            key={p.value}
            type="button"
            onClick={() => setDatePreset(p.value)}
            className={cn(
              "px-3 py-1.5 text-xs font-medium rounded-lg border transition-colors",
              datePreset === p.value
                ? "bg-primary text-white border-primary"
                : "bg-white border-border text-text-secondary hover:bg-slate-50"
            )}
          >
            {p.label}
          </button>
        ))}
      </div>

      {/* Custom date pickers */}
      {datePreset === "CUSTOM" && (
        <div className="flex flex-wrap items-center gap-3">
          <div>
            <label className="block text-[11px] font-medium uppercase tracking-wider text-text-muted mb-1">
              From
            </label>
            <input
              type="date"
              value={customFrom}
              onChange={(e) => setCustomFrom(e.target.value)}
              className="rounded-lg border border-border px-3 py-1.5 text-sm focus:outline-none focus:ring-2 focus:ring-primary/40"
            />
          </div>
          <div>
            <label className="block text-[11px] font-medium uppercase tracking-wider text-text-muted mb-1">
              To
            </label>
            <input
              type="date"
              value={customTo}
              onChange={(e) => setCustomTo(e.target.value)}
              className="rounded-lg border border-border px-3 py-1.5 text-sm focus:outline-none focus:ring-2 focus:ring-primary/40"
            />
          </div>
        </div>
      )}

      {/* Loading */}
      {anyLoading && (
        <div className="flex items-center justify-center py-12">
          <Loader2 className="h-8 w-8 animate-spin text-primary" />
        </div>
      )}

      {!anyLoading && (
        <>
          {/* Stat Cards */}
          <div className="grid grid-cols-2 md:grid-cols-4 gap-3">
            <StatCard
              label="Active Deals"
              value={totalDeals.toString()}
              suffix="deals"
              onClick={() => navigate("/board")}
              icon={<BarChart3 className="h-4 w-4 text-text-muted" />}
            />
            <StatCard
              label="Pipeline Value"
              value={formatINR(totalValue)}
              onClick={() => navigate("/board")}
              icon={<TrendingUp className="h-4 w-4 text-text-muted" />}
            />
            <StatCard
              label="Overdue"
              value={overdueCount.toString()}
              valueClass="text-danger"
              onClick={() => navigate("/tasks")}
              icon={<AlertTriangle className="h-4 w-4 text-danger" />}
            />
            <StatCard
              label="Win Rate"
              value={winLoss ? `${winLoss.winRate}%` : "--"}
              valueClass="text-emerald-600"
              icon={<TrendingUp className="h-4 w-4 text-emerald-500" />}
            />
          </div>

          {/* Pipeline Funnel */}
          <Section title="Pipeline Funnel">
            <div className="space-y-2">
              {pipeline?.stageMetrics.map((metric) => {
                const stageNum = metric.stage.replace("STAGE_", "");
                const barWidth = Math.max(
                  (metric.dealCount / maxDealCount) * 100,
                  4
                );
                return (
                  <button
                    key={metric.stage}
                    type="button"
                    onClick={() => navigate("/board")}
                    className="w-full flex items-center gap-3 py-1.5 hover:bg-slate-50 rounded-lg transition-colors px-2 text-left"
                  >
                    <span className="text-xs text-text-secondary w-32 shrink-0 truncate">
                      {stageNum}. {STAGE_NAMES[metric.stage] ?? metric.stage}
                    </span>
                    <div className="flex-1 h-6 bg-slate-100 rounded-full overflow-hidden">
                      <div
                        className={cn("h-full rounded-full transition-all duration-500", STAGE_COLORS[metric.stage]?.bar || "bg-primary")}
                        style={{ width: `${barWidth}%` }}
                      />
                    </div>
                    <span className="text-xs text-text-primary font-medium w-10 text-right shrink-0">
                      {metric.dealCount}
                    </span>
                    <span className="text-xs text-text-muted w-24 text-right shrink-0 hidden md:block">
                      {formatINR(metric.totalValue)}
                    </span>
                  </button>
                );
              })}
            </div>
          </Section>

          {/* Agent Performance */}
          <Section title="Agent Performance">
            <AgentPerformanceTable data={agentPerf} />
          </Section>

          {/* Win/Loss */}
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <Section title="Win Rate">
              <div className="py-4 text-center">
                <p className="text-3xl font-bold text-emerald-600">
                  {winLoss?.winRate ?? 0}%
                </p>
                <p className="text-xs text-text-muted mt-1">
                  of closed deals resulted in a win
                </p>
              </div>
            </Section>
            <Section title="Lost Reasons">
              <LostReasonsChart data={winLoss} />
            </Section>
          </div>

          {/* Objection Heatmap */}
          {heatmapItems.length > 0 && (
            <Section title="Objection Heatmap">
              <div className="space-y-2">
                {heatmapItems.map((item) => {
                  const barWidth = Math.max(
                    (item.count / maxHeatmapCount) * 100,
                    4
                  );
                  const opacity = 0.3 + (item.count / maxHeatmapCount) * 0.7;
                  return (
                    <div
                      key={item.objection}
                      className="flex items-center gap-3"
                    >
                      <span className="text-xs text-text-secondary w-48 shrink-0 truncate">
                        {item.objection}
                      </span>
                      <div className="flex-1 h-5 bg-slate-100 rounded overflow-hidden">
                        <div
                          className="h-full rounded transition-all duration-500"
                          style={{
                            width: `${barWidth}%`,
                            backgroundColor: `rgba(239, 68, 68, ${opacity})`,
                          }}
                        />
                      </div>
                      <span className="text-xs text-text-primary font-medium w-8 text-right shrink-0">
                        {item.count}
                      </span>
                    </div>
                  );
                })}
              </div>
            </Section>
          )}
        </>
      )}
    </div>
  );
}

/* --- Sub-components --- */

function StatCard({
  label,
  value,
  suffix,
  valueClass,
  onClick,
  icon,
}: {
  label: string;
  value: string;
  suffix?: string;
  valueClass?: string;
  onClick?: () => void;
  icon?: React.ReactNode;
}) {
  return (
    <div
      onClick={onClick}
      className={cn(
        "bg-white rounded-xl border border-border p-4 shadow-[0_1px_2px_rgba(0,0,0,0.05)] transition-shadow",
        onClick && "cursor-pointer hover:shadow-md group"
      )}
    >
      <div className="flex items-center justify-between mb-2">
        <p className="text-[11px] font-medium uppercase tracking-wider text-text-muted">
          {label}
        </p>
        {icon}
      </div>
      <div className="flex items-baseline gap-1.5">
        <p className={cn("text-2xl font-bold text-text-primary", valueClass)}>
          {value}
        </p>
        {suffix && (
          <span className="text-xs text-text-muted">{suffix}</span>
        )}
      </div>
      {onClick && (
        <ArrowUpRight className="h-3.5 w-3.5 text-text-muted mt-2 group-hover:text-primary transition-colors" />
      )}
    </div>
  );
}

function Section({
  title,
  children,
}: {
  title: string;
  children: React.ReactNode;
}) {
  return (
    <div className="bg-white rounded-xl border border-border p-4 shadow-[0_1px_2px_rgba(0,0,0,0.05)]">
      <h3 className="text-sm font-semibold text-text-primary mb-4">
        {title}
      </h3>
      {children}
    </div>
  );
}

function AgentPerformanceTable({ data }: { data?: AgentPerformance }) {
  const agents = useMemo(() => {
    if (!data) return [];
    return [...data.agents].sort((a, b) => b.pipelineValue - a.pipelineValue);
  }, [data]);

  if (agents.length === 0) {
    return (
      <p className="text-sm text-text-muted text-center py-4">
        No agent data available.
      </p>
    );
  }

  return (
    <div className="overflow-x-auto">
      <table className="w-full text-sm">
        <thead>
          <tr className="border-b border-border">
            <th className="px-3 py-2 text-left text-[11px] font-medium uppercase tracking-wider text-text-muted">
              Agent
            </th>
            <th className="px-3 py-2 text-right text-[11px] font-medium uppercase tracking-wider text-text-muted">
              Active Deals
            </th>
            <th className="px-3 py-2 text-right text-[11px] font-medium uppercase tracking-wider text-text-muted">
              Pipeline Value
            </th>
            <th className="px-3 py-2 text-right text-[11px] font-medium uppercase tracking-wider text-text-muted">
              Overdue
            </th>
          </tr>
        </thead>
        <tbody>
          {agents.map((agent) => {
            const initials = agent.agentName
              .split(" ")
              .map((w) => w[0])
              .join("")
              .slice(0, 2)
              .toUpperCase();
            return (
              <tr
                key={agent.agentId}
                className="border-b border-border last:border-0 hover:bg-slate-50"
              >
                <td className="px-3 py-2.5">
                  <div className="flex items-center gap-2">
                    <div className="w-7 h-7 rounded-full bg-gradient-to-br from-teal-400 to-teal-600 flex items-center justify-center text-white text-[10px] font-bold shrink-0">
                      {initials}
                    </div>
                    <span className="text-text-primary font-medium">
                      {agent.agentName}
                    </span>
                  </div>
                </td>
                <td className="px-3 py-2.5 text-right text-text-secondary">
                  {agent.activeDeals}
                </td>
                <td className="px-3 py-2.5 text-right text-text-secondary">
                  {formatINR(agent.pipelineValue)}
                </td>
                <td className="px-3 py-2.5 text-right">
                  <span
                    className={cn(
                      agent.overdueFollowUps > 0
                        ? "text-danger font-medium"
                        : "text-text-secondary"
                    )}
                  >
                    {agent.overdueFollowUps}
                  </span>
                </td>
              </tr>
            );
          })}
        </tbody>
      </table>
    </div>
  );
}

function LostReasonsChart({ data }: { data?: WinLoss }) {
  if (!data || data.lostReasons.length === 0) {
    return (
      <p className="text-sm text-text-muted text-center py-4">
        No lost reason data available.
      </p>
    );
  }

  const maxCount = Math.max(...data.lostReasons.map((r) => r.count), 1);

  return (
    <div className="space-y-2">
      {data.lostReasons.map((reason) => {
        const barWidth = Math.max((reason.count / maxCount) * 100, 4);
        return (
          <div key={reason.reason} className="flex items-center gap-3">
            <span className="text-xs text-text-secondary w-40 shrink-0 truncate">
              {reason.reason}
            </span>
            <div className="flex-1 h-5 bg-slate-100 rounded overflow-hidden">
              <div
                className="h-full bg-red-400 rounded transition-all duration-500"
                style={{ width: `${barWidth}%` }}
              />
            </div>
            <span className="text-xs text-text-primary font-medium w-8 text-right shrink-0">
              {reason.count}
            </span>
          </div>
        );
      })}
    </div>
  );
}
