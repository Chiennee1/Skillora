"use client";

import * as React from "react";
import Link from "next/link";
import { useQuery } from "@tanstack/react-query";
import { BookOpen, FolderOpen, GraduationCap, Info, Plus, Star } from "lucide-react";

import { AppShell } from "@/components/app-shell";
import { EmptyState, ErrorState, PageSkeleton } from "@/components/data-state";
import { MetricCard } from "@/components/metric-card";
import { PageHeader } from "@/components/page-header";
import { StatusBadge } from "@/components/status-badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { instructorApi } from "@/lib/api";
import { queryKeys } from "@/lib/query-keys";

export function InstructorHomePage() {
  const [activeTab, setActiveTab] = React.useState("ALL");

  const courses = useQuery({
    queryKey: queryKeys.instructorCourses(),
    queryFn: () => instructorApi.myCourses({ page: 0, size: 100 }),
  });

  const list = React.useMemo(() => courses.data?.content ?? [], [courses.data?.content]);

  // Filtered lists
  const filtered = React.useMemo(() => {
    if (activeTab === "ALL") return list;
    return list.filter((c) => c.status === activeTab);
  }, [list, activeTab]);

  // Metric stats
  const total = list.length;
  const published = list.filter((c) => c.status === "PUBLISHED").length;
  const reviewing = list.filter((c) => c.status === "REVIEWING").length;

  return (
    <AppShell>
      <PageHeader
        title="Instructor Workspace"
        description="Create, structure, and publish courses for Skillora learners."
        actions={
          <Button asChild>
            <Link href="/instructor/courses/new">
              <Plus className="mr-2 h-4 w-4" />
              New Course
            </Link>
          </Button>
        }
      />

      {courses.isLoading ? <PageSkeleton rows={4} /> : null}
      {courses.isError ? (
        <ErrorState
          message={(courses.error as Error).message}
          onRetry={() => courses.refetch()}
        />
      ) : null}

      {courses.isSuccess ? (
        <div className="space-y-6">
          <div className="grid gap-4 md:grid-cols-3">
            <MetricCard title="Total courses" value={total} detail="Drafts, reviews, and live courses" icon={FolderOpen} />
            <MetricCard title="Published" value={published} detail="Visible in the public catalog" icon={GraduationCap} />
            <MetricCard title="Pending review" value={reviewing} detail="Waiting for admin moderation" icon={BookOpen} />
          </div>

          {total === 0 ? (
            <EmptyState
              title="No courses created yet"
              message="Create a new draft course and construct its curriculum content."
              action={
                <Button asChild>
                  <Link href="/instructor/courses/new">Create Course</Link>
                </Button>
              }
            />
          ) : (
            <Tabs value={activeTab} onValueChange={setActiveTab} className="space-y-4">
              <TabsList className="flex flex-wrap h-auto gap-1">
                <TabsTrigger value="ALL">All ({list.length})</TabsTrigger>
                <TabsTrigger value="DRAFT">Drafts ({list.filter((c) => c.status === "DRAFT").length})</TabsTrigger>
                <TabsTrigger value="REVIEWING">Reviewing ({list.filter((c) => c.status === "REVIEWING").length})</TabsTrigger>
                <TabsTrigger value="PUBLISHED">Published ({list.filter((c) => c.status === "PUBLISHED").length})</TabsTrigger>
                <TabsTrigger value="REJECTED">Rejected ({list.filter((c) => c.status === "REJECTED").length})</TabsTrigger>
              </TabsList>

              <TabsContent value={activeTab} className="space-y-4">
                {filtered.length === 0 ? (
                  <p className="text-sm text-muted-foreground text-center py-8">
                    No courses match this status filter.
                  </p>
                ) : (
                  <div className="grid gap-4 sm:grid-cols-2 xl:grid-cols-3">
                    {filtered.map((course) => (
                      <Card key={course.id} className="flex flex-col justify-between overflow-hidden">
                        <CardContent className="p-4 space-y-3 flex-1 flex flex-col justify-between">
                          <div className="space-y-2">
                            <div className="flex justify-between items-start gap-2">
                              <h4 className="font-semibold text-sm sm:text-base line-clamp-2">
                                {course.title}
                              </h4>
                              <StatusBadge status={course.status} />
                            </div>
                            <p className="text-xs text-muted-foreground line-clamp-2">
                              {course.subtitle ?? "No subtitle added yet."}
                            </p>
                            {(course as Record<string, unknown>).rejectReason && course.status === "REJECTED" ? (
                              <div className="rounded bg-destructive/5 text-destructive border p-2 text-xs flex gap-1.5 items-start mt-1">
                                <Info className="h-3.5 w-3.5 shrink-0 mt-0.5" />
                                <span className="line-clamp-2">Reason: {String((course as Record<string, unknown>).rejectReason)}</span>
                              </div>
                            ) : null}
                          </div>

                          <div className="flex items-center justify-between text-xs text-muted-foreground pt-2">
                            <span className="flex items-center gap-1">
                              <Star className="h-3.5 w-3.5 text-primary fill-current" />
                              {course.avgRating ?? "New"}
                            </span>
                            <span>{course.totalEnrollments ?? 0} Enrolled</span>
                          </div>
                        </CardContent>
                        <div className="bg-muted/30 border-t p-3">
                          <Button asChild className="w-full" size="sm">
                            <Link href={`/instructor/courses/${course.id}`}>
                              {course.status === "DRAFT" || course.status === "REJECTED"
                                ? "Edit Builder"
                                : "View Details"}
                            </Link>
                          </Button>
                        </div>
                      </Card>
                    ))}
                  </div>
                )}
              </TabsContent>
            </Tabs>
          )}
        </div>
      ) : null}
    </AppShell>
  );
}
