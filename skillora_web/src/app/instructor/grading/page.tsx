import { Suspense } from "react";
import { GradingPage } from "@/features/instructor/grading-dashboard";
import { PageSkeleton } from "@/components/data-state";

export default function InstructorGradingRoute() {
  return (
    <Suspense fallback={<PageSkeleton rows={4} />}>
      <GradingPage />
    </Suspense>
  );
}
