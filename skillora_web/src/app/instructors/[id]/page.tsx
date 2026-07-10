import { use } from "react";

import { PublicInstructorPage } from "@/features/instructor/public-instructor-page";

export default function InstructorProfileRoute({
  params,
}: {
  params: Promise<{ id: string }>;
}) {
  const resolvedParams = use(params);
  return <PublicInstructorPage instructorId={Number(resolvedParams.id)} />;
}
