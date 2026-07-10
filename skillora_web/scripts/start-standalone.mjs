import { cpSync, existsSync, rmSync } from "node:fs";
import { createRequire } from "node:module";
import { dirname, join } from "node:path";
import { fileURLToPath } from "node:url";

const require = createRequire(import.meta.url);
const root = join(dirname(fileURLToPath(import.meta.url)), "..");
const standaloneDir = join(root, ".next", "standalone");

function copyDir(source, target) {
  if (!existsSync(source)) {
    return;
  }
  rmSync(target, { recursive: true, force: true });
  cpSync(source, target, { recursive: true });
}

copyDir(join(root, ".next", "static"), join(standaloneDir, ".next", "static"));
copyDir(join(root, "public"), join(standaloneDir, "public"));

process.env.PORT ??= "3000";
process.env.HOSTNAME ??= "127.0.0.1";

require(join(standaloneDir, "server.js"));
