import { NextResponse } from "next/server";

export function GET() {
  return NextResponse.json({
    success: true,
    service: "skillora-web",
    status: "UP",
    timestamp: new Date().toISOString(),
  });
}
