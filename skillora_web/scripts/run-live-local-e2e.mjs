import { spawnSync } from "node:child_process";

const env = {
  ...process.env,
  NODE_OPTIONS: process.env.NODE_OPTIONS ?? "--max-old-space-size=1024",
  SKILLORA_E2E_LIVE: "true",
  SKILLORA_E2E_BACKEND_URL: process.env.SKILLORA_E2E_BACKEND_URL ?? "http://127.0.0.1:8080",
  SKILLORA_E2E_PASSWORD: process.env.SKILLORA_E2E_PASSWORD ?? "Skillora@12345",
};

const result = spawnSync(
  "npx",
  ["playwright", "test", "tests/e2e/live-backend.spec.ts", "--workers=1"],
  {
    env,
    stdio: "inherit",
    shell: process.platform === "win32",
  },
);

process.exit(result.status ?? 1);
