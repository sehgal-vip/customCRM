import { useState, useMemo, useRef, useEffect } from "react";
import { useQuery } from "@tanstack/react-query";
import { Plus, Loader2, X } from "lucide-react";
import { dealsApi, usersApi, operatorsApi, adminApi } from "../services/apiEndpoints";
import {
  PHASE_STAGES,
  PHASE_NAMES,
  PHASE_ORDER,
} from "../lib/constants";
import type { DealListItem, User, Operator, Region } from "../types";
import { cn } from "../lib/utils";
import { useAuth } from "../context/AuthContext";
import PhaseSection from "../components/board/PhaseSection";
import PhaseColumn from "../components/board/PhaseColumn";
import ExpandedPhaseSection from "../components/board/ExpandedPhaseSection";
import CreateDealModal from "../components/modals/CreateDealModal";

export default function PipelineBoardPage() {
  const { isManager } = useAuth();
  const [createModalOpen, setCreateModalOpen] = useState(false);
  const [expandedPhase, setExpandedPhase] = useState<string | null>(null);

  // Agent filter state
  const [selectedAgentIds, setSelectedAgentIds] = useState<number[]>([]);
  const [agentDropdownOpen, setAgentDropdownOpen] = useState(false);
  const agentDropdownRef = useRef<HTMLDivElement>(null);

  // Operator filter state
  const [selectedOperatorIds, setSelectedOperatorIds] = useState<number[]>([]);
  const [operatorDropdownOpen, setOperatorDropdownOpen] = useState(false);
  const operatorDropdownRef = useRef<HTMLDivElement>(null);

  // Created Date filter state
  const [dateFrom, setDateFrom] = useState("");
  const [dateTo, setDateTo] = useState("");
  const [createdDateDropdownOpen, setCreatedDateDropdownOpen] = useState(false);
  const createdDateDropdownRef = useRef<HTMLDivElement>(null);

  // Due Date filter state
  const [dueFrom, setDueFrom] = useState("");
  const [dueTo, setDueTo] = useState("");
  const [dueDateDropdownOpen, setDueDateDropdownOpen] = useState(false);
  const dueDateDropdownRef = useRef<HTMLDivElement>(null);

  // Status filter: "ACTIVE" | "COMPLETED" | "ARCHIVED"
  const [statusFilter, setStatusFilter] = useState<"ACTIVE" | "COMPLETED" | "ARCHIVED">("ACTIVE");
  const [statusDropdownOpen, setStatusDropdownOpen] = useState(false);
  const statusDropdownRef = useRef<HTMLDivElement>(null);

  // Region filter state
  const [selectedRegionIds, setSelectedRegionIds] = useState<number[]>([]);
  const [regionDropdownOpen, setRegionDropdownOpen] = useState(false);
  const regionDropdownRef = useRef<HTMLDivElement>(null);

  // Close dropdowns on outside click
  useEffect(() => {
    function handleClick(e: MouseEvent) {
      if (
        agentDropdownRef.current &&
        !agentDropdownRef.current.contains(e.target as Node)
      ) {
        setAgentDropdownOpen(false);
      }
      if (
        operatorDropdownRef.current &&
        !operatorDropdownRef.current.contains(e.target as Node)
      ) {
        setOperatorDropdownOpen(false);
      }
      if (
        createdDateDropdownRef.current &&
        !createdDateDropdownRef.current.contains(e.target as Node)
      ) {
        setCreatedDateDropdownOpen(false);
      }
      if (
        dueDateDropdownRef.current &&
        !dueDateDropdownRef.current.contains(e.target as Node)
      ) {
        setDueDateDropdownOpen(false);
      }
      if (
        regionDropdownRef.current &&
        !regionDropdownRef.current.contains(e.target as Node)
      ) {
        setRegionDropdownOpen(false);
      }
      if (
        statusDropdownRef.current &&
        !statusDropdownRef.current.contains(e.target as Node)
      ) {
        setStatusDropdownOpen(false);
      }
    }
    document.addEventListener("mousedown", handleClick);
    return () => document.removeEventListener("mousedown", handleClick);
  }, []);

  // Fetch users for agent filter (manager only)
  const { data: users } = useQuery({
    queryKey: ["users"],
    queryFn: () => usersApi.list(),
    enabled: isManager,
  });

  const agents = useMemo(
    () => (users ?? []).filter((u: User) => u.status === "ACTIVE"),
    [users]
  );

  // Fetch operators for operator filter
  const { data: operatorsPage } = useQuery({
    queryKey: ["operators-filter"],
    queryFn: () => operatorsApi.list({ size: 200 }),
  });

  const operators: Operator[] = useMemo(
    () => operatorsPage?.content ?? [],
    [operatorsPage]
  );

  // Fetch regions for region filter
  const { data: regions } = useQuery({
    queryKey: ["regions-filter"],
    queryFn: () => adminApi.listRegions(),
  });

  const activeRegions: Region[] = useMemo(
    () => (regions ?? []).filter((r: Region) => r.active !== false),
    [regions]
  );

  const toggleAgent = (id: number) => {
    setSelectedAgentIds((prev) =>
      prev.includes(id) ? prev.filter((x) => x !== id) : [...prev, id]
    );
  };

  const toggleOperator = (id: number) => {
    setSelectedOperatorIds((prev) =>
      prev.includes(id) ? prev.filter((x) => x !== id) : [...prev, id]
    );
  };

  const toggleRegion = (id: number) => {
    setSelectedRegionIds((prev) =>
      prev.includes(id) ? prev.filter((x) => x !== id) : [...prev, id]
    );
  };

  // Build API params
  const apiParams: Record<string, unknown> = {
    status: statusFilter,
    size: "500",
  };
  if (selectedAgentIds.length > 0) {
    apiParams.agentIds = selectedAgentIds;
  }
  if (selectedOperatorIds.length > 0) {
    apiParams.operatorIds = selectedOperatorIds;
  }
  if (selectedRegionIds.length > 0) {
    apiParams.regionIds = selectedRegionIds;
  }
  if (dateFrom) {
    apiParams.dateFrom = dateFrom + "T00:00:00Z";
  }
  if (dateTo) {
    apiParams.dateTo = dateTo + "T23:59:59Z";
  }

  const {
    data: dealsPage,
    isLoading,
    isError,
  } = useQuery({
    queryKey: ["deals", statusFilter, selectedAgentIds, selectedOperatorIds, selectedRegionIds, dateFrom, dateTo],
    queryFn: () => dealsApi.list(apiParams),
  });

  const rawDeals: DealListItem[] = useMemo(() => dealsPage?.content ?? [], [dealsPage]);

  // Client-side filter for due date (nextActionEta)
  const deals = useMemo(() => {
    if (!dueFrom && !dueTo) return rawDeals;
    return rawDeals.filter((d) => {
      if (!d.nextActionEta) return false;
      const eta = d.nextActionEta.slice(0, 10); // YYYY-MM-DD
      if (dueFrom && eta < dueFrom) return false;
      if (dueTo && eta > dueTo) return false;
      return true;
    });
  }, [rawDeals, dueFrom, dueTo]);

  // Determine visible stages per phase based on status filter
  // Hide STAGE_8 (Vehicle Delivery) for Active/Lost — unless deals exist there (e.g. reopened)
  const hasDealsAtStage8 = deals.some((d) => d.currentStage === "STAGE_8");
  const visiblePhaseStages: Record<string, string[]> = {};
  for (const phase of PHASE_ORDER) {
    let stages = [...PHASE_STAGES[phase]];
    if (phase === "CLOSURE" && statusFilter !== "COMPLETED" && !hasDealsAtStage8) {
      stages = stages.filter((s) => s !== "STAGE_8");
    }
    visiblePhaseStages[phase] = stages;
  }

  // Group deals by phase
  const dealsByPhase: Record<string, DealListItem[]> = {};
  for (const phase of PHASE_ORDER) {
    const stageKeys = visiblePhaseStages[phase];
    dealsByPhase[phase] = deals.filter((d) =>
      stageKeys.includes(d.currentStage)
    );
  }

  // Group deals by stage
  const dealsByStage: Record<string, DealListItem[]> = {};
  for (const d of deals) {
    if (!dealsByStage[d.currentStage]) dealsByStage[d.currentStage] = [];
    dealsByStage[d.currentStage].push(d);
  }

  // Total grid columns: 1 per collapsed phase + N stages for expanded phase
  const totalColumns = PHASE_ORDER.reduce((sum, phase) => {
    if (phase === expandedPhase) {
      return sum + visiblePhaseStages[phase].length;
    }
    return sum + 1;
  }, 0);

  const chevronSvg = (
    <svg
      className="h-3 w-3 text-text-muted"
      fill="none"
      viewBox="0 0 24 24"
      stroke="currentColor"
      strokeWidth={2}
    >
      <path
        strokeLinecap="round"
        strokeLinejoin="round"
        d="M19 9l-7 7-7-7"
      />
    </svg>
  );

  return (
    <div className="space-y-3">
      {/* Filter bar + New Deal */}
      <div className="flex items-center justify-between gap-2 relative z-40">
        <div className="flex items-center gap-2 flex-wrap pb-1 flex-1">
        {/* Agent filter button (functional for managers) */}
        {isManager ? (
          <div className="relative" ref={agentDropdownRef}>
            <button
              type="button"
              onClick={() => setAgentDropdownOpen((v) => !v)}
              className={cn(
                "inline-flex items-center gap-1.5 px-3 py-1.5 text-xs font-medium rounded-lg border transition-colors whitespace-nowrap",
                selectedAgentIds.length > 0
                  ? "border-teal-300 bg-teal-50 text-teal-700"
                  : "border-border text-text-secondary bg-white hover:bg-slate-50"
              )}
            >
              Agent
              {selectedAgentIds.length > 0 && (
                <span className="inline-flex items-center justify-center w-4 h-4 text-[10px] font-semibold rounded-full bg-teal-600 text-white">
                  {selectedAgentIds.length}
                </span>
              )}
              {chevronSvg}
            </button>
            {agentDropdownOpen && (
              <div className="absolute left-0 top-full mt-1 z-50 bg-white rounded-lg border border-border shadow-lg p-2 min-w-[220px]">
                <div className="flex flex-wrap gap-1.5">
                  {agents.map((agent: User) => {
                    const isSelected = selectedAgentIds.includes(agent.id);
                    return (
                      <button
                        key={agent.id}
                        type="button"
                        onClick={() => toggleAgent(agent.id)}
                        className={cn(
                          "inline-flex items-center gap-1 px-2.5 py-1 text-xs font-medium rounded-full transition-colors",
                          isSelected
                            ? "bg-teal-100 text-teal-700 border border-teal-300"
                            : "bg-slate-100 text-slate-600 border border-slate-200 hover:bg-slate-200"
                        )}
                      >
                        {agent.name}
                        {isSelected && <X className="h-3 w-3" />}
                      </button>
                    );
                  })}
                </div>
                {selectedAgentIds.length > 0 && (
                  <button
                    type="button"
                    onClick={() => setSelectedAgentIds([])}
                    className="mt-2 text-xs text-text-muted hover:text-text-secondary underline"
                  >
                    Clear
                  </button>
                )}
              </div>
            )}
          </div>
        ) : null}

        {/* Operator filter */}
        <div className="relative" ref={operatorDropdownRef}>
          <button
            type="button"
            onClick={() => setOperatorDropdownOpen((v) => !v)}
            className={cn(
              "inline-flex items-center gap-1.5 px-3 py-1.5 text-xs font-medium rounded-lg border transition-colors whitespace-nowrap",
              selectedOperatorIds.length > 0
                ? "border-teal-300 bg-teal-50 text-teal-700"
                : "border-border text-text-secondary bg-white hover:bg-slate-50"
            )}
          >
            Operator
            {selectedOperatorIds.length > 0 && (
              <span className="inline-flex items-center justify-center w-4 h-4 text-[10px] font-semibold rounded-full bg-teal-600 text-white">
                {selectedOperatorIds.length}
              </span>
            )}
            {chevronSvg}
          </button>
          {operatorDropdownOpen && (
            <div className="absolute left-0 top-full mt-1 z-50 bg-white rounded-lg border border-border shadow-lg p-2 min-w-[220px] max-h-[300px] overflow-y-auto">
              <div className="flex flex-wrap gap-1.5">
                {operators.map((op: Operator) => {
                  const isSelected = selectedOperatorIds.includes(op.id);
                  return (
                    <button
                      key={op.id}
                      type="button"
                      onClick={() => toggleOperator(op.id)}
                      className={cn(
                        "inline-flex items-center gap-1 px-2.5 py-1 text-xs font-medium rounded-full transition-colors",
                        isSelected
                          ? "bg-teal-100 text-teal-700 border border-teal-300"
                          : "bg-slate-100 text-slate-600 border border-slate-200 hover:bg-slate-200"
                      )}
                    >
                      {op.companyName}
                      {isSelected && <X className="h-3 w-3" />}
                    </button>
                  );
                })}
              </div>
              {selectedOperatorIds.length > 0 && (
                <button
                  type="button"
                  onClick={() => setSelectedOperatorIds([])}
                  className="mt-2 text-xs text-text-muted hover:text-text-secondary underline"
                >
                  Clear
                </button>
              )}
            </div>
          )}
        </div>

        {/* Created Date filter */}
        <div className="relative" ref={createdDateDropdownRef}>
          <button
            type="button"
            onClick={() => setCreatedDateDropdownOpen((v) => !v)}
            className={cn(
              "inline-flex items-center gap-1.5 px-3 py-1.5 text-xs font-medium rounded-lg border transition-colors whitespace-nowrap",
              dateFrom || dateTo
                ? "border-teal-300 bg-teal-50 text-teal-700"
                : "border-border text-text-secondary bg-white hover:bg-slate-50"
            )}
          >
            Created Date
            {chevronSvg}
          </button>
          {createdDateDropdownOpen && (
            <div className="absolute left-0 top-full mt-1 z-50 bg-white rounded-lg border border-border shadow-lg p-3 min-w-[220px] space-y-2">
              <div>
                <label className="block text-[11px] font-medium uppercase tracking-wider text-text-muted mb-1">
                  From
                </label>
                <input
                  type="date"
                  value={dateFrom}
                  onChange={(e) => setDateFrom(e.target.value)}
                  className="w-full rounded-lg border border-border px-2.5 py-1.5 text-xs focus:outline-none focus:ring-2 focus:ring-primary/40 focus:border-primary"
                />
              </div>
              <div>
                <label className="block text-[11px] font-medium uppercase tracking-wider text-text-muted mb-1">
                  To
                </label>
                <input
                  type="date"
                  value={dateTo}
                  onChange={(e) => setDateTo(e.target.value)}
                  className="w-full rounded-lg border border-border px-2.5 py-1.5 text-xs focus:outline-none focus:ring-2 focus:ring-primary/40 focus:border-primary"
                />
              </div>
              {(dateFrom || dateTo) && (
                <button
                  type="button"
                  onClick={() => {
                    setDateFrom("");
                    setDateTo("");
                  }}
                  className="text-xs text-text-muted hover:text-text-secondary underline"
                >
                  Clear
                </button>
              )}
            </div>
          )}
        </div>

        {/* Due Date filter */}
        <div className="relative" ref={dueDateDropdownRef}>
          <button
            type="button"
            onClick={() => setDueDateDropdownOpen((v) => !v)}
            className={cn(
              "inline-flex items-center gap-1.5 px-3 py-1.5 text-xs font-medium rounded-lg border transition-colors whitespace-nowrap",
              dueFrom || dueTo
                ? "border-teal-300 bg-teal-50 text-teal-700"
                : "border-border text-text-secondary bg-white hover:bg-slate-50"
            )}
          >
            Due Date
            {chevronSvg}
          </button>
          {dueDateDropdownOpen && (
            <div className="absolute left-0 top-full mt-1 z-50 bg-white rounded-lg border border-border shadow-lg p-3 min-w-[220px] space-y-2">
              <div>
                <label className="block text-[11px] font-medium uppercase tracking-wider text-text-muted mb-1">
                  From
                </label>
                <input
                  type="date"
                  value={dueFrom}
                  onChange={(e) => setDueFrom(e.target.value)}
                  className="w-full rounded-lg border border-border px-2.5 py-1.5 text-xs focus:outline-none focus:ring-2 focus:ring-primary/40 focus:border-primary"
                />
              </div>
              <div>
                <label className="block text-[11px] font-medium uppercase tracking-wider text-text-muted mb-1">
                  To
                </label>
                <input
                  type="date"
                  value={dueTo}
                  onChange={(e) => setDueTo(e.target.value)}
                  className="w-full rounded-lg border border-border px-2.5 py-1.5 text-xs focus:outline-none focus:ring-2 focus:ring-primary/40 focus:border-primary"
                />
              </div>
              {(dueFrom || dueTo) && (
                <button
                  type="button"
                  onClick={() => {
                    setDueFrom("");
                    setDueTo("");
                  }}
                  className="text-xs text-text-muted hover:text-text-secondary underline"
                >
                  Clear
                </button>
              )}
            </div>
          )}
        </div>

        {/* Region filter */}
        <div className="relative" ref={regionDropdownRef}>
          <button
            type="button"
            onClick={() => setRegionDropdownOpen((v) => !v)}
            className={cn(
              "inline-flex items-center gap-1.5 px-3 py-1.5 text-xs font-medium rounded-lg border transition-colors whitespace-nowrap",
              selectedRegionIds.length > 0
                ? "border-teal-300 bg-teal-50 text-teal-700"
                : "border-border text-text-secondary bg-white hover:bg-slate-50"
            )}
          >
            Region
            {selectedRegionIds.length > 0 && (
              <span className="inline-flex items-center justify-center w-4 h-4 text-[10px] font-semibold rounded-full bg-teal-600 text-white">
                {selectedRegionIds.length}
              </span>
            )}
            {chevronSvg}
          </button>
          {regionDropdownOpen && (
            <div className="absolute left-0 top-full mt-1 z-50 bg-white rounded-lg border border-border shadow-lg p-2 min-w-[220px] max-h-[300px] overflow-y-auto">
              <div className="flex flex-wrap gap-1.5">
                {activeRegions.map((region: Region) => {
                  const isSelected = selectedRegionIds.includes(region.id);
                  return (
                    <button
                      key={region.id}
                      type="button"
                      onClick={() => toggleRegion(region.id)}
                      className={cn(
                        "inline-flex items-center gap-1 px-2.5 py-1 text-xs font-medium rounded-full transition-colors",
                        isSelected
                          ? "bg-teal-100 text-teal-700 border border-teal-300"
                          : "bg-slate-100 text-slate-600 border border-slate-200 hover:bg-slate-200"
                      )}
                    >
                      {region.name}
                      {isSelected && <X className="h-3 w-3" />}
                    </button>
                  );
                })}
              </div>
              {selectedRegionIds.length > 0 && (
                <button
                  type="button"
                  onClick={() => setSelectedRegionIds([])}
                  className="mt-2 text-xs text-text-muted hover:text-text-secondary underline"
                >
                  Clear
                </button>
              )}
            </div>
          )}
        </div>

        {/* Status filter dropdown */}
        <div className="relative" ref={statusDropdownRef}>
          <button
            type="button"
            onClick={() => setStatusDropdownOpen((v) => !v)}
            className={cn(
              "inline-flex items-center gap-1.5 px-3 py-1.5 text-xs font-medium rounded-lg border transition-colors whitespace-nowrap",
              statusFilter !== "ACTIVE"
                ? "border-teal-300 bg-teal-50 text-teal-700"
                : "border-border text-text-secondary bg-white hover:bg-slate-50"
            )}
          >
            {statusFilter === "ACTIVE" ? "Active" : statusFilter === "COMPLETED" ? "Closed" : "Lost"}
            {chevronSvg}
          </button>
          {statusDropdownOpen && (
            <div className="absolute left-0 top-full mt-1 z-50 bg-white rounded-lg border border-border shadow-lg p-2 min-w-[140px]">
              <div className="flex flex-col gap-1">
                {([["ACTIVE", "Active"], ["COMPLETED", "Closed"], ["ARCHIVED", "Lost"]] as const).map(([value, label]) => (
                  <button
                    key={value}
                    type="button"
                    onClick={() => { setStatusFilter(value); setStatusDropdownOpen(false); }}
                    className={cn(
                      "text-left px-3 py-1.5 text-xs font-medium rounded-lg transition-colors",
                      statusFilter === value
                        ? "bg-teal-50 text-teal-700"
                        : "text-text-secondary hover:bg-slate-50"
                    )}
                  >
                    {label}
                  </button>
                ))}
              </div>
            </div>
          )}
        </div>
        </div>
        <button
          type="button"
          onClick={() => setCreateModalOpen(true)}
          className="hidden md:inline-flex items-center gap-1.5 px-3 py-2 text-sm font-medium rounded-lg bg-gradient-to-r from-teal-600 to-teal-500 text-white shadow-sm hover:from-teal-700 hover:to-teal-600 transition-colors whitespace-nowrap shrink-0"
        >
          <Plus className="h-4 w-4" />
          New Deal
        </button>
      </div>

      {/* Loading / Error */}
      {isLoading && (
        <div className="flex items-center justify-center py-12">
          <Loader2 className="h-8 w-8 animate-spin text-primary" />
        </div>
      )}

      {isError && (
        <div className="flex items-center justify-center py-12">
          <div className="text-center">
            <p className="text-sm text-danger font-medium">Failed to load deals</p>
            <p className="text-xs text-text-muted mt-1">Please check your connection and try again.</p>
          </div>
        </div>
      )}

      {/* Desktop: Kanban board */}
      {!isLoading && !isError && deals.length > 0 && (
        <>
          {/* Kanban layout (md+) */}
          <div
            className="hidden md:grid gap-2 transition-all duration-300 relative z-0"
            style={{
              gridTemplateColumns: `repeat(${totalColumns}, minmax(0, 1fr))`,
              height: "calc(100vh - 160px)",
            }}
          >
            {PHASE_ORDER.map((phaseKey) => {
              const isExpanded = expandedPhase === phaseKey;
              const stages = visiblePhaseStages[phaseKey];
              const phaseDeals = dealsByPhase[phaseKey];

              if (isExpanded) {
                return (
                  <div
                    key={phaseKey}
                    style={{ gridColumn: `span ${stages.length}` }}
                    className="min-h-0 flex flex-col"
                  >
                    <ExpandedPhaseSection
                      phaseName={PHASE_NAMES[phaseKey]}
                      phaseKey={phaseKey}
                      stageKeys={stages}
                      dealsByStage={dealsByStage}
                      onCollapse={() => setExpandedPhase(null)}
                    />
                  </div>
                );
              }

              return (
                <PhaseColumn
                  key={phaseKey}
                  phaseName={PHASE_NAMES[phaseKey]}
                  phaseKey={phaseKey}
                  deals={phaseDeals}
                  onExpand={() => setExpandedPhase(phaseKey)}
                />
              );
            })}
          </div>

          {/* Mobile: existing vertical layout */}
          <div className="md:hidden space-y-4">
            {PHASE_ORDER.map((phaseKey) => (
              <PhaseSection
                key={phaseKey}
                phaseName={PHASE_NAMES[phaseKey]}
                stageKeys={visiblePhaseStages[phaseKey]}
                deals={dealsByPhase[phaseKey]}
                defaultExpanded
              />
            ))}
          </div>
        </>
      )}

      {/* Empty state */}
      {!isLoading && !isError && deals.length === 0 && (
        <div className="flex flex-col items-center justify-center py-12 text-center">
          <div className="w-16 h-16 rounded-full bg-slate-100 flex items-center justify-center mb-3">
            <Plus className="h-8 w-8 text-text-muted" />
          </div>
          <p className="text-sm font-medium text-text-primary">No active deals yet</p>
          <p className="text-xs text-text-muted mt-1">Create your first deal to get started.</p>
          <button
            type="button"
            onClick={() => setCreateModalOpen(true)}
            className="mt-4 inline-flex items-center gap-2 px-4 py-2 text-sm font-medium rounded-lg bg-primary text-white shadow-sm hover:bg-primary-hover transition-colors"
          >
            <Plus className="h-4 w-4" />
            New Deal
          </button>
        </div>
      )}

      {/* Mobile FAB */}
      <button
        type="button"
        onClick={() => setCreateModalOpen(true)}
        className="md:hidden fixed right-4 bottom-20 z-20 w-14 h-14 rounded-full bg-primary text-white shadow-lg hover:bg-primary-hover transition-colors flex items-center justify-center"
      >
        <Plus className="h-6 w-6" />
      </button>

      {/* Create Deal Modal */}
      <CreateDealModal
        open={createModalOpen}
        onClose={() => setCreateModalOpen(false)}
      />
    </div>
  );
}
