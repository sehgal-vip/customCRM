import { useState, useRef, useEffect } from "react";
import { useMutation, useQueryClient } from "@tanstack/react-query";
import { X, Loader2 } from "lucide-react";
import { operatorsApi } from "../../services/apiEndpoints";
import { CONTACT_ROLES, CONTACT_ROLE_VALUES } from "../../lib/constants";
import { cn } from "../../lib/utils";

interface AddContactModalProps {
  operatorId: number;
  open: boolean;
  onClose: () => void;
}

export default function AddContactModal({
  operatorId,
  open,
  onClose,
}: AddContactModalProps) {
  const dialogRef = useRef<HTMLDialogElement>(null);
  const queryClient = useQueryClient();

  const [name, setName] = useState("");
  const [selectedRole, setSelectedRole] = useState<string>(CONTACT_ROLES[0]);
  const [mobile, setMobile] = useState("");
  const [email, setEmail] = useState("");
  const [formError, setFormError] = useState("");

  // Render-time reset when modal closes
  const [wasOpen, setWasOpen] = useState(false);
  if (open && !wasOpen) {
    setWasOpen(true);
  }
  if (!open && wasOpen) {
    setWasOpen(false);
    setName("");
    setSelectedRole(CONTACT_ROLES[0]);
    setMobile("");
    setEmail("");
    setFormError("");
  }

  // Dialog open/close
  useEffect(() => {
    const dlg = dialogRef.current;
    if (!dlg) return;
    if (open && !dlg.open) {
      dlg.showModal();
    } else if (!open && dlg.open) {
      dlg.close();
    }
  }, [open]);

  const mutation = useMutation({
    mutationFn: (data: Record<string, unknown>) =>
      operatorsApi.addContact(operatorId, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["operator", operatorId] });
      queryClient.invalidateQueries({ queryKey: ["operators"] });
      onClose();
    },
  });

  const handleSubmit = () => {
    if (!name.trim()) {
      setFormError("Name is required.");
      return;
    }
    if (!mobile.trim() && !email.trim()) {
      setFormError("At least one of mobile or email is required.");
      return;
    }
    setFormError("");
    mutation.mutate({
      name: name.trim(),
      role: CONTACT_ROLE_VALUES[selectedRole] || selectedRole,
      mobile: mobile.trim() || null,
      email: email.trim() || null,
    });
  };

  if (!open) return null;

  return (
    <dialog
      ref={dialogRef}
      onCancel={onClose}
      className="backdrop:bg-black/50 backdrop:backdrop-blur-sm bg-transparent p-0 m-auto rounded-2xl max-w-md w-full"
    >
      <div className="bg-white rounded-2xl shadow-[0_25px_50px_rgba(0,0,0,0.15)] max-h-[90vh] flex flex-col">
        {/* Header */}
        <div className="flex items-center justify-between px-6 py-4 border-b border-border">
          <h2 className="text-lg font-semibold text-text-primary">
            Add Contact
          </h2>
          <button
            type="button"
            onClick={onClose}
            className="p-1 rounded-lg hover:bg-slate-100 text-text-muted transition-colors"
          >
            <X className="h-5 w-5" />
          </button>
        </div>

        {/* Body */}
        <div className="flex-1 overflow-y-auto px-6 py-5 space-y-4">
          {/* Name */}
          <div>
            <label className="block text-[11px] font-medium uppercase tracking-wider text-text-muted mb-1">
              Name <span className="text-danger">*</span>
            </label>
            <input
              value={name}
              onChange={(e) => setName(e.target.value)}
              className="w-full rounded-lg border border-border px-3 py-2 text-sm text-text-primary focus:outline-none focus:ring-2 focus:ring-primary/40 focus:border-primary"
              placeholder="Contact name"
            />
          </div>

          {/* Role */}
          <div>
            <label className="block text-[11px] font-medium uppercase tracking-wider text-text-muted mb-1">
              Role
            </label>
            <select
              value={selectedRole}
              onChange={(e) => setSelectedRole(e.target.value)}
              className="w-full rounded-lg border border-border px-3 py-2 text-sm text-text-primary bg-white focus:outline-none focus:ring-2 focus:ring-primary/40 focus:border-primary"
            >
              {CONTACT_ROLES.map((r) => (
                <option key={r} value={r}>
                  {r}
                </option>
              ))}
            </select>
          </div>

          {/* Mobile */}
          <div>
            <label className="block text-[11px] font-medium uppercase tracking-wider text-text-muted mb-1">
              Mobile
            </label>
            <input
              value={mobile}
              onChange={(e) => setMobile(e.target.value)}
              className="w-full rounded-lg border border-border px-3 py-2 text-sm text-text-primary focus:outline-none focus:ring-2 focus:ring-primary/40 focus:border-primary"
              placeholder="Mobile number"
            />
          </div>

          {/* Email */}
          <div>
            <label className="block text-[11px] font-medium uppercase tracking-wider text-text-muted mb-1">
              Email
            </label>
            <input
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              type="email"
              className="w-full rounded-lg border border-border px-3 py-2 text-sm text-text-primary focus:outline-none focus:ring-2 focus:ring-primary/40 focus:border-primary"
              placeholder="Email address"
            />
          </div>

          <p className="text-xs text-text-muted">
            At least one of mobile or email is required.
          </p>

          {formError && (
            <div className="rounded-lg bg-red-50 border border-red-200 px-4 py-3 text-sm text-danger">
              {formError}
            </div>
          )}

          {mutation.isError && (
            <div className="rounded-lg bg-red-50 border border-red-200 px-4 py-3 text-sm text-danger">
              Failed to add contact. Please try again.
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
            onClick={handleSubmit}
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
                Adding...
              </span>
            ) : (
              "Add Contact"
            )}
          </button>
        </div>
      </div>
    </dialog>
  );
}
