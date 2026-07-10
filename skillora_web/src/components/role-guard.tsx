"use client";

import { useQuery } from "@tanstack/react-query";
import { ShieldAlert } from "lucide-react";

import { PageSkeleton } from "@/components/data-state";
import { authApi } from "@/lib/api";
import { queryKeys } from "@/lib/query-keys";
import type { UserRole } from "@/lib/types";

type RoleGuardProps = {
  roles: UserRole[];
  children: React.ReactNode;
  fallback?: React.ReactNode;
};

export function RoleGuard({ roles, children, fallback }: RoleGuardProps) {
  const { data: user, isLoading } = useQuery({
    queryKey: queryKeys.me,
    queryFn: () => authApi.me(),
    retry: false,
  });

  if (isLoading) {
    return <PageSkeleton rows={4} />;
  }

  const hasRole = user?.roles?.some((role) => roles.includes(role));

  if (!hasRole) {
    return fallback ?? (
      <div className="flex min-h-[50vh] flex-col items-center justify-center gap-4 text-center">
        <div className="rounded-full border bg-muted p-4">
          <ShieldAlert className="h-8 w-8 text-muted-foreground" />
        </div>
        <div className="space-y-2">
          <h2 className="text-lg font-semibold">Access denied</h2>
          <p className="text-sm text-muted-foreground">
            You don&apos;t have permission to view this page.
          </p>
        </div>
      </div>
    );
  }

  return <>{children}</>;
}
