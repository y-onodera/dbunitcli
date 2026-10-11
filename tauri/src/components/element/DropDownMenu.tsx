import {
	type CSSProperties,
	type ReactNode,
	useEffect,
	useRef,
	useState,
} from "react";
import { createPortal } from "react-dom";
import { BlueSettingButton } from "./ButtonIcon";

export default function DropDownMenu({
	children,
	className,
}: {
	children: (closeMenu: () => void) => ReactNode;
	className?: string;
}) {
	const [showMenu, setShowMenu] = useState(false);
	const [menuStyle, setMenuStyle] = useState<CSSProperties>({});
	const buttonRef = useRef<HTMLDivElement>(null);
	const [portalTarget, setPortalTarget] = useState<HTMLElement | null>(null);
	const menuRef = useRef<HTMLDivElement>(null);
	// React のイベントはポータルを越えて伝播するため、メニュー配下から
	// createPortal されたダイアログ内の操作も「内側」として判定できる
	const insideMouseDownRef = useRef(false);

	useEffect(() => {
		if (showMenu && buttonRef.current) {
			const rect = buttonRef.current.getBoundingClientRect();
			const menuWidth = 96;
			// showModal() 済みの dialog は top layer に載り z-index で越えられないため、
			// dialog 内から開いた場合はその dialog 配下に描画する
			setPortalTarget(buttonRef.current.closest("dialog") ?? document.body);
			// スクロール領域に隠れないよう fixed で画面基準に配置する
			if (rect.right + menuWidth > window.innerWidth) {
				setMenuStyle({ right: window.innerWidth - rect.left, top: rect.top });
			} else {
				setMenuStyle({ left: rect.right, top: rect.top });
			}
		}
		function handleScroll(e: Event) {
			if (!menuRef.current?.contains(e.target as Node)) {
				setShowMenu(false);
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
			window.addEventListener("scroll", handleScroll, true);
		}
		return () => {
			document.removeEventListener("mousedown", handleClickOutside);
			window.removeEventListener("scroll", handleScroll, true);
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
			{showMenu &&
				portalTarget &&
				createPortal(
					<div
						ref={menuRef}
						className="fixed z-modal-nested p-4 text-content bg-surface border border-border-faint rounded-lg shadow-dropdown"
						style={menuStyle}
					>
						<ul className="space-y-4">{children(closeMenu)}</ul>
					</div>,
					portalTarget,
				)}
		</div>
	);
}
