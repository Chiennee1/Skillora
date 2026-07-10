import { spawnSync } from "node:child_process";

const env = {
  ...process.env,
  NODE_OPTIONS: process.env.NODE_OPTIONS ?? "--max-old-space-size=2048",
};

const result = spawnSync("npx", ["playwright", "test", ...process.argv.slice(2)], {
  env,
  stdio: "inherit",
  shell: process.platform === "win32",
});

process.exit(result.status ?? 1);
