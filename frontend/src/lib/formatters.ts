export function formatINR(amount: number | null | undefined): string {
  if (amount == null) return "\u2014";
  return new Intl.NumberFormat("en-IN", {
    style: "currency",
    currency: "INR",
    maximumFractionDigits: 0,
  }).format(amount);
}

const LOCAL_TIMEZONE = Intl.DateTimeFormat().resolvedOptions().timeZone;

export function formatDate(date: string | null | undefined): string {
  if (!date) return "\u2014";
  return new Date(date).toLocaleDateString("en-IN", {
    day: "numeric",
    month: "short",
    year: "numeric",
    timeZone: LOCAL_TIMEZONE,
  });
}

export function timeAgo(date: string): string {
  const seconds = Math.floor((Date.now() - new Date(date).getTime()) / 1000);
  if (seconds < 60) return "just now";
  const minutes = Math.floor(seconds / 60);
  if (minutes < 60) return `${minutes}m ago`;
  const hours = Math.floor(minutes / 60);
  if (hours < 24) return `${hours}h ago`;
  const days = Math.floor(hours / 24);
  return `${days}d ago`;
}

/** Format a date string to a localized date+time with timezone abbreviation */
export function formatDateTime(date: string | null | undefined): string {
  if (!date) return "\u2014";
  try {
    const formatted = new Date(date).toLocaleDateString("en-IN", {
      day: "numeric",
      month: "short",
      year: "numeric",
      hour: "2-digit",
      minute: "2-digit",
      timeZone: LOCAL_TIMEZONE,
    });
    const tzAbbr = new Date(date).toLocaleTimeString("en-IN", {
      timeZoneName: "short",
      timeZone: LOCAL_TIMEZONE,
    }).split(" ").pop();
    return `${formatted} ${tzAbbr}`;
  } catch {
    return date ?? "\u2014";
  }
}

export function renderFieldValue(value: unknown): string {
  if (value === null || value === undefined) return "-";
  if (typeof value === "boolean") return value ? "Yes" : "No";
  if (Array.isArray(value)) return value.join(", ");
  return String(value);
}

export function formatEnum(val?: string | null): string {
  if (!val) return "—";
  return val.split("_").map(w => w.charAt(0) + w.slice(1).toLowerCase()).join(" ");
}
