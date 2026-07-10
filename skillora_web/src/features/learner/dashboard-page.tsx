"use client";

import * as React from "react";
import Link from "next/link";
import Image from "next/image";
import { useQuery } from "@tanstack/react-query";
import { Award, BookOpen, GraduationCap, Play, Trophy } from "lucide-react";
import { toast } from "sonner";

import { AppShell } from "@/components/app-shell";
import { EmptyState, ErrorState, PageSkeleton } from "@/components/data-state";
import { MetricCard } from "@/components/metric-card";
import { PageHeader } from "@/components/page-header";
import { StatusBadge } from "@/components/status-badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { Progress } from "@/components/ui/progress";
import { learnerApi } from "@/lib/api";
import { queryKeys } from "@/lib/query-keys";
import { percent } from "@/lib/format";


export function LearnerDashboardPage() {
  const dashboard = useQuery({
    queryKey: queryKeys.learningDashboard,
    queryFn: () => learnerApi.dashboard(),
  });

  const enrollments = useQuery({
    queryKey: queryKeys.enrollments("me"),
    queryFn: () => learnerApi.enrollments({ page: 0, size: 50 }),
  });

  const page = enrollments.data ?? {
    content: [],
    page: 0,
    size: 0,
    totalElements: 0,
    totalPages: 0,
    first: true,
    last: true,
  };

  const active =
    dashboard.data?.inProgress ?? page.content.filter((item) => item.status === "ACTIVE").length;
  const completed =
    dashboard.data?.completed ?? page.content.filter((item) => item.status === "COMPLETED").length;
  const totalEnrolled = dashboard.data?.totalEnrolled ?? page.totalElements;

  const handleDownloadCertificate = async (enrollmentId: number) => {
    try {
      const result = await learnerApi.certificate(enrollmentId);
      if (result.url) {
        window.open(result.url, "_blank");
      } else {
        toast.error("Certificate is not ready or has no download URL");
      }
    } catch (error) {
      toast.error("Failed to fetch certificate: " + (error as Error).message);
    }
  };

  return (
    <AppShell>
      <PageHeader
        title="Learning Dashboard"
        description="Track your course completions and certificate achievements."
      />

      {enrollments.isLoading || dashboard.isLoading ? <PageSkeleton rows={4} /> : null}
      {enrollments.isError || dashboard.isError ? (
        <ErrorState
          message={(enrollments.error as Error)?.message || (dashboard.error as Error)?.message}
          onRetry={() => {
            enrollments.refetch();
            dashboard.refetch();
          }}
        />
      ) : null}

      {enrollments.isSuccess ? (
        <div className="space-y-6">
          <div className="grid gap-4 sm:grid-cols-3">
            <MetricCard title="Active Course(s)" value={active} icon={GraduationCap} />
            <MetricCard title="Completed Course(s)" value={completed} icon={Trophy} />
            <MetricCard title="Total Enrolled" value={totalEnrolled} icon={BookOpen} />
          </div>

          {page.content.length === 0 ? (
            <EmptyState
              title="No enrollments yet"
              message="Get started by selecting from our wide range of published courses."
              action={
                <Button asChild>
                  <Link href="/">Explore Catalog</Link>
                </Button>
              }
            />
          ) : (
            <div className="grid gap-4 lg:grid-cols-2">
              {page.content.map((enrollment, index) => (
                <Card key={enrollment.id} className="overflow-hidden">
                  <CardContent className="flex gap-4 p-4">
                    <div className="relative hidden aspect-video w-36 shrink-0 overflow-hidden rounded-md bg-muted sm:block">
                      {enrollment.courseThumbnailUrl ? (
                        <Image
                          src={enrollment.courseThumbnailUrl}
                          alt={enrollment.courseTitle ?? "Course"}
                          fill
                          sizes="144px"
                          priority={index < 2}
                          className="object-cover"
                        />
                      ) : (
                        <div className="flex h-full items-center justify-center text-muted-foreground">
                          <BookOpen className="h-6 w-6" />
                        </div>
                      )}
                    </div>
                    <div className="min-w-0 flex-1 space-y-3">
                      <div className="flex flex-wrap items-start justify-between gap-2">
                        <div className="min-w-0 flex-1">
                          <h3 className="font-semibold text-sm sm:text-base truncate">
                            {enrollment.courseTitle}
                          </h3>
                          <p className="text-xs text-muted-foreground">
                            {enrollment.instructorName ?? "Skillora Instructor"}
                          </p>
                        </div>
                        <StatusBadge status={enrollment.status} />
                      </div>

                      <div className="space-y-1">
                        <Progress value={Number(enrollment.progressPercent ?? 0)} />
                        <div className="flex items-center justify-between text-xs text-muted-foreground">
                          <span>{percent(Number(enrollment.progressPercent ?? 0))} complete</span>
                        </div>
                      </div>

                      <div className="flex items-center justify-between gap-2 pt-1">
                        {enrollment.status === "COMPLETED" ? (
                          <Button
                            variant="outline"
                            size="sm"
                            onClick={() => handleDownloadCertificate(enrollment.id)}
                          >
                            <Award className="mr-2 h-4 w-4" />
                            Certificate
                          </Button>
                        ) : (
                          <div />
                        )}
                        <Button asChild size="sm">
                          <Link href={`/learn/${enrollment.id}`}>
                            <Play className="mr-2 h-3.5 w-3.5" />
                            Resume
                          </Link>
                        </Button>
                      </div>
                    </div>
                  </CardContent>
                </Card>
              ))}
            </div>
          )}
        </div>
      ) : null}
    </AppShell>
  );
}
