// ブラウザで画面を出すための @tauri-apps/api の代替。撮影に使わないコマンドは何もしない
export const core = {
	invoke: async (cmd: string): Promise<unknown> => {
		if (cmd === "sidecar_logs") {
			// ログのポーリングが空回りしないよう、ログなしの応答を遅らせて返す
			await new Promise((resolve) => setTimeout(resolve, 1000));
			return [0, []];
		}
		return undefined;
	},
};
