import { type ReactNode, useEffect, useRef, useState } from "react";
import { BlueSettingButton } from "./ButtonIcon";

export default function DropDownMenu({
	children,
	className,
}: {
	children: (closeMenu: () => void) => ReactNode;
	className?: string;
}) {
	const [showMenu, setShowMenu] = useState(false);
	const [menuPosition, setMenuPosition] = useState<"right" | "left">("right");
	const buttonRef = useRef<HTMLDivElement>(null);
	// React のイベントはポータルを越えて伝播するため、メニュー配下から
	// createPortal されたダイアログ内の操作も「内側」として判定できる
	const insideMouseDownRef = useRef(false);

	useEffect(() => {
		if (showMenu && buttonRef.current) {
			const rect = buttonRef.current.getBoundingClientRect();
			const menuWidth = 96;
			if (rect.right + menuWidth > window.innerWidth) {
				setMenuPosition("left");
			} else {
				setMenuPosition("right");
			}
		}
		function handleClickOutside() {
			if (insideMouseDownRef.current) {
				insideMouseDownRef.current = false;
				return;
			}
			setShowMenu(false);
		}
		if (showMenu) {
			insideMouseDownRef.current = false;
			document.addEventListener("mousedown", handleClickOutside);
		}
		return () => {
			document.removeEventListener("mousedown", handleClickOutside);
		};
	}, [showMenu]);

	const closeMenu = () => setShowMenu(false);
	const toggleMenu = () => setShowMenu((prev) => !prev);

	return (
		<div
			className={`relative${className ? ` ${className}` : ""}`}
			ref={buttonRef}
			onMouseDownCapture={() => {
				insideMouseDownRef.current = true;
			}}
		>
			<BlueSettingButton handleClick={toggleMenu} />
			{showMenu && (
				<div
					className="absolute z-modal p-4 text-content bg-surface border border-border-faint rounded-lg shadow-dropdown"
					style={
						menuPosition === "right"
							? { left: "100%", top: 0 }
							: { right: "100%", top: 0 }
					}
				>
					<ul className="space-y-4">{children(closeMenu)}</ul>
				</div>
			)}
		</div>
	);
}
