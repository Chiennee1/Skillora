import { Badge } from "@/components/ui/badge";
import { cn } from "@/lib/utils";

const toneByStatus: Record<string, string> = {
  ACTIVE: "border-emerald-500/30 bg-emerald-500/10 text-emerald-700 dark:text-emerald-300",
  COMPLETED: "border-teal-500/30 bg-teal-500/10 text-teal-700 dark:text-teal-300",
  PUBLISHED: "border-emerald-500/30 bg-emerald-500/10 text-emerald-700 dark:text-emerald-300",
  REVIEWING: "border-amber-500/30 bg-amber-500/10 text-amber-700 dark:text-amber-300",
  PENDING: "border-amber-500/30 bg-amber-500/10 text-amber-700 dark:text-amber-300",
  DRAFT: "border-zinc-500/30 bg-zinc-500/10 text-zinc-700 dark:text-zinc-300",
  REJECTED: "border-red-500/30 bg-red-500/10 text-red-700 dark:text-red-300",
  FAILED: "border-red-500/30 bg-red-500/10 text-red-700 dark:text-red-300",
  CANCELLED: "border-zinc-500/30 bg-zinc-500/10 text-zinc-700 dark:text-zinc-300",
  PAID: "border-emerald-500/30 bg-emerald-500/10 text-emerald-700 dark:text-emerald-300",
  SUBMITTED: "border-blue-500/30 bg-blue-500/10 text-blue-700 dark:text-blue-300",
  GRADED: "border-emerald-500/30 bg-emerald-500/10 text-emerald-700 dark:text-emerald-300",
  UNSUBMITTED: "border-zinc-500/30 bg-zinc-500/10 text-zinc-700 dark:text-zinc-300",
};

export function StatusBadge({ status, className }: { status?: string | null; className?: string }) {
  const value = status ?? "UNKNOWN";
  return (
    <Badge variant="outline" className={cn("font-mono text-[11px]", toneByStatus[value] ?? "", className)}>
      {value}
    </Badge>
  );
}
