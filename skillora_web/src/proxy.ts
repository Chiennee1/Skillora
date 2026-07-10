import { NextRequest, NextResponse } from "next/server";

const USER_COOKIE = "skillora_user";

const protectedRoutes = [
  { prefix: "/admin", roles: ["ADMIN"] },
  { prefix: "/instructor", roles: ["INSTRUCTOR", "ADMIN"] },
  { prefix: "/dashboard", roles: ["STUDENT", "INSTRUCTOR", "ADMIN"] },
  { prefix: "/learn", roles: ["STUDENT"] },
  { prefix: "/cart", roles: ["STUDENT"] },
  { prefix: "/orders", roles: ["STUDENT"] },
  { prefix: "/wishlist", roles: ["STUDENT"] },
  { prefix: "/quizzes", roles: ["STUDENT"] },
  { prefix: "/assignments", roles: ["STUDENT"] },
  { prefix: "/chat", roles: ["STUDENT", "INSTRUCTOR", "ADMIN"] },
  { prefix: "/profile", roles: ["STUDENT", "INSTRUCTOR", "ADMIN"] },
  { prefix: "/notifications", roles: ["STUDENT", "INSTRUCTOR", "ADMIN"] },
];

function readRoles(request: NextRequest): string[] {
  const raw = request.cookies.get(USER_COOKIE)?.value;
  if (!raw) {
    return [];
  }
  try {
    const parsed = JSON.parse(decodeURIComponent(raw)) as { roles?: string[] };
    return parsed.roles ?? [];
  } catch {
    return [];
  }
}

function routeMatches(pathname: string, prefix: string) {
  return pathname === prefix || pathname.startsWith(`${prefix}/`);
}

export function proxy(request: NextRequest) {
  const match = protectedRoutes.find((route) => routeMatches(request.nextUrl.pathname, route.prefix));
  if (!match) {
    return NextResponse.next();
  }

  const roles = readRoles(request);
  if (roles.length === 0) {
    const url = request.nextUrl.clone();
    url.pathname = "/login";
    url.searchParams.set("next", request.nextUrl.pathname);
    return NextResponse.redirect(url);
  }

  if (!match.roles.some((role) => roles.includes(role))) {
    const url = request.nextUrl.clone();
    url.pathname = roles.includes("ADMIN") ? "/admin" : roles.includes("INSTRUCTOR") ? "/instructor" : "/dashboard";
    url.search = "";
    return NextResponse.redirect(url);
  }

  return NextResponse.next();
}

export const config = {
  matcher: [
    "/admin/:path*",
    "/instructor/:path*",
    "/dashboard/:path*",
    "/learn/:path*",
    "/cart/:path*",
    "/orders/:path*",
    "/wishlist/:path*",
    "/quizzes/:path*",
    "/assignments/:path*",
    "/chat/:path*",
    "/profile/:path*",
    "/notifications/:path*",
  ],
};
