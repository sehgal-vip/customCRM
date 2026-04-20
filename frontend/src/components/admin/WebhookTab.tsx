import { useState } from "react";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { Loader2, Copy, Key, Shield, Check, AlertTriangle } from "lucide-react";
import { adminApi } from "../../services/apiEndpoints";
import { cn } from "../../lib/utils";
import { formatDate } from "../../lib/formatters";
import type { WebhookKey } from "../../types";

export default function WebhookTab() {
  const queryClient = useQueryClient();
  const [newDescription, setNewDescription] = useState("");
  const [generatedKey, setGeneratedKey] = useState<string | null>(null);
  const [copied, setCopied] = useState(false);
  const [endpointCopied, setEndpointCopied] = useState(false);

  const { data: keys, isLoading } = useQuery({
    queryKey: ["webhook-keys"],
    queryFn: () => adminApi.listWebhookKeys(),
  });

  const generateMutation = useMutation({
    mutationFn: (description: string) => adminApi.generateKey(description),
    onSuccess: (data: { apiKey?: string }) => {
      queryClient.invalidateQueries({ queryKey: ["webhook-keys"] });
      setGeneratedKey(data.apiKey ?? null);
      setNewDescription("");
    },
  });

  const revokeMutation = useMutation({
    mutationFn: (id: number) => adminApi.revokeKey(id),
    onSuccess: () =>
      queryClient.invalidateQueries({ queryKey: ["webhook-keys"] }),
  });

  function copyToClipboard(text: string, type: "key" | "endpoint") {
    navigator.clipboard.writeText(text);
    if (type === "key") {
      setCopied(true);
      setTimeout(() => setCopied(false), 2000);
    } else {
      setEndpointCopied(true);
      setTimeout(() => setEndpointCopied(false), 2000);
    }
  }

  const endpointUrl = `${window.location.origin}/api/v1/webhook/leads`;

  return (
    <div className="space-y-6">
      <h3 className="text-sm font-semibold text-text-primary">
        Webhook Configuration
      </h3>

      {/* API Endpoint */}
      <div className="bg-white rounded-xl border border-border p-4 space-y-2">
        <label className="block text-[11px] font-medium uppercase tracking-wider text-text-muted">
          API Endpoint URL
        </label>
        <div className="flex items-center gap-2">
          <code className="flex-1 bg-slate-50 rounded-lg px-3 py-2 text-sm font-mono text-text-primary border border-border truncate">
            {endpointUrl}
          </code>
          <button
            type="button"
            onClick={() => copyToClipboard(endpointUrl, "endpoint")}
            className="shrink-0 p-2 rounded-lg border border-border text-text-muted hover:bg-slate-50 transition-colors"
          >
            {endpointCopied ? (
              <Check className="h-4 w-4 text-emerald-600" />
            ) : (
              <Copy className="h-4 w-4" />
            )}
          </button>
        </div>
      </div>

      {/* Rate Limit Info */}
      <div className="rounded-lg bg-blue-50 border border-blue-200 px-4 py-3 flex items-center gap-2">
        <Shield className="h-4 w-4 text-blue-600 shrink-0" />
        <p className="text-sm text-blue-700">
          Rate limit: <strong>100 requests per minute per key</strong>
        </p>
      </div>

      {/* Generated Key Alert */}
      {generatedKey && (
        <div className="rounded-lg bg-amber-50 border border-amber-200 p-4 space-y-2">
          <div className="flex items-center gap-2">
            <AlertTriangle className="h-4 w-4 text-amber-600 shrink-0" />
            <p className="text-sm font-medium text-amber-800">
              Copy this key now -- it will not be shown again
            </p>
          </div>
          <div className="flex items-center gap-2">
            <code className="flex-1 bg-white rounded-lg px-3 py-2 text-sm font-mono text-text-primary border border-amber-300 break-all">
              {generatedKey}
            </code>
            <button
              type="button"
              onClick={() => copyToClipboard(generatedKey, "key")}
              className="shrink-0 p-2 rounded-lg border border-amber-300 text-amber-700 hover:bg-amber-100 transition-colors"
            >
              {copied ? (
                <Check className="h-4 w-4 text-emerald-600" />
              ) : (
                <Copy className="h-4 w-4" />
              )}
            </button>
          </div>
        </div>
      )}

      {/* Generate New Key */}
      <div className="bg-white rounded-xl border border-border p-4 space-y-3">
        <h4 className="text-sm font-medium text-text-primary">
          Generate New API Key
        </h4>
        <div className="flex items-center gap-2">
          <input
            type="text"
            value={newDescription}
            onChange={(e) => setNewDescription(e.target.value)}
            placeholder="Key description (e.g., Production webhook)"
            className="flex-1 rounded-lg border border-border px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-primary/40 focus:border-primary"
          />
          <button
            type="button"
            onClick={() => generateMutation.mutate(newDescription.trim())}
            disabled={!newDescription.trim() || generateMutation.isPending}
            className="inline-flex items-center gap-1.5 px-4 py-2 text-sm font-medium rounded-lg bg-primary text-white hover:bg-primary-hover disabled:opacity-50 transition-colors shrink-0"
          >
            {generateMutation.isPending ? (
              <Loader2 className="h-4 w-4 animate-spin" />
            ) : (
              <Key className="h-4 w-4" />
            )}
            Generate Key
          </button>
        </div>
        {generateMutation.isError && (
          <div className="rounded-lg bg-red-50 border border-red-200 px-3 py-2 text-xs text-danger">
            Failed to generate key. {(generateMutation.error as Error)?.message || "Please try again."}
          </div>
        )}
      </div>

      {/* Existing Keys */}
      <div className="space-y-2">
        <h4 className="text-sm font-medium text-text-primary">API Keys</h4>
        {isLoading ? (
          <div className="flex justify-center py-6">
            <Loader2 className="h-6 w-6 animate-spin text-primary" />
          </div>
        ) : (
          <div className="space-y-2">
            {((keys as WebhookKey[] | undefined) ?? []).map((key) => (
              <div
                key={key.id}
                className={cn(
                  "bg-white rounded-xl border border-border p-4 flex items-center gap-4 flex-wrap",
                  !key.active && "opacity-50"
                )}
              >
                <code className="text-sm font-mono text-text-primary">
                  {key.keyPrefix}****
                </code>
                <span className="text-sm text-text-secondary flex-1 min-w-0 truncate">
                  {key.description}
                </span>
                <span
                  className={cn(
                    "px-2 py-0.5 rounded-full text-xs font-medium shrink-0",
                    key.active
                      ? "bg-emerald-50 text-emerald-700"
                      : "bg-red-50 text-red-700"
                  )}
                >
                  {key.active ? "Active" : "Revoked"}
                </span>
                <span className="text-xs text-text-muted shrink-0">
                  {formatDate(key.createdAt)}
                </span>
                {key.active && (
                  <button
                    type="button"
                    onClick={() => revokeMutation.mutate(key.id)}
                    disabled={revokeMutation.isPending}
                    className="text-xs font-medium text-danger hover:underline shrink-0"
                  >
                    Revoke
                  </button>
                )}
              </div>
            ))}
            {(keys ?? []).length === 0 && (
              <p className="text-sm text-text-muted py-4 text-center">
                No API keys generated yet.
              </p>
            )}
          </div>
        )}
      </div>

      {/* Payload Documentation */}
      <div className="bg-white rounded-xl border border-border p-4 space-y-3">
        <h4 className="text-sm font-medium text-text-primary">
          Payload Documentation
        </h4>
        <p className="text-xs text-text-muted">
          POST to the endpoint above with the following JSON body. Include the
          API key in the <code className="bg-slate-100 px-1 rounded">X-API-Key</code> header.
        </p>
        <pre className="bg-slate-50 rounded-lg p-4 text-xs font-mono text-text-secondary overflow-x-auto border border-border">
{`{
  "operatorName": "string (required)",
  "contactName": "string (required)",
  "contactPhone": "string (phone or email required)",
  "contactEmail": "string (phone or email required)",
  "leadSource": "string (required)",
  "fleetSize": "number (optional)",
  "region": "string (optional)",
  "notes": "string (optional)"
}`}
        </pre>
      </div>
    </div>
  );
}
