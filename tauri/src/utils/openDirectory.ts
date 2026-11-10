import { core } from "@tauri-apps/api";
import { reportError } from "./fetchUtils";

// OSのファイラーで開く。失敗したら理由を表示する
export async function openDirectory(path: string): Promise<void> {
	try {
		await core.invoke("open_directory", { path });
	} catch (ex) {
		reportError("Failed to open directory", ex);
	}
}
