import { use } from "react";
import { LessonPage } from "@/features/learner/lesson-page";

export default function EnrollmentLessonRoute({
  params,
}: {
  params: Promise<{ enrollmentId: string; lessonId: string }>;
}) {
  const resolvedParams = use(params);
  return (
    <LessonPage
      lessonId={Number(resolvedParams.lessonId)}
      enrollmentId={Number(resolvedParams.enrollmentId)}
    />
  );
}
