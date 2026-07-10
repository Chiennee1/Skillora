import { use } from "react";
import { OrderDetailPage } from "@/features/learner/order-detail-page";

export default function OrderDetailRoute({ params }: { params: Promise<{ orderId: string }> }) {
  const resolvedParams = use(params);
  return <OrderDetailPage orderId={Number(resolvedParams.orderId)} />;
}
