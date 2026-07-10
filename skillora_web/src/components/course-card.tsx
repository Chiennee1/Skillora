"use client";

import Link from "next/link";
import Image from "next/image";
import { BookOpen, Clock, Star, Users, CheckCircle2 } from "lucide-react";
import { useQuery } from "@tanstack/react-query";

import { StatusBadge } from "@/components/status-badge";
import { Badge } from "@/components/ui/badge";
import { Card } from "@/components/ui/card";
import { HoverCard, HoverCardContent, HoverCardTrigger } from "@/components/ui/hover-card";
import { formatDuration, formatMoney, formatRating } from "@/lib/format";
import { courseApi } from "@/lib/api";
import type { CourseSummary } from "@/lib/types";

function StarRating({ rating, count }: { rating?: number | string | null; count?: number }) {
  const num = Number(rating ?? 0);
  const display = formatRating(rating);
  const isNew = display === "New";

  return (
    <span className="flex items-center gap-1.5 text-xs text-muted-foreground">
      {!isNew ? (
        <>
          <span className="font-bold text-amber-600 dark:text-amber-500">{display}</span>
          <span className="flex items-center">
            {Array.from({ length: 5 }).map((_, i) => (
              <Star
                key={i}
                className={`h-3 w-3 ${
                  i < Math.round(num)
                    ? "fill-amber-500 text-amber-500"
                    : "text-muted-foreground/20"
                }`}
              />
            ))}
          </span>
          {count !== undefined ? (
            <span className="text-[10px] text-muted-foreground/80">({count.toLocaleString()})</span>
          ) : null}
        </>
      ) : (
        <Badge variant="secondary" className="text-[9px] uppercase tracking-wider bg-emerald-500/10 text-emerald-600 dark:text-emerald-400 border-0 font-bold px-1.5 py-0">
          New
        </Badge>
      )}
    </span>
  );
}

function QuickViewContent({ idOrSlug }: { idOrSlug: string | number }) {
  const { data: course, isLoading } = useQuery({
    queryKey: ["course-quick-view", idOrSlug],
    queryFn: () => courseApi.getById(idOrSlug),
    staleTime: 5 * 60 * 1000,
  });

  if (isLoading) {
    return (
      <div className="space-y-4 p-4 text-xs">
        <div className="space-y-2">
          <div className="h-4 bg-muted animate-pulse rounded w-3/4" />
          <div className="h-3 bg-muted animate-pulse rounded w-5/6" />
        </div>
        <div className="h-3 bg-muted animate-pulse rounded w-1/4" />
        <div className="space-y-2 pt-2">
          <div className="h-3 bg-muted animate-pulse rounded w-full" />
          <div className="h-3 bg-muted animate-pulse rounded w-full" />
        </div>
      </div>
    );
  }

  if (!course) return null;

  return (
    <div className="space-y-3.5 p-4 text-xs leading-relaxed">
      <div>
        <h4 className="font-bold text-sm text-foreground leading-snug">{course.title}</h4>
        {course.subtitle && (
          <p className="text-muted-foreground/90 mt-1.5 text-[11px] leading-normal line-clamp-3">
            {course.subtitle}
          </p>
        )}
      </div>

      <div className="flex flex-wrap items-center gap-2 text-[10px]">
        {course.level && (
          <Badge variant="secondary" className="bg-primary/10 text-primary border-0 font-medium px-1.5 py-0 hover:bg-primary/10">
            {course.level.replace("_", " ")}
          </Badge>
        )}
        <span className="text-muted-foreground">
          Updated {course.updatedAt ? new Date(course.updatedAt).toLocaleDateString() : "recently"}
        </span>
      </div>

      {course.outcomes && course.outcomes.length > 0 && (
        <div className="space-y-2 pt-1 border-t border-border/45">
          <p className="font-semibold text-foreground">What you will learn:</p>
          <ul className="space-y-1.5">
            {course.outcomes.slice(0, 3).map((outcome, idx) => (
              <li key={idx} className="flex gap-2 items-start text-muted-foreground">
                <CheckCircle2 className="h-3.5 w-3.5 text-primary shrink-0 mt-0.5" />
                <span className="line-clamp-2">{outcome}</span>
              </li>
            ))}
          </ul>
        </div>
      )}

      {course.requirements && course.requirements.length > 0 && (
        <div className="space-y-1 pt-1 border-t border-border/45 text-[11px]">
          <p className="font-semibold text-foreground">Requirements:</p>
          <p className="text-muted-foreground/80 line-clamp-1">{course.requirements.join(", ")}</p>
        </div>
      )}
    </div>
  );
}

export function CourseCard({ course, priority = false }: { course: CourseSummary; priority?: boolean }) {
  const href = `/courses/${course.slug ?? course.id}`;
  const price = course.discountPrice ?? course.price;
  const hasDiscount = course.discountPrice !== null && course.discountPrice !== undefined;

  return (
    <HoverCard openDelay={500} closeDelay={100}>
      <HoverCardTrigger asChild>
        <Card className="premium-card course-card group flex h-full overflow-hidden rounded-[--radius-card] border border-border/60 bg-card py-0 transition-all duration-300">
          <Link href={href} className="flex h-full flex-col">
            {/* Thumbnail */}
            <div className="relative aspect-video overflow-hidden bg-muted">
              {course.thumbnailUrl ? (
                <Image
                  src={course.thumbnailUrl}
                  alt={course.title}
                  fill
                  priority={priority}
                  sizes="(min-width: 1280px) 33vw, (min-width: 640px) 50vw, 100vw"
                  className="course-card-image object-cover"
                />
              ) : (
                <div className="flex h-full items-center justify-center text-muted-foreground">
                  <BookOpen className="h-8 w-8" />
                </div>
              )}
              <div className="pointer-events-none absolute inset-x-0 bottom-0 h-14 bg-gradient-to-t from-black/25 to-transparent opacity-0 transition-opacity duration-300 md:group-hover:opacity-100" />
              {/* Status overlay for non-published */}
              {course.status && course.status !== "PUBLISHED" ? (
                <div className="absolute left-2.5 top-2.5">
                  <StatusBadge status={course.status} />
                </div>
              ) : null}
            </div>

            {/* Content */}
            <div className="flex flex-1 flex-col gap-2 p-4">
              {/* Title */}
              <h3 className="line-clamp-2 min-h-[2.45rem] text-sm font-bold leading-snug tracking-tight text-foreground transition-colors duration-200 group-hover:text-primary">
                {course.title}
              </h3>

              {/* Instructor */}
              {course.instructorName ? (
                <p className="truncate text-xs text-muted-foreground/80">
                  {course.instructorName}
                </p>
              ) : null}

              {/* Rating */}
              <StarRating rating={course.avgRating} count={course.totalReviews} />

              {/* Meta row */}
              <div className="flex flex-wrap items-center gap-3 text-[11px] text-muted-foreground/75 mt-0.5">
                <span className="flex items-center gap-1">
                  <Clock className="h-3 w-3" />
                  {formatDuration(course.totalDurationSeconds)}
                </span>
                <span className="flex items-center gap-1">
                  <BookOpen className="h-3 w-3" />
                  {course.totalLessons ?? 0} lessons
                </span>
                {(course.totalEnrollments ?? 0) > 0 ? (
                  <span className="flex items-center gap-1">
                    <Users className="h-3 w-3" />
                    {(course.totalEnrollments ?? 0).toLocaleString()}
                  </span>
                ) : null}
              </div>

              {/* Price + Level */}
              <div className="mt-auto flex items-center justify-between gap-2 border-t border-border/40 pt-3">
                <div className="flex items-baseline gap-2">
                  <span className="text-base font-extrabold text-foreground">
                    {formatMoney(price, course.currency)}
                  </span>
                  {hasDiscount ? (
                    <span className="text-xs text-muted-foreground/60 line-through font-medium">
                      {formatMoney(course.price, course.currency)}
                    </span>
                  ) : null}
                </div>
                {course.level ? (
                  <Badge variant="secondary" className="text-[9px] px-1.5 py-0 font-semibold bg-secondary/80 text-secondary-foreground border-0">
                    {course.level.replace("_", " ")}
                  </Badge>
                ) : null}
              </div>
            </div>
          </Link>
        </Card>
      </HoverCardTrigger>
      
      <HoverCardContent
        side="right"
        align="start"
        sideOffset={12}
        className="w-80 hidden md:block border border-border/80 bg-popover rounded-[--radius-card] shadow-2xl p-0 overflow-hidden"
      >
        <QuickViewContent idOrSlug={course.id} />
      </HoverCardContent>
    </HoverCard>
  );
}

export { StarRating };
