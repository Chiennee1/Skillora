"use client";

import * as React from "react";
import Image from "next/image";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { Camera, Globe, MapPin, Phone, Save, User } from "lucide-react";
import { toast } from "sonner";

import { AppShell } from "@/components/app-shell";
import { ErrorState, PageSkeleton } from "@/components/data-state";
import { PageHeader } from "@/components/page-header";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Separator } from "@/components/ui/separator";
import { Textarea } from "@/components/ui/textarea";
import { profileApi } from "@/lib/api";
import { queryKeys } from "@/lib/query-keys";
import type { Profile } from "@/lib/types";

export function ProfilePage() {
  const profile = useQuery({
    queryKey: queryKeys.profile,
    queryFn: () => profileApi.me(),
  });

  return (
    <AppShell>
      <PageHeader title="Profile" description="Keep your public learner and instructor information current." />
      {profile.isLoading ? <PageSkeleton rows={4} /> : null}
      {profile.isError ? <ErrorState message={(profile.error as Error).message} onRetry={() => profile.refetch()} /> : null}
      {profile.data ? (
        <ProfileEditor profile={profile.data} />
      ) : null}
    </AppShell>
  );
}

function ProfileEditor({ profile }: { profile: Profile }) {
  const queryClient = useQueryClient();
  const [form, setForm] = React.useState<Partial<Profile>>(profile);
  const update = useMutation({
    mutationFn: () => profileApi.updateMe(form),
    onSuccess: () => {
      toast.success("Profile updated");
      queryClient.invalidateQueries({ queryKey: queryKeys.profile });
      queryClient.invalidateQueries({ queryKey: queryKeys.me });
    },
    onError: (error) => toast.error((error as Error).message),
  });
  const set = (key: keyof Profile, value: string) => setForm((current) => ({ ...current, [key]: value }));

  const avatarUrl = form.avatarUrl?.trim();
  const initials = (form.fullName ?? profile.email ?? "S")
    .split(" ")
    .map((n) => n[0])
    .join("")
    .slice(0, 2)
    .toUpperCase();

  return (
    <div className="grid gap-6 lg:grid-cols-[280px_1fr]">
      {/* Sidebar: avatar preview + summary */}
      <div className="space-y-4">
        <Card>
          <CardContent className="flex flex-col items-center gap-4 pt-6 pb-6">
            {avatarUrl ? (
              <Image
                src={avatarUrl}
                alt="Avatar"
                width={96}
                height={96}
                className="h-24 w-24 rounded-full object-cover border-2 border-primary/20 shadow-md"
              />
            ) : (
              <div className="flex h-24 w-24 items-center justify-center rounded-full bg-primary/10 text-primary font-bold text-2xl border-2 border-primary/20 shadow-md">
                {initials}
              </div>
            )}
            <div className="text-center space-y-1">
              <h3 className="font-semibold text-base">{form.fullName ?? "User"}</h3>
              {form.headline ? (
                <p className="text-xs text-muted-foreground line-clamp-2">{form.headline}</p>
              ) : null}
              <Badge variant="secondary" className="text-[10px]">{profile.email}</Badge>
            </div>
          </CardContent>
        </Card>

        {/* Quick info */}
        <Card>
          <CardContent className="space-y-3 text-xs pt-4 pb-4">
            {form.location ? (
              <div className="flex items-center gap-2 text-muted-foreground">
                <MapPin className="h-3.5 w-3.5 shrink-0" />
                <span>{form.location}</span>
              </div>
            ) : null}
            {form.website ? (
              <div className="flex items-center gap-2 text-muted-foreground">
                <Globe className="h-3.5 w-3.5 shrink-0" />
                <a href={form.website} target="_blank" rel="noreferrer" className="hover:text-primary transition-colors truncate">
                  {form.website}
                </a>
              </div>
            ) : null}
            {form.phone ? (
              <div className="flex items-center gap-2 text-muted-foreground">
                <Phone className="h-3.5 w-3.5 shrink-0" />
                <span>{form.phone}</span>
              </div>
            ) : null}
            {!form.location && !form.website && !form.phone ? (
              <p className="text-muted-foreground text-center py-2">Fill in your details on the right.</p>
            ) : null}
          </CardContent>
        </Card>
      </div>

      {/* Main form */}
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2 text-base">
            <User className="h-4 w-4 text-muted-foreground" />
            Personal Information
          </CardTitle>
        </CardHeader>
        <CardContent className="grid gap-5">
          <div className="grid gap-4 md:grid-cols-2">
            <div className="grid gap-2">
              <Label htmlFor="fullName">Full name</Label>
              <Input id="fullName" value={form.fullName ?? ""} onChange={(event) => set("fullName", event.target.value)} />
            </div>
            <div className="grid gap-2">
              <Label htmlFor="email">Email</Label>
              <Input id="email" value={profile.email} disabled className="bg-muted/30" />
            </div>
            <div className="grid gap-2">
              <Label htmlFor="headline">Headline</Label>
              <Input id="headline" value={form.headline ?? ""} onChange={(event) => set("headline", event.target.value)} placeholder="e.g. Full-stack developer & educator" />
            </div>
            <div className="grid gap-2">
              <Label htmlFor="phone">Phone</Label>
              <Input id="phone" value={form.phone ?? ""} onChange={(event) => set("phone", event.target.value)} placeholder="+84 ..." />
            </div>
            <div className="grid gap-2">
              <Label htmlFor="website">Website</Label>
              <Input id="website" value={form.website ?? ""} onChange={(event) => set("website", event.target.value)} placeholder="https://..." />
            </div>
            <div className="grid gap-2">
              <Label htmlFor="location">Location</Label>
              <Input id="location" value={form.location ?? ""} onChange={(event) => set("location", event.target.value)} placeholder="e.g. Ho Chi Minh City, Vietnam" />
            </div>
          </div>

          <Separator />

          <div className="grid gap-2">
            <Label htmlFor="avatarUrl" className="flex items-center gap-1.5">
              <Camera className="h-3.5 w-3.5 text-muted-foreground" />
              Avatar URL
            </Label>
            <Input id="avatarUrl" value={form.avatarUrl ?? ""} onChange={(event) => set("avatarUrl", event.target.value)} placeholder="https://example.com/your-photo.jpg" />
          </div>
          <div className="grid gap-2">
            <Label htmlFor="bio">Bio</Label>
            <Textarea id="bio" rows={6} value={form.bio ?? ""} onChange={(event) => set("bio", event.target.value)} placeholder="Tell learners and instructors about yourself..." />
          </div>

          <Button className="w-fit" disabled={update.isPending} onClick={() => update.mutate()}>
            <Save className="mr-2 h-4 w-4" />
            Save Profile
          </Button>
        </CardContent>
      </Card>
    </div>
  );
}
