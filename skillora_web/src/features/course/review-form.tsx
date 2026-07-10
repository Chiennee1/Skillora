"use client";

import * as React from "react";
import { useMutation, useQueryClient } from "@tanstack/react-query";
import { Star, Trash2 } from "lucide-react";
import { toast } from "sonner";

import { ConfirmActionDialog } from "@/components/confirm-action-dialog";
import { Button } from "@/components/ui/button";
import { Label } from "@/components/ui/label";
import { Textarea } from "@/components/ui/textarea";
import { reviewApi } from "@/lib/api";
import { queryKeys } from "@/lib/query-keys";
import type { Review } from "@/lib/types";

type ReviewFormProps = {
  courseId: number;
  existingReview?: Review | null;
  onSuccess?: () => void;
  onCancel?: () => void;
};

export function ReviewForm({
  courseId,
  existingReview,
  onSuccess,
  onCancel,
}: ReviewFormProps) {
  const queryClient = useQueryClient();
  const [rating, setRating] = React.useState(existingReview?.rating ?? 5);
  const [hoverRating, setHoverRating] = React.useState<number | null>(null);
  const [content, setContent] = React.useState(existingReview?.content ?? "");

  const createReview = useMutation({
    mutationFn: (body: { courseId: number; rating: number; content?: string }) =>
      reviewApi.create(body),
    onSuccess: () => {
      toast.success("Review submitted successfully");
      queryClient.invalidateQueries({ queryKey: queryKeys.course(courseId) });
      queryClient.invalidateQueries({ queryKey: queryKeys.reviews(courseId, 0) });
      onSuccess?.();
    },
    onError: (error) => toast.error((error as Error).message),
  });

  const updateReview = useMutation({
    mutationFn: ({ id, body }: { id: number; body: { rating: number; content?: string } }) =>
      reviewApi.update(id, body),
    onSuccess: () => {
      toast.success("Review updated successfully");
      queryClient.invalidateQueries({ queryKey: queryKeys.course(courseId) });
      queryClient.invalidateQueries({ queryKey: queryKeys.reviews(courseId, 0) });
      onSuccess?.();
    },
    onError: (error) => toast.error((error as Error).message),
  });

  const deleteReview = useMutation({
    mutationFn: (id: number) => reviewApi.remove(id),
    onSuccess: () => {
      toast.success("Review deleted");
      queryClient.invalidateQueries({ queryKey: queryKeys.course(courseId) });
      queryClient.invalidateQueries({ queryKey: queryKeys.reviews(courseId, 0) });
      onSuccess?.();
    },
    onError: (error) => toast.error((error as Error).message),
  });

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (existingReview) {
      updateReview.mutate({
        id: existingReview.id,
        body: { rating, content: content.trim() || undefined },
      });
    } else {
      createReview.mutate({
        courseId,
        rating,
        content: content.trim() || undefined,
      });
    }
  };

  const isPending = createReview.isPending || updateReview.isPending;

  return (
    <form onSubmit={handleSubmit} className="space-y-4">
      <div className="space-y-1.5">
        <Label>Rating</Label>
        <div className="flex gap-1">
          {Array.from({ length: 5 }).map((_, i) => {
            const val = i + 1;
            const isFilled = hoverRating !== null ? val <= hoverRating : val <= rating;
            return (
              <button
                key={i}
                type="button"
                className="p-1 focus:outline-none transition-transform active:scale-95"
                onMouseEnter={() => setHoverRating(val)}
                onMouseLeave={() => setHoverRating(null)}
                onClick={() => setRating(val)}
                disabled={isPending}
                aria-label={`Rate ${val} star${val !== 1 ? "s" : ""}`}
              >
                <Star
                  className={`h-7 w-7 ${
                    isFilled ? "star-filled fill-current" : "text-muted-foreground/30"
                  }`}
                />
              </button>
            );
          })}
        </div>
      </div>

      <div className="grid gap-1.5">
        <Label htmlFor="review-content">Written feedback (optional)</Label>
        <Textarea
          id="review-content"
          value={content}
          onChange={(e) => setContent(e.target.value)}
          placeholder="What did you like or dislike about this course? How can it be improved?"
          rows={4}
          disabled={isPending}
        />
      </div>

      <div className="flex items-center justify-between gap-3 pt-2">
        <div className="flex gap-2">
          <Button type="submit" disabled={isPending}>
            {existingReview ? "Save Review" : "Submit Review"}
          </Button>
          {onCancel ? (
            <Button type="button" variant="outline" onClick={onCancel} disabled={isPending}>
              Cancel
            </Button>
          ) : null}
        </div>

        {existingReview ? (
          <ConfirmActionDialog
            title="Delete your review?"
            description="Are you sure you want to permanently delete your review? This action cannot be undone."
            confirmLabel="Delete Review"
            variant="destructive"
            onConfirm={() => deleteReview.mutate(existingReview.id)}
            disabled={deleteReview.isPending}
            trigger={
              <Button type="button" variant="ghost" size="icon" className="text-destructive hover:text-destructive">
                <Trash2 className="h-4 w-4" />
              </Button>
            }
          />
        ) : null}
      </div>
    </form>
  );
}
