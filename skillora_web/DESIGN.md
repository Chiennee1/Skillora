# Design System: Skillora Web

## 1. Product Direction

Skillora is a professional online learning marketplace with three surfaces: public course discovery, learner workspace, and role-based operations for instructors and admins. The direction is **Skillora marketplace, not Udemy clone**: familiar course flows, cleaner density, stronger hierarchy, and a calm teal brand.

- Visual promise: structured learning, trustworthy operations, practical course creation.
- Signature: teal editorial hero on public pages, disciplined product surfaces everywhere else.
- Motion: restrained hover, active, sheet, dialog, accordion, and skeleton transitions only.
- Density: comfortable on marketplace/learner pages, compact on admin and instructor work screens.

## 2. Brand Tokens

- Typography: Geist Sans for UI, Geist Mono for numeric IDs, timestamps, tokens, prices, and audit details.
- Primary: Skillora teal via `--primary`, used for CTAs, focus rings, active nav, progress, and positive emphasis.
- Neutral base: cool slate/zinc neutrals through shadcn CSS variables, not beige and not AI-purple.
- Radius: `--radius-button` for controls, `--radius-input` for fields, `--radius-card` for repeated surfaces, `--radius-panel` for large hero/auth panels.
- Shadows: use `--premium-shadow` and `--premium-shadow-hover`; avoid ad-hoc black shadows unless the surface is media/video.
- Utilities: `app-surface`, `panel-ring`, `glass-panel`, `text-balance`, `text-pretty`, `course-card-image`, and `mobile-purchase-bar`.

## 3. Layout Patterns

- Public marketplace routes use `MarketplaceLayout`, `MarketplaceHeader`, and one `main#main-content` from the layout.
- App routes use `AppShell` with role-aware navigation and one `main#main-content`.
- Public content width is `max-w-[1400px]`; workspace content width is `max-w-[1500px]`.
- Course detail uses desktop sticky purchase card plus mobile bottom purchase bar.
- Tables live inside cards and rely on horizontal overflow from the shared `Table` component.
- Forms use shadcn `Label`, `Input`, `Textarea`, `Select`, `Checkbox`, and submit semantics where possible.

## 4. Surface Rules

- Cards are for course entities, checkout/order panels, dashboard metrics, moderation work, form sections, and chat panes.
- Avoid nested cards; use separators, table sections, tabs, accordions, and spacing inside a card.
- Keep dashboard metrics through `MetricCard` unless a workflow requires a custom card.
- Use `EmptyState`, `ErrorState`, and `PageSkeleton` instead of loose text or spinner-only screens.
- Prefer `StatusBadge` for operational states and keep destructive actions behind confirmation dialogs.

## 5. Route Quality Bar

Every primary route should have:

- A clear `PageHeader` or public hero with one primary action.
- Loading state resembling the final layout.
- Error state with retry when data can be refetched.
- Empty state with a next action or clear instruction.
- Keyboard-visible focus, 40px minimum touch targets, and no dead `href="#"`.
- Responsive collapse below tablet width without overlap or clipped text.

## 6. Role Surfaces

- Learner: catalog, course detail, wishlist, cart, orders, payment result, dashboard, lesson player, quiz, assignment, notifications, chat.
- Instructor: workspace, course creation, builder, grading queue, version draft workflow, public instructor profile.
- Admin: dashboard, review queue, courses, users, categories, coupons, audit logs.
- Chat: two-pane on desktop, stacked on mobile, with retry/copy states and course context badges.

## 7. Accessibility And Performance

- Preserve the root skip link and avoid nested `main` landmarks inside layouts.
- Meaningful images need descriptive alt text and should not use `unoptimized` unless there is a backend constraint.
- Prioritize only above-the-fold media: hero images, first course cards, and key profile imagery.
- Use `min-h-[100dvh]`, not `h-screen`, for full-height shells.
- Respect reduced motion from `globals.css`; do not add custom motion that bypasses it.

## 8. Anti-Patterns

- No clone of Udemy logo, colors, copy, or proprietary assets.
- No purple/blue AI glow palette or unrelated accent colors.
- No pure-black section jumps in the light theme.
- No placeholder-only labels, unlabeled controls, or mobile-only hidden context.
- No spinner-only loading screens.
- No `window.confirm`, `window.alert`, or vague "Oops" errors.
- No success messages with exclamation marks.
- No extra `<main>` inside `MarketplaceLayout` or `AppShell`.
