"use client";

import * as React from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { Check, Eye, GitBranch, X } from "lucide-react";
import { toast } from "sonner";

import { AppShell } from "@/components/app-shell";
import { EmptyState, ErrorState, PageSkeleton } from "@/components/data-state";
import { PageHeader } from "@/components/page-header";
import { RoleGuard } from "@/components/role-guard";
import { StatusBadge } from "@/components/status-badge";
import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
  AlertDialogTrigger,
} from "@/components/ui/alert-dialog";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { Card, CardContent } from "@/components/ui/card";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from "@/components/ui/dialog";
import { Label } from "@/components/ui/label";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { Textarea } from "@/components/ui/textarea";
import { adminApi } from "@/lib/api";
import { formatDate, formatMoney } from "@/lib/format";
import { queryKeys } from "@/lib/query-keys";
import type { CourseVersion } from "@/lib/types";

type VersionSnapshot = {
  title?: string | null;
  subtitle?: string | null;
  description?: string | null;
  thumbnailUrl?: string | null;
  price?: number | string | null;
  level?: string | null;
  categories?: Array<{ id?: number; name?: string; slug?: string }>;
  requirements?: string[];
  outcomes?: string[];
  sections?: Array<{
    id?: number;
    title?: string | null;
    published?: boolean;
    lessons?: Array<{
      id?: number;
      title?: string | null;
      preview?: boolean;
      published?: boolean;
      durationSeconds?: number;
    }>;
  }>;
};

function parseVersionSnapshot(snapshotJson?: string | null): VersionSnapshot | null {
  if (!snapshotJson) {
    return null;
  }

  try {
    return JSON.parse(snapshotJson) as VersionSnapshot;
  } catch {
    return null;
  }
}

export function ReviewQueuePage() {
  const queryClient = useQueryClient();
  const [activeTab, setActiveTab] = React.useState("courses");

  // Fetch pending courses
  const pendingCourses = useQuery({
    queryKey: queryKeys.adminCourses("REVIEWING"),
    queryFn: () => adminApi.courses({ status: "REVIEWING", page: 0, size: 100 }),
  });

  // Fetch pending course versions
  const pendingVersions = useQuery({
    queryKey: queryKeys.adminCourseVersions,
    queryFn: () => adminApi.courseVersions({ page: 0, size: 100 }),
  });

  const approveCourse = useMutation({
    mutationFn: (id: number) => adminApi.approveCourse(id),
    onSuccess: () => {
      toast.success("Course approved and published");
      queryClient.invalidateQueries({ queryKey: queryKeys.adminCoursesRoot });
      queryClient.invalidateQueries({ queryKey: queryKeys.adminDashboard });
      queryClient.invalidateQueries({ queryKey: queryKeys.adminRevenue });
      queryClient.invalidateQueries({ queryKey: queryKeys.auditLogsRoot });
    },
    onError: (error) => toast.error((error as Error).message),
  });

  const rejectCourse = useMutation({
    mutationFn: ({ id, reason }: { id: number; reason: string }) => adminApi.rejectCourse(id, reason),
    onSuccess: () => {
      toast.success("Course review rejected");
      queryClient.invalidateQueries({ queryKey: queryKeys.adminCoursesRoot });
      queryClient.invalidateQueries({ queryKey: queryKeys.adminDashboard });
      queryClient.invalidateQueries({ queryKey: queryKeys.auditLogsRoot });
    },
    onError: (error) => toast.error((error as Error).message),
  });

  const approveVersion = useMutation({
    mutationFn: (version: CourseVersion) => adminApi.approveVersion(version.courseId, version.id),
    onSuccess: () => {
      toast.success("Course version approved");
      queryClient.invalidateQueries({ queryKey: queryKeys.adminCourseVersions });
      queryClient.invalidateQueries({ queryKey: queryKeys.adminCoursesRoot });
      queryClient.invalidateQueries({ queryKey: queryKeys.adminDashboard });
      queryClient.invalidateQueries({ queryKey: queryKeys.auditLogsRoot });
    },
    onError: (error) => toast.error((error as Error).message),
  });

  const rejectVersion = useMutation({
    mutationFn: ({ version, reason }: { version: CourseVersion; reason: string }) =>
      adminApi.rejectVersion(version.courseId, version.id, reason),
    onSuccess: () => {
      toast.success("Course version rejected");
      queryClient.invalidateQueries({ queryKey: queryKeys.adminCourseVersions });
      queryClient.invalidateQueries({ queryKey: queryKeys.adminCoursesRoot });
      queryClient.invalidateQueries({ queryKey: queryKeys.adminDashboard });
      queryClient.invalidateQueries({ queryKey: queryKeys.auditLogsRoot });
    },
    onError: (error) => toast.error((error as Error).message),
  });

  const courseList = pendingCourses.data?.content ?? [];
  const versionList = pendingVersions.data?.content ?? [];

  return (
    <RoleGuard roles={["ADMIN"]}>
      <AppShell>
        <PageHeader
          title="Review Queue"
          description="Moderate pending course submissions and new course versions."
        />

        <Tabs value={activeTab} onValueChange={setActiveTab} className="space-y-4">
          <TabsList>
            <TabsTrigger value="courses">
              Courses ({courseList.length})
            </TabsTrigger>
            <TabsTrigger value="versions">
              Versions ({versionList.length})
            </TabsTrigger>
          </TabsList>

          <TabsContent value="courses" className="space-y-4">
            {pendingCourses.isLoading ? <PageSkeleton rows={4} /> : null}
            {pendingCourses.isError ? (
              <ErrorState
                message={(pendingCourses.error as Error).message}
                onRetry={() => pendingCourses.refetch()}
              />
            ) : null}

            {pendingCourses.isSuccess && courseList.length === 0 ? (
              <EmptyState
                title="No courses to review"
                message="All submitted courses have been processed."
              />
            ) : null}

            {courseList.length > 0 ? (
              <Card>
                <Table>
                  <TableHeader>
                    <TableRow>
                      <TableHead>Course</TableHead>
                      <TableHead>Instructor</TableHead>
                      <TableHead className="text-right">Actions</TableHead>
                    </TableRow>
                  </TableHeader>
                  <TableBody>
                    {courseList.map((course) => (
                      <TableRow key={course.id}>
                        <TableCell>
                          <p className="font-medium">{course.title}</p>
                          {course.subtitle ? (
                            <p className="text-xs text-muted-foreground truncate max-w-[500px]">
                              {course.subtitle}
                            </p>
                          ) : null}
                        </TableCell>
                        <TableCell>{course.instructorName ?? course.instructorEmail}</TableCell>
                        <TableCell className="text-right">
                          <div className="flex justify-end gap-2">
                            <CoursePreviewDialog courseId={course.id} />
                            
                            <AlertDialog>
                              <AlertDialogTrigger asChild>
                                <Button size="sm" disabled={approveCourse.isPending}>
                                  <Check className="mr-2 h-4 w-4" />
                                  Approve
                                </Button>
                              </AlertDialogTrigger>
                              <AlertDialogContent>
                                <AlertDialogHeader>
                                  <AlertDialogTitle>Approve course?</AlertDialogTitle>
                                  <AlertDialogDescription>
                                    Approving &quot;{course.title}&quot; will publish it immediately to the public catalog.
                                  </AlertDialogDescription>
                                </AlertDialogHeader>
                                <AlertDialogFooter>
                                  <AlertDialogCancel>Cancel</AlertDialogCancel>
                                  <AlertDialogAction onClick={() => approveCourse.mutate(course.id)}>
                                    Approve and Publish
                                  </AlertDialogAction>
                                </AlertDialogFooter>
                              </AlertDialogContent>
                            </AlertDialog>

                            <RejectReasonDialog
                              title={`Reject "${course.title}"`}
                              description="Specify the reason for rejection. This will be shown to the instructor."
                              onConfirm={(reason) => rejectCourse.mutate({ id: course.id, reason })}
                              disabled={rejectCourse.isPending}
                            />
                          </div>
                        </TableCell>
                      </TableRow>
                    ))}
                  </TableBody>
                </Table>
              </Card>
            ) : null}
          </TabsContent>

          <TabsContent value="versions" className="space-y-4">
            {pendingVersions.isLoading ? <PageSkeleton rows={4} /> : null}
            {pendingVersions.isError ? (
              <ErrorState
                message={(pendingVersions.error as Error).message}
                onRetry={() => pendingVersions.refetch()}
              />
            ) : null}

            {pendingVersions.isSuccess && versionList.length === 0 ? (
              <EmptyState
                title="No versions to review"
                message="All version updates have been processed."
              />
            ) : null}

            {versionList.length > 0 ? (
              <div className="grid gap-4">
                {versionList.map((version) => (
                  <Card key={version.id}>
                    <CardContent className="flex flex-col justify-between gap-4 p-5 sm:flex-row sm:items-center">
                      <div className="space-y-1.5">
                        <div className="flex items-center gap-2">
                          <h4 className="font-semibold">{version.title ?? `Course #${version.courseId}`}</h4>
                          <StatusBadge status={version.status} />
                        </div>
                        <p className="text-xs text-muted-foreground">
                          Course ID: #{version.courseId} - Version: {version.versionNumber} - Submitted: {formatDate(version.updatedAt)}
                        </p>
                        {version.subtitle ? (
                          <p className="text-sm text-muted-foreground max-w-[600px]">{version.subtitle}</p>
                        ) : null}
                      </div>
                      <div className="flex items-center gap-2 shrink-0">
                        <VersionReviewDialog version={version} />

                        <AlertDialog>
                          <AlertDialogTrigger asChild>
                            <Button size="sm" disabled={approveVersion.isPending}>
                              <Check className="mr-2 h-4 w-4" />
                              Approve
                            </Button>
                          </AlertDialogTrigger>
                          <AlertDialogContent>
                            <AlertDialogHeader>
                              <AlertDialogTitle>Approve version {version.versionNumber}?</AlertDialogTitle>
                              <AlertDialogDescription>
                                This will apply version {version.versionNumber}&apos;s updates to the live published course.
                              </AlertDialogDescription>
                            </AlertDialogHeader>
                            <AlertDialogFooter>
                              <AlertDialogCancel>Cancel</AlertDialogCancel>
                              <AlertDialogAction onClick={() => approveVersion.mutate(version)}>
                                Apply updates
                              </AlertDialogAction>
                            </AlertDialogFooter>
                          </AlertDialogContent>
                        </AlertDialog>

                        <RejectReasonDialog
                          title={`Reject version ${version.versionNumber}`}
                          description="Describe why this version update is being rejected."
                          onConfirm={(reason) => rejectVersion.mutate({ version, reason })}
                          disabled={rejectVersion.isPending}
                        />
                      </div>
                    </CardContent>
                  </Card>
                ))}
              </div>
            ) : null}
          </TabsContent>
        </Tabs>
      </AppShell>
    </RoleGuard>
  );
}

// Dialog helper to preview course details
function CoursePreviewDialog({ courseId }: { courseId: number }) {
  const [open, setOpen] = React.useState(false);
  const detail = useQuery({
    queryKey: queryKeys.adminCourseDetail(courseId),
    queryFn: () => adminApi.courseDetail(courseId),
    enabled: open,
  });

  return (
    <Dialog open={open} onOpenChange={setOpen}>
      <DialogTrigger asChild>
        <Button size="sm" variant="outline">
          <Eye className="mr-2 h-4 w-4" />
          Preview
        </Button>
      </DialogTrigger>
      <DialogContent className="max-h-[85vh] overflow-y-auto sm:max-w-3xl">
        <DialogHeader>
          <DialogTitle>{detail.data?.course.title ?? `Course #${courseId}`}</DialogTitle>
          <DialogDescription>Review details and structure of the submitted course.</DialogDescription>
        </DialogHeader>

        {detail.isLoading ? <PageSkeleton rows={4} /> : null}
        {detail.isError ? (
          <ErrorState message={(detail.error as Error).message} onRetry={() => detail.refetch()} />
        ) : null}

        {detail.data ? (
          <div className="space-y-6">
            <div className="grid gap-2 rounded-lg border p-4 text-sm bg-muted/20">
              <div className="flex items-center gap-3">
                <StatusBadge status={detail.data.course.status} />
                <span className="text-muted-foreground">{detail.data.course.level}</span>
                <span className="font-semibold">
                  {formatMoney(detail.data.course.price, detail.data.course.currency)}
                </span>
              </div>
              {detail.data.course.subtitle ? (
                <p className="font-medium text-foreground">{detail.data.course.subtitle}</p>
              ) : null}
              {detail.data.course.description ? (
                <p className="text-muted-foreground whitespace-pre-wrap">{detail.data.course.description}</p>
              ) : null}
            </div>

            <div className="space-y-3">
              <h4 className="font-semibold text-sm">Curriculum structure</h4>
              {detail.data.sections.length === 0 ? (
                <p className="text-sm text-muted-foreground">No sections created yet.</p>
              ) : null}
              {detail.data.sections.map((section) => (
                <div key={section.id} className="rounded-lg border p-4">
                  <div className="mb-2 flex items-center justify-between">
                    <p className="font-medium">{section.title}</p>
                    <StatusBadge status={section.published ? "PUBLISHED" : "DRAFT"} />
                  </div>
                  <div className="grid gap-1.5 pl-4 border-l">
                    {section.lessons?.map((lesson) => (
                      <div key={lesson.id} className="flex items-center justify-between text-xs py-1">
                        <span>{lesson.title}</span>
                        <div className="flex flex-wrap items-center justify-end gap-1.5 text-muted-foreground">
                          {lesson.preview ? <Badge variant="outline">Preview</Badge> : null}
                          {lesson.quizId ? <Badge variant="secondary">Quiz #{lesson.quizId}</Badge> : null}
                          {lesson.assignmentId ? <Badge variant="secondary">Assignment #{lesson.assignmentId}</Badge> : null}
                          <StatusBadge status={lesson.published ? "PUBLISHED" : "DRAFT"} />
                        </div>
                      </div>
                    ))}
                    {section.lessons?.length === 0 ? (
                      <p className="text-xs text-muted-foreground">No lessons in this section.</p>
                    ) : null}
                  </div>
                </div>
              ))}
            </div>
          </div>
        ) : null}
      </DialogContent>
    </Dialog>
  );
}

// Dialog helper to require non-blank rejection reasons
function RejectReasonDialog({
  title,
  description,
  onConfirm,
  disabled,
}: {
  title: string;
  description: string;
  onConfirm: (reason: string) => void;
  disabled?: boolean;
}) {
  const [reason, setReason] = React.useState("");
  const [open, setOpen] = React.useState(false);

  const handleConfirm = () => {
    if (!reason.trim()) return;
    onConfirm(reason.trim());
    setOpen(false);
    setReason("");
  };

  return (
    <Dialog open={open} onOpenChange={setOpen}>
      <DialogTrigger asChild>
        <Button size="sm" variant="destructive">
          <X className="mr-2 h-4 w-4" />
          Reject
        </Button>
      </DialogTrigger>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>{title}</DialogTitle>
          <DialogDescription>{description}</DialogDescription>
        </DialogHeader>
        <div className="grid gap-2 py-2">
          <Label htmlFor="reject-reason">Reason for rejection</Label>
          <Textarea
            id="reject-reason"
            placeholder="e.g. Please provide high-quality video content..."
            value={reason}
            onChange={(e) => setReason(e.target.value)}
            rows={4}
          />
        </div>
        <DialogFooter>
          <Button variant="outline" onClick={() => setOpen(false)}>Cancel</Button>
          <Button
            variant="destructive"
            onClick={handleConfirm}
            disabled={!reason.trim() || disabled}
          >
            Confirm Rejection
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}

// Dialog helper to preview version changes and submitted snapshot context.
function VersionReviewDialog({ version }: { version: CourseVersion }) {
  const [open, setOpen] = React.useState(false);
  const snapshot = React.useMemo(() => parseVersionSnapshot(version.snapshotJson), [version.snapshotJson]);
  const detail = useQuery({
    queryKey: queryKeys.adminCourseDetail(version.courseId),
    queryFn: () => adminApi.courseDetail(version.courseId),
    enabled: open,
  });
  const parsedSnapshot: Record<string, unknown> = {
    title: `Current: ${detail.data?.course.title || "Not set"}\nProposed: ${version.title ?? snapshot?.title ?? "Not set"}`,
    subtitle: `Current: ${detail.data?.course.subtitle || "Not set"}\nProposed: ${version.subtitle ?? snapshot?.subtitle ?? "Not set"}`,
    description: `Current: ${detail.data?.course.description || "Not set"}\nProposed: ${version.description ?? snapshot?.description ?? "Not set"}`,
    thumbnailUrl: `Current: ${detail.data?.course.thumbnailUrl || "Not set"}\nProposed: ${version.thumbnailUrl ?? snapshot?.thumbnailUrl ?? "Not set"}`,
  };

  const fields = [
    { label: "Title", key: "title" },
    { label: "Subtitle", key: "subtitle" },
    { label: "Description", key: "description" },
    { label: "Thumbnail", key: "thumbnailUrl" },
  ];

  return (
    <Dialog open={open} onOpenChange={setOpen}>
      <DialogTrigger asChild>
        <Button size="sm" variant="outline">
          <GitBranch className="mr-2 h-4 w-4" />
          Review
        </Button>
      </DialogTrigger>
      <DialogContent className="max-h-[85vh] overflow-y-auto sm:max-w-4xl">
        <DialogHeader>
          <DialogTitle>Version {version.versionNumber} review</DialogTitle>
          <DialogDescription>
            Compare live course metadata with the proposed update and inspect the submitted snapshot.
          </DialogDescription>
        </DialogHeader>

        {detail.isLoading ? <PageSkeleton rows={3} /> : null}
        {detail.isError ? (
          <ErrorState message={(detail.error as Error).message} onRetry={() => detail.refetch()} />
        ) : null}

        <div className="space-y-4 py-2">
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead className="w-[120px]">Field</TableHead>
                <TableHead>Current vs proposed value</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {fields.map((f) => {
                const proposed = parsedSnapshot[f.key] !== undefined ? String(parsedSnapshot[f.key]) : "Not set";
                return (
                  <TableRow key={f.key}>
                    <TableCell className="font-semibold text-xs">{f.label}</TableCell>
                    <TableCell className="text-xs break-all whitespace-pre-wrap">{proposed}</TableCell>
                  </TableRow>
                );
              })}
            </TableBody>
          </Table>

          <div className="grid gap-4 md:grid-cols-2">
            <Card>
              <CardContent className="space-y-3 p-4">
                <h4 className="text-sm font-semibold">Snapshot summary</h4>
                <div className="grid gap-2 text-xs text-muted-foreground">
                  <p>
                    Categories:{" "}
                    {snapshot?.categories?.map((item) => item.name).filter(Boolean).join(", ") || "No categories"}
                  </p>
                  <p>Requirements: {snapshot?.requirements?.length ?? 0}</p>
                  <p>Outcomes: {snapshot?.outcomes?.length ?? 0}</p>
                  <p>Sections: {snapshot?.sections?.length ?? 0}</p>
                </div>
              </CardContent>
            </Card>
            <Card>
              <CardContent className="space-y-3 p-4">
                <h4 className="text-sm font-semibold">Review note</h4>
                <p className="text-xs leading-relaxed text-muted-foreground">
                  Backend v1 currently applies version changes to course metadata only:
                  title, subtitle, description, and thumbnail. Curriculum data is shown here as a review snapshot.
                </p>
              </CardContent>
            </Card>
          </div>

          {snapshot?.sections?.length ? (
            <div className="space-y-3">
              <h4 className="text-sm font-semibold">Snapshot curriculum</h4>
              <div className="grid gap-3">
                {snapshot.sections.map((section, index) => (
                  <div key={`${section.id ?? index}-${section.title ?? "section"}`} className="rounded-lg border p-4">
                    <div className="mb-2 flex items-center justify-between gap-3">
                      <p className="font-medium">{section.title || `Section ${index + 1}`}</p>
                      <StatusBadge status={section.published ? "PUBLISHED" : "DRAFT"} />
                    </div>
                    <div className="grid gap-1.5 border-l pl-4">
                      {(section.lessons ?? []).map((lesson, lessonIndex) => (
                        <div
                          key={`${lesson.id ?? lessonIndex}-${lesson.title ?? "lesson"}`}
                          className="flex items-center justify-between gap-3 py-1 text-xs"
                        >
                          <span className="min-w-0 truncate">{lesson.title || `Lesson ${lessonIndex + 1}`}</span>
                          <div className="flex items-center gap-2 text-muted-foreground">
                            <span>{lesson.preview ? "Preview" : "Protected"}</span>
                            <StatusBadge status={lesson.published ? "PUBLISHED" : "DRAFT"} />
                          </div>
                        </div>
                      ))}
                      {section.lessons?.length === 0 ? (
                        <p className="text-xs text-muted-foreground">No lessons in this section.</p>
                      ) : null}
                    </div>
                  </div>
                ))}
              </div>
            </div>
          ) : null}
        </div>
      </DialogContent>
    </Dialog>
  );
}
