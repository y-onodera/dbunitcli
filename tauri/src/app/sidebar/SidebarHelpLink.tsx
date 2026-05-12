import { useEffect, useRef, useState } from "react";
import { Button } from "../../components/element/Button";
import { ButtonIcon } from "../../components/element/ButtonIcon";
import { HelpIcon } from "../../components/element/Icon";
import { openHelpWindow } from "../../utils/helpWindow";

const helpCommands = [
	{ command: "convert", label: "Convert", description: "Convert data between formats" },
	{ command: "compare", label: "Compare", description: "Compare datasets and detect differences" },
	{ command: "generate", label: "Generate", description: "Generate files from templates" },
	{ command: "run", label: "Run", description: "Execute SQL or scripts" },
	{ command: "parameterize", label: "Parameterize", description: "Batch process with parameters" },
] as const;

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
				<div className="absolute bottom-full left-0 mb-1 z-modal p-2 bg-surface border border-border-subtle rounded-lg shadow-dropdown w-64">
					<ul className="space-y-1">
						{helpCommands.map((item) => (
							<li key={item.command}>
								<Button
									buttonstyle="w-full text-left px-3 py-2"
									bgcolor="hover:bg-surface-subtle"
									textstyle=""
									border="outline-hidden"
									handleClick={() => {
										openHelpWindow(item.command, item.label);
										setShowMenu(false);
									}}
								>
									<span className="block text-sm font-semibold text-content">{item.label}</span>
									<span className="block text-xs text-content-secondary">{item.description}</span>
								</Button>
							</li>
						))}
					</ul>
				</div>
			)}
		</div>
	);
}
