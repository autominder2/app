#!/usr/bin/env node
/**
 * Launches the Stitch MCP proxy with a freshly minted Google access token.
 *
 * The proxy refuses to start without STITCH_ACCESS_TOKEN or STITCH_API_KEY, and
 * gcloud access tokens expire after roughly an hour. Minting one at launch keeps
 * the credential out of the committed .mcp.json entirely — nothing secret is
 * written to disk, and a new token is issued every time Claude Code starts the
 * server.
 *
 * Auth lives in the isolated SDK that stitch-mcp installed under
 * ~/.stitch-mcp, not in any system-wide gcloud.
 *
 * If `npx @_davideast/stitch-mcp init` later configures a self-refreshing
 * credential, point .mcp.json straight at `npx @_davideast/stitch-mcp proxy`
 * and delete this file.
 */
import { execSync, spawn } from "node:child_process";
import { existsSync } from "node:fs";
import { homedir } from "node:os";
import { join } from "node:path";

const PROJECT_ID = process.env.STITCH_PROJECT_ID || "autominder-stitch";
const stitchHome = join(homedir(), ".stitch-mcp");
const cloudsdkConfig = join(stitchHome, "config");
const isWindows = process.platform === "win32";
const gcloud = join(
    stitchHome,
    "google-cloud-sdk",
    "bin",
    isWindows ? "gcloud.cmd" : "gcloud"
);

const fail = (message) => {
    // stderr only — stdout is the MCP transport and must stay pure JSON-RPC.
    process.stderr.write(`stitch-proxy: ${message}\n`);
    process.exit(1);
};

if (!existsSync(gcloud)) {
    fail(`bundled gcloud missing at ${gcloud}. Run: npx @_davideast/stitch-mcp init`);
}

// gcloud and npx are batch files on Windows, so both are invoked through a
// shell. Each command is a fixed string with no interpolated user input —
// passing an args array alongside `shell` is what Node deprecates (DEP0190),
// since those arguments would be concatenated unescaped.
let token;
try {
    token = execSync(`"${gcloud}" auth print-access-token`, {
        env: { ...process.env, CLOUDSDK_CONFIG: cloudsdkConfig },
        encoding: "utf8",
        stdio: ["ignore", "pipe", "pipe"],
    }).trim();
} catch (error) {
    fail(`could not mint an access token. Run: npx @_davideast/stitch-mcp doctor\n${error.stderr || error.message}`);
}

if (!token) {
    fail("gcloud returned an empty token. Run: npx @_davideast/stitch-mcp doctor");
}

const proxy = spawn("npx @_davideast/stitch-mcp proxy", {
    env: {
        ...process.env,
        CLOUDSDK_CONFIG: cloudsdkConfig,
        STITCH_ACCESS_TOKEN: token,
        STITCH_PROJECT_ID: PROJECT_ID,
    },
    stdio: "inherit",
    shell: true,
});

proxy.on("exit", (code, signal) => process.exit(signal ? 1 : code ?? 0));
proxy.on("error", (error) => fail(`failed to start proxy: ${error.message}`));
