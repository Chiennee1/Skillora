import { AuthCard } from "@/components/auth-card";
import { ResetPasswordForm } from "@/features/auth/auth-forms";

export default async function ResetPasswordPage({
  searchParams,
}: {
  searchParams: Promise<{ token?: string }>;
}) {
  const params = await searchParams;

  return (
    <AuthCard title="Choose a new password" description="Use the secure reset token from your email.">
      <ResetPasswordForm token={params.token} />
    </AuthCard>
  );
}

