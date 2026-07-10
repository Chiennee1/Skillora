import Link from "next/link";

import { AuthCard } from "@/components/auth-card";
import { ForgotPasswordForm } from "@/features/auth/auth-forms";

export default function ForgotPasswordPage() {
  return (
    <AuthCard
      title="Reset password"
      description="Enter your email and Skillora will send a reset link when the account exists."
      footer={
        <Link href="/login" className="text-primary hover:underline">
          Back to sign in
        </Link>
      }
    >
      <ForgotPasswordForm />
    </AuthCard>
  );
}

