import Link from "next/link";
import { Compass, Search } from "lucide-react";

import { MarketplaceLayout } from "@/components/marketplace-layout";
import { Button } from "@/components/ui/button";

export default function NotFoundPage() {
  return (
    <MarketplaceLayout>
      <section className="mx-auto flex min-h-[60dvh] max-w-[760px] flex-col items-center justify-center gap-6 px-4 py-16 text-center">
        <div className="rounded-full border bg-primary/10 p-4 text-primary">
          <Compass className="h-8 w-8" />
        </div>
        <div className="space-y-2">
          <p className="font-mono text-xs font-semibold uppercase tracking-[0.18em] text-muted-foreground">
            404
          </p>
          <h1 className="text-3xl font-bold tracking-tight md:text-4xl">
            We could not find that page
          </h1>
          <p className="mx-auto max-w-[56ch] text-sm leading-6 text-muted-foreground">
            The course, workspace, or admin page may have moved. Start from the catalog or search for a course again.
          </p>
        </div>
        <div className="flex flex-wrap justify-center gap-3">
          <Button asChild>
            <Link href="/">
              <Search className="mr-2 h-4 w-4" />
              Browse courses
            </Link>
          </Button>
          <Button asChild variant="outline">
            <Link href="/help">Help center</Link>
          </Button>
        </div>
      </section>
    </MarketplaceLayout>
  );
}
