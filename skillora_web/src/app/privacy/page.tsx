import { MarketplaceLayout } from "@/components/marketplace-layout";

export default function PrivacyPage() {
  return (
    <MarketplaceLayout>
      <article className="mx-auto max-w-3xl px-4 py-12 md:px-6">
        <div className="space-y-5">
          <p className="text-sm font-medium text-primary">Privacy</p>
          <h1 className="text-3xl font-bold tracking-tight">Privacy policy</h1>
          <p className="text-muted-foreground">
            Skillora uses account, course, order, and learning-progress data to provide the platform experience.
            Replace this page with reviewed legal copy before production launch.
          </p>
          <section className="space-y-3 text-sm leading-7 text-muted-foreground">
            <h2 className="text-lg font-semibold text-foreground">Data we use</h2>
            <p>
              We process profile information, enrollments, payment status, quiz attempts, submissions, reviews,
              notifications, and support chat messages.
            </p>
            <h2 className="text-lg font-semibold text-foreground">Security</h2>
            <p>
              Authentication tokens, payment callbacks, and admin actions are protected by backend validation,
              role checks, audit logs, and rate limits.
            </p>
            <h2 className="text-lg font-semibold text-foreground">Contact</h2>
            <p>For privacy requests, contact the Skillora platform operator configured for your deployment.</p>
          </section>
        </div>
      </article>
    </MarketplaceLayout>
  );
}
