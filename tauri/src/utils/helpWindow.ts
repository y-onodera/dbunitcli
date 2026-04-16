import { WebviewWindow } from "@tauri-apps/api/webviewWindow";

export async function openHelpWindow(command: string, label: string) {
	const windowLabel = `help-${command}`;
	const existing = await WebviewWindow.getByLabel(windowLabel);
	if (existing !== null) {
		await existing.setFocus();
		return;
	}
	const webview = new WebviewWindow(windowLabel, {
		url: `/help/${command}.html`,
		title: `Help - ${label}`,
		width: 900,
		height: 700,
	});
	webview.once("tauri://error", console.error);
}
