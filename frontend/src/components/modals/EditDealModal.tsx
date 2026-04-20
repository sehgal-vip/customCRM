import { useEffect, useRef } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { X, Loader2 } from "lucide-react";
import { toast } from "sonner";
import { dealsApi, operatorsApi, usersApi } from "../../services/apiEndpoints";
import { cn, getErrorMessage } from "../../lib/utils";
import type { Deal } from "../../types";
import { useState } from "react";

interface EditDealModalProps {
  open: boolean;
  onClose: () => void;
  deal: Deal;
}

export default function EditDealModal({ open, onClose, deal }: EditDealModalProps) {
  const dialogRef = useRef<HTMLDialogElement>(null);
  const queryClient = useQueryClient();

  const [assignedAgentId, setAssignedAgentId] = useState<number>(deal.assignedAgent?.id || 0);
  const [operatorId, setOperatorId] = useState<number>(deal.operator?.id || 0);

  const { data: users } = useQuery({
    queryKey: ["users"],
    queryFn: () => usersApi.list(),
    enabled: open,
  });

  const { data: operatorsPage } = useQuery({
    queryKey: ["operators"],
    queryFn: () => operatorsApi.list({ size: "200" }),
    enabled: open,
  });

  const operators = operatorsPage?.content ?? [];

  const mutation = useMutation({
    mutationFn: () => dealsApi.update(deal.id, { assignedAgentId, operatorId }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["deal", deal.id] });
      queryClient.invalidateQueries({ queryKey: ["deals"] });
      toast.success("Deal updated");
      onClose();
    },
    onError: (err: unknown) => {
      toast.error(getErrorMessage(err, "Failed to update deal"));
    },
  });

  // Render-time sync from deal prop when modal opens
  const [prevDealKey, setPrevDealKey] = useState<string | null>(null);
  const dealKey = open ? `${deal.id}-${deal.assignedAgent?.id}-${deal.operator?.id}` : null;
  if (dealKey && dealKey !== prevDealKey) {
    setPrevDealKey(dealKey);
    setAssignedAgentId(deal.assignedAgent?.id || 0);
    setOperatorId(deal.operator?.id || 0);
  }
  if (!open && prevDealKey) {
    setPrevDealKey(null);
  }

  useEffect(() => {
    const dlg = dialogRef.current;
    if (!dlg) return;
    if (open && !dlg.open) dlg.showModal();
    else if (!open && dlg.open) dlg.close();
  }, [open]);

  if (!open) return null;

  return (
    <dialog
      ref={dialogRef}
      onCancel={onClose}
      className="backdrop:bg-black/50 backdrop:backdrop-blur-sm bg-transparent p-0 m-auto rounded-2xl max-w-md w-full"
    >
      <div className="bg-white rounded-2xl shadow-[0_25px_50px_rgba(0,0,0,0.15)] flex flex-col">
        {/* Header */}
        <div className="flex items-center justify-between px-6 py-4 border-b border-border">
          <h2 className="text-lg font-semibold text-text-primary">Edit Deal</h2>
          <button
            type="button"
            aria-label="Close"
            onClick={onClose}
            className="p-1 rounded-lg hover:bg-slate-100 text-text-muted transition-colors"
          >
            <X className="h-5 w-5" />
          </button>
        </div>

        {/* Body */}
        <div className="px-6 py-5 space-y-4">
          {/* Reassign Agent */}
          <div>
            <label className="block text-[11px] font-medium uppercase tracking-wider text-text-muted mb-1">
              Assigned Agent
            </label>
            <select
              value={assignedAgentId}
              onChange={(e) => setAssignedAgentId(parseInt(e.target.value, 10))}
              autoFocus
              className="w-full rounded-lg border border-border px-3 py-2 text-sm text-text-primary bg-white focus:outline-none focus:ring-2 focus:ring-primary/40 focus:border-primary"
            >
              {(users ?? []).map((u) => (
                <option key={u.id} value={u.id}>
                  {u.name}{u.role === "MANAGER" ? " [Manager]" : ""}
                </option>
              ))}
            </select>
          </div>

          {/* Change Operator */}
          <div>
            <label className="block text-[11px] font-medium uppercase tracking-wider text-text-muted mb-1">
              Operator
            </label>
            <select
              value={operatorId}
              onChange={(e) => setOperatorId(parseInt(e.target.value, 10))}
              className="w-full rounded-lg border border-border px-3 py-2 text-sm text-text-primary bg-white focus:outline-none focus:ring-2 focus:ring-primary/40 focus:border-primary"
            >
              {operators.map((op) => (
                <option key={op.id} value={op.id}>
                  {op.companyName}
                </option>
              ))}
            </select>
          </div>

          <p className="text-xs text-text-muted italic">Changes will be audited.</p>

          {/* Error */}
          {mutation.isError && (
            <div className="rounded-lg bg-red-50 border border-red-200 px-4 py-3 text-sm text-danger">
              Failed to update deal. Please try again.
            </div>
          )}
        </div>

        {/* Footer */}
        <div className="flex items-center justify-end gap-3 px-6 py-4 border-t border-border">
          <button
            type="button"
            onClick={onClose}
            className="px-4 py-2 text-sm rounded-lg text-text-secondary hover:bg-slate-100 transition-colors"
          >
            Cancel
          </button>
          <button
            type="button"
            onClick={() => mutation.mutate()}
            disabled={mutation.isPending}
            className={cn(
              "px-4 py-2 text-sm font-medium rounded-lg text-white shadow-sm transition-colors",
              "bg-primary hover:bg-primary-hover",
              "disabled:opacity-50 disabled:cursor-not-allowed"
            )}
          >
            {mutation.isPending ? (
              <span className="inline-flex items-center gap-2">
                <Loader2 className="h-4 w-4 animate-spin" />
                Saving...
              </span>
            ) : (
              "Save Changes"
            )}
          </button>
        </div>
      </div>
    </dialog>
  );
}
