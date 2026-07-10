export function formatMoney(value?: number | string | null, currency = "VND") {
  if (value === undefined || value === null || value === "") {
    return "Free";
  }
  const amount = Number(value);
  if (!Number.isFinite(amount) || amount <= 0) {
    return "Free";
  }
  return new Intl.NumberFormat("vi-VN", {
    style: "currency",
    currency,
    maximumFractionDigits: 0,
  }).format(amount);
}

export function formatDuration(totalSeconds?: number | null) {
  const seconds = Number(totalSeconds ?? 0);
  if (!seconds) {
    return "0 min";
  }
  const hours = Math.floor(seconds / 3600);
  const minutes = Math.round((seconds % 3600) / 60);
  if (hours <= 0) {
    return `${minutes} min`;
  }
  return `${hours}h ${minutes}m`;
}

export function formatDate(value?: string | null) {
  if (!value) {
    return "Not set";
  }
  return new Intl.DateTimeFormat("vi-VN", {
    dateStyle: "medium",
    timeStyle: "short",
  }).format(new Date(value));
}

export function percent(value?: number | null) {
  return `${Math.round(Number(value ?? 0))}%`;
}

export function formatRating(value?: number | string | null) {
  if (value === undefined || value === null || value === "") {
    return "New";
  }
  const num = Number(value);
  if (!Number.isFinite(num) || num <= 0) {
    return "New";
  }
  return num.toFixed(1);
}

export function formatRelativeTime(value?: string | null) {
  if (!value) {
    return "";
  }
  const date = new Date(value);
  const now = new Date();
  const diffMs = now.getTime() - date.getTime();
  const diffMins = Math.floor(diffMs / 60000);
  if (diffMins < 1) return "Just now";
  if (diffMins < 60) return `${diffMins}m ago`;
  const diffHours = Math.floor(diffMins / 60);
  if (diffHours < 24) return `${diffHours}h ago`;
  const diffDays = Math.floor(diffHours / 24);
  if (diffDays < 30) return `${diffDays}d ago`;
  return formatDate(value);
}


