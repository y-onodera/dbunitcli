import "../App.css";
import { useRef, useState } from "react";
import NameEditMenu from "./sidebar/NameEditMenu";
import NamedParameters from "./sidebar/NamedParameters";

interface SidebarProps {
	setSidebarWidth: (width: number) => void;
}

export default function Sidebar({ setSidebarWidth }: SidebarProps) {
	const sidebarRef = useRef(null);
	const [width, setWidth] = useState(200);
	const minWidth = 200;
	const maxWidth = 600;

	const handleMouseDown = (e: { clientX: number; }) => {
		const startX = e.clientX;
		const startWidth = width;

		const handleMouseMove = (e: { clientX: number; }) => {
			const newWidth = startWidth + (e.clientX - startX);
			if (newWidth >= minWidth && newWidth <= maxWidth) {
				setWidth(newWidth);
				setSidebarWidth(newWidth);
			}
		};

		const handleMouseUp = () => {
			document.removeEventListener("mousemove", handleMouseMove);
			document.removeEventListener("mouseup", handleMouseUp);
		};

		document.addEventListener("mousemove", handleMouseMove);
		document.addEventListener("mouseup", handleMouseUp);
	};

	return (
		<div
			ref={sidebarRef}
			style={{ width: `${width}px` }}
			className="h-full overflow-y-auto"
		>
			<div className="h-full px-3 pb-4 pt-4">
				<NameEditMenu />
				<NamedParameters />
			</div>
			<div
				onMouseDown={handleMouseDown}
				style={{
					width: "5px",
					cursor: "col-resize",
					position: "absolute",
					top: 0,
					right: 0,
					bottom: 0,
				}}
			/>
		</div>
	);
}
