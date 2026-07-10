import { NextRequest, NextResponse } from "next/server";

const API_BASE_URL =
  process.env.API_BASE_URL?.replace(/\/$/, "") ??
  process.env.NEXT_PUBLIC_API_BASE_URL?.replace(/\/$/, "") ??
  "http://localhost:8080";

const ACCESS_COOKIE = "skillora_access_token";
const REFRESH_COOKIE = "skillora_refresh_token";
const USER_COOKIE = "skillora_user";

type AuthPayload = {
  success?: boolean;
  data?: {
    accessToken?: string;
    refreshToken?: string;
    expiresIn?: number;
    user?: unknown;
  };
};

type CookieUser = {
  id?: number;
  roles?: string[];
};

type RouteContext = {
  params: Promise<{ path: string[] }>;
};

type ErrorPayload = {
  success: false;
  message: string;
  errors?: string[];
  timestamp: string;
};

function jsonError(message: string, status: number, errors?: string[]) {
  return NextResponse.json(
    {
      success: false,
      message,
      errors,
      timestamp: new Date().toISOString(),
    } satisfies ErrorPayload,
    { status },
  );
}

function cookieOptions(maxAge?: number) {
  return {
    httpOnly: true,
    sameSite: "lax" as const,
    secure: process.env.NODE_ENV === "production",
    path: "/",
    maxAge,
  };
}

function clearAuth(response: NextResponse) {
  response.cookies.set(ACCESS_COOKIE, "", cookieOptions(0));
  response.cookies.set(REFRESH_COOKIE, "", cookieOptions(0));
  response.cookies.set(USER_COOKIE, "", cookieOptions(0));
}

function serializeUserForCookie(user: unknown) {
  if (!user || typeof user !== "object") {
    return null;
  }
  const value = user as CookieUser;
  return {
    id: value.id,
    roles: Array.isArray(value.roles) ? value.roles : [],
  };
}

function applyAuthCookies(response: NextResponse, payload: AuthPayload) {
  const auth = payload.data;
  if (!auth?.accessToken || !auth.refreshToken) {
    return;
  }
  response.cookies.set(ACCESS_COOKIE, auth.accessToken, cookieOptions(auth.expiresIn ?? 900));
  response.cookies.set(REFRESH_COOKIE, auth.refreshToken, cookieOptions(60 * 60 * 24 * 30));
  if (auth.user) {
    const cookieUser = serializeUserForCookie(auth.user);
    if (cookieUser) {
      response.cookies.set(USER_COOKIE, encodeURIComponent(JSON.stringify(cookieUser)), cookieOptions(60 * 60 * 24 * 30));
    }
  }
}

async function readBody(request: NextRequest) {
  if (request.method === "GET" || request.method === "HEAD") {
    return undefined;
  }
  const contentType = request.headers.get("content-type") ?? "";
  if (contentType.includes("multipart/form-data")) {
    return request.formData();
  }
  const text = await request.text();
  return text || undefined;
}

function parseJson<T>(text: string): T | null {
  if (!text) {
    return null;
  }
  try {
    return JSON.parse(text) as T;
  } catch {
    return null;
  }
}

async function refreshAccessToken(request: NextRequest) {
  const refreshToken = request.cookies.get(REFRESH_COOKIE)?.value;
  if (!refreshToken) {
    return null;
  }

  try {
    const response = await fetch(`${API_BASE_URL}/api/v1/auth/refresh`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ refreshToken }),
      cache: "no-store",
    });

    const text = await response.text();
    if (!response.ok) {
      return null;
    }

    return parseJson<AuthPayload>(text);
  } catch {
    return null;
  }
}

async function forward(request: NextRequest, path: string[], accessToken?: string, body?: BodyInit) {
  const headers = new Headers();
  const contentType = request.headers.get("content-type");
  if (contentType && !contentType.includes("multipart/form-data")) {
    headers.set("Content-Type", contentType);
  } else if (typeof body === "string") {
    headers.set("Content-Type", "application/json");
  }
  if (accessToken) {
    headers.set("Authorization", `Bearer ${accessToken}`);
  }

  const url = new URL(`${API_BASE_URL}/api/v1/${path.join("/")}`);
  request.nextUrl.searchParams.forEach((value, key) => url.searchParams.set(key, value));

  return fetch(url, {
    method: request.method,
    headers,
    body,
    cache: "no-store",
  });
}

async function handler(request: NextRequest, context: RouteContext) {
  const { path } = await context.params;
  const last = path.join("/");
  let body = (await readBody(request)) as BodyInit | undefined;
  const refreshToken = request.cookies.get(REFRESH_COOKIE)?.value;

  if (last === "auth/logout" && refreshToken) {
    body = JSON.stringify({ refreshToken });
  }

  let accessToken = request.cookies.get(ACCESS_COOKIE)?.value;
  let upstream: Response;
  try {
    upstream = await forward(request, path, accessToken, body);
  } catch {
    return jsonError("Backend service is unavailable. Please try again later.", 503);
  }
  let refreshed: AuthPayload | null = null;
  let refreshAttempted = false;

  if (upstream.status === 401 && accessToken) {
    refreshAttempted = true;
    refreshed = await refreshAccessToken(request);
    accessToken = refreshed?.data?.accessToken;
    if (accessToken) {
      try {
        upstream = await forward(request, path, accessToken, body);
      } catch {
        const response = jsonError("Backend service is unavailable. Please try again later.", 503);
        clearAuth(response);
        return response;
      }
    }
  }

  const upstreamContentType = upstream.headers.get("content-type") ?? "application/json";
  if (upstreamContentType.includes("text/event-stream")) {
    const response = new NextResponse(upstream.body, {
      status: upstream.status,
      headers: {
        "Content-Type": upstreamContentType,
        "Cache-Control": "no-cache, no-transform",
        Connection: "keep-alive",
      },
    });
    if (refreshed) {
      applyAuthCookies(response, refreshed);
    }
    if (upstream.status === 401 || (refreshAttempted && !refreshed)) {
      clearAuth(response);
    }
    return response;
  }

  const text = await upstream.text();
  const response = new NextResponse(text || null, {
    status: upstream.status,
    headers: {
      "Content-Type": upstreamContentType,
    },
  });

  if (refreshed) {
    applyAuthCookies(response, refreshed);
  }

  if (text && ["auth/login", "auth/register", "auth/refresh"].includes(last)) {
    const payload = parseJson<AuthPayload>(text);
    if (payload) {
      applyAuthCookies(response, payload);
    }
  }
  if (last === "auth/logout") {
    clearAuth(response);
  }
  if (upstream.status === 401 || (refreshAttempted && !refreshed)) {
    clearAuth(response);
  }

  return response;
}

export const GET = handler;
export const POST = handler;
export const PUT = handler;
export const PATCH = handler;
export const DELETE = handler;
