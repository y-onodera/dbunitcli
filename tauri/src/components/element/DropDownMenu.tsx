import {
	type ReactNode,
	useEffect,
	useRef,
	useState,
} from "react";
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
	const menuRef = useRef<HTMLDivElement>(null);

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
		function handleClickOutside(event: MouseEvent) {
			if (
				menuRef.current &&
				!menuRef.current.contains(event.target as Node) &&
				buttonRef.current &&
				!buttonRef.current.contains(event.target as Node)
			) {
				setShowMenu(false);
			}
		}
		if (showMenu) {
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
		>
			<BlueSettingButton handleClick={toggleMenu} />
			{showMenu && (
				<div
					ref={menuRef}
					className="absolute z-50 p-4 text-gray-900 bg-white border border-gray-100 rounded-lg shadow-md"
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
