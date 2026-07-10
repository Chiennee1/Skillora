import { use } from "react";
import { CourseBuilderPage } from "@/features/instructor/course-builder";

export default function CourseBuilderRoute({ params }: { params: Promise<{ courseId: string }> }) {
  const resolvedParams = use(params);
  return <CourseBuilderPage courseId={Number(resolvedParams.courseId)} />;
}
