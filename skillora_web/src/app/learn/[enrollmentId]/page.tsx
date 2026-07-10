import { use } from "react";
import { LearningProgressPage } from "@/features/learner/learning-progress-page";

export default function LearningProgressRoute({ params }: { params: Promise<{ enrollmentId: string }> }) {
  const resolvedParams = use(params);
  return <LearningProgressPage enrollmentId={Number(resolvedParams.enrollmentId)} />;
}
