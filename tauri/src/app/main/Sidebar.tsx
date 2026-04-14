import { useRef, useState } from "react";
import NamedParameters from "../sidebar/NamedParameters";
import NameEditMenu from "../sidebar/NameEditMenu";
import SidebarHelpLink from "../sidebar/SidebarHelpLink";

interface SidebarProps {
	setSidebarWidth: (width: number) => void;
}
export type EditName = {
	name: string;
	command: string;
	x: number;
	y: number;
	afterEdge: boolean;
};
export default function Sidebar({ setSidebarWidth }: SidebarProps) {
	const [editName, setEditName] = useState<EditName>({} as EditName);
	const sidebarRef = useRef(null);
	const [width, setWidth] = useState(200);
	const minWidth = 200;
	const maxWidth = 600;

	const handleMouseDown = (e: { clientX: number }) => {
		const startX = e.clientX;
		const startWidth = width;

		const handleMouseMove = (e: { clientX: number }) => {
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
			className="flex flex-col h-full"
		>
			<div className="flex-1 overflow-y-auto px-3 pt-4 pb-2">
				<NameEditMenu editName={editName} setEditName={setEditName} />
				<NamedParameters setEditName={setEditName} />
			</div>
			<div className="border-t border-gray-200 px-3 py-2">
				<SidebarHelpLink />
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
