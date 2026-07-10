/* Centralized React Query key factory */

export const queryKeys = {
  /* Auth */
  me: ["me"] as const,
  profile: ["profile", "me"] as const,

  /* Catalog */
  categories: ["categories"] as const,
  courses: (filters?: Record<string, unknown>) => ["courses", filters] as const,
  course: (idOrSlug: string | number) => ["course", idOrSlug] as const,
  sections: (courseId: number | undefined) => ["sections", courseId] as const,
  reviews: (courseId: number | undefined, page?: number) => ["reviews", courseId, page] as const,

  /* Learner */
  learningDashboard: ["learning-dashboard"] as const,
  enrollmentsRoot: ["enrollments"] as const,
  enrollments: (scope: string, filters?: Record<string, unknown>) =>
    ["enrollments", scope, filters] as const,
  enrollmentProgress: (enrollmentId: number) =>
    ["enrollment-progress", enrollmentId] as const,
  lesson: (lessonId: number) => ["lesson", lessonId] as const,

  /* Commerce */
  cart: ["cart"] as const,
  wishlist: ["wishlist"] as const,
  ordersRoot: ["orders"] as const,
  orders: (scope: string, filters?: Record<string, unknown>) =>
    ["orders", scope, filters] as const,
  order: (orderId: number | null) => ["order", orderId] as const,

  /* Practice */
  quiz: (quizId: number) => ["quiz", quizId] as const,
  quizAttempts: (quizId: number) => ["quiz-attempts", quizId] as const,
  assignment: (assignmentId: number) => ["assignment", assignmentId] as const,

  /* Notifications */
  notificationsRoot: ["notifications"] as const,
  notifications: (filters?: Record<string, unknown>) =>
    ["notifications", filters] as const,

  /* Chat */
  chatConversationsRoot: ["chat-conversations"] as const,
  chatConversations: (filters?: Record<string, unknown>) =>
    ["chat-conversations", filters] as const,
  chatMessages: (conversationId: number, filters?: Record<string, unknown>) =>
    ["chat-messages", conversationId, filters] as const,

  /* Instructor */
  instructorCoursesRoot: ["instructor-courses"] as const,
  instructorCourses: (filters?: Record<string, unknown>) =>
    ["instructor-courses", filters] as const,
  builderCourse: (courseId: number) => ["builder-course", courseId] as const,
  builderSections: (courseId: number) => ["builder-sections", courseId] as const,
  courseVersions: (courseId: number) => ["course-versions", courseId] as const,
  assignmentSubmissions: (assignmentId: string | number) =>
    ["assignment-submissions", assignmentId] as const,

  /* Admin */
  adminDashboard: ["admin-dashboard"] as const,
  adminRevenue: ["admin-revenue"] as const,
  adminCoursesRoot: ["admin-courses"] as const,
  adminCourses: (status?: string) => ["admin-courses", status] as const,
  adminCourseDetail: (courseId: number) =>
    ["admin-course-detail", courseId] as const,
  adminCourseVersions: ["admin-course-versions"] as const,
  adminUsersRoot: ["admin-users"] as const,
  adminUsers: (filters?: Record<string, unknown>) =>
    ["admin-users", filters] as const,
  adminCoupons: ["admin-coupons"] as const,
  auditLogsRoot: ["audit-logs"] as const,
  auditLogs: (filters?: Record<string, unknown>) =>
    ["audit-logs", filters] as const,
};
