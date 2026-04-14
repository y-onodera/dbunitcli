import { WebviewWindow } from "@tauri-apps/api/webviewWindow";
import { ButtonIcon } from "../../components/element/ButtonIcon";
import { HelpIcon } from "../../components/element/Icon";

export default function SidebarHelpLink() {
	const handleOpenHelp = async () => {
		const existing = await WebviewWindow.getByLabel("help");
		if (existing !== null) {
			await existing.setFocus();
			return;
		}
		const webview = new WebviewWindow("help", {
			url: "/help/index.html",
			title: "Help",
			width: 900,
			height: 700,
		});
		webview.once("tauri://error", console.error);
	};
	return (
		<ButtonIcon title="Help" handleClick={handleOpenHelp}>
			<HelpIcon />
		</ButtonIcon>
	);
}
