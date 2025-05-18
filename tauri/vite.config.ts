/// <reference types="vitest" />
import react from "@vitejs/plugin-react";
import { defineConfig } from "vite";

// https://vitejs.dev/config/
export default defineConfig(async () => ({
	plugins: [react()],
	build: {
		target: "esnext",
	},
	// Vite options tailored for Tauri development and only applied in `tauri dev` or `tauri build`
	//
	// 1. prevent vite from obscuring rust errors
	clearScreen: false,
	// 2. tauri expects a fixed port, fail if that port is not available
	server: {
		port: 1420,
		strictPort: true,
		watch: {
			// 3. tell vite to ignore watching `src-tauri`
			ignored: ["**/src-tauri/**"],
		},
	},
	// Vitestの設定
	test: {
		globals: true, // グローバルなテストAPI（describe, it, expect）を有効化
		environment: "jsdom", // テスト環境としてjsdomを使用
		setupFiles: ["./src/tests/setup.tsx"], // テストのセットアップファイル
		include: ["src/**/*.{test,spec}.{ts,tsx}"], // テスト対象ファイル
		mockReset: true, // 各テスト前にモックをリセット
	},
}));
