import { readdirSync, readFileSync, statSync } from "node:fs";
import { join, relative } from "node:path";

const root = join(process.cwd(), "src");
const blocked = /\b(mockData|mockCourse|mockUser|fakeData|sampleData|demoData|hardcodedData)\b/;
const checkedExtensions = new Set([".ts", ".tsx"]);
const offenders = [];

function extname(path) {
  const index = path.lastIndexOf(".");
  return index >= 0 ? path.slice(index) : "";
}

function walk(dir) {
  for (const entry of readdirSync(dir)) {
    const path = join(dir, entry);
    const stat = statSync(path);
    if (stat.isDirectory()) {
      walk(path);
      continue;
    }
    if (!checkedExtensions.has(extname(path))) {
      continue;
    }
    const text = readFileSync(path, "utf8");
    if (blocked.test(text)) {
      offenders.push(relative(process.cwd(), path));
    }
  }
}

walk(root);

if (offenders.length > 0) {
  console.error("Production source contains blocked mock/sample data identifiers:");
  offenders.forEach((file) => console.error(`- ${file}`));
  process.exit(1);
}

console.log("No production source mock data identifiers found.");
