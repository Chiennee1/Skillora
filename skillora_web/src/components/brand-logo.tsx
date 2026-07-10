"use client";

import Link from "next/link";
import { cn } from "@/lib/utils";

type BrandLogoProps = {
  size?: "sm" | "md" | "lg";
  variant?: "mark" | "wordmark" | "full";
  href?: string;
  className?: string;
};

const sizeMap = {
  sm: { mark: 24, text: "text-sm", gap: "gap-1.5" },
  md: { mark: 32, text: "text-base", gap: "gap-2" },
  lg: { mark: 40, text: "text-xl", gap: "gap-2.5" },
};

function SkillMark({ size }: { size: number }) {
  return (
    <svg
      width={size}
      height={size}
      viewBox="0 0 40 40"
      fill="none"
      xmlns="http://www.w3.org/2000/svg"
      aria-hidden="true"
      className="shrink-0"
    >
      <rect width="40" height="40" rx="10" className="fill-primary" />
      <path
        d="M12 28C12 28 14.5 25 20 25C25.5 25 28 28 28 28"
        stroke="white"
        strokeWidth="2.5"
        strokeLinecap="round"
        opacity="0.5"
      />
      <path
        d="M12 21C12 21 14.5 18 20 18C25.5 18 28 21 28 21"
        stroke="white"
        strokeWidth="2.5"
        strokeLinecap="round"
        opacity="0.75"
      />
      <path
        d="M12 14C12 14 14.5 11 20 11C25.5 11 28 14 28 14"
        stroke="white"
        strokeWidth="2.5"
        strokeLinecap="round"
      />
    </svg>
  );
}

function LogoContent({ size = "md", variant = "full" }: BrandLogoProps) {
  const s = sizeMap[size];
  const showMark = variant === "mark" || variant === "full";
  const showText = variant === "wordmark" || variant === "full";

  return (
    <span className={cn("inline-flex items-center", s.gap)}>
      {showMark ? <SkillMark size={s.mark} /> : null}
      {showText ? (
        <span
          className={cn(
            "font-semibold tracking-tight text-foreground",
            s.text,
          )}
        >
          Skillora
        </span>
      ) : null}
    </span>
  );
}

export function BrandLogo({
  size = "md",
  variant = "full",
  href,
  className,
}: BrandLogoProps) {
  const content = <LogoContent size={size} variant={variant} />;

  if (href) {
    return (
      <Link
        href={href}
        className={cn("inline-flex items-center focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring rounded-sm", className)}
        aria-label="Skillora home"
      >
        {content}
      </Link>
    );
  }

  return <span className={cn("inline-flex items-center", className)}>{content}</span>;
}
