import Link from "next/link";
import type { ComponentProps } from "react";
import { Bot } from "lucide-react";

import { Button } from "@/components/ui/button";
import { cn } from "@/lib/utils";

type AskAiButtonProps = Omit<ComponentProps<typeof Button>, "asChild"> & {
  courseId?: number | string | null;
  prompt?: string | null;
  label?: string;
};

export function buildChatHref({
  courseId,
  prompt,
}: {
  courseId?: number | string | null;
  prompt?: string | null;
}) {
  const params = new URLSearchParams();

  if (courseId !== undefined && courseId !== null && courseId !== "") {
    params.set("courseId", String(courseId));
  }

  if (prompt?.trim()) {
    params.set("prompt", prompt.trim());
  }

  const query = params.toString();
  return query ? `/chat?${query}` : "/chat";
}

export function AskAiButton({
  courseId,
  prompt,
  label = "Ask AI",
  className,
  variant = "outline",
  size = "sm",
  ...props
}: AskAiButtonProps) {
  return (
    <Button
      asChild
      className={cn("gap-2", className)}
      size={size}
      variant={variant}
      {...props}
    >
      <Link href={buildChatHref({ courseId, prompt })}>
        <Bot className="h-4 w-4" />
        {label}
      </Link>
    </Button>
  );
}
