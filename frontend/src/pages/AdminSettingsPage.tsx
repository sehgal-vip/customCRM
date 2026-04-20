import { useState } from "react";
import { cn } from "../lib/utils";
import { useAuth } from "../context/AuthContext";
import TeamTab from "../components/admin/TeamTab";
import StagesTab from "../components/admin/StagesTab";
import TaxonomiesTab from "../components/admin/TaxonomiesTab";
import ChecklistTab from "../components/admin/ChecklistTab";
import RegionsTab from "../components/admin/RegionsTab";
import NotificationsTab from "../components/admin/NotificationsTab";
import WebhookTab from "../components/admin/WebhookTab";

const TABS = [
  { key: "team", label: "Team" },
  { key: "stages", label: "Stages" },
  { key: "taxonomies", label: "Taxonomies" },
  { key: "checklist", label: "Checklist" },
  { key: "regions", label: "Regions" },
  { key: "notifications", label: "Notifications" },
  { key: "webhook", label: "Webhook" },
] as const;

type TabKey = (typeof TABS)[number]["key"];

export default function AdminSettingsPage() {
  const { isManager } = useAuth();
  const [activeTab, setActiveTab] = useState<TabKey>("team");

  if (!isManager) {
    return (
      <div className="flex items-center justify-center py-12 text-center">
        <div>
          <p className="text-sm font-medium text-text-primary">Access Restricted</p>
          <p className="text-xs text-text-muted mt-1">Admin settings are only available to managers.</p>
        </div>
      </div>
    );
  }

  return (
    <div className="space-y-5">
      {/* Tab Navigation */}
      <div className="border-b border-border overflow-x-auto">
        <div className="flex gap-0 -mb-px min-w-max">
          {TABS.map((tab) => (
            <button
              key={tab.key}
              type="button"
              onClick={() => setActiveTab(tab.key)}
              className={cn(
                "px-4 py-2.5 text-sm font-medium border-b-2 transition-colors whitespace-nowrap",
                activeTab === tab.key
                  ? "border-primary text-primary"
                  : "border-transparent text-text-muted hover:text-text-secondary"
              )}
            >
              {tab.label}
            </button>
          ))}
        </div>
      </div>

      {/* Tab Content */}
      {activeTab === "team" && <TeamTab />}
      {activeTab === "stages" && <StagesTab />}
      {activeTab === "taxonomies" && <TaxonomiesTab />}
      {activeTab === "checklist" && <ChecklistTab />}
      {activeTab === "regions" && <RegionsTab />}
      {activeTab === "notifications" && <NotificationsTab />}
      {activeTab === "webhook" && <WebhookTab />}
    </div>
  );
}
