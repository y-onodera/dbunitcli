import { useEffect, useRef, useState } from "react";
import { WebviewWindow } from "@tauri-apps/api/webviewWindow";
import { ButtonIcon } from "../../components/element/ButtonIcon";
import { HelpIcon } from "../../components/element/Icon";

const helpCommands = [
	{ command: "convert", label: "Convert", description: "Convert data between formats" },
	{ command: "compare", label: "Compare", description: "Compare datasets and detect differences" },
	{ command: "generate", label: "Generate", description: "Generate files from templates" },
	{ command: "run", label: "Run", description: "Execute SQL or scripts" },
	{ command: "parameterize", label: "Parameterize", description: "Batch process with parameters" },
] as const;

async function openHelpWindow(command: string, label: string) {
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

export default function SidebarHelpLink() {
	const [showMenu, setShowMenu] = useState(false);
	const wrapperRef = useRef<HTMLDivElement>(null);

	useEffect(() => {
		if (!showMenu) {
			return;
		}
		function handleClickOutside(event: Event) {
			if (
				wrapperRef.current &&
				!wrapperRef.current.contains(event.target as HTMLElement)
			) {
				setShowMenu(false);
			}
		}
		document.addEventListener("mousedown", handleClickOutside);
		return () => document.removeEventListener("mousedown", handleClickOutside);
	}, [showMenu]);

	return (
		<div ref={wrapperRef} className="relative">
			<ButtonIcon title="Help" handleClick={() => setShowMenu((prev) => !prev)}>
				<HelpIcon />
			</ButtonIcon>
			{showMenu && (
				<div className="absolute bottom-full left-0 mb-1 z-50 p-2 bg-white border border-gray-100 rounded-lg shadow-md w-64">
					<ul className="space-y-1">
						{helpCommands.map((item) => (
							<li key={item.command}>
								<button
									type="button"
									className="w-full text-left px-3 py-2 rounded hover:bg-gray-100 transition duration-100 ring-indigo-300 focus-visible:ring-3"
									onClick={() => {
										openHelpWindow(item.command, item.label);
										setShowMenu(false);
									}}
								>
									<span className="block text-sm font-semibold text-gray-700">{item.label}</span>
									<span className="block text-xs text-gray-500">{item.description}</span>
								</button>
							</li>
						))}
					</ul>
				</div>
			)}
		</div>
	);
}
