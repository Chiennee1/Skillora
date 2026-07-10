"use client";

import * as React from "react";
import Link from "next/link";
import { useQuery } from "@tanstack/react-query";
import { Eye, ShoppingBag } from "lucide-react";

import { MarketplaceLayout } from "@/components/marketplace-layout";
import { EmptyState, ErrorState, PageSkeleton } from "@/components/data-state";
import { PageHeader } from "@/components/page-header";
import { StatusBadge } from "@/components/status-badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import { commerceApi } from "@/lib/api";
import { formatDate, formatMoney } from "@/lib/format";
import { queryKeys } from "@/lib/query-keys";


export function OrdersPage() {
  const [page, setPage] = React.useState(0);
  const orders = useQuery({
    queryKey: queryKeys.orders("me", { page, size: 20 }),
    queryFn: () => commerceApi.getOrders({ page, size: 20 }),
  });

  const pageData = orders.data ?? {
    content: [],
    page: 0,
    size: 0,
    totalElements: 0,
    totalPages: 0,
    first: true,
    last: true,
  };

  const orderList = pageData.content;

  return (
    <MarketplaceLayout>
      <div className="mx-auto max-w-[1400px] px-4 py-8 md:px-6">
        <PageHeader
          title="Order History"
          description="Track payments, pending orders, and check invoices."
        />

        {orders.isLoading ? <PageSkeleton rows={5} /> : null}
        {orders.isError ? (
          <ErrorState
            message={(orders.error as Error).message}
            onRetry={() => orders.refetch()}
          />
        ) : null}

        {orders.isSuccess && orderList.length === 0 ? (
          <EmptyState
            title="No orders found"
            message="You haven't placed any orders yet. Explore our catalog to find courses."
            action={
              <Button asChild>
                <Link href="/">
                  <ShoppingBag className="mr-2 h-4 w-4" />
                  Browse Catalog
                </Link>
              </Button>
            }
          />
        ) : null}

        {orderList.length > 0 ? (
          <div className="space-y-4">
            <Card>
              <CardContent className="p-0">
                <Table>
                  <TableHeader>
                    <TableRow>
                      <TableHead>Order ID</TableHead>
                      <TableHead>Date</TableHead>
                      <TableHead>Total Amount</TableHead>
                      <TableHead>Status</TableHead>
                      <TableHead className="w-[100px] text-right">Actions</TableHead>
                    </TableRow>
                  </TableHeader>
                  <TableBody>
                    {orderList.map((order) => (
                      <TableRow key={order.id}>
                        <TableCell className="font-semibold">#{order.id}</TableCell>
                        <TableCell className="text-muted-foreground">
                          {formatDate(order.createdAt)}
                        </TableCell>
                        <TableCell className="font-medium">
                          {formatMoney(order.totalAmount, order.currency)}
                        </TableCell>
                        <TableCell>
                          <StatusBadge status={order.status} />
                        </TableCell>
                        <TableCell className="text-right">
                          <Button asChild variant="ghost" size="sm">
                            <Link href={`/orders/${order.id}`}>
                              <Eye className="mr-2 h-4 w-4" />
                              View
                            </Link>
                          </Button>
                        </TableCell>
                      </TableRow>
                    ))}
                  </TableBody>
                </Table>
              </CardContent>
            </Card>

            {pageData.totalPages > 1 ? (
              <div className="flex items-center justify-between gap-3 pt-2">
                <p className="text-sm text-muted-foreground">
                  Page {pageData.page + 1} of {pageData.totalPages}
                </p>
                <div className="flex gap-2">
                  <Button
                    variant="outline"
                    size="sm"
                    disabled={pageData.first}
                    onClick={() => setPage((v) => Math.max(0, v - 1))}
                  >
                    Previous
                  </Button>
                  <Button
                    variant="outline"
                    size="sm"
                    disabled={pageData.last}
                    onClick={() => setPage((v) => v + 1)}
                  >
                    Next
                  </Button>
                </div>
              </div>
            ) : null}
          </div>
        ) : null}
      </div>
    </MarketplaceLayout>
  );
}
