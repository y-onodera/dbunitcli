// ヘルプの画面イメージを撮るため、Tauri なしでブラウザに画面を出す設定。Tauri の API を stubs/ に差し替え、sidecar へは proxy で送る
import { fileURLToPath } from "node:url";
import {
	type ConfigEnv,
	defineConfig,
	mergeConfig,
	type UserConfig,
} from "vite";
import base from "../vite.config";

const stub = (name: string) =>
	fileURLToPath(new URL(`./stubs/${name}.ts`, import.meta.url));

const sidecarPort = process.env.SIDECAR_PORT ?? "8080";

export default defineConfig(async (env: ConfigEnv) => {
	const baseConfig = await (base as (env: ConfigEnv) => Promise<UserConfig>)(
		env,
	);
	return mergeConfig(baseConfig, {
		root: fileURLToPath(new URL("..", import.meta.url)),
		resolve: {
			alias: [
				{ find: /^@tauri-apps\/api$/, replacement: stub("api") },
				{ find: /^@tauri-apps\/api\/path$/, replacement: stub("path") },
				{
					find: /^@tauri-apps\/api\/webviewWindow$/,
					replacement: stub("webviewWindow"),
				},
				{ find: /^@tauri-apps\/plugin-cli$/, replacement: stub("plugin-cli") },
				{
					find: /^@tauri-apps\/plugin-dialog$/,
					replacement: stub("plugin-dialog"),
				},
				{
					find: /^@tauri-apps\/plugin-http$/,
					replacement: stub("plugin-http"),
				},
			],
		},
		server: {
			port: 1430,
			proxy: {
				"/dbunit-cli": `http://localhost:${sidecarPort}`,
			},
		},
	});
});
