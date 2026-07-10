import type { Metadata } from "next";
import { CourseDetailPage } from "@/features/course/course-detail-page";

const API_BASE_URL =
  process.env.API_BASE_URL?.replace(/\/$/, "") ??
  process.env.NEXT_PUBLIC_API_BASE_URL?.replace(/\/$/, "") ??
  "http://localhost:8080";

type Props = {
  params: Promise<{ idOrSlug: string }>;
};

// Next.js Dynamic Metadata generation for SEO optimization
export async function generateMetadata({ params }: Props): Promise<Metadata> {
  const { idOrSlug } = await params;
  try {
    const res = await fetch(`${API_BASE_URL}/api/v1/courses/${idOrSlug}`, {
      cache: "no-store",
    });

    if (!res.ok) {
      return { title: "Course Details | Skillora" };
    }

    const payload = await res.json();
    const course = payload?.data;

    if (!course) {
      return { title: "Course Details | Skillora" };
    }

    const title = `${course.title} | Skillora`;
    const description = course.subtitle || course.description || "Learn from industry experts on Skillora.";
    const imageUrl = course.thumbnailUrl || "";

    return {
      title,
      description,
      openGraph: {
        title,
        description,
        type: "video.other",
        url: `${process.env.NEXT_PUBLIC_APP_URL || "http://localhost:3000"}/courses/${idOrSlug}`,
        images: imageUrl ? [{ url: imageUrl, alt: course.title }] : [],
      },
      twitter: {
        card: "summary_large_image",
        title,
        description,
        images: imageUrl ? [imageUrl] : [],
      },
    };
  } catch {
    return { title: "Course Details | Skillora" };
  }
}

export default async function CoursePage({ params }: Props) {
  const { idOrSlug } = await params;
  return <CourseDetailPage idOrSlug={idOrSlug} />;
}
