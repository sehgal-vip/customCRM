import { useState, useRef, useEffect, useMemo } from "react";
import { useMutation, useQueryClient } from "@tanstack/react-query";
import { X, Pencil, AlertTriangle, Phone, Mail, Loader2 } from "lucide-react";
import { operatorsApi } from "../../services/apiEndpoints";
import { CONTACT_ROLES, CONTACT_ROLE_VALUES } from "../../lib/constants";
import type { Contact } from "../../types";
import { cn } from "../../lib/utils";

// Reverse map: "OWNER" -> "Owner"
const ROLE_DISPLAY: Record<string, string> = Object.fromEntries(
  Object.entries(CONTACT_ROLE_VALUES).map(([label, value]) => [value, label])
);

interface ContactDetailPopupProps {
  contact: Contact | null;
  operatorId: number;
  open: boolean;
  onClose: () => void;
}

export default function ContactDetailPopup({
  contact,
  operatorId,
  open,
  onClose,
}: ContactDetailPopupProps) {
  const dialogRef = useRef<HTMLDialogElement>(null);
  const queryClient = useQueryClient();
  const [editing, setEditing] = useState(false);

  // Derive initial form values from contact prop
  const contactDefaults = useMemo(() => ({
    name: contact?.name ?? "",
    role: contact ? (ROLE_DISPLAY[contact.role] || contact.role) : CONTACT_ROLES[0],
    mobile: contact?.mobile ?? "",
    email: contact?.email ?? "",
  }), [contact]);

  // Form state
  const [name, setName] = useState(contactDefaults.name);
  const [role, setRole] = useState<string>(contactDefaults.role);
  const [mobile, setMobile] = useState(contactDefaults.mobile);
  const [email, setEmail] = useState(contactDefaults.email);
  const [formError, setFormError] = useState("");

  // Reset form when contact changes (new contact selected)
  const [prevContactId, setPrevContactId] = useState(contact?.id);
  if (contact?.id !== prevContactId) {
    setPrevContactId(contact?.id);
    setName(contactDefaults.name);
    setRole(contactDefaults.role);
    setMobile(contactDefaults.mobile);
    setEmail(contactDefaults.email);
    setFormError("");
  }

  // Render-time reset when modal closes
  const [wasOpen, setWasOpen] = useState(false);
  if (open && !wasOpen) {
    setWasOpen(true);
  }
  if (!open && wasOpen) {
    setWasOpen(false);
    setEditing(false);
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
    mutationFn: (data: { name: string; role: string; mobile: string | null; email: string | null }) =>
      operatorsApi.updateContact(operatorId, contact!.id, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["operator", operatorId] });
      queryClient.invalidateQueries({ queryKey: ["operators"] });
      setEditing(false);
    },
  });

  const handleSave = () => {
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
      role: CONTACT_ROLE_VALUES[role] || role,
      mobile: mobile.trim() || null,
      email: email.trim() || null,
    });
  };

  const handleCancel = () => {
    if (contact) {
      setName(contact.name);
      setRole(ROLE_DISPLAY[contact.role] || contact.role);
      setMobile(contact.mobile || "");
      setEmail(contact.email || "");
    }
    setFormError("");
    setEditing(false);
  };

  if (!open || !contact) return null;

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
            Contact Details
          </h2>
          <div className="flex items-center gap-1">
            {!editing && (
              <button
                type="button"
                onClick={() => setEditing(true)}
                className="p-1.5 rounded-lg hover:bg-slate-100 text-text-muted transition-colors"
                title="Edit"
                aria-label="Edit contact"
              >
                <Pencil className="h-4 w-4" />
              </button>
            )}
            <button
              type="button"
              aria-label="Close"
              onClick={onClose}
              className="p-1 rounded-lg hover:bg-slate-100 text-text-muted transition-colors"
            >
              <X className="h-5 w-5" />
            </button>
          </div>
        </div>

        {/* Body */}
        <div className="flex-1 overflow-y-auto px-6 py-5 space-y-4">
          {editing ? (
            <>
              {/* Name */}
              <div>
                <label className="block text-[11px] font-medium uppercase tracking-wider text-text-muted mb-1">
                  Name
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
                  value={role}
                  onChange={(e) => setRole(e.target.value)}
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
                  Failed to update contact. Please try again.
                </div>
              )}
            </>
          ) : (
            <>
              {/* View mode */}
              <p className="text-base font-semibold text-text-primary">
                {contact.name}
              </p>

              <span className="inline-block px-2 py-0.5 rounded-full text-xs font-medium bg-blue-50 text-blue-700">
                {ROLE_DISPLAY[contact.role] || contact.role}
              </span>

              <div className="space-y-2 mt-2">
                <div className="flex items-center gap-2 text-sm">
                  <Phone className="h-4 w-4 text-text-muted" />
                  {contact.mobile ? (
                    <span className="text-text-primary">{contact.mobile}</span>
                  ) : (
                    <span className="text-amber-600">(not set)</span>
                  )}
                </div>
                <div className="flex items-center gap-2 text-sm">
                  <Mail className="h-4 w-4 text-text-muted" />
                  {contact.email ? (
                    <span className="text-text-primary">{contact.email}</span>
                  ) : (
                    <span className="text-amber-600">(not set)</span>
                  )}
                </div>
              </div>

              {contact.incomplete && (
                <div className="flex items-center gap-1.5 text-amber-700 text-xs mt-2">
                  <AlertTriangle className="h-3.5 w-3.5" />
                  Contact information is incomplete
                </div>
              )}
            </>
          )}
        </div>

        {/* Footer */}
        <div className="flex items-center justify-end gap-3 px-6 py-4 border-t border-border">
          {editing ? (
            <>
              <button
                type="button"
                onClick={handleCancel}
                className="px-4 py-2 text-sm rounded-lg text-text-secondary hover:bg-slate-100 transition-colors"
              >
                Cancel
              </button>
              <button
                type="button"
                onClick={handleSave}
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
            </>
          ) : (
            <button
              type="button"
              onClick={onClose}
              className="px-4 py-2 text-sm rounded-lg text-text-secondary hover:bg-slate-100 transition-colors"
            >
              Close
            </button>
          )}
        </div>
      </div>
    </dialog>
  );
}
