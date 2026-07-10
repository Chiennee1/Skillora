"use client";

import * as React from "react";
import Image from "next/image";
import { usePathname, useRouter, useSearchParams } from "next/navigation";
import { useQuery } from "@tanstack/react-query";
import { Search, SlidersHorizontal, X } from "lucide-react";

import { CourseCard } from "@/components/course-card";
import { MarketplaceLayout } from "@/components/marketplace-layout";
import { EmptyState, ErrorState } from "@/components/data-state";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { Sheet, SheetContent, SheetTitle, SheetTrigger } from "@/components/ui/sheet";
import { Skeleton } from "@/components/ui/skeleton";
import { courseApi, emptyPage } from "@/lib/api";
import { PAGE_SIZE } from "@/lib/config";
import { queryKeys } from "@/lib/query-keys";
import { cn } from "@/lib/utils";
import type { Category, CourseLevel, CourseSummary } from "@/lib/types";

const levels: { label: string; value: CourseLevel | "ALL" }[] = [
  { label: "All levels", value: "ALL" },
  { label: "Beginner", value: "BEGINNER" },
  { label: "Intermediate", value: "INTERMEDIATE" },
  { label: "Advanced", value: "ADVANCED" },
  { label: "All levels course", value: "ALL_LEVELS" },
];

const sortOptions = [
  { label: "Newest", value: "createdAt,desc" },
  { label: "Popular", value: "totalEnrollments,desc" },
  { label: "Rating", value: "avgRating,desc" },
  { label: "Price: Low to High", value: "price,asc" },
  { label: "Price: High to Low", value: "price,desc" },
];

function useDebounce<T>(value: T, delay: number): T {
  const [debounced, setDebounced] = React.useState(value);
  React.useEffect(() => {
    const timer = setTimeout(() => setDebounced(value), delay);
    return () => clearTimeout(timer);
  }, [value, delay]);
  return debounced;
}

function FilterPanel({
  search,
  setSearch,
  level,
  setLevel,
  categoryId,
  setCategoryId,
  sort,
  setSort,
  categories,
}: {
  search: string;
  setSearch: (value: string) => void;
  level: string;
  setLevel: (value: string) => void;
  categoryId: string;
  setCategoryId: (value: string) => void;
  sort: string;
  setSort: (value: string) => void;
  categories: Category[];
}) {
  return (
    <div className="grid gap-5">
      <div className="grid gap-2">
        <Label htmlFor="filter-search" className="text-xs font-bold text-foreground">Search keywords</Label>
        <div className="relative group/input">
          <Search className="pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground group-focus-within/input:text-primary transition-colors" />
          <Input
            id="filter-search"
            value={search}
            onChange={(event) => setSearch(event.target.value)}
            placeholder="Search keywords..."
            className="h-10 pl-9 text-xs"
          />
        </div>
      </div>
      <div className="grid gap-2">
        <Label className="text-xs font-bold text-foreground">Course Level</Label>
        <Select value={level} onValueChange={setLevel}>
          <SelectTrigger className="h-10 text-xs"><SelectValue /></SelectTrigger>
          <SelectContent>
            {levels.map((item) => (
              <SelectItem key={item.value} value={item.value} className="text-xs">
                {item.label}
              </SelectItem>
            ))}
          </SelectContent>
        </Select>
      </div>
      <div className="grid gap-2">
        <Label className="text-xs font-bold text-foreground">Category</Label>
        <Select value={categoryId} onValueChange={setCategoryId}>
          <SelectTrigger className="h-10 text-xs"><SelectValue /></SelectTrigger>
          <SelectContent>
            <SelectItem value="ALL" className="text-xs">All categories</SelectItem>
            {categories.map((category) => (
              <SelectItem key={category.id} value={String(category.id)} className="text-xs">
                {category.name}
              </SelectItem>
            ))}
          </SelectContent>
        </Select>
      </div>
      <div className="grid gap-2">
        <Label className="text-xs font-bold text-foreground">Sort order</Label>
        <Select value={sort} onValueChange={setSort}>
          <SelectTrigger className="h-10 text-xs"><SelectValue /></SelectTrigger>
          <SelectContent>
            {sortOptions.map((item) => (
              <SelectItem key={item.value} value={item.value} className="text-xs">
                {item.label}
              </SelectItem>
            ))}
          </SelectContent>
        </Select>
      </div>
    </div>
  );
}

function HeroSection({ onSearch }: { onSearch: (value: string) => void }) {
  const [value, setValue] = React.useState("");

  const handleSubmit = (event: React.FormEvent) => {
    event.preventDefault();
    onSearch(value);
  };

  return (
    <section className="hero-gradient relative overflow-hidden text-white">
      <div className="absolute inset-0 bg-[linear-gradient(to_right,rgba(255,255,255,0.035)_1px,transparent_1px),linear-gradient(to_bottom,rgba(255,255,255,0.035)_1px,transparent_1px)] bg-[size:3.25rem_3.25rem] [mask-image:radial-gradient(ellipse_68%_58%_at_50%_0%,#000_68%,transparent_100%)]" />

      <div className="relative z-10 mx-auto max-w-[1400px] px-4 py-12 md:px-6 md:py-[4.5rem] lg:py-20">
        <div className="grid items-center gap-10 lg:grid-cols-[minmax(0,1fr)_460px] xl:grid-cols-[minmax(0,1fr)_520px]">
          <div className="space-y-6 text-center lg:text-left">
            <Badge className="border border-white/14 bg-white/10 px-2.5 py-1 text-[11px] font-semibold text-white hover:bg-white/15">
              Skillora marketplace
            </Badge>
            <h1 className="max-w-[11ch] text-4xl font-black leading-[0.98] tracking-[-0.055em] text-balance md:text-6xl lg:text-7xl">
              Skills for your future
            </h1>
            <p className="mx-auto max-w-[48ch] text-base leading-7 text-white/78 md:text-lg lg:mx-0">
              Find production-ready courses, practice with structure, and keep every learning path moving.
            </p>
            <form onSubmit={handleSubmit} className="mx-auto flex max-w-2xl flex-col gap-2 rounded-[--radius-card] border border-white/15 bg-white/10 p-2 shadow-2xl backdrop-blur sm:flex-row lg:mx-0">
              <div className="group/search relative flex-1">
                <Search className="pointer-events-none absolute left-4 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground transition-colors group-focus-within/search:text-primary" />
                <Input
                  value={value}
                  onChange={(event) => setValue(event.target.value)}
                  placeholder="What do you want to learn today?"
                  className="h-12 rounded-[calc(var(--radius-card)-0.25rem)] border-0 bg-white pl-11 text-foreground shadow-none focus-visible:ring-2 focus-visible:ring-teal-300/70"
                />
              </div>
              <Button type="submit" size="lg" className="h-12 rounded-[calc(var(--radius-card)-0.25rem)] border-none bg-teal-300 px-7 font-bold text-teal-950 shadow-none transition-colors duration-200 hover:bg-teal-200 sm:w-auto">
                Search
              </Button>
            </form>
          </div>

          <div className="course-card relative hidden aspect-[4/3] w-full overflow-hidden rounded-[--radius-panel] border border-white/16 bg-white/8 p-2 shadow-2xl backdrop-blur-md lg:block">
            <div className="relative h-full overflow-hidden rounded-[calc(var(--radius-panel)-0.5rem)]">
              <Image
                src="/hero.png"
                alt="Skillora online learning lifestyle illustration"
                fill
                priority
                sizes="(min-width: 1280px) 520px, (min-width: 1024px) 460px, 100vw"
                className="course-card-image object-cover"
              />
              <div className="absolute inset-0 bg-gradient-to-t from-black/30 via-transparent to-white/5" />
            </div>
          </div>
        </div>
      </div>
    </section>
  );
}

function CategoryChips({
  categories,
  selected,
  onSelect,
}: {
  categories: Category[];
  selected: string;
  onSelect: (id: string) => void;
}) {
  return (
    <div className="flex gap-2 overflow-x-auto pb-4 pt-2 md:flex-wrap md:overflow-visible">
      <Badge
        variant={selected === "ALL" ? "default" : "outline"}
        className={cn(
          "shrink-0 cursor-pointer transition-all duration-200 px-3.5 py-1 text-xs font-semibold rounded-full border-border/70",
          selected === "ALL"
            ? "bg-primary text-primary-foreground hover:bg-primary/95 shadow-sm"
            : "bg-background hover:bg-muted text-muted-foreground hover:text-foreground"
        )}
        onClick={() => onSelect("ALL")}
      >
        All Fields
      </Badge>
      {categories.map((category) => (
        <Badge
          key={category.id}
          variant={selected === String(category.id) ? "default" : "outline"}
          className={cn(
            "shrink-0 cursor-pointer transition-all duration-200 px-3.5 py-1 text-xs font-semibold rounded-full border-border/70",
            selected === String(category.id)
              ? "bg-primary text-primary-foreground hover:bg-primary/95 shadow-sm"
              : "bg-background hover:bg-muted text-muted-foreground hover:text-foreground"
          )}
          onClick={() => onSelect(String(category.id))}
        >
          {category.name}
        </Badge>
      ))}
    </div>
  );
}

function CourseGridSkeleton() {
  return (
    <div className="grid gap-6 sm:grid-cols-2 lg:grid-cols-3">
      {Array.from({ length: 6 }).map((_, i) => (
        <div key={i} className="space-y-3 rounded-[--radius-card] border p-0 overflow-hidden bg-card">
          <Skeleton className="aspect-video w-full" />
          <div className="space-y-2 p-4">
            <Skeleton className="h-4 w-3/4" />
            <Skeleton className="h-3 w-1/2" />
            <Skeleton className="h-3 w-1/3" />
            <div className="pt-2 flex justify-between">
              <Skeleton className="h-5 w-20" />
              <Skeleton className="h-5 w-12" />
            </div>
          </div>
        </div>
      ))}
    </div>
  );
}

export function CatalogPage() {
  const router = useRouter();
  const pathname = usePathname();
  const searchParams = useSearchParams();
  const initialSearch = searchParams.get("search") ?? "";
  const initialLevel = searchParams.get("level") ?? "ALL";
  const initialCategoryId = searchParams.get("categoryId") ?? "ALL";
  const initialSort = searchParams.get("sort") ?? "createdAt,desc";
  const initialPage = Number(searchParams.get("page") ?? 0);

  const [search, setSearch] = React.useState(initialSearch);
  const [level, setLevel] = React.useState(initialLevel);
  const [categoryId, setCategoryId] = React.useState(initialCategoryId);
  const [sort, setSort] = React.useState(initialSort);
  const [page, setPage] = React.useState(Number.isFinite(initialPage) && initialPage > 0 ? initialPage : 0);

  const debouncedSearch = useDebounce(search, 300);

  React.useEffect(() => {
    const params = new URLSearchParams();
    if (debouncedSearch) params.set("search", debouncedSearch);
    if (level !== "ALL") params.set("level", level);
    if (categoryId !== "ALL") params.set("categoryId", categoryId);
    if (sort !== "createdAt,desc") params.set("sort", sort);
    if (page > 0) params.set("page", String(page));
    const next = params.toString() ? `${pathname}?${params.toString()}` : pathname;
    router.replace(next, { scroll: false });
  }, [categoryId, debouncedSearch, level, page, pathname, router, sort]);

  const categoriesQuery = useQuery({
    queryKey: queryKeys.categories,
    queryFn: () => courseApi.categories(),
  });

  const courseFilters = {
    search: debouncedSearch || undefined,
    level: level === "ALL" ? undefined : level,
    categoryId: categoryId === "ALL" ? undefined : categoryId,
    page,
    size: PAGE_SIZE,
    sort,
  };

  const coursesQuery = useQuery({
    queryKey: queryKeys.courses(courseFilters),
    queryFn: () => courseApi.list(courseFilters),
  });

  const courses = coursesQuery.data ?? emptyPage<CourseSummary>();
  const categories = categoriesQuery.data ?? [];

  const resetPage = () => setPage(0);

  const handleHeroSearch = (value: string) => {
    setSearch(value);
    resetPage();
  };

  const activeFilters = [
    level !== "ALL" && level,
    categoryId !== "ALL" && categories.find((c) => String(c.id) === categoryId)?.name,
    debouncedSearch,
  ].filter(Boolean);

  const filters = (
    <FilterPanel
      search={search}
      setSearch={(value) => { setSearch(value); resetPage(); }}
      level={level}
      setLevel={(value) => { setLevel(value); resetPage(); }}
      categoryId={categoryId}
      setCategoryId={(value) => { setCategoryId(value); resetPage(); }}
      sort={sort}
      setSort={(value) => { setSort(value); resetPage(); }}
      categories={categories}
    />
  );

  return (
    <MarketplaceLayout>
      <HeroSection onSearch={handleHeroSearch} />

      <section className="border-b border-border/70 bg-background/72 backdrop-blur">
        <div className="mx-auto grid max-w-[1400px] gap-3 px-4 py-4 text-sm md:grid-cols-3 md:px-6">
          {[
            ["Structured courses", "Clear outcomes, lessons, quiz and assignment links."],
            ["Role-aware workspace", "Learners, instructors, and admins each get focused tools."],
            ["Checkout ready", "Wishlist, cart, coupon, order, and retry flows are wired."],
          ].map(([title, body]) => (
            <div key={title} className="rounded-[--radius-card] border border-border/70 bg-card/70 p-4">
              <p className="font-semibold tracking-tight">{title}</p>
              <p className="mt-1 text-xs leading-5 text-muted-foreground">{body}</p>
            </div>
          ))}
        </div>
      </section>

      <div className="mx-auto max-w-[1400px] px-4 py-8 md:px-6">
        {/* Category chips */}
        {categories.length > 0 ? (
          <div className="mb-6 overflow-x-auto scrollbar-none pb-2">
            <CategoryChips
              categories={categories}
              selected={categoryId}
              onSelect={(id) => { setCategoryId(id); resetPage(); }}
            />
          </div>
        ) : null}

        {/* Results header */}
        <div className="mb-6 flex flex-wrap items-center justify-between gap-3 border-b border-border/50 pb-5">
          <div className="flex items-center gap-3">
            <h2 className="text-xl font-bold tracking-tight text-foreground">
              {debouncedSearch
                ? `Results for "${debouncedSearch}"`
                : "All courses"}
            </h2>
            {coursesQuery.isSuccess ? (
              <span className="rounded-[--radius-button] bg-muted/70 px-2.5 py-1 text-sm font-semibold text-muted-foreground/80">
                {courses.totalElements.toLocaleString()} result{courses.totalElements !== 1 ? "s" : ""}
              </span>
            ) : null}
          </div>
          <div className="flex items-center gap-2">
            {/* Active filter badges */}
            {activeFilters.map((filter) => (
              <Badge key={String(filter)} variant="secondary" className="gap-1.5 px-2.5 py-0.5 text-xs rounded-md border-0 bg-primary/10 text-primary">
                {String(filter)}
                <X
                  className="h-3.5 w-3.5 cursor-pointer hover:text-foreground transition-colors"
                  onClick={() => {
                    if (filter === debouncedSearch) setSearch("");
                    if (filter === level) setLevel("ALL");
                    const matchedCategory = categories.find((c) => c.name === filter);
                    if (matchedCategory) setCategoryId("ALL");
                    resetPage();
                  }}
                />
              </Badge>
            ))}
            {activeFilters.length > 0 || sort !== "createdAt,desc" ? (
              <Button
                variant="ghost"
                size="sm"
                className="text-xs font-semibold text-muted-foreground hover:text-foreground"
                onClick={() => {
                  setSearch("");
                  setLevel("ALL");
                  setCategoryId("ALL");
                  setSort("createdAt,desc");
                  setPage(0);
                }}
              >
                Clear all filters
              </Button>
            ) : null}
            {/* Mobile filter */}
            <Sheet>
              <SheetTrigger asChild>
                <Button variant="outline" size="sm" className="md:hidden">
                  <SlidersHorizontal className="mr-2 h-4 w-4" />
                  Filters
                </Button>
              </SheetTrigger>
              <SheetContent>
                <SheetTitle>Filters</SheetTitle>
                <div className="mt-6">{filters}</div>
              </SheetContent>
            </Sheet>
          </div>
        </div>

        {/* Main grid with sidebar */}
        <div className="grid gap-8 md:grid-cols-[250px_1fr]">
          {/* Desktop filter sidebar */}
          <aside className="panel-ring hidden h-fit rounded-[--radius-card] bg-card/86 p-5 md:block sticky-purchase">
            <h3 className="text-sm font-bold text-foreground mb-4 pb-2 border-b border-border/40">Filters</h3>
            {filters}
          </aside>

          {/* Course grid */}
          <section>
            {coursesQuery.isLoading ? <CourseGridSkeleton /> : null}
            {coursesQuery.isError ? (
              <ErrorState
                message={(coursesQuery.error as Error).message}
                onRetry={() => coursesQuery.refetch()}
              />
            ) : null}
            {coursesQuery.isSuccess && courses.content.length === 0 ? (
              <EmptyState
                title="No courses found"
                message="Try adjusting the filters or searching for a different topic."
              />
            ) : null}
            {courses.content.length > 0 ? (
              <>
                <div className="grid items-stretch gap-6 sm:grid-cols-2 lg:grid-cols-3">
                  {courses.content.map((course, index) => (
                    <CourseCard key={course.id} course={course} priority={index < 3} />
                  ))}
                </div>
                <div className="mt-8 flex items-center justify-between gap-3 border-t border-border/40 pt-6">
                  <p className="text-xs font-semibold text-muted-foreground/80">
                    Showing Page {courses.page + 1} of {Math.max(courses.totalPages, 1)}
                  </p>
                  <div className="flex gap-2">
                    <Button
                      variant="outline"
                      size="sm"
                      className="text-xs h-9 font-semibold"
                      disabled={courses.first}
                      onClick={() => setPage((value) => Math.max(0, value - 1))}
                    >
                      Previous
                    </Button>
                    <Button
                      variant="outline"
                      size="sm"
                      className="text-xs h-9 font-semibold"
                      disabled={courses.last}
                      onClick={() => setPage((value) => value + 1)}
                    >
                      Next
                    </Button>
                  </div>
                </div>
              </>
            ) : null}
          </section>
        </div>
      </div>
    </MarketplaceLayout>
  );
}
