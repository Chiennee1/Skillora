"use client";

import * as React from "react";
import Link from "next/link";
import { useQuery } from "@tanstack/react-query";
import { CheckCircle2, FileCheck2, HelpCircle, Play } from "lucide-react";

import { AppShell } from "@/components/app-shell";
import { EmptyState, ErrorState, PageSkeleton } from "@/components/data-state";
import { PageHeader } from "@/components/page-header";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { courseApi, learnerApi } from "@/lib/api";
import { formatDuration } from "@/lib/format";
import { queryKeys } from "@/lib/query-keys";
import type { LessonProgress, LessonSummary } from "@/lib/types";

export function LearningProgressPage({ enrollmentId }: { enrollmentId: number }) {
  const progress = useQuery({
    queryKey: queryKeys.enrollmentProgress(enrollmentId),
    queryFn: () => learnerApi.enrollmentProgress(enrollmentId),
  });

  const enrollments = useQuery({
    queryKey: queryKeys.enrollments("me", { page: 0, size: 100 }),
    queryFn: () => learnerApi.enrollments({ page: 0, size: 100 }),
  });

  const enrollment = enrollments.data?.content.find((item) => item.id === enrollmentId);

  const sections = useQuery({
    queryKey: queryKeys.sections(enrollment?.courseId),
    queryFn: () => courseApi.getSections(enrollment!.courseId),
    enabled: Boolean(enrollment?.courseId),
    retry: false,
  });

  const lessons = React.useMemo(() => progress.data ?? [], [progress.data]);
  const progressByLesson = React.useMemo(() => {
    const map = new Map<number, LessonProgress>();
    lessons.forEach((item) => map.set(item.lessonId, item));
    return map;
  }, [lessons]);

  return (
    <AppShell>
      <PageHeader
        title="Course Progress"
        description="View lessons and check off completed modules."
      />

      {progress.isLoading || enrollments.isLoading ? <PageSkeleton rows={5} /> : null}
      {progress.isError || enrollments.isError ? (
        <ErrorState
          message={(progress.error as Error)?.message || (enrollments.error as Error)?.message}
          onRetry={() => {
            progress.refetch();
            enrollments.refetch();
          }}
        />
      ) : null}

      {progress.isSuccess && lessons.length === 0 ? (
        <EmptyState
          title="No lessons published"
          message="This course does not have any lessons available yet."
        />
      ) : null}

      {sections.data?.length ? (
        <div className="grid gap-4">
          {sections.data.map((section) => (
            <Card key={section.id}>
              <CardContent className="space-y-3 p-4">
                <div>
                  <h2 className="font-semibold">{section.title}</h2>
                  <p className="text-xs text-muted-foreground">
                    {(section.lessons ?? []).length} lesson{(section.lessons ?? []).length !== 1 ? "s" : ""}
                  </p>
                </div>
                <div className="grid gap-2">
                  {(section.lessons ?? []).map((lesson) => (
                    <LessonProgressRow
                      key={lesson.id}
                      enrollmentId={enrollmentId}
                      lesson={lesson}
                      progress={progressByLesson.get(lesson.id)}
                    />
                  ))}
                </div>
              </CardContent>
            </Card>
          ))}
        </div>
      ) : lessons.length > 0 ? (
        <div className="grid gap-3">
          {lessons.map((item) => (
            <Card key={item.lessonId}>
              <CardContent className="flex items-center justify-between gap-4 p-4">
                <div className="flex items-center gap-3">
                  <div className="rounded-md border bg-muted p-2 shrink-0">
                    {item.completed ? (
                      <CheckCircle2 className="h-4 w-4 text-primary" />
                    ) : (
                      <Play className="h-4 w-4 text-muted-foreground" />
                    )}
                  </div>
                  <div>
                    <p className="font-semibold text-sm sm:text-base">{item.lessonTitle}</p>
                    <p className="text-xs text-muted-foreground">
                      {formatDuration(item.totalDurationSeconds)}
                    </p>
                  </div>
                </div>
                <Button asChild size="sm" variant={item.completed ? "outline" : "default"}>
                  <Link href={`/learn/${enrollmentId}/lessons/${item.lessonId}`}>
                    {item.completed ? "Review" : "Start"}
                  </Link>
                </Button>
              </CardContent>
            </Card>
          ))}
        </div>
      ) : null}
    </AppShell>
  );
}

function LessonProgressRow({
  enrollmentId,
  lesson,
  progress,
}: {
  enrollmentId: number;
  lesson: LessonSummary;
  progress?: LessonProgress;
}) {
  const completed = Boolean(progress?.completed);

  return (
    <div className="flex flex-col gap-3 rounded-md border bg-card p-3 sm:flex-row sm:items-center sm:justify-between">
      <div className="flex min-w-0 items-center gap-3">
        <div className="rounded-md border bg-muted p-2 shrink-0">
          {completed ? (
            <CheckCircle2 className="h-4 w-4 text-primary" />
          ) : (
            <Play className="h-4 w-4 text-muted-foreground" />
          )}
        </div>
        <div className="min-w-0">
          <p className="truncate font-semibold text-sm sm:text-base">{lesson.title}</p>
          <p className="text-xs text-muted-foreground">
            {formatDuration(progress?.totalDurationSeconds ?? lesson.durationSeconds)}
          </p>
        </div>
      </div>
      <div className="flex flex-wrap gap-2 sm:justify-end">
        {lesson.quizId ? (
          <Button asChild size="sm" variant="outline">
            <Link href={`/quizzes/${lesson.quizId}`}>
              <HelpCircle className="mr-1.5 h-3.5 w-3.5" />
              Quiz
            </Link>
          </Button>
        ) : null}
        {lesson.assignmentId ? (
          <Button asChild size="sm" variant="outline">
            <Link href={`/assignments/${lesson.assignmentId}`}>
              <FileCheck2 className="mr-1.5 h-3.5 w-3.5" />
              Assignment
            </Link>
          </Button>
        ) : null}
        <Button asChild size="sm" variant={completed ? "outline" : "default"}>
          <Link href={`/learn/${enrollmentId}/lessons/${lesson.id}`}>
            {completed ? "Review" : "Start"}
          </Link>
        </Button>
      </div>
    </div>
  );
}
