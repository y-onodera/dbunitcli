import {
	type CSSProperties,
	type ReactNode,
	useLayoutEffect,
	useRef,
	useState,
} from "react";
import { createPortal } from "react-dom";
import { BlueSettingButton } from "./ButtonIcon";

// スクロール領域に隠れないよう fixed で画面基準に置く。ボタンの右に収まらなければ左へ、下にはみ出すなら上へずらす
function menuPosition(
	button: DOMRect,
	menu: { width: number; height: number },
): CSSProperties {
	const top = Math.max(
		0,
		Math.min(button.top, window.innerHeight - menu.height),
	);
	if (button.right + menu.width > window.innerWidth) {
		return { right: window.innerWidth - button.left, top };
	}
	return { left: button.right, top };
}

// スクロールのたびに呼ばれるため、位置が変わらなければ同じオブジェクトを返して再描画を避ける
function keepIfSame(prev: CSSProperties, next: CSSProperties): CSSProperties {
	return prev.top === next.top &&
		prev.left === next.left &&
		prev.right === next.right &&
		prev.visibility === next.visibility
		? prev
		: next;
}

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
	const menuRef = useRef<HTMLDivElement>(null);
	const [portalTarget, setPortalTarget] = useState<HTMLElement | null>(null);
	// React のイベントはポータルを越えて伝播するため、メニュー配下から
	// createPortal されたダイアログ内の操作も「内側」として判定できる
	const insideMouseDownRef = useRef(false);

	// 位置はメニューの実寸で決めるため、描画後・表示前に計算する
	useLayoutEffect(() => {
		// 寸法はスクロールでは変わらないため開いたときに1回だけ測る
		const menuSize = menuRef.current?.getBoundingClientRect();
		function updatePosition() {
			if (buttonRef.current && menuSize) {
				const next = menuPosition(
					buttonRef.current.getBoundingClientRect(),
					menuSize,
				);
				setMenuStyle((prev) => keepIfSame(prev, next));
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
			updatePosition();
			document.addEventListener("mousedown", handleClickOutside);
			// スクロールしても閉じず追従させる (メニュー配下のダイアログごと消えるため)
			window.addEventListener("scroll", updatePosition, true);
			window.addEventListener("resize", updatePosition);
		}
		return () => {
			document.removeEventListener("mousedown", handleClickOutside);
			window.removeEventListener("scroll", updatePosition, true);
			window.removeEventListener("resize", updatePosition);
		};
	}, [showMenu]);

	const closeMenu = () => setShowMenu(false);
	const toggleMenu = () => {
		if (!showMenu) {
			// showModal() 済みの dialog は top layer に載り z-index で越えられないため、
			// dialog 内から開いた場合はその dialog 配下に描画する
			setPortalTarget(buttonRef.current?.closest("dialog") ?? document.body);
			setMenuStyle({ visibility: "hidden" });
		}
		setShowMenu(!showMenu);
	};

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
