import { MarketplaceLayout } from "@/components/marketplace-layout";

export default function TermsPage() {
  return (
    <MarketplaceLayout>
      <article className="mx-auto max-w-3xl px-4 py-12 md:px-6">
        <div className="space-y-5">
          <p className="text-sm font-medium text-primary">Legal</p>
          <h1 className="text-3xl font-bold tracking-tight">Terms of service</h1>
          <p className="text-muted-foreground">
            These terms describe the baseline rules for using Skillora learning, instructor, and admin services.
            Production legal copy should be reviewed before public launch.
          </p>
          <section className="space-y-3 text-sm leading-7 text-muted-foreground">
            <h2 className="text-lg font-semibold text-foreground">Platform use</h2>
            <p>
              Learners may access enrolled courses for personal learning. Instructors are responsible for the
              accuracy and ownership of submitted course materials.
            </p>
            <h2 className="text-lg font-semibold text-foreground">Payments</h2>
            <p>
              Paid orders are confirmed after gateway verification. Pending orders can be retried without creating
              duplicate enrollments.
            </p>
            <h2 className="text-lg font-semibold text-foreground">Moderation</h2>
            <p>
              Skillora may reject or remove content that violates course quality, security, or platform policies.
            </p>
          </section>
        </div>
      </article>
    </MarketplaceLayout>
  );
}
