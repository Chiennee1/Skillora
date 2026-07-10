import Link from "next/link";

import { AuthCard } from "@/components/auth-card";
import { LoginForm } from "@/features/auth/auth-forms";

export default async function LoginPage({
  searchParams,
}: {
  searchParams: Promise<{ next?: string }>;
}) {
  const params = await searchParams;

  return (
    <AuthCard
      title="Sign in"
      description="Access your courses, teaching workspace, or admin review queue."
      footer={
        <>
          New to Skillora?{" "}
          <Link href="/register" className="text-primary hover:underline">
            Create an account
          </Link>
        </>
      }
    >
      <LoginForm next={params.next} />
    </AuthCard>
  );
}

