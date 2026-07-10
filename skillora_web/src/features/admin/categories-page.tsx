"use client";

import * as React from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { Edit2, Plus, Trash2 } from "lucide-react";
import { toast } from "sonner";

import { AppShell } from "@/components/app-shell";
import { ConfirmActionDialog } from "@/components/confirm-action-dialog";
import { EmptyState, ErrorState, PageSkeleton } from "@/components/data-state";
import { PageHeader } from "@/components/page-header";
import { RoleGuard } from "@/components/role-guard";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from "@/components/ui/dialog";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import { Textarea } from "@/components/ui/textarea";
import { adminApi, courseApi } from "@/lib/api";
import { queryKeys } from "@/lib/query-keys";
import type { Category } from "@/lib/types";

export function CategoriesPage() {
  const queryClient = useQueryClient();
  const [isCreateOpen, setIsCreateOpen] = React.useState(false);
  const [editingCategory, setEditingCategory] = React.useState<Category | null>(null);

  // Form states
  const [name, setName] = React.useState("");
  const [description, setDescription] = React.useState("");

  const categories = useQuery({
    queryKey: queryKeys.categories,
    queryFn: () => courseApi.categories(),
  });

  const createMutation = useMutation({
    mutationFn: (body: { name: string; description?: string }) =>
      adminApi.createCategory(body),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.categories });
      toast.success("Category created successfully");
      setIsCreateOpen(false);
      setName("");
      setDescription("");
    },
    onError: (error) => toast.error((error as Error).message),
  });

  const updateMutation = useMutation({
    mutationFn: ({ id, body }: { id: number; body: { name: string; description?: string } }) =>
      adminApi.updateCategory(id, body),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.categories });
      toast.success("Category updated successfully");
      setEditingCategory(null);
      setName("");
      setDescription("");
    },
    onError: (error) => toast.error((error as Error).message),
  });

  const deleteMutation = useMutation({
    mutationFn: (id: number) => adminApi.deleteCategory(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.categories });
      toast.success("Category deleted successfully");
    },
    onError: (error) => toast.error((error as Error).message),
  });

  const handleEditClick = (category: Category) => {
    setEditingCategory(category);
    setName(category.name);
    setDescription(category.description ?? "");
  };

  const handleCreateSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!name.trim()) return;
    createMutation.mutate({ name: name.trim(), description: description.trim() || undefined });
  };

  const handleUpdateSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!editingCategory || !name.trim()) return;
    updateMutation.mutate({
      id: editingCategory.id,
      body: { name: name.trim(), description: description.trim() || undefined },
    });
  };

  const categoryList = categories.data ?? [];

  return (
    <RoleGuard roles={["ADMIN"]}>
      <AppShell>
        <PageHeader
          title="Categories"
          description="Manage course categories on the platform."
          actions={
          <Dialog open={isCreateOpen} onOpenChange={setIsCreateOpen}>
            <DialogTrigger asChild>
              <Button onClick={() => { setName(""); setDescription(""); }}>
                <Plus className="mr-2 h-4 w-4" />
                Add Category
              </Button>
            </DialogTrigger>
            <DialogContent>
              <form onSubmit={handleCreateSubmit}>
                <DialogHeader>
                  <DialogTitle>Add Category</DialogTitle>
                  <DialogDescription>
                    Create a new category for courses on the marketplace.
                  </DialogDescription>
                </DialogHeader>
                <div className="grid gap-4 py-4">
                  <div className="grid gap-2">
                    <Label htmlFor="create-name">Name</Label>
                    <Input
                      id="create-name"
                      value={name}
                      onChange={(e) => setName(e.target.value)}
                      placeholder="e.g. Web Development"
                      required
                    />
                  </div>
                  <div className="grid gap-2">
                    <Label htmlFor="create-desc">Description</Label>
                    <Textarea
                      id="create-desc"
                      value={description}
                      onChange={(e) => setDescription(e.target.value)}
                      placeholder="Describe this category..."
                      rows={3}
                    />
                  </div>
                </div>
                <DialogFooter>
                  <Button
                    type="button"
                    variant="outline"
                    onClick={() => setIsCreateOpen(false)}
                  >
                    Cancel
                  </Button>
                  <Button type="submit" disabled={createMutation.isPending}>
                    Create
                  </Button>
                </DialogFooter>
              </form>
            </DialogContent>
          </Dialog>
          }
        />

        {categories.isLoading ? <PageSkeleton rows={5} /> : null}
        {categories.isError ? (
          <ErrorState
            message={(categories.error as Error).message}
            onRetry={() => categories.refetch()}
          />
        ) : null}

        {categories.isSuccess && categoryList.length === 0 ? (
          <EmptyState
            title="No categories"
            message="Get started by creating your first course category."
          />
        ) : null}

        {categoryList.length > 0 ? (
          <Card>
            <CardContent className="p-0">
              <Table>
                <TableHeader>
                  <TableRow>
                    <TableHead>Name</TableHead>
                    <TableHead>Description</TableHead>
                    <TableHead className="w-[120px] text-right">Actions</TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {categoryList.map((category) => (
                    <TableRow key={category.id}>
                      <TableCell className="font-medium">{category.name}</TableCell>
                      <TableCell className="text-muted-foreground">
                        {category.description ?? "No description"}
                      </TableCell>
                      <TableCell className="text-right">
                        <div className="flex justify-end gap-2">
                          <Button
                            variant="ghost"
                            size="icon"
                            onClick={() => handleEditClick(category)}
                            aria-label="Edit category"
                          >
                            <Edit2 className="h-4 w-4" />
                          </Button>
                          <ConfirmActionDialog
                            title="Delete category?"
                            description={`Are you sure you want to delete "${category.name}"? This action cannot be undone.`}
                            confirmLabel="Delete"
                            variant="destructive"
                            onConfirm={() => deleteMutation.mutate(category.id)}
                            disabled={deleteMutation.isPending}
                            trigger={
                              <Button
                                variant="ghost"
                                size="icon"
                                className="text-destructive hover:text-destructive"
                                aria-label="Delete category"
                              >
                                <Trash2 className="h-4 w-4" />
                              </Button>
                            }
                          />
                        </div>
                      </TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            </CardContent>
          </Card>
        ) : null}

        {/* Edit Dialog */}
        <Dialog
          open={editingCategory !== null}
          onOpenChange={(open) => !open && setEditingCategory(null)}
        >
          <DialogContent>
            {editingCategory ? (
              <form onSubmit={handleUpdateSubmit}>
                <DialogHeader>
                  <DialogTitle>Edit Category</DialogTitle>
                  <DialogDescription>Update the category details.</DialogDescription>
                </DialogHeader>
                <div className="grid gap-4 py-4">
                  <div className="grid gap-2">
                    <Label htmlFor="edit-name">Name</Label>
                    <Input
                      id="edit-name"
                      value={name}
                      onChange={(e) => setName(e.target.value)}
                      required
                    />
                  </div>
                  <div className="grid gap-2">
                    <Label htmlFor="edit-desc">Description</Label>
                    <Textarea
                      id="edit-desc"
                      value={description}
                      onChange={(e) => setDescription(e.target.value)}
                      rows={3}
                    />
                  </div>
                </div>
                <DialogFooter>
                  <Button
                    type="button"
                    variant="outline"
                    onClick={() => setEditingCategory(null)}
                  >
                    Cancel
                  </Button>
                  <Button type="submit" disabled={updateMutation.isPending}>
                    Save Changes
                  </Button>
                </DialogFooter>
              </form>
            ) : null}
          </DialogContent>
        </Dialog>
      </AppShell>
    </RoleGuard>
  );
}
