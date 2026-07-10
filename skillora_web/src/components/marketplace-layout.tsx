"use client";

import Link from "next/link";

import { MarketplaceHeader } from "@/components/marketplace-header";

export function MarketplaceLayout({ children }: { children: React.ReactNode }) {
  return (
    <div className="app-surface flex min-h-[100dvh] flex-col bg-background">
      <MarketplaceHeader />
      <main id="main-content" className="flex-1">
        {children}
      </main>
      <footer className="border-t border-border/70 bg-background/72 backdrop-blur">
        <div className="mx-auto flex max-w-[1400px] flex-col gap-4 px-4 py-8 text-sm text-muted-foreground md:flex-row md:items-center md:justify-between md:px-6">
          <p>&copy; {new Date().getFullYear()} Skillora. Learn with better structure.</p>
          <nav className="flex flex-wrap gap-4">
            <Link href="/terms" className="transition-colors hover:text-foreground">Terms</Link>
            <Link href="/privacy" className="transition-colors hover:text-foreground">Privacy</Link>
            <Link href="/help" className="transition-colors hover:text-foreground">Help</Link>
          </nav>
        </div>
      </footer>
    </div>
  );
}
