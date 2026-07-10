import Link from "next/link";

import { AuthCard } from "@/components/auth-card";
import { RegisterForm } from "@/features/auth/auth-forms";

export default function RegisterPage() {
  return (
    <AuthCard
      title="Create account"
      description="Start as a learner or create an instructor workspace."
      footer={
        <>
          Already registered?{" "}
          <Link href="/login" className="text-primary hover:underline">
            Sign in
          </Link>
        </>
      }
    >
      <RegisterForm />
    </AuthCard>
  );
}

