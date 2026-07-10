import { use } from "react";
import { LessonPage } from "@/features/learner/lesson-page";

export default function PublicLessonRoute({ params }: { params: Promise<{ lessonId: string }> }) {
  const resolvedParams = use(params);
  return <LessonPage lessonId={Number(resolvedParams.lessonId)} />;
}
