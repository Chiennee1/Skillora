import { Suspense } from "react";
import { PaymentResultPage } from "@/features/learner/payment-result-page";
import { PageSkeleton } from "@/components/data-state";

export default function PaymentResultRoute() {
  return (
    <Suspense fallback={<PageSkeleton rows={4} />}>
      <PaymentResultPage />
    </Suspense>
  );
}
