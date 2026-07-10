import Link from "next/link";

import { BrandLogo } from "@/components/brand-logo";
import { ThemeToggle } from "@/components/theme-toggle";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";

export function AuthCard({
  title,
  description,
  children,
  footer,
}: {
  title: string;
  description: string;
  children: React.ReactNode;
  footer?: React.ReactNode;
}) {
  return (
    <main id="main-content" className="app-surface grid min-h-[100dvh] px-4 py-6 lg:grid-cols-[minmax(0,1fr)_520px] lg:p-6">
      <div className="absolute right-4 top-4">
        <ThemeToggle />
      </div>

      <section className="hidden min-h-[calc(100dvh-3rem)] overflow-hidden rounded-[--radius-panel] border border-border/70 bg-primary/10 p-10 shadow-[var(--premium-shadow)] lg:flex lg:flex-col lg:justify-between">
        <Link href="/" className="w-fit">
          <BrandLogo size="lg" variant="full" />
        </Link>
        <div className="max-w-2xl space-y-6">
          <p className="text-sm font-semibold text-primary">Skillora marketplace</p>
          <h1 className="max-w-[12ch] text-6xl font-black leading-[0.95] tracking-[-0.055em] text-balance">
            Learn with a cleaner path.
          </h1>
          <p className="max-w-[52ch] text-base leading-7 text-muted-foreground">
            Courses, practice, checkout, instructor publishing, and admin review stay in one focused learning platform.
          </p>
        </div>
        <div className="grid max-w-xl grid-cols-3 gap-3 text-xs">
          {["Catalog", "Practice", "Review"].map((item) => (
            <div key={item} className="rounded-[--radius-card] border border-border/70 bg-background/70 p-4 font-semibold">
              {item}
            </div>
          ))}
        </div>
      </section>

      <section className="flex min-h-[calc(100dvh-3rem)] items-center justify-center py-12 lg:py-0">
        <div className="w-full max-w-md">
          <Link href="/" className="mb-8 flex items-center justify-center gap-2.5 lg:hidden">
            <BrandLogo size="md" variant="full" />
          </Link>
          <Card className="border-border/70 bg-card/92 shadow-[var(--premium-shadow)]">
            <CardHeader className="text-center pb-2">
              <CardTitle className="text-2xl font-black tracking-[-0.035em]">{title}</CardTitle>
              <CardDescription className="text-sm leading-6">{description}</CardDescription>
            </CardHeader>
            <CardContent className="space-y-5 pt-2">
              {children}
              {footer ? (
                <div className="border-t pt-5 text-center text-sm text-muted-foreground">
                  {footer}
                </div>
              ) : null}
            </CardContent>
          </Card>
          <p className="mt-6 text-center text-xs text-muted-foreground">
            &copy; {new Date().getFullYear()} Skillora. All rights reserved.
          </p>
        </div>
      </section>
    </main>
  );
}
