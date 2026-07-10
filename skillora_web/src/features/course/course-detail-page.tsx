"use client";

import * as React from "react";
import Link from "next/link";
import Image from "next/image";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import {
  Award,
  BookOpen,
  CheckCircle2,
  ChevronDown,
  Clock,
  Heart,
  Infinity,
  Play,
  ShoppingCart,
  ThumbsUp,
  Users,
  Calendar,
  Globe,
} from "lucide-react";
import { toast } from "sonner";

import { AskAiButton } from "@/components/ask-ai-button";
import { StarRating } from "@/components/course-card";
import { MarketplaceLayout } from "@/components/marketplace-layout";
import { EmptyState, ErrorState, PageSkeleton } from "@/components/data-state";
import { ReviewForm } from "@/features/course/review-form";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import {
  Collapsible,
  CollapsibleContent,
  CollapsibleTrigger,
} from "@/components/ui/collapsible";
import { Separator } from "@/components/ui/separator";
import { Skeleton } from "@/components/ui/skeleton";
import { authApi, commerceApi, courseApi, emptyPage, learnerApi, reviewApi } from "@/lib/api";
import { formatDuration, formatMoney, formatRating } from "@/lib/format";
import { queryKeys } from "@/lib/query-keys";
import type { Review, Section } from "@/lib/types";

export function CourseDetailPage({ idOrSlug }: { idOrSlug: string }) {
  const queryClient = useQueryClient();
  const [reviewPage, setReviewPage] = React.useState(0);
  const [showReviewForm, setShowReviewForm] = React.useState(false);
  const courseQuery = useQuery({
    queryKey: queryKeys.course(idOrSlug),
    queryFn: () => courseApi.getById(idOrSlug),
  });

  const courseId = courseQuery.data?.id;

  const sectionsQuery = useQuery({
    queryKey: queryKeys.sections(courseId),
    queryFn: () => courseApi.getSections(courseId!),
    enabled: Boolean(courseId),
    retry: false,
  });

  const reviewsQuery = useQuery({
    queryKey: queryKeys.reviews(courseId, reviewPage),
    queryFn: () =>
      courseApi.getReviews({ courseId, page: reviewPage, size: 10 }),
    enabled: Boolean(courseId),
  });

  const { data: user } = useQuery({
    queryKey: queryKeys.me,
    queryFn: () => authApi.me(),
    retry: false,
  });

  const enrollmentsQuery = useQuery({
    queryKey: queryKeys.enrollments("me", { page: 0, size: 100 }),
    queryFn: () => learnerApi.enrollments({ page: 0, size: 100 }),
    enabled: Boolean(user),
    retry: false,
  });

  const myEnrollment = enrollmentsQuery.data?.content.find(
    (e) => e.courseId === courseId && (e.status === "ACTIVE" || e.status === "COMPLETED"),
  );

  const addCart = useMutation({
    mutationFn: () => commerceApi.addToCart(courseId!),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.cart });
      toast.success("Course added to cart");
    },
    onError: (error) => toast.error((error as Error).message),
  });

  const addWishlist = useMutation({
    mutationFn: () => commerceApi.addToWishlist(courseId!),
    onSuccess: () => toast.success("Course saved to wishlist"),
    onError: (error) => toast.error((error as Error).message),
  });

  const enrollFree = useMutation({
    mutationFn: () => learnerApi.enroll(courseId!),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.enrollmentsRoot });
      queryClient.invalidateQueries({ queryKey: queryKeys.learningDashboard });
      toast.success("Enrollment confirmed");
    },
    onError: (error) => toast.error((error as Error).message),
  });

  const toggleReviewLike = useMutation({
    mutationFn: (review: Review) =>
      review.likedByMe ? reviewApi.unlike(review.id) : reviewApi.like(review.id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.reviews(courseId, reviewPage) });
    },
    onError: (error) => toast.error((error as Error).message),
  });

  if (courseQuery.isLoading) {
    return (
      <MarketplaceLayout>
        <div className="mx-auto max-w-[1400px] px-4 py-8 md:px-6">
          <div className="space-y-4">
            <Skeleton className="h-48 w-full rounded-[--radius-card]" />
            <Skeleton className="h-8 w-2/3" />
            <Skeleton className="h-4 w-1/2" />
          </div>
        </div>
      </MarketplaceLayout>
    );
  }

  if (courseQuery.isError || !courseQuery.data) {
    return (
      <MarketplaceLayout>
        <div className="mx-auto max-w-[1400px] px-4 py-8 md:px-6">
          <ErrorState
            message={(courseQuery.error as Error)?.message}
            onRetry={() => courseQuery.refetch()}
          />
        </div>
      </MarketplaceLayout>
    );
  }

  const course = courseQuery.data;
  const sections = sectionsQuery.data ?? [];
  const reviews = reviewsQuery.data ?? emptyPage<Review>();
  const price = course.discountPrice ?? course.price;
  const isFree = !price || Number(price) <= 0;
  const isStudent = user?.roles?.includes("STUDENT");
  const totalLessons = sections.reduce((sum, s) => sum + (s.lessons?.length ?? 0), 0);
  const existingReview = reviews.content.find((review) => review.userId === user?.id) ?? null;

  return (
    <MarketplaceLayout>
      {/* Hero banner - Premium luxury mesh */}
      <section className="hero-gradient text-white relative overflow-hidden">
        {/* Background visual mesh grid */}
        <div className="absolute inset-0 bg-[linear-gradient(to_right,rgba(255,255,255,0.02)_1px,transparent_1px),linear-gradient(to_bottom,rgba(255,255,255,0.02)_1px,transparent_1px)] bg-[size:4rem_4rem] [mask-image:radial-gradient(ellipse_60%_50%_at_50%_0%,#000_80%,transparent_100%)]" />
        
        <div className="mx-auto max-w-[1400px] px-4 py-10 md:px-6 md:py-16 relative z-10">
          <div className="max-w-4xl space-y-6">
            <div className="flex flex-wrap gap-2">
              {course.level ? (
                <Badge variant="secondary" className="bg-white/10 hover:bg-white/15 text-teal-200 border-0 font-semibold px-2.5 py-0.5 text-xs">
                  {course.level.replace("_", " ")}
                </Badge>
              ) : null}
              {course.categories?.map((cat) => (
                <Badge key={cat.id} variant="secondary" className="bg-white/10 hover:bg-white/15 text-white border-0 font-semibold px-2.5 py-0.5 text-xs">
                  {cat.name}
                </Badge>
              ))}
            </div>
            
            <h1 className="text-3xl font-extrabold tracking-tight md:text-4xl lg:text-5xl leading-[1.1] text-balance">
              {course.title}
            </h1>
            
            {course.subtitle ? (
              <p className="text-base text-white/80 md:text-xl max-w-3xl leading-relaxed font-medium">
                {course.subtitle}
              </p>
            ) : null}
            
            <div className="flex flex-wrap items-center gap-x-6 gap-y-2 text-sm text-white/70">
              <StarRating rating={course.avgRating} count={course.totalReviews} />
              
              <span className="flex items-center gap-1.5">
                <Users className="h-4 w-4 text-teal-400" />
                <span className="font-semibold text-white">{(course.totalEnrollments ?? 0).toLocaleString()}</span> students
              </span>
              
              <span className="flex items-center gap-1.5">
                <BookOpen className="h-4 w-4 text-teal-400" />
                <span className="font-semibold text-white">{totalLessons}</span> lessons
              </span>
              
              <span className="flex items-center gap-1.5">
                <Clock className="h-4 w-4 text-teal-400" />
                <span className="font-semibold text-white">{formatDuration(course.totalDurationSeconds)}</span>
              </span>
            </div>
            
            <div className="flex flex-wrap items-center gap-4 text-xs text-white/60 pt-1">
              {course.instructorName ? (
                <p>
                  Created by{" "}
                  {course.instructorId ? (
                    <Link href={`/instructors/${course.instructorId}`} className="underline text-teal-300 font-semibold hover:text-teal-200">
                      {course.instructorName}
                    </Link>
                  ) : (
                    <span className="underline text-teal-300 font-semibold">{course.instructorName}</span>
                  )}
                </p>
              ) : null}
              
              <span className="flex items-center gap-1">
                <Calendar className="h-3.5 w-3.5" />
                Updated {course.updatedAt ? new Date(course.updatedAt).toLocaleDateString() : "recently"}
              </span>

              <span className="flex items-center gap-1">
                <Globe className="h-3.5 w-3.5" />
                {course.language ?? "English"}
              </span>
            </div>
          </div>
        </div>
      </section>

      <div className="mx-auto max-w-[1400px] px-4 pt-6 md:px-6 xl:hidden">
        <div className="course-card relative aspect-video overflow-hidden rounded-[--radius-card] border border-border/70 bg-muted shadow-[0_16px_55px_-42px_rgba(15,23,42,0.75)]">
          {course.thumbnailUrl ? (
            <Image
              src={course.thumbnailUrl}
              alt={course.title}
              fill
              priority
              sizes="(min-width: 768px) 720px, 100vw"
              className="course-card-image object-cover"
            />
          ) : (
            <div className="flex h-full items-center justify-center text-muted-foreground">
              <BookOpen className="h-10 w-10" />
            </div>
          )}
          <div className="pointer-events-none absolute inset-x-0 bottom-0 h-20 bg-gradient-to-t from-black/35 to-transparent" />
        </div>
      </div>

      <div className="mx-auto max-w-[1400px] px-4 py-8 pb-28 md:px-6 xl:pb-8">
        <div className="grid gap-8 xl:grid-cols-[1fr_380px]">
          {/* Main content */}
          <section className="space-y-8">
            {/* Learning outcomes */}
            {course.outcomes?.length ? (
              <Card className="rounded-[--radius-card] border-border/60 shadow-[0_1px_3px_0_rgba(0,0,0,0.01)]">
                <CardHeader className="pb-3">
                  <CardTitle className="text-lg font-bold">What you will learn</CardTitle>
                </CardHeader>
                <CardContent>
                  <ul className="grid gap-3 sm:grid-cols-2">
                    {course.outcomes.map((item) => (
                      <li key={item} className="flex gap-2.5 text-xs text-muted-foreground leading-relaxed">
                        <CheckCircle2 className="mt-0.5 h-4 w-4 shrink-0 text-primary" />
                        <span>{item}</span>
                      </li>
                    ))}
                  </ul>
                </CardContent>
              </Card>
            ) : null}

            {/* Curriculum */}
            <Card className="rounded-[--radius-card] border-border/60">
              <CardHeader className="pb-3">
                <div className="flex items-center justify-between gap-3">
                  <CardTitle className="text-lg font-bold">Course content</CardTitle>
                  <span className="text-xs font-semibold text-muted-foreground/80">
                    {sections.length} section{sections.length !== 1 ? "s" : ""} &bull; {totalLessons} lesson{totalLessons !== 1 ? "s" : ""}
                  </span>
                </div>
              </CardHeader>
              <CardContent className="space-y-3">
                {sectionsQuery.isLoading ? <PageSkeleton rows={3} /> : null}
                {sectionsQuery.isError ? (
                  <ErrorState message={(sectionsQuery.error as Error).message} />
                ) : null}
                {sections.length === 0 && sectionsQuery.isSuccess ? (
                  <EmptyState
                    title="No curriculum yet"
                    message="The instructor has not published any sections."
                  />
                ) : null}
                {sections.map((section) => (
                  <CurriculumSection
                    key={section.id}
                    section={section}
                    isEnrolled={Boolean(myEnrollment)}
                    enrollmentId={myEnrollment?.id}
                  />
                ))}
              </CardContent>
            </Card>

            {/* About */}
            <Card className="rounded-[--radius-card] border-border/60">
              <CardHeader className="pb-3">
                <CardTitle className="text-lg font-bold">About this course</CardTitle>
              </CardHeader>
              <CardContent className="space-y-4 text-xs leading-relaxed text-muted-foreground">
                <p className="whitespace-pre-line">{course.description ?? "No description has been added yet."}</p>
              </CardContent>
            </Card>

            {/* Requirements */}
            {course.requirements?.length ? (
              <Card className="rounded-[--radius-card] border-border/60">
                <CardHeader className="pb-3">
                  <CardTitle className="text-lg font-bold">Requirements</CardTitle>
                </CardHeader>
                <CardContent>
                  <ul className="grid gap-2.5">
                    {course.requirements.map((item) => (
                      <li key={item} className="flex gap-2 text-xs text-muted-foreground leading-relaxed">
                        <span className="text-foreground/60 font-bold">&bull;</span>
                        {item}
                      </li>
                    ))}
                  </ul>
                </CardContent>
              </Card>
            ) : null}

            {/* Reviews */}
            <Card className="rounded-[--radius-card] border-border/60">
              <CardHeader className="pb-3">
                <div className="flex items-center justify-between gap-3">
                  <CardTitle className="text-lg font-bold">Student reviews</CardTitle>
                  {course.avgRating ? (
                    <div className="flex items-center gap-2.5">
                      <span className="text-2xl font-extrabold text-foreground">{formatRating(course.avgRating)}</span>
                      <StarRating rating={course.avgRating} count={course.totalReviews} />
                    </div>
                  ) : null}
                </div>
              </CardHeader>
              <CardContent className="space-y-4">
                {myEnrollment && isStudent ? (
                  <div className="rounded-lg border border-border/45 bg-muted/20 p-4">
                    {showReviewForm || existingReview ? (
                      <ReviewForm
                        courseId={course.id}
                        existingReview={existingReview}
                        onCancel={() => setShowReviewForm(false)}
                        onSuccess={() => {
                          setShowReviewForm(false);
                          reviewsQuery.refetch();
                          courseQuery.refetch();
                        }}
                      />
                    ) : (
                      <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
                        <div>
                          <p className="text-sm font-bold">Share your learning experience</p>
                          <p className="text-xs text-muted-foreground/80 mt-0.5">
                            Your review helps other learners choose the right course.
                          </p>
                        </div>
                        <Button size="sm" className="font-semibold text-xs h-9" onClick={() => setShowReviewForm(true)}>
                          Write a review
                        </Button>
                      </div>
                    )}
                  </div>
                ) : null}

                {reviews.content.length === 0 ? (
                  <p className="text-xs text-muted-foreground">No reviews yet.</p>
                ) : null}
                
                {reviews.content.map((review) => (
                  <div
                    key={review.id}
                    className="border-b border-border/40 pb-4 last:border-b-0 last:pb-0 pt-2"
                  >
                    <div className="flex items-center justify-between gap-3">
                      <div className="flex items-center gap-2.5">
                        <div className="flex h-8 w-8 items-center justify-center rounded-full bg-primary/10 text-xs font-bold text-primary">
                          {(review.userName ?? "L")[0].toUpperCase()}
                        </div>
                        <div>
                          <p className="text-xs font-bold text-foreground">{review.userName ?? "Learner"}</p>
                          <StarRating rating={review.rating} />
                        </div>
                      </div>
                    </div>
                    {review.content ? (
                      <p className="mt-2 text-xs leading-relaxed text-muted-foreground">{review.content}</p>
                    ) : null}
                    {user ? (
                      <Button
                        variant="ghost"
                        size="sm"
                        className="mt-2.5 h-8 px-2.5 text-[11px] font-semibold text-muted-foreground hover:text-foreground"
                        disabled={toggleReviewLike.isPending}
                        onClick={() => toggleReviewLike.mutate(review)}
                      >
                        <ThumbsUp className={review.likedByMe ? "mr-1.5 h-3.5 w-3.5 fill-current text-primary" : "mr-1.5 h-3.5 w-3.5"} />
                        {review.likedByMe ? "Liked" : "Like"}
                        {review.likeCount !== undefined ? ` (${review.likeCount})` : ""}
                      </Button>
                    ) : null}
                  </div>
                ))}
                
                {reviews.totalPages > 1 ? (
                  <div className="flex items-center justify-between gap-3 pt-4 border-t border-border/40">
                    <p className="text-xs font-semibold text-muted-foreground">
                      Page {reviews.page + 1} of {reviews.totalPages}
                    </p>
                    <div className="flex gap-2">
                      <Button
                        variant="outline"
                        size="sm"
                        className="text-xs h-8"
                        disabled={reviews.first}
                        onClick={() => setReviewPage((value) => Math.max(0, value - 1))}
                      >
                        Previous
                      </Button>
                      <Button
                        variant="outline"
                        size="sm"
                        className="text-xs h-8"
                        disabled={reviews.last}
                        onClick={() => setReviewPage((value) => value + 1)}
                      >
                        Next
                      </Button>
                    </div>
                  </div>
                ) : null}
              </CardContent>
            </Card>
          </section>

          {/* Sticky purchase card (desktop) - Glass card */}
          <aside className="hidden xl:block">
            <div className="sticky-purchase space-y-4">
              <Card className="course-card overflow-hidden rounded-[--radius-card] border border-border/70 bg-card/90 py-0 shadow-2xl backdrop-blur-md">
                <div className="relative aspect-video bg-muted border-b border-border/40">
                  {course.thumbnailUrl ? (
                    <Image
                      src={course.thumbnailUrl}
                      alt={course.title}
                      fill
                      priority
                      sizes="380px"
                      className="course-card-image object-cover"
                    />
                  ) : (
                    <div className="flex h-full items-center justify-center text-muted-foreground">
                      <BookOpen className="h-8 w-8" />
                    </div>
                  )}
                </div>
                <CardContent className="space-y-4 p-6">
                  <div className="flex items-baseline gap-3">
                    <span className="text-3xl font-extrabold text-foreground">
                      {formatMoney(price, course.currency)}
                    </span>
                    {course.discountPrice ? (
                      <span className="text-sm text-muted-foreground/60 line-through font-medium">
                        {formatMoney(course.price, course.currency)}
                      </span>
                    ) : null}
                  </div>

                  <div className="grid gap-2 pt-2">
                    {myEnrollment ? (
                      <Button asChild className="w-full h-11 text-xs font-bold rounded-[--radius-button]" size="lg">
                        <Link href={`/learn/${myEnrollment.id}`}>
                          <Play className="mr-2 h-4 w-4 fill-current" />
                          Go to course
                        </Link>
                      </Button>
                    ) : isStudent ? (
                      <>
                        {isFree ? (
                          <Button
                            className="w-full h-11 text-xs font-bold rounded-[--radius-button]"
                            size="lg"
                            onClick={() => enrollFree.mutate()}
                            disabled={enrollFree.isPending}
                          >
                            Enroll for free
                          </Button>
                        ) : (
                          <Button
                            className="w-full h-11 text-xs font-bold rounded-[--radius-button]"
                            size="lg"
                            onClick={() => addCart.mutate()}
                            disabled={addCart.isPending}
                          >
                            <ShoppingCart className="mr-2 h-4 w-4" />
                            Add to cart
                          </Button>
                        )}
                        <Button
                          className="w-full h-11 text-xs font-bold rounded-[--radius-button] border-border/80 text-foreground hover:bg-muted"
                          variant="outline"
                          onClick={() => addWishlist.mutate()}
                          disabled={addWishlist.isPending}
                        >
                          <Heart className="mr-2 h-4 w-4" />
                          Save to wishlist
                        </Button>
                      </>
                    ) : (
                      <Button asChild className="w-full h-11 text-xs font-bold rounded-[--radius-button]" size="lg">
                        <Link href={`/login?next=/courses/${idOrSlug}`}>
                          Sign in to enroll
                        </Link>
                      </Button>
                    )}

                    {user ? (
                      <AskAiButton
                        className="w-full h-11 text-xs font-bold rounded-[--radius-button] border-primary/20 hover:bg-primary/5 text-primary"
                        courseId={course.id}
                        label="Ask AI about this course"
                        prompt={`Help me understand whether "${course.title}" fits my goals. Summarize the learning path, prerequisites, and first steps.`}
                        size="lg"
                        variant="outline"
                      />
                    ) : null}
                  </div>

                  <Separator className="bg-border/50" />

                  <div className="space-y-3.5 text-xs text-muted-foreground pt-1">
                    <h4 className="font-bold text-foreground">This course includes:</h4>
                    <div className="flex items-center gap-2.5">
                      <Clock className="h-4 w-4 text-primary shrink-0" />
                      <span>{formatDuration(course.totalDurationSeconds)} of video content</span>
                    </div>
                    <div className="flex items-center gap-2.5">
                      <BookOpen className="h-4 w-4 text-primary shrink-0" />
                      <span>{totalLessons} structured lessons</span>
                    </div>
                    <div className="flex items-center gap-2.5">
                      <Infinity className="h-4 w-4 text-primary shrink-0" />
                      <span>Full lifetime access</span>
                    </div>
                    <div className="flex items-center gap-2.5">
                      <Award className="h-4 w-4 text-primary shrink-0" />
                      <span>Certificate of completion</span>
                    </div>
                  </div>
                </CardContent>
              </Card>
            </div>
          </aside>
        </div>
      </div>

      {/* Mobile bottom purchase bar */}
      {!myEnrollment ? (
        <div className="mobile-purchase-bar xl:hidden border-t border-border/70 bg-background/95 backdrop-blur-md shadow-2xl">
          <div className="flex items-center justify-between gap-3">
            <div>
              <span className="text-lg font-extrabold text-foreground">
                {formatMoney(price, course.currency)}
              </span>
              {course.discountPrice ? (
                <span className="ml-2 text-xs text-muted-foreground/60 line-through font-medium">
                  {formatMoney(course.price, course.currency)}
                </span>
              ) : null}
            </div>
            {isStudent ? (
              isFree ? (
                <Button className="font-bold text-xs h-9 rounded-[--radius-button]" onClick={() => enrollFree.mutate()} disabled={enrollFree.isPending}>
                  Enroll free
                </Button>
              ) : (
                <Button className="font-bold text-xs h-9 rounded-[--radius-button]" onClick={() => addCart.mutate()} disabled={addCart.isPending}>
                  <ShoppingCart className="mr-2 h-4 w-4" />
                  Add to cart
                </Button>
              )
            ) : (
              <Button asChild className="font-bold text-xs h-9 rounded-[--radius-button]">
                <Link href={`/login?next=/courses/${idOrSlug}`}>Sign in</Link>
              </Button>
            )}
          </div>
        </div>
      ) : null}
    </MarketplaceLayout>
  );
}

function CurriculumSection({
  section,
  isEnrolled,
  enrollmentId,
}: {
  section: Section;
  isEnrolled: boolean;
  enrollmentId?: number;
}) {
  const lessons = section.lessons ?? [];
  const duration = lessons.reduce((sum, l) => sum + (l.durationSeconds ?? 0), 0);

  return (
    <Collapsible defaultOpen className="rounded-lg border border-border/60 overflow-hidden bg-card/40">
      <CollapsibleTrigger className="flex w-full items-center justify-between gap-3 p-4 text-left hover:bg-muted/30 transition-all [&[data-state=open]>svg]:rotate-180">
        <div>
          <p className="font-bold text-sm text-foreground">{section.title}</p>
          <p className="text-xs text-muted-foreground/80 mt-0.5">
            {lessons.length} lesson{lessons.length !== 1 ? "s" : ""} &bull; {formatDuration(duration)}
          </p>
        </div>
        <ChevronDown className="h-4 w-4 shrink-0 text-muted-foreground/80 transition-transform duration-300" />
      </CollapsibleTrigger>
      <CollapsibleContent>
        <div className="border-t border-border/40 divide-y divide-border/20 bg-background/25">
          {lessons.map((lesson) => (
            <div
              key={lesson.id}
              className="flex items-center justify-between gap-3 px-4 py-3 text-xs hover:bg-muted/20 transition-all"
            >
              <div className="flex items-center gap-2.5 min-w-0">
                {lesson.preview ? (
                  <Play className="h-3.5 w-3.5 shrink-0 text-primary fill-primary/10" />
                ) : (
                  <BookOpen className="h-3.5 w-3.5 shrink-0 text-muted-foreground/60" />
                )}
                <span className="truncate text-muted-foreground font-medium">{lesson.title}</span>
              </div>
              <div className="flex items-center gap-2 shrink-0">
                {lesson.preview ? (
                  <Button asChild size="sm" variant="link" className="h-auto p-0 text-primary font-bold text-xs">
                    <Link href={`/lessons/${lesson.id}`}>Preview</Link>
                  </Button>
                ) : null}
                {isEnrolled && enrollmentId ? (
                  <Button asChild size="sm" variant="link" className="h-auto p-0 font-bold text-xs text-primary">
                    <Link href={`/learn/${enrollmentId}/lessons/${lesson.id}`}>Open</Link>
                  </Button>
                ) : null}
                <span className="text-[11px] text-muted-foreground/50 font-mono">
                  {formatDuration(lesson.durationSeconds)}
                </span>
              </div>
            </div>
          ))}
        </div>
      </CollapsibleContent>
    </Collapsible>
  );
}
