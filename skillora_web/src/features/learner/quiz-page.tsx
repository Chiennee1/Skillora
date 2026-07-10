"use client";

import * as React from "react";
import { useMutation, useQuery } from "@tanstack/react-query";
import { Check, CheckCircle2, CircleAlert, History, RefreshCw, X } from "lucide-react";
import { toast } from "sonner";

import { AppShell } from "@/components/app-shell";
import { EmptyState, ErrorState, PageSkeleton } from "@/components/data-state";
import { PageHeader } from "@/components/page-header";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Checkbox } from "@/components/ui/checkbox";
import { practiceApi } from "@/lib/api";
import { formatDate } from "@/lib/format";
import { queryKeys } from "@/lib/query-keys";
import { cn } from "@/lib/utils";
import type { QuizAttempt } from "@/lib/types";

export function QuizPage({ quizId }: { quizId: number }) {
  const [answers, setAnswers] = React.useState<Record<number, number[]>>({});
  const [showResults, setShowResults] = React.useState<QuizAttempt | null>(null);

  const quiz = useQuery({
    queryKey: queryKeys.quiz(quizId),
    queryFn: () => practiceApi.getQuiz(quizId),
  });

  const attempts = useQuery({
    queryKey: queryKeys.quizAttempts(quizId),
    queryFn: () => practiceApi.getAttempts(quizId),
    retry: false,
  });

  const submit = useMutation({
    mutationFn: () =>
      practiceApi.submitQuiz(
        quizId,
        Object.entries(answers).map(([questionId, selectedOptionIds]) => ({
          questionId: Number(questionId),
          selectedOptionIds,
        })),
      ),
    onSuccess: (data: QuizAttempt) => {
      toast.success("Quiz submitted");
      setShowResults(data);
      attempts.refetch();
    },
    onError: (error) => toast.error((error as Error).message),
  });

  const quizData = quiz.data;
  const attemptList = attempts.data ?? [];
  const totalQuestions = quizData?.questions?.length ?? 0;
  const resultByQuestion = React.useMemo(() => {
    const map = new Map<number, NonNullable<QuizAttempt["answers"]>[number]>();
    showResults?.answers?.forEach((answer) => map.set(answer.questionId, answer));
    return map;
  }, [showResults]);
  const answeredQuestionCount =
    quizData?.questions?.filter((question) => (answers[question.id] ?? []).length > 0).length ?? 0;
  const canSubmit = totalQuestions > 0 && answeredQuestionCount === totalQuestions;

  const handleCheckboxChange = (questionId: number, optionId: number, checked: boolean) => {
    if (showResults) return;

    setAnswers((current) => {
      const existing = current[questionId] ?? [];
      return {
        ...current,
        [questionId]: checked
          ? [...existing, optionId]
          : existing.filter((id) => id !== optionId),
      };
    });
  };

  const handleRetake = () => {
    setShowResults(null);
    setAnswers({});
  };

  return (
    <AppShell>
      <div className="max-w-[1280px] mx-auto space-y-4">
        {quiz.isLoading ? <PageSkeleton rows={5} /> : null}
        {quiz.isError ? (
          <ErrorState
            message={(quiz.error as Error).message}
            onRetry={() => quiz.refetch()}
          />
        ) : null}

        {quizData ? (
          <div className="grid gap-6 lg:grid-cols-[1fr_320px]">
            <section className="space-y-4">
              <PageHeader
                title={quizData.title}
                description={quizData.description ?? "Complete all questions below."}
              />

              {/* If quiz has no questions */}
              {totalQuestions === 0 ? (
                <EmptyState title="No questions" message="This quiz has no questions yet." />
              ) : null}

              {/* Quiz Result Header Alert */}
              {showResults ? (
                <Card className={cn(
                  "border rounded-[--radius-card] overflow-hidden",
                  showResults.passed 
                    ? "border-emerald-500/35 bg-emerald-500/5 dark:bg-emerald-500/10" 
                    : "border-destructive/35 bg-destructive/5 dark:bg-destructive/10"
                )}>
                  <CardContent className="flex flex-col sm:flex-row items-center justify-between gap-4 p-5">
                    <div className="space-y-1 text-center sm:text-left">
                      <h3 className="font-extrabold text-lg flex items-center justify-center sm:justify-start gap-2">
                        {showResults.passed ? (
                          <>
                            <CheckCircle2 className="h-5 w-5 text-emerald-600 dark:text-emerald-400" />
                            <span className="text-emerald-600 dark:text-emerald-400">Quiz Completed!</span>
                          </>
                        ) : (
                          <>
                            <CircleAlert className="h-5 w-5 text-destructive" />
                            <span className="text-destructive">Quiz Failed</span>
                          </>
                        )}
                      </h3>
                      <p className="text-xs font-semibold text-muted-foreground/90">
                        Final Score: <span className="font-extrabold text-foreground">{showResults.score}</span> / 100
                        &bull; Required: {quizData.passingScore ?? 80}
                      </p>
                    </div>
                    <Button onClick={handleRetake} className="font-bold text-xs h-9 rounded-[--radius-button]">
                      <RefreshCw className="mr-2 h-3.5 w-3.5" />
                      Retake Quiz
                    </Button>
                  </CardContent>
                </Card>
              ) : null}

              {/* Questions list */}
              {quizData.questions?.map((question, index) => {
                const options = question.answerOptions ?? question.options ?? [];
                const userSelected = answers[question.id] ?? [];
                const result = resultByQuestion.get(question.id);
                
                return (
                  <Card
                    key={question.id}
                    className={cn(
                      "border border-border/60 rounded-[--radius-card] shadow-[0_1px_3px_0_rgba(0,0,0,0.01)] transition-colors duration-200",
                      result
                        ? result.correct
                          ? "border-emerald-500/35 bg-emerald-500/5 dark:bg-emerald-500/10"
                          : "border-destructive/35 bg-destructive/5 dark:bg-destructive/10"
                        : undefined
                    )}
                  >
                    <CardHeader className="pb-3">
                      <div className="flex flex-wrap items-start justify-between gap-3">
                        <CardTitle className="text-sm font-bold text-foreground">
                          Question {index + 1}: {question.content ?? question.text}
                        </CardTitle>
                        {result ? (
                          <Badge variant={result.correct ? "default" : "destructive"} className="text-[10px] font-bold border-none px-2 py-0.5">
                            {result.correct ? "Correct" : "Incorrect"}
                            {result.pointsEarned !== undefined && result.pointsEarned !== null
                              ? ` - ${result.pointsEarned} pts`
                              : ""}
                          </Badge>
                        ) : null}
                      </div>
                    </CardHeader>
                    <CardContent className="grid gap-2">
                      {options.map((option) => {
                        const isChecked = userSelected.includes(option.id);
                        let optionStyle = "border-border/60 bg-card/40 hover:bg-muted/30";
                        if (isChecked) {
                          optionStyle = "border-primary bg-primary/5 hover:bg-primary/5";
                        }
                        if (showResults && isChecked) {
                          optionStyle = result?.correct
                            ? "border-emerald-500 bg-emerald-500/10 hover:bg-emerald-500/10"
                            : "border-destructive bg-destructive/10 hover:bg-destructive/10";
                        }

                        return (
                          <label
                            key={option.id}
                            className={cn(
                              "flex min-h-11 cursor-pointer items-center gap-3 rounded-[--radius-input] border p-3 text-xs font-semibold text-muted-foreground/90 transition-all duration-200 active:scale-[0.99] select-none",
                              optionStyle
                            )}
                          >
                            <Checkbox
                              checked={isChecked}
                              disabled={Boolean(showResults)}
                              onCheckedChange={(checked) =>
                                handleCheckboxChange(question.id, option.id, Boolean(checked))
                              }
                              className="data-[state=checked]:bg-primary data-[state=checked]:text-primary-foreground border-border/70"
                            />
                            <span className="flex-1 text-foreground leading-relaxed">{option.content ?? option.text}</span>
                            {showResults && isChecked && result?.correct ? (
                              <Check className="h-4 w-4 text-emerald-600 dark:text-emerald-400 shrink-0" />
                            ) : null}
                            {showResults && isChecked && result?.correct === false ? (
                              <X className="h-4 w-4 text-destructive shrink-0" />
                            ) : null}
                          </label>
                        );
                      })}
                    </CardContent>
                  </Card>
                );
              })}

              {!showResults && totalQuestions > 0 ? (
                <div className="space-y-2 pt-2">
                  <Button
                    size="lg"
                    disabled={submit.isPending || !canSubmit}
                    onClick={() => submit.mutate()}
                    className="font-bold text-xs h-10 rounded-[--radius-button] px-6"
                  >
                    Submit Quiz
                  </Button>
                  {!canSubmit ? (
                    <p className="text-xs text-muted-foreground/80 font-medium">
                      Please answer all {totalQuestions} question{totalQuestions !== 1 ? "s" : ""} before submitting.
                    </p>
                  ) : null}
                </div>
              ) : null}
            </section>

            <aside className="space-y-4">
              <Card className="border border-border/60 rounded-[--radius-card] shadow-[0_1px_3px_0_rgba(0,0,0,0.01)]">
                <CardHeader className="pb-3">
                  <CardTitle className="text-xs font-bold text-foreground flex items-center gap-2">
                    <History className="h-4 w-4 text-muted-foreground" />
                    Attempt History
                  </CardTitle>
                </CardHeader>
                <CardContent className="space-y-3">
                  {attempts.isLoading ? <PageSkeleton rows={2} /> : null}
                  {attemptList.length === 0 && attempts.isSuccess ? (
                    <p className="text-xs text-muted-foreground/70">No attempts made yet.</p>
                  ) : null}
                  {attemptList.map((attempt) => (
                    <div key={attempt.id} className="rounded-lg border border-border/50 p-3 text-xs space-y-1.5 bg-muted/10">
                      <div className="flex justify-between items-center">
                        <span className="font-bold text-foreground">
                          Attempt {attempt.attemptNo ?? attempt.id}
                        </span>
                        <Badge variant={attempt.passed ? "default" : "secondary"} className={cn(
                          "text-[9px] font-bold px-1.5 py-0 border-none",
                          attempt.passed ? "bg-emerald-500/10 text-emerald-600 dark:text-emerald-400" : "bg-muted text-muted-foreground"
                        )}>
                          {attempt.passed ? "Passed" : "Failed"}
                        </Badge>
                      </div>
                      <div className="flex justify-between text-[11px] text-muted-foreground/80 font-semibold">
                        <span>Score: {attempt.score ?? 0}%</span>
                        <span>{formatDate(attempt.submittedAt)}</span>
                      </div>
                    </div>
                  ))}
                </CardContent>
              </Card>
            </aside>
          </div>
        ) : null}
      </div>
    </AppShell>
  );
}
