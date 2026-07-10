import { Suspense } from "react";
import { CatalogPage } from "@/features/catalog/catalog-page";
import { PageSkeleton } from "@/components/data-state";

export default function HomePage() {
  return (
    <Suspense fallback={<PageSkeleton rows={4} />}>
      <CatalogPage />
    </Suspense>
  );
}

