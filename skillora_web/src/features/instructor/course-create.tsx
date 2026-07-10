"use client";

import * as React from "react";
import { useRouter } from "next/navigation";
import { useMutation, useQuery } from "@tanstack/react-query";
import { BookPlus } from "lucide-react";
import { toast } from "sonner";

import { AppShell } from "@/components/app-shell";
import { ErrorState, PageSkeleton } from "@/components/data-state";
import { PageHeader } from "@/components/page-header";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Checkbox } from "@/components/ui/checkbox";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { Textarea } from "@/components/ui/textarea";
import { courseApi, instructorApi } from "@/lib/api";
import { queryKeys } from "@/lib/query-keys";
import type { Course, CourseLevel } from "@/lib/types";

function csv(value: string) {
  return value
    .split("\n")
    .map((item) => item.trim())
    .filter(Boolean);
}

export function CourseCreatePage() {
  const router = useRouter();
  const [title, setTitle] = React.useState("");
  const [subtitle, setSubtitle] = React.useState("");
  const [description, setDescription] = React.useState("");
  const [level, setLevel] = React.useState<CourseLevel>("BEGINNER");
  const [price, setPrice] = React.useState("0");
  const [thumbnailUrl, setThumbnailUrl] = React.useState("");
  const [categoryIds, setCategoryIds] = React.useState<number[]>([]);
  const [requirements, setRequirements] = React.useState("");
  const [outcomes, setOutcomes] = React.useState("");

  const create = useMutation({
    mutationFn: () =>
      instructorApi.createCourse({
        title: title.trim(),
        subtitle: subtitle.trim(),
        description: description.trim(),
        level,
        price: Number(price),
        currency: "VND",
        thumbnailUrl: thumbnailUrl.trim() || undefined,
        categoryIds,
        requirements: csv(requirements),
        outcomes: csv(outcomes),
      }),
    onSuccess: (course: Course) => {
      toast.success("Course draft created successfully");
      router.push(`/instructor/courses/${course.id}`);
    },
    onError: (error) => toast.error((error as Error).message),
  });

  const handleSubmit = (event: React.FormEvent) => {
    event.preventDefault();
    if (!title.trim() || create.isPending) return;
    create.mutate();
  };

  return (
    <AppShell>
      <PageHeader
        title="New Course"
        description="Fill out the basic details. You can add section modules and lessons afterwards."
      />

      <div className="grid gap-6 xl:grid-cols-[minmax(0,1fr)_320px]">
        <Card className="bg-card/90">
          <CardHeader className="border-b border-border/60 pb-5">
            <CardTitle>Course foundation</CardTitle>
            <CardDescription>
              Give learners enough context to understand the promise before you build curriculum.
            </CardDescription>
          </CardHeader>
          <CardContent>
            <form onSubmit={handleSubmit} className="grid gap-5">
              <div className="grid gap-4 md:grid-cols-2">
                <div className="grid gap-2">
                  <Label htmlFor="title">Title</Label>
                  <Input
                    id="title"
                    value={title}
                    onChange={(e) => setTitle(e.target.value)}
                    placeholder="e.g. Next.js fullstack mastery"
                    required
                  />
                </div>
                <div className="grid gap-2">
                  <Label htmlFor="subtitle">Subtitle</Label>
                  <Input
                    id="subtitle"
                    value={subtitle}
                    onChange={(e) => setSubtitle(e.target.value)}
                    placeholder="Learn routing, data flows, UI polish, and deployment."
                  />
                </div>
              </div>

              <div className="grid gap-2">
                <Label htmlFor="description">Description</Label>
                <Textarea
                  id="description"
                  rows={6}
                  value={description}
                  onChange={(e) => setDescription(e.target.value)}
                  placeholder="Describe who this course is for, what they will build, and how lessons are structured."
                />
              </div>

              <div className="grid gap-4 md:grid-cols-3">
                <div className="grid gap-2">
                  <Label>Level</Label>
                  <Select value={level} onValueChange={(val) => setLevel(val as CourseLevel)}>
                    <SelectTrigger><SelectValue /></SelectTrigger>
                    <SelectContent>
                      <SelectItem value="BEGINNER">Beginner</SelectItem>
                      <SelectItem value="INTERMEDIATE">Intermediate</SelectItem>
                      <SelectItem value="ADVANCED">Advanced</SelectItem>
                      <SelectItem value="ALL_LEVELS">All levels</SelectItem>
                    </SelectContent>
                  </Select>
                </div>
                <div className="grid gap-2">
                  <Label htmlFor="price">Price (VND)</Label>
                  <Input
                    id="price"
                    type="number"
                    min="0"
                    value={price}
                    onChange={(e) => setPrice(e.target.value)}
                  />
                </div>
                <div className="grid gap-2">
                  <Label htmlFor="thumbnailUrl">Thumbnail image URL</Label>
                  <Input
                    id="thumbnailUrl"
                    value={thumbnailUrl}
                    onChange={(e) => setThumbnailUrl(e.target.value)}
                    placeholder="https://example.com/image.jpg"
                  />
                </div>
              </div>

              <CategoryPicker selectedIds={categoryIds} onChange={setCategoryIds} />

              <div className="grid gap-4 md:grid-cols-2">
                <div className="grid gap-2">
                  <Label htmlFor="requirements">Requirements (one per line)</Label>
                  <Textarea
                    id="requirements"
                    rows={5}
                    value={requirements}
                    onChange={(e) => setRequirements(e.target.value)}
                    placeholder="Basic JavaScript knowledge&#10;Computer with Node.js installed"
                  />
                </div>
                <div className="grid gap-2">
                  <Label htmlFor="outcomes">What learners will achieve (one per line)</Label>
                  <Textarea
                    id="outcomes"
                    rows={5}
                    value={outcomes}
                    onChange={(e) => setOutcomes(e.target.value)}
                    placeholder="Build robust server-side web applications&#10;Master database schema design"
                  />
                </div>
              </div>

              <Button className="w-full sm:w-fit" disabled={!title.trim() || create.isPending}>
                <BookPlus className="mr-2 h-4 w-4" />
                Create course draft
              </Button>
            </form>
          </CardContent>
        </Card>

        <aside className="space-y-4">
          <Card className="bg-primary/5">
            <CardHeader>
              <CardTitle className="text-sm">Before review</CardTitle>
              <CardDescription>Admin approval is easier when these are clear.</CardDescription>
            </CardHeader>
            <CardContent className="grid gap-3 text-sm">
              {[
                "A specific learner outcome",
                "At least one category",
                "Clear requirements",
                "Practical lessons, quiz, or assignment",
              ].map((item) => (
                <div key={item} className="flex items-center gap-2 text-muted-foreground">
                  <span className="h-1.5 w-1.5 rounded-full bg-primary" />
                  {item}
                </div>
              ))}
            </CardContent>
          </Card>
        </aside>
      </div>
    </AppShell>
  );
}

function CategoryPicker({
  selectedIds,
  onChange,
}: {
  selectedIds: number[];
  onChange: (ids: number[]) => void;
}) {
  const categories = useQuery({
    queryKey: queryKeys.categories,
    queryFn: () => courseApi.categories(),
  });

  const toggle = (id: number, checked: boolean) => {
    onChange(checked ? [...selectedIds, id] : selectedIds.filter((v) => v !== id));
  };

  return (
    <div className="grid gap-2">
      <Label>Categories</Label>
      {categories.isLoading ? <PageSkeleton rows={2} /> : null}
      {categories.isError ? (
        <ErrorState message={(categories.error as Error).message} onRetry={() => categories.refetch()} />
      ) : null}
      {categories.data?.length ? (
        <div className="grid gap-2 rounded-md border p-3 sm:grid-cols-2 lg:grid-cols-3 bg-muted/10">
          {categories.data.map((category) => (
            <label key={category.id} className="flex items-center gap-2 text-sm cursor-pointer select-none">
              <Checkbox
                checked={selectedIds.includes(category.id)}
                onCheckedChange={(checked) => toggle(category.id, Boolean(checked))}
              />
              {category.name}
            </label>
          ))}
        </div>
      ) : null}
    </div>
  );
}
export { CategoryPicker };
