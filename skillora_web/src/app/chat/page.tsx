import { Suspense } from "react";

import { ChatPage } from "@/features/learner/chat-page";

export default function ChatRoute() {
  return (
    <Suspense fallback={null}>
      <ChatPage />
    </Suspense>
  );
}
