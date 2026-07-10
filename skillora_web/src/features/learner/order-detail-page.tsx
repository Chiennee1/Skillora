"use client";

import * as React from "react";
import Link from "next/link";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { CreditCard, RefreshCw, XCircle } from "lucide-react";
import { toast } from "sonner";

import { MarketplaceLayout } from "@/components/marketplace-layout";
import { ErrorState, PageSkeleton } from "@/components/data-state";
import { PageHeader } from "@/components/page-header";
import { StatusBadge } from "@/components/status-badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Separator } from "@/components/ui/separator";
import { commerceApi } from "@/lib/api";
import { formatDate, formatMoney } from "@/lib/format";
import { queryKeys } from "@/lib/query-keys";
import type { PaymentCreateResponse } from "@/lib/types";

export function OrderDetailPage({ orderId }: { orderId: number }) {
  const queryClient = useQueryClient();
  const order = useQuery({
    queryKey: queryKeys.order(orderId),
    queryFn: () => commerceApi.getOrder(orderId),
  });

  const pay = useMutation({
    mutationFn: (gateway: "vnpay" | "momo") =>
      commerceApi.createPayment(gateway, orderId),
    onSuccess: (payment: PaymentCreateResponse) => {
      if (payment.payUrl) {
        window.location.href = payment.payUrl;
      } else {
        toast.error("Could not generate payment URL");
      }
    },
    onError: (error) => toast.error((error as Error).message),
  });

  const cancel = useMutation({
    mutationFn: () => commerceApi.cancelOrder(orderId),
    onSuccess: () => {
      toast.success("Order cancelled");
      queryClient.invalidateQueries({ queryKey: queryKeys.order(orderId) });
    },
    onError: (error) => toast.error((error as Error).message),
  });

  const orderData = order.data;

  return (
    <MarketplaceLayout>
      <div className="mx-auto max-w-[1400px] px-4 py-8 md:px-6">
        <PageHeader
          title={`Order #${orderId}`}
          description="View order details and initiate secure payment."
          actions={
            <Button
              variant="outline"
              size="sm"
              onClick={() => order.refetch()}
              disabled={order.isFetching}
            >
              <RefreshCw className={order.isFetching ? "mr-2 h-4 w-4 animate-spin" : "mr-2 h-4 w-4"} />
              Refresh Status
            </Button>
          }
        />

        {order.isLoading ? <PageSkeleton rows={4} /> : null}
        {order.isError ? (
          <ErrorState
            message={(order.error as Error).message}
            onRetry={() => order.refetch()}
          />
        ) : null}

        {orderData ? (
          <div className="grid gap-6 lg:grid-cols-[1fr_360px]">
            <div className="space-y-6">
              {/* Items card */}
              <Card>
                <CardHeader className="flex-row items-center justify-between space-y-0">
                  <CardTitle className="text-base font-semibold">Items in Order</CardTitle>
                  <StatusBadge status={orderData.status} />
                </CardHeader>
                <CardContent className="space-y-4">
                  <div className="divide-y">
                    {orderData.items?.map((item) => (
                      <div
                        key={item.courseId}
                        className="flex justify-between items-start gap-4 py-3 first:pt-0 last:pb-0"
                      >
                        <div>
                          <p className="font-medium text-sm sm:text-base">
                            {item.title ?? item.courseTitleSnapshot}
                          </p>
                          <p className="text-xs text-muted-foreground mt-0.5">
                            Course ID: #{item.courseId}
                          </p>
                        </div>
                        <span className="font-semibold text-sm sm:text-base shrink-0">
                          {formatMoney(item.finalPrice ?? item.price, orderData.currency)}
                        </span>
                      </div>
                    ))}
                  </div>
                  <Separator />
                  <div className="flex flex-wrap justify-between text-xs text-muted-foreground gap-2 pt-1">
                    <span>Order Date: {formatDate(orderData.createdAt)}</span>
                    {orderData.status === "PAID" && (orderData as Record<string, unknown>).paidAt ? (
                      <span>Paid Date: {formatDate(String((orderData as Record<string, unknown>).paidAt))}</span>
                    ) : null}
                  </div>
                </CardContent>
              </Card>

              {/* Actions card for pending orders */}
              {orderData.status === "PENDING" ? (
                <Card className="border-destructive/30 bg-destructive/5">
                  <CardContent className="flex flex-col sm:flex-row items-center justify-between gap-4 p-5">
                    <div className="space-y-1 text-center sm:text-left">
                      <p className="font-semibold text-sm">Need to cancel?</p>
                      <p className="text-xs text-muted-foreground">
                        You can cancel this pending order if you made a mistake or want to modify your cart.
                      </p>
                    </div>
                    <Button
                      variant="destructive"
                      size="sm"
                      onClick={() => cancel.mutate()}
                      disabled={cancel.isPending}
                    >
                      <XCircle className="mr-2 h-4 w-4" />
                      Cancel Order
                    </Button>
                  </CardContent>
                </Card>
              ) : null}
            </div>

            <aside className="space-y-4">
              <Card>
                <CardHeader>
                  <CardTitle className="text-base">Payment Details</CardTitle>
                </CardHeader>
                <CardContent className="space-y-4">
                  <div className="flex justify-between items-baseline">
                    <span className="text-sm text-muted-foreground">Total Amount</span>
                    <span className="text-xl font-bold text-foreground">
                      {formatMoney(orderData.totalAmount, orderData.currency)}
                    </span>
                  </div>

                  {orderData.status === "PENDING" ? (
                    <div className="grid gap-2 pt-2">
                      <Button
                        className="w-full"
                        disabled={pay.isPending}
                        onClick={() => pay.mutate("vnpay")}
                      >
                        <CreditCard className="mr-2 h-4 w-4" />
                        Pay with VNPay
                      </Button>
                      <Button
                        className="w-full"
                        variant="outline"
                        disabled={pay.isPending}
                        onClick={() => pay.mutate("momo")}
                      >
                        Pay with MoMo
                      </Button>
                    </div>
                  ) : (
                    <div className="rounded-md border p-3 text-center text-sm text-muted-foreground bg-muted/20">
                      {orderData.status === "PAID"
                        ? "This order has been paid. Happy learning!"
                        : "This order has been cancelled and cannot be paid."}
                    </div>
                  )}

                  <Separator />

                  <div className="flex justify-center gap-2">
                    <Button asChild variant="ghost" size="sm" className="w-full">
                      <Link href="/orders">Order History</Link>
                    </Button>
                  </div>
                </CardContent>
              </Card>
            </aside>
          </div>
        ) : null}
      </div>
    </MarketplaceLayout>
  );
}
