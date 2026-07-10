"use client";

import * as React from "react";
import Link from "next/link";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import {
  Activity,
  ArrowRight,
  BookOpen,
  Check,
  CircleDollarSign,
  Eye,
  Plus,
  Search,
  UserCheck,
  UserX,
  Users,
  X,
} from "lucide-react";
import { toast } from "sonner";

import { AskAiButton } from "@/components/ask-ai-button";
import { AppShell } from "@/components/app-shell";
import { ConfirmActionDialog } from "@/components/confirm-action-dialog";
import { EmptyState, ErrorState, PageSkeleton } from "@/components/data-state";
import { MetricCard } from "@/components/metric-card";
import { PageHeader } from "@/components/page-header";
import { StatusBadge } from "@/components/status-badge";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
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
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { Textarea } from "@/components/ui/textarea";
import { adminApi } from "@/lib/api";
import { formatDate, formatMoney } from "@/lib/format";
import { queryKeys } from "@/lib/query-keys";
import type { AdminUser, AuditLog, Coupon } from "@/lib/types";

function useDebounce<T>(value: T, delay: number): T {
  const [debounced, setDebounced] = React.useState(value);
  React.useEffect(() => {
    const timer = setTimeout(() => setDebounced(value), delay);
    return () => clearTimeout(timer);
  }, [value, delay]);
  return debounced;
}

/* 1. Admin Dashboard Page */
export function AdminDashboardPage() {
  const stats = useQuery({
    queryKey: queryKeys.adminDashboard,
    queryFn: () => adminApi.dashboard(),
  });
  const revenue = useQuery({
    queryKey: queryKeys.adminRevenue,
    queryFn: () => adminApi.revenue(),
  });

  const dashboardData = stats.data;
  const revenueData = revenue.data;

  return (
    <AppShell>
      <PageHeader
        title="Admin Dashboard"
        description="Monitor general platform performance, audit details, and course reviews."
      />

      {stats.isLoading || revenue.isLoading ? <PageSkeleton rows={4} /> : null}
      {stats.isError || revenue.isError ? (
        <ErrorState
          message={(stats.error as Error)?.message || (revenue.error as Error)?.message}
          onRetry={() => {
            stats.refetch();
            revenue.refetch();
          }}
        />
      ) : null}

      {dashboardData ? (
        <div className="space-y-6">
          <div className="grid gap-4 md:grid-cols-2 xl:grid-cols-4">
            <MetricCard
              title="Total Users"
              value={dashboardData.users?.total ?? dashboardData.totalUsers ?? 0}
              detail={`${dashboardData.users?.active ?? 0} active accounts`}
              icon={Users}
            />
            <MetricCard
              title="Courses"
              value={dashboardData.courses?.total ?? dashboardData.totalCourses ?? 0}
              detail={`${dashboardData.courses?.reviewing ?? 0} pending review`}
              icon={BookOpen}
            />
            <MetricCard
              title="Enrollments"
              value={dashboardData.enrollments?.total ?? 0}
              detail={`${dashboardData.enrollments?.completed ?? 0} completions`}
              icon={Activity}
            />
            <MetricCard
              title="Total Earnings"
              value={formatMoney(revenueData?.totalRevenue ?? dashboardData.revenue?.totalRevenue, "VND")}
              detail={`${revenueData?.totalPaidOrders ?? dashboardData.revenue?.paidOrders ?? 0} paid checkouts`}
              icon={CircleDollarSign}
            />
          </div>

          <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
            <Button asChild variant="outline" className="h-20 flex-col gap-1 items-start p-4">
              <Link href="/admin/review-queue">
                <span className="font-semibold text-sm">Review Queue</span>
                <span className="text-xs text-muted-foreground flex items-center gap-1">
                  Moderate course versions <ArrowRight className="h-3 w-3" />
                </span>
              </Link>
            </Button>
            <Button asChild variant="outline" className="h-20 flex-col gap-1 items-start p-4">
              <Link href="/admin/users">
                <span className="font-semibold text-sm">Manage Users</span>
                <span className="text-xs text-muted-foreground flex items-center gap-1">
                  View and ban accounts <ArrowRight className="h-3 w-3" />
                </span>
              </Link>
            </Button>
            <Button asChild variant="outline" className="h-20 flex-col gap-1 items-start p-4">
              <Link href="/admin/coupons">
                <span className="font-semibold text-sm">Promotional Coupons</span>
                <span className="text-xs text-muted-foreground flex items-center gap-1">
                  Add checkout code offers <ArrowRight className="h-3 w-3" />
                </span>
              </Link>
            </Button>
            <Button asChild variant="outline" className="h-20 flex-col gap-1 items-start p-4">
              <Link href="/admin/categories">
                <span className="font-semibold text-sm">Categories</span>
                <span className="text-xs text-muted-foreground flex items-center gap-1">
                  Modify system categories <ArrowRight className="h-3 w-3" />
                </span>
              </Link>
            </Button>
          </div>
        </div>
      ) : null}
    </AppShell>
  );
}

/* 2. Admin Courses Page */
export function AdminCoursesPage() {
  const queryClient = useQueryClient();
  const [activeStatus, setActiveStatus] = React.useState("REVIEWING");

  const courses = useQuery({
    queryKey: queryKeys.adminCourses(activeStatus),
    queryFn: () =>
      adminApi.courses({
        status: activeStatus === "ALL" ? undefined : activeStatus,
        page: 0,
        size: 100,
      }),
  });

  const approve = useMutation({
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

  const reject = useMutation({
    mutationFn: ({ id, reason }: { id: number; reason: string }) =>
      adminApi.rejectCourse(id, reason),
    onSuccess: () => {
      toast.success("Course rejected");
      queryClient.invalidateQueries({ queryKey: queryKeys.adminCoursesRoot });
      queryClient.invalidateQueries({ queryKey: queryKeys.adminDashboard });
      queryClient.invalidateQueries({ queryKey: queryKeys.auditLogsRoot });
    },
    onError: (error) => toast.error((error as Error).message),
  });

  const page = courses.data ?? {
    content: [],
    page: 0,
    size: 0,
    totalElements: 0,
    totalPages: 0,
    first: true,
    last: true,
  };

  const list = page.content;

  return (
    <AppShell>
      <PageHeader
        title="All Courses"
        description="Filter and moderate draft, published, reviewing, or rejected courses."
      />

      <Tabs value={activeStatus} onValueChange={setActiveStatus} className="space-y-4">
        <TabsList>
          <TabsTrigger value="ALL">All</TabsTrigger>
          <TabsTrigger value="REVIEWING">Reviewing</TabsTrigger>
          <TabsTrigger value="PUBLISHED">Published</TabsTrigger>
          <TabsTrigger value="REJECTED">Rejected</TabsTrigger>
          <TabsTrigger value="DRAFT">Drafts</TabsTrigger>
        </TabsList>

        <TabsContent value={activeStatus} className="space-y-4">
          {courses.isLoading ? <PageSkeleton rows={5} /> : null}
          {courses.isError ? (
            <ErrorState
              message={(courses.error as Error).message}
              onRetry={() => courses.refetch()}
            />
          ) : null}

          {courses.isSuccess && list.length === 0 ? (
            <EmptyState title="No courses found" message="No courses match this status filter." />
          ) : null}

          {list.length > 0 ? (
            <Card>
              <Table>
                <TableHeader>
                  <TableRow>
                    <TableHead>Course</TableHead>
                    <TableHead>Instructor</TableHead>
                    <TableHead>Status</TableHead>
                    <TableHead className="text-right">Actions</TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {list.map((course) => (
                    <TableRow key={course.id}>
                      <TableCell>
                        <p className="font-semibold">{course.title}</p>
                        <p className="text-xs text-muted-foreground truncate max-w-[450px]">
                          {course.subtitle}
                        </p>
                      </TableCell>
                      <TableCell>{course.instructorName ?? course.instructorEmail}</TableCell>
                      <TableCell>
                        <StatusBadge status={course.status} />
                      </TableCell>
                      <TableCell className="text-right">
                        <div className="flex justify-end gap-2">
                          <CoursePreviewDialog courseId={course.id} />
                          {course.status === "REVIEWING" ? (
                            <>
                              <ConfirmActionDialog
                                title="Approve Course?"
                                description="Are you sure you want to approve this course? It will immediately become live in the catalog."
                                confirmLabel="Approve"
                                onConfirm={() => approve.mutate(course.id)}
                                disabled={approve.isPending}
                                trigger={
                                  <Button size="sm">
                                    <Check className="mr-1.5 h-4 w-4" />
                                    Approve
                                  </Button>
                                }
                              />
                              <RejectDialog
                                onConfirm={(reason) => reject.mutate({ id: course.id, reason })}
                                isSubmitting={reject.isPending}
                              />
                            </>
                          ) : null}
                        </div>
                      </TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            </Card>
          ) : null}
        </TabsContent>
      </Tabs>
    </AppShell>
  );
}

function RejectDialog({
  onConfirm,
  isSubmitting,
}: {
  onConfirm: (reason: string) => void;
  isSubmitting: boolean;
}) {
  const [reason, setReason] = React.useState("");
  const [open, setOpen] = React.useState(false);

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!reason.trim()) return;
    onConfirm(reason.trim());
    setOpen(false);
    setReason("");
  };

  return (
    <Dialog open={open} onOpenChange={setOpen}>
      <DialogTrigger asChild>
        <Button size="sm" variant="destructive">
          <X className="mr-1.5 h-4 w-4" />
          Reject
        </Button>
      </DialogTrigger>
      <DialogContent>
        <form onSubmit={handleSubmit}>
          <DialogHeader>
            <DialogTitle>Reject Course Submission</DialogTitle>
            <DialogDescription>
              Specify details describing why this course is rejected.
            </DialogDescription>
          </DialogHeader>
          <div className="grid gap-2 py-4">
            <Label htmlFor="reject-reason">Feedback / Reason</Label>
            <Textarea
              id="reject-reason"
              value={reason}
              onChange={(e) => setReason(e.target.value)}
              placeholder="e.g. Please record video lectures with better audio quality..."
              rows={4}
              required
            />
          </div>
          <DialogFooter>
            <Button type="button" variant="outline" onClick={() => setOpen(false)}>
              Cancel
            </Button>
            <Button type="submit" variant="destructive" disabled={isSubmitting}>
              Confirm Reject
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  );
}

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
          <Eye className="mr-1.5 h-4 w-4" />
          Preview
        </Button>
      </DialogTrigger>
      <DialogContent className="max-h-[85vh] overflow-y-auto sm:max-w-3xl">
        <DialogHeader>
          <DialogTitle>{detail.data?.course.title ?? `Course #${courseId}`}</DialogTitle>
          <DialogDescription>Review curriculum layout details and metadata.</DialogDescription>
        </DialogHeader>

        {detail.isLoading ? <PageSkeleton rows={4} /> : null}
        {detail.isError ? (
          <ErrorState message={(detail.error as Error).message} onRetry={() => detail.refetch()} />
        ) : null}

        {detail.data ? (
          <div className="space-y-6">
            <AskAiButton
              courseId={courseId}
              label="Ask AI to review this course"
              prompt={`Review the course "${detail.data.course.title}" for approval readiness. Focus on curriculum completeness, learner clarity, unpublished content flags, and policy risks. Do not include user, payment, or audit-log personal data.`}
              variant="outline"
            />

            <div className="grid gap-2 rounded-lg border p-4 text-sm bg-muted/10">
              <div className="flex items-center gap-3">
                <StatusBadge status={detail.data.course.status} />
                <span className="text-muted-foreground">{detail.data.course.level}</span>
                <span className="font-semibold">
                  {formatMoney(detail.data.course.price, detail.data.course.currency)}
                </span>
              </div>
              <p className="font-semibold">{detail.data.course.subtitle}</p>
              <p className="text-muted-foreground whitespace-pre-wrap">{detail.data.course.description}</p>
            </div>

            <div className="space-y-3">
              <h4 className="font-semibold text-sm">Curriculum Sections</h4>
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
                        <div className="flex flex-wrap items-center justify-end gap-1.5">
                          {lesson.preview ? <Badge variant="outline">Preview</Badge> : null}
                          {lesson.quizId ? <Badge variant="secondary">Quiz #{lesson.quizId}</Badge> : null}
                          {lesson.assignmentId ? <Badge variant="secondary">Assignment #{lesson.assignmentId}</Badge> : null}
                          <StatusBadge status={lesson.published ? "PUBLISHED" : "DRAFT"} />
                        </div>
                      </div>
                    ))}
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

/* 3. Admin Users Page */
export function AdminUsersPage() {
  const queryClient = useQueryClient();
  const [search, setSearch] = React.useState("");
  const [role, setRole] = React.useState("ALL");
  const [status, setStatus] = React.useState("ALL");
  const [pageNumber, setPageNumber] = React.useState(0);

  const debouncedSearch = useDebounce(search, 300);

  const users = useQuery({
    queryKey: queryKeys.adminUsers({ search: debouncedSearch, role, status, page: pageNumber }),
    queryFn: () =>
      adminApi.users({
        search: debouncedSearch || undefined,
        role: role === "ALL" ? undefined : role,
        status: status === "ALL" ? undefined : status,
        page: pageNumber,
        size: 20,
      }),
  });

  const toggleStatus = useMutation({
    mutationFn: ({ id, status }: { id: number; status: string }) =>
      adminApi.updateUserStatus(id, status),
    onSuccess: () => {
      toast.success("User status changed");
      queryClient.invalidateQueries({ queryKey: queryKeys.adminUsersRoot });
      queryClient.invalidateQueries({ queryKey: queryKeys.adminDashboard });
      queryClient.invalidateQueries({ queryKey: queryKeys.auditLogsRoot });
    },
    onError: (error) => toast.error((error as Error).message),
  });

  const page = users.data ?? {
    content: [],
    page: 0,
    size: 0,
    totalElements: 0,
    totalPages: 0,
    first: true,
    last: true,
  };

  const list = page.content;

  return (
    <AppShell>
      <PageHeader title="Users Management" description="Search users and ban or activate accounts." />

      <Card className="mb-6">
        <CardContent className="grid gap-4 p-5 sm:grid-cols-[1fr_200px_200px] items-end">
          <div className="grid gap-2">
            <Label htmlFor="userSearch">Search Users</Label>
            <div className="relative">
              <Search className="pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
              <Input
                id="userSearch"
                value={search}
                onChange={(e) => {
                  setSearch(e.target.value);
                  setPageNumber(0);
                }}
                placeholder="Name or email..."
                className="pl-9"
              />
            </div>
          </div>

          <div className="grid gap-2">
            <Label>Filter Role</Label>
            <Select value={role} onValueChange={(value) => { setRole(value); setPageNumber(0); }}>
              <SelectTrigger>
                <SelectValue />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="ALL">All Roles</SelectItem>
                <SelectItem value="STUDENT">Student</SelectItem>
                <SelectItem value="INSTRUCTOR">Instructor</SelectItem>
                <SelectItem value="ADMIN">Admin</SelectItem>
              </SelectContent>
            </Select>
          </div>

          <div className="grid gap-2">
            <Label>Filter Status</Label>
            <Select value={status} onValueChange={(value) => { setStatus(value); setPageNumber(0); }}>
              <SelectTrigger>
                <SelectValue />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="ALL">All Statuses</SelectItem>
                <SelectItem value="ACTIVE">Active</SelectItem>
                <SelectItem value="INACTIVE">Inactive</SelectItem>
                <SelectItem value="BANNED">Banned</SelectItem>
                <SelectItem value="DELETED">Deleted</SelectItem>
              </SelectContent>
            </Select>
          </div>
        </CardContent>
      </Card>

      {users.isLoading ? <PageSkeleton rows={5} /> : null}
      {users.isError ? (
        <ErrorState
          message={(users.error as Error).message}
          onRetry={() => users.refetch()}
        />
      ) : null}

      {users.isSuccess && list.length === 0 ? (
        <EmptyState title="No users found" message="No matches for the current query." />
      ) : null}

      {list.length > 0 ? (
        <Card>
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>User Details</TableHead>
                <TableHead>Assigned Roles</TableHead>
                <TableHead>Joined At</TableHead>
                <TableHead>Status</TableHead>
                <TableHead className="text-right">Actions</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {list.map((user) => {
                const isActive = user.status === "ACTIVE";
                return (
                  <TableRow key={user.id}>
                    <TableCell>
                      <p className="font-semibold text-sm sm:text-base">{user.fullName}</p>
                      <p className="text-xs text-muted-foreground">{user.email}</p>
                    </TableCell>
                    <TableCell>
                      <div className="flex flex-wrap gap-1">
                        {(user.roles ?? user.roleNames ?? []).map((r) => (
                          <Badge key={r} variant="secondary" className="text-[10px]">
                            {r}
                          </Badge>
                        ))}
                      </div>
                    </TableCell>
                    <TableCell className="text-muted-foreground text-xs">
                      {formatDate(user.createdAt)}
                    </TableCell>
                    <TableCell>
                      <StatusBadge status={user.status} />
                    </TableCell>
                    <TableCell className="text-right">
                      <div className="flex justify-end gap-2">
                        <UserDetailDialog userId={user.id} />
                        <ConfirmActionDialog
                          title={isActive ? "Ban User Account?" : "Activate User Account?"}
                          description={`Are you sure you want to ${isActive ? "ban" : "activate"} user "${user.fullName}"?`}
                          confirmLabel={isActive ? "Ban" : "Activate"}
                          variant={isActive ? "destructive" : "default"}
                          onConfirm={() =>
                            toggleStatus.mutate({
                              id: user.id,
                              status: isActive ? "BANNED" : "ACTIVE",
                            })
                          }
                          disabled={toggleStatus.isPending}
                          trigger={
                            <Button size="sm" variant={isActive ? "destructive" : "outline"}>
                              {isActive ? (
                                <>
                                  <UserX className="mr-1.5 h-3.5 w-3.5" />
                                  Ban
                                </>
                              ) : (
                                <>
                                  <UserCheck className="mr-1.5 h-3.5 w-3.5" />
                                  Activate
                                </>
                              )}
                            </Button>
                          }
                        />
                      </div>
                    </TableCell>
                  </TableRow>
                );
              })}
            </TableBody>
          </Table>
        </Card>
      ) : null}

      {page.totalPages > 1 ? (
        <div className="mt-4 flex items-center justify-between gap-3">
          <p className="text-sm text-muted-foreground">
            Page {page.page + 1} of {page.totalPages}
          </p>
          <div className="flex gap-2">
            <Button variant="outline" size="sm" disabled={page.first} onClick={() => setPageNumber((value) => Math.max(0, value - 1))}>
              Previous
            </Button>
            <Button variant="outline" size="sm" disabled={page.last} onClick={() => setPageNumber((value) => value + 1)}>
              Next
            </Button>
          </div>
        </div>
      ) : null}
    </AppShell>
  );
}

function UserDetailDialog({ userId }: { userId: number }) {
  const [open, setOpen] = React.useState(false);
  const user = useQuery({
    queryKey: ["admin-user-detail", userId],
    queryFn: () => adminApi.getUser(userId),
    enabled: open,
  });

  const data = user.data as AdminUser | undefined;

  return (
    <Dialog open={open} onOpenChange={setOpen}>
      <DialogTrigger asChild>
        <Button size="sm" variant="outline">
          <Eye className="mr-1.5 h-3.5 w-3.5" />
          Details
        </Button>
      </DialogTrigger>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>User Details</DialogTitle>
          <DialogDescription>Account metadata and current access state.</DialogDescription>
        </DialogHeader>
        {user.isLoading ? <PageSkeleton rows={3} /> : null}
        {user.isError ? <ErrorState message={(user.error as Error).message} onRetry={() => user.refetch()} /> : null}
        {data ? (
          <div className="grid gap-3 text-sm">
            <div className="flex items-center justify-between gap-3">
              <span className="text-muted-foreground">Name</span>
              <span className="font-medium">{data.fullName}</span>
            </div>
            <div className="flex items-center justify-between gap-3">
              <span className="text-muted-foreground">Email</span>
              <span className="font-medium">{data.email}</span>
            </div>
            <div className="flex items-center justify-between gap-3">
              <span className="text-muted-foreground">Status</span>
              <StatusBadge status={data.status} />
            </div>
            <div className="flex items-center justify-between gap-3">
              <span className="text-muted-foreground">Roles</span>
              <span className="font-medium">{(data.roles ?? data.roleNames ?? []).join(", ")}</span>
            </div>
            <div className="flex items-center justify-between gap-3">
              <span className="text-muted-foreground">Joined</span>
              <span>{formatDate(data.createdAt)}</span>
            </div>
            <div className="flex items-center justify-between gap-3">
              <span className="text-muted-foreground">Last login</span>
              <span>{formatDate(data.lastLoginAt)}</span>
            </div>
          </div>
        ) : null}
      </DialogContent>
    </Dialog>
  );
}

/* 4. Admin Coupons Page */
export function AdminCouponsPage() {
  const queryClient = useQueryClient();
  const [isCreateOpen, setIsCreateOpen] = React.useState(false);
  const [editingCoupon, setEditingCoupon] = React.useState<Coupon | null>(null);
  const [pageNumber, setPageNumber] = React.useState(0);

  // Form states
  const [code, setCode] = React.useState("");
  const [name, setName] = React.useState("");
  const [discountType, setDiscountType] = React.useState("PERCENT");
  const [discountValue, setDiscountValue] = React.useState("10");
  const [maxUses, setMaxUses] = React.useState("100");
  const [minOrderAmount, setMinOrderAmount] = React.useState("0");

  const coupons = useQuery({
    queryKey: ["admin-coupons", pageNumber],
    queryFn: () => adminApi.coupons({ page: pageNumber, size: 20 }),
  });

  const create = useMutation({
    mutationFn: (body: Record<string, unknown>) => adminApi.createCoupon(body),
    onSuccess: () => {
      toast.success("Coupon created successfully");
      setIsCreateOpen(false);
      queryClient.invalidateQueries({ queryKey: queryKeys.adminCoupons });
      queryClient.invalidateQueries({ queryKey: ["admin-coupons"] });
      queryClient.invalidateQueries({ queryKey: queryKeys.auditLogsRoot });
      resetForm();
    },
    onError: (error) => toast.error((error as Error).message),
  });

  const update = useMutation({
    mutationFn: ({ id, body }: { id: number; body: Record<string, unknown> }) =>
      adminApi.updateCoupon(id, body),
    onSuccess: () => {
      toast.success("Coupon metadata updated");
      setEditingCoupon(null);
      queryClient.invalidateQueries({ queryKey: queryKeys.adminCoupons });
      queryClient.invalidateQueries({ queryKey: ["admin-coupons"] });
      queryClient.invalidateQueries({ queryKey: queryKeys.auditLogsRoot });
      resetForm();
    },
    onError: (error) => toast.error((error as Error).message),
  });

  const deactivate = useMutation({
    mutationFn: (id: number) => adminApi.deactivateCoupon(id),
    onSuccess: () => {
      toast.success("Coupon deactivated");
      queryClient.invalidateQueries({ queryKey: queryKeys.adminCoupons });
      queryClient.invalidateQueries({ queryKey: ["admin-coupons"] });
      queryClient.invalidateQueries({ queryKey: queryKeys.auditLogsRoot });
    },
  });

  const resetForm = () => {
    setCode("");
    setName("");
    setDiscountType("PERCENT");
    setDiscountValue("10");
    setMaxUses("100");
    setMinOrderAmount("0");
  };

  const handleEditClick = (coupon: Coupon) => {
    setEditingCoupon(coupon);
    setCode(coupon.code);
    setName(coupon.name ?? "");
    setDiscountType(coupon.discountType ?? "PERCENT");
    setDiscountValue(String(coupon.discountValue ?? 0));
    setMaxUses(String(coupon.maxUses ?? 100));
    setMinOrderAmount(String(coupon.minOrderAmount ?? 0));
  };

  const handleCreateSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!code.trim() || !name.trim()) return;
    create.mutate({
      code: code.trim().toUpperCase(),
      name: name.trim(),
      discountType,
      discountValue: Number(discountValue),
      maxUses: Number(maxUses),
      minOrderAmount: Number(minOrderAmount),
    });
  };

  const handleUpdateSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!editingCoupon || !code.trim() || !name.trim()) return;
    update.mutate({
      id: editingCoupon.id,
      body: {
        code: code.trim().toUpperCase(),
        name: name.trim(),
        discountType,
        discountValue: Number(discountValue),
        maxUses: Number(maxUses),
        minOrderAmount: Number(minOrderAmount),
      },
    });
  };

  const page = coupons.data ?? {
    content: [],
    page: 0,
    size: 0,
    totalElements: 0,
    totalPages: 0,
    first: true,
    last: true,
  };

  const list = page.content;

  return (
    <AppShell>
      <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
        <PageHeader title="Promo Coupons" description="View system coupons and code discounts." />
        <Dialog open={isCreateOpen} onOpenChange={setIsCreateOpen}>
          <DialogTrigger asChild>
            <Button onClick={resetForm}>
              <Plus className="mr-2 h-4 w-4" />
              Add Coupon
            </Button>
          </DialogTrigger>
          <DialogContent className="sm:max-w-md">
            <form onSubmit={handleCreateSubmit}>
              <DialogHeader>
                <DialogTitle>Create Coupon</DialogTitle>
                <DialogDescription>Create a new promotional checkout discount code.</DialogDescription>
              </DialogHeader>
              <div className="grid gap-4 py-4 text-sm">
                <div className="grid gap-1.5">
                  <Label htmlFor="code">Code</Label>
                  <Input id="code" value={code} onChange={(e) => setCode(e.target.value.toUpperCase())} placeholder="e.g. SALE50" required />
                </div>
                <div className="grid gap-1.5">
                  <Label htmlFor="name">Friendly Name</Label>
                  <Input id="name" value={name} onChange={(e) => setName(e.target.value)} placeholder="e.g. Summer discount" required />
                </div>
                <div className="grid grid-cols-2 gap-3">
                  <div className="grid gap-1.5">
                    <Label>Discount Type</Label>
                    <Select value={discountType} onValueChange={setDiscountType}>
                      <SelectTrigger><SelectValue /></SelectTrigger>
                      <SelectContent>
                        <SelectItem value="PERCENT">Percentage (%)</SelectItem>
                        <SelectItem value="FIXED">Fixed Amount</SelectItem>
                      </SelectContent>
                    </Select>
                  </div>
                  <div className="grid gap-1.5">
                    <Label htmlFor="value">Discount Value</Label>
                    <Input id="value" type="number" min="0" value={discountValue} onChange={(e) => setDiscountValue(e.target.value)} required />
                  </div>
                </div>
                <div className="grid grid-cols-2 gap-3">
                  <div className="grid gap-1.5">
                    <Label htmlFor="maxUses">Max Uses</Label>
                    <Input id="maxUses" type="number" min="0" value={maxUses} onChange={(e) => setMaxUses(e.target.value)} />
                  </div>
                  <div className="grid gap-1.5">
                    <Label htmlFor="minOrder">Min Order VND</Label>
                    <Input id="minOrder" type="number" min="0" value={minOrderAmount} onChange={(e) => setMinOrderAmount(e.target.value)} />
                  </div>
                </div>
              </div>
              <DialogFooter>
                <Button type="button" variant="outline" onClick={() => setIsCreateOpen(false)}>Cancel</Button>
                <Button type="submit" disabled={create.isPending}>Create Coupon</Button>
              </DialogFooter>
            </form>
          </DialogContent>
        </Dialog>
      </div>

      {coupons.isLoading ? <PageSkeleton rows={4} /> : null}
      {coupons.isError ? (
        <ErrorState
          message={(coupons.error as Error).message}
          onRetry={() => coupons.refetch()}
        />
      ) : null}

      {list.length > 0 ? (
        <Card>
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>Coupon Code</TableHead>
                <TableHead>Discount Type / Value</TableHead>
                <TableHead>Usage Limits</TableHead>
                <TableHead>Min Spend</TableHead>
                <TableHead>Status</TableHead>
                <TableHead className="text-right">Actions</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {list.map((coupon) => (
                <TableRow key={coupon.id}>
                  <TableCell>
                    <span className="font-mono font-bold text-sm bg-muted px-2 py-1 rounded">
                      {coupon.code}
                    </span>
                    <p className="text-xs text-muted-foreground mt-1.5">{coupon.name}</p>
                  </TableCell>
                  <TableCell>
                    {coupon.discountValue ?? coupon.value} {coupon.discountType === "PERCENT" ? "%" : "VND"}
                  </TableCell>
                  <TableCell>
                    {coupon.usedCount ?? 0} / {coupon.maxUses ?? "Unlimited"}
                  </TableCell>
                  <TableCell>
                    {coupon.minOrderAmount ? formatMoney(coupon.minOrderAmount, "VND") : "None"}
                  </TableCell>
                  <TableCell>
                    <StatusBadge status={coupon.active === false ? "CANCELLED" : "ACTIVE"} />
                  </TableCell>
                  <TableCell className="text-right">
                    <div className="flex justify-end gap-2">
                      <Button size="sm" variant="outline" onClick={() => handleEditClick(coupon)}>
                        Edit
                      </Button>
                      <ConfirmActionDialog
                        title="Deactivate Coupon?"
                        description={`Are you sure you want to deactivate "${coupon.code}"?`}
                        confirmLabel="Deactivate"
                        variant="destructive"
                        onConfirm={() => deactivate.mutate(coupon.id)}
                        disabled={coupon.active === false || deactivate.isPending}
                        trigger={
                          <Button size="sm" variant="ghost" className="text-destructive hover:text-destructive" disabled={coupon.active === false}>
                            Disable
                          </Button>
                        }
                      />
                    </div>
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </Card>
      ) : null}

      {coupons.isSuccess && list.length === 0 ? (
        <EmptyState title="No coupons found" message="Create a promotional coupon to offer learners a discount." />
      ) : null}

      {page.totalPages > 1 ? (
        <div className="mt-4 flex items-center justify-between gap-3">
          <p className="text-sm text-muted-foreground">
            Page {page.page + 1} of {page.totalPages}
          </p>
          <div className="flex gap-2">
            <Button variant="outline" size="sm" disabled={page.first} onClick={() => setPageNumber((value) => Math.max(0, value - 1))}>
              Previous
            </Button>
            <Button variant="outline" size="sm" disabled={page.last} onClick={() => setPageNumber((value) => value + 1)}>
              Next
            </Button>
          </div>
        </div>
      ) : null}

      {/* Edit Coupon Dialog */}
      <Dialog
        open={editingCoupon !== null}
        onOpenChange={(open) => !open && setEditingCoupon(null)}
      >
        <DialogContent className="sm:max-w-md">
          {editingCoupon ? (
            <form onSubmit={handleUpdateSubmit}>
              <DialogHeader>
                <DialogTitle>Edit Coupon details</DialogTitle>
                <DialogDescription>Modify parameters for promotional code &quot;{editingCoupon.code}&quot;.</DialogDescription>
              </DialogHeader>
              <div className="grid gap-4 py-4 text-sm">
                <div className="grid gap-1.5">
                  <Label htmlFor="edit-name">Friendly Name</Label>
                  <Input id="edit-name" value={name} onChange={(e) => setName(e.target.value)} required />
                </div>
                <div className="grid grid-cols-2 gap-3">
                  <div className="grid gap-1.5">
                    <Label>Discount Type</Label>
                    <Select value={discountType} onValueChange={setDiscountType}>
                      <SelectTrigger><SelectValue /></SelectTrigger>
                      <SelectContent>
                        <SelectItem value="PERCENT">Percentage (%)</SelectItem>
                        <SelectItem value="FIXED">Fixed Amount</SelectItem>
                      </SelectContent>
                    </Select>
                  </div>
                  <div className="grid gap-1.5">
                    <Label htmlFor="edit-value">Discount Value</Label>
                    <Input id="edit-value" type="number" min="0" value={discountValue} onChange={(e) => setDiscountValue(e.target.value)} required />
                  </div>
                </div>
                <div className="grid grid-cols-2 gap-3">
                  <div className="grid gap-1.5">
                    <Label htmlFor="edit-maxUses">Max Uses</Label>
                    <Input id="edit-maxUses" type="number" min="0" value={maxUses} onChange={(e) => setMaxUses(e.target.value)} />
                  </div>
                  <div className="grid gap-1.5">
                    <Label htmlFor="edit-minOrder">Min Order VND</Label>
                    <Input id="edit-minOrder" type="number" min="0" value={minOrderAmount} onChange={(e) => setMinOrderAmount(e.target.value)} />
                  </div>
                </div>
              </div>
              <DialogFooter>
                <Button type="button" variant="outline" onClick={() => setEditingCoupon(null)}>Cancel</Button>
                <Button type="submit" disabled={update.isPending}>Save Changes</Button>
              </DialogFooter>
            </form>
          ) : null}
        </DialogContent>
      </Dialog>
    </AppShell>
  );
}

/* 5. Audit Logs Page */
export function AuditLogsPage() {
  const [selectedLog, setSelectedLog] = React.useState<AuditLog | null>(null);
  const [entityType, setEntityType] = React.useState("");
  const [action, setAction] = React.useState("");
  const [actorId, setActorId] = React.useState("");
  const [from, setFrom] = React.useState("");
  const [to, setTo] = React.useState("");
  const [pageNumber, setPageNumber] = React.useState(0);
  const filters = {
    entityType: entityType.trim() || undefined,
    action: action.trim() || undefined,
    actorId: actorId ? Number(actorId) : undefined,
    from: from || undefined,
    to: to || undefined,
    page: pageNumber,
    size: 20,
  };
  const logs = useQuery({
    queryKey: queryKeys.auditLogs(filters),
    queryFn: () => adminApi.auditLogs(filters),
  });

  const page = logs.data ?? {
    content: [],
    page: 0,
    size: 0,
    totalElements: 0,
    totalPages: 0,
    first: true,
    last: true,
  };

  const list = page.content;

  return (
    <AppShell>
      <PageHeader title="Audit Logs" description="Track security configurations, updates, and moderation actions." />
      <Card className="mb-4">
        <CardContent className="grid gap-3 p-4 md:grid-cols-[1fr_1fr_120px_1fr_1fr_auto] items-end">
          <div className="grid gap-1.5">
            <Label htmlFor="audit-entity">Entity Type</Label>
            <Input id="audit-entity" value={entityType} onChange={(e) => { setEntityType(e.target.value); setPageNumber(0); }} placeholder="COURSE" />
          </div>
          <div className="grid gap-1.5">
            <Label htmlFor="audit-action">Action</Label>
            <Input id="audit-action" value={action} onChange={(e) => { setAction(e.target.value); setPageNumber(0); }} placeholder="APPROVE_COURSE" />
          </div>
          <div className="grid gap-1.5">
            <Label htmlFor="audit-actor">Actor ID</Label>
            <Input id="audit-actor" type="number" min="1" value={actorId} onChange={(e) => { setActorId(e.target.value); setPageNumber(0); }} />
          </div>
          <div className="grid gap-1.5">
            <Label htmlFor="audit-from">From</Label>
            <Input id="audit-from" type="datetime-local" value={from} onChange={(e) => { setFrom(e.target.value); setPageNumber(0); }} />
          </div>
          <div className="grid gap-1.5">
            <Label htmlFor="audit-to">To</Label>
            <Input id="audit-to" type="datetime-local" value={to} onChange={(e) => { setTo(e.target.value); setPageNumber(0); }} />
          </div>
          <Button
            variant="outline"
            onClick={() => {
              setEntityType("");
              setAction("");
              setActorId("");
              setFrom("");
              setTo("");
              setPageNumber(0);
            }}
          >
            Reset
          </Button>
        </CardContent>
      </Card>
      {logs.isLoading ? <PageSkeleton rows={5} /> : null}
      {logs.isError ? (
        <ErrorState
          message={(logs.error as Error).message}
          onRetry={() => logs.refetch()}
        />
      ) : null}

      {list.length === 0 && logs.isSuccess ? (
        <EmptyState title="No audit logs recorded" message="Admin actions will show up here." />
      ) : null}

      {list.length > 0 ? (
        <div className="space-y-4">
          <Card>
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Action Log</TableHead>
                  <TableHead>Resource ID</TableHead>
                  <TableHead>Actor</TableHead>
                  <TableHead>IP Address</TableHead>
                  <TableHead>Created At</TableHead>
                  <TableHead className="text-right">Preview</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {list.map((log) => (
                  <TableRow key={log.id}>
                    <TableCell className="font-semibold text-xs">{log.action}</TableCell>
                    <TableCell>
                      {log.entityType} #{log.entityId}
                    </TableCell>
                    <TableCell className="text-xs">{log.actorEmail ?? `Actor #${log.actorId}`}</TableCell>
                    <TableCell className="font-mono text-xs text-muted-foreground">{log.ipAddress}</TableCell>
                    <TableCell className="text-xs text-muted-foreground">{formatDate(log.createdAt)}</TableCell>
                    <TableCell className="text-right">
                      <Button size="sm" variant="ghost" onClick={() => setSelectedLog(log)}>
                        Details
                      </Button>
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </Card>
        </div>
      ) : null}

      {page.totalPages > 1 ? (
        <div className="mt-4 flex items-center justify-between gap-3">
          <p className="text-sm text-muted-foreground">
            Page {page.page + 1} of {page.totalPages}
          </p>
          <div className="flex gap-2">
            <Button variant="outline" size="sm" disabled={page.first} onClick={() => setPageNumber((value) => Math.max(0, value - 1))}>
              Previous
            </Button>
            <Button variant="outline" size="sm" disabled={page.last} onClick={() => setPageNumber((value) => value + 1)}>
              Next
            </Button>
          </div>
        </div>
      ) : null}

      {/* Details drawer/dialog for old and new JSON audit differences */}
      <Dialog
        open={selectedLog !== null}
        onOpenChange={(open) => !open && setSelectedLog(null)}
      >
        <DialogContent className="max-w-xl max-h-[80vh] overflow-y-auto">
          {selectedLog ? (
            <div className="space-y-4">
              <DialogHeader>
                <DialogTitle>Audit Action Log Details</DialogTitle>
                <DialogDescription>
                  Details for action log #{selectedLog.id}
                </DialogDescription>
              </DialogHeader>
              <div className="space-y-3 text-xs leading-relaxed">
                <div>
                  <span className="font-semibold text-muted-foreground block mb-0.5">Action Code</span>
                  <span>{selectedLog.action}</span>
                </div>
                <div className="grid grid-cols-2 gap-4">
                  <div>
                    <span className="font-semibold text-muted-foreground block mb-0.5">Target Type</span>
                    <span>{selectedLog.entityType}</span>
                  </div>
                  <div>
                    <span className="font-semibold text-muted-foreground block mb-0.5">Target ID</span>
                    <span>#{selectedLog.entityId}</span>
                  </div>
                </div>
                {selectedLog.oldValues ? (
                  <div className="space-y-1">
                    <span className="font-semibold text-muted-foreground block">Old Properties JSON</span>
                    <pre className="rounded bg-muted p-3 font-mono text-[10px] break-all whitespace-pre-wrap overflow-x-auto">
                      {selectedLog.oldValues}
                    </pre>
                  </div>
                ) : null}
                {selectedLog.newValues ? (
                  <div className="space-y-1">
                    <span className="font-semibold text-muted-foreground block">New Properties JSON</span>
                    <pre className="rounded bg-muted p-3 font-mono text-[10px] break-all whitespace-pre-wrap overflow-x-auto">
                      {selectedLog.newValues}
                    </pre>
                  </div>
                ) : null}
              </div>
              <DialogFooter>
                <Button onClick={() => setSelectedLog(null)}>Close</Button>
              </DialogFooter>
            </div>
          ) : null}
        </DialogContent>
      </Dialog>
    </AppShell>
  );
}
