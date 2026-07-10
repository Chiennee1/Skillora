import { use } from "react";
import { AssignmentPage } from "@/features/learner/assignment-page";

export default function AssignmentRoute({ params }: { params: Promise<{ assignmentId: string }> }) {
  const resolvedParams = use(params);
  return <AssignmentPage assignmentId={Number(resolvedParams.assignmentId)} />;
}
