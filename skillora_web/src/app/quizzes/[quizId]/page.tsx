import { use } from "react";
import { QuizPage } from "@/features/learner/quiz-page";

export default function QuizRoute({ params }: { params: Promise<{ quizId: string }> }) {
  const resolvedParams = use(params);
  return <QuizPage quizId={Number(resolvedParams.quizId)} />;
}
