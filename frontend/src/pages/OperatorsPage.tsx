import { useState, useRef, useEffect } from "react";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { useNavigate } from "react-router-dom";
import { Loader2, Search, Bus, Route, Phone, Mail, AlertTriangle, Plus, X } from "lucide-react";
import { operatorsApi } from "../services/apiEndpoints";
import { cn } from "../lib/utils";
import { formatEnum } from "../lib/formatters";
import type { Operator, Contact } from "../types";

const TYPE_FILTERS = [
  { key: "all", label: "All" },
  { key: "Private Fleet", label: "Private Fleet" },
  { key: "Govt Contract", label: "Govt Contract" },
  { key: "School/Corporate", label: "School/Corporate" },
  { key: "Mixed", label: "Mixed" },
];

export default function OperatorsPage() {
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const [search, setSearch] = useState("");
  const [typeFilter, setTypeFilter] = useState("all");
  const [showCreateModal, setShowCreateModal] = useState(false);

  const { data, isLoading } = useQuery({
    queryKey: ["operators", search, typeFilter],
    queryFn: () =>
      operatorsApi.list({
        search: search || undefined,
        operatorType: typeFilter !== "all" ? typeFilter : undefined,
        size: 100,
      }),
  });

  const operators: Operator[] = data?.content ?? [];

  return (
    <div className="space-y-5">
      {/* Header with New Operator */}
      <div className="flex items-center justify-between">
        <h2 className="text-lg font-semibold text-text-primary">Operators</h2>
        <button
          onClick={() => setShowCreateModal(true)}
          className="inline-flex items-center gap-1.5 px-3 py-2 text-sm font-medium rounded-lg text-white bg-primary hover:bg-primary-hover shadow-sm transition-colors"
        >
          <Plus className="h-4 w-4" />
          New Operator
        </button>
      </div>

      {/* Create Operator Modal */}
      {showCreateModal && (
        <CreateOperatorModal
          onClose={() => setShowCreateModal(false)}
          onCreated={() => {
            queryClient.invalidateQueries({ queryKey: ["operators"] });
            setShowCreateModal(false);
          }}
        />
      )}

      {/* Search + Filters */}
      <div className="space-y-3">
        <div className="relative">
          <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-text-muted" />
          <input
            type="text"
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            placeholder="Search operators..."
            className="w-full pl-10 pr-4 py-2.5 rounded-lg border border-border text-sm focus:outline-none focus:ring-2 focus:ring-primary/40 focus:border-primary"
          />
        </div>
        <div className="flex flex-wrap gap-2">
          {TYPE_FILTERS.map((f) => (
            <button
              key={f.key}
              type="button"
              onClick={() => setTypeFilter(f.key)}
              className={cn(
                "px-3 py-1.5 rounded-lg text-xs font-medium border transition-colors",
                typeFilter === f.key
                  ? "bg-primary text-white border-primary"
                  : "bg-white text-text-secondary border-border hover:border-primary"
              )}
            >
              {f.label}
            </button>
          ))}
        </div>
      </div>

      {/* Operator Cards */}
      {isLoading ? (
        <div className="flex items-center justify-center py-12">
          <Loader2 className="h-8 w-8 animate-spin text-primary" />
        </div>
      ) : operators.length === 0 ? (
        <div className="py-12 text-center text-sm text-text-muted">
          No operators found.
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          {operators.map((op) => (
            <OperatorCard
              key={op.id}
              operator={op}
              onClick={() => navigate(`/operators/${op.id}`)}
            />
          ))}
        </div>
      )}
    </div>
  );
}

function OperatorCard({
  operator,
  onClick,
}: {
  operator: Operator;
  onClick: () => void;
}) {
  return (
    <div
      onClick={onClick}
      className="bg-white rounded-xl border border-border p-4 shadow-[0_1px_2px_rgba(0,0,0,0.05)] hover:shadow-[0_4px_6px_rgba(0,0,0,0.07)] transition-shadow cursor-pointer space-y-3"
    >
      {/* Header */}
      <div className="flex items-start justify-between gap-2">
        <div>
          <h3 className="text-sm font-semibold text-text-primary">
            {operator.companyName}
          </h3>
          {operator.region && (
            <p className="text-xs text-text-muted mt-0.5">
              {operator.region.name}
            </p>
          )}
        </div>
        {operator.operatorType && (
          <span className="px-2 py-0.5 rounded-full text-[10px] font-medium bg-blue-50 text-blue-700 shrink-0">
            {formatEnum(operator.operatorType)}
          </span>
        )}
      </div>

      {/* Stats */}
      <div className="flex items-center gap-4">
        {operator.fleetSize != null && (
          <div className="flex items-center gap-1 text-xs text-text-secondary">
            <Bus className="h-3.5 w-3.5 text-text-muted" />
            <span>{operator.fleetSize} buses</span>
          </div>
        )}
        {operator.numRoutes != null && (
          <div className="flex items-center gap-1 text-xs text-text-secondary">
            <Route className="h-3.5 w-3.5 text-text-muted" />
            <span>{operator.numRoutes} routes</span>
          </div>
        )}
      </div>

      {/* Contacts */}
      {operator.contacts.length > 0 && (
        <div className="space-y-1.5 pt-2 border-t border-border">
          {operator.contacts.slice(0, 2).map((contact) => (
            <ContactRow key={contact.id} contact={contact} />
          ))}
          {operator.contacts.length > 2 && (
            <p className="text-[10px] text-text-muted">
              +{operator.contacts.length - 2} more contacts
            </p>
          )}
        </div>
      )}

      {/* Deal count */}
      {operator.dealCount > 0 && (
        <div className="pt-2 border-t border-border">
          <span className="text-xs text-text-muted">
            {operator.dealCount} linked {operator.dealCount === 1 ? "deal" : "deals"}
          </span>
        </div>
      )}
    </div>
  );
}

function ContactRow({ contact }: { contact: Contact }) {
  const initials = contact.name
    .split(" ")
    .map((n) => n[0])
    .join("")
    .slice(0, 2);

  return (
    <div className="flex items-center gap-2">
      <div className="w-6 h-6 rounded-full bg-slate-200 flex items-center justify-center text-[10px] font-semibold text-slate-600 shrink-0">
        {initials}
      </div>
      <div className="flex-1 min-w-0">
        <div className="flex items-center gap-1.5">
          <span className="text-xs font-medium text-text-primary truncate">
            {contact.name}
          </span>
          <span className="text-[10px] text-text-muted">{formatEnum(contact.role)}</span>
          {contact.incomplete && (
            <span className="px-1.5 py-0.5 rounded-full text-[9px] font-medium bg-amber-100 text-amber-700 flex items-center gap-0.5">
              <AlertTriangle className="h-2.5 w-2.5" />
              Incomplete
            </span>
          )}
        </div>
        <div className="flex items-center gap-2 text-[10px] text-text-muted">
          {contact.mobile && (
            <span className="flex items-center gap-0.5">
              <Phone className="h-2.5 w-2.5" />
              {contact.mobile}
            </span>
          )}
          {contact.email && (
            <span className="flex items-center gap-0.5">
              <Mail className="h-2.5 w-2.5" />
              {contact.email}
            </span>
          )}
        </div>
      </div>
    </div>
  );
}

function CreateOperatorModal({ onClose, onCreated }: { onClose: () => void; onCreated: () => void }) {
  const dialogRef = useRef<HTMLDialogElement>(null);
  const [form, setForm] = useState({
    companyName: "", phone: "", email: "", operatorType: "", fleetSize: "", numRoutes: "", primaryUseCase: "",
  });
  const [error, setError] = useState("");

  const mutation = useMutation({
    mutationFn: (data: Record<string, unknown>) => operatorsApi.create(data),
    onSuccess: () => onCreated(),
    onError: (err: unknown) => {
      const error = err as { response?: { data?: { error?: { message?: string } } } };
      setError(error?.response?.data?.error?.message || "Failed to create operator");
    },
  });

  useEffect(() => {
    dialogRef.current?.showModal();
  }, []);

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!form.companyName.trim()) { setError("Company name is required"); return; }
    const payload: Record<string, unknown> = {
      companyName: form.companyName,
      phone: form.phone || undefined,
      email: form.email || undefined,
      operatorType: form.operatorType || undefined,
      fleetSize: form.fleetSize ? parseInt(form.fleetSize) : undefined,
      numRoutes: form.numRoutes ? parseInt(form.numRoutes) : undefined,
      primaryUseCase: form.primaryUseCase || undefined,
    };
    mutation.mutate(payload);
  };

  return (
    <dialog ref={dialogRef} onCancel={onClose} className="backdrop:bg-black/50 backdrop:backdrop-blur-sm bg-transparent p-0 m-auto rounded-2xl max-w-lg w-full">
      <div className="bg-white rounded-2xl shadow-[0_25px_50px_rgba(0,0,0,0.15)] max-h-[90vh] flex flex-col">
        <div className="flex items-center justify-between px-6 py-4 border-b border-border">
          <h2 className="text-lg font-semibold text-text-primary">New Operator</h2>
          <button type="button" onClick={onClose} className="p-1 rounded-lg hover:bg-slate-100 text-text-muted"><X className="h-5 w-5" /></button>
        </div>
        <form onSubmit={handleSubmit} className="flex-1 overflow-y-auto px-6 py-5 space-y-4">
          <div>
            <label className="block text-[11px] font-medium uppercase tracking-wider text-text-muted mb-1">Company Name *</label>
            <input value={form.companyName} onChange={e => setForm(p => ({ ...p, companyName: e.target.value }))} className="w-full rounded-lg border border-border px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-primary/40 focus:border-primary" />
          </div>
          <div className="grid grid-cols-2 gap-3">
            <div>
              <label className="block text-[11px] font-medium uppercase tracking-wider text-text-muted mb-1">Phone</label>
              <input value={form.phone} onChange={e => setForm(p => ({ ...p, phone: e.target.value }))} className="w-full rounded-lg border border-border px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-primary/40 focus:border-primary" />
            </div>
            <div>
              <label className="block text-[11px] font-medium uppercase tracking-wider text-text-muted mb-1">Email</label>
              <input value={form.email} onChange={e => setForm(p => ({ ...p, email: e.target.value }))} type="email" className="w-full rounded-lg border border-border px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-primary/40 focus:border-primary" />
            </div>
          </div>
          <div className="grid grid-cols-2 gap-3">
            <div>
              <label className="block text-[11px] font-medium uppercase tracking-wider text-text-muted mb-1">Operator Type</label>
              <select value={form.operatorType} onChange={e => setForm(p => ({ ...p, operatorType: e.target.value }))} className="w-full rounded-lg border border-border px-3 py-2 text-sm bg-white focus:outline-none focus:ring-2 focus:ring-primary/40 focus:border-primary">
                <option value="">Select...</option>
                <option value="PRIVATE_FLEET">Private Fleet</option>
                <option value="GOVT_CONTRACT">Govt Contract</option>
                <option value="SCHOOL_CORPORATE">School/Corporate</option>
                <option value="MIXED">Mixed</option>
              </select>
            </div>
            <div>
              <label className="block text-[11px] font-medium uppercase tracking-wider text-text-muted mb-1">Primary Use Case</label>
              <select value={form.primaryUseCase} onChange={e => setForm(p => ({ ...p, primaryUseCase: e.target.value }))} className="w-full rounded-lg border border-border px-3 py-2 text-sm bg-white focus:outline-none focus:ring-2 focus:ring-primary/40 focus:border-primary">
                <option value="">Select...</option>
                <option value="CITY_TRANSPORT">City Transport</option>
                <option value="INTERCITY">Intercity</option>
                <option value="SCHOOL_SHUTTLE">School Shuttle</option>
                <option value="CORPORATE_SHUTTLE">Corporate Shuttle</option>
                <option value="LAST_MILE">Last Mile</option>
                <option value="MIXED">Mixed</option>
              </select>
            </div>
          </div>
          <div className="grid grid-cols-2 gap-3">
            <div>
              <label className="block text-[11px] font-medium uppercase tracking-wider text-text-muted mb-1">Fleet Size</label>
              <input value={form.fleetSize} onChange={e => setForm(p => ({ ...p, fleetSize: e.target.value }))} type="number" min={1} className="w-full rounded-lg border border-border px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-primary/40 focus:border-primary" />
            </div>
            <div>
              <label className="block text-[11px] font-medium uppercase tracking-wider text-text-muted mb-1">Number of Routes</label>
              <input value={form.numRoutes} onChange={e => setForm(p => ({ ...p, numRoutes: e.target.value }))} type="number" min={0} className="w-full rounded-lg border border-border px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-primary/40 focus:border-primary" />
            </div>
          </div>
          {error && <div className="rounded-lg bg-red-50 border border-red-200 px-4 py-3 text-sm text-danger">{error}</div>}
        </form>
        <div className="flex items-center justify-end gap-3 px-6 py-4 border-t border-border">
          <button type="button" onClick={onClose} className="px-4 py-2 text-sm rounded-lg text-text-secondary hover:bg-slate-100 transition-colors">Cancel</button>
          <button type="button" onClick={(e: React.MouseEvent) => handleSubmit(e as unknown as React.FormEvent)} disabled={mutation.isPending} className="px-4 py-2 text-sm font-medium rounded-lg text-white bg-primary hover:bg-primary-hover shadow-sm transition-colors disabled:opacity-50">
            {mutation.isPending ? "Creating..." : "Create Operator"}
          </button>
        </div>
      </div>
    </dialog>
  );
}

