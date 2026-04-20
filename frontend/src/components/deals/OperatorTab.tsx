import { useState } from "react";
import { Loader2, Phone, Mail, Bus, Route, MapPin, Building2, AlertTriangle, Plus } from "lucide-react";
import type { Operator, Contact } from "../../types";
import { formatEnum } from "../../lib/formatters";
import ContactDetailPopup from "../shared/ContactDetailPopup";
import AddContactModal from "../shared/AddContactModal";

interface OperatorTabProps {
  operator: Operator | undefined;
  isLoading: boolean;
}

export default function OperatorTab({ operator, isLoading }: OperatorTabProps) {
  const [selectedContact, setSelectedContact] = useState<Contact | null>(null);
  const [addContactOpen, setAddContactOpen] = useState(false);

  if (isLoading) {
    return (
      <div className="flex items-center justify-center py-12">
        <Loader2 className="h-6 w-6 animate-spin text-primary" />
      </div>
    );
  }

  if (!operator) {
    return (
      <div className="py-12 text-center text-sm text-text-muted">
        No operator data available.
      </div>
    );
  }

  return (
    <div className="space-y-5">
      {/* Info Grid */}
      <div className="grid grid-cols-2 md:grid-cols-4 gap-3">
        <InfoCard
          label="Company Name"
          value={operator.companyName}
          icon={<Building2 className="h-3.5 w-3.5 text-text-muted" />}
        />
        <InfoCard
          label="Phone"
          value={operator.phone}
          icon={<Phone className="h-3.5 w-3.5 text-text-muted" />}
        />
        <InfoCard
          label="Email"
          value={operator.email}
          icon={<Mail className="h-3.5 w-3.5 text-text-muted" />}
        />
        <InfoCard
          label="Region"
          value={operator.region?.name}
          icon={<MapPin className="h-3.5 w-3.5 text-text-muted" />}
        />
        <InfoCard
          label="Type"
          value={formatEnum(operator.operatorType)}
        />
        <InfoCard
          label="Fleet Size"
          value={operator.fleetSize != null ? `${operator.fleetSize} buses` : undefined}
          icon={<Bus className="h-3.5 w-3.5 text-text-muted" />}
        />
        <InfoCard
          label="Routes"
          value={operator.numRoutes != null ? `${operator.numRoutes}` : undefined}
          icon={<Route className="h-3.5 w-3.5 text-text-muted" />}
        />
        <InfoCard
          label="Use Case"
          value={formatEnum(operator.primaryUseCase)}
        />
      </div>

      {/* Contacts */}
      <div className="space-y-3">
        <div className="flex items-center justify-between">
          <h3 className="text-sm font-semibold text-text-primary">Contacts</h3>
          <button
            type="button"
            onClick={() => setAddContactOpen(true)}
            className="inline-flex items-center gap-1 px-2.5 py-1 text-xs font-medium rounded-lg text-white bg-primary hover:bg-primary-hover transition-colors"
          >
            <Plus className="h-3.5 w-3.5" />
            Add Contact
          </button>
        </div>
        {operator.contacts.length === 0 ? (
          <p className="text-sm text-text-muted">No contacts added yet.</p>
        ) : (
          <div className="space-y-2">
            {operator.contacts.map((contact) => (
              <div
                key={contact.id}
                className="cursor-pointer hover:shadow-[0_4px_6px_rgba(0,0,0,0.07)] transition-shadow rounded-xl"
                onClick={() => setSelectedContact(contact)}
              >
                <ContactCard contact={contact} />
              </div>
            ))}
          </div>
        )}
      </div>

      {/* Contact modals */}
      <ContactDetailPopup
        contact={selectedContact}
        operatorId={operator.id}
        open={!!selectedContact}
        onClose={() => setSelectedContact(null)}
      />
      <AddContactModal
        operatorId={operator.id}
        open={addContactOpen}
        onClose={() => setAddContactOpen(false)}
      />
    </div>
  );
}

function InfoCard({
  label,
  value,
  icon,
}: {
  label: string;
  value?: string | null;
  icon?: React.ReactNode;
}) {
  return (
    <div className="bg-white rounded-xl border border-border p-3 shadow-[0_1px_2px_rgba(0,0,0,0.05)]">
      <p className="text-[10px] font-medium uppercase tracking-wider text-text-muted mb-1">
        {label}
      </p>
      <div className="flex items-center gap-1.5">
        {icon}
        <p className="text-sm font-semibold text-text-primary truncate">
          {value || "--"}
        </p>
      </div>
    </div>
  );
}

function ContactCard({ contact }: { contact: Contact }) {
  const initials = contact.name
    .split(" ")
    .map((n) => n[0])
    .join("")
    .slice(0, 2);

  return (
    <div className="bg-white rounded-xl border border-border p-4 flex items-start gap-3">
      <div className="w-10 h-10 rounded-full bg-slate-200 flex items-center justify-center text-sm font-semibold text-slate-600 shrink-0">
        {initials}
      </div>
      <div className="flex-1 min-w-0">
        <div className="flex items-center gap-2">
          <p className="text-sm font-medium text-text-primary">{contact.name}</p>
          {contact.incomplete && (
            <span className="px-1.5 py-0.5 rounded-full text-[10px] font-medium bg-amber-100 text-amber-700 flex items-center gap-0.5">
              <AlertTriangle className="h-3 w-3" />
              Incomplete
            </span>
          )}
        </div>
        <p className="text-xs text-text-muted">{formatEnum(contact.role)}</p>
        <div className="flex flex-col gap-0.5 mt-1.5">
          {contact.mobile && (
            <span className="flex items-center gap-1 text-xs text-text-secondary">
              <Phone className="h-3 w-3 text-text-muted" />
              {contact.mobile}
            </span>
          )}
          {contact.email && (
            <span className="flex items-center gap-1 text-xs text-text-secondary">
              <Mail className="h-3 w-3 text-text-muted" />
              {contact.email}
            </span>
          )}
        </div>
      </div>
    </div>
  );
}
